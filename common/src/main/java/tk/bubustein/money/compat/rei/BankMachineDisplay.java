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

package tk.bubustein.money.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.plugin.common.displays.crafting.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.*;
import tk.bubustein.money.recipe.BankMachineRecipe;
import tk.bubustein.money.recipe.BankMachineRecipeShaped;
import java.util.*;

public class BankMachineDisplay extends DefaultCraftingDisplay<BankMachineRecipe> {
    public BankMachineDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<RecipeHolder<BankMachineRecipe>> recipe) {
        super(inputs, outputs, recipe);
        this.recipe = recipe;
    }
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BankMachineCategory.BANK_MACHINE_CATEGORY;
    }
    public Optional<RecipeHolder<BankMachineRecipe>> getOptionalRecipe() {
        return recipe;
    }
    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return getOptionalRecipe().map(RecipeHolder::id);
    }
    @Override
    public boolean isShapeless(){
        return getOptionalRecipe().map(holder -> holder.value().isShapeless()).orElse(false);
    }
    @Override
    public int getWidth() {
        if (recipe.isPresent() && recipe.get().value() instanceof BankMachineRecipeShaped shapedRecipe) {
            return shapedRecipe.getWidth();
        }
        return 3;
    }
    @Override
    public int getHeight() {
        if (recipe.isPresent() && recipe.get().value() instanceof BankMachineRecipeShaped shapedRecipe) {
            return shapedRecipe.getHeight();
        }
        return 3;
    }
}