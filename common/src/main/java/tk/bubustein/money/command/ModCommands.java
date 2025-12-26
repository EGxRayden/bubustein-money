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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.bank.*;
import tk.bubustein.money.config.ModConfig;
import tk.bubustein.money.item.CardItem;
import tk.bubustein.money.item.ModItems;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ModCommands {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000");
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bubustein")
                .then(Commands.literal("help")
                        .executes(context -> showHelp(context.getSource())))
                .then(Commands.literal("setcurrency")
                        .then(Commands.argument("currency", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String input = builder.getRemaining().toLowerCase();
                                    ModItems.EXCHANGE_RATES.keySet().stream()
                                            .filter(c -> c.toLowerCase().startsWith(input))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> setCurrency(context.getSource(), StringArgumentType.getString(context, "currency")))))
                .then(Commands.literal("setdefaultcurrency")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("currency", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String input = builder.getRemaining().toLowerCase();
                                    ModItems.EXCHANGE_RATES.keySet().stream()
                                            .filter(c -> c.toLowerCase().startsWith(input))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> setDefaultCurrency(context.getSource(), StringArgumentType.getString(context, "currency")))))
                .then(Commands.literal("accounts")
                        .executes(context -> showAccounts(context.getSource(), null))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .requires(source -> source.hasPermission(2)) // doar OP
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
                                        StringArgumentType.getString(context, "player")))))
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
                                            builder.suggest("BSTN");
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> createAccount(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "type"),
                                                StringArgumentType.getString(context, "bank"))))))

                .then(Commands.literal("link")
                        .executes(context -> linkCard(context.getSource()))
                        .then(Commands.argument("iban", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    MinecraftServer server = context.getSource().getServer();
                                    BankAccountSavedData data = BankAccountSavedData.get(server);
                                    data.getAccountsForPlayer(player.getUUID()).values().stream()
                                            .filter(acc -> !acc.isActive())
                                            .map(BankAccount::getIban)
                                            .forEach(builder::suggest);

                                    return builder.buildFuture();
                                })
                                .executes(context -> linkCardToSpecificIban(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "iban")))))
                .then(Commands.literal("rates")
                        .executes(context -> showRates(context.getSource(), null))
                        .then(Commands.argument("currency", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String input = builder.getRemaining().toLowerCase();
                                    ModItems.EXCHANGE_RATES.keySet().stream()
                                            .filter(c -> c.toLowerCase().startsWith(input))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> showRates(context.getSource(),
                                        StringArgumentType.getString(context, "currency")))))
                .then(Commands.literal("updateRates")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> updateRates(context.getSource())))
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
                .then(Commands.literal("deleteAccount")
                        .executes(context -> showDeleteMenu(context.getSource()))
                        .then(Commands.argument("iban", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    MinecraftServer server = context.getSource().getServer();
                                    BankAccountSavedData data = BankAccountSavedData.get(server);
                                    data.getAccountsForPlayer(player.getUUID()).values().stream()
                                            .filter(acc -> acc.getBalance() == 0.0)
                                            .map(BankAccount::getIban)
                                            .forEach(builder::suggest);

                                    return builder.buildFuture();
                                })
                                .executes(context -> deleteAccount(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "iban")))))

                .then(Commands.literal("withdraw")
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(context -> withdraw(context.getSource(), DoubleArgumentType.getDouble(context, "amount")))))
                .then(Commands.literal("defaultCurrency")
                        .executes(context -> showDefaultCurrency(context.getSource())))
                .then(Commands.literal("ecoAddMoney")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(context -> ecoAddMoney(context.getSource(), DoubleArgumentType.getDouble(context, "amount"), null))
                                .then(Commands.argument("currency", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String input = builder.getRemaining().toLowerCase();
                                            ModItems.EXCHANGE_RATES.keySet().stream()
                                                    .filter(c -> c.toLowerCase().startsWith(input))
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> ecoAddMoney(context.getSource(), DoubleArgumentType.getDouble(context, "amount"), StringArgumentType.getString(context, "currency"))))))
                .then(Commands.literal("ecoSetMoney")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(context -> ecoSetMoney(context.getSource(), DoubleArgumentType.getDouble(context, "amount"), null))
                                .then(Commands.argument("currency", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String input = builder.getRemaining().toLowerCase();
                                            ModItems.EXCHANGE_RATES.keySet().stream()
                                                    .filter(c -> c.toLowerCase().startsWith(input))
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> ecoSetMoney(context.getSource(), DoubleArgumentType.getDouble(context, "amount"), StringArgumentType.getString(context, "currency"))))))
                .then(Commands.literal("resetMoney")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> resetMoney(context.getSource())))
                .then(Commands.literal("pay")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    List<String> onlinePlayers = context.getSource().getServer().getPlayerList().getPlayers().stream().map(player -> player.getGameProfile().getName()).toList();
                                    String inputLower = builder.getRemaining().toLowerCase();
                                    for (String playerName : onlinePlayers) {
                                        if (playerName.toLowerCase().startsWith(inputLower)) {
                                            builder.suggest(playerName);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(context -> pay(context.getSource(), StringArgumentType.getString(context, "player"), DoubleArgumentType.getDouble(context, "amount"))))))
        );
    }
    private static int showHelp(CommandSourceStack source) {
        Player player = source.getPlayer();
        if (player != null) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.title")
                    .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true)));

            player.sendSystemMessage(createStyledHelpMessage("/bubustein help", "message.bubusteinmoneymod.help"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein accounts [player]", "message.bubusteinmoneymod.accounts"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein rates [currency]", "message.bubusteinmoneymod.rates"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein updateRates", "message.bubusteinmoneymod.updateRates", true));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein setcurrency <currency>", "message.bubusteinmoneymod.setcurrency"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein setdefaultcurrency <currency>", "message.bubusteinmoneymod.setdefaultcurrency", true));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein deposit <amount> [currency]", "message.bubusteinmoneymod.deposit"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein withdraw <amount>", "message.bubusteinmoneymod.withdraw"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein defaultCurrency", "message.bubusteinmoneymod.defaultCurrency"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein invalidate [iban]", "message.bubusteinmoneymod.invalidate"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein link [iban]", "message.bubusteinmoneymod.link"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein createAccount <type> [bank]", "message.bubusteinmoneymod.createAccount"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein deleteAccount [iban]", "message.bubusteinmoneymod.deleteAccount"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein ecoAddMoney <amount> [currency]", "message.bubusteinmoneymod.ecoAddMoney", true));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein ecoSetMoney <amount> [currency]", "message.bubusteinmoneymod.ecoSetMoney", true));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein resetMoney", "message.bubusteinmoneymod.resetMoney", true));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein pay <player> <amount>", "message.bubusteinmoneymod.pay"));
            player.sendSystemMessage(Component.literal("=======================================================================")
                    .withStyle(ChatFormatting.GOLD));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static MutableComponent createStyledHelpMessage(String command, String descriptionKey) {
        return createStyledHelpMessage(command, descriptionKey, false);
    }
    private static MutableComponent createStyledHelpMessage(String command, String descriptionKey, boolean requiresOp) {
        MutableComponent styledCommand = Component.literal(command).withStyle(ChatFormatting.AQUA);
        MutableComponent styledDescription = Component.literal(": ").append(Component.translatable(descriptionKey)).withStyle(ChatFormatting.GRAY);
        MutableComponent fullMessage = styledCommand.append(styledDescription);
        if (requiresOp) {
            fullMessage.append(Component.translatable("message.bubusteinmoneymod.requires_op")
                    .withStyle(style -> style.withColor(ChatFormatting.RED).withBold(true)));
        }
        return fullMessage;
    }
    private static int showRates(CommandSourceStack source, String specificCurrency) {
        ModConfig config = ModConfig.getInstance();
        if (specificCurrency != null) {
            String upper = specificCurrency.toUpperCase();
            if (!ModItems.EXCHANGE_RATES.containsKey(upper)) {
                source.sendFailure(
                        Component.translatable("commands.bubusteinmoneymod.rates.currency_not_found", upper)
                                .withStyle(ChatFormatting.RED)
                );
                return 0;
            }
            double rate = ModItems.EXCHANGE_RATES.get(upper);
            source.sendSuccess(() ->
                            Component.literal("═══════════════════════════")
                                    .withStyle(ChatFormatting.GOLD)
                                    .append(Component.literal("\n")
                                            .append(Component.translatable(
                                                            "commands.bubusteinmoneymod.rates.title_single", upper
                                                    ).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                                            )
                                    )
                                    .append(Component.literal("\n═══════════════════════════")
                                            .withStyle(ChatFormatting.GOLD)
                                    )
                                    .append(Component.literal("\n\n")
                                            .append(Component.translatable(
                                                            "commands.bubusteinmoneymod.rates.line_eur_to_currency",
                                                            rate, upper
                                                    ).withStyle(ChatFormatting.WHITE)
                                            )
                                    )
                                    .append(Component.literal("\n")
                                            .append(Component.translatable(
                                                            "commands.bubusteinmoneymod.rates.line_currency_to_eur",
                                                            upper, 1.0 / rate
                                                    ).withStyle(ChatFormatting.GRAY)
                                            )
                                    )
                                    .append(Component.literal("\n\n═══════════════════════════")
                                            .withStyle(ChatFormatting.GOLD)
                                    )
                                    .append(Component.literal("\n")
                                            .append(Component.translatable(
                                                            "commands.bubusteinmoneymod.rates.last_updated",
                                                            config.getLastRatesUpdateReadable()
                                                    ).withStyle(ChatFormatting.DARK_GRAY)
                                            )
                                    ),
                    false
            );
        } else {
            source.sendSuccess(() -> {
                MutableComponent msg = Component.literal("═══════════════════════════")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal("\n")
                                .append(Component.translatable("commands.bubusteinmoneymod.rates.title_all")
                                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                                )
                        )
                        .append(Component.literal("\n═══════════════════════════")
                                .withStyle(ChatFormatting.GOLD)
                        );

                ModItems.EXCHANGE_RATES.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            String currency = entry.getKey();
                            double rate = entry.getValue();
                            ChatFormatting color = currency.equals("EUR") ? ChatFormatting.GOLD : ChatFormatting.GRAY;

                            msg.append(Component.literal("\n")
                                    .append(Component.translatable(
                                                    "commands.bubusteinmoneymod.rates.line_eur_to_currency",
                                                    rate, currency
                                            ).withStyle(color)
                                    )
                            );
                        });
                msg.append(Component.literal("\n═══════════════════════════")
                                .withStyle(ChatFormatting.GOLD)
                        )
                        .append(Component.literal("\n")
                                .append(Component.translatable(
                                                "commands.bubusteinmoneymod.rates.total",
                                                ModItems.EXCHANGE_RATES.size()
                                        ).withStyle(ChatFormatting.DARK_GRAY)
                                )
                        )
                        .append(Component.literal("\n")
                                .append(Component.translatable(
                                                "commands.bubusteinmoneymod.rates.last_updated",
                                                config.getLastRatesUpdateReadable()
                                        ).withStyle(ChatFormatting.DARK_GRAY)
                                )
                        );

                return msg;
            }, false);
        }

        return Command.SINGLE_SUCCESS;
    }
    private static int updateRates(CommandSourceStack source) {
        source.sendSuccess(
                () -> Component.translatable("commands.bubusteinmoneymod.update.start")
                        .withStyle(ChatFormatting.YELLOW),
                false
        );
        Map<String, Double> oldRates = new HashMap<>(ModItems.EXCHANGE_RATES);
        ModItems.updateExchangeRatesAsync(source.getServer(), false).thenRun(() -> {
            int updatedCount = 0;
            int unchangedCount = 0;
            for (String currency : ModItems.EXCHANGE_RATES.keySet()) {
                Double oldRate = oldRates.get(currency);
                Double newRate = ModItems.EXCHANGE_RATES.get(currency);
                if (oldRate == null || Math.abs(oldRate - newRate) > 0.0001) {
                    updatedCount++;
                } else {
                    unchangedCount++;
                }
            }
            int finalUpdated = updatedCount;
            int finalUnchanged = unchangedCount;
            source.sendSuccess(() ->
                            Component.literal("═══════════════════════════")
                                    .withStyle(ChatFormatting.GREEN)
                                    .append(Component.literal("\n")
                                            .append(Component.translatable("commands.bubusteinmoneymod.update.title")
                                                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                                            )
                                    )
                                    .append(Component.literal("\n═══════════════════════════")
                                            .withStyle(ChatFormatting.GREEN)
                                    )
                                    .append(Component.literal("\n")
                                            .append(Component.translatable(
                                                            "commands.bubusteinmoneymod.update.updated", finalUpdated
                                                    ).withStyle(ChatFormatting.WHITE)
                                            )
                                    )
                                    .append(Component.literal("\n")
                                            .append(Component.translatable(
                                                            "commands.bubusteinmoneymod.update.unchanged", finalUnchanged
                                                    ).withStyle(ChatFormatting.GRAY)
                                            )
                                    )
                                    .append(Component.literal("\n")
                                            .append(Component.translatable(
                                                            "commands.bubusteinmoneymod.update.total",
                                                            ModItems.EXCHANGE_RATES.size()
                                                    ).withStyle(ChatFormatting.GRAY)
                                            )
                                    )
                                    .append(Component.literal("\n")
                                            .append(Component.translatable(
                                                            "commands.bubusteinmoneymod.update.last_update",
                                                            ModConfig.getInstance().getLastRatesUpdateReadable()
                                                    ).withStyle(ChatFormatting.DARK_GRAY)
                                            )
                                    ),
                    true
            );
        }).exceptionally(ex -> {
            source.sendFailure(
                    Component.translatable("commands.bubusteinmoneymod.update.failed", String.valueOf(ex.getMessage())).withStyle(ChatFormatting.RED));
            return null;
        });
        return Command.SINGLE_SUCCESS;
    }
    private static int showAccounts(CommandSourceStack source, String targetPlayerName) throws CommandSyntaxException {
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
        source.sendSuccess(() ->
                        Component.literal("═══════════════════════════════════════")
                                .withStyle(ChatFormatting.GOLD)
                                .append(Component.literal("\n")
                                        .append(Component.translatable("message.bubusteinmoneymod.accounts_title", displayName)
                                                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)))
                                .append(Component.literal("\n═══════════════════════════════════════")
                                        .withStyle(ChatFormatting.GOLD)),
                false);
        int index = 1;
        for (BankAccount acc : accounts.values()) {
            String iban = acc.getIban();
            double balance = acc.getBalance();
            String currency = acc.getCurrency();
            String tier = acc.getCardTier();
            AccountKind kind = acc.getKind();
            boolean active = acc.isActive();
            String bankPrefix = acc.getBankPrefix();

            String formattedBalance = CardItem.formatMoney(balance);

            String statusIcon = active ? "✓" : "✗";
            ChatFormatting statusColor = active ? ChatFormatting.GREEN : ChatFormatting.RED;

            int finalIndex = index;
            source.sendSuccess(() -> {
                MutableComponent msg = Component.literal("\n   ");

                msg.append(Component.literal("[" + finalIndex + "] ")
                                .withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(statusIcon + " ")
                                .withStyle(statusColor));
                msg.append(Component.literal(iban)
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

                msg.append(Component.literal("\n   ")
                                .withStyle(ChatFormatting.GRAY))
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
                                    .withStyle(getTierColor(tier)));
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
            index++;
        }
        int totalAccounts = accounts.size();
        source.sendSuccess(() ->
                        Component.literal("\n═══════════════════════════════════════")
                                .withStyle(ChatFormatting.GOLD)
                                .append(Component.literal("\n")
                                        .append(Component.translatable("message.bubusteinmoneymod.total_accounts", totalAccounts)
                                                .withStyle(ChatFormatting.DARK_GRAY))),
                false);

        return Command.SINGLE_SUCCESS;
    }
    private static ChatFormatting getTierColor(String tier) {
        return switch (tier.toUpperCase()) {
            case "SUPREME" -> ChatFormatting.LIGHT_PURPLE;
            case "STEEL" -> ChatFormatting.DARK_AQUA;
            case "GOLD" -> ChatFormatting.GOLD;
            case "CLASSIC" -> ChatFormatting.WHITE;
            case "RUSTY" -> ChatFormatting.RED;
            default -> ChatFormatting.GRAY;
        };
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
                            .append(Component.literal(formatMoney(acc.getBalance()) + " " + acc.getCurrency())
                                    .withStyle(ChatFormatting.GOLD)))
                    .append(Component.literal("\n   Tier: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(acc.getCardTier())
                                    .withStyle(getTierColor(acc.getCardTier())))));
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
                        iban, CardItem.formatMoney(acc.getBalance()), acc.getCurrency())
                .withStyle(ChatFormatting.YELLOW));

        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_invalidated_hint")
                .withStyle(ChatFormatting.GRAY));

        player.sendSystemMessage(Component.literal("Any physical cards linked to this IBAN will become Empty Cards automatically.")
                .withStyle(ChatFormatting.AQUA));

        return Command.SINGLE_SUCCESS;
    }

    private static int linkCard(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof CardItem)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_blank_card")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        String existingIban = CardItem.getIban(stack);
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
        player.sendSystemMessage(Component.literal("═══════════════════════════════════════")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.link_menu_title")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("═══════════════════════════════════════")
                .withStyle(ChatFormatting.GOLD));
        int index = 1;
        for (BankAccount acc : inactiveAccounts) {
            player.sendSystemMessage(Component.literal("\n[" + index + "] ")
                    .withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(acc.getIban())
                            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                    .append(Component.literal("\n   Balance: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(formatMoney(acc.getBalance()) + " " + acc.getCurrency())
                                    .withStyle(ChatFormatting.GOLD)))
                    .append(Component.literal("\n   Tier: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(acc.getCardTier())
                                    .withStyle(getTierColor(acc.getCardTier())))));
            index++;
        }
        player.sendSystemMessage(Component.literal("\n═══════════════════════════════════════")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.link_hint")
                .withStyle(ChatFormatting.GRAY));

        return Command.SINGLE_SUCCESS;
    }
    private static int linkCardToSpecificIban(CommandSourceStack source, String iban) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof CardItem)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_blank_card")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        String existingIban = CardItem.getIban(stack);
        if (existingIban != null && !existingIban.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_already_linked")
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
    }
    private static int linkCardToAccount(ServerPlayer player, ItemStack stack,
                                         BankAccount account, BankAccountSavedData data) {
        synchronized (stack) {
            if (!(stack.getItem() instanceof CardItem)) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_blank_card")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
            String existingIban = CardItem.getIban(stack);
            if (existingIban != null && !existingIban.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_already_linked")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }
            boolean hasLegacyData = stack.has(CardItem.MONEY_COMPONENT.get()) ||
                    stack.has(CardItem.CURRENCY_COMPONENT.get());
            if (hasLegacyData) {
                double legacyBalance = stack.getOrDefault(CardItem.MONEY_COMPONENT.get(), 0.0);
                String legacyCurrency = stack.getOrDefault(CardItem.CURRENCY_COMPONENT.get(), null);
                if (legacyBalance > 0) {
                    if (legacyCurrency == null || legacyCurrency.isEmpty()) {
                        legacyCurrency = account.getCurrency();
                    }

                    if (!ModItems.EXCHANGE_RATES.containsKey(legacyCurrency)) {
                        MoneyMod.LOGGER.warn("[LEGACY MIGRATION] Player {} has invalid legacy currency {} on card, assuming {}",
                                player.getName().getString(), legacyCurrency, account.getCurrency());
                        legacyCurrency = account.getCurrency();
                    }
                    try {
                        if (!legacyCurrency.equals(account.getCurrency())) {
                            double converted = convertCurrency(legacyBalance, legacyCurrency, account.getCurrency());
                            account.setBalance(converted);
                            MoneyMod.LOGGER.info("[LEGACY MIGRATION] Player {} migrated {} {} (converted to {} {}) from legacy card to account {}",
                                    player.getName().getString(),
                                    legacyBalance, legacyCurrency,
                                    converted, account.getCurrency(),
                                    account.getIban());
                        } else {
                            account.setBalance(legacyBalance);
                            MoneyMod.LOGGER.info("[LEGACY MIGRATION] Player {} migrated {} {} from legacy card to account {}",
                                    player.getName().getString(), legacyBalance, legacyCurrency, account.getIban());
                        }
                        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.legacy_balance_migrated",
                                        formatMoney(legacyBalance), legacyCurrency)
                                .withStyle(ChatFormatting.GOLD));
                    } catch (IllegalArgumentException e) {
                        MoneyMod.LOGGER.error("[LEGACY MIGRATION] Failed to convert currency: {}", e.getMessage());
                        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.migration_failed")
                                .withStyle(ChatFormatting.RED));
                        return 0;
                    }
                }
            }
            String physicalTier = CardItem.getTierFromItem(stack).name();
            if (!account.getCardTier().equals(physicalTier)) {
                MoneyMod.LOGGER.info("[CARD LINK] Updating account {} tier from {} to {} (physical card tier)",
                        account.getIban(), account.getCardTier(), physicalTier);
                account.setCardTier(physicalTier);
            }
            account.setActive(true);
            CardItem.setIban(stack, account.getIban());
            CardItem.setOwner(stack, player.getUUID());
            CardItem.setOwnerName(stack, player.getName().getString());
            stack.set(CardItem.MONEY_COMPONENT.get(), account.getBalance());
            stack.set(CardItem.CURRENCY_COMPONENT.get(), account.getCurrency());

            String cardNameKey = "item.bubusteinmoneymod." + physicalTier.toLowerCase() + "_card.named";
            stack.set(DataComponents.CUSTOM_NAME, Component.translatable(cardNameKey, player.getName().getString()));
            data.setDirty();
            MoneyMod.LOGGER.info("[CARD LINKED] Player {} linked {} card to account {} (balance: {} {})",
                    player.getName().getString(), physicalTier, account.getIban(),
                    account.getBalance(), account.getCurrency());

            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_linked",
                            account.getIban(), formatMoney(account.getBalance()), account.getCurrency())
                    .withStyle(ChatFormatting.GREEN));

            return Command.SINGLE_SUCCESS;
        }
    }
    private static int createAccount(CommandSourceStack source, String typeStr, String bankPrefix) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MinecraftServer server = source.getServer();
        AccountKind kind;
        try {
            kind = AccountKind.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_account_type",
                            "DEBIT, CREDIT, SAVINGS")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (bankPrefix == null || bankPrefix.isEmpty()) {
            bankPrefix = "BSTN";
        }
        bankPrefix = bankPrefix.toUpperCase();
        if (bankPrefix.length() != 4) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_bank_prefix")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        UUID owner = player.getUUID();
        String defaultCurrency = MoneyMod.getDefaultCurrency();
        if (!ModItems.EXCHANGE_RATES.containsKey(defaultCurrency)) {
            defaultCurrency = "EUR";
        }
        BankAccountManager mgr = BankAccountManager.get();
        int accountId = mgr.nextAccountId(server);
        String countryCode = ModConfig.getInstance().getServerCountryCode();
        String iban = IbanGenerator.generateIban(
                countryCode,
                bankPrefix,
                kind,
                accountId
        );
        if (mgr.getByIban(server, iban).isPresent()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.account_exists")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        BankAccount acc = mgr.createAccount(server, owner, kind, defaultCurrency, bankPrefix, accountId, iban);
        acc.setActive(false);
        acc.setCardTier("");
        BankAccountSavedData data = BankAccountSavedData.get(server);
        data.setDirty();
        MoneyMod.LOGGER.info("[ACCOUNT CREATED] Player {} created {} account {} (bank: {})",
                player.getName().getString(), kind, iban, bankPrefix);

        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.account_created",
                        kind.name(), iban, defaultCurrency)
                .withStyle(ChatFormatting.GREEN));

        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.account_created_hint")
                .withStyle(ChatFormatting.GRAY));

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
                                    .withStyle(getTierColor(acc.getCardTier())))));
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
                            CardItem.formatMoney(acc.getBalance()), acc.getCurrency())
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
    private static int ecoAddMoney(CommandSourceStack source, double amount, String specifiedCurrency) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();

        Optional<BankAccount> optAcc = getAccountFromCard(stack, player, true);
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
        if (hasMoreThanTwoDecimals(amount)) {
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
        if (current.add(add).compareTo(MAX_AMOUNT) > 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large",
                    CardItem.formatMoney(MAX_AMOUNT.doubleValue())).withStyle(ChatFormatting.RED));
            return 0;
        }

        double amountInAccountCurrency = convertCurrency(amount, addCurrency, accountCurrency);
        acc.setBalance(acc.getBalance() + amountInAccountCurrency);
        syncCardWithAccount(stack, acc);

        boolean isAdminAction = !acc.getOwnerUuid().equals(player.getUUID());

        if (isAdminAction) {
            MoneyMod.LOGGER.warn("[ADMIN ACTION] {} added {} {} to card {} (owner: {})",
                    player.getName().getString(), formatMoney(amount), addCurrency,
                    acc.getIban(), acc.getOwnerUuid());

            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.admin.amount_added",
                            formatMoney(amount), addCurrency, formatMoney(acc.getBalance()), accountCurrency)
                    .withStyle(ChatFormatting.GOLD));
            ServerPlayer owner = getCardOwner(source.getServer(), acc);
            if (owner != null) {
                owner.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.admin.your_card_added",
                                player.getName().getString(), formatMoney(amountInAccountCurrency), accountCurrency)
                        .withStyle(ChatFormatting.GREEN));
            }
        } else {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_added",
                    formatMoney(amount), addCurrency,
                    formatMoney(acc.getBalance()), accountCurrency).withStyle(ChatFormatting.GREEN));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int ecoSetMoney(CommandSourceStack source, double amount, String specifiedCurrency) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();

        Optional<BankAccount> optAcc = getAccountFromCard(stack, player, true);
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
        if (hasMoreThanTwoDecimals(amount)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.two_decimals").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (BigDecimal.valueOf(amount).compareTo(MAX_AMOUNT) > 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large",
                    CardItem.formatMoney(MAX_AMOUNT.doubleValue())).withStyle(ChatFormatting.RED));
            return 0;
        }

        String accountCurrency = acc.getCurrency();
        String setCurrency = (specifiedCurrency != null) ? specifiedCurrency : accountCurrency;

        if (!ModItems.EXCHANGE_RATES.containsKey(setCurrency)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_currency",
                    String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
            return 0;
        }

        double amountInAccountCurrency = convertCurrency(amount, setCurrency, accountCurrency);
        acc.setBalance(amountInAccountCurrency);
        syncCardWithAccount(stack, acc);

        boolean isAdminAction = !acc.getOwnerUuid().equals(player.getUUID());

        if (isAdminAction) {
            MoneyMod.LOGGER.warn("[ADMIN ACTION] {} set card {} balance to {} {} (owner: {})",
                    player.getName().getString(), acc.getIban(),
                    formatMoney(amount), setCurrency, acc.getOwnerUuid());

            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.admin.amount_set",
                            formatMoney(amount), setCurrency, formatMoney(acc.getBalance()), accountCurrency)
                    .withStyle(ChatFormatting.GOLD));

            ServerPlayer owner = getCardOwner(source.getServer(), acc);
            if (owner != null) {
                owner.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.admin.your_card_set",
                                player.getName().getString(), formatMoney(amountInAccountCurrency), accountCurrency)
                        .withStyle(ChatFormatting.YELLOW));
            }
        } else {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_set",
                    formatMoney(amount), setCurrency,
                    formatMoney(acc.getBalance()), accountCurrency).withStyle(ChatFormatting.GREEN));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int resetMoney(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();

        Optional<BankAccount> optAcc = getAccountFromCard(stack, player, true);
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
        syncCardWithAccount(stack, acc);

        if (isAdminAction) {
            MoneyMod.LOGGER.warn("[ADMIN ACTION] {} reset card {} (owner: {})",
                    player.getName().getString(), acc.getIban(), acc.getOwnerUuid());

            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.admin.card_reset", currency)
                    .withStyle(ChatFormatting.GOLD));
            ServerPlayer owner = getCardOwner(source.getServer(), acc);
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
    private static int pay(CommandSourceStack source, String targetPlayerName, double amount) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(targetPlayerName);

        if (targetPlayer == null) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.player_not_online", targetPlayerName)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (targetPlayerName.equalsIgnoreCase(player.getName().getString())) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.cannot_pay_self")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (amount <= 0.0 || hasMoreThanTwoDecimals(amount)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_positive")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        ItemStack playerStackInitial = player.getMainHandItem();
        ItemStack targetStackInitial = targetPlayer.getMainHandItem();

        if (!(playerStackInitial.getItem() instanceof CardItem)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_card")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!(targetStackInitial.getItem() instanceof CardItem)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.target_no_card", targetPlayerName)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        Optional<BankAccount> optSourceAcc = getAccountFromCard(playerStackInitial, player);
        Optional<BankAccount> optTargetAcc = getAccountFromCard(targetStackInitial, targetPlayer);

        if (optSourceAcc.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_card")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (optTargetAcc.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.target_no_card", targetPlayerName)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        BankAccount sourceAcc = optSourceAcc.get();
        BankAccount targetAcc = optTargetAcc.get();

        if (!sourceAcc.getOwnerUuid().equals(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_card_owner")
                    .withStyle(ChatFormatting.RED));
            MoneyMod.LOGGER.warn("[SECURITY] Player {} attempted to pay from card {} owned by {}",
                    player.getName().getString(), sourceAcc.getIban(), sourceAcc.getOwnerUuid());
            return 0;
        }

        if (!sourceAcc.isActive()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_not_active")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!targetAcc.isActive()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.target_card_not_active", targetPlayerName)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        double playerBalance = sourceAcc.getBalance();
        if (playerBalance < amount) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_enough_funds")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        ItemStack playerStackFinal = player.getMainHandItem();
        ItemStack targetStackFinal = targetPlayer.getMainHandItem();

        if (playerStackFinal != playerStackInitial || !(playerStackFinal.getItem() instanceof CardItem)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_card")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (targetStackFinal != targetStackInitial || !(targetStackFinal.getItem() instanceof CardItem)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.target_no_card", targetPlayerName)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        String playerCurrency = sourceAcc.getCurrency();
        String targetCurrency = targetAcc.getCurrency();

        if (!ModItems.EXCHANGE_RATES.containsKey(playerCurrency) ||
                !ModItems.EXCHANGE_RATES.containsKey(targetCurrency)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_currency",
                            String.join(", ", ModItems.EXCHANGE_RATES.keySet()))
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        double amountInTargetCurrency;
        try {
            amountInTargetCurrency = convertCurrency(amount, playerCurrency, targetCurrency);
        } catch (IllegalArgumentException e) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.conversion_failed")
                    .withStyle(ChatFormatting.RED));
            MoneyMod.LOGGER.error("[PAY] Currency conversion failed: {}", e.getMessage());
            return 0;
        }
        BigDecimal targetNewBalance = BigDecimal.valueOf(targetAcc.getBalance())
                .add(BigDecimal.valueOf(amountInTargetCurrency));
        if (targetNewBalance.compareTo(MAX_AMOUNT) > 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large",
                            CardItem.formatMoney(MAX_AMOUNT.doubleValue()))
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        synchronized (sourceAcc) {
            synchronized (targetAcc) {
                sourceAcc.setBalance(playerBalance - amount);
                targetAcc.setBalance(targetNewBalance.doubleValue());

                syncCardWithAccount(playerStackFinal, sourceAcc);
                syncCardWithAccount(targetStackFinal, targetAcc);
            }
        }
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.transfer_success",
                        formatMoney(amount), playerCurrency, targetPlayerName)
                .withStyle(ChatFormatting.GREEN));
        targetPlayer.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.receive_success",
                        formatMoney(amountInTargetCurrency), targetCurrency, player.getName().getString())
                .withStyle(ChatFormatting.GREEN));
        MoneyMod.LOGGER.info("[TRANSFER] {} sent {} {} to {} (converted to {} {})",
                player.getName().getString(), formatMoney(amount), playerCurrency,
                targetPlayerName, formatMoney(amountInTargetCurrency), targetCurrency);

        return Command.SINGLE_SUCCESS;
    }
    private static int setCurrency(CommandSourceStack source, String currency) throws CommandSyntaxException {
        if (!ModItems.EXCHANGE_RATES.containsKey(currency)) {
            source.sendFailure(Component.translatable("message.bubusteinmoneymod.invalid_currency",
                    String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
            return 0;
        }

        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();

        Optional<BankAccount> optAcc = getAccountFromCard(stack, player);
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
        String oldCurrency = acc.getCurrency();
        double currentAmount = acc.getBalance();

        if (!ModItems.EXCHANGE_RATES.containsKey(oldCurrency)) {
            source.sendFailure(Component.translatable("message.bubusteinmoneymod.invalid_currency",
                    String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
            return 0;
        }

        double convertedAmount = convertCurrency(currentAmount, oldCurrency, currency);
        if (BigDecimal.valueOf(convertedAmount).compareTo(MAX_AMOUNT) > 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large",
                    CardItem.formatMoney(MAX_AMOUNT.doubleValue())).withStyle(ChatFormatting.RED));
            return 0;
        }

        acc.setCurrency(currency);
        acc.setBalance(convertedAmount);
        syncCardWithAccount(stack, acc);

        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.currency_changed",
                currency, formatMoney(convertedAmount), currency).withStyle(ChatFormatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }
    private static int setDefaultCurrency(CommandSourceStack source, String currency) {
        if (ModItems.EXCHANGE_RATES.containsKey(currency)) {
            MoneyMod.setDefaultCurrency(currency);
            MoneyMod.saveConfig(source.getServer());
            source.sendSuccess(() -> Component.translatable("message.bubusteinmoneymod.default_currency_set", currency).withStyle(ChatFormatting.GREEN), true);
        } else {
            source.sendFailure(Component.translatable("message.bubusteinmoneymod.invalid_currency", String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
        }
        return Command.SINGLE_SUCCESS;
    }
    private static int showDefaultCurrency(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("message.bubusteinmoneymod.default_currency_show", MoneyMod.getDefaultCurrency()).withStyle(ChatFormatting.GREEN), false);
        return Command.SINGLE_SUCCESS;
    }
    private static int deposit(CommandSourceStack source, double amount, String currencyArg) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (hasMoreThanTwoDecimals(amount)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.two_decimals")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (amount <= 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_positive")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (amount > MAX_AMOUNT.doubleValue()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        ItemStack cardStack = player.getMainHandItem();
        Optional<BankAccount> optAcc = getAccountFromCard(cardStack, player);

        if (optAcc.isEmpty()) {
            return 0;
        }

        BankAccount acc = optAcc.get();

        if (!acc.isActive()) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_not_active")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        String depositCurrency = currencyArg != null ? currencyArg.toUpperCase() : acc.getCurrency();
        if (!ModItems.EXCHANGE_RATES.containsKey(depositCurrency)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_currency",
                            depositCurrency)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        NavigableMap<Double, Item> currencyItems = ModItems.getCurrencyItems().get(depositCurrency);
        if (currencyItems == null) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.no_physical_currency",
                            depositCurrency)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        Map<Item, Integer> itemsNeeded = calculateItemsForDeposit(amount, currencyItems);
        if (itemsNeeded == null) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_denomination")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
        Map<Item, Integer> removedItems = new HashMap<>();
        boolean hasAll = true;

        for (Map.Entry<Item, Integer> entry : itemsNeeded.entrySet()) {
            Item item = entry.getKey();
            int needed = entry.getValue();
            int removed = removeItemsFromInventory(player, item, needed);
            if (removed < needed) {
                hasAll = false;
                removedItems.put(item, removed);
                break;
            }
            removedItems.put(item, removed);
        }
        if (!hasAll) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_enough_items")
                    .withStyle(ChatFormatting.RED));
            for (Map.Entry<Item, Integer> entry : removedItems.entrySet()) {
                if (entry.getValue() > 0) {
                    giveItemsToPlayer(player, entry.getKey(), entry.getValue());
                }
            }

            return 0;
        }
        double convertedAmount = amount;
        if (!depositCurrency.equals(acc.getCurrency())) {
            try {
                convertedAmount = convertCurrency(amount, depositCurrency, acc.getCurrency());
            } catch (IllegalArgumentException e) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.conversion_failed")
                        .withStyle(ChatFormatting.RED));
                for (Map.Entry<Item, Integer> entry : removedItems.entrySet()) {
                    giveItemsToPlayer(player, entry.getKey(), entry.getValue());
                }

                return 0;
            }
        }
        BigDecimal newBalance = BigDecimal.valueOf(acc.getBalance())
                .add(BigDecimal.valueOf(convertedAmount));

        if (newBalance.compareTo(MAX_AMOUNT) > 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.deposit_exceeds_limit")
                    .withStyle(ChatFormatting.RED));
            for (Map.Entry<Item, Integer> entry : removedItems.entrySet()) {
                giveItemsToPlayer(player, entry.getKey(), entry.getValue());
            }
            return 0;
        }
        synchronized (acc) {
            acc.setBalance(newBalance.doubleValue());
        }

        MinecraftServer server = source.getServer();
        BankAccountSavedData.get(server).setDirty();
        syncCardWithAccount(cardStack, acc);
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.deposit_success",
                        formatMoney(amount), depositCurrency,
                        formatMoney(acc.getBalance()), acc.getCurrency())
                .withStyle(ChatFormatting.GREEN));

        MoneyMod.LOGGER.info("[DEPOSIT] Player {} deposited {} {} to account {} (new balance: {} {})",
                player.getName().getString(), amount, depositCurrency,
                acc.getIban(), acc.getBalance(), acc.getCurrency());

        return Command.SINGLE_SUCCESS;
    }
    private static Map<Item, Integer> calculateItemsForDeposit(double amount,
                                                               NavigableMap<Double, Item> currencyItems) {
        Map<Item, Integer> result = new HashMap<>();
        BigDecimal remaining = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_EVEN);

        for (Map.Entry<Double, Item> entry : currencyItems.descendingMap().entrySet()) {
            BigDecimal denomination = BigDecimal.valueOf(entry.getKey()).setScale(2, RoundingMode.HALF_EVEN);

            if (denomination.compareTo(BigDecimal.ZERO) > 0 &&
                    remaining.compareTo(denomination) >= 0) {

                int count = remaining.divide(denomination, 0, RoundingMode.DOWN).intValue();

                if (count > 0) {
                    result.put(entry.getValue(), count);
                    remaining = remaining.subtract(denomination.multiply(BigDecimal.valueOf(count)));
                }
            }
        }
        if (remaining.abs().compareTo(BigDecimal.valueOf(0.01)) >= 0) {
            return null;
        }

        return result;
    }
    private static void giveItemsToPlayer(Player player, Item item, int count) {
        while (count > 0) {
            int stackSize = Math.min(count, item.getDefaultMaxStackSize());
            ItemStack stack = new ItemStack(item, stackSize);

            if (!player.getInventory().add(stack)) {
                dropItemNearPlayer(player, stack);
            }

            count -= stackSize;
        }
    }
    private static int removeItemsFromInventory(Player player, Item item, int count) {
        int removed = 0;
        for (int i = 0; i < player.getInventory().getContainerSize() && removed < count; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                int remove = Math.min(count - removed, stack.getCount());
                stack.shrink(remove);
                removed += remove;
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
        return removed;
    }
    private static int withdraw(CommandSourceStack source, double amount) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        Optional<BankAccount> optAcc = getAccountFromCard(stack, player, true);

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
        if (hasMoreThanTwoDecimals(amount)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.two_decimals").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (amount <= 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_positive").withStyle(ChatFormatting.RED));
            return 0;
        }
        String cardCurrency = acc.getCurrency();
        double currentBalance = acc.getBalance();

        boolean isAdminAction = !acc.getOwnerUuid().equals(player.getUUID());

        double fee;
        BigDecimal totalNeeded;

        if (isAdminAction) {
            fee = 0;
            totalNeeded = BigDecimal.valueOf(amount);
        } else {
            fee = calculateWithdrawFee(stack, amount);
            totalNeeded = BigDecimal.valueOf(amount).add(BigDecimal.valueOf(fee));
        }

        if (BigDecimal.valueOf(currentBalance).compareTo(totalNeeded) < 0) {
            if (isAdminAction) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_enough_funds")
                        .append(" ")
                        .append(Component.translatable("message.bubusteinmoneymod.available",
                                formatMoney(currentBalance), cardCurrency))
                        .withStyle(ChatFormatting.RED));
            } else {
                double feeRate = fee / amount;
                BigDecimal maxWithdrawable = BigDecimal.valueOf(currentBalance)
                        .divide(BigDecimal.ONE.add(BigDecimal.valueOf(feeRate)), 2, RoundingMode.DOWN);
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_enough_funds_with_fee",
                                formatMoney(maxWithdrawable.doubleValue()), cardCurrency,
                                formatMoney(calculateWithdrawFee(stack, maxWithdrawable.doubleValue())), cardCurrency)
                        .withStyle(ChatFormatting.RED));
            }
            return 0;
        }
        double remainingAmount;
        if (ModItems.getCurrencyItems().containsKey(cardCurrency)) {
            remainingAmount = withdrawCurrency(player, amount, cardCurrency);
            if (remainingAmount > 0) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.withdraw_partial",
                        formatMoney(remainingAmount), cardCurrency).withStyle(ChatFormatting.YELLOW));
            }
        } else {
            remainingAmount = amount;
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.withdraw_no_currency", cardCurrency)
                    .withStyle(ChatFormatting.YELLOW));
        }
        double actuallyWithdrawn = amount - remainingAmount;
        double actualFee;

        if (isAdminAction) {
            actualFee = 0;
        } else {
            actualFee = calculateWithdrawFee(stack, actuallyWithdrawn);
        }
        double newBalance = currentBalance - actuallyWithdrawn - actualFee;
        acc.setBalance(newBalance);
        syncCardWithAccount(stack, acc);
        if (isAdminAction) {
            MoneyMod.LOGGER.warn("[ADMIN ACTION] {} withdrew {} {} from card {} (owner: {})",
                    player.getName().getString(), formatMoney(actuallyWithdrawn), cardCurrency,
                    acc.getIban(), acc.getOwnerUuid());

            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.admin.withdraw_success",
                            formatMoney(actuallyWithdrawn), cardCurrency, formatMoney(newBalance), cardCurrency)
                    .withStyle(ChatFormatting.GOLD));
            ServerPlayer owner = getCardOwner(source.getServer(), acc);
            if (owner != null) {
                owner.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.admin.your_card_withdrawn",
                                player.getName().getString(), formatMoney(actuallyWithdrawn), cardCurrency)
                        .withStyle(ChatFormatting.RED));
            }
        } else {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.withdraw_success",
                            formatMoney(actuallyWithdrawn), cardCurrency,
                            formatMoney(actualFee), cardCurrency,
                            formatMoney(newBalance), cardCurrency)
                    .withStyle(ChatFormatting.GREEN));
        }

        return Command.SINGLE_SUCCESS;
    }


    private static double calculateWithdrawFee(ItemStack stack, double amount) {
        if (stack.getItem() == ModItems.Card.get()) return amount * 0.03; // 3% fee
        else if (stack.getItem() == ModItems.GoldCard.get()) return amount * 0.02; // 2% fee
        else if (stack.getItem() == ModItems.SteelCard.get()) return amount * 0.01; // 1% fee
        else if(stack.getItem() == ModItems.RustyCard.get()) return amount * 0.1; // 10% fee
        return 0;
    }
    private static double withdrawCurrency(Player player, double amount, String currency) {
        NavigableMap<Double, Item> items = ModItems.getCurrencyItems().get(currency);
        BigDecimal remainingAmount = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_EVEN);
        for (Map.Entry<Double, Item> entry : items.descendingMap().entrySet()) {
            BigDecimal denomination = BigDecimal.valueOf(entry.getKey()).setScale(2, RoundingMode.HALF_EVEN);
            Item currencyItem = entry.getValue();
            if (denomination.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal[] divideAndRemainder = remainingAmount.divideAndRemainder(denomination);
                int count = divideAndRemainder[0].intValue();
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        ItemStack currencyStack = new ItemStack(currencyItem);
                        if (!player.getInventory().add(currencyStack)) {
                            dropItemNearPlayer(player, currencyStack);
                        }
                    }
                    remainingAmount = divideAndRemainder[1].setScale(2, RoundingMode.HALF_EVEN);
                }
            }
        }
        return remainingAmount.doubleValue();
    }
    private static void dropItemNearPlayer(Player player, ItemStack stack) {
        Vec3 playerPos = player.position();
        double offsetX = player.getRandom().nextDouble() * 0.5 - 0.25;
        double offsetZ = player.getRandom().nextDouble() * 0.5 - 0.25;
        ItemEntity itemEntity = new ItemEntity(player.level(),
                playerPos.x + offsetX,
                playerPos.y + 0.5,
                playerPos.z + offsetZ,
                stack);
        player.level().addFreshEntity(itemEntity);
    }
    private static double convertCurrency(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }
        if (!ModItems.EXCHANGE_RATES.containsKey(fromCurrency)) {
            throw new IllegalArgumentException("Invalid source currency: " + fromCurrency);
        }
        if (!ModItems.EXCHANGE_RATES.containsKey(toCurrency)) {
            throw new IllegalArgumentException("Invalid target currency: " + toCurrency);
        }
        BigDecimal amountBD = BigDecimal.valueOf(amount);
        BigDecimal fromRate = BigDecimal.valueOf(ModItems.EXCHANGE_RATES.get(fromCurrency));
        BigDecimal toRate = BigDecimal.valueOf(ModItems.EXCHANGE_RATES.get(toCurrency));
        return amountBD.divide(fromRate, 10, RoundingMode.HALF_UP)
                .multiply(toRate)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
    private static Optional<BankAccount> getAccountFromCard(ItemStack stack, ServerPlayer player) {
        return getAccountFromCard(stack, player, false);
    }
    private static Optional<BankAccount> getAccountFromCard(ItemStack stack, ServerPlayer player, boolean allowAdminBypass) {
        if (!(stack.getItem() instanceof CardItem)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_card").withStyle(ChatFormatting.RED));
            return Optional.empty();
        }
        String iban = CardItem.getIban(stack);
        if(iban==null || iban.isEmpty()){
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_no_iban")
                    .withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_no_iban_hint")
                    .withStyle(ChatFormatting.GRAY));
            return Optional.empty();}

        MinecraftServer server = player.getServer();
        if (server == null) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.internal_error_server")
                    .withStyle(ChatFormatting.RED));
            return Optional.empty();
        }
        BankAccountManager mgr = BankAccountManager.get();
        Optional<BankAccount> opt = mgr.getByIban(server, iban);
        if (opt.isEmpty()){
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.account_not_found_from_card", iban)
                    .withStyle(ChatFormatting.RED));
            return Optional.empty();}

        BankAccount acc = opt.get();
        boolean isOwner = acc.getOwnerUuid().equals(player.getUUID());
        boolean isAdmin = allowAdminBypass && player.hasPermissions(2);

        if (!isOwner && !isAdmin) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_card_owner")
                    .withStyle(ChatFormatting.RED));
            return Optional.empty();
        }

        return opt;
    }
    private static ServerPlayer getCardOwner(MinecraftServer server, BankAccount account) {
        return server.getPlayerList().getPlayer(account.getOwnerUuid());
    }
    private static void syncCardWithAccount(ItemStack stack, BankAccount account) {
        if (stack.getItem() instanceof CardItem) {
            stack.set(CardItem.MONEY_COMPONENT.get(), account.getBalance());
            stack.set(CardItem.CURRENCY_COMPONENT.get(), account.getCurrency());
        }
    }
    private static String formatMoney(double amount) {
        return CardItem.formatMoney(amount);
    }
    private static boolean hasMoreThanTwoDecimals(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        return bd.stripTrailingZeros().scale() > 2;
    }
}