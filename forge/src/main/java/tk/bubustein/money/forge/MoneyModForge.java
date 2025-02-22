package tk.bubustein.money.forge;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tk.bubustein.money.MoneyMod;
import net.minecraftforge.eventbus.api.IEventBus;
import tk.bubustein.money.command.ModCommands;
import tk.bubustein.money.item.ModItems;
import tk.bubustein.money.screen.BankMachineScreen;
import tk.bubustein.money.screen.ModMenuTypes;
import tk.bubustein.money.villager.ModVillagers;

@Mod(MoneyMod.MOD_ID)
public class MoneyModForge {
    public MoneyModForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(MoneyMod.MOD_ID, bus);
        MoneyMod.init();
        ModItems.registerExchangeRates();
        MoneyExpectPlatformImpl.register(bus);
        MinecraftForge.EVENT_BUS.register(this);
    }
    @Mod.EventBusSubscriber(modid = MoneyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onCommonSetup(final FMLCommonSetupEvent event) {
            event.enqueueWork(ModVillagers::fillTradeData);
            event.enqueueWork(ModItems::registerCurrencyItems);
        }
    }
    @Mod.EventBusSubscriber(modid = MoneyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents{
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event){
            MenuScreens.register(ModMenuTypes.BANK_MACHINE_MENU.get(), BankMachineScreen::new);
        }
    }
    @SubscribeEvent
    public void onServerAboutToStartEvent(ServerAboutToStartEvent event) {
        MoneyMod.registerJigsaws(event.getServer());
        MoneyMod.onServerStarting(event.getServer());
    }
    @SubscribeEvent
    public void ServerStopping(ServerStoppingEvent event){
        MoneyMod.saveConfig(event.getServer());
    }
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ModCommands.register(dispatcher);
    }
}