package tk.bubustein.money.compat.rei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import tk.bubustein.money.recipe.BankMachineRecipeShaped;

public class BankMachineShapedDisplay extends BankMachineDisplay {
    public static final DisplaySerializer<BankMachineDisplay> SERIALIZER;
    private final int width;
    private final int height;
    public BankMachineShapedDisplay(RecipeHolder<BankMachineRecipeShaped> recipe) {
        super(CollectionUtils.map(recipe.value().getIngredients(), (opt) -> opt.map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty())), List.of(EntryIngredients.of((ItemLike) recipe.value())), Optional.of(recipe.id().location()));
        this.width = (recipe.value()).getWidth();
        this.height = (recipe.value()).getHeight();
    }
    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
    public boolean isShapeless() {
        return false;
    }
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
    static {
        SERIALIZER = DisplaySerializer.of(RecordCodecBuilder.mapCodec((instance) -> instance.group(EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(BasicDisplay::getInputEntries), EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(BasicDisplay::getOutputEntries), ResourceLocation.CODEC.optionalFieldOf("location").forGetter(BasicDisplay::getDisplayLocation), Codec.INT.fieldOf("width").forGetter(SimpleGridMenuDisplay::getWidth), Codec.INT.fieldOf("height").forGetter(SimpleGridMenuDisplay::getHeight)).apply(instance, CustomShapedDisplay::new)), StreamCodec.composite(EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getInputEntries, EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getOutputEntries, ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), BasicDisplay::getDisplayLocation, ByteBufCodecs.INT, SimpleGridMenuDisplay::getWidth, ByteBufCodecs.INT, SimpleGridMenuDisplay::getHeight, CustomShapedDisplay::new));
    }
}