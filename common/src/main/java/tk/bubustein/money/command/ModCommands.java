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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.item.CardItem;
import tk.bubustein.money.item.ModItems;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ModCommands {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000");
    private static final Set<String> CURRENCIES_CACHE = Collections.unmodifiableSet(ModItems.EXCHANGE_RATES.keySet());
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bubustein")
                .then(Commands.literal("help")
                        .executes(context -> showHelp(context.getSource())))
                .then(Commands.literal("setcurrency")
                        .then(Commands.argument("currency", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String input = builder.getRemaining().toLowerCase();
                                    CURRENCIES_CACHE.stream()
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
                                    CURRENCIES_CACHE.stream()
                                            .filter(c -> c.toLowerCase().startsWith(input))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> setDefaultCurrency(context.getSource(), StringArgumentType.getString(context, "currency")))))
                .then(Commands.literal("deposit")
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(context -> deposit(context.getSource(), DoubleArgumentType.getDouble(context, "amount"), null))
                                .then(Commands.argument("currency", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String input = builder.getRemaining().toLowerCase();
                                            CURRENCIES_CACHE.stream()
                                                    .filter(c -> c.toLowerCase().startsWith(input))
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> deposit(context.getSource(), DoubleArgumentType.getDouble(context, "amount"), StringArgumentType.getString(context, "currency"))))))
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
                                            CURRENCIES_CACHE.stream()
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
                                            CURRENCIES_CACHE.stream()
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
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.title").withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true)));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein help", "message.bubusteinmoneymod.help"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein setcurrency <currency>", "message.bubusteinmoneymod.setcurrency"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein setdefaultcurrency <currency>", "message.bubusteinmoneymod.setdefaultcurrency", true));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein deposit <amount> [currency]", "message.bubusteinmoneymod.deposit"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein withdraw <amount>", "message.bubusteinmoneymod.withdraw"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein defaultCurrency", "message.bubusteinmoneymod.defaultCurrency"));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein ecoAddMoney <amount> [currency]", "message.bubusteinmoneymod.ecoAddMoney", true));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein ecoSetMoney <amount> [currency]", "message.bubusteinmoneymod.ecoSetMoney", true));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein resetMoney", "message.bubusteinmoneymod.resetMoney", true));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein pay <player> <amount>", "message.bubusteinmoneymod.pay"));
            player.sendSystemMessage(Component.literal("=======================================================================").withStyle(ChatFormatting.GOLD));
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
    private static int ecoAddMoney(CommandSourceStack source, double amount, String specifiedCurrency) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof CardItem cardItem)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_card").withStyle(ChatFormatting.RED));
            return 0;
        }
        if(amount <= 0){
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_positive").withStyle(ChatFormatting.RED));
            return 0;
        }
        if(amount != Math.round(amount * 100) / 100.0){
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.two_decimals").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (BigDecimal.valueOf(cardItem.getMoney(stack)+amount).compareTo(MAX_AMOUNT) > 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large", MAX_AMOUNT).withStyle(ChatFormatting.RED));
            return 0;
        }
        String cardCurrency = cardItem.getCurrency(stack);
        String addCurrency = (specifiedCurrency != null) ? specifiedCurrency : cardCurrency;
        if (!ModItems.EXCHANGE_RATES.containsKey(addCurrency)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_currency", String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
            return 0;
        }
        double amountInCardCurrency = convertCurrency(amount, addCurrency, cardCurrency);
        cardItem.addMoney(stack, amountInCardCurrency);
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_added", formatMoney(amount), addCurrency,
                formatMoney(cardItem.getMoney(stack)), cardCurrency).withStyle(ChatFormatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }
    private static int ecoSetMoney(CommandSourceStack source, double amount, String specifiedCurrency) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof CardItem cardItem)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_card").withStyle(ChatFormatting.RED));
            return 0;
        }
        if(amount <= 0){
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_positive").withStyle(ChatFormatting.RED));
            return 0;
        }
        if(amount != Math.round(amount * 100) / 100.0){
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.two_decimals").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (BigDecimal.valueOf(cardItem.getMoney(stack)+amount).compareTo(MAX_AMOUNT) > 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large", MAX_AMOUNT).withStyle(ChatFormatting.RED));
            return 0;
        }
        String cardCurrency = cardItem.getCurrency(stack);
        String setCurrency = (specifiedCurrency != null) ? specifiedCurrency : cardCurrency;
        if (!ModItems.EXCHANGE_RATES.containsKey(setCurrency)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_currency", String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
            return 0;
        }
        double amountInCardCurrency = convertCurrency(amount, setCurrency, cardCurrency);
        cardItem.setMoney(stack, amountInCardCurrency);
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_set", formatMoney(amount), setCurrency,
                formatMoney(cardItem.getMoney(stack)), cardCurrency).withStyle(ChatFormatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }
    private static int resetMoney(CommandSourceStack source) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof CardItem cardItem)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_card").withStyle(ChatFormatting.RED));
            return 0;
        }
        String cardCurrency = cardItem.getCurrency(stack);
        cardItem.setMoney(stack, 0);
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.card_reset", cardCurrency).withStyle(ChatFormatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }
    private static int pay(CommandSourceStack source, String targetPlayerName, double amount) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(targetPlayerName);
        if (targetPlayer == null) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.player_not_online", targetPlayerName).withStyle(ChatFormatting.RED));
            return 0;
        }
        if (targetPlayerName.equalsIgnoreCase(player.getName().getString())) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.cannot_pay_self").withStyle(ChatFormatting.RED));
            return 0;
        }
        if(amount <= 0){
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_positive").withStyle(ChatFormatting.RED));
            return 0;
        }
        ItemStack playerStack = player.getMainHandItem();
        ItemStack targetStack = targetPlayer.getMainHandItem();
        if (!(playerStack.getItem() instanceof CardItem playerCard)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_card").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!(targetStack.getItem() instanceof CardItem targetCard)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.target_no_card", targetPlayerName).withStyle(ChatFormatting.RED));
            return 0;
        }
        if (BigDecimal.valueOf(targetCard.getMoney(targetStack)+amount).compareTo(MAX_AMOUNT) > 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large", MAX_AMOUNT).withStyle(ChatFormatting.RED));
            return 0;
        }
        String playerCurrency = playerCard.getCurrency(playerStack);
        String targetCurrency = targetCard.getCurrency(targetStack);
        if(amount != Math.round(amount * 100) / 100.0){
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.two_decimals").withStyle(ChatFormatting.RED));
            return 0;
        }
        double playerBalance = playerCard.getMoney(playerStack);
        if (playerBalance < amount) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_enough_funds").withStyle(ChatFormatting.RED));
            return 0;
        }
        double amountInTargetCurrency = amount;
        if (!playerCurrency.equals(targetCurrency)) {
            amountInTargetCurrency = convertCurrency(amount, playerCurrency, targetCurrency);
        }
        playerCard.setMoney(playerStack, playerBalance - amount);
        targetCard.addMoney(targetStack, amountInTargetCurrency);
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.transfer_success", formatMoney(amount), playerCurrency, targetPlayerName).withStyle(ChatFormatting.GREEN));
        targetPlayer.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.receive_success", formatMoney(amountInTargetCurrency), targetCurrency, player.getName().getString()).withStyle(ChatFormatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }
    private static int setCurrency(CommandSourceStack source, String currency) throws CommandSyntaxException {
        if (ModItems.EXCHANGE_RATES.containsKey(currency)) {
            Player player = source.getPlayerOrException();
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof CardItem cardItem) {
                String oldCurrency = cardItem.getCurrency(stack);
                double currentAmount = cardItem.getMoney(stack);
                double convertedAmount = convertCurrency(currentAmount, oldCurrency, currency);
                if (BigDecimal.valueOf(convertedAmount).compareTo(MAX_AMOUNT) > 0) {
                    player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large", MAX_AMOUNT).withStyle(ChatFormatting.RED));
                    return 0;
                }
                cardItem.setCurrency(stack, currency);
                cardItem.setMoney(stack, convertedAmount);
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.currency_changed", currency, formatMoney(convertedAmount), currency).withStyle(ChatFormatting.GREEN));
            } else {
                source.sendFailure(Component.translatable("message.bubusteinmoneymod.hold_card").withStyle(ChatFormatting.RED));
            }
        } else {
            source.sendFailure(Component.translatable("message.bubusteinmoneymod.invalid_currency", String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
        }
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
    private static int deposit(CommandSourceStack source, double amount, String specifiedCurrency) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof CardItem cardItem) {
            String cardCurrency = cardItem.getCurrency(stack);
            String depositCurrency = (specifiedCurrency != null) ? specifiedCurrency : cardCurrency;
            if (!ModItems.EXCHANGE_RATES.containsKey(depositCurrency)) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.invalid_currency", String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
                return 0;
            }
            if(amount != Math.round(amount * 100) / 100.0){
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.two_decimals").withStyle(ChatFormatting.RED));
                return 0;
            }
            double totalDeposited = 0;
            TreeMap<Double, Item> items = ModItems.CURRENCY_ITEMS.get(depositCurrency);
            if (items == null) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.no_physical_currency", depositCurrency).withStyle(ChatFormatting.RED));
                return 0;
            }
            if (BigDecimal.valueOf(cardItem.getMoney(stack)+amount).compareTo(MAX_AMOUNT) > 0) {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large", MAX_AMOUNT).withStyle(ChatFormatting.RED));
                return 0;
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
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_enough_funds").append(" ").append(
                        Component.translatable("message.bubusteinmoneymod.available", formatMoney(totalAvailable), depositCurrency)).withStyle(ChatFormatting.RED));
                return 0;
            }
            double remainingAmount = amount;
            for (Map.Entry<Double, Item> entry : items.descendingMap().entrySet()) {
                double denomination = entry.getKey();
                Item item = entry.getValue();
                int countAvailable = availableItems.get(item);
                int countNeeded = (int) Math.min(remainingAmount / denomination, countAvailable);
                if (countNeeded > 0) {
                    double depositedAmount = denomination * countNeeded;
                    totalDeposited += depositedAmount;
                    removeItemsFromInventory(player, item, countNeeded);
                    remainingAmount -= depositedAmount;
                }
                if (remainingAmount < 0.01) break;
            }
            if (totalDeposited > 0) {
                cardItem.addMoney(stack, convertCurrency(totalDeposited, depositCurrency, cardCurrency));
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.deposit_success",
                        formatMoney(totalDeposited), depositCurrency,
                        formatMoney(convertCurrency(totalDeposited, depositCurrency, cardCurrency)), cardCurrency,
                        formatMoney(cardItem.getMoney(stack)), cardCurrency).withStyle(ChatFormatting.GREEN));
                if (remainingAmount > 0.01) {
                    player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.deposit_partial",
                            formatMoney(remainingAmount), depositCurrency).withStyle(ChatFormatting.YELLOW));
                }
            } else {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_enough_funds").withStyle(ChatFormatting.RED));
            }
            player.inventoryMenu.broadcastChanges();
        } else {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_card").withStyle(ChatFormatting.RED));
        }
        return Command.SINGLE_SUCCESS;
    }
    public static void removeItemsFromInventory(Player player, Item item, int count) {
        for (int i = 0; i < player.getInventory().getContainerSize() && count > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                int remove = Math.min(count, stack.getCount());
                stack.shrink(remove);
                count -= remove;
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }
    private static int withdraw(CommandSourceStack source, double amount) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof CardItem cardItem)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_card").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (amount != Math.round(amount * 100) / 100.0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.two_decimals").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (amount <= 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_positive").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (BigDecimal.valueOf(cardItem.getMoney(stack)+amount).compareTo(MAX_AMOUNT) > 0) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.amount_too_large", MAX_AMOUNT).withStyle(ChatFormatting.RED));
            return 0;
        }
        String cardCurrency = cardItem.getCurrency(stack);
        double currentBalance = cardItem.getMoney(stack);
        double fee = calculateWithdrawFee(stack, amount);
        BigDecimal totalWithdraw = BigDecimal.valueOf(amount).add(BigDecimal.valueOf(fee));
        if (BigDecimal.valueOf(currentBalance).compareTo(totalWithdraw) < 0) {
            BigDecimal maxWithdrawable = BigDecimal.valueOf(currentBalance)
                    .divide(BigDecimal.ONE.add(BigDecimal.valueOf(fee).divide(BigDecimal.valueOf(amount), 4, RoundingMode.HALF_UP)), RoundingMode.DOWN);
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.not_enough_funds_with_fee",
                    formatMoney(maxWithdrawable.doubleValue()), cardCurrency,
                    formatMoney(fee), cardCurrency).withStyle(ChatFormatting.RED));
            return 0;
        }
        double remainingAmount = 0;
        if (ModItems.CURRENCY_ITEMS.containsKey(cardCurrency)) {
            remainingAmount = withdrawCurrency(player, amount, cardCurrency);
            if (remainingAmount > 0) {
                cardItem.addMoney(stack, remainingAmount);
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.withdraw_partial",
                        formatMoney(remainingAmount), cardCurrency).withStyle(ChatFormatting.YELLOW));
            }
        } else {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.withdraw_no_currency",
                    cardCurrency).withStyle(ChatFormatting.YELLOW));
        }
        double newBalance = BigDecimal.valueOf(currentBalance)
                .subtract(BigDecimal.valueOf(amount)
                        .subtract(BigDecimal.valueOf(fee))
                        .add(BigDecimal.valueOf(remainingAmount)))
                        .doubleValue();
        cardItem.setMoney(stack, newBalance);
        player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.withdraw_success",
                formatMoney(amount - remainingAmount), cardCurrency,
                formatMoney(fee), cardCurrency,
                formatMoney(newBalance), cardCurrency).withStyle(ChatFormatting.GREEN));

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
        TreeMap<Double, Item> items = ModItems.CURRENCY_ITEMS.get(currency);
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
    public static double convertCurrency(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }
        BigDecimal amountBD = BigDecimal.valueOf(amount);
        BigDecimal fromRate = BigDecimal.valueOf(ModItems.EXCHANGE_RATES.get(fromCurrency));
        BigDecimal toRate = BigDecimal.valueOf(ModItems.EXCHANGE_RATES.get(toCurrency));
        return amountBD.divide(fromRate, 10, RoundingMode.HALF_UP)
                .multiply(toRate)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
    private static String formatMoney(double amount) {
        return String.format("%.2f", Math.round(amount * 100) / 100.0);
    }
}