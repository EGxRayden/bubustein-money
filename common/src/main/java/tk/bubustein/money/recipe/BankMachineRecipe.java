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
    @NotNull RecipeSerializer<? extends BankMachineRecipe> getSerializer();

    default @NotNull RecipeBookCategory recipeBookCategory() {
        return ModRecipes.BANK_MACHINE_CATEGORY.get();
    }
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