package tk.bubustein.money.bank;

import net.minecraft.server.MinecraftServer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BankAccountManager {
    private static final BankAccountManager INSTANCE = new BankAccountManager();

    private BankAccountManager() {
    }

    public static BankAccountManager get() {
        return INSTANCE;
    }
    public BankAccount createAccount(MinecraftServer server, UUID owner, AccountKind kind,
                                     String currency, String bankPrefix, int accountId, String iban) {
        if (server == null) {
            throw new IllegalArgumentException("MinecraftServer cannot be null");
        }
        BankAccountSavedData data = BankAccountSavedData.get(server);
        return data.createAccount(owner, kind, currency, bankPrefix, accountId, iban);
    }

    public Optional<BankAccount> getByIban(MinecraftServer server, String iban) {
        if (server == null) {
            throw new IllegalArgumentException("MinecraftServer cannot be null");
        }
        if (iban == null) return Optional.empty();
        BankAccountSavedData data = BankAccountSavedData.get(server);
        return data.getByIban(iban);
    }

    public List<BankAccount> getAccounts(MinecraftServer server, UUID playerUuid) {
        if (server == null) {
            throw new IllegalArgumentException("MinecraftServer cannot be null");
        }
        BankAccountSavedData data = BankAccountSavedData.get(server);
        return new ArrayList<>(data.getAccountsForPlayer(playerUuid).values());
    }

    public int nextAccountId(MinecraftServer server) {
        if (server == null) {
            throw new IllegalArgumentException("MinecraftServer cannot be null");
        }
        BankAccountSavedData data = BankAccountSavedData.get(server);
        return data.nextAccountId();
    }

    public void deleteAccount(MinecraftServer server, String iban) {
        if (server == null) {
            throw new IllegalArgumentException("MinecraftServer cannot be null");
        }
        BankAccountSavedData data = BankAccountSavedData.get(server);
        data.deleteAccount(iban);
    }

    private final ConcurrentHashMap<String, Bank> banksByPrefix = new ConcurrentHashMap<>();

    public Optional<Bank> getBankByPrefix(String prefix) {
        return Optional.ofNullable(banksByPrefix.get(prefix));
    }
    public void registerBank(Bank bank) {
        banksByPrefix.put(bank.getPrefix(), bank);
    }
    public Collection<Bank> getAllBanks() {
        return Collections.unmodifiableCollection(banksByPrefix.values());
    }

    public boolean bankExists(String prefix) {
        return banksByPrefix.containsKey(prefix);
    }

    public void unregisterBank(String prefix) {
        banksByPrefix.remove(prefix);
    }
}