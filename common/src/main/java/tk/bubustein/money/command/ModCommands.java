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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import tk.bubustein.money.bank.*;
import tk.bubustein.money.item.CardItem;
import tk.bubustein.money.item.CreditCardItem;
import tk.bubustein.money.item.ModItems;
import tk.bubustein.money.util.CardUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ModCommands {
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000");
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bubustein")
                        .then(Commands.literal("help")
                                .executes(context -> showHelp(context.getSource()))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(context -> showHelpPage(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "page")
                                        ))
                                )
                        )
        );
        AccountCommands.register(dispatcher);
        AdminCommands.register(dispatcher);
        BankCommands.register(dispatcher);
        CurrencyCommands.register(dispatcher);
        TransferCommands.register(dispatcher);
    }

    private static final int HELP_PAGE_SIZE = 6;

    // Structura unei intrări de help: command, descriptionKey, requiresOp
    private static final Object[][] HELP_ENTRIES = {
            // Sortate alfabetic după comandă
            {"/bubustein accounts [player]",                    "message.bubusteinmoneymod.accounts",         false},
            {"/bubustein bank create <prefix> <name>",          "message.bubusteinmoneymod.bank_create",      true},
            {"/bubustein bank delete <prefix>",                 "message.bubusteinmoneymod.bank_delete",      true},
            {"/bubustein banks",                                "message.bubusteinmoneymod.banks",            false},
            {"/bubustein confirm",                              "message.bubusteinmoneymod.confirm",          false},
            {"/bubustein createAccount <type> [bank]",          "message.bubusteinmoneymod.createAccount",    false},
            {"/bubustein defaultCurrency",                      "message.bubusteinmoneymod.defaultCurrency",  false},
            {"/bubustein deleteAccount [iban]",                 "message.bubusteinmoneymod.deleteAccount",    false},
            {"/bubustein deposit <amount> [currency]",          "message.bubusteinmoneymod.deposit",          false},
            {"/bubustein ecoAddMoney <amount> [currency]",      "message.bubusteinmoneymod.ecoAddMoney",      true},
            {"/bubustein ecoSetMoney <amount> [currency]",      "message.bubusteinmoneymod.ecoSetMoney",      true},
            {"/bubustein help",                                 "message.bubusteinmoneymod.help",             false},
            {"/bubustein invalidate [iban]",                    "message.bubusteinmoneymod.invalidate",       false},
            {"/bubustein invalidateAll <UUID>",                 "message.bubusteinmoneymod.invalidateAll",    true},
            {"/bubustein link [iban]",                          "message.bubusteinmoneymod.link",             false},
            {"/bubustein rates [currency]",                     "message.bubusteinmoneymod.rates",            false},
            {"/bubustein resetMoney",                           "message.bubusteinmoneymod.resetMoney",       true},
            {"/bubustein setcurrency <currency>",               "message.bubusteinmoneymod.setcurrency",      false},
            {"/bubustein setdefaultcurrency <currency>",        "message.bubusteinmoneymod.setdefaultcurrency", true},
            {"/bubustein transfer <player> <iban> <amount> [currency]", "message.bubusteinmoneymod.transfer", false},
            {"/bubustein updateRates",                          "message.bubusteinmoneymod.updateRates",      true},
            {"/bubustein withdraw <amount>",                    "message.bubusteinmoneymod.withdraw",         false},
    };

    private static int showHelp(CommandSourceStack source) {
        return showHelpPage(source, 1);
    }

    private static int showHelpPage(CommandSourceStack source, int page) {
        Player player = source.getPlayer();
        if (player == null) return Command.SINGLE_SUCCESS;

        int totalEntries = HELP_ENTRIES.length;
        int totalPages = (int) Math.ceil((double) totalEntries / HELP_PAGE_SIZE);
        page = Math.max(1, Math.min(page, totalPages));

        int start = (page - 1) * HELP_PAGE_SIZE;
        int end = Math.min(start + HELP_PAGE_SIZE, totalEntries);

        // ── Header ──────────────────────────────────────────────
        player.sendSystemMessage(
                Component.translatable("message.bubusteinmoneymod.title")
                        .withStyle(s -> s.withColor(ChatFormatting.GOLD).withBold(true))
        );

        // ── Comenzile pentru pagina curentă ─────────────────────
        for (int i = start; i < end; i++) {
            String cmd        = (String)  HELP_ENTRIES[i][0];
            String descKey    = (String)  HELP_ENTRIES[i][1];
            boolean requiresOp = (boolean) HELP_ENTRIES[i][2];
            player.sendSystemMessage(createStyledHelpMessage(cmd, descKey, requiresOp));
        }

        // ── Linie separator ─────────────────────────────────────
        player.sendSystemMessage(
                Component.literal("======================================================================")
                        .withStyle(ChatFormatting.GOLD)
        );

        // ── Footer: pagina curentă + butoane ────────────────────
        MutableComponent footer = Component.literal("");

        // Buton "«" Prev (doar dacă nu suntem pe prima pagină)
        if (page > 1) {
            int finalPage = page;
            MutableComponent prev = Component.literal("« ")
                    .withStyle(s -> s
                            .withColor(ChatFormatting.YELLOW)
                            .withBold(true)
                            .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                    net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                    "/bubustein help " + (finalPage - 1)
                            ))
                            .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                    net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("Page " + (finalPage - 1)).withStyle(ChatFormatting.YELLOW)
                            ))
                    );
            footer.append(prev);
        }

        // Textul paginii curente
        footer.append(
                Component.literal("Page " + page + " / " + totalPages)
                        .withStyle(ChatFormatting.GRAY)
        );

        // Buton Next » (doar dacă nu suntem pe ultima pagină)
        if (page < totalPages) {
            int finalPage1 = page;
            MutableComponent next = Component.literal(" »")
                    .withStyle(s -> s
                            .withColor(ChatFormatting.YELLOW)
                            .withBold(true)
                            .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                    net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                    "/bubustein help " + (finalPage1 + 1)
                            ))
                            .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                    net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("Page " + (finalPage1 + 1)).withStyle(ChatFormatting.YELLOW)
                            ))
                    );
            footer.append(next);
        }

        player.sendSystemMessage(footer);

        return Command.SINGLE_SUCCESS;
    }

    private static MutableComponent createStyledHelpMessage(String command, String descriptionKey) {
        return createStyledHelpMessage(command, descriptionKey, false);
    }

    private static MutableComponent createStyledHelpMessage(String command, String descriptionKey, boolean requiresOp) {
        MutableComponent styledCommand = Component.literal(command).withStyle(ChatFormatting.AQUA);
        MutableComponent styledDescription = Component.literal(": ")
                .append(Component.translatable(descriptionKey))
                .withStyle(ChatFormatting.GRAY);
        MutableComponent fullMessage = styledCommand.append(styledDescription);
        if (requiresOp) {
            fullMessage.append(
                    Component.translatable("message.bubusteinmoneymod.requires_op")
                            .withStyle(s -> s.withColor(ChatFormatting.RED).withBold(true))
            );
        }
        return fullMessage;
    }
    public static ChatFormatting getTierColor(String tierStr, AccountKind kind) {
        if (kind == AccountKind.DEBIT) {
            try {
                CardTier tier = CardTier.valueOf(tierStr.toUpperCase());
                return tier.getColor();
            } catch (IllegalArgumentException e) {
                return ChatFormatting.GRAY;
            }
        } else if (kind == AccountKind.CREDIT) {
            try {
                CreditCardTier tier = CreditCardTier.valueOf(tierStr.toUpperCase());
                return tier.getColor();
            } catch (IllegalArgumentException e) {
                return ChatFormatting.GRAY;
            }
        }
        return ChatFormatting.GRAY;
    }
    public static int removeItemsFromInventory(Player player, Item item, int count) {
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
    public static double calculateWithdrawFee(ItemStack stack, double amount) {
        CardTier tier = CardUtils.getDebitTierFromItem(stack);
        return amount * tier.getWithdrawFee();
    }
    public static double withdrawCurrency(Player player, double amount, String currency) {
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
    public static double convertCurrency(double amount, String fromCurrency, String toCurrency) {
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
    public static Optional<BankAccount> getAccountFromCard(ItemStack stack, ServerPlayer player) {
        return getAccountFromCard(stack, player, false);
    }
    public static Optional<BankAccount> getAccountFromCard(ItemStack stack, ServerPlayer player, boolean allowAdminBypass) {
        if (!(stack.getItem() instanceof CardItem) && !(stack.getItem() instanceof CreditCardItem)) {
            player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.hold_card").withStyle(ChatFormatting.RED));
            return Optional.empty();
        }
        String iban = CardUtils.getIban(stack);
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
    public static ServerPlayer getCardOwner(MinecraftServer server, BankAccount account) {
        return server.getPlayerList().getPlayer(account.getOwnerUuid());
    }
    public static void syncCardWithAccount(ItemStack stack, BankAccount account) {
        if (stack.getItem() instanceof CardItem || stack.getItem() instanceof CreditCardItem) {
            stack.set(CardUtils.MONEY_COMPONENT.get(), account.getBalance());
            stack.set(CardUtils.CURRENCY_COMPONENT.get(), account.getCurrency());
        }
    }
    public static boolean hasMoreThanTwoDecimals(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        return bd.stripTrailingZeros().scale() > 2;
    }
}