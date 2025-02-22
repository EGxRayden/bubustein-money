package tk.bubustein.money.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.command.ModCommands;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

public class CardItem extends Item {
    private static final double GLOW_THRESHOLD_EUR = 20000.0;
    public CardItem(Properties properties) {
        super(properties);
    }
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putDouble("money", 0.0);
        tag.putString("currency", MoneyMod.getDefaultCurrency());
    }
    @Override
    public boolean isFoil(ItemStack stack) {
        double money = getMoney(stack);
        String currency = getCurrency(stack);
        if (ModItems.EXCHANGE_RATES.isEmpty()) {
            return money >= GLOW_THRESHOLD_EUR;
        }
        if (!currency.equals("EUR")) {
            if (ModItems.EXCHANGE_RATES.containsKey(currency)) {
                money = ModCommands.convertCurrency(money, currency, "EUR");
            }
        }
        return money >= GLOW_THRESHOLD_EUR;
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!stack.hasTag() || !Objects.requireNonNull(stack.getTag()).contains("currency")) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString("currency", MoneyMod.getDefaultCurrency());
            if (!tag.contains("money")) {
                tag.putDouble("money", 0.0);
            }
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }
    public void addMoney(ItemStack stack, double amount) {
        double existingMoney = getMoney(stack);
        setMoney(stack, existingMoney + amount);
    }
    public double getMoney(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            return tag.getDouble("money");
        }
        return 0.0;
    }
    public void setMoney(ItemStack stack, double amount) {
        if (amount < 0) amount = 0;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putDouble("money", amount);
    }
    public void convertMoney(ItemStack stack, String fromCurrency, String toCurrency) {
        double currentAmount = getMoney(stack);
        double convertedAmount = ModCommands.convertCurrency(currentAmount, fromCurrency, toCurrency);
        setMoney(stack, convertedAmount);
    }
    public String getCurrency(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("currency")) {
            return tag.getString("currency");
        }
        return MoneyMod.getDefaultCurrency();
    }
    public void setCurrency(ItemStack stack, String currency) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("currency", currency);
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        double money = getMoney(stack);
        String currency = getCurrency(stack);
        DecimalFormat df = new DecimalFormat("#.##");
        String formattedMoney = df.format(((int)(money * 100)) / 100.0);
        tooltip.add(Component.literal("Balance: " + formattedMoney + " " + currency).withStyle(style -> style.withColor(TextColor.fromRgb(0xFFD700))));
        if (stack.getItem() == ModItems.VisaClassic.get()) {
            tooltip.add(Component.literal("Withdrawal Fee: 3%").withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
        } else if (stack.getItem() == ModItems.VisaGold.get()) {
            tooltip.add(Component.literal("Withdrawal Fee: 2%").withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
        } else if (stack.getItem() == ModItems.VisaSteel.get()) {
            tooltip.add(Component.literal("Withdrawal Fee: 0.5%").withStyle(style -> style.withColor(TextColor.fromRgb(0xFF0000))));
        }
    }
}