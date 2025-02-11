package tk.bubustein.money.compat.rei;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import tk.bubustein.money.recipe.BankMachineRecipeShapeless;

public class BankMachineShapelessDisplay extends BankMachineDisplay {
    public static final DisplaySerializer<BankMachineDisplay> SERIALIZER;
    public BankMachineShapelessDisplay(RecipeHolder<BankMachineRecipeShapeless> recipe) {
        super(CollectionUtils.map(recipe.value().placementInfo().ingredients(), EntryIngredients::ofIngredient), List.of(EntryIngredients.of((ItemLike) recipe.value())), Optional.of(recipe.id().location()));
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