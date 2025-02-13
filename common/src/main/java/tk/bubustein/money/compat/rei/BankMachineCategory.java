package tk.bubustein.money.compat.rei;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.crafting.CraftingDisplay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NonBlocking;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.block.ModBlocks;
import tk.bubustein.money.block.custom.BankMachine;

@Environment(EnvType.CLIENT)
public class BankMachineCategory implements DisplayCategory<CraftingDisplay> {
    public BankMachineCategory(){}
    public static final CategoryIdentifier<CraftingDisplay> BANK_MACHINE_CATEGORY = CategoryIdentifier.of(MoneyMod.MOD_ID, "bank_machine");
    @Override
    public CategoryIdentifier<? extends CraftingDisplay> getCategoryIdentifier() {
        return BANK_MACHINE_CATEGORY;
    }
    @Override
    public Component getTitle() {
        return BankMachine.TITLE;
    }
    @Override
    @NonBlocking
    public Renderer getIcon() {
        return EntryStacks.of(ModBlocks.BANK_MACHINE.get());
    }
    @Override
    public int getDisplayHeight() {
        return 90;
    }
}