package tk.bubustein.money.recipe;

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

public record BankMachineRecipeShapelessDisplay(List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {
    public static final MapCodec<BankMachineRecipeShapelessDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(SlotDisplay.CODEC.listOf().fieldOf("ingredients").forGetter(BankMachineRecipeShapelessDisplay::ingredients), SlotDisplay.CODEC.fieldOf("result").forGetter(BankMachineRecipeShapelessDisplay::result), SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(BankMachineRecipeShapelessDisplay::craftingStation)).apply(instance, BankMachineRecipeShapelessDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BankMachineRecipeShapelessDisplay> STREAM_CODEC;
    public static final RecipeDisplay.Type<BankMachineRecipeShapelessDisplay> TYPE;

    public BankMachineRecipeShapelessDisplay(List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation) {
        this.ingredients = ingredients;
        this.result = result;
        this.craftingStation = craftingStation;
    }
    public RecipeDisplay.Type<BankMachineRecipeShapelessDisplay> type() {
        return TYPE;
    }
    public boolean isEnabled(FeatureFlagSet featureFlagSet) {
        return this.ingredients.stream().allMatch((slotDisplay) -> slotDisplay.isEnabled(featureFlagSet)) && RecipeDisplay.super.isEnabled(featureFlagSet);
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
        STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), BankMachineRecipeShapelessDisplay::ingredients, SlotDisplay.STREAM_CODEC, BankMachineRecipeShapelessDisplay::result, SlotDisplay.STREAM_CODEC, BankMachineRecipeShapelessDisplay::craftingStation, BankMachineRecipeShapelessDisplay::new);
        TYPE = new RecipeDisplay.Type<>(MAP_CODEC, STREAM_CODEC);
    }
}