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

package tk.bubustein.money.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.bank.*;
import tk.bubustein.money.util.CardUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CardItem extends Item {
    private static final ConcurrentHashMap<CardCacheKey, CachedCardData> cardCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_NANOS = 1_000_000_000L; // 1 second (~20 ticks at 20 TPS)
    private static final int CACHE_CLEANUP_INTERVAL_SECONDS = 60;
    private static ScheduledExecutorService cacheCleanupExecutor;
    private static volatile boolean cleanupScheduled = false;
    private static final double GLOW_THRESHOLD_EUR = 20000.0;

    public CardItem(Properties properties) {
        super(properties);
        initializeCacheCleanup();
    }
    private static void initializeCacheCleanup() {
        if (!cleanupScheduled) {
            synchronized (CardItem.class) {
                if (!cleanupScheduled) {
                    cacheCleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                        Thread t = new Thread(r, "CardCacheCleanup");
                        t.setDaemon(true);
                        return t;
                    });
                    cacheCleanupExecutor.scheduleAtFixedRate(
                            CardItem::cleanupOldCacheEntries,
                            CACHE_CLEANUP_INTERVAL_SECONDS,
                            CACHE_CLEANUP_INTERVAL_SECONDS,
                            TimeUnit.SECONDS
                    );

                    cleanupScheduled = true;
                    MoneyMod.LOGGER.info("[{}] Card cache cleanup scheduled every {}s",
                            MoneyMod.MOD_ID, CACHE_CLEANUP_INTERVAL_SECONDS);
                }
            }
        }
    }
    private static void cleanupOldCacheEntries() {
        try {
            int sizeBefore = cardCache.size();
            long now = System.nanoTime();

            cardCache.entrySet().removeIf(entry ->
                    !entry.getValue().isValid(now)
            );

            int sizeAfter = cardCache.size();
            int removed = sizeBefore - sizeAfter;

            if (removed > 0) {
                MoneyMod.LOGGER.debug("[{}] Cleaned {} old entries from card cache ({} remaining)",
                        MoneyMod.MOD_ID, removed, sizeAfter);
            }
        } catch (Exception e) {
            MoneyMod.LOGGER.error("[{}] Error during card cache cleanup", MoneyMod.MOD_ID, e);
        }
    }
    public static void clearCache() {
        int size = cardCache.size();
        cardCache.clear();
        MoneyMod.LOGGER.info("[{}] Cleared entire card cache ({} entries removed)",
                MoneyMod.MOD_ID, size);
    }

    // --- Shared cache API for CreditCardItem ---

    /**
     * Checks if cache entry is still valid for the given card slot.
     * Used by both CardItem and CreditCardItem to avoid per-tick SavedData lookups.
     */
    public static boolean isCacheValid(String iban, UUID playerUuid, int slot, long currentNanos) {
        CardCacheKey key = new CardCacheKey(iban, playerUuid, slot);
        CachedCardData cached = cardCache.get(key);
        return cached != null && cached.isValid(currentNanos);
    }

    /**
     * Stores a cache entry after a successful account sync.
     */
    public static void putInCache(String iban, UUID playerUuid, int slot, double balance, String currency) {
        CardCacheKey key = new CardCacheKey(iban, playerUuid, slot);
        cardCache.put(key, new CachedCardData(System.nanoTime(), balance, currency));
    }

    /**
     * Removes a cache entry (e.g. when a card is invalidated).
     */
    public static void removeFromCache(String iban, UUID playerUuid, int slot) {
        CardCacheKey key = new CardCacheKey(iban, playerUuid, slot);
        cardCache.remove(key);
    }
    public static void shutdown() {
        if (cacheCleanupExecutor != null) {
            cacheCleanupExecutor.shutdown();
            try {
                if (!cacheCleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    cacheCleanupExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cacheCleanupExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            MoneyMod.LOGGER.info("[{}] Card cache cleanup executor shut down", MoneyMod.MOD_ID);
        }
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide()) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (stack.isEmpty() || !stack.has(CardUtils.IBAN_COMPONENT.get())) return;

        ServerLevel serverLevel = (ServerLevel) level;
        MinecraftServer server = serverLevel.getServer();

        String iban = CardUtils.getIban(stack);
        if (iban == null || iban.isEmpty()) return;

        long now = System.nanoTime();
        if (isCacheValid(iban, player.getUUID(), slot, now)) {
            return;
        }

        BankAccountManager mgr = BankAccountManager.get();
        Optional<BankAccount> optAcc = mgr.getByIban(server, iban);

        if (optAcc.isEmpty()) {
            MoneyMod.LOGGER.warn("[{}] Card with IBAN {} has no associated account (player: {})",
                    MoneyMod.MOD_ID, iban, player.getGameProfile().getName());
            invalidateCard(stack);
            removeFromCache(iban, player.getUUID(), slot);
            return;
        }

        BankAccount acc = optAcc.get();

        if (acc.getKind() != AccountKind.DEBIT) {
            MoneyMod.LOGGER.error("[{}] CRITICAL: CardItem {} linked to non-DEBIT account {} (type: {}). Invalidating card!",
                    MoneyMod.MOD_ID, iban, acc.getIban(), acc.getKind().name());
            invalidateCard(stack);
            removeFromCache(iban, player.getUUID(), slot);
            return;
        }
        if (!acc.isActive()) {
            MoneyMod.LOGGER.info("[{}] Card with IBAN {} is inactive, clearing data (player: {})",
                    MoneyMod.MOD_ID, iban, player.getGameProfile().getName());
            invalidateCard(stack);
            removeFromCache(iban, player.getUUID(), slot);
            return;
        }

        UUID cardOwner = CardUtils.getOwner(stack);
        if (cardOwner != null && !cardOwner.equals(player.getUUID())) {
            MoneyMod.LOGGER.warn("[{}] Card owner mismatch: card owner={}, holder={}",
                    MoneyMod.MOD_ID, cardOwner, player.getUUID());
            return;
        }
        CardTier currentTier = CardUtils.getDebitTierFromItem(stack);
        String newTier = currentTier.name();
        if (!newTier.equals(acc.getCardTier())) {
            acc.setCardTier(newTier);
        }

        double newBalance = acc.getBalance();
        String newCurrency = acc.getCurrency();

        if (Double.isFinite(newBalance) && newBalance >= 0) {
            stack.set(CardUtils.MONEY_COMPONENT.get(), newBalance);
        } else {
            MoneyMod.LOGGER.error("[{}] Invalid balance detected for IBAN {}: {}",
                    MoneyMod.MOD_ID, iban, newBalance);
            stack.set(CardUtils.MONEY_COMPONENT.get(), 0.0);
        }

        if (newCurrency != null && !newCurrency.isEmpty() && ModItems.EXCHANGE_RATES.containsKey(newCurrency)) {
            stack.set(CardUtils.CURRENCY_COMPONENT.get(), newCurrency);
        } else {
            MoneyMod.LOGGER.warn("[{}] Invalid currency for IBAN {}: {}, using EUR",
                    MoneyMod.MOD_ID, iban, newCurrency);
            stack.set(CardUtils.CURRENCY_COMPONENT.get(), "EUR");
        }

        putInCache(iban, player.getUUID(), slot, newBalance, newCurrency);
    }
    private void invalidateCard(ItemStack stack) {
        stack.remove(CardUtils.IBAN_COMPONENT.get());
        stack.remove(CardUtils.OWNER_COMPONENT.get());
        stack.remove(CardUtils.OWNER_NAME_COMPONENT.get());
        stack.remove(CardUtils.ACCOUNT_KIND_COMPONENT.get());
        stack.set(CardUtils.MONEY_COMPONENT.get(), 0.0);
        stack.set(CardUtils.CURRENCY_COMPONENT.get(), "EUR");
        stack.remove(DataComponents.CUSTOM_NAME);
    }
    @Override
    public boolean isFoil(ItemStack stack) {
        double money = stack.getOrDefault(CardUtils.MONEY_COMPONENT.get(), 0.0);
        if (!Double.isFinite(money) || money < 0) {
            return false;
        }
        String currency = stack.getOrDefault(CardUtils.CURRENCY_COMPONENT.get(), "EUR");
        if (!"EUR".equals(currency)) {
            Double rate = ModItems.EXCHANGE_RATES.get(currency);
            if (rate == null || rate <= 0) {
                return false;
            }
            money = money / rate;
        }
        return money >= GLOW_THRESHOLD_EUR;
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        Component custom = stack.get(DataComponents.CUSTOM_NAME);
        if (custom != null) {
            return custom;
        }
        String ownerName = CardUtils.getOwnerName(stack);
        if (ownerName == null || ownerName.trim().isEmpty()) {
            return super.getName(stack);
        }
        Item item = stack.getItem();
        String key = getTranslationKey(item);

        if (key == null) {
            MoneyMod.LOGGER.warn("[{}] Unknown card type: {}", MoneyMod.MOD_ID, item);
            return super.getName(stack);
        }
        return Component.translatable(key, ownerName);
    }
    private String getTranslationKey(Item item) {
        if (item == ModItems.RustyCard.get()) {
            return "item.bubusteinmoneymod.rusty_card.named";
        } else if (item == ModItems.Card.get()) {
            return "item.bubusteinmoneymod.classic_card.named";
        } else if (item == ModItems.GoldCard.get()) {
            return "item.bubusteinmoneymod.gold_card.named";
        } else if (item == ModItems.SteelCard.get()) {
            return "item.bubusteinmoneymod.steel_card.named";
        } else if (item == ModItems.SupremeCard.get()) {
            return "item.bubusteinmoneymod.supreme_card.named";
        }
        return null;
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        String iban = CardUtils.getIban(stack);
        if (iban == null || iban.isEmpty()) {
            tooltip.add(Component.literal("Empty Card")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }
        tooltip.add(Component.literal("IBAN: " + iban)
                .withStyle(style -> style.withColor(ChatFormatting.GRAY)));
        boolean shiftDown = Screen.hasShiftDown();
        if (!shiftDown) {
            tooltip.add(Component.literal("Press ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("L-SHIFT")
                            .withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" for more details")
                            .withStyle(ChatFormatting.GRAY)));
        } else {
            double balance = stack.getOrDefault(CardUtils.MONEY_COMPONENT.get(), 0.0);
            String currency = stack.getOrDefault(CardUtils.CURRENCY_COMPONENT.get(), "EUR");
            String formattedMoney = Double.isFinite(balance) && balance >= 0
                    ? CardUtils.formatMoney(balance)
                    : "ERROR";
            tooltip.add(Component.translatable(
                            "cardItem.bubusteinmoneymod.balance",
                            formattedMoney,
                            currency)
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFD700))));
            addWithdrawFeeTooltip(stack, tooltip);
        }
    }
    private void addWithdrawFeeTooltip(ItemStack stack, List<Component> tooltip) {
        Item item = stack.getItem();
        String feePercentage;

        if (item == ModItems.RustyCard.get()) {
            feePercentage = "10%";
        } else if (item == ModItems.Card.get()) {
            feePercentage = "3%";
        } else if (item == ModItems.GoldCard.get()) {
            feePercentage = "2%";
        } else if (item == ModItems.SteelCard.get()) {
            feePercentage = "1%";
        } else if (item == ModItems.SupremeCard.get()) {
            feePercentage = "0%";
        } else {
            return;
        }
        tooltip.add(Component.translatable("cardItem.bubusteinmoneymod.withdraw_fee", feePercentage)
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
    }

    private record CardCacheKey(String iban, UUID playerUuid, int slot) {
    }
    private record CachedCardData(long timestampNanos, double balance, String currency) {
        public boolean isValid(long currentNanos) {
            return (currentNanos - timestampNanos) < CACHE_DURATION_NANOS;
        }
    }
}