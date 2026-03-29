package tk.bubustein.money.bank;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import tk.bubustein.money.MoneyMod;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;

public class BankAccountSavedData extends SavedData {

    private static final String DATA_NAME = "bubustein_bank_accounts";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final ConcurrentHashMap<String, BankAccount> accountsByIban = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, List<PendingTransfer>> pendingTransfers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<String, BankAccount>> accountsByOwner = new ConcurrentHashMap<>();
    private final AtomicInteger nextAccountId = new AtomicInteger(1);

    private final ReadWriteLock dataLock = new ReentrantReadWriteLock();

    public BankAccountSavedData() {
    }

    public static BankAccountSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        BankAccountSavedData data = new BankAccountSavedData();

        data.dataLock.writeLock().lock();
        try {
            if (tag.contains("accounts_json")) {
                String json = tag.getString("accounts_json");
                Type type = new TypeToken<Map<String, BankAccount>>(){}.getType();
                Map<String, BankAccount> loaded = GSON.fromJson(json, type);

                if (loaded != null) {
                    data.accountsByIban.clear();
                    data.accountsByOwner.clear();
                    data.accountsByIban.putAll(loaded);

                    for (BankAccount acc : loaded.values()) {
                        data.accountsByOwner
                                .computeIfAbsent(acc.getOwnerUuid(), k -> new ConcurrentHashMap<>())
                                .put(acc.getIban(), acc);
                    }

                    int maxAccountId = 0;
                    for (BankAccount acc : loaded.values()) {
                        try {
                            String iban = acc.getIban();
                            String accountNumber = iban.substring(iban.length() - 10);
                            int accountId = Integer.parseInt(accountNumber);
                            if (accountId > maxAccountId) {
                                maxAccountId = accountId;
                            }
                        } catch (Exception e) {
                            MoneyMod.LOGGER.warn("[{}] Could not parse account ID from IBAN: {}",
                                    MoneyMod.MOD_ID, acc.getIban());
                        }
                    }

                    if (tag.contains("next_account_id")) {
                        int savedNext = tag.getInt("next_account_id");
                        data.nextAccountId.set(Math.max(savedNext, maxAccountId + 1));
                    } else {
                        data.nextAccountId.set(maxAccountId + 1);
                    }

                    MoneyMod.LOGGER.info("[{}] Loaded {} bank accounts successfully (next ID: {})",
                            MoneyMod.MOD_ID, loaded.size(), data.nextAccountId.get());
                }
            }

            if (tag.contains("pending_transfers")) {
                String transfersJson = tag.getString("pending_transfers");
                Type transfersType = new TypeToken<ConcurrentHashMap<UUID, List<PendingTransfer>>>(){}.getType();

                try {
                    ConcurrentHashMap<UUID, List<PendingTransfer>> loadedTransfers =
                            GSON.fromJson(transfersJson, transfersType);

                    if (loadedTransfers != null && !loadedTransfers.isEmpty()) {
                        data.pendingTransfers.clear();
                        data.pendingTransfers.putAll(loadedTransfers);

                        int totalTransfers = loadedTransfers.values().stream()
                                .mapToInt(List::size)
                                .sum();

                        MoneyMod.LOGGER.info("[{}] Loaded {} pending transfer(s) for {} player(s)",
                                MoneyMod.MOD_ID, totalTransfers, loadedTransfers.size());
                    }
                } catch (JsonSyntaxException e) {
                    MoneyMod.LOGGER.error("[{}] Failed to parse pending transfers JSON",
                            MoneyMod.MOD_ID, e);
                }
            }

        } catch (Exception e) {
            MoneyMod.LOGGER.error("[{}] Failed to load bank accounts data", MoneyMod.MOD_ID, e);
        } finally {
            data.dataLock.writeLock().unlock();
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        dataLock.readLock().lock();
        try {
            String json = GSON.toJson(accountsByIban);
            tag.putString("accounts_json", json);
            tag.putInt("next_account_id", nextAccountId.get());

            MoneyMod.LOGGER.debug("[{}] Saved {} bank accounts (next ID: {})",
                    MoneyMod.MOD_ID, accountsByIban.size(), nextAccountId.get());

            if (!pendingTransfers.isEmpty()) {
                String transfersJson = GSON.toJson(pendingTransfers);
                tag.putString("pending_transfers", transfersJson);

                int totalTransfers = pendingTransfers.values().stream()
                        .mapToInt(List::size)
                        .sum();

                MoneyMod.LOGGER.debug("[{}] Saved {} pending transfer(s) for {} player(s)",
                        MoneyMod.MOD_ID, totalTransfers, pendingTransfers.size());
            } else {
                tag.remove("pending_transfers");
            }

        } catch (Exception e) {
            MoneyMod.LOGGER.error("[{}] Failed to save bank accounts data", MoneyMod.MOD_ID, e);
        } finally {
            dataLock.readLock().unlock();
        }

        return tag;
    }

    public static BankAccountSavedData get(@NotNull MinecraftServer server) {
        if (server == null) {
            throw new IllegalArgumentException("MinecraftServer cannot be null");
        }
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
    public BankAccount createAccount(UUID owner, String ownerName, AccountKind kind,
                                     String currency, String bankPrefix,
                                     int accountId, String iban, String initialTier) {
        dataLock.writeLock().lock();
        try {
            if (accountsByIban.containsKey(iban)) {
                MoneyMod.LOGGER.error("[{}] Attempted to create account with duplicate IBAN: {}",
                        MoneyMod.MOD_ID, iban);
                return null;
            }
            BankAccount acc = new BankAccount(
                    iban,
                    owner,
                    ownerName,
                    0.0,
                    currency,
                    initialTier,
                    kind,
                    false,
                    bankPrefix,
                    0L,
                    0L
            );

            accountsByIban.put(iban, acc);
            accountsByOwner.computeIfAbsent(owner, k -> new ConcurrentHashMap<>()).put(iban, acc);

            setDirty();
            MoneyMod.LOGGER.info("[{}] Created {} account {} for player {} (tier: {})",
                    MoneyMod.MOD_ID, kind.name(), iban, ownerName, initialTier);
            return acc;
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    public Optional<BankAccount> getByIban(String iban) {
        if (iban == null || iban.trim().isEmpty()) {
            return Optional.empty();
        }
        dataLock.readLock().lock();
        try {
            return Optional.ofNullable(accountsByIban.get(iban));
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public Map<String, BankAccount> getAccountsForPlayer(UUID owner) {
        dataLock.readLock().lock();
        try {
            ConcurrentHashMap<String, BankAccount> ownerAccounts = accountsByOwner.get(owner);
            if (ownerAccounts == null) {
                return new HashMap<>();
            }
            return new HashMap<>(ownerAccounts);
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public void addPendingTransfer(PendingTransfer transfer) {
        dataLock.writeLock().lock();
        try {
            pendingTransfers.computeIfAbsent(transfer.getRecipientUuid(), k -> new ArrayList<>())
                    .add(transfer);
            setDirty();

            MoneyMod.LOGGER.debug("[{}] Added pending transfer: {} {} to {} (IBAN: {})",
                    MoneyMod.MOD_ID, transfer.getAmount(), transfer.getCurrency(),
                    transfer.getRecipientUuid(), transfer.getTargetIban());
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    public List<PendingTransfer> getPendingTransfers(UUID playerUuid) {
        dataLock.readLock().lock();
        try {
            List<PendingTransfer> transfers = pendingTransfers.get(playerUuid);
            return transfers != null ? new ArrayList<>(transfers) : new ArrayList<>();
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public void removePendingTransfer(UUID playerUuid, PendingTransfer transfer) {
        dataLock.writeLock().lock();
        try {
            List<PendingTransfer> transfers = pendingTransfers.get(playerUuid);
            if (transfers != null) {
                transfers.remove(transfer);
                if (transfers.isEmpty()) {
                    pendingTransfers.remove(playerUuid);
                }
                setDirty();

                MoneyMod.LOGGER.debug("[{}] Removed pending transfer for player {}",
                        MoneyMod.MOD_ID, playerUuid);
            }
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    public void clearPendingTransfers(UUID playerUuid) {
        dataLock.writeLock().lock();
        try {
            List<PendingTransfer> removed = pendingTransfers.remove(playerUuid);
            if (removed != null && !removed.isEmpty()) {
                setDirty();
                MoneyMod.LOGGER.info("[{}] Cleared {} pending transfer(s) for player {}",
                        MoneyMod.MOD_ID, removed.size(), playerUuid);
            }
        } finally {
            dataLock.writeLock().unlock();
        }
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
        dataLock.writeLock().lock();
        try {
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
                MoneyMod.LOGGER.info("[{}] Deleted {} account {} for player {}",
                        MoneyMod.MOD_ID, acc.getKind().name(), iban, acc.getOwnerName());
            }
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    /**
     * Migrates all accounts from one bank prefix to a new bank prefix.
     * Creates new IBANs for each account, preserving balance, currency, tier, kind, etc.
     * Old accounts are deleted, new accounts are created as INACTIVE (cards must be re-linked).
     *
     * @return the number of accounts migrated
     */
    public int migrateAccountsFromBank(String oldPrefix, String newPrefix, String countryCode) {
        dataLock.writeLock().lock();
        try {
            // Find all accounts with the old prefix
            java.util.List<BankAccount> toMigrate = new java.util.ArrayList<>();
            for (BankAccount acc : accountsByIban.values()) {
                if (acc.getBankPrefix().equalsIgnoreCase(oldPrefix)) {
                    toMigrate.add(acc);
                }
            }

            if (toMigrate.isEmpty()) {
                return 0;
            }

            int migrated = 0;
            for (BankAccount old : toMigrate) {
                // Remove old account from maps
                accountsByIban.remove(old.getIban());
                ConcurrentHashMap<String, BankAccount> ownerAccounts = accountsByOwner.get(old.getOwnerUuid());
                if (ownerAccounts != null) {
                    ownerAccounts.remove(old.getIban());
                    if (ownerAccounts.isEmpty()) {
                        accountsByOwner.remove(old.getOwnerUuid());
                    }
                }

                // Generate new IBAN with the new prefix
                int newAccountId = nextAccountId.getAndIncrement();
                String newIban;
                try {
                    newIban = IbanGenerator.generateIban(countryCode, newPrefix, old.getKind(), newAccountId);
                } catch (IllegalArgumentException e) {
                    MoneyMod.LOGGER.error("[{}] Failed to generate new IBAN for migrated account {}: {}",
                            MoneyMod.MOD_ID, old.getIban(), e.getMessage());
                    // Put old account back to avoid data loss
                    accountsByIban.put(old.getIban(), old);
                    accountsByOwner.computeIfAbsent(old.getOwnerUuid(), k -> new ConcurrentHashMap<>())
                            .put(old.getIban(), old);
                    continue;
                }

                // Create new account with all data copied, but inactive
                BankAccount newAcc = new BankAccount(
                        newIban,
                        old.getOwnerUuid(),
                        old.getOwnerName(),
                        old.getBalance(),
                        old.getCurrency(),
                        old.getCardTier(),
                        old.getKind(),
                        false, // inactive — cards need to be re-linked
                        newPrefix.toUpperCase(),
                        old.getLastInterestTimestamp(),
                        old.getLastInterestGameTick()
                );
                newAcc.setLastInterestTimestamp(old.getLastInterestTimestamp());

                accountsByIban.put(newIban, newAcc);
                accountsByOwner.computeIfAbsent(old.getOwnerUuid(), k -> new ConcurrentHashMap<>())
                        .put(newIban, newAcc);

                migrated++;
                MoneyMod.LOGGER.info("[{}] Migrated account {} -> {} (owner: {}, balance: {} {})",
                        MoneyMod.MOD_ID, old.getIban(), newIban, old.getOwnerName(),
                        old.getBalance(), old.getCurrency());
            }

            if (migrated > 0) {
                setDirty();
            }
            return migrated;
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    public int getTotalAccounts() {
        dataLock.readLock().lock();
        try {
            return accountsByIban.size();
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public int getActiveAccounts() {
        dataLock.readLock().lock();
        try {
            return (int) accountsByIban.values().stream()
                    .filter(BankAccount::isActive)
                    .count();
        } finally {
            dataLock.readLock().unlock();
        }
    }
}