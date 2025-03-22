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

package tk.bubustein.money;

import com.mojang.logging.LogUtils;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;
import tk.bubustein.money.block.ModBlocks;
import tk.bubustein.money.config.ModConfig;
import tk.bubustein.money.item.CardItem;
import tk.bubustein.money.item.ModItems;
import tk.bubustein.money.recipe.ModRecipes;
import tk.bubustein.money.screen.ModMenuTypes;
import tk.bubustein.money.util.JigsawHelper;
import tk.bubustein.money.villager.ModVillagers;

public class MoneyMod {
    public static final String MOD_ID = "bubusteinmoneymod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> BANKNOTES = TABS.register("banknotes", () ->
            CreativeTabRegistry.create(Component.translatable("itemGroup.bubusteinmoneymod.banknotes"),
                    () -> new ItemStack(ModItems.Euro500.get())));
    public static final RegistrySupplier<CreativeModeTab> COINS = TABS.register("coins", () ->
            CreativeTabRegistry.create(Component.translatable("itemGroup.bubusteinmoneymod.coins"),
                    () -> new ItemStack(ModItems.Euro2.get())));
    public static final RegistrySupplier<CreativeModeTab> SPECIAL = TABS.register("special", () ->
            CreativeTabRegistry.create(Component.translatable("itemGroup.bubusteinmoneymod.special"),
                    () -> new ItemStack(ModBlocks.ATM.get())));
    public static final ResourceKey<LootTable> BANKER_HOUSE_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "chests/banker_house_chest"));
    public static final ResourceKey<LootTable> MANSION_DOUBLE_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "chests/mansion_double_chest"));
    public static final ResourceKey<LootTable> MANSION_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "chests/mansion_chest"));
    private static ModConfig config;
    public static void init() {
        LOGGER.info("[" + MOD_ID + "] Printing money. . . ;)");
        ModItems.init();
        config = ModConfig.getInstance();
        LOGGER.info("[" + MOD_ID + "] Crafting ATM. . .");
        ModBlocks.init();
        LOGGER.info("[" + MOD_ID + "] Registering Bank Machine GUI. . .");
        ModMenuTypes.init();
        LOGGER.info("[" + MOD_ID + "] Registering Bank Machine Recipes. . .");
        ModRecipes.init();
        LOGGER.info("[" + MOD_ID + "] Making new jobs. . .");
        ModVillagers.init();
        LOGGER.info("[" + MOD_ID + "] Creating Tabs. . .");
        TABS.register();
        LOGGER.info("[" + MOD_ID + "] Registering Cards. . .");
        CardItem.COMPONENTS.register();
        LOGGER.info("[" + MOD_ID + "] The Mod has been loaded successfully");
    }
    public static void registerJigsaws(MinecraftServer server){
        Registry<StructureTemplatePool> templatePoolRegistry = server.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
        Registry<StructureProcessorList> processorListRegistry = server.registryAccess().lookupOrThrow(Registries.PROCESSOR_LIST);

        ResourceLocation plainsPoolLocation = ResourceLocation.parse("minecraft:village/plains/houses");
        ResourceLocation desertPoolLocation = ResourceLocation.parse("minecraft:village/desert/houses");
        ResourceLocation savannaPoolLocation = ResourceLocation.parse("minecraft:village/savanna/houses");
        ResourceLocation snowyPoolLocation = ResourceLocation.parse("minecraft:village/snowy/houses");
        ResourceLocation taigaPoolLocation = ResourceLocation.parse("minecraft:village/taiga/houses");

        JigsawHelper.addBuildingToPool(templatePoolRegistry, processorListRegistry, plainsPoolLocation, "bubusteinmoneymod:banker_house", 20);
        JigsawHelper.addBuildingToPool(templatePoolRegistry, processorListRegistry, desertPoolLocation, "bubusteinmoneymod:banker_house", 7);
        JigsawHelper.addBuildingToPool(templatePoolRegistry, processorListRegistry, savannaPoolLocation, "bubusteinmoneymod:banker_house", 20);
        JigsawHelper.addBuildingToPool(templatePoolRegistry, processorListRegistry, taigaPoolLocation, "bubusteinmoneymod:banker_house", 20);
        JigsawHelper.addBuildingToPool(templatePoolRegistry, processorListRegistry, snowyPoolLocation, "bubusteinmoneymod:banker_house", 7);
    }
    public static void onServerStarting(MinecraftServer server) {
        config.load(server);
        setDefaultCurrency(config.getDefaultCurrency());
    }
    public static void setDefaultCurrency(String currency) {
        config.setDefaultCurrency(currency);
    }
    public static String getDefaultCurrency() {
        return config.getDefaultCurrency();
    }
    public static void saveConfig(MinecraftServer server) {
        config.save(server);
    }
}