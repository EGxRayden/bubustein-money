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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.AdvancementRequirements.Strategy;
import net.minecraft.advancements.AdvancementRewards.Builder;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BankMachineRecipeShapelessBuilder implements RecipeBuilder {
    private final Item result;
    private final int count;
    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    public BankMachineRecipeShapelessBuilder(ItemLike itemLike, int i) {
        this.result = itemLike.asItem();
        this.count = i;
    }
    public static BankMachineRecipeShapelessBuilder shapeless(ItemLike itemLike) {
        return new BankMachineRecipeShapelessBuilder( itemLike, 1);
    }
    public static BankMachineRecipeShapelessBuilder shapeless(ItemLike itemLike, int i) {
        return new BankMachineRecipeShapelessBuilder( itemLike, i);
    }
    public BankMachineRecipeShapelessBuilder requires(TagKey<Item> tagKey) {
        return this.requires(Ingredient.of(tagKey));
    }
    public BankMachineRecipeShapelessBuilder requires(ItemLike itemLike) {
        return this.requires(itemLike, 1);
    }
    public BankMachineRecipeShapelessBuilder requires(ItemLike itemLike, int i) {
        for(int j = 0; j < i; ++j) {
            this.requires(Ingredient.of(itemLike));
        }
        return this;
    }
    public BankMachineRecipeShapelessBuilder requires(Ingredient ingredient) {
        return this.requires(ingredient, 1);
    }
    public BankMachineRecipeShapelessBuilder requires(Ingredient ingredient, int i) {
        for(int j = 0; j < i; ++j) {
            this.ingredients.add(ingredient);
        }
        return this;
    }
    public @NotNull BankMachineRecipeShapelessBuilder unlockedBy(String string, Criterion<?> criterion) {
        this.criteria.put(string, criterion);
        return this;
    }
    public BankMachineRecipeShapelessBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }
    public @NotNull Item getResult() {
        return this.result;
    }
    public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
        this.ensureValid(resourceLocation);
        Advancement.Builder builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation)).rewards(Builder.recipe(resourceLocation)).requirements(Strategy.OR);
        Objects.requireNonNull(builder);
        this.criteria.forEach(builder::addCriterion);
        BankMachineRecipeShapeless shapelessRecipe = new BankMachineRecipeShapeless(Objects.requireNonNullElse(this.group, ""), new ItemStack(this.result, this.count), this.ingredients);
        recipeOutput.accept(resourceLocation, shapelessRecipe, builder.build(resourceLocation.withPrefix("recipes/")));
    }
    private void ensureValid(ResourceLocation resourceLocation) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
        }
    }
}