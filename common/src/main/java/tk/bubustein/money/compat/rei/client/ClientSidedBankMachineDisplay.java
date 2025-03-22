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

package tk.bubustein.money.compat.rei.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import tk.bubustein.money.compat.rei.BankMachineDisplay;
import tk.bubustein.money.recipe.BankMachineRecipeShapedDisplay;
import tk.bubustein.money.recipe.BankMachineRecipeShapelessDisplay;

public abstract class ClientSidedBankMachineDisplay extends BankMachineDisplay {
    private final Optional<RecipeDisplayId> id;

    public ClientSidedBankMachineDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<RecipeDisplayId> id) {
        super(inputs, outputs, Optional.empty());
        this.id = id;
    }
    public Optional<RecipeDisplayId> recipeDisplayId() {
        return this.id;
    }

    public static class Shaped extends ClientSidedBankMachineDisplay {
        public static final DisplaySerializer<Shaped> SERIALIZER;
        private final int width;
        private final int height;

        public Shaped(BankMachineRecipeShapedDisplay recipe, Optional<RecipeDisplayId> id) {
            super(EntryIngredients.ofSlotDisplays(recipe.ingredients()), List.of(EntryIngredients.ofSlotDisplay(recipe.result())), id);
            this.width = recipe.width();
            this.height = recipe.height();
        }
        public Shaped(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<RecipeDisplayId> id, int width, int height) {
            super(inputs, outputs, id);
            this.width = width;
            this.height = height;
        }
        public boolean isShapeless() {
            return false;
        }
        public int getWidth() {
            return this.width;
        }
        public int getHeight() {
            return this.height;
        }
        public DisplaySerializer<? extends Display> getSerializer() {
            return SERIALIZER;
        }
        static {
            SERIALIZER = DisplaySerializer.of(RecordCodecBuilder.mapCodec((instance) -> instance.group(EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(BasicDisplay::getInputEntries), EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(BasicDisplay::getOutputEntries), Codec.INT.xmap(RecipeDisplayId::new, RecipeDisplayId::index).optionalFieldOf("id").forGetter(ClientSidedBankMachineDisplay::recipeDisplayId), Codec.INT.fieldOf("width").forGetter(Shaped::getWidth), Codec.INT.fieldOf("height").forGetter(Shaped::getHeight)).apply(instance, Shaped::new)), StreamCodec.composite(EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getInputEntries, EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getOutputEntries, ByteBufCodecs.optional(ByteBufCodecs.INT.map(RecipeDisplayId::new, RecipeDisplayId::index)), ClientSidedBankMachineDisplay::recipeDisplayId, ByteBufCodecs.INT, Shaped::getWidth, ByteBufCodecs.INT, Shaped::getHeight, Shaped::new), false);
        }
    }
    public static class Shapeless extends ClientSidedBankMachineDisplay {
        public static final DisplaySerializer<Shapeless> SERIALIZER;

        public Shapeless(BankMachineRecipeShapelessDisplay recipe, Optional<RecipeDisplayId> id) {
            super(EntryIngredients.ofSlotDisplays(recipe.ingredients()), List.of(EntryIngredients.ofSlotDisplay(recipe.result())), id);
        }

        public Shapeless(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<RecipeDisplayId> id) {
            super(inputs, outputs, id);
        }

        public boolean isShapeless() {
            return true;
        }

        public int getWidth() {
            return this.getInputEntries().size() > 4 ? 3 : 2;
        }

        public int getHeight() {
            return this.getInputEntries().size() > 4 ? 3 : 2;
        }
        public int getInputWidth(int craftingWidth, int craftingHeight) {
            return craftingWidth * craftingHeight <= this.getInputEntries().size() ? craftingWidth : Math.min(this.getInputEntries().size(), 3);
        }
        public int getInputHeight(int craftingWidth, int craftingHeight) {
            return (int)Math.ceil((double)this.getInputEntries().size() / (double)this.getInputWidth(craftingWidth, craftingHeight));
        }
        public DisplaySerializer<? extends Display> getSerializer() {
            return SERIALIZER;
        }
        static {
            SERIALIZER = DisplaySerializer.of(RecordCodecBuilder.mapCodec((instance) -> instance.group(EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(BasicDisplay::getInputEntries), EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(BasicDisplay::getOutputEntries), Codec.INT.xmap(RecipeDisplayId::new, RecipeDisplayId::index).optionalFieldOf("id").forGetter(ClientSidedBankMachineDisplay::recipeDisplayId)).apply(instance, Shapeless::new)), StreamCodec.composite(EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getInputEntries, EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getOutputEntries, ByteBufCodecs.optional(ByteBufCodecs.INT.map(RecipeDisplayId::new, RecipeDisplayId::index)), ClientSidedBankMachineDisplay::recipeDisplayId, Shapeless::new), false);
        }
    }
}