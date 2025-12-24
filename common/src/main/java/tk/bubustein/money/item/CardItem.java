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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.bank.*;
import tk.bubustein.money.config.ModConfig;
import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;
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
    private static final double GLOW_THRESHOLD_EUR = 20000.0;
    public CardItem(Properties properties) {
        super(properties);
    }
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        if (level.isClientSide) return;
        if (stack.has(IBAN_COMPONENT.get())) return;
        UUID owner = player.getUUID();
        String defaultCurrency = MoneyMod.getDefaultCurrency();
        if (!ModItems.EXCHANGE_RATES.containsKey(defaultCurrency)) {
            defaultCurrency = "EUR";
        }
        String bankPrefix = "BSTN";
        AccountKind kind = AccountKind.DEBIT;

        MinecraftServer server = ((ServerLevel) level).getServer();
        BankAccountManager mgr = BankAccountManager.get();

        int accountId = mgr.nextAccountId(server);
        String countryCode = ModConfig.getInstance().getServerCountryCode();
        String iban = IbanGenerator.generateIban(
                countryCode,
                player.getName().getString(),
                bankPrefix,
                kind,
                accountId
        );
        BankAccount acc = mgr.createAccount(server, owner, kind, defaultCurrency, bankPrefix, accountId, iban);
        CardTier tier = CardItem.getTierFromItem(stack);
        acc.setCardTier(tier.name());

        stack.set(MONEY_COMPONENT.get(), acc.getBalance());
        stack.set(CURRENCY_COMPONENT.get(), acc.getCurrency());

        setIban(stack, iban);
        setOwner(stack, owner);
        setAccountKind(stack, kind);
        setOwnerName(stack, player.getName().getString());

    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;

        ServerLevel serverLevel = (ServerLevel) level;
        MinecraftServer server = serverLevel.getServer();
        if (!stack.has(IBAN_COMPONENT.get())) {
            boolean hasLegacyMoney = stack.has(MONEY_COMPONENT.get()) || stack.has(CURRENCY_COMPONENT.get());
            if (hasLegacyMoney) {
                migrateLegacyCardWithBalance(stack, player, serverLevel);
            } else {
                migrateNewEmptyCard(stack, player, serverLevel);
            }
        }
        String iban = getIban(stack);
        BankAccountManager mgr = BankAccountManager.get();
        mgr.getByIban(server, iban).ifPresent(acc -> {
            String newTier = getTierFromItem(stack).name();
            if (!newTier.equals(acc.getCardTier())) {
                acc.setCardTier(newTier);
            }
        });

    }

    private void migrateNewEmptyCard(ItemStack stack, ServerPlayer player, ServerLevel level) {
        UUID owner = player.getUUID();
        String defaultCurrency = MoneyMod.getDefaultCurrency();
        if (!ModItems.EXCHANGE_RATES.containsKey(defaultCurrency)) {
            defaultCurrency = "EUR";
        }

        String bankPrefix = "BSTN";
        AccountKind kind = AccountKind.DEBIT;
        MinecraftServer server = level.getServer();
        BankAccountManager mgr = BankAccountManager.get();

        int accountId = mgr.nextAccountId(server);
        String countryCode = ModConfig.getInstance().getServerCountryCode();
        String iban = IbanGenerator.generateIban(
                countryCode,
                player.getName().getString(),
                bankPrefix,
                kind,
                accountId
        );
        BankAccount acc = mgr.createAccount(server, owner, kind, defaultCurrency, bankPrefix, accountId, iban);
        acc.setCardTier(getTierFromItem(stack).name());

        stack.set(MONEY_COMPONENT.get(), acc.getBalance());
        stack.set(CURRENCY_COMPONENT.get(), acc.getCurrency());

        setIban(stack, iban);
        setOwner(stack, owner);
        setOwnerName(stack, player.getName().getString());
        setAccountKind(stack, kind);
    }

    private void migrateLegacyCardWithBalance(ItemStack stack, ServerPlayer player, ServerLevel level) {
        UUID owner = player.getUUID();

        double legacyBalance = stack.getOrDefault(MONEY_COMPONENT.get(), 0.0);
        String legacyCurrency = stack.getOrDefault(CURRENCY_COMPONENT.get(), MoneyMod.getDefaultCurrency());
        if (!ModItems.EXCHANGE_RATES.containsKey(legacyCurrency)) {
            legacyCurrency = MoneyMod.getDefaultCurrency();
        }

        String bankPrefix = "BSTN";
        AccountKind kind = AccountKind.DEBIT;
        MinecraftServer server = level.getServer();
        BankAccountManager mgr = BankAccountManager.get();

        int accountId = mgr.nextAccountId(server);
        String countryCode = ModConfig.getInstance().getServerCountryCode();
        String iban = IbanGenerator.generateIban(
                countryCode,
                player.getName().getString(),
                bankPrefix,
                kind,
                accountId
        );

        BankAccount acc = mgr.createAccount(server, owner, kind, legacyCurrency, bankPrefix, accountId, iban);
        acc.setBalance(legacyBalance);
        acc.setCardTier(getTierFromItem(stack).name());

        stack.set(MONEY_COMPONENT.get(), acc.getBalance());
        stack.set(CURRENCY_COMPONENT.get(), acc.getCurrency());

        setIban(stack, iban);
        setOwner(stack, owner);
        setOwnerName(stack, player.getName().getString());
        setAccountKind(stack, kind);

    }
    public static void setOwnerName(ItemStack stack, String name) {
        stack.set(OWNER_NAME_COMPONENT.get(), name);
    }

    public static String getOwnerName(ItemStack stack) {
        return stack.getOrDefault(OWNER_NAME_COMPONENT.get(), null);
    }
    @Override
    public boolean isFoil(ItemStack stack) {
        double money = stack.getOrDefault(MONEY_COMPONENT.get(), 0.0);
        String currency = stack.getOrDefault(CURRENCY_COMPONENT.get(), "EUR");

        if (!"EUR".equals(currency)) {
            Double rate = ModItems.EXCHANGE_RATES.get(currency);
            if (rate == null) return false;
            money = money / rate;
        }
        return money >= GLOW_THRESHOLD_EUR;
    }
    public static String formatMoney(double amount) {
        if(amount >= 1000000000){
            if(amount == 1000000000) {
                return "1B";
            } else {
                DecimalFormat df = new DecimalFormat("#.##");
                return df.format(amount / 1000000000.0) + "B";
            }
        } else if(amount >= 1000000){
            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(amount / 1000000.0) + "M";
        } else if(amount >= 100000){
            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(amount / 1000.0) + "K";
        } else if(amount >= 10000){
            DecimalFormat df = new DecimalFormat("#.##");
            String formatted = df.format(Math.round(amount * 100) / 100.0);
            if(formatted.contains(".")) {
                String[] parts = formatted.split("\\.");
                String integerPart = parts[0];
                String decimalPart = parts[1];
                if(integerPart.length() >= 4) {
                    String thousands = integerPart.substring(0, integerPart.length() - 3);
                    String hundreds = integerPart.substring(integerPart.length() - 3);
                    return thousands + " " + hundreds + "." + decimalPart;
                } else {
                    return formatted;
                }
            } else {
                if(formatted.length() >= 4) {
                    String thousands = formatted.substring(0, formatted.length() - 3);
                    String hundreds = formatted.substring(formatted.length() - 3);
                    return thousands + " " + hundreds;
                } else {
                    return formatted;
                }
            }
        } else {
            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(Math.round(amount * 100) / 100.0);
        }
    }
    @Override
    public @NotNull Component getName(ItemStack stack) {
        Component custom = stack.get(DataComponents.CUSTOM_NAME);
        if (custom != null) {
            return custom;
        }
        String ownerName = getOwnerName(stack);
        if (ownerName == null || ownerName.isEmpty()) {
            return super.getName(stack);
        }
        Item item = stack.getItem();
        String key;
        if (item == ModItems.RustyCard.get()) {
            key = "item.bubusteinmoneymod.rusty_card.named";
        } else if (item == ModItems.Card.get()) {
            key = "item.bubusteinmoneymod.classic_card.named";
        } else if (item == ModItems.GoldCard.get()) {
            key = "item.bubusteinmoneymod.gold_card.named";
        } else if (item == ModItems.SteelCard.get()) {
            key = "item.bubusteinmoneymod.steel_card.named";
        } else if (item == ModItems.SupremeCard.get()) {
            key = "item.bubusteinmoneymod.supreme_card.named";
        } else {
            return super.getName(stack);
        }

        return Component.translatable(key, ownerName);
    }
    public static void setIban(ItemStack stack, String iban) {
        stack.set(IBAN_COMPONENT.get(), iban);
    }
    public static String getIban(ItemStack stack) {
        return stack.getOrDefault(IBAN_COMPONENT.get(), null);
    }

    public static void setOwner(ItemStack stack, UUID owner) {
        stack.set(OWNER_COMPONENT.get(), owner.toString());
    }
    public static UUID getOwner(ItemStack stack) {
        String s = stack.getOrDefault(OWNER_COMPONENT.get(), null);
        return UUID.fromString(s);
    }
    public static void setAccountKind(ItemStack stack, AccountKind kind) {
        stack.set(ACCOUNT_KIND_COMPONENT.get(), kind.name());
    }
    public static AccountKind getAccountKind(ItemStack stack) {
        String s = stack.getOrDefault(ACCOUNT_KIND_COMPONENT.get(), AccountKind.DEBIT.name());
        return AccountKind.valueOf(s);
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
        if (iban == null) {
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

            String formattedMoney = formatMoney(balance);
            tooltip.add(Component.translatable(
                            "cardItem.bubusteinmoneymod.balance",
                            formattedMoney,
                            currency)
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFD700))));

            // Fee-uri
            if (stack.getItem() == ModItems.RustyCard.get()) {
                tooltip.add(Component.translatable("cardItem.bubusteinmoneymod.withdraw_fee", "10%")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
            } else if (stack.getItem() == ModItems.Card.get()) {
                tooltip.add(Component.translatable("cardItem.bubusteinmoneymod.withdraw_fee", "3%")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
            } else if (stack.getItem() == ModItems.GoldCard.get()) {
                tooltip.add(Component.translatable("cardItem.bubusteinmoneymod.withdraw_fee", "2%")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
            } else if (stack.getItem() == ModItems.SteelCard.get()) {
                tooltip.add(Component.translatable("cardItem.bubusteinmoneymod.withdraw_fee", "1%")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
            } else if (stack.getItem() == ModItems.SupremeCard.get()) {
                tooltip.add(Component.translatable("cardItem.bubusteinmoneymod.withdraw_fee", "0%")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
            }
        }
    }
}