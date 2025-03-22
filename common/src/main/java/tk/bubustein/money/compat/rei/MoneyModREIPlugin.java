package tk.bubustein.money.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.plugin.common.displays.crafting.CraftingDisplay;
import net.minecraft.resources.ResourceLocation;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.compat.rei.client.ClientSidedBankMachineDisplay;
import tk.bubustein.money.recipe.BankMachineRecipe;
import tk.bubustein.money.recipe.ModRecipes;

public class MoneyModREIPlugin implements REICommonPlugin {
    public static final CategoryIdentifier<CraftingDisplay> BANK_MACHINE_CATEGORY = CategoryIdentifier.of(MoneyMod.MOD_ID, "bank_machine");
    @Override
    public void registerDisplays(ServerDisplayRegistry registry){
        registry.beginRecipeFiller(BankMachineRecipe.class)
                .filterType(ModRecipes.BANK_MACHINE_RECIPE.get())
                .fill(BankMachineDisplay::of);
    }
    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "bank_machine_shaped"), ClientSidedBankMachineDisplay.Shaped.SERIALIZER);
        registry.register(ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID,"bank_machine_shapeless"), ClientSidedBankMachineDisplay.Shapeless.SERIALIZER);
        registry.register(ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "bank_machine_shaped"), BankMachineShapedDisplay.SERIALIZER);
        registry.register(ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "bank_machine_custom"), BankMachineCustomDisplay.SERIALIZER);
        registry.register(ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "bank_machine_shapeless"), BankMachineShapelessDisplay.SERIALIZER);
        registry.register(ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "bank_machine_custom_shaped"), BankMachineCustomShapedDisplay.SERIALIZER);
        registry.register(ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "bank_machine_custom_shapeless"), BankMachineCustomShapelessDisplay.SERIALIZER);
    }
}