/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package tk.bubustein.money.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.plugin.common.displays.crafting.CraftingDisplay;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.jetbrains.annotations.Nullable;
import tk.bubustein.money.compat.rei.client.ClientSidedBankMachineDisplay;
import tk.bubustein.money.recipe.*;
import java.util.List;
import java.util.Optional;

public abstract class BankMachineDisplay extends BasicDisplay implements CraftingDisplay {
    public BankMachineDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> recipe) {
        super(inputs, outputs, recipe);
    }
    public static @Nullable BankMachineDisplay of(RecipeHolder<? extends Recipe<?>> holder) {
        Recipe<?> recipe = holder.value();
        if (recipe instanceof BankMachineRecipeShapeless) {
            return new BankMachineShapelessDisplay((RecipeHolder<BankMachineRecipeShapeless>) holder);
        } else if (recipe instanceof BankMachineRecipeShaped) {
            return new BankMachineShapedDisplay((RecipeHolder<BankMachineRecipeShaped>) holder);
        } else {
            if (!recipe.isSpecial()) {
                for(RecipeDisplay d : recipe.display()) {
                    if (d instanceof BankMachineRecipeShapedDisplay display) {
                        return new ClientSidedBankMachineDisplay.Shaped(display, Optional.empty()) {
                        };
                    }
                    if (d instanceof BankMachineRecipeShapelessDisplay display) {
                        return new ClientSidedBankMachineDisplay.Shapeless(display, Optional.empty());
                    }
                }
            }
            return null;
        }
    }
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return MoneyModREIPlugin.BANK_MACHINE_CATEGORY;
    }
}