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
package tk.bubustein.money.villager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import tk.bubustein.money.bank.AccountKind;
import tk.bubustein.money.bank.BankAccountSavedData;
import tk.bubustein.money.bank.CreditCardTier;
import tk.bubustein.money.item.ModItems;

import java.util.Optional;

public record CreditCardTrade(CreditCardTier tier, int emeraldBlocksCost,
                              double minDebitBalance) implements VillagerTrades.ItemListing {

    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        if (trader instanceof ServerPlayer player) {
            MinecraftServer server = player.getServer();
            if (server == null) return null;

            BankAccountSavedData data = BankAccountSavedData.get(server);
            boolean eligible = data.getAccountsForPlayer(player.getUUID()).values().stream()
                    .filter(acc -> acc.getKind() == AccountKind.DEBIT)
                    .anyMatch(acc -> {
                        double balance = acc.getBalance();
                        String currency = acc.getCurrency();
                        if (Math.abs(balance) < 0.01) {
                            return false;
                        }
                        double balanceEur;
                        if ("EUR".equalsIgnoreCase(currency)) {
                            balanceEur = balance;
                        } else {
                            Double rate = ModItems.EXCHANGE_RATES.get(currency);
                            if (rate == null || rate <= 0) {
                                return false;
                            }
                            balanceEur = balance / rate;
                        }
                        return balanceEur >= minDebitBalance;
                    });
            if (!eligible) {
                return new MerchantOffer(new ItemCost(Items.DIAMOND), Optional.empty(), new ItemStack(Items.DIRT, 64), 2, 8, 0.9f);
            }
        }
        ItemCost input1 = new ItemCost(Items.EMERALD_BLOCK, emeraldBlocksCost);
        ItemStack input2 = getMaterialForTier(tier);
        ItemStack output = new ItemStack(getCreditCardItem(tier));

        return new MerchantOffer(input1, Optional.of(new ItemCost(input2.getItem(), getCount(tier))), output, 3, 16, 0.5f);
    }
    private int getCount(CreditCardTier tier){
        return tier==CreditCardTier.CLASSIC ? 1 : 8;
    }
    private ItemStack getMaterialForTier(CreditCardTier tier) {
        return switch (tier) {
            case CLASSIC -> new ItemStack(ModItems.PlasticCard.get());
            case GOLD -> new ItemStack(Items.GOLD_INGOT);
            case PLATINUM -> new ItemStack(Items.DIAMOND);
        };
    }
    private Item getCreditCardItem(CreditCardTier tier) {
        return switch (tier) {
            case CLASSIC -> ModItems.ClassicCreditCard.get();
            case GOLD -> ModItems.GoldCreditCard.get();
            case PLATINUM -> ModItems.PlatinumCreditCard.get();
        };
    }
}
