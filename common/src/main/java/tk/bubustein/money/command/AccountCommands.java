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
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.bank.*;
import tk.bubustein.money.config.ModConfig;
import tk.bubustein.money.item.CardItem;
import tk.bubustein.money.item.CreditCardItem;
import tk.bubustein.money.item.ModItems;
import tk.bubustein.money.util.CardUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AccountCommands {
    private static final int ACCOUNTS_PAGE_SIZE = 2;
    private static final long INTEREST_PERIOD_TICKS = CreditCardItem.INTEREST_PERIOD_TICKS;
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bubustein")
                .then(Commands.literal("accounts")
                        .executes(context -> showAccounts(context.getSource(), null, 1))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .requires(source -> source.hasPermission(2))
                                .suggests((context, builder) -> {
                                    MinecraftServer server = context.getSource().getServer();
                                    BankAccountSavedData data = BankAccountSavedData.get(server);
                                    String input = builder.getRemaining().toLowerCase();
                                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                                        UUID uuid = player.getUUID();
                                        if (!data.getAccountsForPlayer(uuid).isEmpty()) {
                                            String name = player.getGameProfile().getName();
                                            if (name.toLowerCase().startsWith(input)) {
                                                builder.suggest(name);
                                            }
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> showAccounts(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "player"),
                                        1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(context -> showAccounts(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "player"),
                                                IntegerArgumentType.getInteger(context, "page"))))
                        )
                )
                .then(Commands.literal("createAccount")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("DEBIT");
                                    builder.suggest("CREDIT");
                                    builder.suggest("SAVINGS");
                                    return builder.buildFuture();
                                })
                                .executes(context -> createAccount(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "type"),
                                        null))
                                .then(Commands.argument("bank", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            MinecraftServer server = context.getSource().getServer();
                                            BankAccountManager mgr = BankAccountManager.get();
                                            String input = builder.getRemaining().toUpperCase();
                                            for (String prefix : mgr.getBanks().keySet()) {
                                                if (prefix.toUpperCase().startsWith(input)) {
                                                    builder.suggest(prefix);
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> createAccount(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "type"),
                                                StringArgumentType.getString(context, "bank"))))))

                .then(Commands.literal("deleteAccount")
                        .executes(context -> showDeleteMenu(context.getSource()))
                        .then(Commands.argument("iban", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    MinecraftServer server = context.getSource().getServer();
                                    BankAccountSavedData data = BankAccountSavedData.get(server);
                                    data.getAccountsForPlayer(player.getUUID()).values().stream()
                                            .filter(BankAccount::isBalanceZero)
                                            .map(BankAccount::getIban)
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> deleteAccount(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "iban")))))

                .then(Commands.literal("link")
                        .executes(context -> linkCard(context.getSource()))
                        .then(Commands.argument("iban", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    try {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        MinecraftServer server = context.getSource().getServer();
                                        BankAccountSavedData data = BankAccountSavedData.get(server);
                                        data.getAccountsForPlayer(player.getUUID()).values().stream()
                                                .filter(acc -> !acc.isActive())
                                                .map(BankAccount::getIban)
                                                .forEach(builder::suggest);
                                    } catch (CommandSyntaxException ignored) {}
                                    return builder.buildFuture();
                                })
                                .executes(context -> linkCardToSpecificIban(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "iban")))))

                .then(Commands.literal("invalidate")
                        .executes(context -> showInvalidateMenu(context.getSource()))
                        .then(Commands.argument("iban", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    MinecraftServer server = context.getSource().getServer();
                                    BankAccountSavedData data = BankAccountSavedData.get(server);
                                    data.getAccountsForPlayer(player.getUUID()).values().stream()
                                            .filter(BankAccount::isActive)
                                            .map(BankAccount::getIban)
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> invalidateCardByIban(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "iban")))))
        );
    }
    private static int showAccounts(CommandSourceStack source, String targetPlayerName, int page)
            throws CommandSyntaxException {
        ServerPlayer executor = source.getPlayerOrException();
        MinecraftServer server = source.getServer();
        BankAccountSavedData data = BankAccountSavedData.get(server);

        UUID targetUuid;
        String displayName;

        if (targetPlayerName == null) {
            targetUuid = executor.getUUID();
            displayName = executor.getGameProfile().getName();
        } else {
            if (!source.hasPermission(2)) {
                source.sendFailure(Component.translatable("message.bubusteinmoneymod.no_permission")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
            ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(targetPlayerName);
            if (targetPlayer == null) {
                source.sendFailure(Component.translatable("message.bubusteinmoneymod.player_not_found", targetPlayerName)
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
            targetUuid = targetPlayer.getUUID();
            displayName = targetPlayer.getGameProfile().getName();
        }

        Map<String, BankAccount> accounts = data.getAccountsForPlayer(targetUuid);

        if (accounts.isEmpty()) {
            source.sendSuccess(() ->
                            Component.translatable("message.bubusteinmoneymod.no_accounts", displayName)
                                    .withStyle(ChatFormatting.YELLOW),
                    false);
            return 0;
        }
        List<BankAccount> accountList = new java.util.ArrayList<>(accounts.values());
        int totalAccounts = accountList.size();
        int totalPages = (int) Math.ceil((double) totalAccounts / ACCOUNTS_PAGE_SIZE);
        int finalPage = Math.max(1, Math.min(page, totalPages));

        int start = (finalPage - 1) * ACCOUNTS_PAGE_SIZE;
        int end = Math.min(start + ACCOUNTS_PAGE_SIZE, totalAccounts);

        // ── Header ──────────────────────────────────────────────────────────────
        String finalDisplayName = displayName;
        source.sendSuccess(() ->
                        Component.literal("═══════════════════════════════════════")
                                .withStyle(ChatFormatting.GOLD)
                                .append(Component.literal("\n")
                                        .append(Component.translatable("message.bubusteinmoneymod.accounts_title", finalDisplayName)
                                                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)))
                                .append(Component.literal("\n═══════════════════════════════════════")
                                        .withStyle(ChatFormatting.GOLD)),
                false);

        // ── Conturile pentru pagina curentă ─────────────────────────────────────
        for (int i = start; i < end; i++) {
            BankAccount acc = accountList.get(i);
            String iban = acc.getIban();
            double balance = acc.getBalance();
            String currency = acc.getCurrency();
            String tier = acc.getCardTier();
            AccountKind kind = acc.getKind();
            boolean active = acc.isActive();
            String bankPrefix = acc.getBankPrefix();

            String formattedBalance = CardUtils.formatMoney(balance);
            String statusIcon = active ? "✓" : "✗";
            ChatFormatting statusColor = active ? ChatFormatting.GREEN : ChatFormatting.RED;

            int globalIndex = i + 1;
            source.sendSuccess(() -> {
                MutableComponent msg = Component.literal("\n   ");
                msg.append(Component.literal("[" + globalIndex + "] ")
                                .withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(statusIcon + " ")
                                .withStyle(statusColor));
                msg.append(Component.literal(iban)
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
                msg.append(Component.literal("\n   "))
                        .append(Component.translatable("cardItem.bubusteinmoneymod.balance", formattedBalance, currency)
                                .withStyle(ChatFormatting.GOLD));
                msg.append(Component.literal("\n   "))
                        .append(Component.translatable("message.bubusteinmoneymod.account_type")
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(": "))
                        .append(Component.literal(kind.name())
                                .withStyle(ChatFormatting.WHITE));
                if (tier != null && !tier.isEmpty()) {
                    msg.append(Component.literal("\n   "))
                            .append(Component.translatable("message.bubusteinmoneymod.card_tier")
                                    .withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(": "))
                            .append(Component.literal(tier)
                                    .withStyle(ModCommands.getTierColor(tier, kind)));
                }
                msg.append(Component.literal("\n   "))
                        .append(Component.translatable("message.bubusteinmoneymod.bank")
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(": "))
                        .append(Component.literal(bankPrefix)
                                .withStyle(ChatFormatting.YELLOW));
                msg.append(Component.literal("\n   "))
                        .append(Component.translatable("message.bubusteinmoneymod.active")
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(": "))
                        .append(Component.translatable(active ? "message.bubusteinmoneymod.yes" : "message.bubusteinmoneymod.no")
                                .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
                return msg;
            }, false);
        }
        // ── Separator ────────────────────────────────────────────────────────────
        source.sendSuccess(() ->
                        Component.literal("\n═══════════════════════════════════════")
                                .withStyle(ChatFormatting.GOLD),
                false);

        // ── Footer: total conturi + paginator ────────────────────────────────────
        source.sendSuccess(() -> {
            MutableComponent footer = Component.empty();
            if (finalPage > 1) {
                String prevCmd = targetPlayerName != null
                        ? "/bubustein accounts " + targetPlayerName + " " + (finalPage - 1)
                        : "/bubustein accounts " + executor.getGameProfile().getName()+ " " + (finalPage - 1);
                footer.append(Component.literal("« ")
                        .withStyle(s -> s
                                .withColor(ChatFormatting.YELLOW)
                                .withBold(true)
                                .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                        net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, prevCmd))
                                .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                        net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                        Component.literal("Page " + (finalPage - 1))
                                                .withStyle(ChatFormatting.YELLOW)))));
            }
            footer.append(Component.translatable("message.bubusteinmoneymod.total_accounts", totalAccounts)
                    .withStyle(ChatFormatting.DARK_GRAY));
            footer.append(Component.literal("  |  Page " + finalPage + " / " + totalPages)
                    .withStyle(ChatFormatting.GRAY));
            if (finalPage < totalPages) {
                String nextCmd = targetPlayerName != null
                        ? "/bubustein accounts " + targetPlayerName + " " + (finalPage + 1)
                        : "/bubustein accounts " + executor.getGameProfile().getName()+ " "  + (finalPage + 1);
                footer.append(Component.literal(" »")
                        .withStyle(s -> s
                                .withColor(ChatFormatting.YELLOW)
                                .withBold(true)
                                .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                        net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, nextCmd))
                                .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                        net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                        Component.literal("Page " + (finalPage + 1))
                                                .withStyle(ChatFormatting.YELLOW)))));
            }

            return footer;
        }, false);
        return Command.SINGLE_SUCCESS;
    }
    private static int createAccount(CommandSourceStack source, String typeStr, String bankPrefix)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MinecraftServer server = source.getServer();
        AccountKind kind;
        try {
            kind = AccountKind.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.createAccount.invalid_type",
                    typeStr,
                    "DEBIT, CREDIT, SAVINGS"
            ).withStyle(ChatFormatting.RED));
            return 0;
        }
        if (bankPrefix == null || bankPrefix.trim().isEmpty()) {
            bankPrefix = "BSTN";
        } else {
            bankPrefix = bankPrefix.toUpperCase().trim();

            if (bankPrefix.length() != 4) {
                source.sendFailure(Component.translatable(
                        "commands.bubusteinmoneymod.createAccount.invalid_prefix_length",
                        bankPrefix
                ).withStyle(ChatFormatting.RED));
                return 0;
            }

            if (!bankPrefix.matches("[A-Z]{4}")) {
                source.sendFailure(Component.translatable(
                        "commands.bubusteinmoneymod.createAccount.invalid_prefix_chars",
                        bankPrefix
                ).withStyle(ChatFormatting.RED));
                return 0;
            }
        }

        BankAccountManager mgr = BankAccountManager.get();
        if (!mgr.bankExists(bankPrefix)) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.createAccount.bank_not_found",
                    bankPrefix
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        // Credit eligibility check
        if (kind == AccountKind.CREDIT) {
            java.util.Optional<String> reason = mgr.checkCreditEligibility(server, player.getUUID());
            if (reason.isPresent()) {
                source.sendFailure(Component.literal(reason.get()).withStyle(ChatFormatting.RED));
                return 0;
            }
        }
        String currency = ModConfig.getInstance().getDefaultCurrency();
        if (currency == null || !ModItems.EXCHANGE_RATES.containsKey(currency)) {
            currency = "EUR";
        }
        int accountId = mgr.nextAccountId(server);
        String countryCode = ModConfig.getInstance().getServerCountryCode();
        String iban;

        try {
            iban = IbanGenerator.generateIban(countryCode, bankPrefix, kind, accountId);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.createAccount.iban_generation_failed",
                    e.getMessage()
            ).withStyle(ChatFormatting.RED));
            MoneyMod.LOGGER.error("[{}] Failed to generate IBAN", MoneyMod.MOD_ID, e);
            return 0;
        }

        if (!IbanGenerator.validateIban(iban)) {
            source.sendFailure(Component.literal("Generated IBAN is invalid: " + iban)
                    .withStyle(ChatFormatting.RED));
            MoneyMod.LOGGER.error("[{}] Generated invalid IBAN: {}", MoneyMod.MOD_ID, iban);
            return 0;
        }
        if (mgr.getByIban(server, iban).isPresent()) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.createAccount.already_exists",
                    iban
            ).withStyle(ChatFormatting.RED));
            return 0;
        }
        BankAccount account = mgr.createAccount(
                server,
                player.getUUID(),
                kind,
                currency,
                bankPrefix,
                accountId,
                iban
        );
        if (account == null) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.createAccount.failed"
            ).withStyle(ChatFormatting.RED));
            return 0;
        }
        account.setActive(false);
        BankAccountSavedData data = BankAccountSavedData.get(server);
        data.setDirty();

        String finalCurrency = currency;
        source.sendSuccess(() -> Component.translatable(
                "commands.bubusteinmoneymod.createAccount.success",
                kind.name(),
                iban,
                finalCurrency
        ).withStyle(ChatFormatting.GREEN), true);
        player.sendSystemMessage(Component.translatable(
                "message.bubusteinmoneymod.createAccount.hint"
        ).withStyle(ChatFormatting.GRAY));
        MoneyMod.LOGGER.info("[{}] Player {} created account {} (type: {}, bank: {}, currency: {})",
                MoneyMod.MOD_ID, player.getGameProfile().getName(), iban, kind, bankPrefix, currency);

        return Command.SINGLE_SUCCESS;
    }
    private static int showDeleteMenu(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MinecraftServer server = source.getServer();
        BankAccountSavedData data = BankAccountSavedData.get(server);
        Map<String, BankAccount> accounts = data.getAccountsForPlayer(player.getUUID());
        List<BankAccount> zeroBalanceAccounts = accounts.values().stream()
                .filter(BankAccount::isBalanceZero)
                .toList();

        if (zeroBalanceAccounts.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.no_zero_balance_accounts")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        player.sendSystemMessage(Component.literal("═══════════════════════════════════════")
                .withStyle(ChatFormatting.RED));
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.delete_menu_title")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("═══════════════════════════════════════")
                .withStyle(ChatFormatting.RED));
        int index = 1;
        for (BankAccount acc : zeroBalanceAccounts) {
            String status = acc.isActive() ? "✓ Active" : "✗ Inactive";
            ChatFormatting statusColor = acc.isActive() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            player.sendSystemMessage(Component.literal("\n[" + index + "] ")
                    .withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(acc.getIban())
                            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .append(Component.literal("\n   Status: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(status)
                                    .withStyle(statusColor)))
                    .append(Component.literal("\n   Tier: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(acc.getCardTier())
                                    .withStyle(ModCommands.getTierColor(acc.getCardTier(), acc.getKind())))));
            index++;
        }
        player.sendSystemMessage(Component.literal("\n═══════════════════════════════════════")
                .withStyle(ChatFormatting.RED));
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.delete_hint")
                .withStyle(ChatFormatting.GRAY));

        return Command.SINGLE_SUCCESS;
    }
    private static int deleteAccount(CommandSourceStack source, String iban) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MinecraftServer server = source.getServer();
        BankAccountSavedData data = BankAccountSavedData.get(server);
        Map<String, BankAccount> accounts = data.getAccountsForPlayer(player.getUUID());
        BankAccount acc = accounts.get(iban);
        if (acc == null) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.account_not_found", iban)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!acc.isBalanceZero()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.account_has_balance",
                            CardUtils.formatMoney(acc.getBalance()), acc.getCurrency())
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!acc.getOwnerUuid().equals(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_your_account")
                    .withStyle(ChatFormatting.RED));
            MoneyMod.LOGGER.warn("[{}] Player {} attempted to delete account {} owned by {}",
                    MoneyMod.MOD_ID, player.getName().getString(), iban, acc.getOwnerUuid());
            return 0;
        }
        data.deleteAccount(iban);
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.account_deleted", iban)
                .withStyle(ChatFormatting.GREEN));
        MoneyMod.LOGGER.info("[{}] Player {} deleted account {}",
                MoneyMod.MOD_ID, player.getName().getString(), iban);

        return Command.SINGLE_SUCCESS;
    }
    private static int linkCard(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof CardItem) && !(stack.getItem() instanceof CreditCardItem)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_blank_card")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        String existingIban = CardUtils.getIban(stack);
        if (existingIban != null && !existingIban.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_already_linked")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        MinecraftServer server = source.getServer();
        BankAccountSavedData data = BankAccountSavedData.get(server);
        Map<String, BankAccount> accounts = data.getAccountsForPlayer(player.getUUID());

        List<BankAccount> inactiveAccounts = accounts.values().stream()
                .filter(acc -> !acc.isActive())
                .toList();
        if (inactiveAccounts.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.no_inactive_accounts")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        if (inactiveAccounts.size() == 1) {
            BankAccount acc = inactiveAccounts.getFirst();
            return linkCardToAccount(player, stack, acc, data);
        }
        player.sendSystemMessage(Component.literal("═══════════════════════════════")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.link_menu_title")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("═══════════════════════════════")
                .withStyle(ChatFormatting.GOLD));

        int index = 1;
        for (BankAccount acc : inactiveAccounts) {
            player.sendSystemMessage(Component.literal(index + ". ")
                    .withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(acc.getIban())
                            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .append(Component.literal(" | Balance: ")
                            .withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(CardUtils.formatMoney(acc.getBalance()) + " " + acc.getCurrency())
                            .withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" | Tier: ")
                            .withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(acc.getCardTier())
                            .withStyle(ModCommands.getTierColor(acc.getCardTier(), acc.getKind()))));
            index++;
        }

        player.sendSystemMessage(Component.literal("═══════════════════════════════")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.link_hint")
                .withStyle(ChatFormatting.GRAY));

        return Command.SINGLE_SUCCESS;
    }
    private static int linkCardToSpecificIban(CommandSourceStack source, String iban) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();

        try {
            if (!(stack.getItem() instanceof CardItem) && !(stack.getItem() instanceof CreditCardItem)) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_blank_card")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
            String existingIban = CardUtils.getIban(stack);
            if (existingIban != null && !existingIban.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_already_linked")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
            if (iban == null || iban.trim().isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_iban")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            MinecraftServer server = source.getServer();
            BankAccountSavedData data = BankAccountSavedData.get(server);
            Map<String, BankAccount> accounts = data.getAccountsForPlayer(player.getUUID());

            BankAccount targetAccount = accounts.get(iban);
            if (targetAccount == null) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.account_not_found", iban)
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
            if (targetAccount.isActive()) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.account_already_active", iban)
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
            return linkCardToAccount(player, stack, targetAccount, data);

        } catch (Exception e) {
            MoneyMod.LOGGER.error("[{}] Error in linkCardToSpecificIban for player {} and IBAN {}: {}",
                    MoneyMod.MOD_ID, player.getName().getString(), iban, e.getMessage(), e);

            player.sendSystemMessage(Component.literal("Internal error while linking card. Check server logs.")
                    .withStyle(ChatFormatting.RED));

            return 0;
        }
    }
    private static int linkCardToAccount(ServerPlayer player, ItemStack stack,
                                         BankAccount account, BankAccountSavedData data) {
        synchronized (stack) {
            if (!(stack.getItem() instanceof CardItem) && !(stack.getItem() instanceof CreditCardItem)) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_blank_card")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
            Item item = stack.getItem();
            boolean isCreditCard = item == ModItems.ClassicCreditCard.get() ||
                    item == ModItems.GoldCreditCard.get() ||
                    item == ModItems.PlatinumCreditCard.get();

            boolean isDebitCard = item == ModItems.Card.get() ||
                    item == ModItems.GoldCard.get() ||
                    item == ModItems.SteelCard.get() ||
                    item == ModItems.RustyCard.get() ||
                    item == ModItems.SupremeCard.get();
            if (isCreditCard && account.getKind() != AccountKind.CREDIT) {
                player.sendSystemMessage(Component.translatable(
                                "message.bubusteinmoneymod.creditcard.onlycredit",
                                account.getKind().name())
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            if (isDebitCard && account.getKind() != AccountKind.DEBIT) {
                player.sendSystemMessage(Component.translatable(
                                "message.bubusteinmoneymod.debitcard.onlydebit",
                                account.getKind().name())
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            String existingIban = CardUtils.getIban(stack);
            if (existingIban != null && !existingIban.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_already_linked")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            String physicalTier;
            if (isCreditCard) {
                physicalTier = CardUtils.getCreditTierFromItem(stack).name();
            } else {
                physicalTier = CardUtils.getDebitTierFromItem(stack).name();
            }

            if (!account.getCardTier().equals(physicalTier)) {
                MoneyMod.LOGGER.info("[{}] CARD LINK: Updating account {} tier from {} to {} (physical card tier)",
                        MoneyMod.MOD_ID, account.getIban(), account.getCardTier(), physicalTier);
                account.setCardTier(physicalTier);
            }
            account.setActive(true);
            long nowTick = player.serverLevel().getGameTime();
            long lastTick = account.getLastInterestGameTick();

            if (lastTick == 0L) {
                account.setLastInterestGameTick(nowTick);
            } else {
                long tickDiff = nowTick - lastTick;
                if (tickDiff >= INTEREST_PERIOD_TICKS && account.hasDebt()) {
                    int periods = (int) (tickDiff / INTEREST_PERIOD_TICKS);
                    if (periods > 0) {
                        double added = account.applyInterestForPeriods(periods);
                        account.setLastInterestGameTick(lastTick + (long) periods * INTEREST_PERIOD_TICKS);
                        data.setDirty();
                        MoneyMod.LOGGER.info(
                                "[{}] Applied {} delayed interest periods (added {}) on reactivation for IBAN {}",
                                MoneyMod.MOD_ID, periods, added, account.getIban()
                        );
                    }
                }
            }
            CardUtils.setIban(stack, account.getIban());
            CardUtils.setOwner(stack, player.getUUID());
            CardUtils.setOwnerName(stack, player.getName().getString());

            stack.set(CardUtils.MONEY_COMPONENT.get(), account.getBalance());
            stack.set(CardUtils.CURRENCY_COMPONENT.get(), account.getCurrency());

            String cardNameKey;
            if (isCreditCard) {
                cardNameKey = "item.bubusteinmoneymod.card_" + physicalTier.toLowerCase() + "_credit.named";
            } else {
                cardNameKey = "item.bubusteinmoneymod." + physicalTier.toLowerCase() + "_card.named";
            }

            stack.set(DataComponents.CUSTOM_NAME,
                    Component.translatable(cardNameKey, player.getName().getString()));

            data.setDirty();

            MoneyMod.LOGGER.info("[{}] CARD LINKED: Player '{}' linked {} card ({}) to {} account {} (balance: {} {})",
                    MoneyMod.MOD_ID,
                    player.getName().getString(),
                    isCreditCard ? "CREDIT" : "DEBIT",
                    physicalTier,
                    account.getKind().name(),
                    account.getIban(),
                    account.getBalance(),
                    account.getCurrency());

            player.sendSystemMessage(Component.translatable(
                            "message.bubusteinmoneymod.card_linked",
                            account.getIban(),
                            CardUtils.formatMoney(account.getBalance()),
                            account.getCurrency())
                    .withStyle(ChatFormatting.GREEN));

            return Command.SINGLE_SUCCESS;
        }
    }
    private static int showInvalidateMenu(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MinecraftServer server = source.getServer();
        BankAccountSavedData data = BankAccountSavedData.get(server);

        Map<String, BankAccount> accounts = data.getAccountsForPlayer(player.getUUID());
        List<BankAccount> activeAccounts = accounts.values().stream()
                .filter(BankAccount::isActive)
                .toList();

        if (activeAccounts.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.no_active_accounts")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        player.sendSystemMessage(Component.literal("═══════════════════════════════════════")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalidate_menu_title")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("═══════════════════════════════════════")
                .withStyle(ChatFormatting.GOLD));

        int index = 1;
        for (BankAccount acc : activeAccounts) {
            player.sendSystemMessage(Component.literal("\n[" + index + "] ")
                    .withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(acc.getIban())
                            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .append(Component.literal("\n   Balance: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(CardUtils.formatMoney(acc.getBalance()) + " " + acc.getCurrency())
                                    .withStyle(ChatFormatting.GOLD)))
                    .append(Component.literal("\n   Tier: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(acc.getCardTier())
                                    .withStyle(ModCommands.getTierColor(acc.getCardTier(), acc.getKind())))));
            index++;
        }

        player.sendSystemMessage(Component.literal("\n═══════════════════════════════════════")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalidate_hint")
                .withStyle(ChatFormatting.GRAY));

        return Command.SINGLE_SUCCESS;
    }
    private static int invalidateCardByIban(CommandSourceStack source, String iban) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MinecraftServer server = source.getServer();
        BankAccountSavedData data = BankAccountSavedData.get(server);
        Map<String, BankAccount> accounts = data.getAccountsForPlayer(player.getUUID());
        BankAccount acc = accounts.get(iban);
        if (acc == null) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.account_not_found", iban)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        synchronized (acc) {
            if (!acc.isActive()) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.account_already_inactive", iban)
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
            acc.setActive(false);
        }
        data.setDirty();
        MoneyMod.LOGGER.warn("[CARD INVALIDATED] Player '{}' invalidated account '{}' remotely (balance: {} {})",
                player.getName().getString(), iban, acc.getBalance(), acc.getCurrency());

        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_invalidated_remote",
                        iban, CardUtils.formatMoney(acc.getBalance()), acc.getCurrency())
                .withStyle(ChatFormatting.YELLOW));

        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_invalidated_hint")
                .withStyle(ChatFormatting.GRAY));

        player.sendSystemMessage(Component.literal("Any physical cards linked to this IBAN will become Empty Cards automatically.")
                .withStyle(ChatFormatting.AQUA));

        return Command.SINGLE_SUCCESS;
    }
}
