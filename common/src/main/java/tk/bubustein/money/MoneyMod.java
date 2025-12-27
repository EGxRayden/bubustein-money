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
import tk.bubustein.money.util.PlayerJoinHandler;
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
    public static final ResourceKey<LootTable> TAIGA_BANKER_HOUSE_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "chests/taiga_banker_house_chest"));
    public static final ResourceKey<LootTable> SNOWY_BANKER_HOUSE_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "chests/snowy_banker_house_chest"));
    public static final ResourceKey<LootTable> SAVANNA_BANKER_HOUSE_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "chests/savanna_banker_house_chest"));
    public static final ResourceKey<LootTable> DESERT_BANKER_HOUSE_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "chests/desert_banker_house_chest"));
    public static final ResourceKey<LootTable> COTTAGE_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "chests/mansion_forest_chest"));
    public static final ResourceKey<LootTable> MANSION_DOUBLE_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "chests/mansion_double_chest"));
    public static final ResourceKey<LootTable> HOTEL_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "chests/hotel_chest"));
    public static final ResourceKey<LootTable> MANSION_CHEST = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "chests/mansion_chest"));

    private static ModConfig config;
    private static volatile String cachedDefaultCurrency = "EUR";

    public static void init() {
        config = ModConfig.getInstance();

        LOGGER.info("[{}] Registering Data Components...", MOD_ID);
        CardItem.COMPONENTS.register();

        LOGGER.info("[{}] Printing money. . . ;)", MOD_ID);
        ModItems.init();

        LOGGER.info("[{}] Crafting ATM. . .", MOD_ID);
        ModBlocks.init();

        LOGGER.info("[{}] Registering Bank Machine GUI. . .", MOD_ID);
        ModMenuTypes.init();

        LOGGER.info("[{}] Registering Bank Machine Recipes. . .", MOD_ID);
        ModRecipes.init();

        LOGGER.info("[{}] Making new jobs. . .", MOD_ID);
        ModVillagers.init();

        LOGGER.info("[{}] Creating Tabs. . .", MOD_ID);
        TABS.register();
        LOGGER.info("[{}] Registering player join handler...", MOD_ID);
        PlayerJoinHandler.register();
        LOGGER.info("[{}] The Mod has been loaded successfully", MOD_ID);
    }

    public static void registerJigsaws(MinecraftServer server){
        Registry<StructureTemplatePool> templatePoolRegistry = server.registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = server.registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();
        try {
            JigsawHelper.addBuildingToPool(templatePoolRegistry, processorListRegistry, ResourceLocation.parse("minecraft:village/plains/houses"), "bubusteinmoneymod:plains_banker_house", 20);
        } catch (Exception e) {
            LOGGER.warn("[{}] Jigsaw plains pool error: {}", MOD_ID, e.toString());
        }
        try {
            JigsawHelper.addBuildingToPool(templatePoolRegistry, processorListRegistry, ResourceLocation.parse("minecraft:village/desert/houses"), "bubusteinmoneymod:desert_banker_house", 20);
        } catch (Exception e) {
            LOGGER.warn("[{}] Jigsaw desert pool error: {}", MOD_ID, e.toString());
        }
        try {
            JigsawHelper.addBuildingToPool(templatePoolRegistry, processorListRegistry, ResourceLocation.parse("minecraft:village/savanna/houses"), "bubusteinmoneymod:savanna_banker_house", 20);
        } catch (Exception e) {
            LOGGER.warn("[{}] Jigsaw savanna pool error: {}", MOD_ID, e.toString());
        }
        try {
            JigsawHelper.addBuildingToPool(templatePoolRegistry, processorListRegistry, ResourceLocation.parse("minecraft:village/taiga/houses"), "bubusteinmoneymod:taiga_banker_house", 20);
        } catch (Exception e) {
            LOGGER.warn("[{}] Jigsaw taiga pool error: {}", MOD_ID, e.toString());
        }
        try {
            JigsawHelper.addBuildingToPool(templatePoolRegistry, processorListRegistry, ResourceLocation.parse("minecraft:village/snowy/houses"), "bubusteinmoneymod:snowy_banker_house", 20);
        } catch (Exception e) {
            LOGGER.warn("[{}] Jigsaw snowy pool error: {}", MOD_ID, e.toString());
        }
    }
    public static void onServerStarting(MinecraftServer server) {
        config.loadWithMigration(server);
        ModItems.initializeExchangeRates(server);
        String loadedCurrency = config.getDefaultCurrency();
        if (loadedCurrency == null || !ModItems.EXCHANGE_RATES.containsKey(loadedCurrency)) {
            LOGGER.warn("[{}] Invalid default currency '{}' in config. Resetting to EUR.", MOD_ID, loadedCurrency);
            loadedCurrency = "EUR";
            config.setDefaultCurrency(loadedCurrency);
        }
        setDefaultCurrency(loadedCurrency);
        LOGGER.info("[{}] Default currency set to: {}", MOD_ID, loadedCurrency);
    }
    public static void setDefaultCurrency(String currency) {
        if (currency == null || !ModItems.EXCHANGE_RATES.containsKey(currency)) {
            LOGGER.error("[{}] Attempted to set invalid default currency: {}", MOD_ID, currency);
            config.setDefaultCurrency("EUR");
            cachedDefaultCurrency = "EUR";
            return;
        }
        config.setDefaultCurrency(currency);
        cachedDefaultCurrency = currency;
        LOGGER.info("[{}] Default currency changed to: {}", MOD_ID, currency);
    }
    public static String getDefaultCurrency() {
        if (config == null) {
            LOGGER.warn("[{}] Config not initialized, using fallback EUR", MOD_ID);
            return "EUR";
        }
        return cachedDefaultCurrency;
    }
    public static void saveConfig(MinecraftServer server) {
        if (config != null) config.save(server);
        else LOGGER.error("[{}] Tried to save config but config is null!", MOD_ID);
    }
}