package tk.bubustein.money.compat.jei;
/*
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.block.ModBlocks;
import tk.bubustein.money.block.custom.BankMachine;
import tk.bubustein.money.recipe.BankMachineRecipe;
import tk.bubustein.money.recipe.BankMachineRecipeShaped;
import tk.bubustein.money.recipe.BankMachineRecipeShapeless;
import java.util.List;

public class BankMachineCategory implements IRecipeCategory<BankMachineRecipe> {
    public static final RecipeType<BankMachineRecipe> RECIPE_TYPE = RecipeType.create(MoneyMod.MOD_ID, "bank_machine", BankMachineRecipe.class);
    private final IDrawable icon;
    public BankMachineCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.BANK_MACHINE.get()));
    }
    @Override
    public @NotNull RecipeType<BankMachineRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }
    @Override
    public @NotNull Component getTitle() {
        return BankMachine.TITLE;
    }
    @Override
    public void draw(BankMachineRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "textures/container/bank_machine.png"), 0, 0, 29, 16, getWidth(), getHeight(), 256, 256);
    }
    @Override
    public int getWidth() {
        return 116;
    }
    @Override
    public int getHeight() {
        return 54;
    }
    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BankMachineRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.getIngredients();
        if (recipe instanceof BankMachineRecipeShaped shapedRecipe) {
            int width = shapedRecipe.getWidth();
            int height = shapedRecipe.getHeight();
            int index = 0;
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    if (index < ingredients.size()) {
                        if (!ingredients.get(index).isEmpty()) {
                            builder.addSlot(RecipeIngredientRole.INPUT, 1 + col * 18, 1 + row * 18)
                                    .addIngredients(ingredients.get(index));
                        } else {
                            builder.addSlot(RecipeIngredientRole.INPUT, 1 + col * 18, 1 + row * 18)
                                    .addItemStack(ItemStack.EMPTY);
                        }
                    } else {
                        builder.addSlot(RecipeIngredientRole.INPUT, 1 + col * 18, 1 + row * 18)
                                .addItemStack(ItemStack.EMPTY);
                    }
                    index++;
                }
            }
        } else if (recipe instanceof BankMachineRecipeShapeless) {
            if (!ingredients.isEmpty() && !ingredients.getFirst().isEmpty()) {
                builder.addSlot(RecipeIngredientRole.INPUT, 1 + 18, 1 + 18)
                        .addIngredients(ingredients.getFirst());
            }
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 18)
                .addItemStack(recipe.getResultItem(null));
    }
}*/