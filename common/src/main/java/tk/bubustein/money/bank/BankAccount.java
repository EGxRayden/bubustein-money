package tk.bubustein.money.bank;

import java.util.UUID;

public class BankAccount {
    private UUID ownerUuid;
    private String iban;
    private String currency;
    private AccountKind kind;
    private double balance;
    private String bankPrefix;
    private int accountId;
    private boolean active = true;
    private String cardTier;


    public String getCardTier() { return cardTier; }
    public void setCardTier(String cardTier) { this.cardTier = cardTier; }

    public UUID getOwnerUuid() { return ownerUuid; }
    public void setOwnerUuid(UUID ownerUuid) { this.ownerUuid = ownerUuid; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public AccountKind getKind() { return kind; }
    public void setKind(AccountKind kind) { this.kind = kind; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getBankPrefix() { return bankPrefix; }
    public void setBankPrefix(String bankPrefix) { this.bankPrefix = bankPrefix; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}