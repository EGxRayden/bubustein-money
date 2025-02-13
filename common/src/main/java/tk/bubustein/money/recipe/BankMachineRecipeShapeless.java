package tk.bubustein.money.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tk.bubustein.money.block.ModBlocks;

import java.util.List;

public class BankMachineRecipeShapeless implements BankMachineRecipe {
    final String group;
    final ItemStack result;
    final List<Ingredient> ingredients;
    @Nullable
    private PlacementInfo placementInfo;
    public BankMachineRecipeShapeless(String string, ItemStack itemStack, List<Ingredient> list) {
        this.group = string;
        this.result = itemStack;
        this.ingredients = list;
    }
    @Override
    public boolean isShapeless() {
        return true;
    }
    public RecipeSerializer<BankMachineRecipeShapeless> getSerializer() {
        return ModRecipes.BANK_MACHINE_SHAPELESS.get();
    }
    public String group() {
        return this.group;
    }
    public @NotNull PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.ingredients);
        }
        return this.placementInfo;
    }
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (craftingInput.ingredientCount() != this.ingredients.size()) {
            return false;
        } else {
            return craftingInput.size() == 1 && this.ingredients.size() == 1 ? this.ingredients.getFirst().test(craftingInput.getItem(0)) : craftingInput.stackedContents().canCraft(this, null);
        }
    }
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        return this.result.copy();
    }
    public List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(this.ingredients.stream().map(Ingredient::display).toList(), new SlotDisplay.ItemStackSlotDisplay(this.result), new SlotDisplay.ItemSlotDisplay(Item.byBlock(ModBlocks.BANK_MACHINE.get()))));
    }
    public static class Serializer implements RecipeSerializer<BankMachineRecipeShapeless> {
        public static final Serializer INSTANCE = new Serializer();
        private static final MapCodec<BankMachineRecipeShapeless> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter((shapelessRecipe) -> shapelessRecipe.group), ItemStack.STRICT_CODEC.fieldOf("result").forGetter((shapelessRecipe) -> shapelessRecipe.result), Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter((shapelessRecipe) -> shapelessRecipe.ingredients)).apply(instance, BankMachineRecipeShapeless::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, BankMachineRecipeShapeless> STREAM_CODEC;
        public Serializer() {
        }
        public MapCodec<BankMachineRecipeShapeless> codec() {
            return CODEC;
        }
        public StreamCodec<RegistryFriendlyByteBuf, BankMachineRecipeShapeless> streamCodec() {
            return STREAM_CODEC;
        }
        static {
            STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, (shapelessRecipe) -> shapelessRecipe.group, ItemStack.STREAM_CODEC, (shapelessRecipe) -> shapelessRecipe.result, Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), (shapelessRecipe) -> shapelessRecipe.ingredients, BankMachineRecipeShapeless::new);
        }
    }
}