package tk.bubustein.money.fabric.rei;

import me.shedaniel.rei.api.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;
import tk.bubustein.money.recipe.BankMachineRecipeShaped;
import tk.bubustein.money.recipe.BankMachineRecipeShapeless;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class BankMachineDisplayShapeless implements BankMachineDisplay {
    private final BankMachineRecipeShapeless display;
    private final List<List<EntryStack>> input;
    private final List<EntryStack> output;
    public BankMachineDisplayShapeless(BankMachineRecipeShapeless recipe) {
        this.display = recipe;
        this.input = EntryStack.ofIngredients(recipe.getIngredients());
        this.output = Collections.singletonList(EntryStack.create(recipe.getResultItem()));
    }
    @Override
    public Optional<Recipe<?>> getOptionalRecipe() {
        return Optional.ofNullable(display);
    }
    @Override
    public @NotNull Optional<ResourceLocation> getRecipeLocation() {
        return Optional.ofNullable(display).map(BankMachineRecipeShapeless::getId);
    }
    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        return input;
    }
    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        return Collections.singletonList(output);
    }
    @Override
    public @NotNull List<List<EntryStack>> getRequiredEntries() {
        return input;
    }
}