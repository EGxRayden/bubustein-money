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
import net.minecraft.world.item.ItemStack;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.bank.BankAccount;
import tk.bubustein.money.bank.BankAccountSavedData;
import tk.bubustein.money.item.ModItems;
import tk.bubustein.money.util.CardUtils;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AdminCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bubustein")
                .then(Commands.literal("ecoAddMoney")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(context -> ecoAddMoney(
                                        context.getSource(),
                                        DoubleArgumentType.getDouble(context, "amount"),
                                        null))
                                .then(Commands.argument("currency", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String input = builder.getRemaining().toLowerCase();
                                            ModItems.EXCHANGE_RATES.keySet().stream()
                                                    .filter(c -> c.toLowerCase().startsWith(input))
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> ecoAddMoney(
                                                context.getSource(),
                                                DoubleArgumentType.getDouble(context, "amount"),
                                                StringArgumentType.getString(context, "currency"))))))

                .then(Commands.literal("ecoSetMoney")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(context -> ecoSetMoney(
                                        context.getSource(),
                                        DoubleArgumentType.getDouble(context, "amount"),
                                        null))
                                .then(Commands.argument("currency", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String input = builder.getRemaining().toLowerCase();
                                            ModItems.EXCHANGE_RATES.keySet().stream()
                                                    .filter(c -> c.toLowerCase().startsWith(input))
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> ecoSetMoney(
                                                context.getSource(),
                                                DoubleArgumentType.getDouble(context, "amount"),
                                                StringArgumentType.getString(context, "currency"))))))

                .then(Commands.literal("resetMoney")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> resetMoney(context.getSource())))

                .then(Commands.literal("invalidateAll")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("uuid", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    MinecraftServer server = context.getSource().getServer();
                                    String input = builder.getRemaining().toLowerCase();
                                    for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                                        String uuidStr = p.getUUID().toString();
                                        String name = p.getGameProfile().getName();
                                        if (uuidStr.toLowerCase().startsWith(input) || name.toLowerCase().startsWith(input)) {
                                            builder.suggest(uuidStr, Component.literal(name));
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> invalidateAll(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "uuid")))))
        );
    }
    private static int ecoAddMoney(CommandSourceStack source, double amount, String specifiedCurrency) throws CommandSyntaxException {
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
        if (amount <= 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_positive").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (ModCommands.hasMoreThanTwoDecimals(amount)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.two_decimals").withStyle(ChatFormatting.RED));
            return 0;
        }

        String accountCurrency = acc.getCurrency();
        String addCurrency = (specifiedCurrency != null) ? specifiedCurrency : accountCurrency;

        if (!ModItems.EXCHANGE_RATES.containsKey(addCurrency)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_currency",
                    String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
            return 0;
        }

        BigDecimal current = BigDecimal.valueOf(acc.getBalance());
        BigDecimal add = BigDecimal.valueOf(amount);
        if (current.add(add).compareTo(ModCommands.MAX_AMOUNT) > 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large",
                    CardUtils.formatMoney(ModCommands.MAX_AMOUNT.doubleValue())).withStyle(ChatFormatting.RED));
            return 0;
        }

        double amountInAccountCurrency = ModCommands.convertCurrency(amount, addCurrency, accountCurrency);
        acc.setBalance(acc.getBalance() + amountInAccountCurrency);
        ModCommands.syncCardWithAccount(stack, acc);

        boolean isAdminAction = !acc.getOwnerUuid().equals(player.getUUID());

        if (isAdminAction) {
            MoneyMod.LOGGER.warn("[ADMIN ACTION] {} added {} {} to card {} (owner: {})",
                    player.getName().getString(), CardUtils.formatMoney(amount), addCurrency,
                    acc.getIban(), acc.getOwnerUuid());

            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.admin.amount_added",
                            CardUtils.formatMoney(amount), addCurrency, CardUtils.formatMoney(acc.getBalance()), accountCurrency)
                    .withStyle(ChatFormatting.GOLD));
            ServerPlayer owner = ModCommands.getCardOwner(source.getServer(), acc);
            if (owner != null) {
                owner.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.admin.your_card_added",
                                player.getName().getString(), CardUtils.formatMoney(amountInAccountCurrency), accountCurrency)
                        .withStyle(ChatFormatting.GREEN));
            }
        } else {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_added",
                    CardUtils.formatMoney(amount), addCurrency,
                    CardUtils.formatMoney(acc.getBalance()), accountCurrency).withStyle(ChatFormatting.GREEN));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int ecoSetMoney(CommandSourceStack source, double amount, String specifiedCurrency) throws CommandSyntaxException {
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
        if (amount <= 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_positive").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (ModCommands.hasMoreThanTwoDecimals(amount)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.two_decimals").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (BigDecimal.valueOf(amount).compareTo(ModCommands.MAX_AMOUNT) > 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large",
                    CardUtils.formatMoney(ModCommands.MAX_AMOUNT.doubleValue())).withStyle(ChatFormatting.RED));
            return 0;
        }

        String accountCurrency = acc.getCurrency();
        String setCurrency = (specifiedCurrency != null) ? specifiedCurrency : accountCurrency;

        if (!ModItems.EXCHANGE_RATES.containsKey(setCurrency)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_currency",
                    String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
            return 0;
        }

        double amountInAccountCurrency = ModCommands.convertCurrency(amount, setCurrency, accountCurrency);
        acc.setBalance(amountInAccountCurrency);
        ModCommands.syncCardWithAccount(stack, acc);

        boolean isAdminAction = !acc.getOwnerUuid().equals(player.getUUID());

        if (isAdminAction) {
            MoneyMod.LOGGER.warn("[ADMIN ACTION] {} set card {} balance to {} {} (owner: {})",
                    player.getName().getString(), acc.getIban(),
                    CardUtils.formatMoney(amount), setCurrency, acc.getOwnerUuid());

            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.admin.amount_set",
                            CardUtils.formatMoney(amount), setCurrency, CardUtils.formatMoney(acc.getBalance()), accountCurrency)
                    .withStyle(ChatFormatting.GOLD));

            ServerPlayer owner = ModCommands.getCardOwner(source.getServer(), acc);
            if (owner != null) {
                owner.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.admin.your_card_set",
                                player.getName().getString(), CardUtils.formatMoney(amountInAccountCurrency), accountCurrency)
                        .withStyle(ChatFormatting.YELLOW));
            }
        } else {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_set",
                    CardUtils.formatMoney(amount), setCurrency,
                    CardUtils.formatMoney(acc.getBalance()), accountCurrency).withStyle(ChatFormatting.GREEN));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int resetMoney(CommandSourceStack source) throws CommandSyntaxException {
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
        String currency = acc.getCurrency();
        boolean isAdminAction = !acc.getOwnerUuid().equals(player.getUUID());

        acc.setBalance(0.0);
        ModCommands.syncCardWithAccount(stack, acc);

        if (isAdminAction) {
            MoneyMod.LOGGER.warn("[ADMIN ACTION] {} reset card {} (owner: {})",
                    player.getName().getString(), acc.getIban(), acc.getOwnerUuid());

            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.admin.card_reset", currency)
                    .withStyle(ChatFormatting.GOLD));
            ServerPlayer owner = ModCommands.getCardOwner(source.getServer(), acc);
            if (owner != null) {
                owner.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.admin.your_card_reset",
                                player.getName().getString(), currency)
                        .withStyle(ChatFormatting.RED));
            }
        } else {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_reset", currency)
                    .withStyle(ChatFormatting.GREEN));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int invalidateAll(CommandSourceStack source, String uuidStr) {
        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Invalid UUID format: " + uuidStr)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        MinecraftServer server = source.getServer();
        BankAccountSavedData data = BankAccountSavedData.get(server);
        Map<String, BankAccount> accounts = data.getAccountsForPlayer(targetUuid);

        if (accounts.isEmpty()) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.invalidateAll.no_accounts", uuidStr
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        int invalidatedCount = 0;
        for (BankAccount acc : accounts.values()) {
            if (acc.isActive()) {
                acc.setActive(false);
                invalidatedCount++;
                MoneyMod.LOGGER.info("[ADMIN INVALIDATE ALL] Invalidated account {} (owner: {}, balance: {} {})",
                        acc.getIban(), acc.getOwnerName(), acc.getBalance(), acc.getCurrency());
            }
        }

        if (invalidatedCount == 0) {
            source.sendSuccess(() -> Component.translatable(
                    "commands.bubusteinmoneymod.invalidateAll.none_active", uuidStr
            ).withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }

        data.setDirty();

        // Notify the target player if online
        ServerPlayer targetPlayer = server.getPlayerList().getPlayer(targetUuid);
        if (targetPlayer != null) {
            targetPlayer.sendSystemMessage(Component.translatable(
                    "message.bubusteinmoneymod.admin.all_cards_invalidated",
                    source.getTextName()
            ).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }

        int finalCount = invalidatedCount;
        source.sendSuccess(() -> Component.translatable(
                "commands.bubusteinmoneymod.invalidateAll.success", finalCount, uuidStr
        ).withStyle(ChatFormatting.GREEN), true);

        MoneyMod.LOGGER.warn("[ADMIN ACTION] {} invalidated ALL {} account(s) for UUID {}",
                source.getTextName(), finalCount, uuidStr);

        return Command.SINGLE_SUCCESS;
    }
}
