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

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

public class BankMachineRecipeShaped implements BankMachineRecipe {
    final ShapedRecipePattern pattern;
    public final ItemStack result;
    final String group;
    final boolean showNotification;
    @Nullable
    private PlacementInfo placementInfo;
    public BankMachineRecipeShaped(String string,ShapedRecipePattern shapedRecipePattern, ItemStack itemStack, boolean bl) {
        this.group = string;
        this.pattern = shapedRecipePattern;
        this.result = itemStack;
        this.showNotification = bl;
    }
    public BankMachineRecipeShaped(String string, ShapedRecipePattern shapedRecipePattern, ItemStack itemStack) {
        this(string, shapedRecipePattern, itemStack, true);
    }
    public @NotNull RecipeSerializer<? extends BankMachineRecipeShaped> getSerializer() {
        return ModRecipes.BANK_MACHINE_SHAPED.get();
    }
    @Override
    public boolean isShapeless() {
        return false;
    }
    public String group() {
        return this.group;
    }
    @VisibleForTesting
    public List<Optional<Ingredient>> getIngredients() {
        return this.pattern.ingredients();
    }
    public @NotNull PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.createFromOptionals(this.pattern.ingredients());
        }
        return this.placementInfo;
    }
    public boolean showNotification() {
        return this.showNotification;
    }
    public boolean matches(CraftingInput craftingInput, Level level) {
        return this.pattern.matches(craftingInput);
    }
    public @NotNull ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        return this.result.copy();
    }
    public int getWidth() {
        return this.pattern.width();
    }
    public int getHeight() {
        return this.pattern.height();
    }
    public @NotNull List<RecipeDisplay> display() {
        return List.of(new BankMachineRecipeShapedDisplay(this.pattern.width(), this.pattern.height(), this.pattern.ingredients().stream().map((optional) -> optional.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE)).toList(), new SlotDisplay.ItemStackSlotDisplay(this.result), new SlotDisplay.ItemSlotDisplay(Item.byBlock(ModBlocks.BANK_MACHINE.get()))));
    }
    public static class Serializer implements RecipeSerializer<BankMachineRecipeShaped> {
        public static final Serializer INSTANCE = new Serializer();
        public static final MapCodec<BankMachineRecipeShaped> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(Codec.STRING.optionalFieldOf("group", "")
                                .forGetter((shapedRecipe) -> shapedRecipe.group),
                        ShapedRecipePattern.MAP_CODEC.forGetter((shapedRecipe) -> shapedRecipe.pattern),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter((shapedRecipe) -> shapedRecipe.result),
                        Codec.BOOL.optionalFieldOf("show_notification", true)
                                .forGetter((shapedRecipe) -> shapedRecipe.showNotification)).apply(instance, BankMachineRecipeShaped::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, BankMachineRecipeShaped> STREAM_CODEC = StreamCodec.of(BankMachineRecipeShaped.Serializer::toNetwork, BankMachineRecipeShaped.Serializer::fromNetwork);
        public Serializer() {
        }
        public MapCodec<BankMachineRecipeShaped> codec() {
            return CODEC;
        }
        public StreamCodec<RegistryFriendlyByteBuf, BankMachineRecipeShaped> streamCodec() {
            return STREAM_CODEC;
        }
        private static BankMachineRecipeShaped fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            String string = registryFriendlyByteBuf.readUtf();
            ShapedRecipePattern shapedRecipePattern = ShapedRecipePattern.STREAM_CODEC.decode(registryFriendlyByteBuf);
            ItemStack itemStack = ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
            boolean bl = registryFriendlyByteBuf.readBoolean();
            return new BankMachineRecipeShaped(string, shapedRecipePattern, itemStack, bl);
        }
        private static void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, BankMachineRecipeShaped shapedRecipe) {
            registryFriendlyByteBuf.writeUtf(shapedRecipe.group);
            ShapedRecipePattern.STREAM_CODEC.encode(registryFriendlyByteBuf, shapedRecipe.pattern);
            ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, shapedRecipe.result);
            registryFriendlyByteBuf.writeBoolean(shapedRecipe.showNotification);
        }
    }
}