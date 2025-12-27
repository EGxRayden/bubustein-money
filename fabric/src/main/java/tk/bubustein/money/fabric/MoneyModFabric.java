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

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import tk.bubustein.money.MoneyMod;
import net.fabricmc.api.ModInitializer;
import tk.bubustein.money.command.ModCommands;
import tk.bubustein.money.item.CardItem;
import tk.bubustein.money.item.ModItems;
import tk.bubustein.money.villager.ModVillagers;

public class MoneyModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MoneyMod.init();
        ModItems.registerCurrencyItems();
        ModItems.finalizeCurrencyItems();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ModVillagers.fillTradeData(server);
            MoneyMod.registerJigsaws(server);
            MoneyMod.onServerStarting(server);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            MoneyMod.LOGGER.info("[{}] Server stopping, cleaning up...", MoneyMod.MOD_ID);
            CardItem.shutdown();
            MoneyMod.saveConfig(server);
            MoneyMod.LOGGER.info("[{}] Cleanup complete", MoneyMod.MOD_ID);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ModCommands.register(dispatcher));
    }
}