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
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bubustein")
                .then(Commands.literal("help")
                        .executes(context -> showHelp(context.getSource())))
                .then(Commands.literal("setcurrency")
                        .then(Commands.argument("currency", StringArgumentType.word())
                                .executes(context -> setCurrency(context.getSource(), StringArgumentType.getString(context, "currency")))))
                .then(Commands.literal("setdefaultcurrency")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("currency", StringArgumentType.word())
                                .executes(context -> setDefaultCurrency(context.getSource(), StringArgumentType.getString(context, "currency")))))
                .then(Commands.literal("deposit")
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(context -> deposit(context.getSource(), DoubleArgumentType.getDouble(context, "amount"), null))
                                .then(Commands.argument("currency", StringArgumentType.word())
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
                                        .executes(context -> ecoAddMoney(context.getSource(), DoubleArgumentType.getDouble(context, "amount"), StringArgumentType.getString(context, "currency"))))))
                .then(Commands.literal("ecoSetMoney")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(context -> ecoSetMoney(context.getSource(), DoubleArgumentType.getDouble(context, "amount"), null))
                                .then(Commands.argument("currency", StringArgumentType.word())
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
            player.sendSystemMessage(Component.literal("====== Bubustein Commands ======").withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true)));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein help", "Display this list of commands."));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein setcurrency <currency>", "Change the currency of the card in hand."));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein setdefaultcurrency <currency>", "Set the default currency for all new cards.", true));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein deposit <amount> [currency]", "Deposit money into the card in hand."));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein withdraw <amount>", "Withdraw money from the card in hand."));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein defaultCurrency", "Display the current default currency."));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein ecoAddMoney <amount> [currency]", "Add money to the card in hand.", true));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein ecoSetMoney <amount> [currency]", "Set the amount of money on the card in hand.", true));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein resetMoney", "Reset the amount of money on the card in hand to 0.", true));
            player.sendSystemMessage(createStyledHelpMessage("/bubustein pay <player> <amount>", "Transfer money to another player."));
            player.sendSystemMessage(Component.literal("=======================================================================").withStyle(ChatFormatting.GOLD));
        }
        return Command.SINGLE_SUCCESS;
    }
    private static MutableComponent createStyledHelpMessage(String command, String description) {
        return createStyledHelpMessage(command, description, false);
    }
    private static MutableComponent createStyledHelpMessage(String command, String description, boolean requiresOp) {
        MutableComponent styledCommand = Component.literal(command).withStyle(ChatFormatting.AQUA);
        MutableComponent styledDescription = Component.literal(": " + description).withStyle(ChatFormatting.GRAY);
        MutableComponent fullMessage = styledCommand.append(styledDescription);
        if (requiresOp) {
            fullMessage.append(Component.literal(" (Requires OP)")
                    .withStyle(style -> style.withColor(ChatFormatting.RED).withBold(true)));
        }
        return fullMessage;
    }
    private static int ecoAddMoney(CommandSourceStack source, double amount, String specifiedCurrency) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof CardItem cardItem)) {
            player.sendSystemMessage(Component.literal("You must hold a card in your hand.").withStyle(ChatFormatting.RED));
            return 0;
        }
        if(amount != ((int)(amount*100))/100.0){
            player.sendSystemMessage(Component.literal("The amount must have only 2 decimals.").withStyle(ChatFormatting.RED));
            return 0;
        }
        String cardCurrency = cardItem.getCurrency(stack);
        String addCurrency = (specifiedCurrency != null) ? specifiedCurrency : cardCurrency;
        if (!ModItems.EXCHANGE_RATES.containsKey(addCurrency)) {
            player.sendSystemMessage(Component.literal("Invalid currency. Available currencies are: " + String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
            return 0;
        }
        double amountInCardCurrency = convertCurrency(amount, addCurrency, cardCurrency);
        cardItem.addMoney(stack, amountInCardCurrency);
        player.sendSystemMessage(Component.literal("You added " + formatMoney(amount) + " " + addCurrency +
                " to the card. New balance: " + formatMoney(cardItem.getMoney(stack)) + " " + cardCurrency).withStyle(ChatFormatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }
    private static int ecoSetMoney(CommandSourceStack source, double amount, String specifiedCurrency) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof CardItem cardItem)) {
            player.sendSystemMessage(Component.literal("You must hold a card in your hand.").withStyle(ChatFormatting.RED));
            return 0;
        }
        if(amount != ((int)(amount*100))/100.0){
            player.sendSystemMessage(Component.literal("The amount must have only 2 decimals.").withStyle(ChatFormatting.RED));
            return 0;
        }
        String cardCurrency = cardItem.getCurrency(stack);
        String setCurrency = (specifiedCurrency != null) ? specifiedCurrency : cardCurrency;
        if (!ModItems.EXCHANGE_RATES.containsKey(setCurrency)) {
            player.sendSystemMessage(Component.literal("Invalid currency. Available currencies are: " + String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
            return 0;
        }
        double amountInCardCurrency = convertCurrency(amount, setCurrency, cardCurrency);
        cardItem.setMoney(stack, amountInCardCurrency);
        player.sendSystemMessage(Component.literal("You set the amount to " + formatMoney(amount) + " " + setCurrency +
                " on the card. New balance: " + formatMoney(cardItem.getMoney(stack)) + " " + cardCurrency).withStyle(ChatFormatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }
    private static int resetMoney(CommandSourceStack source) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof CardItem cardItem)) {
            player.sendSystemMessage(Component.literal("You must hold a card in your hand.").withStyle(ChatFormatting.RED));
            return 0;
        }
        String cardCurrency = cardItem.getCurrency(stack);
        cardItem.setMoney(stack, 0);
        player.sendSystemMessage(Component.literal("You reset the amount on the card to 0 " + cardCurrency + ".").withStyle(ChatFormatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }
    private static int pay(CommandSourceStack source, String targetPlayerName, double amount) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(targetPlayerName);
        if (targetPlayer == null) {
            player.sendSystemMessage(Component.literal("Player " + targetPlayerName + " is not online.").withStyle(ChatFormatting.RED));
            return 0;
        }
        ItemStack playerStack = player.getMainHandItem();
        ItemStack targetStack = targetPlayer.getMainHandItem();
        if (!(playerStack.getItem() instanceof CardItem playerCard)) {
            player.sendSystemMessage(Component.literal("You must hold a card in your hand.").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!(targetStack.getItem() instanceof CardItem targetCard)) {
            player.sendSystemMessage(Component.literal(targetPlayerName + " doesn't hold a card in their hand.").withStyle(ChatFormatting.RED));
            return 0;
        }
        String playerCurrency = playerCard.getCurrency(playerStack);
        String targetCurrency = targetCard.getCurrency(targetStack);
        if (amount <= 0) {
            player.sendSystemMessage(Component.literal("The amount must be greater than 0.").withStyle(ChatFormatting.RED));
            return 0;
        }
        if(amount != ((int)(amount*100))/100.0){
            player.sendSystemMessage(Component.literal("The amount must have only 2 decimals.").withStyle(ChatFormatting.RED));
            return 0;
        }
        double playerBalance = playerCard.getMoney(playerStack);
        if (playerBalance < amount) {
            player.sendSystemMessage(Component.literal("You don't have enough money on your card.").withStyle(ChatFormatting.RED));
            return 0;
        }
        double amountInTargetCurrency = amount;
        if (!playerCurrency.equals(targetCurrency)) {
            amountInTargetCurrency = convertCurrency(amount, playerCurrency, targetCurrency);
        }
        playerCard.setMoney(playerStack, playerBalance - amount);
        targetCard.addMoney(targetStack, amountInTargetCurrency);
        player.sendSystemMessage(Component.literal("You transferred " + formatMoney(amount) + " " + playerCurrency + " to " + targetPlayerName + ".").withStyle(ChatFormatting.GREEN));
        targetPlayer.sendSystemMessage(Component.literal("You received " + formatMoney(amountInTargetCurrency) + " " + targetCurrency + " from " + player.getName().getString() + ".").withStyle(ChatFormatting.GREEN));
        return Command.SINGLE_SUCCESS;
    }
    private static int setCurrency(CommandSourceStack source, String currency) throws CommandSyntaxException {
        if (ModItems.EXCHANGE_RATES.containsKey(currency)) {
            Player player = source.getPlayerOrException();
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof CardItem cardItem) {
                String oldCurrency = cardItem.getCurrency(stack);
                cardItem.setCurrency(stack, currency);
                cardItem.convertMoney(stack, oldCurrency, currency);
                player.sendSystemMessage(Component.literal("The currency of the card in hand has been changed to " + currency +
                        ". New balance: " + formatMoney(cardItem.getMoney(stack)) + " " + currency).withStyle(ChatFormatting.GREEN));
            } else {
                source.sendFailure(Component.literal("You must hold a card in your hand to execute this command.").withStyle(ChatFormatting.RED));
            }
        } else {
            source.sendFailure(Component.literal("Invalid currency. Available currencies are: " + String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
        }
        return Command.SINGLE_SUCCESS;
    }
    private static int setDefaultCurrency(CommandSourceStack source, String currency) {
        if (ModItems.EXCHANGE_RATES.containsKey(currency)) {
            for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
                for (ItemStack stack : player.getInventory().items) {
                    if (stack.getItem() instanceof CardItem cardItem) {
                        String oldCurrency = cardItem.getCurrency(stack);
                        cardItem.setCurrency(stack, currency);
                        cardItem.convertMoney(stack, oldCurrency, currency);

                        player.sendSystemMessage(Component.literal("Your card has been converted from " + oldCurrency +
                                        " to " + currency + ": " + formatMoney(cardItem.getMoney(stack)) + " " + currency)
                                .withStyle(ChatFormatting.GREEN));
                    }
                }
            }
            MoneyMod.setDefaultCurrency(currency);
            MoneyMod.saveConfig(source.getServer());
            source.sendSuccess(() -> Component.literal("The default currency has been set to " + currency).withStyle(ChatFormatting.GREEN), true);
        } else {
            source.sendFailure(Component.literal("Invalid currency. Available currencies are: " + String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
        }
        return Command.SINGLE_SUCCESS;
    }
    private static int showDefaultCurrency(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("The current default currency is: " + MoneyMod.getDefaultCurrency()).withStyle(ChatFormatting.GREEN), false);
        return Command.SINGLE_SUCCESS;
    }
    private static int deposit(CommandSourceStack source, double amount, String specifiedCurrency) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof CardItem cardItem) {
            String cardCurrency = cardItem.getCurrency(stack);
            String depositCurrency = (specifiedCurrency != null) ? specifiedCurrency : cardCurrency;
            if (!ModItems.EXCHANGE_RATES.containsKey(depositCurrency)) {
                player.sendSystemMessage(Component.literal("Invalid currency. Available currencies are: " + String.join(", ", ModItems.EXCHANGE_RATES.keySet())).withStyle(ChatFormatting.RED));
                return 0;
            }
            if(amount != ((int)(amount*100))/100.0){
                player.sendSystemMessage(Component.literal("The amount must have only 2 decimals.").withStyle(ChatFormatting.RED));
                return 0;
            }
            double totalDeposited = 0;
            TreeMap<Double, Item> items = ModItems.CURRENCY_ITEMS.get(depositCurrency);
            if (items == null) {
                player.sendSystemMessage(Component.literal("We don't have banknotes/coins for the currency " + depositCurrency).withStyle(ChatFormatting.RED));
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
                player.sendSystemMessage(Component.literal("You don't have enough funds to make this deposit. Available: " + formatMoney(totalAvailable) + " " + depositCurrency).withStyle(ChatFormatting.RED));
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
                player.sendSystemMessage(Component.literal(String.format("You deposited %.2f %s into the card (%.2f %s). New balance: " + formatMoney(cardItem.getMoney(stack)) + " " + cardCurrency, totalDeposited, depositCurrency, convertCurrency(totalDeposited, depositCurrency, cardCurrency), cardCurrency)).withStyle(ChatFormatting.GREEN));
                if (remainingAmount > 0.01) {
                    player.sendSystemMessage(Component.literal(String.format("The exact amount couldn't be deposited. Uncovered difference: %.2f %s", remainingAmount, depositCurrency)).withStyle(ChatFormatting.YELLOW));
                }
            } else {
                player.sendSystemMessage(Component.literal("You don't have enough banknotes/coins to make the deposit.").withStyle(ChatFormatting.RED));
            }
            player.inventoryMenu.broadcastChanges();
        } else {
            player.sendSystemMessage(Component.literal("You must hold a card in your hand.").withStyle(ChatFormatting.RED));
        }
        return Command.SINGLE_SUCCESS;
    }
    private static void removeItemsFromInventory(Player player, Item item, int count) {
        int removedCount = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                int toRemove = Math.min(stack.getCount(), count - removedCount);
                stack.shrink(toRemove);
                removedCount += toRemove;
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
                if (removedCount >= count) {
                    break;
                }
            }
        }
    }
    private static int withdraw(CommandSourceStack source, double amount) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof CardItem cardItem) {
            if(amount != ((int)(amount*100))/100.0){
                player.sendSystemMessage(Component.literal("The amount must have only 2 decimals.").withStyle(ChatFormatting.RED));
                return 0;
            }
            String cardCurrency = cardItem.getCurrency(stack);
            double currentBalance = cardItem.getMoney(stack);
            double feeInCardCurrency = calculateWithdrawFee(stack, amount);
            double totalWithdrawInCardCurrency = amount + feeInCardCurrency;
            if (currentBalance >= totalWithdrawInCardCurrency) {
                cardItem.setMoney(stack, currentBalance - totalWithdrawInCardCurrency);
                double remainingAmount = 0;
                if (ModItems.CURRENCY_ITEMS.containsKey(cardCurrency)) {
                    remainingAmount = withdrawCurrency(player, amount, cardCurrency);
                    if (remainingAmount > 0) {
                        cardItem.addMoney(stack, remainingAmount);
                        player.sendSystemMessage(Component.literal(
                                String.format("The exact amount couldn't be withdrawn. " + formatMoney(remainingAmount) + " " + cardCurrency + " has been returned to your card due to missing denominations.")).withStyle(ChatFormatting.YELLOW));
                    }
                } else {
                    player.sendSystemMessage(Component.literal(
                                    "No physical currency is available for " + cardCurrency + ". The amount was still deducted from your card.")
                            .withStyle(ChatFormatting.YELLOW));
                }
                player.sendSystemMessage(Component.literal(
                        String.format("You withdrew " + formatMoney(amount-remainingAmount) + " " + cardCurrency + ". Withdrawal fee: " + formatMoney(feeInCardCurrency) + " " + cardCurrency + ". New balance: " + formatMoney(cardItem.getMoney(stack)) + " " + cardCurrency)).withStyle(ChatFormatting.GREEN));
            } else {
                player.sendSystemMessage(Component.literal("You don't have enough funds for this withdrawal.").withStyle(ChatFormatting.RED));
            }
        } else {
            player.sendSystemMessage(Component.literal("You must hold a card in your hand.").withStyle(ChatFormatting.RED));
        }
        return Command.SINGLE_SUCCESS;
    }
    private static double calculateWithdrawFee(ItemStack stack, double amount) {
        if (stack.getItem() == ModItems.VisaClassic.get()) {
            return amount * 0.03; // 3% fee
        } else if (stack.getItem() == ModItems.VisaGold.get()) {
            return amount * 0.02; // 2% fee
        } else if (stack.getItem() == ModItems.VisaSteel.get()) {
            return amount * 0.005; // 0.5% fee
        }
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
        double fromRate = ModItems.EXCHANGE_RATES.get(fromCurrency);
        double toRate = ModItems.EXCHANGE_RATES.get(toCurrency);
        double amountInEUR = amount / fromRate;
        return amountInEUR * toRate;
    }
    private static String formatMoney(double amount) {
        return String.format("%.2f", ((int)(amount*100))/100.0);
    }
}