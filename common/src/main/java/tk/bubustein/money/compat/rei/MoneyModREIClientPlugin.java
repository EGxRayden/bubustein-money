package tk.bubustein.money.compat.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.block.ModBlocks;
import static tk.bubustein.money.MoneyMod.LOGGER;

@Environment(EnvType.CLIENT)
public class MoneyModREIClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new BankMachineCategory());
        registry.addWorkstations(BankMachineCategory.BANK_MACHINE_CATEGORY, EntryStacks.of(ModBlocks.BANK_MACHINE.get()));
        LOGGER.info("[" + MoneyMod.MOD_ID + "] Bank Machine Category has been registered successfully.");
    }
}