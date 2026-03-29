package tk.bubustein.money.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.bank.BankAccount;
import tk.bubustein.money.config.ModConfig;
import tk.bubustein.money.item.ModItems;
import tk.bubustein.money.util.CardUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CurrencyCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bubustein")
                .then(Commands.literal("setcurrency")
                        .then(Commands.argument("currency", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String input = builder.getRemaining().toLowerCase();
                                    ModItems.EXCHANGE_RATES.keySet().stream()
                                            .filter(c -> c.toLowerCase().startsWith(input))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> setCurrency(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "currency")))))

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
                                .executes(context -> setDefaultCurrency(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "currency")))))

                .then(Commands.literal("defaultCurrency")
                        .executes(context -> showDefaultCurrency(context.getSource())))

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
                                .executes(context -> showRates(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "currency")))))

                .then(Commands.literal("updateRates")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> updateRates(context.getSource())))
        );
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
    private static int setCurrency(CommandSourceStack source, String currency) throws CommandSyntaxException {
        if (!ModItems.EXCHANGE_RATES.containsKey(currency)) {
            source.sendFailure(Component.translatable("message.bubusteinmoneymod.invalid_currency",
                    String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
            return 0;
        }

        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();

        Optional<BankAccount> optAcc = ModCommands.getAccountFromCard(stack, player);
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

        double convertedAmount = ModCommands.convertCurrency(currentAmount, oldCurrency, currency);
        if (BigDecimal.valueOf(convertedAmount).compareTo(ModCommands.MAX_AMOUNT) > 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large",
                    CardUtils.formatMoney(ModCommands.MAX_AMOUNT.doubleValue())).withStyle(ChatFormatting.RED));
            return 0;
        }

        acc.setCurrency(currency);
        acc.setBalance(convertedAmount);
        ModCommands.syncCardWithAccount(stack, acc);

        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.currency_changed",
                currency, CardUtils.formatMoney(convertedAmount), currency).withStyle(ChatFormatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }
    private static int setDefaultCurrency(CommandSourceStack source, String currency) {
        if (ModItems.EXCHANGE_RATES.containsKey(currency)) {
            MoneyMod.setDefaultCurrency(currency);
            MoneyMod.saveConfig(source.getServer());
            source.sendSuccess(() -> Component.translatable("message.bubusteinmoneymod.default_currency_set", currency).withStyle(ChatFormatting.GREEN), true);
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendFailure(Component.translatable("message.bubusteinmoneymod.invalid_currency", String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
            return 0;
        }
    }
    private static int showDefaultCurrency(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("message.bubusteinmoneymod.default_currency_show", MoneyMod.getDefaultCurrency()).withStyle(ChatFormatting.GREEN), false);
        return Command.SINGLE_SUCCESS;
    }
}
