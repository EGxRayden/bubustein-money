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
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class CreditCardItem extends Item {
    public static final long INTEREST_PERIOD_TICKS = 576_000L;

    public CreditCardItem(Properties properties) {
        super(properties);
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

        // Use shared cache from CardItem to avoid per-tick SavedData lookups
        long now = System.nanoTime();
        if (CardItem.isCacheValid(iban, player.getUUID(), slot, now)) {
            return;
        }

        BankAccountManager mgr = BankAccountManager.get();
        Optional<BankAccount> optAcc = mgr.getByIban(server, iban);

        if (optAcc.isEmpty()) {
            MoneyMod.LOGGER.warn("[{}] Credit card with IBAN {} has no associated account (player: {})",
                    MoneyMod.MOD_ID, iban, player.getGameProfile().getName());
            invalidateCard(stack);
            CardItem.removeFromCache(iban, player.getUUID(), slot);
            return;
        }

        BankAccount acc = optAcc.get();
        if (acc.getKind() != AccountKind.CREDIT) {
            MoneyMod.LOGGER.error("[{}] CRITICAL: CreditCardItem {} linked to non-CREDIT account {} (type: {}). Invalidating card!",
                    MoneyMod.MOD_ID, iban, acc.getIban(), acc.getKind().name());
            invalidateCard(stack);
            CardItem.removeFromCache(iban, player.getUUID(), slot);
            return;
        }

        if (!acc.isActive()) {
            MoneyMod.LOGGER.info("[{}] Credit card with IBAN {} is inactive, clearing data (player: {})",
                    MoneyMod.MOD_ID, iban, player.getGameProfile().getName());
            invalidateCard(stack);
            CardItem.removeFromCache(iban, player.getUUID(), slot);
            return;
        }
        boolean interestApplied = false;
        long currentGameTick = serverLevel.getGameTime();
        long lastTick = acc.getLastInterestGameTick();

        if (lastTick == 0L) {
            acc.setLastInterestGameTick(currentGameTick);
            BankAccountSavedData.get(server).setDirty();
        } else {
            long tickDiff = currentGameTick - lastTick;
            if (tickDiff >= INTEREST_PERIOD_TICKS && acc.hasDebt()) {
                int periods = (int) (tickDiff / INTEREST_PERIOD_TICKS);
                if (periods > 0) {
                    double added = acc.applyInterestForPeriods(periods);
                    acc.setLastInterestGameTick(lastTick + (long) periods * INTEREST_PERIOD_TICKS);
                    interestApplied = true;

                    BankAccountSavedData.get(server).setDirty();

                    MoneyMod.LOGGER.info(
                            "[{}] Applied {} interest periods (added {} {}) for IBAN {} at game tick {}",
                            MoneyMod.MOD_ID, periods, added, acc.getCurrency(), acc.getIban(), currentGameTick
                    );

                    player.sendSystemMessage(Component.translatable(
                            "message.bubusteinmoneymod.credit.interest_applied",
                            CardUtils.formatMoney(added),
                            acc.getCurrency(),
                            acc.getIban()
                    ).withStyle(ChatFormatting.RED));
                }
            }
        }

        // After interest: check if debt exceeds card limit and force recovery
        if (interestApplied && acc.hasDebt()) {
            double recovered = mgr.forceDebtRecovery(server, player.getUUID(), acc);
            if (recovered > 0) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                        "message.bubusteinmoneymod.credit.forced_recovery",
                        tk.bubustein.money.util.CardUtils.formatMoney(recovered),
                        acc.getCurrency(),
                        acc.getIban()
                ).withStyle(net.minecraft.ChatFormatting.YELLOW));
            }
        }
        /*
        UUID cardOwner = CardUtils.getOwner(stack);
        if (cardOwner != null && !cardOwner.equals(player.getUUID())) {
            MoneyMod.LOGGER.warn("[{}] Credit card owner mismatch: card owner={}, holder={}",
                    MoneyMod.MOD_ID, cardOwner, player.getUUID());
            return;
        }*/
        CreditCardTier currentTier = CardUtils.getCreditTierFromItem(stack);
        String newTier = currentTier.name();
        if (!newTier.equals(acc.getCardTier())) {
            acc.setCardTier(newTier);
        }
        double newBalance = acc.getBalance();
        String newCurrency = acc.getCurrency();

        if (Double.isFinite(newBalance)) {
            stack.set(CardUtils.MONEY_COMPONENT.get(), newBalance);
        } else {
            MoneyMod.LOGGER.error("[{}] Invalid balance detected for credit card IBAN {}: {}",
                    MoneyMod.MOD_ID, iban, newBalance);
            stack.set(CardUtils.MONEY_COMPONENT.get(), 0.0);
        }

        if (newCurrency != null && !newCurrency.isEmpty() && ModItems.EXCHANGE_RATES.containsKey(newCurrency)) {
            stack.set(CardUtils.CURRENCY_COMPONENT.get(), newCurrency);
        } else {
            MoneyMod.LOGGER.warn("[{}] Invalid currency for credit card IBAN {}: {}, using EUR",
                    MoneyMod.MOD_ID, iban, newCurrency);
            stack.set(CardUtils.CURRENCY_COMPONENT.get(), "EUR");
        }

        // Update shared cache after successful sync
        CardItem.putInCache(iban, player.getUUID(), slot, newBalance, newCurrency);
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
        double balance = stack.getOrDefault(CardUtils.MONEY_COMPONENT.get(), 0.0);
        if (!Double.isFinite(balance)) return false;
        if (balance < 0) {
            String currency = stack.getOrDefault(CardUtils.CURRENCY_COMPONENT.get(), "EUR");
            double debt = Math.abs(balance);

            if (!"EUR".equals(currency)) {
                Double rate = ModItems.EXCHANGE_RATES.get(currency);
                if (rate != null && rate > 0) {
                    debt = debt / rate;
                }
            }
            return debt >= 10000.0;
        }
        return false;
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
            MoneyMod.LOGGER.warn("[{}] Unknown credit card type: {}", MoneyMod.MOD_ID, item);
            return super.getName(stack);
        }
        return Component.translatable(key, ownerName);
    }
    private String getTranslationKey(Item item) {
        if (item == ModItems.ClassicCreditCard.get()) {
            return "item.bubusteinmoneymod.card_classic_credit.named";
        } else if (item == ModItems.GoldCreditCard.get()) {
            return "item.bubusteinmoneymod.card_gold_credit.named";
        } else if (item == ModItems.PlatinumCreditCard.get()) {
            return "item.bubusteinmoneymod.card_platinum_credit.named";
        }
        return null;
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        String iban = CardUtils.getIban(stack);
        if (iban == null || iban.isEmpty()) {
            tooltip.add(Component.literal("Empty Credit Card")
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

            if (!Double.isFinite(balance)) {
                tooltip.add(Component.literal("ERROR: Invalid balance")
                        .withStyle(ChatFormatting.RED));
                return;
            }
            String formattedMoney = CardUtils.formatMoney(Math.abs(balance));

            if (balance < 0) {
                tooltip.add(Component.translatable(
                                "cardItem.bubusteinmoneymod.credit_debt",
                                formattedMoney,
                                currency)
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF4444))));
            } else if (balance > 0) {
                tooltip.add(Component.translatable(
                                "cardItem.bubusteinmoneymod.credit_available",
                                formattedMoney,
                                currency)
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0x44FF44))));
            } else {
                tooltip.add(Component.translatable(
                                "cardItem.bubusteinmoneymod.credit_zero")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF))));
            }
            addCreditLimitTooltip(stack, tooltip);
        }
    }
    private void addCreditLimitTooltip(ItemStack stack, List<Component> tooltip) {
        Item item = stack.getItem();
        double creditLimit;
        String currency = stack.getOrDefault(CardUtils.CURRENCY_COMPONENT.get(), "EUR").toUpperCase(Locale.ROOT);

        if (item == ModItems.ClassicCreditCard.get()) {
            if (currency.equals("EUR")) {
                creditLimit = CreditCardTier.CLASSIC.getCreditLimit();
            } else {
                Double rate = ModItems.EXCHANGE_RATES.get(currency);
                if (rate == null || rate <= 0) {
                    return;
                }
                creditLimit = CreditCardTier.CLASSIC.getCreditLimit() * rate;
            }
        } else if (item == ModItems.GoldCreditCard.get()) {
            if (currency.equals("EUR")) {
                creditLimit = CreditCardTier.GOLD.getCreditLimit();
            } else {
                Double rate = ModItems.EXCHANGE_RATES.get(currency);
                if (rate == null || rate <= 0) {
                    return;
                }
                creditLimit = CreditCardTier.GOLD.getCreditLimit() * rate;
            }
        } else if (item == ModItems.PlatinumCreditCard.get()) {
            if (currency.equals("EUR")) {
                creditLimit = CreditCardTier.PLATINUM.getCreditLimit();
            } else {
                Double rate = ModItems.EXCHANGE_RATES.get(currency);
                if (rate == null || rate <= 0) {
                    return;
                }
                creditLimit = CreditCardTier.PLATINUM.getCreditLimit() * rate;
            }
        } else {
            return;
        }
        tooltip.add(Component.translatable(
                        "cardItem.bubusteinmoneymod.credit_limit",
                        CardUtils.formatMoney(creditLimit),
                        currency)
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA))));
    }
}