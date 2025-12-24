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
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BankAccountSavedData extends SavedData {
    private static final String DATA_NAME = "bubustein_bank_accounts";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, BankAccount> accountsByIban = new HashMap<>();
    private final Map<UUID, Map<String, BankAccount>> accountsByOwner = new HashMap<>();
    private int nextAccountId = 1;

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
                for (BankAccount acc : loaded.values()) {
                    data.accountsByOwner
                            .computeIfAbsent(acc.getOwnerUuid(), k -> new HashMap<>())
                            .put(acc.getIban(), acc);
                }
            }
        }
        if (tag.contains("next_account_id")) {
            data.nextAccountId = tag.getInt("next_account_id");
        }

        return data;
    }
    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        String json = GSON.toJson(accountsByIban);
        tag.putString("accounts_json", json);
        tag.putInt("next_account_id", nextAccountId);
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
        accountsByOwner.computeIfAbsent(owner, k -> new HashMap<>()).put(iban, acc);
        setDirty();
        return acc;
    }
    public Optional<BankAccount> getByIban(String iban) {
        return Optional.ofNullable(accountsByIban.get(iban));
    }

    public Map<String, BankAccount> getAccountsForPlayer(UUID owner) {
        return accountsByOwner.getOrDefault(owner, new HashMap<>());
    }

    public int nextAccountId() {
        int id = nextAccountId++;
        setDirty();
        return id;
    }

    public void deleteAccount(String iban) {
        BankAccount acc = accountsByIban.remove(iban);
        if (acc != null) {
            Map<String, BankAccount> ownerAccounts = accountsByOwner.get(acc.getOwnerUuid());
            if (ownerAccounts != null) {
                ownerAccounts.remove(iban);
                if (ownerAccounts.isEmpty()) {
                    accountsByOwner.remove(acc.getOwnerUuid());
                }
            }
            setDirty();
        }
    }
}