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
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.NotNull;

public record BankMachineRecipeShapedDisplay(int width, int height, List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {
    public static final MapCodec<BankMachineRecipeShapedDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(Codec.INT.fieldOf("width").forGetter(BankMachineRecipeShapedDisplay::width), Codec.INT.fieldOf("height").forGetter(BankMachineRecipeShapedDisplay::height), SlotDisplay.CODEC.listOf().fieldOf("ingredients").forGetter(BankMachineRecipeShapedDisplay::ingredients), SlotDisplay.CODEC.fieldOf("result").forGetter(BankMachineRecipeShapedDisplay::result), SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(BankMachineRecipeShapedDisplay::craftingStation)).apply(instance, BankMachineRecipeShapedDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BankMachineRecipeShapedDisplay> STREAM_CODEC;
    public static final RecipeDisplay.Type<BankMachineRecipeShapedDisplay> TYPE;

    public BankMachineRecipeShapedDisplay(int width, int height, List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation) {
        if (ingredients.size() != width * height) {
            throw new IllegalArgumentException("Invalid shaped recipe display contents");
        } else {
            this.width = width;
            this.height = height;
            this.ingredients = ingredients;
            this.result = result;
            this.craftingStation = craftingStation;
        }
    }
    public RecipeDisplay.@NotNull Type<BankMachineRecipeShapedDisplay> type() {
        return TYPE;
    }
    public boolean isEnabled(FeatureFlagSet featureFlagSet) {
        return this.ingredients.stream().allMatch((slotDisplay) -> slotDisplay.isEnabled(featureFlagSet)) && RecipeDisplay.super.isEnabled(featureFlagSet);
    }
    public int width() {
        return this.width;
    }
    public int height() {
        return this.height;
    }
    public List<SlotDisplay> ingredients() {
        return this.ingredients;
    }
    public @NotNull SlotDisplay result() {
        return this.result;
    }
    public @NotNull SlotDisplay craftingStation() {
        return this.craftingStation;
    }
    static {
        STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, BankMachineRecipeShapedDisplay::width, ByteBufCodecs.VAR_INT, BankMachineRecipeShapedDisplay::height, SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), BankMachineRecipeShapedDisplay::ingredients, SlotDisplay.STREAM_CODEC, BankMachineRecipeShapedDisplay::result, SlotDisplay.STREAM_CODEC, BankMachineRecipeShapedDisplay::craftingStation, BankMachineRecipeShapedDisplay::new);
        TYPE = new RecipeDisplay.Type<>(MAP_CODEC, STREAM_CODEC);
    }
}