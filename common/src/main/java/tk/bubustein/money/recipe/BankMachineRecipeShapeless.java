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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tk.bubustein.money.block.ModBlocks;
import java.util.List;

public class BankMachineRecipeShapeless implements BankMachineRecipe {
    final String group;
    public final ItemStack result;
    final List<Ingredient> ingredients;
    @Nullable
    private PlacementInfo placementInfo;
    public BankMachineRecipeShapeless(String string, ItemStack itemStack, List<Ingredient> list) {
        this.group = string;
        this.result = itemStack;
        this.ingredients = list;
    }
    @Override
    public boolean isShapeless() {
        return true;
    }
    public @NotNull RecipeSerializer<BankMachineRecipeShapeless> getSerializer() {
        return ModRecipes.BANK_MACHINE_SHAPELESS.get();
    }
    public String group() {
        return this.group;
    }
    public @NotNull PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.ingredients);
        }
        return this.placementInfo;
    }
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (craftingInput.ingredientCount() != this.ingredients.size()) {
            return false;
        } else {
            return craftingInput.size() == 1 && this.ingredients.size() == 1 ? this.ingredients.getFirst().test(craftingInput.getItem(0)) : craftingInput.stackedContents().canCraft(this, null);
        }
    }
    public @NotNull ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        return this.result.copy();
    }
    public @NotNull List<RecipeDisplay> display() {
        return List.of(new BankMachineRecipeShapelessDisplay(this.ingredients.stream().map(Ingredient::display).toList(), new SlotDisplay.ItemStackSlotDisplay(this.result), new SlotDisplay.ItemSlotDisplay(Item.byBlock(ModBlocks.BANK_MACHINE.get()))));
    }
    public static class Serializer implements RecipeSerializer<BankMachineRecipeShapeless> {
        public static final Serializer INSTANCE = new Serializer();
        private static final MapCodec<BankMachineRecipeShapeless> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter((shapelessRecipe) -> shapelessRecipe.group), ItemStack.STRICT_CODEC.fieldOf("result").forGetter((shapelessRecipe) -> shapelessRecipe.result), Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter((shapelessRecipe) -> shapelessRecipe.ingredients)).apply(instance, BankMachineRecipeShapeless::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, BankMachineRecipeShapeless> STREAM_CODEC;
        public Serializer() {
        }
        public @NotNull MapCodec<BankMachineRecipeShapeless> codec() {
            return CODEC;
        }
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BankMachineRecipeShapeless> streamCodec() {
            return STREAM_CODEC;
        }
        static {
            STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, (shapelessRecipe) -> shapelessRecipe.group, ItemStack.STREAM_CODEC, (shapelessRecipe) -> shapelessRecipe.result, Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), (shapelessRecipe) -> shapelessRecipe.ingredients, BankMachineRecipeShapeless::new);
        }
    }
}