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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import tk.bubustein.money.recipe.BankMachineRecipeShaped;
import java.util.List;
import java.util.Optional;

public class BankMachineShapedDisplay extends BankMachineDisplay {
    public static final DisplaySerializer<BankMachineDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(BankMachineDisplay::getInputEntries),
                    EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(BankMachineDisplay::getOutputEntries),
                    ResourceLocation.CODEC.optionalFieldOf("location").forGetter(BankMachineDisplay::getDisplayLocation),
                    Codec.INT.fieldOf("width").forGetter(BankMachineDisplay::getWidth),
                    Codec.INT.fieldOf("height").forGetter(BankMachineDisplay::getHeight)
            ).apply(instance, BankMachineCustomShapedDisplay::new)),
            StreamCodec.composite(
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    BankMachineDisplay::getInputEntries,
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    BankMachineDisplay::getOutputEntries,
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
                    BankMachineDisplay::getDisplayLocation,
                    ByteBufCodecs.INT,
                    BankMachineDisplay::getWidth,
                    ByteBufCodecs.INT,
                    BankMachineDisplay::getHeight,
                    BankMachineCustomShapedDisplay::new
            ));
    private final int width;
    private final int height;
    public BankMachineShapedDisplay(RecipeHolder<BankMachineRecipeShaped> recipe) {
        super(
                CollectionUtils.map(recipe.value().getIngredients(), opt -> opt.map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty())),
                List.of(EntryIngredients.of(recipe.value().result)),
                Optional.of(recipe.id().location())
        );
        this.width = recipe.value().getWidth();
        this.height = recipe.value().getHeight();
    }
    @Override
    public int getWidth() {
        return this.width;
    }
    @Override
    public int getHeight() {
        return this.height;
    }
    @Override
    public boolean isShapeless() {
        return false;
    }
    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}