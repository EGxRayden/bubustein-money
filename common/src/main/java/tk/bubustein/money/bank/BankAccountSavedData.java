package tk.bubustein.money.bank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import tk.bubustein.money.MoneyMod;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BankAccountSavedData extends SavedData {
    private static final String DATA_NAME = "bubustein_bank_accounts";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final ConcurrentHashMap<String, BankAccount> accountsByIban = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<String, BankAccount>> accountsByOwner = new ConcurrentHashMap<>();
    private final AtomicInteger nextAccountId = new AtomicInteger(1);
    public BankAccountSavedData() {
    }
    public static BankAccountSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        BankAccountSavedData data = new BankAccountSavedData();
        if (tag.contains("accounts_json")) {
            String json = tag.getString("accounts_json");
            Type type = new TypeToken<Map<String, BankAccount>>(){}.getType();
            Map<String, BankAccount> loaded = GSON.fromJson(json, type);
            if (loaded != null) {
                data.accountsByIban.putAll(loaded);
                int maxAccountId = 0;
                for (BankAccount acc : loaded.values()) {
                    data.accountsByOwner
                            .computeIfAbsent(acc.getOwnerUuid(), k -> new ConcurrentHashMap<>())
                            .put(acc.getIban(), acc);

                    if (acc.getAccountId() > maxAccountId) {
                        maxAccountId = acc.getAccountId();
                    }
                }
                if (tag.contains("next_account_id")) {
                    int savedNext = tag.getInt("next_account_id");
                    data.nextAccountId.set(Math.max(savedNext, maxAccountId + 1));
                } else {
                    data.nextAccountId.set(maxAccountId + 1);
                }
            }
        }

        return data;
    }
    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        Map<String, BankAccount> snapshot = new HashMap<>(accountsByIban);
        String json = GSON.toJson(snapshot);
        tag.putString("accounts_json", json);
        tag.putInt("next_account_id", nextAccountId.get());
        return tag;
    }
    public static BankAccountSavedData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(
                new SavedData.Factory<>(
                        BankAccountSavedData::new,
                        BankAccountSavedData::load,
                        null
                ),
                DATA_NAME
        );
    }
    public BankAccount createAccount(UUID owner, AccountKind kind, String currency,
                                     String bankPrefix, int accountId, String iban) {
        if (accountsByIban.containsKey(iban)) {
            MoneyMod.LOGGER.error("[{}] Attempted to create account with duplicate IBAN: {}",
                    MoneyMod.MOD_ID, iban);
            return null;
        }
        BankAccount acc = new BankAccount();
        acc.setOwnerUuid(owner);
        acc.setKind(kind);
        acc.setCurrency(currency);
        acc.setBankPrefix(bankPrefix);
        acc.setAccountId(accountId);
        acc.setIban(iban);
        acc.setBalance(0.0);
        acc.setActive(true);
        accountsByIban.put(iban, acc);
        accountsByOwner.computeIfAbsent(owner, k -> new ConcurrentHashMap<>()).put(iban, acc);

        setDirty();
        MoneyMod.LOGGER.info("[{}] Created account {} for player {}",
                MoneyMod.MOD_ID, iban, owner);

        return acc;
    }
    public Optional<BankAccount> getByIban(String iban) {
        if (iban == null || iban.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(accountsByIban.get(iban));
    }
    public Map<String, BankAccount> getAccountsForPlayer(UUID owner) {
        ConcurrentHashMap<String, BankAccount> ownerAccounts = accountsByOwner.get(owner);
        if (ownerAccounts == null) {
            return new HashMap<>();
        }
        return new HashMap<>(ownerAccounts);
    }
    public int nextAccountId() {
        int id = nextAccountId.getAndIncrement();
        setDirty();
        return id;
    }
    public void deleteAccount(String iban) {
        if (iban == null || iban.trim().isEmpty()) {
            return;
        }

        BankAccount acc = accountsByIban.remove(iban);
        if (acc != null) {
            ConcurrentHashMap<String, BankAccount> ownerAccounts = accountsByOwner.get(acc.getOwnerUuid());
            if (ownerAccounts != null) {
                ownerAccounts.remove(iban);
                if (ownerAccounts.isEmpty()) {
                    accountsByOwner.remove(acc.getOwnerUuid());
                }
            }
            setDirty();
            MoneyMod.LOGGER.info("[{}] Deleted account {} for player {}",
                    MoneyMod.MOD_ID, iban, acc.getOwnerUuid());
        }
    }
}