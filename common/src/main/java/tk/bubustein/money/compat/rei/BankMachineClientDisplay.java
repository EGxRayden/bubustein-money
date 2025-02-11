package tk.bubustein.money.compat.rei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.displays.crafting.CraftingDisplay;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import tk.bubustein.money.recipe.BankMachineRecipeShapedDisplay;
import tk.bubustein.money.recipe.BankMachineRecipeShapelessDisplay;

public abstract class BankMachineClientDisplay extends BasicDisplay implements CraftingDisplay {
    private final Optional<RecipeDisplayId> id;
    public BankMachineClientDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<RecipeDisplayId> id) {
        super(inputs, outputs, Optional.empty());
        this.id = id;
    }
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BankMachineCategory.BANK_MACHINE_CATEGORY;
    }
    public Optional<RecipeDisplayId> recipeDisplayId() {
        return this.id;
    }
    public static class Shaped extends BankMachineClientDisplay {
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
            SERIALIZER = DisplaySerializer.of(RecordCodecBuilder.mapCodec((instance) -> instance.group(EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(BasicDisplay::getInputEntries), EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(BasicDisplay::getOutputEntries), Codec.INT.xmap(RecipeDisplayId::new, RecipeDisplayId::index).optionalFieldOf("id").forGetter(BankMachineClientDisplay::recipeDisplayId), Codec.INT.fieldOf("width").forGetter(Shaped::getWidth), Codec.INT.fieldOf("height").forGetter(Shaped::getHeight)).apply(instance, Shaped::new)), StreamCodec.composite(EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getInputEntries, EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getOutputEntries, ByteBufCodecs.optional(ByteBufCodecs.INT.map(RecipeDisplayId::new, RecipeDisplayId::index)), BankMachineClientDisplay::recipeDisplayId, ByteBufCodecs.INT, Shaped::getWidth, ByteBufCodecs.INT, Shaped::getHeight, Shaped::new), false);
        }
    }

    public static class Shapeless extends BankMachineClientDisplay {
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
            SERIALIZER = DisplaySerializer.of(RecordCodecBuilder.mapCodec((instance) -> instance.group(EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(BasicDisplay::getInputEntries), EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(BasicDisplay::getOutputEntries), Codec.INT.xmap(RecipeDisplayId::new, RecipeDisplayId::index).optionalFieldOf("id").forGetter(BankMachineClientDisplay::recipeDisplayId)).apply(instance, Shapeless::new)), StreamCodec.composite(EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getInputEntries, EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), BasicDisplay::getOutputEntries, ByteBufCodecs.optional(ByteBufCodecs.INT.map(RecipeDisplayId::new, RecipeDisplayId::index)), BankMachineClientDisplay::recipeDisplayId, Shapeless::new), false);
        }
    }
}
