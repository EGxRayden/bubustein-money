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

import net.minecraft.server.MinecraftServer;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.command.ModCommands;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BankAccountManager {
    private static final BankAccountManager INSTANCE = new BankAccountManager();

    private final ConcurrentHashMap<String, Bank> banks = new ConcurrentHashMap<>();

    /** Bank prefixes that cannot be deleted. */
    private static final java.util.Set<String> PROTECTED_BANKS = java.util.Set.of("BSTN", "HDGR");

    /**
     * Absolute maximum total debt (in EUR equivalent) any player can have across ALL credit accounts.
     * No exceptions, regardless of income.
     */
    public static final double GLOBAL_DEBT_CAP_EUR = 500_000.0;

    /**
     * Multiplier for income-based credit limit.
     * A player can borrow up to INCOME_MULTIPLIER × their total debit/savings balance,
     * but never more than GLOBAL_DEBT_CAP_EUR.
     */
    public static final double INCOME_MULTIPLIER = 2.0;

    /**
     * When a player's total debt exceeds their per-card limit after interest,
     * this fraction of the overage must be forcefully recovered from debit/savings.
     */
    public static final double MIN_RECOVERY_FRACTION = 0.25;

    private BankAccountManager() {
        registerBank("BSTN", "Bubustein Global Bank System");
        registerBank("HDGR", "HDGR American Bank System");
    }

    public static BankAccountManager get() {
        return INSTANCE;
    }

    public void registerBank(String prefix, String name) {
        registerBank(prefix, name, null);
    }

    public void registerBank(String prefix, String name, UUID ownerUuid) {
        if (prefix == null || prefix.length() != 4) {
            throw new IllegalArgumentException("Bank prefix must be exactly 4 characters");
        }
        String upper = prefix.toUpperCase();
        if (!upper.matches("[A-Z]{4}")) {
            throw new IllegalArgumentException("Bank prefix must be 4 uppercase letters: " + prefix);
        }
        Bank bank = new Bank(upper, name);
        if (ownerUuid != null) {
            bank.setOwnerUuid(ownerUuid);
        }
        banks.put(upper, bank);
        MoneyMod.LOGGER.info("[{}] Registered bank: {} ({}) [owner: {}]", MoneyMod.MOD_ID, name, upper, ownerUuid);
    }

    public boolean bankExists(String prefix) {
        return banks.containsKey(prefix.toUpperCase());
    }

    public int nextAccountId(MinecraftServer server) {
        BankAccountSavedData data = BankAccountSavedData.get(server);
        return data.nextAccountId();
    }

    public BankAccount createAccount(MinecraftServer server, UUID owner, AccountKind kind,
                                     String currency, String bankPrefix, int accountId, String iban) {
        BankAccountSavedData data = BankAccountSavedData.get(server);
        String ownerName = server.getPlayerList().getPlayer(owner) != null
                ? Objects.requireNonNull(server.getPlayerList().getPlayer(owner)).getGameProfile().getName()
                : "Unknown";

        String initialTier;
        if (kind == AccountKind.CREDIT) {
            initialTier = CreditCardTier.CLASSIC.name();
        } else {
            initialTier = CardTier.CLASSIC.name();
        }

        return data.createAccount(
                owner,
                ownerName,
                kind,
                currency,
                bankPrefix,
                accountId,
                iban,
                initialTier
        );
    }

    public Optional<BankAccount> getByIban(MinecraftServer server, String iban) {
        BankAccountSavedData data = BankAccountSavedData.get(server);
        return data.getByIban(iban);
    }

    public Map<String, BankAccount> getAccountsForPlayer(MinecraftServer server, UUID playerUuid) {
        BankAccountSavedData data = BankAccountSavedData.get(server);
        return data.getAccountsForPlayer(playerUuid);
    }

    public void deleteAccount(MinecraftServer server, String iban) {
        BankAccountSavedData data = BankAccountSavedData.get(server);
        data.deleteAccount(iban);
    }

    public Map<String, Bank> getBanks() {
        return Collections.unmodifiableMap(banks);
    }

    public Optional<Bank> getBank(String prefix) {
        return Optional.ofNullable(banks.get(prefix.toUpperCase()));
    }

    public boolean isProtectedBank(String prefix) {
        return PROTECTED_BANKS.contains(prefix.toUpperCase());
    }

    /**
     * Returns a random protected bank prefix (BSTN or HDGR).
     */
    public String getRandomProtectedPrefix() {
        java.util.List<String> list = new java.util.ArrayList<>(PROTECTED_BANKS);
        return list.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(list.size()));
    }

    /**
     * Removes a bank. Returns true if removed, false if not found.
     * Does NOT check protection — caller must check isProtectedBank() first.
     */
    public boolean removeBank(String prefix) {
        String upper = prefix.toUpperCase();
        Bank removed = banks.remove(upper);
        if (removed != null) {
            MoneyMod.LOGGER.info("[{}] Bank removed: {} ({})", MoneyMod.MOD_ID, removed.getName(), upper);
            return true;
        }
        return false;
    }

    // ─── Credit eligibility logic ───────────────────────────────────────

    /**
     * Calculates the total debt (in EUR) across all credit accounts for a player.
     */
    public double getTotalDebtEur(MinecraftServer server, UUID playerUuid) {
        Map<String, BankAccount> accounts = getAccountsForPlayer(server, playerUuid);
        double totalDebtEur = 0.0;
        for (BankAccount acc : accounts.values()) {
            if (acc.getKind() == AccountKind.CREDIT && acc.getBalance() < 0) {
                try {
                    totalDebtEur += ModCommands.convertCurrency(Math.abs(acc.getBalance()), acc.getCurrency(), "EUR");
                } catch (IllegalArgumentException e) {
                    MoneyMod.LOGGER.warn("[{}] Could not convert debt {} {} to EUR",
                            MoneyMod.MOD_ID, acc.getBalance(), acc.getCurrency());
                }
            }
        }
        return totalDebtEur;
    }

    /**
     * Calculates the total debit+savings balance (in EUR) across all accounts for a player.
     */
    public double getTotalDebitBalanceEur(MinecraftServer server, UUID playerUuid) {
        Map<String, BankAccount> accounts = getAccountsForPlayer(server, playerUuid);
        double totalEur = 0.0;
        for (BankAccount acc : accounts.values()) {
            if ((acc.getKind() == AccountKind.DEBIT || acc.getKind() == AccountKind.SAVINGS)
                    && acc.isActive() && acc.getBalance() > 0) {
                try {
                    totalEur += ModCommands.convertCurrency(acc.getBalance(), acc.getCurrency(), "EUR");
                } catch (IllegalArgumentException e) {
                    MoneyMod.LOGGER.warn("[{}] Could not convert {} {} to EUR for balance check",
                            MoneyMod.MOD_ID, acc.getBalance(), acc.getCurrency());
                }
            }
        }
        return totalEur;
    }

    /**
     * Calculates the personal credit cap for a player.
     * Rule: min(GLOBAL_DEBT_CAP_EUR, INCOME_MULTIPLIER × debit/savings balance)
     * <p>
     * Examples:
     *   - Player has 50 EUR on debit  → cap = min(500K, 2×50)  = 100 EUR
     *   - Player has 100K EUR on debit → cap = min(500K, 2×100K) = 200K EUR
     *   - Player has 300K EUR on debit → cap = min(500K, 2×300K) = 500K EUR (global cap)
     */
    public double getPersonalCreditCapEur(MinecraftServer server, UUID playerUuid) {
        double debitBalanceEur = getTotalDebitBalanceEur(server, playerUuid);
        double incomeBased = debitBalanceEur * INCOME_MULTIPLIER;
        return Math.min(GLOBAL_DEBT_CAP_EUR, incomeBased);
    }

    /**
     * Checks whether a player is eligible to open a new credit account.
     * Rules:
     *  - Personal cap = min(500K EUR, 2× total debit/savings balance)
     *  - Total debt (across ALL credit accounts) cannot exceed the personal cap
     *  - No limit on the number of credit accounts
     * <p>
     * Returns an empty Optional if eligible, or a reason string if not.
     */
    public Optional<String> checkCreditEligibility(MinecraftServer server, UUID playerUuid) {
        double currentDebtEur = getTotalDebtEur(server, playerUuid);
        double debitBalanceEur = getTotalDebitBalanceEur(server, playerUuid);
        double personalCap = getPersonalCreditCapEur(server, playerUuid);

        double remainingRoom = personalCap - currentDebtEur;

        if (remainingRoom <= 0) {
            String defaultCurrency = MoneyMod.getDefaultCurrency();
            double capInDefault, debtInDefault, incomeInDefault;
            try {
                capInDefault = ModCommands.convertCurrency(personalCap, "EUR", defaultCurrency);
                debtInDefault = ModCommands.convertCurrency(currentDebtEur, "EUR", defaultCurrency);
                incomeInDefault = ModCommands.convertCurrency(debitBalanceEur, "EUR", defaultCurrency);
            } catch (IllegalArgumentException e) {
                capInDefault = personalCap;
                debtInDefault = currentDebtEur;
                incomeInDefault = debitBalanceEur;
                defaultCurrency = "EUR";
            }

            // Show the player their ACTUAL cap, not the global 500K
            return Optional.of(String.format(
                    "Your personal credit limit is %s %s (2× your debit/savings balance of %s %s, max 500K EUR). Current debt: %s %s. No room for more credit.",
                    tk.bubustein.money.util.CardUtils.formatMoney(capInDefault), defaultCurrency,
                    tk.bubustein.money.util.CardUtils.formatMoney(incomeInDefault), defaultCurrency,
                    tk.bubustein.money.util.CardUtils.formatMoney(debtInDefault), defaultCurrency
            ));
        }

        return Optional.empty(); // Eligible
    }

    /**
     * Checks if a specific withdrawal on a credit account would breach the player's personal cap.
     * Returns true if allowed, false if it would exceed the cap.
     */
    public boolean canAccumulateMoreDebt(MinecraftServer server, UUID playerUuid, double additionalDebtEur) {
        double currentDebtEur = getTotalDebtEur(server, playerUuid);
        double personalCap = getPersonalCreditCapEur(server, playerUuid);
        return (currentDebtEur + additionalDebtEur) <= personalCap;
    }

    // ─── Forced debt recovery ─────────────────────────────────────────

    /**
     * Forces recovery of debt from a player's debit/savings accounts when their
     * credit account debt exceeds the limit after interest.
     * <p>
     * Takes at least MIN_RECOVERY_FRACTION (25%) of the overage from debit/savings.
     *
     * @return the total amount recovered (in EUR equivalent)
     */
    public double forceDebtRecovery(MinecraftServer server, UUID playerUuid, BankAccount creditAccount) {
        if (creditAccount.getKind() != AccountKind.CREDIT || !creditAccount.hasDebt()) {
            return 0.0;
        }

        CreditCardTier tier = creditAccount.getCreditCardTier();
        double debtInAccountCurrency = Math.abs(creditAccount.getBalance());

        // Convert credit limit to account currency
        double limitInAccountCurrency;
        try {
            limitInAccountCurrency = ModCommands.convertCurrency(tier.getCreditLimit(), "EUR", creditAccount.getCurrency());
        } catch (IllegalArgumentException e) {
            limitInAccountCurrency = tier.getCreditLimit();
        }

        // Only trigger if debt exceeds the card's limit
        if (debtInAccountCurrency <= limitInAccountCurrency) {
            return 0.0;
        }

        double overage = debtInAccountCurrency - limitInAccountCurrency;
        double targetRecovery = overage * MIN_RECOVERY_FRACTION; // At least 25% of overage

        if (targetRecovery < 0.01) {
            return 0.0;
        }

        Map<String, BankAccount> accounts = getAccountsForPlayer(server, playerUuid);
        double totalRecovered = 0.0;

        // Try debit accounts first, then savings
        java.util.List<BankAccount> sources = new java.util.ArrayList<>();
        for (BankAccount acc : accounts.values()) {
            if (acc.getKind() == AccountKind.DEBIT && acc.isActive() && acc.getBalance() > 0.01) {
                sources.add(acc);
            }
        }
        for (BankAccount acc : accounts.values()) {
            if (acc.getKind() == AccountKind.SAVINGS && acc.isActive() && acc.getBalance() > 0.01) {
                sources.add(acc);
            }
        }
        for (BankAccount acc : accounts.values()) {
            if (acc.getKind() == AccountKind.CREDIT && acc.isActive() && acc.getBalance() > 0.01) {
                sources.add(acc);
            }
        }

        for (BankAccount source : sources) {
            if (totalRecovered >= targetRecovery) break;

            double stillNeeded = targetRecovery - totalRecovered;

            // Convert needed amount to source account's currency
            double neededInSourceCurrency;
            try {
                neededInSourceCurrency = ModCommands.convertCurrency(
                        stillNeeded, creditAccount.getCurrency(), source.getCurrency());
            } catch (IllegalArgumentException e) {
                continue;
            }

            double canTake = Math.min(neededInSourceCurrency, source.getBalance());
            if (canTake < 0.01) continue;

            // Withdraw from source
            source.setBalance(source.getBalance() - canTake);

            // Convert to credit account currency and repay
            double repayAmount;
            try {
                repayAmount = ModCommands.convertCurrency(canTake, source.getCurrency(), creditAccount.getCurrency());
            } catch (IllegalArgumentException e) {
                // Revert
                source.setBalance(source.getBalance() + canTake);
                continue;
            }

            creditAccount.setBalance(creditAccount.getBalance() + repayAmount);
            totalRecovered += repayAmount;

            MoneyMod.LOGGER.warn("[{}] FORCED RECOVERY: Took {} {} from {} account {} to repay credit account {}",
                    MoneyMod.MOD_ID,
                    tk.bubustein.money.util.CardUtils.formatMoney(canTake), source.getCurrency(),
                    source.getKind().name(), source.getIban(),
                    creditAccount.getIban());
        }

        if (totalRecovered > 0) {
            BankAccountSavedData.get(server).setDirty();
        }

        return totalRecovered;
    }
}
