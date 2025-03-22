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

package tk.bubustein.money.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import tk.bubustein.money.MoneyExpectPlatform;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.block.custom.ATM;
import tk.bubustein.money.block.custom.BankMachine;
import java.util.function.Supplier;
@SuppressWarnings("UnstableApiUsage")
public class ModBlocks {
    public static void init(){}
    public static final Supplier<Block> ATM = registerBlock("atm",
            () -> new ATM(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(6f).noOcclusion().requiresCorrectToolForDrops().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "atm")))));
    public static final Supplier<Block> BANK_MACHINE = registerBlock("bank_machine", BankMachine::new);
    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        Supplier<T> toReturn = MoneyExpectPlatform.registerBlock(name, block);
        MoneyExpectPlatform.registerItem(name, () -> new BlockItem(toReturn.get(), new Item.Properties().arch$tab(MoneyMod.SPECIAL).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, name)))));
        return toReturn;
    }
}