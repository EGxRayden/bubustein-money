package tk.bubustein.money.compat.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.block.ModBlocks;
import tk.bubustein.money.compat.rei.client.BankMachineCategory;
import tk.bubustein.money.compat.rei.client.ClientSidedBankMachineDisplay;
import tk.bubustein.money.recipe.BankMachineRecipeShapedDisplay;
import tk.bubustein.money.recipe.BankMachineRecipeShapelessDisplay;
import static tk.bubustein.money.MoneyMod.LOGGER;

@Environment(EnvType.CLIENT)
public class MoneyModREIClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new BankMachineCategory());
        registry.addWorkstations(MoneyModREIPlugin.BANK_MACHINE_CATEGORY, EntryStacks.of(ModBlocks.BANK_MACHINE.get()));
        LOGGER.info("[" + MoneyMod.MOD_ID + "] Bank Machine Category has been registered successfully.");
    }
    @Override
    public void registerDisplays(DisplayRegistry registry){
        registry.beginRecipeFiller(BankMachineRecipeShapelessDisplay.class)
                .filterType(BankMachineRecipeShapelessDisplay.TYPE)
                .fill(ClientSidedBankMachineDisplay.Shapeless::new);
        registry.beginRecipeFiller(BankMachineRecipeShapedDisplay.class)
                .filterType(BankMachineRecipeShapedDisplay.TYPE)
                .fill(ClientSidedBankMachineDisplay.Shaped::new);
    }
}