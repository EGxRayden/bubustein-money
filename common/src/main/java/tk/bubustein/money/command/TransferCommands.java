/*
 * This file is licensed under the GNU Lesser General Public License v3.0,
 * part of Bubustein's Money Mod.
 * Copyright (c) 2022-2025 BUBUSTEIN (GitHub username: BUBUSTEIN13)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */
package tk.bubustein.money.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.bank.*;
import tk.bubustein.money.item.CardItem;
import tk.bubustein.money.item.CreditCardItem;
import tk.bubustein.money.item.ModItems;
import tk.bubustein.money.util.CardUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class TransferCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bubustein")
                .then(Commands.literal("deposit")
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(context -> deposit(context.getSource(), DoubleArgumentType.getDouble(context, "amount"), null))
                                .then(Commands.argument("currency", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String input = builder.getRemaining().toLowerCase();
                                            ModItems.EXCHANGE_RATES.keySet().stream()
                                                    .filter(c -> c.toLowerCase().startsWith(input))
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> deposit(context.getSource(), DoubleArgumentType.getDouble(context, "amount"), StringArgumentType.getString(context, "currency"))))))
                .then(Commands.literal("transfer")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    MinecraftServer server = context.getSource().getServer();
                                    BankAccountSavedData data = BankAccountSavedData.get(server);
                                    String input = builder.getRemaining().toLowerCase();
                                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                                        UUID uuid = player.getUUID();
                                        boolean hasDebitAccount = data.getAccountsForPlayer(uuid).values().stream()
                                                .anyMatch(acc -> acc.getKind() == AccountKind.DEBIT && acc.isActive());
                                        if (hasDebitAccount) {
                                            String name = player.getGameProfile().getName();
                                            if (name.toLowerCase().startsWith(input)) {
                                                builder.suggest(name);
                                            }
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("iban", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String targetPlayerName = StringArgumentType.getString(context, "player");
                                            MinecraftServer server = context.getSource().getServer();
                                            ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(targetPlayerName);
                                            ServerPlayer sender;
                                            try {
                                                sender = context.getSource().getPlayerOrException();
                                            } catch (CommandSyntaxException e) {
                                                return builder.buildFuture();
                                            }
                                            if (targetPlayer != null) {
                                                BankAccountSavedData data = BankAccountSavedData.get(server);
                                                boolean isSamePlayer = targetPlayer.getUUID().equals(sender.getUUID());
                                                data.getAccountsForPlayer(targetPlayer.getUUID()).values().stream()
                                                        .filter(acc -> {
                                                            if (!acc.isActive()) return false;
                                                            if (isSamePlayer) {
                                                                return acc.getKind() == AccountKind.DEBIT
                                                                        || acc.getKind() == AccountKind.CREDIT
                                                                        || acc.getKind() == AccountKind.SAVINGS;
                                                            } else {
                                                                return acc.getKind() == AccountKind.DEBIT;
                                                            }
                                                        })
                                                        .map(BankAccount::getIban)
                                                        .forEach(builder::suggest);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                                .executes(context -> transfer(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "player"),
                                                        StringArgumentType.getString(context, "iban"),
                                                        DoubleArgumentType.getDouble(context, "amount"),
                                                        null
                                                ))
                                                .then(Commands.argument("currency", StringArgumentType.word())
                                                        .suggests((context, builder) -> {
                                                            String input = builder.getRemaining().toLowerCase();
                                                            ModItems.EXCHANGE_RATES.keySet().stream()
                                                                    .filter(c -> c.toLowerCase().startsWith(input))
                                                                    .forEach(builder::suggest);
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(context -> transfer(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "player"),
                                                                StringArgumentType.getString(context, "iban"),
                                                                DoubleArgumentType.getDouble(context, "amount"),
                                                                StringArgumentType.getString(context, "currency")
                                                        ))
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("withdraw")
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(context -> withdraw(context.getSource(), DoubleArgumentType.getDouble(context, "amount")))))

        );
    }
    private static int transfer(CommandSourceStack source, String targetPlayerName,
                                String targetIban, double amount, String specifiedCurrency)
            throws CommandSyntaxException {

        ServerPlayer sender = source.getPlayerOrException();
        MinecraftServer server = source.getServer();
        BankAccountSavedData data = BankAccountSavedData.get(server);

        if (amount <= 0.0 || ModCommands.hasMoreThanTwoDecimals(amount)) {
            sender.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_positive")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        ItemStack senderCard = sender.getMainHandItem();
        if (!(senderCard.getItem() instanceof CardItem) && !(senderCard.getItem() instanceof CreditCardItem)) {
            sender.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_card")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        Optional<BankAccount> optSenderAcc = ModCommands.getAccountFromCard(senderCard, sender);
        if (optSenderAcc.isEmpty()) {
            return 0;
        }

        BankAccount senderAcc = optSenderAcc.get();

        if (!senderAcc.getOwnerUuid().equals(sender.getUUID())) {
            sender.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_card_owner")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!senderAcc.isActive()) {
            sender.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_not_active")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        Optional<BankAccount> optTargetAcc = data.getByIban(targetIban);
        if (optTargetAcc.isEmpty()) {
            sender.sendSystemMessage(Component.translatable(
                            "message.bubusteinmoneymod.account_not_found", targetIban)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        BankAccount targetAcc = optTargetAcc.get();

        if (targetAcc.getKind() != AccountKind.DEBIT) {
            if (!targetAcc.getOwnerUuid().equals(sender.getUUID())) {
                sender.sendSystemMessage(Component.translatable(
                                "message.bubusteinmoneymod.transfer.only_debit_or_own", targetIban)
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
        }
        if (!targetAcc.isActive()) {
            sender.sendSystemMessage(Component.translatable(
                            "message.bubusteinmoneymod.transfer.targetinactive", targetIban)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (senderAcc.getIban().equals(targetIban)) {
            sender.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.cannot_transfer_same_iban")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(targetPlayerName);
        UUID targetUuid = null;

        if (targetPlayer != null) {
            targetUuid = targetPlayer.getUUID();
        } else {
            for (var entry : data.getAccountsForPlayer(targetAcc.getOwnerUuid()).entrySet()) {
                if (entry.getKey().equals(targetIban)) {
                    targetUuid = entry.getValue().getOwnerUuid();
                    break;
                }
            }
        }
        if (targetUuid == null || !targetUuid.equals(targetAcc.getOwnerUuid())) {
            sender.sendSystemMessage(Component.translatable(
                            "message.bubusteinmoneymod.transfer.playernomatch", targetPlayerName, targetIban)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        String senderCurrency = senderAcc.getCurrency();
        String transferCurrency = specifiedCurrency != null ? specifiedCurrency : senderCurrency;
        String targetCurrency = targetAcc.getCurrency();

        if (!ModItems.EXCHANGE_RATES.containsKey(transferCurrency)) {
            sender.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_currency",
                            String.join(", ", ModItems.EXCHANGE_RATES.keySet()))
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        double amountInSenderCurrency;
        try {
            amountInSenderCurrency = ModCommands.convertCurrency(amount, transferCurrency, senderCurrency);
        } catch (IllegalArgumentException e) {
            sender.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.conversion_failed")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (senderAcc.getBalance() < amountInSenderCurrency) {
            sender.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_enough_funds")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        double amountInTargetCurrency;
        try {
            amountInTargetCurrency = ModCommands.convertCurrency(amount, transferCurrency, targetCurrency);
        } catch (IllegalArgumentException e) {
            sender.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.conversion_failed")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        BigDecimal targetNewBalance = BigDecimal.valueOf(targetAcc.getBalance())
                .add(BigDecimal.valueOf(amountInTargetCurrency));
        if (targetNewBalance.compareTo(ModCommands.MAX_AMOUNT) > 0) {
            sender.sendSystemMessage(Component.translatable(
                            "message.bubusteinmoneymod.amount_too_large",
                            CardUtils.formatMoney(ModCommands.MAX_AMOUNT.doubleValue()))
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        synchronized (senderAcc) {
            synchronized (targetAcc) {
                if (!senderAcc.withdraw(amountInSenderCurrency)) {
                    sender.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_enough_funds")
                            .withStyle(ChatFormatting.RED));
                    return 0;
                }
                boolean targetOnline = (targetPlayer != null);

                if (targetOnline) {
                    targetAcc.deposit(amountInTargetCurrency);
                    ItemStack targetCard = targetPlayer.getMainHandItem();
                    if (targetCard.getItem() instanceof CardItem) {
                        String targetCardIban = CardUtils.getIban(targetCard);
                        if (targetIban.equals(targetCardIban)) {
                            ModCommands.syncCardWithAccount(targetCard, targetAcc);
                        }
                    }
                    targetPlayer.sendSystemMessage(Component.translatable(
                            "message.bubusteinmoneymod.transfer.received",
                            CardUtils.formatMoney(amountInTargetCurrency),
                            targetCurrency,
                            sender.getGameProfile().getName(),
                            targetIban
                    ).withStyle(ChatFormatting.GREEN));

                    MoneyMod.LOGGER.info("[{}] TRANSFER (ONLINE): {} sent {} {} to {} (account {})",
                            MoneyMod.MOD_ID, sender.getGameProfile().getName(),
                            amount, transferCurrency, targetPlayerName, targetIban);
                } else {
                    PendingTransfer pending = new PendingTransfer(
                            targetUuid,
                            targetIban,
                            amountInTargetCurrency,
                            targetCurrency,
                            sender.getGameProfile().getName()
                    );
                    data.addPendingTransfer(pending);
                    sender.sendSystemMessage(Component.translatable(
                            "message.bubusteinmoneymod.transfer.pendingoffline",
                            targetPlayerName
                    ).withStyle(ChatFormatting.YELLOW));

                    MoneyMod.LOGGER.info("[{}] TRANSFER (PENDING): {} sent {} {} to {} (offline, account {})",
                            MoneyMod.MOD_ID, sender.getGameProfile().getName(),
                            amount, transferCurrency, targetPlayerName, targetIban);
                }

                ModCommands.syncCardWithAccount(senderCard, senderAcc);
                sender.sendSystemMessage(Component.translatable(
                        "message.bubusteinmoneymod.transfer.success",
                        CardUtils.formatMoney(amount),
                        transferCurrency,
                        targetPlayerName,
                        targetIban
                ).withStyle(ChatFormatting.GREEN));
                data.setDirty();
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int deposit(CommandSourceStack source, double amount, String specifiedCurrency) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        MinecraftServer server = source.getServer();

        if (amount <= 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_positive")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (BigDecimal.valueOf(amount).compareTo(ModCommands.MAX_AMOUNT) > 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large",
                    CardUtils.formatMoney(ModCommands.MAX_AMOUNT.doubleValue())).withStyle(ChatFormatting.RED));
            return 0;
        }

        Optional<BankAccount> optAcc = ModCommands.getAccountFromCard(stack, player);
        if (optAcc.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_card").withStyle(ChatFormatting.RED));
            return 0;
        }
        BankAccount acc = optAcc.get();

        if (!acc.isActive()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_not_active")
                    .withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_not_active_hint")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }

        String accountCurrency = acc.getCurrency();
        String depositCurrency = (specifiedCurrency != null) ? specifiedCurrency : accountCurrency;

        if (!ModItems.EXCHANGE_RATES.containsKey(depositCurrency)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_currency",
                    String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
            return 0;
        }
        if (ModCommands.hasMoreThanTwoDecimals(amount)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.two_decimals").withStyle(ChatFormatting.RED));
            return 0;
        }

        NavigableMap<Double, Item> items = ModItems.getCurrencyItems().get(depositCurrency);
        if (items == null) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.no_physical_currency", depositCurrency).withStyle(ChatFormatting.RED));
            return 0;
        }

        BigDecimal currentBalance = BigDecimal.valueOf(acc.getBalance());
        BigDecimal addAmount = BigDecimal.valueOf(amount);
        if (acc.getKind() != AccountKind.CREDIT) {
            if (currentBalance.add(addAmount).compareTo(ModCommands.MAX_AMOUNT) > 0) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large",
                        CardUtils.formatMoney(ModCommands.MAX_AMOUNT.doubleValue())).withStyle(ChatFormatting.RED));
                return 0;
            }
        }

        Map<Item, Integer> availableItems = new HashMap<>();
        for (Item item : items.values()) {
            availableItems.put(item, player.getInventory().countItem(item));
        }

        double totalAvailable = 0;
        for (Map.Entry<Double, Item> entry : items.entrySet()) {
            totalAvailable += entry.getKey() * availableItems.get(entry.getValue());
        }

        if (totalAvailable < amount) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_enough_funds")
                    .append(" ")
                    .append(Component.translatable("message.bubusteinmoneymod.available",
                            CardUtils.formatMoney(totalAvailable), depositCurrency))
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        double totalDeposited = 0;
        double remainingAmount = amount;

        for (Map.Entry<Double, Item> entry : items.descendingMap().entrySet()) {
            double denomination = entry.getKey();
            Item item = entry.getValue();
            int countAvailable = availableItems.get(item);
            int countNeeded = (int) Math.min(remainingAmount / denomination, countAvailable);
            if (countNeeded > 0) {
                int actuallyRemoved = ModCommands.removeItemsFromInventory(player, item, countNeeded);
                double depositedAmount = denomination * actuallyRemoved;
                totalDeposited += depositedAmount;
                remainingAmount -= depositedAmount;
            }
            if (remainingAmount < 0.01) break;
        }

        if (totalDeposited > 0) {
            double amountInAccountCurrency = ModCommands.convertCurrency(totalDeposited, depositCurrency, accountCurrency);
            acc.setBalance(acc.getBalance() + amountInAccountCurrency);
            ModCommands.syncCardWithAccount(stack, acc);
            BankAccountSavedData.get(server).setDirty();
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.deposit_success",
                            CardUtils.formatMoney(totalDeposited), depositCurrency,
                            CardUtils.formatMoney(acc.getBalance()), accountCurrency)
                    .withStyle(ChatFormatting.GREEN));
            if (remainingAmount > 0.01) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.deposit_partial",
                                CardUtils.formatMoney(remainingAmount), depositCurrency)
                        .withStyle(ChatFormatting.YELLOW));
            }
        } else {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_enough_funds")
                    .withStyle(ChatFormatting.RED));
        }
        player.inventoryMenu.broadcastChanges();
        return Command.SINGLE_SUCCESS;
    }
    private static int withdraw(CommandSourceStack source, double amount) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        Optional<BankAccount> optAcc = ModCommands.getAccountFromCard(stack, player, true);

        if (optAcc.isEmpty()) {
            return 0;
        }

        BankAccount acc = optAcc.get();

        if (!acc.isActive()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_not_active")
                    .withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_not_active_hint")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }

        if (ModCommands.hasMoreThanTwoDecimals(amount)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.two_decimals")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (amount <= 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_positive")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        String cardCurrency = acc.getCurrency();
        double currentBalance = acc.getBalance();
        boolean isAdminAction = !acc.getOwnerUuid().equals(player.getUUID());

        double fee;
        BigDecimal totalNeeded;

        if (acc.getKind() == AccountKind.CREDIT) {
            if (!acc.canWithdraw(amount)) {
                player.sendSystemMessage(Component.translatable(
                                "message.bubusteinmoneymod.credit_limit_exceeded",
                                CardUtils.formatMoney(amount),
                                cardCurrency)
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            double currentDebtInCardCurrency = Math.max(0.0, -acc.getBalance());
            double newBalance = acc.getBalance() - amount;
            double newDebtInCardCurrency = Math.max(0.0, -newBalance);
            double additionalDebtInCardCurrency = newDebtInCardCurrency - currentDebtInCardCurrency;

            if (additionalDebtInCardCurrency > 0.0) {
                double additionalDebtEur;
                try {
                    additionalDebtEur = ModCommands.convertCurrency(additionalDebtInCardCurrency, cardCurrency, "EUR");
                } catch (IllegalArgumentException e) {
                    additionalDebtEur = additionalDebtInCardCurrency;
                }

                tk.bubustein.money.bank.BankAccountManager mgr = tk.bubustein.money.bank.BankAccountManager.get();
                if (!mgr.canAccumulateMoreDebt(source.getServer(), player.getUUID(), additionalDebtEur)) {
                    double personalCapEur = mgr.getPersonalCreditCapEur(source.getServer(), player.getUUID());
                    double personalCapInCurrency;
                    try {
                        personalCapInCurrency = ModCommands.convertCurrency(personalCapEur, "EUR", cardCurrency);
                    } catch (IllegalArgumentException e) {
                        personalCapInCurrency = personalCapEur;
                    }

                    player.sendSystemMessage(Component.translatable(
                                    "message.bubusteinmoneymod.credit.global_debt_cap",
                                    CardUtils.formatMoney(personalCapInCurrency),
                                    cardCurrency)
                            .withStyle(ChatFormatting.RED));
                    return 0;
                }
            }
        } else {
            if (isAdminAction) {
                fee = 0.0;
                totalNeeded = BigDecimal.valueOf(amount);
            } else {
                fee = ModCommands.calculateWithdrawFee(stack, amount);
                totalNeeded = BigDecimal.valueOf(amount).add(BigDecimal.valueOf(fee));
            }

            if (BigDecimal.valueOf(currentBalance).compareTo(totalNeeded) < 0) {
                if (isAdminAction) {
                    player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_enough_funds")
                            .append(" ")
                            .append(Component.translatable("message.bubusteinmoneymod.available",
                                    CardUtils.formatMoney(currentBalance), cardCurrency))
                            .withStyle(ChatFormatting.RED));
                } else {
                    double feeRate = fee / amount;
                    BigDecimal maxWithdrawable = BigDecimal.valueOf(currentBalance)
                            .divide(BigDecimal.ONE.add(BigDecimal.valueOf(feeRate)), 2, RoundingMode.DOWN);

                    player.sendSystemMessage(Component.translatable(
                                    "message.bubusteinmoneymod.not_enough_funds_with_fee",
                                    CardUtils.formatMoney(maxWithdrawable.doubleValue()), cardCurrency,
                                    CardUtils.formatMoney(ModCommands.calculateWithdrawFee(stack, maxWithdrawable.doubleValue())), cardCurrency)
                            .withStyle(ChatFormatting.RED));
                }
                return 0;
            }
        }
        double remainingAmount;
        if (ModItems.getCurrencyItems().containsKey(cardCurrency)) {
            remainingAmount = ModCommands.withdrawCurrency(player, amount, cardCurrency);
            if (remainingAmount > 0) {
                player.sendSystemMessage(Component.translatable(
                                "message.bubusteinmoneymod.withdraw_partial",
                                CardUtils.formatMoney(remainingAmount), cardCurrency)
                        .withStyle(ChatFormatting.YELLOW));
            }
        } else {
            remainingAmount = amount;
            player.sendSystemMessage(Component.translatable(
                            "message.bubusteinmoneymod.withdraw_no_currency", cardCurrency)
                    .withStyle(ChatFormatting.YELLOW));
        }

        double actuallyWithdrawn = amount - remainingAmount;
        double actualFee = 0.0;

        if (actuallyWithdrawn > 0) {
            if (acc.getKind() == AccountKind.CREDIT) {
                actualFee = 0.0;

                CreditCardTier tier = acc.getCreditCardTier();
                double cardLimitInCardCurrency;
                try {
                    cardLimitInCardCurrency = ModCommands.convertCurrency(
                            tier.getCreditLimit(), "EUR", cardCurrency
                    );
                } catch (IllegalArgumentException e) {
                    cardLimitInCardCurrency = tier.getCreditLimit();
                }

                double postWithdrawBalance = acc.getBalance() - actuallyWithdrawn;
                if (postWithdrawBalance < -cardLimitInCardCurrency) {
                    player.sendSystemMessage(Component.translatable(
                                    "message.bubusteinmoneymod.credit_limit_exceeded",
                                    CardUtils.formatMoney(actuallyWithdrawn), cardCurrency)
                            .withStyle(ChatFormatting.RED));
                    return 0;
                }

                acc.setBalance(postWithdrawBalance);
            } else if (isAdminAction) {
                actualFee = 0.0;
                acc.setBalance(currentBalance - actuallyWithdrawn);
            } else {
                actualFee = ModCommands.calculateWithdrawFee(stack, actuallyWithdrawn);
                double newBalance = currentBalance - actuallyWithdrawn - actualFee;
                acc.setBalance(newBalance);
            }
        }

        ModCommands.syncCardWithAccount(stack, acc);
        BankAccountSavedData.get(source.getServer()).setDirty();

        double finalBalance = acc.getBalance();

        if (isAdminAction && acc.getKind() != AccountKind.CREDIT) {
            MoneyMod.LOGGER.warn("[ADMIN ACTION] {} withdrew {} {} from card {} (owner: {})",
                    player.getName().getString(),
                    CardUtils.formatMoney(actuallyWithdrawn), cardCurrency,
                    acc.getIban(), acc.getOwnerUuid());

            player.sendSystemMessage(Component.translatable(
                            "message.bubusteinmoneymod.admin.withdraw_success",
                            CardUtils.formatMoney(actuallyWithdrawn), cardCurrency,
                            CardUtils.formatMoney(finalBalance), cardCurrency)
                    .withStyle(ChatFormatting.GOLD));

            ServerPlayer owner = ModCommands.getCardOwner(source.getServer(), acc);
            if (owner != null) {
                owner.sendSystemMessage(Component.translatable(
                                "message.bubusteinmoneymod.admin.your_card_withdrawn",
                                player.getName().getString(),
                                CardUtils.formatMoney(actuallyWithdrawn), cardCurrency)
                        .withStyle(ChatFormatting.RED));
            }
        } else {
            player.sendSystemMessage(Component.translatable(
                            "message.bubusteinmoneymod.withdraw_success",
                            CardUtils.formatMoney(actuallyWithdrawn), cardCurrency,
                            CardUtils.formatMoney(actualFee), cardCurrency,
                            CardUtils.formatMoney(finalBalance), cardCurrency)
                    .withStyle(ChatFormatting.GREEN));
        }

        return Command.SINGLE_SUCCESS;
    }

}