package tk.bubustein.money.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;
import tk.bubustein.money.recipe.BankMachineRecipe;
import tk.bubustein.money.recipe.BankMachineRecipeShaped;
import tk.bubustein.money.recipe.ModRecipes;

import java.util.List;
import java.util.Optional;

public class BankMachineDisplay extends DefaultCraftingDisplay {
    private final Optional<RecipeHolder<BankMachineRecipe>> recipe;

    public BankMachineDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<RecipeHolder<BankMachineRecipe>> recipe) {
        super(inputs, outputs, recipe.map(bankMachineRecipeRecipeHolder -> bankMachineRecipeRecipeHolder.id().location()));
        this.recipe = recipe;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BankMachineCategory.BANK_MACHINE_CATEGORY;
    }
    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return getOptionalRecipe().map(holder -> holder.id().location());
    }
    @Override
    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
        if(isShapeless()) return (DisplaySerializer<? extends Display>) ModRecipes.BANK_MACHINE_SHAPELESS.get();
        else return (DisplaySerializer<? extends Display>) ModRecipes.BANK_MACHINE_SHAPED.get();
    }
    public Optional<RecipeHolder<BankMachineRecipe>> getOptionalRecipe() {
        return recipe;
    }
    @Override
    public boolean isShapeless() {
        return getOptionalRecipe().map(holder -> holder.value().isShapeless()).orElse(false);
    }
    @Override
    public int getWidth() {
        if (recipe.isPresent() && recipe.get().value() instanceof BankMachineRecipeShaped shapedRecipe) {
            return shapedRecipe.getIngredients().size() == 4 ? 2 : 3;
        }
        return 3;
    }
    @Override
    public int getHeight() {
        if (recipe.isPresent() && recipe.get().value() instanceof BankMachineRecipeShaped shapedRecipe) {
            return shapedRecipe.getIngredients().size() == 4 ? 2 : 3;
        }
        return 3;
    }
}