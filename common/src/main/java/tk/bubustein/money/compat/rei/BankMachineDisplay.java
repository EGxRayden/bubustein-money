package tk.bubustein.money.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.plugin.common.displays.crafting.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.jetbrains.annotations.Nullable;
import tk.bubustein.money.recipe.*;
import java.util.*;
public abstract class BankMachineDisplay extends BasicDisplay implements CraftingDisplay {
    public BankMachineDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> recipe) {
        super(inputs, outputs, recipe);
    }
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BankMachineCategory.BANK_MACHINE_CATEGORY;
    }
    public static @Nullable CraftingDisplay of(RecipeHolder<? extends Recipe<?>> holder) {
        Recipe<?> recipe = holder.value();
        if (recipe instanceof BankMachineRecipeShapeless) {
            return new BankMachineShapelessDisplay((RecipeHolder<BankMachineRecipeShapeless>) holder);
        } else if (recipe instanceof BankMachineRecipeShaped) {
            return new BankMachineShapedDisplay((RecipeHolder<BankMachineRecipeShaped>) holder);
        } else {
            if (!recipe.isSpecial()) {
                for(RecipeDisplay d : recipe.display()) {
                    if (d instanceof BankMachineRecipeShapedDisplay display) {
                        return new BankMachineClientDisplay.Shaped(display, Optional.empty());
                    }
                    if (d instanceof BankMachineRecipeShapelessDisplay display) {
                        return new BankMachineClientDisplay.Shapeless(display, Optional.empty());
                    }
                }
            }
            return null;
        }
    }
}