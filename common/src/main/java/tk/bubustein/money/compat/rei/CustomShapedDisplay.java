package tk.bubustein.money.compat.rei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Optional;

public class CustomShapedDisplay extends BankMachineDisplay {
    public static final DisplaySerializer<CustomShapedDisplay> SERIALIZER;
    private final int width;
    private final int height;
    public CustomShapedDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location, int width, int height) {
        super(input, output, location);
        this.width = width;
        this.height = height;
    }
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
    static {
        SERIALIZER = DisplaySerializer.of(RecordCodecBuilder.mapCodec((instance) -> instance.group(EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(BasicDisplay::getInputEntries), EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(BasicDisplay::getOutputEntries), ResourceLocation.CODEC.optionalFieldOf("location").forGetter(BasicDisplay::getDisplayLocation), Codec.INT.fieldOf("width").forGetter(CustomShapedDisplay::getWidth), Codec.INT.fieldOf("height").forGetter(CustomShapedDisplay::getHeight)).apply(instance, CustomShapedDisplay::new)), StreamCodec.composite(EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getInputEntries, EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getOutputEntries, ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), BasicDisplay::getDisplayLocation, ByteBufCodecs.INT, CustomShapedDisplay::getWidth, ByteBufCodecs.INT, CustomShapedDisplay::getHeight, CustomShapedDisplay::new));
    }
    @Override
    public boolean isShapeless() {
        return true;
    }
    @Override
    public int getWidth() {
        return this.width;
    }
    @Override
    public int getHeight() {
        return this.height;
    }
}