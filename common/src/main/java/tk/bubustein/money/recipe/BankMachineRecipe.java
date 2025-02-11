package tk.bubustein.money.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

public interface BankMachineRecipe extends Recipe<CraftingInput> {
    default @NotNull RecipeType<BankMachineRecipe> getType() {
        return ModRecipes.BANK_MACHINE_RECIPE.get();
    }
    boolean isShapeless();
    RecipeSerializer<? extends BankMachineRecipe> getSerializer();
    default NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput) {
        return defaultCraftingReminder(craftingInput);
    }
    static NonNullList<ItemStack> defaultCraftingReminder(CraftingInput craftingInput) {
        NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);
        for(int i = 0; i < nonNullList.size(); ++i) {
            Item item = craftingInput.getItem(i).getItem();
            nonNullList.set(i, item.getCraftingRemainder());
        }
        return nonNullList;
    }
}