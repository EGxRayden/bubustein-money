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

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
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

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class CardItem extends Item {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(MoneyMod.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    public static final Supplier<DataComponentType<String>> IBAN_COMPONENT =
            COMPONENTS.register("iban", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    public static final Supplier<DataComponentType<String>> OWNER_COMPONENT =
            COMPONENTS.register("owner", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    public static final Supplier<DataComponentType<String>> ACCOUNT_KIND_COMPONENT =
            COMPONENTS.register("account_kind", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    public static final Supplier<DataComponentType<Double>> MONEY_COMPONENT =
            COMPONENTS.register("money", () -> DataComponentType.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    .networkSynchronized(ByteBufCodecs.DOUBLE)
                    .build());

    public static final Supplier<DataComponentType<String>> CURRENCY_COMPONENT =
            COMPONENTS.register("currency", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    public static final Supplier<DataComponentType<String>> OWNER_NAME_COMPONENT =
            COMPONENTS.register("owner_name", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    private static final ConcurrentHashMap<CardCacheKey, CachedCardData> cardCache = new ConcurrentHashMap<>();
    private static final int CACHE_DURATION_TICKS = 20;
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
            long currentTimeApprox = System.currentTimeMillis() / 50; // Aproximare pentru game ticks

            cardCache.entrySet().removeIf(entry ->
                    !entry.getValue().isValid(currentTimeApprox)
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
        if (stack.isEmpty() || !stack.has(IBAN_COMPONENT.get())) return;

        ServerLevel serverLevel = (ServerLevel) level;
        MinecraftServer server = serverLevel.getServer();

        String iban = getIban(stack);
        if (iban == null || iban.isEmpty()) return;

        CardCacheKey cacheKey = new CardCacheKey(iban, player.getUUID(), slot);
        CachedCardData cached = cardCache.get(cacheKey);

        if (cached != null && cached.isValid(level.getGameTime())) {
            return;
        }

        BankAccountManager mgr = BankAccountManager.get();
        Optional<BankAccount> optAcc = mgr.getByIban(server, iban);

        if (optAcc.isEmpty()) {
            MoneyMod.LOGGER.warn("[{}] Card with IBAN {} has no associated account (player: {})",
                    MoneyMod.MOD_ID, iban, player.getGameProfile().getName());
            invalidateCard(stack);
            cardCache.remove(cacheKey);
            return;
        }

        BankAccount acc = optAcc.get();

        if (!acc.isActive()) {
            MoneyMod.LOGGER.info("[{}] Card with IBAN {} is inactive, clearing data (player: {})",
                    MoneyMod.MOD_ID, iban, player.getGameProfile().getName());
            invalidateCard(stack);
            cardCache.remove(cacheKey);
            return;
        }

        UUID cardOwner = getOwner(stack);
        if (cardOwner != null && !cardOwner.equals(player.getUUID())) {
            MoneyMod.LOGGER.warn("[{}] Card owner mismatch: card owner={}, holder={}",
                    MoneyMod.MOD_ID, cardOwner, player.getUUID());
            return;
        }

        CardTier currentTier = getTierFromItem(stack);
        String newTier = currentTier.name();
        if (!newTier.equals(acc.getCardTier())) {
            acc.setCardTier(newTier);
        }

        double newBalance = acc.getBalance();
        String newCurrency = acc.getCurrency();

        if (Double.isFinite(newBalance) && newBalance >= 0) {
            stack.set(MONEY_COMPONENT.get(), newBalance);
        } else {
            MoneyMod.LOGGER.error("[{}] Invalid balance detected for IBAN {}: {}",
                    MoneyMod.MOD_ID, iban, newBalance);
            stack.set(MONEY_COMPONENT.get(), 0.0);
        }

        if (newCurrency != null && !newCurrency.isEmpty() && ModItems.EXCHANGE_RATES.containsKey(newCurrency)) {
            stack.set(CURRENCY_COMPONENT.get(), newCurrency);
        } else {
            MoneyMod.LOGGER.warn("[{}] Invalid currency for IBAN {}: {}, using EUR",
                    MoneyMod.MOD_ID, iban, newCurrency);
            stack.set(CURRENCY_COMPONENT.get(), "EUR");
        }

        cardCache.put(cacheKey, new CachedCardData(level.getGameTime(), newBalance, newCurrency));
    }
    private void invalidateCard(ItemStack stack) {
        stack.remove(IBAN_COMPONENT.get());
        stack.remove(OWNER_COMPONENT.get());
        stack.remove(OWNER_NAME_COMPONENT.get());
        stack.remove(ACCOUNT_KIND_COMPONENT.get());
        stack.set(MONEY_COMPONENT.get(), 0.0);
        stack.set(CURRENCY_COMPONENT.get(), "EUR");
        stack.remove(DataComponents.CUSTOM_NAME);
    }
    public static void setOwnerName(ItemStack stack, String name) {
        if (name != null && !name.trim().isEmpty()) {
            stack.set(OWNER_NAME_COMPONENT.get(), name.trim());
        }
    }
    public static String getOwnerName(ItemStack stack) {
        return stack.getOrDefault(OWNER_NAME_COMPONENT.get(), null);
    }
    @Override
    public boolean isFoil(ItemStack stack) {
        double money = stack.getOrDefault(MONEY_COMPONENT.get(), 0.0);
        if (!Double.isFinite(money) || money < 0) {
            return false;
        }
        String currency = stack.getOrDefault(CURRENCY_COMPONENT.get(), "EUR");
        if (!"EUR".equals(currency)) {
            Double rate = ModItems.EXCHANGE_RATES.get(currency);
            if (rate == null || rate <= 0) {
                return false;
            }
            money = money / rate;
        }
        return money >= GLOW_THRESHOLD_EUR;
    }
    public static String formatMoney(double amount) {
        if (!Double.isFinite(amount)) {
            MoneyMod.LOGGER.error("[{}] Invalid money amount: {}", MoneyMod.MOD_ID, amount);
            return "ERROR";
        }
        if (amount < 0) {
            return "-" + formatMoney(Math.abs(amount));
        }
        amount = Math.round(amount * 100.0) / 100.0;

        if (amount >= 1000000000) {
            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(amount / 1000000000.0) + "B";
        } else if (amount >= 1000000) {
            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(amount / 1000000.0) + "M";
        } else if (amount >= 100000) {
            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(amount / 1000.0) + "K";
        } else if (amount >= 10000) {
            // Formatare specială pentru valori între 10,000 și 99,999
            DecimalFormat df = new DecimalFormat("#.##");
            String formatted = df.format(amount);

            if (formatted.contains(".")) {
                String[] parts = formatted.split("\\.");
                return formatThousandsSeparator(parts[0]) + "." + parts[1];
            } else {
                return formatThousandsSeparator(formatted);
            }
        } else {
            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(amount);
        }
    }
    private static String formatThousandsSeparator(String number) {
        if (number.length() >= 4) {
            String thousands = number.substring(0, number.length() - 3);
            String hundreds = number.substring(number.length() - 3);
            return thousands + " " + hundreds;
        }
        return number;
    }
    @Override
    public @NotNull Component getName(ItemStack stack) {
        Component custom = stack.get(DataComponents.CUSTOM_NAME);
        if (custom != null) {
            return custom;
        }
        String ownerName = getOwnerName(stack);
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
    public static void setIban(ItemStack stack, String iban) {
        if (iban != null && !iban.trim().isEmpty()) {
            stack.set(IBAN_COMPONENT.get(), iban.trim());
        }
    }
    public static String getIban(ItemStack stack) {
        return stack.getOrDefault(IBAN_COMPONENT.get(), null);
    }
    public static void setOwner(ItemStack stack, UUID owner) {
        if (owner != null) {
            stack.set(OWNER_COMPONENT.get(), owner.toString());
        }
    }
    public static UUID getOwner(ItemStack stack) {
        String s = stack.getOrDefault(OWNER_COMPONENT.get(), null);
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            MoneyMod.LOGGER.error("[{}] Invalid UUID format for card owner: {}", MoneyMod.MOD_ID, s);
            return null;
        }
    }
    public static void setAccountKind(ItemStack stack, AccountKind kind) {
        if (kind != null) {
            stack.set(ACCOUNT_KIND_COMPONENT.get(), kind.name());
        }
    }
    public static AccountKind getAccountKind(ItemStack stack) {
        String s = stack.getOrDefault(ACCOUNT_KIND_COMPONENT.get(), null);
        if (s == null) {
            return AccountKind.DEBIT;
        }
        try {
            return AccountKind.valueOf(s);
        } catch (IllegalArgumentException e) {
            MoneyMod.LOGGER.error("[{}] Invalid AccountKind: {}", MoneyMod.MOD_ID, s);
            return AccountKind.DEBIT;
        }
    }
    public static CardTier getTierFromItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item == ModItems.RustyCard.get()) return CardTier.RUSTY;
        if (item == ModItems.Card.get()) return CardTier.CLASSIC;
        if (item == ModItems.GoldCard.get()) return CardTier.GOLD;
        if (item == ModItems.SteelCard.get()) return CardTier.STEEL;
        if (item == ModItems.SupremeCard.get()) return CardTier.SUPREME;
        return CardTier.CLASSIC;
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        String iban = getIban(stack);
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
            double balance = stack.getOrDefault(MONEY_COMPONENT.get(), 0.0);
            String currency = stack.getOrDefault(CURRENCY_COMPONENT.get(), "EUR");
            String formattedMoney = Double.isFinite(balance) && balance >= 0
                    ? formatMoney(balance)
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

    private static class CardCacheKey {
        private final String iban;
        private final UUID playerUuid;
        private final int slot;

        public CardCacheKey(String iban, UUID playerUuid, int slot) {
            this.iban = iban;
            this.playerUuid = playerUuid;
            this.slot = slot;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CardCacheKey that = (CardCacheKey) o;
            return slot == that.slot &&
                    Objects.equals(iban, that.iban) &&
                    Objects.equals(playerUuid, that.playerUuid);
        }
        @Override
        public int hashCode() {
            return Objects.hash(iban, playerUuid, slot);
        }
    }
    private record CachedCardData(long timestamp, double balance, String currency) {
        public boolean isValid(long currentTime) {
            return (currentTime - timestamp) < CACHE_DURATION_TICKS;
        }
    }
}