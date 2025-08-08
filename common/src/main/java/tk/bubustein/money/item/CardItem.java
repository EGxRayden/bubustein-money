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
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.TooltipFlag;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.command.ModCommands;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class CardItem extends Item {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(MoneyMod.MOD_ID, Registries.DATA_COMPONENT_TYPE);
    public static final Supplier<DataComponentType<Double>> MONEY_COMPONENT = COMPONENTS.register("money", () -> DataComponentType.<Double>builder()
            .persistent(Codec.DOUBLE)
            .networkSynchronized(ByteBufCodecs.DOUBLE)
            .build());
    public static final Supplier<DataComponentType<String>> CURRENCY_COMPONENT = COMPONENTS.register("currency", () -> DataComponentType.<String>builder()
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            .build());
    private static final double GLOW_THRESHOLD_EUR = 20000.0;
    public CardItem(Properties properties) {
        super(properties);
    }
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        if(!stack.has(MONEY_COMPONENT.get())) {
            stack.set(MONEY_COMPONENT.get(), 0.0);
            stack.set(CURRENCY_COMPONENT.get(), MoneyMod.getDefaultCurrency());
        }
    }
    @Override
    public boolean isFoil(ItemStack stack) {
        double money = getMoney(stack);
        String currency = getCurrency(stack);
        if(!currency.equals("EUR")) {
            money = ModCommands.convertCurrency(money, currency, "EUR");
        }
        return money >= GLOW_THRESHOLD_EUR;
    }
    public void addMoney(ItemStack stack, double amount) {
        UnaryOperator<Double> addMoney = existingMoney -> existingMoney + amount;
        stack.update(MONEY_COMPONENT.get(), 0.0, addMoney);
    }
    public double getMoney(ItemStack stack) {
        return stack.getOrDefault(MONEY_COMPONENT.get(), 0.0);
    }
    public void setMoney(ItemStack stack, double amount) {
        if(amount < 0) amount = 0;
        double finalAmount = amount;
        stack.update(MONEY_COMPONENT.get(), 0.0, existingMoney -> finalAmount);
    }
    public String getCurrency(ItemStack stack) {
        return stack.getOrDefault(CURRENCY_COMPONENT.get(), MoneyMod.getDefaultCurrency());
    }
    public void setCurrency(ItemStack stack, String currency) {
        stack.set(CURRENCY_COMPONENT.get(), currency);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        double money = getMoney(stack);
        String currency = getCurrency(stack);
        String formattedMoney = formatMoney(money);
        tooltip.add(Component.translatable("cardItem.bubusteinmoneymod.balance", formattedMoney, currency)
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFD700))));
        if (stack.getItem() == ModItems.Card.get())
            tooltip.add(Component.translatable("cardItem.bubusteinmoneymod.withdraw_fee", "3%")
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
        else if (stack.getItem() == ModItems.GoldCard.get())
            tooltip.add(Component.translatable("cardItem.bubusteinmoneymod.withdraw_fee", "2%")
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
        else if (stack.getItem() == ModItems.SteelCard.get())
            tooltip.add(Component.translatable("cardItem.bubusteinmoneymod.withdraw_fee", "1%")
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
        else if (stack.getItem() == ModItems.SupremeCard.get())
            tooltip.add(Component.translatable("cardItem.bubusteinmoneymod.withdraw_fee", "0%")
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
        else if(stack.getItem() == ModItems.RustyCard.get())
            tooltip.add(Component.translatable("cardItem.bubusteinmoneymod.withdraw_fee", "10%")
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
    }
}