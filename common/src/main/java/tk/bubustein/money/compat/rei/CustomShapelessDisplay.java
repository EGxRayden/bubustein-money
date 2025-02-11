package tk.bubustein.money.compat.rei;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class CustomShapelessDisplay extends BankMachineDisplay {
    public static final DisplaySerializer<CustomShapelessDisplay> SERIALIZER;
    public CustomShapelessDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location) {
        super(input, output, location);
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
    public boolean isShapeless() {
        return true;
    }
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
    static {
        SERIALIZER = DisplaySerializer.of(RecordCodecBuilder.mapCodec((instance) -> instance.group(EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(BasicDisplay::getInputEntries), EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(BasicDisplay::getOutputEntries), ResourceLocation.CODEC.optionalFieldOf("location").forGetter(BasicDisplay::getDisplayLocation)).apply(instance, CustomShapelessDisplay::new)), StreamCodec.composite(EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getInputEntries, EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getOutputEntries, ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), BasicDisplay::getDisplayLocation, CustomShapelessDisplay::new));
    }
}