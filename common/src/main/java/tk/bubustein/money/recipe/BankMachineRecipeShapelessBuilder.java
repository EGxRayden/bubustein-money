package tk.bubustein.money.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.AdvancementRequirements.Strategy;
import net.minecraft.advancements.AdvancementRewards.Builder;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public class BankMachineRecipeShapelessBuilder implements RecipeBuilder {
    private final HolderGetter<Item> items;
    private final ItemStack result;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    private BankMachineRecipeShapelessBuilder(HolderGetter<Item> holderGetter, ItemStack itemStack) {
        this.items = holderGetter;
        this.result = itemStack;
    }
    public static BankMachineRecipeShapelessBuilder shapeless(HolderGetter<Item> holderGetter, ItemStack itemStack) {
        return new BankMachineRecipeShapelessBuilder(holderGetter, itemStack);
    }
    public static BankMachineRecipeShapelessBuilder shapeless(HolderGetter<Item> holderGetter, ItemLike itemLike) {
        return shapeless(holderGetter, itemLike, 1);
    }
    public static BankMachineRecipeShapelessBuilder shapeless(HolderGetter<Item> holderGetter, ItemLike itemLike, int i) {
        return new BankMachineRecipeShapelessBuilder(holderGetter, itemLike.asItem().getDefaultInstance().copyWithCount(i));
    }
    public BankMachineRecipeShapelessBuilder requires(TagKey<Item> tagKey) {
        return this.requires(Ingredient.of(this.items.getOrThrow(tagKey)));
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
    public BankMachineRecipeShapelessBuilder unlockedBy(String string, Criterion<?> criterion) {
        this.criteria.put(string, criterion);
        return this;
    }
    public BankMachineRecipeShapelessBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }
    public Item getResult() {
        return this.result.getItem();
    }
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> resourceKey) {
        this.ensureValid(resourceKey);
        Advancement.Builder builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceKey)).rewards(Builder.recipe(resourceKey)).requirements(Strategy.OR);
        Objects.requireNonNull(builder);
        this.criteria.forEach(builder::addCriterion);
        BankMachineRecipeShapeless shapelessRecipe = new BankMachineRecipeShapeless(Objects.requireNonNullElse(this.group, ""), this.result, this.ingredients);
        recipeOutput.accept(resourceKey, shapelessRecipe, builder.build(resourceKey.location().withPrefix("recipes/")));
    }
    private void ensureValid(ResourceKey<Recipe<?>> resourceKey) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + resourceKey.location());
        }
    }
}