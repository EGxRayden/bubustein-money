package tk.bubustein.money.compat.rei;

import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import tk.bubustein.money.recipe.BankMachineRecipe;
import tk.bubustein.money.recipe.ModRecipes;

public class MoneyModREIPlugin implements REICommonPlugin {
    @Override
    public void registerDisplays(ServerDisplayRegistry registry){
        registry.beginRecipeFiller(BankMachineRecipe.class)
                .filterType(ModRecipes.BANK_MACHINE_RECIPE.get())
                .fill(BankMachineDisplay::of);
    }
}