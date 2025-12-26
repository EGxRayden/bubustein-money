package tk.bubustein.money.bank;

import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.item.ModItems;

import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    private transient ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final double EPSILON = 0.01;
    private static final double MAX_BALANCE = 1_000_000_000_000.0;

    public BankAccount() {
        if (lock == null) {
            lock = new ReentrantReadWriteLock();
        }
    }

    public String getCardTier() {
        lock.readLock().lock();
        try {
            return cardTier;
        } finally {
            lock.readLock().unlock();
        }
    }
    public void setCardTier(String cardTier) {
        lock.writeLock().lock();
        try {
            this.cardTier = cardTier;
        } finally {
            lock.writeLock().unlock();
        }
    }
    public UUID getOwnerUuid() {
        lock.readLock().lock();
        try {
            return ownerUuid;
        } finally {
            lock.readLock().unlock();
        }
    }
    public void setOwnerUuid(UUID ownerUuid) {
        lock.writeLock().lock();
        try {
            this.ownerUuid = ownerUuid;
        } finally {
            lock.writeLock().unlock();
        }
    }
    public String getIban() {
        lock.readLock().lock();
        try {
            return iban;
        } finally {
            lock.readLock().unlock();
        }
    }
    public void setIban(String iban) {
        lock.writeLock().lock();
        try {
            this.iban = iban;
        } finally {
            lock.writeLock().unlock();
        }
    }
    public String getCurrency() {
        lock.readLock().lock();
        try {
            return currency;
        } finally {
            lock.readLock().unlock();
        }
    }
    public void setCurrency(String currency) {
        lock.writeLock().lock();
        try {
            if (currency == null || currency.trim().isEmpty()) {
                MoneyMod.LOGGER.warn("[{}] Attempted to set null/empty currency, using EUR", MoneyMod.MOD_ID);
                this.currency = "EUR";
                return;
            }
            if (!ModItems.EXCHANGE_RATES.containsKey(currency.toUpperCase())) {
                MoneyMod.LOGGER.warn("[{}] Invalid currency '{}', using EUR", MoneyMod.MOD_ID, currency);
                this.currency = "EUR";
                return;
            }
            this.currency = currency.toUpperCase();
        } finally {
            lock.writeLock().unlock();
        }
    }
    public AccountKind getKind() {
        lock.readLock().lock();
        try {
            return kind;
        } finally {
            lock.readLock().unlock();
        }
    }
    public void setKind(AccountKind kind) {
        lock.writeLock().lock();
        try {
            this.kind = kind;
        } finally {
            lock.writeLock().unlock();
        }
    }
    public double getBalance() {
        lock.readLock().lock();
        try {
            return balance;
        } finally {
            lock.readLock().unlock();
        }
    }
    public void setBalance(double balance) {
        lock.writeLock().lock();
        try {
            if (!Double.isFinite(balance)) {
                MoneyMod.LOGGER.error("[{}] Attempted to set invalid balance: {}", MoneyMod.MOD_ID, balance);
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
        } finally {
            lock.writeLock().unlock();
        }
    }
    public boolean deposit(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            MoneyMod.LOGGER.warn("[{}] Invalid deposit amount: {}", MoneyMod.MOD_ID, amount);
            return false;
        }
        lock.writeLock().lock();
        try {
            double newBalance = this.balance + amount;
            if (newBalance > MAX_BALANCE) {
                MoneyMod.LOGGER.warn("[{}] Deposit would exceed maximum balance", MoneyMod.MOD_ID);
                return false;
            }
            this.balance = newBalance;
            MoneyMod.LOGGER.info("[{}] Deposited {} {} to account {}, new balance: {}",
                    MoneyMod.MOD_ID, amount, currency, iban, this.balance);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    public boolean withdraw(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            MoneyMod.LOGGER.warn("[{}] Invalid withdraw amount: {}", MoneyMod.MOD_ID, amount);
            return false;
        }
        lock.writeLock().lock();
        try {
            if (this.balance < amount - EPSILON) {
                MoneyMod.LOGGER.info("[{}] Insufficient funds for withdrawal: has {}, needs {}",
                        MoneyMod.MOD_ID, this.balance, amount);
                return false;
            }
            this.balance = Math.max(0, this.balance - amount);
            MoneyMod.LOGGER.info("[{}] Withdrew {} {} from account {}, new balance: {}",
                    MoneyMod.MOD_ID, amount, currency, iban, this.balance);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    public boolean isBalanceZero() {
        lock.readLock().lock();
        try {
            return Math.abs(this.balance) < EPSILON;
        } finally {
            lock.readLock().unlock();
        }
    }
    public String getBankPrefix() {
        lock.readLock().lock();
        try {
            return bankPrefix;
        } finally {
            lock.readLock().unlock();
        }
    }
    public void setBankPrefix(String bankPrefix) {
        lock.writeLock().lock();
        try {
            this.bankPrefix = bankPrefix;
        } finally {
            lock.writeLock().unlock();
        }
    }
    public int getAccountId() {
        lock.readLock().lock();
        try {
            return accountId;
        } finally {
            lock.readLock().unlock();
        }
    }
    public void setAccountId(int accountId) {
        lock.writeLock().lock();
        try {
            this.accountId = accountId;
        } finally {
            lock.writeLock().unlock();
        }
    }
    public boolean isActive() {
        lock.readLock().lock();
        try {
            return active;
        } finally {
            lock.readLock().unlock();
        }
    }
    public void setActive(boolean active) {
        lock.writeLock().lock();
        try {
            this.active = active;
        } finally {
            lock.writeLock().unlock();
        }
    }
}