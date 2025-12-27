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

package tk.bubustein.money.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import tk.bubustein.money.MoneyMod;
import net.neoforged.bus.api.IEventBus;
import tk.bubustein.money.command.ModCommands;
import tk.bubustein.money.item.CardItem;
import tk.bubustein.money.item.ModItems;
import tk.bubustein.money.screen.BankMachineScreen;
import tk.bubustein.money.screen.ModMenuTypes;
import tk.bubustein.money.villager.ModVillagers;
import java.util.HashMap;

@SuppressWarnings("UnstableApiUsage")
@Mod(MoneyMod.MOD_ID)
public class MoneyModNeoForge {
    public MoneyModNeoForge(IEventBus modEventBus) {
        MoneyMod.init();
        MoneyExpectPlatformImpl.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent
    public void onServerAboutToStartEvent(ServerAboutToStartEvent event) {
        MoneyMod.registerJigsaws(event.getServer());
        ModVillagers.fillTradeData(event.getServer());
        MoneyMod.onServerStarting(event.getServer());
    }
    @EventBusSubscriber(modid = MoneyMod.MOD_ID)
    public static class ModEvents {
        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            ModItems.registerCurrencyItems();
            ModItems.finalizeCurrencyItems();
        }
    }
    @SubscribeEvent
    public void ServerStopping(ServerStoppingEvent event){
        MoneyMod.LOGGER.info("[{}] Server stopping, cleaning up...", MoneyMod.MOD_ID);
        CardItem.shutdown();
        MoneyMod.saveConfig(event.getServer());
        MoneyMod.LOGGER.info("[{}] Cleanup complete", MoneyMod.MOD_ID);
    }
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ModCommands.register(dispatcher);
    }
    @EventBusSubscriber(modid = MoneyMod.MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            registerScreens(new RegisterMenuScreensEvent(HashMap.newHashMap(1)));
        }
        @SubscribeEvent
        private static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.BANK_MACHINE_MENU.get(), BankMachineScreen::new);
        }
    }
}