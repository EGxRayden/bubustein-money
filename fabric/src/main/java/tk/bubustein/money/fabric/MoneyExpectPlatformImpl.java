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

package tk.bubustein.money.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import tk.bubustein.money.MoneyExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.mixin.PoiTypesInvoker;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class MoneyExpectPlatformImpl {
    public static List<Supplier<Block>> REGISTERED_BLOCKS = new ArrayList<>();
    /**
     * This is our actual method to {@link MoneyExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        var registry = Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, name), block.get());
        REGISTERED_BLOCKS.add(() -> registry);
        return () -> registry;
    }
    public static <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        var registry = Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, name), item.get());
        return () -> registry;
    }
    public static Supplier<PoiType> registerPoiType(String name, Supplier<Set<BlockState>> matchingStates) {
        ResourceKey<PoiType> resourceKey = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, name));
        var registry = Registry.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, resourceKey, new PoiType(matchingStates.get(), 1, 1));
        PoiTypesInvoker.invokeRegisterBlockStates(BuiltInRegistries.POINT_OF_INTEREST_TYPE.getOrThrow(resourceKey), matchingStates.get());
        return () -> registry;
    }
    public static Supplier<VillagerProfession> registerProfession(String name, Supplier<VillagerProfession> profession) {
        var registry = Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, name), profession.get());
        return () -> registry;
    }
}