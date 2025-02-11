package tk.bubustein.money.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import tk.bubustein.money.MoneyExpectPlatform;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.block.custom.ATM;
import tk.bubustein.money.block.custom.BankMachine;
import java.util.function.Supplier;
@SuppressWarnings("UnstableApiUsage")
public class ModBlocks {
    public static void init(){}
    public static final Supplier<Block> ATM = registerBlock("atm",
            () -> new ATM(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(6f).noOcclusion().requiresCorrectToolForDrops().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "atm")))));
    public static final Supplier<Block> BANK_MACHINE = registerBlock("bank_machine", BankMachine::new);
    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        Supplier<T> toReturn = MoneyExpectPlatform.registerBlock(name, block);
        MoneyExpectPlatform.registerItem(name, () -> new BlockItem(toReturn.get(), new Item.Properties().arch$tab(MoneyMod.SPECIAL).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, name)))));
        return toReturn;
    }
}