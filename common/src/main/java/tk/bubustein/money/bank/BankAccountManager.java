package tk.bubustein.money.bank;

import net.minecraft.server.MinecraftServer;
import java.util.*;

public class BankAccountManager {
    private static final BankAccountManager INSTANCE = new BankAccountManager();

    private BankAccountManager() {
    }

    public static BankAccountManager get() {
        return INSTANCE;
    }

    public BankAccount createAccount(MinecraftServer server, UUID owner, AccountKind kind,
                                     String currency, String bankPrefix, int accountId, String iban) {
        BankAccountSavedData data = BankAccountSavedData.get(server);
        return data.createAccount(owner, kind, currency, bankPrefix, accountId, iban);
    }

    public Optional<BankAccount> getByIban(MinecraftServer server, String iban) {
        if (iban == null) return Optional.empty();
        BankAccountSavedData data = BankAccountSavedData.get(server);
        return data.getByIban(iban);
    }

    public List<BankAccount> getAccounts(MinecraftServer server, UUID playerUuid) {
        BankAccountSavedData data = BankAccountSavedData.get(server);
        return new ArrayList<>(data.getAccountsForPlayer(playerUuid).values());
    }

    public int nextAccountId(MinecraftServer server) {
        BankAccountSavedData data = BankAccountSavedData.get(server);
        return data.nextAccountId();
    }

    public void deleteAccount(MinecraftServer server, String iban) {
        BankAccountSavedData data = BankAccountSavedData.get(server);
        data.deleteAccount(iban);
    }
    private final Map<String, Bank> banksByPrefix = new HashMap<>();

    public synchronized Optional<Bank> getBankByPrefix(String prefix) {
        return Optional.ofNullable(banksByPrefix.get(prefix));
    }

    public synchronized void registerBank(Bank bank) {
        banksByPrefix.put(bank.getPrefix(), bank);
    }
}
