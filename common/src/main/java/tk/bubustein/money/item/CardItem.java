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
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.TooltipFlag;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.command.ModCommands;
import java.text.DecimalFormat;
import java.util.function.Consumer;
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
    public void onCraftedBy(ItemStack stack, Player player) {
        stack.set(MONEY_COMPONENT.get(), 0.0);
        stack.set(CURRENCY_COMPONENT.get(), MoneyMod.getDefaultCurrency());
    }
    @Override
    public boolean isFoil(ItemStack stack) {
        double money = getMoney(stack);
        String currency = getCurrency(stack);
        if (!currency.equals("EUR")) {
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
        if (amount < 0) amount = 0;
        double finalAmount = amount;
        stack.update(MONEY_COMPONENT.get(), 0.0, existingMoney -> finalAmount);
    }
    public void convertMoney(ItemStack stack, String fromCurrency, String toCurrency) {
        double currentAmount = getMoney(stack);
        double convertedAmount = ModCommands.convertCurrency(currentAmount, fromCurrency, toCurrency);
        setMoney(stack, convertedAmount);
    }
    public String getCurrency(ItemStack stack) {
        return stack.getOrDefault(CURRENCY_COMPONENT.get(), MoneyMod.getDefaultCurrency());
    }
    public void setCurrency(ItemStack stack, String currency) {
        stack.set(CURRENCY_COMPONENT.get(), currency);
    }
    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext tooltip, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag) {
        double money = getMoney(stack);
        String currency = getCurrency(stack);
        DecimalFormat df = new DecimalFormat("#.##");
        String formattedMoney = df.format(Math.round(money * 100)/ 100.0);
        consumer.accept(Component.literal("Balance: " + formattedMoney + " " + currency)
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFD700))));
        if (stack.getItem() == ModItems.Card.get())
            consumer.accept(Component.literal("Withdrawal Fee: 3%").withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
        else if (stack.getItem() == ModItems.GoldCard.get())
            consumer.accept(Component.literal("Withdrawal Fee: 2%").withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
        else if (stack.getItem() == ModItems.SteelCard.get())
            consumer.accept(Component.literal("Withdrawal Fee: 0.5%").withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
    }
}