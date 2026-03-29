package tk.bubustein.money.bank;

import net.minecraft.ChatFormatting;

public enum CreditCardTier {
    /*
     * Interest rate design:
     * 7 real-life days ≈ 1 Minecraft "year".
     * Interest is applied once per period (7 days).
     *
     * CLASSIC:  15% per period — harsh, discourages casual borrowing
     * GOLD:     10% per period — moderate, for established players
     * PLATINUM:  5% per period — low, reward for wealthy players
     *
     * Credit limits are in EUR (converted to account currency at withdrawal time).
     */
    CLASSIC(5_000.0, 0.15, ChatFormatting.WHITE),
    GOLD(20_000.0, 0.10, ChatFormatting.GOLD),
    PLATINUM(100_000.0, 0.05, ChatFormatting.AQUA);

    private final double creditLimit;
    private final double interestRate;
    private final ChatFormatting color;

    CreditCardTier(double creditLimit, double interestRate, ChatFormatting color) {
        this.creditLimit = creditLimit;
        this.interestRate = interestRate;
        this.color = color;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public String getInterestRatePercent() {
        return String.format("%.0f%%", interestRate * 100);
    }

    public ChatFormatting getColor() {
        return color;
    }

    public boolean exceedsLimit(double amount) {
        return Math.abs(amount) > creditLimit;
    }

    public double calculateInterest(double debt) {
        return debt * interestRate;
    }
}
