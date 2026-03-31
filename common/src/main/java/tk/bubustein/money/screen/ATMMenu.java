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

package tk.bubustein.money.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tk.bubustein.money.bank.AccountKind;
import tk.bubustein.money.bank.BankAccount;
import tk.bubustein.money.bank.BankAccountManager;
import tk.bubustein.money.bank.BankAccountSavedData;
import tk.bubustein.money.block.ModBlocks;
import tk.bubustein.money.command.ModCommands;
import tk.bubustein.money.item.CardItem;
import tk.bubustein.money.item.CreditCardItem;
import tk.bubustein.money.item.ModItems;
import tk.bubustein.money.item.SavingsCardItem;
import tk.bubustein.money.util.CardUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;

public class ATMMenu extends AbstractContainerMenu {

    private final Container cardSlot = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            ATMMenu.this.slotsChanged(this);
        }
    };

    private final Player player;
    private final ContainerLevelAccess access;

    // ContainerData syncs: [0] = balance high bits, [1] = balance low bits, [2] = status
    // balance is stored as (long)(actualBalance * 100) split into two ints
    // status: 0 = no card, 1 = card inserted ok, 2 = error
    private long syncedBalance = 0L;
    private int syncedStatus = 0;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) ((syncedBalance >> 32) & 0xFFFFFFFFL);
                case 1 -> (int) (syncedBalance & 0xFFFFFFFFL);
                case 2 -> syncedStatus;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    syncedBalance = (((long) value & 0xFFFFFFFFL) << 32) | (syncedBalance & 0xFFFFFFFFL);
                    break;
                case 1:
                    syncedBalance = (syncedBalance & 0xFFFFFFFF00000000L) | ((long) value & 0xFFFFFFFFL);
                    break;
                case 2:
                    syncedStatus = value;
                    break;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    // Client-side constructor (called via MenuType from network)
    public ATMMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    // Client-side constructor for extended menus
    public ATMMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    // Server-side constructor
    public ATMMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuTypes.ATM_MENU.get(), containerId);
        this.player = playerInventory.player;
        this.access = access;

        // Card input slot (slot 0) — positioned at (80, 20) in the GUI
        this.addSlot(new Slot(cardSlot, 0, 80, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof CardItem || stack.getItem() instanceof CreditCardItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Player main inventory (3 rows × 9 = 27 slots), starting at (8, 132)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, 128 + row * 18));
            }
        }

        // Player hotbar (9 slots), starting at (8, 180)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col,
                    8 + col * 18, 186));
        }

        this.addDataSlots(containerData);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == cardSlot) {
            updateCardSync();
        }
    }

    private void updateCardSync() {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        MinecraftServer server = serverPlayer.getServer();
        if (server == null) return;

        ItemStack cardStack = cardSlot.getItem(0);
        if (cardStack.isEmpty()) {
            syncedBalance = 0L;
            syncedStatus = 0;
            return;
        }

        String iban = CardUtils.getIban(cardStack);
        if (iban == null || iban.isEmpty()) {
            syncedBalance = 0L;
            syncedStatus = 2; // error
            return;
        }

        BankAccountManager mgr = BankAccountManager.get();
        Optional<BankAccount> optAcc = mgr.getByIban(server, iban);
        if (optAcc.isEmpty()) {
            syncedBalance = 0L;
            syncedStatus = 2; // error
            return;
        }

        BankAccount acc = optAcc.get();
        syncedBalance = (long) (acc.getBalance() * 100.0);
        syncedStatus = acc.isActive() ? 1 : 2;
        broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            returnStack = slotStack.copy();

            if (index == 0) {
                // Move from card slot to player inventory
                if (!this.moveItemStackTo(slotStack, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player inventory to card slot
                if (slotStack.getItem() instanceof CardItem || slotStack.getItem() instanceof CreditCardItem) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return returnStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.ATM.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Return card to player when menu is closed
        this.clearContainer(player, cardSlot);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Deposit logic
    // ─────────────────────────────────────────────────────────────────────────

    public void executeDeposit(double amount) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        MinecraftServer server = serverPlayer.getServer();
        if (server == null) return;

        ItemStack cardStack = cardSlot.getItem(0);
        if (cardStack.isEmpty()) return;
        if (cardStack.getItem() instanceof SavingsCardItem) return;

        String iban = CardUtils.getIban(cardStack);
        if (iban == null || iban.isEmpty()) return;

        BankAccountManager mgr = BankAccountManager.get();
        Optional<BankAccount> optAcc = mgr.getByIban(server, iban);
        if (optAcc.isEmpty()) return;
        BankAccount acc = optAcc.get();

        if (!acc.isActive()) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.bubusteinmoneymod.card_not_active")
                            .withStyle(ChatFormatting.RED));
            return;
        }
        boolean isAdmin = serverPlayer.hasPermissions(2);
        if (!isAdmin && !acc.getOwnerUuid().equals(serverPlayer.getUUID())) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.bubusteinmoneymod.not_card_owner")
                            .withStyle(ChatFormatting.RED));
            return;
        }
        if (acc.getKind() != AccountKind.CREDIT) {
            BigDecimal newBalance = BigDecimal.valueOf(acc.getBalance())
                    .add(BigDecimal.valueOf(amount));
            if (newBalance.compareTo(ModCommands.MAX_AMOUNT) > 0) {
                serverPlayer.sendSystemMessage(
                        Component.translatable("message.bubusteinmoneymod.amount_too_large",
                                        CardUtils.formatMoney(ModCommands.MAX_AMOUNT.doubleValue()))
                                .withStyle(ChatFormatting.RED));
                return;
            }
        }

        String currency = acc.getCurrency();
        NavigableMap<Double, Item> items = ModItems.getCurrencyItems().get(currency);
        if (items == null) return;

        // Count what player has in inventory
        Map<Item, Integer> available = new HashMap<>();
        for (Item item : items.values()) {
            available.put(item, serverPlayer.getInventory().countItem(item));
        }

        double totalAvailable = 0;
        for (Map.Entry<Double, Item> e : items.entrySet()) {
            totalAvailable += e.getKey() * available.getOrDefault(e.getValue(), 0);
        }

        if (totalAvailable < amount) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.bubusteinmoneymod.not_enough_funds")
                            .withStyle(ChatFormatting.RED));
            return;
        }

        // Remove items from inventory, largest denomination first
        double remaining = amount;
        double totalDeposited = 0;
        for (Map.Entry<Double, Item> e : items.descendingMap().entrySet()) {
            double denom = e.getKey();
            Item item = e.getValue();
            int avail = available.getOrDefault(item, 0);
            int needed = (int) Math.min(remaining / denom, avail);
            if (needed > 0) {
                int removed = ModCommands.removeItemsFromInventory(serverPlayer, item, needed);
                double dep = denom * removed;
                totalDeposited += dep;
                remaining -= dep;
            }
            if (remaining < 0.005) break;
        }

        if (totalDeposited > 0) {
            double inAccCurrency = ModCommands.convertCurrency(totalDeposited, currency, acc.getCurrency());
            acc.setBalance(acc.getBalance() + inAccCurrency);

            // Sync data components on the card
            cardStack.set(CardUtils.MONEY_COMPONENT.get(), acc.getBalance());
            cardStack.set(CardUtils.CURRENCY_COMPONENT.get(), acc.getCurrency());

            BankAccountSavedData.get(server).setDirty();

            // Refresh synced balance
            syncedBalance = (long) (acc.getBalance() * 100.0);
            syncedStatus = 1;
            broadcastChanges();

            serverPlayer.sendSystemMessage(
                    Component.translatable("message.bubusteinmoneymod.deposit_success",
                                    CardUtils.formatMoney(totalDeposited), currency,
                                    CardUtils.formatMoney(acc.getBalance()), acc.getCurrency())
                            .withStyle(ChatFormatting.GREEN));
        }

        serverPlayer.inventoryMenu.broadcastChanges();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Withdraw logic
    // ─────────────────────────────────────────────────────────────────────────

    public void executeWithdraw(double amount) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        MinecraftServer server = serverPlayer.getServer();
        if (server == null) return;

        ItemStack cardStack = cardSlot.getItem(0);
        if (cardStack.isEmpty()) return;
        if (cardStack.getItem() instanceof SavingsCardItem) return;

        String iban = CardUtils.getIban(cardStack);
        if (iban == null || iban.isEmpty()) return;

        BankAccountManager mgr = BankAccountManager.get();
        Optional<BankAccount> optAcc = mgr.getByIban(server, iban);
        if (optAcc.isEmpty()) return;
        BankAccount acc = optAcc.get();

        if (!acc.isActive()) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.bubusteinmoneymod.card_not_active")
                            .withStyle(ChatFormatting.RED));
            return;
        }

        if (amount <= 0) return;
        boolean isAdmin = serverPlayer.hasPermissions(2);
        if (!isAdmin && !acc.getOwnerUuid().equals(serverPlayer.getUUID())) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.bubusteinmoneymod.not_card_owner")
                            .withStyle(ChatFormatting.RED));
            return;
        }
        String currency = acc.getCurrency();

        if (acc.getKind() == AccountKind.CREDIT) {
            if (!acc.canWithdraw(amount)) {
                serverPlayer.sendSystemMessage(
                        Component.translatable("message.bubusteinmoneymod.credit_limit_exceeded",
                                        CardUtils.formatMoney(amount), currency)
                                .withStyle(ChatFormatting.RED));
                return;
            }
            // Global debt cap check
            if (acc.getBalance() - amount < 0) {
                double debtIncrease = Math.min(amount, amount - Math.max(acc.getBalance(), 0));
                double debtIncreaseEur;
                try {
                    debtIncreaseEur = ModCommands.convertCurrency(debtIncrease, currency, "EUR");
                } catch (Exception e) {
                    debtIncreaseEur = debtIncrease;
                }

                if (!mgr.canAccumulateMoreDebt(server, serverPlayer.getUUID(), debtIncreaseEur)) {
                    double cap = mgr.getPersonalCreditCapEur(server, serverPlayer.getUUID());
                    double capInCurrency;
                    try {
                        capInCurrency = ModCommands.convertCurrency(cap, "EUR", currency);
                    } catch (Exception e) {
                        capInCurrency = cap;
                    }
                    serverPlayer.sendSystemMessage(
                            Component.translatable("message.bubusteinmoneymod.credit.global_debt_cap",
                                            CardUtils.formatMoney(capInCurrency), currency)
                                    .withStyle(ChatFormatting.RED));
                    return;
                }
            }

            boolean ok = acc.withdraw(amount);
            if (!ok) return;

        } else if (acc.getKind() == AccountKind.DEBIT) {
            boolean isAdminAction = serverPlayer.hasPermissions(2) && !acc.getOwnerUuid().equals(serverPlayer.getUUID());
            double fee = isAdminAction ? 0.0 : ModCommands.calculateWithdrawFee(cardStack, amount);
            double total = amount + fee;
            if (acc.getBalance() < total) {
                serverPlayer.sendSystemMessage(
                        Component.translatable("message.bubusteinmoneymod.not_enough_funds")
                                .withStyle(ChatFormatting.RED));
                return;
            }
            acc.setBalance(acc.getBalance() - total);
        } else {
            // Savings — not supported at ATM
            return;
        }

        // Give physical currency items to player
        if (ModItems.getCurrencyItems().containsKey(currency)) {
            ModCommands.withdrawCurrency(serverPlayer, amount, currency);
        }

        // Sync data components on the card
        cardStack.set(CardUtils.MONEY_COMPONENT.get(), acc.getBalance());
        cardStack.set(CardUtils.CURRENCY_COMPONENT.get(), acc.getCurrency());

        BankAccountSavedData.get(server).setDirty();

        // Refresh synced balance
        syncedBalance = (long) (acc.getBalance() * 100.0);
        syncedStatus = 1;
        broadcastChanges();

        if (acc.getKind() == AccountKind.DEBIT) {
            boolean isAdminAction = serverPlayer.hasPermissions(2) && !acc.getOwnerUuid().equals(serverPlayer.getUUID());
            double fee = isAdminAction ? 0.0 : ModCommands.calculateWithdrawFee(cardStack, amount);
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.bubusteinmoneymod.withdraw_success",
                                    CardUtils.formatMoney(amount), currency,
                                    CardUtils.formatMoney(fee), currency,
                                    CardUtils.formatMoney(acc.getBalance()), currency)
                            .withStyle(ChatFormatting.GREEN));
        } else {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.bubusteinmoneymod.withdraw_success",
                                    CardUtils.formatMoney(amount), currency,
                                    CardUtils.formatMoney(0), currency,
                                    CardUtils.formatMoney(acc.getBalance()), currency)
                            .withStyle(ChatFormatting.GREEN));
        }

        serverPlayer.inventoryMenu.broadcastChanges();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers for the client screen
    // ─────────────────────────────────────────────────────────────────────────

    /** 0 = no card, 1 = ok, 2 = error */
    public int getStatus() {
        return containerData.get(2);
    }
}
