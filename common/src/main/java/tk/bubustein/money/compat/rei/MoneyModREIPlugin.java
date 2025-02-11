package tk.bubustein.money.compat.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.block.ModBlocks;
import tk.bubustein.money.recipe.BankMachineRecipe;
import tk.bubustein.money.recipe.BankMachineRecipeShapedDisplay;
import tk.bubustein.money.recipe.BankMachineRecipeShapelessDisplay;
import tk.bubustein.money.recipe.ModRecipes;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static tk.bubustein.money.MoneyMod.LOGGER;

@Environment(EnvType.CLIENT)
public class MoneyModREIPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new BankMachineCategory());
        registry.addWorkstations(BankMachineCategory.BANK_MACHINE_CATEGORY, EntryStacks.of(ModBlocks.BANK_MACHINE.get()));
        LOGGER.info("[" + MoneyMod.MOD_ID + "] Bank Machine Category has been registered successfully.");
    }
    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.beginRecipeFiller(BankMachineRecipeShapelessDisplay.class)
                .filterType(BankMachineRecipeShapelessDisplay.TYPE)
                .fill(BankMachineClientDisplay.Shapeless::new);
        registry.beginRecipeFiller(BankMachineRecipeShapedDisplay.class)
                .filterType(BankMachineRecipeShapedDisplay.TYPE)
                .fill(BankMachineClientDisplay.Shaped::new);
        LOGGER.info("[" + MoneyMod.MOD_ID + "] Bank Machine Display has been registered successfully.");
    }
}