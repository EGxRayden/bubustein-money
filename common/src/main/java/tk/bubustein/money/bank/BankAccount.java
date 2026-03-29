/*
 * This file is licensed under the GNU Lesser General Public License v3.0,
 * part of Bubustein's Money Mod.
 * Copyright (c) 2022-2025 BUBUSTEIN (GitHub username: BUBUSTEIN13)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package tk.bubustein.money.bank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tk.bubustein.money.MoneyMod;

import java.util.UUID;

public class BankAccount {
    public static final double MAX_BALANCE = 1_000_000_000.0;
    public static final double MIN_BALANCE = -1_000_000_000.0;
    public static final Codec<BankAccount> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("iban").forGetter(BankAccount::getIban),
                    Codec.STRING.fieldOf("ownerUuid").forGetter(acc -> acc.ownerUuid.toString()),
                    Codec.STRING.fieldOf("ownerName").forGetter(BankAccount::getOwnerName),
                    Codec.DOUBLE.fieldOf("balance").forGetter(BankAccount::getBalance),
                    Codec.STRING.fieldOf("currency").forGetter(BankAccount::getCurrency),
                    Codec.STRING.fieldOf("cardTier").forGetter(BankAccount::getCardTier),
                    Codec.STRING.fieldOf("kind").forGetter(acc -> acc.kind.name()),
                    Codec.BOOL.fieldOf("active").forGetter(BankAccount::isActive),
                    Codec.STRING.fieldOf("bankPrefix").forGetter(BankAccount::getBankPrefix),
                    Codec.LONG.optionalFieldOf("lastInterestTimestamp", 0L)
                            .forGetter(BankAccount::getLastInterestTimestamp),
                    Codec.LONG.optionalFieldOf("lastInterestGameTick", 0L).forGetter(BankAccount::getLastInterestGameTick)
            ).apply(instance, (iban, ownerUuidStr, ownerName, balance, currency,
                               cardTier, kindStr, active, bankPrefix, lastInterestTimestamp, lastInterestGameTick) -> {
                UUID ownerUuid = UUID.fromString(ownerUuidStr);
                AccountKind kind = AccountKind.valueOf(kindStr);
                return new BankAccount(iban, ownerUuid, ownerName, balance,
                        currency, cardTier, kind, active, bankPrefix, lastInterestTimestamp, lastInterestGameTick);
            })
    );
    private final String iban;
    private final UUID ownerUuid;
    private final String ownerName;
    private double balance;
    private String currency;
    private String cardTier;
    private final AccountKind kind;
    private boolean active;
    private final String bankPrefix;
    private long lastInterestTimestamp;
    private long lastInterestGameTick=0L;

    public long getLastInterestGameTick() { return lastInterestGameTick; }
    public void setLastInterestGameTick(long tick) { this.lastInterestGameTick = tick; }

    public BankAccount(String iban, UUID ownerUuid, String ownerName, double balance, String currency,
                       String cardTier, AccountKind kind, boolean active, String bankPrefix, long lastInterestTimestamp, long lastInterestGameTick) {
        this.iban = iban;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.balance = balance;
        this.currency = currency;
        this.cardTier = cardTier;
        this.kind = kind;
        this.active = active;
        this.bankPrefix = bankPrefix;
        this.lastInterestTimestamp = lastInterestTimestamp;
        this.lastInterestGameTick = lastInterestGameTick;
    }

    public String getIban() {
        return iban;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public double getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCardTier() {
        return cardTier;
    }

    public CardTier getDebitCardTier() {
        if (this.kind != AccountKind.DEBIT) {
            throw new IllegalStateException("Cannot get CardTier for non-DEBIT account: " + this.iban);
        }
        try {
            return CardTier.valueOf(this.cardTier);
        } catch (IllegalArgumentException e) {
            MoneyMod.LOGGER.warn("[{}] Invalid CardTier '{}' for DEBIT account {}, defaulting to CLASSIC",
                    MoneyMod.MOD_ID, this.cardTier, this.iban);
            return CardTier.CLASSIC;
        }
    }

    public CreditCardTier getCreditCardTier() {
        if (this.kind != AccountKind.CREDIT) {
            throw new IllegalStateException("Cannot get CreditCardTier for non-CREDIT account: " + this.iban);
        }
        try {
            return CreditCardTier.valueOf(this.cardTier);
        } catch (IllegalArgumentException e) {
            MoneyMod.LOGGER.warn("[{}] Invalid CreditCardTier '{}' for CREDIT account {}, defaulting to CLASSIC",
                    MoneyMod.MOD_ID, this.cardTier, this.iban);
            return CreditCardTier.CLASSIC;
        }
    }

    public AccountKind getKind() {
        return kind;
    }

    public boolean isActive() {
        return active;
    }
    public long getLastInterestTimestamp() {
        return lastInterestTimestamp;
    }

    public void setLastInterestTimestamp(long lastInterestTimestamp) {
        this.lastInterestTimestamp = lastInterestTimestamp;
    }
    public double applyInterestForPeriods(int periods) {
        if (periods <= 0) return 0.0;
        if (this.kind != AccountKind.CREDIT || !hasDebt()) return 0.0;

        double total = 0.0;
        for (int i = 0; i < periods; i++) {
            total += applyInterest();
        }
        return total;
    }
    public String getBankPrefix() {
        return bankPrefix;
    }

    public void setBalance(double balance) {
        if (balance > MAX_BALANCE) {
            MoneyMod.LOGGER.warn("[{}] Attempted to set balance above maximum for {}: {}",
                    MoneyMod.MOD_ID, this.iban, balance);
            this.balance = MAX_BALANCE;
        } else if (balance < MIN_BALANCE) {
            MoneyMod.LOGGER.warn("[{}] Attempted to set balance below minimum for {}: {}",
                    MoneyMod.MOD_ID, this.iban, balance);
            this.balance = MIN_BALANCE;
        } else {
            this.balance = balance;
        }
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCardTier(String cardTier) {
        this.cardTier = cardTier;
    }

    public void setCardTier(CardTier tier) {
        if (this.kind != AccountKind.DEBIT) {
            MoneyMod.LOGGER.warn("[{}] Attempting to set CardTier on non-DEBIT account {}",
                    MoneyMod.MOD_ID, this.iban);
        }
        this.cardTier = tier.name();
    }

    public void setCardTier(CreditCardTier tier) {
        if (this.kind != AccountKind.CREDIT) {
            MoneyMod.LOGGER.warn("[{}] Attempting to set CreditCardTier on non-CREDIT account {}",
                    MoneyMod.MOD_ID, this.iban);
        }
        this.cardTier = tier.name();
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive");
        if (amount > MAX_BALANCE) throw new IllegalArgumentException("Deposit amount exceeds maximum allowed");

        double newBalance = this.balance + amount;
        if (newBalance > MAX_BALANCE) throw new IllegalArgumentException("Deposit would exceed maximum balance");
        this.balance = newBalance;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (amount > MAX_BALANCE) {
            return false;
        }

        if (this.kind == AccountKind.CREDIT) {
            CreditCardTier tier = getCreditCardTier();
            double newBalance = this.balance - amount;
            // Credit cards: balance can be positive (overpay), but cannot go below -creditLimit
            if (newBalance < -tier.getCreditLimit()) {
                MoneyMod.LOGGER.warn("[{}] Credit card {} withdrawal of {} would exceed limit of {}",
                        MoneyMod.MOD_ID, this.iban, amount, tier.getCreditLimit());
                return false;
            }
            this.balance = newBalance;
            return true;
        } else {
            // DEBIT and SAVINGS: cannot go below 0
            if (this.balance < amount) {
                return false;
            }
            this.balance -= amount;
            return true;
        }
    }

    public double getDebt() {
        if (this.kind == AccountKind.CREDIT && this.balance < 0) {
            return Math.abs(this.balance);
        }
        return 0.0;
    }

    public double getAvailableCredit() {
        if (this.kind != AccountKind.CREDIT) {
            return 0.0;
        }
        CreditCardTier tier = getCreditCardTier();
        // Available = creditLimit + balance (if positive, you have more room; if negative, less)
        return tier.getCreditLimit() + this.balance;
    }

    public double applyInterest() {
        if (this.kind != AccountKind.CREDIT || this.balance >= 0) {
            return 0.0;
        }

        CreditCardTier tier = getCreditCardTier();
        double debt = Math.abs(this.balance);
        double interest = tier.calculateInterest(debt);

        this.balance -= interest;

        MoneyMod.LOGGER.info("[{}] Applied {}% interest to credit account {}: {} {} interest added",
                MoneyMod.MOD_ID, tier.getInterestRatePercent(), this.iban, interest, this.currency);

        return interest;
    }

    public boolean isBalanceZero() {
        return Math.abs(this.balance) < 0.001; // Toleranță pentru erori de floating point
    }

    public boolean hasPositiveBalance() {
        return this.balance > 0.001;
    }

    public boolean hasDebt() {
        return this.balance < -0.001;
    }

    public boolean canBeDeleted() {
        return !this.active && isBalanceZero();
    }

    public boolean canWithdraw(double amount) {
        if (amount <= 0) {
            return false;
        }

        if (this.kind == AccountKind.DEBIT || this.kind == AccountKind.SAVINGS) {
            return this.balance >= amount;
        } else if (this.kind == AccountKind.CREDIT) {
            CreditCardTier tier = getCreditCardTier();
            double newBalance = this.balance - amount;
            // Cannot go below -creditLimit
            return newBalance >= -tier.getCreditLimit();
        }

        return false;
    }

    public double getCreditUtilization() {
        if (this.kind != AccountKind.CREDIT) {
            return 0.0;
        }

        CreditCardTier tier = getCreditCardTier();
        if (tier.getCreditLimit() <= 0) {
            return 0.0;
        }

        // Only count negative balance (debt) for utilization
        double debt = Math.max(0.0, -this.balance);
        return Math.min(1.0, debt / tier.getCreditLimit());
    }

    public String getStatusDescription() {
        if (!this.active) {
            return "Inactive";
        }

        if (this.kind == AccountKind.CREDIT) {
            if (hasDebt()) {
                double utilization = getCreditUtilization() * 100;
                return String.format("In Debt (%.1f%% utilization)", utilization);
            }
            return "Active (No Debt)";
        }

        if (isBalanceZero()) {
            return "Active (Empty)";
        }

        return "Active";
    }
    @Override
    public String toString() {
        return "BankAccount{" +
                "iban='" + iban + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                ", cardTier='" + cardTier + '\'' +
                ", kind=" + kind +
                ", active=" + active +
                ", bankPrefix='" + bankPrefix + '\'' +
                '}';
    }
}