package tk.bubustein.money.bank;

import net.minecraft.ChatFormatting;

public enum CardTier {
    RUSTY(0.10, ChatFormatting.RED),
    CLASSIC(0.03, ChatFormatting.WHITE),
    GOLD(0.02, ChatFormatting.GOLD),
    STEEL(0.01, ChatFormatting.DARK_AQUA),
    SUPREME(0.00, ChatFormatting.LIGHT_PURPLE);

    private final double withdrawFee;
    private final ChatFormatting color;

    CardTier(double withdrawFee, ChatFormatting color) {
        this.withdrawFee = withdrawFee;
        this.color = color;
    }
    public double getWithdrawFee() {
        return withdrawFee;
    }
    public String getWithdrawFeePercent() {
        return String.format("%.0f%%", withdrawFee * 100);
    }
    public ChatFormatting getColor() {
        return color;
    }
    public double applyWithdrawFee(double amount) {
        return amount * (1.0 - withdrawFee);
    }
}