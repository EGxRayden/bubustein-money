package tk.bubustein.money.bank;

import net.minecraft.server.MinecraftServer;
import tk.bubustein.money.config.ModConfig;

import java.util.*;

public class BankAccountManager {
    private static final BankAccountManager INSTANCE = new BankAccountManager();

    private final Map<String, BankAccount> accountsByIban = new HashMap<>();
    private final Map<UUID, List<BankAccount>> accountsByPlayer = new HashMap<>();
    private final Map<String, Bank> banksByPrefix = new HashMap<>();

    public static BankAccountManager get() {
        return INSTANCE;
    }

    public synchronized Optional<BankAccount> getByIban(String iban) {
        return Optional.ofNullable(accountsByIban.get(iban));
    }

    public synchronized List<BankAccount> getAccounts(UUID playerUuid) {
        return accountsByPlayer.getOrDefault(playerUuid, List.of());
    }

    public synchronized BankAccount createAccount(UUID owner, AccountKind kind,
                                                  String currency, String bankPrefix,
                                                  int accountId, String iban) {
        BankAccount acc = new BankAccount();
        acc.setOwnerUuid(owner);
        acc.setKind(kind);
        acc.setCurrency(currency);
        acc.setBankPrefix(bankPrefix);
        acc.setAccountId(accountId);
        acc.setIban(iban);
        acc.setBalance(0.0);
        accountsByIban.put(iban, acc);
        accountsByPlayer.computeIfAbsent(owner, k -> new ArrayList<>()).add(acc);
        return acc;
    }

    // încărcare/salvare din ModConfig – le facem în pasul următor

    public synchronized void loadFromConfig(MinecraftServer server) {
        ModConfig cfg = ModConfig.getInstance();

        accountsByIban.clear();
        accountsByPlayer.clear();
        banksByPrefix.clear();

        // conturi
        for (ModConfig.BankAccountData data : cfg.getAccounts().values()) {
            try {
                BankAccount acc = new BankAccount();
                acc.setOwnerUuid(UUID.fromString(data.ownerUuid));
                acc.setIban(data.iban);
                acc.setCurrency(data.currency);
                acc.setKind(AccountKind.valueOf(data.kind));
                acc.setBalance(data.balance);
                acc.setBankPrefix(data.bankPrefix);
                acc.setAccountId(data.accountId);
                acc.setActive(data.active);

                accountsByIban.put(acc.getIban(), acc);
                accountsByPlayer
                        .computeIfAbsent(acc.getOwnerUuid(), k -> new ArrayList<>())
                        .add(acc);
            } catch (Exception e) {
                // log, dar nu oprești tot
            }
        }

        // bănci
        for (ModConfig.BankData data : cfg.getBanks().values()) {
            try {
                Bank bank = new Bank();
                bank.setOwnerUuid(UUID.fromString(data.ownerUuid));
                bank.setPrefix(data.prefix);
                bank.setName(data.name);
                bank.setActive(data.active);

                banksByPrefix.put(bank.getPrefix(), bank);
            } catch (Exception e) {
                // log
            }
        }
    }
    public synchronized void saveToConfig(MinecraftServer server) {
        ModConfig cfg = ModConfig.getInstance();

        Map<String, ModConfig.BankAccountData> accMap = new HashMap<>();
        for (BankAccount acc : accountsByIban.values()) {
            ModConfig.BankAccountData d = new ModConfig.BankAccountData();
            d.ownerUuid = acc.getOwnerUuid().toString();
            d.iban = acc.getIban();
            d.currency = acc.getCurrency();
            d.kind = acc.getKind().name();
            d.balance = acc.getBalance();
            d.bankPrefix = acc.getBankPrefix();
            d.accountId = acc.getAccountId();
            d.active = acc.isActive();
            accMap.put(d.iban, d);
        }
        cfg.setAccounts(accMap);

        Map<String, ModConfig.BankData> bankMap = new HashMap<>();
        for (Bank bank : banksByPrefix.values()) {
            ModConfig.BankData d = new ModConfig.BankData();
            d.ownerUuid = bank.getOwnerUuid().toString();
            d.prefix = bank.getPrefix();
            d.name = bank.getName();
            d.active = bank.isActive();
            bankMap.put(d.prefix, d);
        }
        cfg.setBanks(bankMap);

        cfg.save(server);
    }
    public synchronized int nextAccountId(MinecraftServer server) {
        ModConfig cfg = ModConfig.getInstance();
        int id = cfg.consumeNextAccountId();
        cfg.save(server);
        return id;
    }
}
