package tk.bubustein.money.bank;

import com.google.gson.annotations.SerializedName;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.item.ModItems;

import java.util.UUID;

public class BankAccount {
    @SerializedName("owner_uuid")
    private UUID ownerUuid;

    @SerializedName("iban")
    private String iban;

    @SerializedName("currency")
    private String currency;

    @SerializedName("kind")
    private AccountKind kind;

    @SerializedName("balance")
    private volatile double balance;

    @SerializedName("bank_prefix")
    private String bankPrefix;

    @SerializedName("account_id")
    private int accountId;

    @SerializedName("active")
    private volatile boolean active = true;

    @SerializedName("card_tier")
    private String cardTier = "CLASSIC";

    private static final double EPSILON = 0.01;
    private static final double MAX_BALANCE = 1_000_000_000_000.0;

    public BankAccount() {
    }
    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountKind getKind() {
        return kind;
    }

    public double getBalance() {
        return balance;
    }

    public String getBankPrefix() {
        return bankPrefix;
    }

    public int getAccountId() {
        return accountId;
    }

    public boolean isActive() {
        return active;
    }

    public String getCardTier() {
        return cardTier != null ? cardTier : "CLASSIC";
    }

    public boolean isBalanceZero() {
        return Math.abs(this.balance) < EPSILON;
    }
    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public void setCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            MoneyMod.LOGGER.warn("[{}] Attempted to set null/empty currency, using EUR",
                    MoneyMod.MOD_ID);
            this.currency = "EUR";
            return;
        }

        String upperCurrency = currency.toUpperCase();
        if (!ModItems.EXCHANGE_RATES.containsKey(upperCurrency)) {
            MoneyMod.LOGGER.warn("[{}] Invalid currency '{}', using EUR",
                    MoneyMod.MOD_ID, currency);
            this.currency = "EUR";
            return;
        }

        this.currency = upperCurrency;
    }

    public void setKind(AccountKind kind) {
        this.kind = kind;
    }

    public void setBankPrefix(String bankPrefix) {
        this.bankPrefix = bankPrefix;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCardTier(String cardTier) {
        this.cardTier = cardTier;
    }

    public void setBalance(double balance) {
        if (!Double.isFinite(balance)) {
            MoneyMod.LOGGER.error("[{}] Attempted to set invalid balance: {}",
                    MoneyMod.MOD_ID, balance);
            return;
        }

        if (balance < 0) {
            MoneyMod.LOGGER.warn("[{}] Attempted to set negative balance: {}, clamping to 0",
                    MoneyMod.MOD_ID, balance);
            this.balance = 0.0;
            return;
        }

        if (balance > MAX_BALANCE) {
            MoneyMod.LOGGER.warn("[{}] Balance exceeds maximum: {}, clamping to {}",
                    MoneyMod.MOD_ID, balance, MAX_BALANCE);
            this.balance = MAX_BALANCE;
            return;
        }

        this.balance = balance;
    }
    public synchronized boolean deposit(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            MoneyMod.LOGGER.warn("[{}] Invalid deposit amount: {}", MoneyMod.MOD_ID, amount);
            return false;
        }

        double newBalance = this.balance + amount;
        if (newBalance > MAX_BALANCE) {
            MoneyMod.LOGGER.warn("[{}] Deposit would exceed maximum balance", MoneyMod.MOD_ID);
            return false;
        }

        this.balance = newBalance;
        MoneyMod.LOGGER.debug("[{}] Deposited {} {} to account {}, new balance: {}",
                MoneyMod.MOD_ID, amount, currency, iban, this.balance);
        return true;
    }
    public synchronized boolean withdraw(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            MoneyMod.LOGGER.warn("[{}] Invalid withdraw amount: {}", MoneyMod.MOD_ID, amount);
            return false;
        }

        if (this.balance < amount - EPSILON) {
            MoneyMod.LOGGER.debug("[{}] Insufficient funds for withdrawal: has {}, needs {}",
                    MoneyMod.MOD_ID, this.balance, amount);
            return false;
        }

        this.balance = Math.max(0, this.balance - amount);
        MoneyMod.LOGGER.debug("[{}] Withdrew {} {} from account {}, new balance: {}",
                MoneyMod.MOD_ID, amount, currency, iban, this.balance);
        return true;
    }
}
