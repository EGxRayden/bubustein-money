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

package tk.bubustein.money.util;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.bank.AccountKind;
import tk.bubustein.money.bank.CardTier;
import tk.bubustein.money.bank.CreditCardTier;
import tk.bubustein.money.item.ModItems;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.Supplier;

public class CardUtils {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(MoneyMod.MOD_ID, Registries.DATA_COMPONENT_TYPE);

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
    public static CardTier getDebitTierFromItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item == ModItems.RustyCard.get()) return CardTier.RUSTY;
        if (item == ModItems.Card.get()) return CardTier.CLASSIC;
        if (item == ModItems.GoldCard.get()) return CardTier.GOLD;
        if (item == ModItems.SteelCard.get()) return CardTier.STEEL;
        if (item == ModItems.SupremeCard.get()) return CardTier.SUPREME;
        return CardTier.CLASSIC;
    }

    public static CreditCardTier getCreditTierFromItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item == ModItems.ClassicCreditCard.get()) return CreditCardTier.CLASSIC;
        if (item == ModItems.GoldCreditCard.get()) return CreditCardTier.GOLD;
        if (item == ModItems.PlatinumCreditCard.get()) return CreditCardTier.PLATINUM;
        return CreditCardTier.CLASSIC;
    }
    public static void setOwnerName(ItemStack stack, String name) {
        if (name != null && !name.trim().isEmpty()) {
            stack.set(OWNER_NAME_COMPONENT.get(), name.trim());
        }
    }

    public static String getOwnerName(ItemStack stack) {
        return stack.getOrDefault(OWNER_NAME_COMPONENT.get(), null);
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
}