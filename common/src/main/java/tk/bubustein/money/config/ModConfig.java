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
package tk.bubustein.money.config;

import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.item.ModItems;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ModConfig {
    private static final String CONFIG_FILE_NAME = "bubusteinmoneymod-config.json";
    private static final String CONFIG_BAK_SUFFIX = ".bak";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ModConfig.class, new ModConfigDeserializer())
            .create();

    private static final int CURRENT_CONFIG_VERSION = 1;

    private String serverCountryCode = "RO";
    private String adminResetPassword = "";
    private int configVersion = CURRENT_CONFIG_VERSION;
    private String defaultCurrency = "EUR";
    private Map<String, Double> exchangeRates = new HashMap<>();
    private long lastRatesUpdateEpoch = 0;
    private String lastRatesUpdateReadable = "Never";

    private static volatile ModConfig instance;

    private transient volatile ReadWriteLock configLock;

    private ModConfig() {
        ensureLockInitialized();
    }
    public void initializeLock() {
        if (configLock == null) {
            configLock = new ReentrantReadWriteLock();
        }
    }

    private void ensureLockInitialized() {
        if (configLock == null) {
            synchronized (this) {
                if (configLock == null) {
                    configLock = new ReentrantReadWriteLock();
                }
            }
        }
    }
    public static ModConfig getInstance() {
        if (instance == null) {
            synchronized (ModConfig.class) {
                if (instance == null) {
                    instance = new ModConfig();
                }
            }
        }
        return instance;
    }
    public void load(MinecraftServer server) {
        if (server == null) {
            MoneyMod.LOGGER.warn("[{}] Tried to load config with null server, skipping", MoneyMod.MOD_ID);
            return;
        }
        ensureLockInitialized();
        configLock.writeLock().lock();
        try {
            loadInternal(server);
        } finally {
            configLock.writeLock().unlock();
        }
    }
    public void save(MinecraftServer server) {
        if (server == null) {
            MoneyMod.LOGGER.warn("[{}] Tried to save config with null server, skipping", MoneyMod.MOD_ID);
            return;
        }
        ensureLockInitialized();
        configLock.writeLock().lock();
        try {
            saveInternal(server);
        } finally {
            configLock.writeLock().unlock();
        }
    }
    private static Path getConfigPath(MinecraftServer server) {
        Path dir = server.getServerDirectory().toAbsolutePath().resolve("config");
        return dir.resolve(CONFIG_FILE_NAME);
    }

    public String getDefaultCurrency() {
        ensureLockInitialized();
        configLock.readLock().lock();
        try {
            return defaultCurrency;
        } finally {
            configLock.readLock().unlock();
        }
    }

    public void setDefaultCurrency(String defaultCurrency) {
        ensureLockInitialized();
        configLock.writeLock().lock();
        try {
            this.defaultCurrency = defaultCurrency;
        } finally {
            configLock.writeLock().unlock();
        }
    }

    public int getConfigVersion() {
        ensureLockInitialized();
        configLock.readLock().lock();
        try {
            return configVersion;
        } finally {
            configLock.readLock().unlock();
        }
    }

    public void setConfigVersion(int version) {
        ensureLockInitialized();
        configLock.writeLock().lock();
        try {
            this.configVersion = version;
        } finally {
            configLock.writeLock().unlock();
        }
    }

    public Map<String, Double> getExchangeRates() {
        ensureLockInitialized();
        configLock.readLock().lock();
        try {
            return new HashMap<>(exchangeRates);
        } finally {
            configLock.readLock().unlock();
        }
    }

    public void setExchangeRates(Map<String, Double> rates) {
        ensureLockInitialized();
        configLock.writeLock().lock();
        try {
            Map<String, Double> filteredRates = new HashMap<>();
            for (Map.Entry<String, Double> entry : rates.entrySet()) {
                if (ModItems.getCurrencyItems().containsKey(entry.getKey())) {
                    filteredRates.put(entry.getKey(), entry.getValue());
                }
            }
            this.exchangeRates = filteredRates;
            this.lastRatesUpdateEpoch = Instant.now().getEpochSecond();
            this.lastRatesUpdateReadable = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.now());
        } finally {
            configLock.writeLock().unlock();
        }
    }

    public long getLastRatesUpdateEpoch() {
        ensureLockInitialized();
        configLock.readLock().lock();
        try {
            return lastRatesUpdateEpoch;
        } finally {
            configLock.readLock().unlock();
        }
    }

    public String getLastRatesUpdateReadable() {
        ensureLockInitialized();
        configLock.readLock().lock();
        try {
            return lastRatesUpdateReadable;
        } finally {
            configLock.readLock().unlock();
        }
    }
    public void loadWithMigration(MinecraftServer server) {
        if (server == null) {
            MoneyMod.LOGGER.warn("[{}] Tried to load config with migration with null server, skipping",
                    MoneyMod.MOD_ID);
            return;
        }

        ensureLockInitialized();
        configLock.writeLock().lock();
        try {
            Path newPath = getConfigPath(server);
            File newFile = newPath.toFile();

            if (!newFile.exists()) {
                Path legacyPath = getLegacyConfigPath(server);
                File legacyFile = legacyPath.toFile();

                if (legacyFile.exists()) {
                    try (FileReader reader = new FileReader(legacyFile)) {
                        ModConfig legacyConfig = GSON.fromJson(reader, ModConfig.class);
                        if (legacyConfig != null && legacyConfig.defaultCurrency != null) {
                            this.defaultCurrency = legacyConfig.defaultCurrency;
                            MoneyMod.LOGGER.info("[{}] Migrated defaultCurrency ('{}') from old config in world/data.",
                                    MoneyMod.MOD_ID, this.defaultCurrency);
                        }
                    } catch (Exception e) {
                        MoneyMod.LOGGER.error("[{}] Failed to migrate config from legacy location.",
                                MoneyMod.MOD_ID, e);
                    }
                }
                saveInternal(server);
            }
            loadInternal(server);
        } finally {
            configLock.writeLock().unlock();
        }
    }
    private void loadInternal(MinecraftServer server) {
        Path configPath = getConfigPath(server);
        File configFile = configPath.toFile();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                ModConfig loadedConfig = GSON.fromJson(reader, ModConfig.class);
                if (loadedConfig != null) {
                    this.configVersion = loadedConfig.configVersion;
                    this.defaultCurrency = loadedConfig.defaultCurrency;
                    this.exchangeRates = loadedConfig.exchangeRates != null ?
                            new HashMap<>(loadedConfig.exchangeRates) : new HashMap<>();
                    this.lastRatesUpdateEpoch = loadedConfig.lastRatesUpdateEpoch;
                    this.lastRatesUpdateReadable = loadedConfig.lastRatesUpdateReadable;
                    this.serverCountryCode = loadedConfig.serverCountryCode;
                    this.adminResetPassword = loadedConfig.adminResetPassword;

                    MoneyMod.LOGGER.info("[{}] Config loaded successfully", MoneyMod.MOD_ID);
                }
            } catch (Exception e) {
                MoneyMod.LOGGER.error("[{}] Failed to load config file; using defaults", MoneyMod.MOD_ID, e);
            }
        } else {
            MoneyMod.LOGGER.info("[{}] Config file not found, using defaults", MoneyMod.MOD_ID);
            this.configVersion = CURRENT_CONFIG_VERSION;
        }

        // Validări
        if (this.configVersion < CURRENT_CONFIG_VERSION) {
            MoneyMod.LOGGER.info("[{}] Config upgrade: {} → {}",
                    MoneyMod.MOD_ID, this.configVersion, CURRENT_CONFIG_VERSION);
            this.configVersion = CURRENT_CONFIG_VERSION;
            saveInternal(server);
        }

        if (!ModItems.getCurrencyItems().containsKey(this.defaultCurrency)) {
            MoneyMod.LOGGER.warn("[{}] Default currency '{}' invalid, reset to EUR",
                    MoneyMod.MOD_ID, this.defaultCurrency);
            this.defaultCurrency = "EUR";
            saveInternal(server);
        }
    }
    private void saveInternal(MinecraftServer server) {
        Path configPath = getConfigPath(server);
        File configFile = configPath.toFile();

        if (configFile.exists()) {
            File bakFile = new File(configFile.getAbsolutePath() + CONFIG_BAK_SUFFIX);
            try {
                Files.copy(configFile.toPath(), bakFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                MoneyMod.LOGGER.debug("[{}] Created config backup", MoneyMod.MOD_ID);
            } catch (IOException e) {
                MoneyMod.LOGGER.warn("[{}] Couldn't create backup config file: {}",
                        MoneyMod.MOD_ID, bakFile, e);
            }
        }

        try {
            File parentDir = configFile.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
            }

            File tempFile = new File(configFile.getAbsolutePath() + ".tmp");
            try (FileWriter writer = new FileWriter(tempFile)) {
                GSON.toJson(this, writer);
                writer.flush();
            }
            Files.move(tempFile.toPath(), configFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

            MoneyMod.LOGGER.debug("[{}] Config saved successfully", MoneyMod.MOD_ID);

        } catch (Exception e) {
            MoneyMod.LOGGER.error("[{}] Failed to save config file: {}",
                    MoneyMod.MOD_ID, configFile, e);

            File bakFile = new File(configFile.getAbsolutePath() + CONFIG_BAK_SUFFIX);
            if (bakFile.exists()) {
                try {
                    Files.copy(bakFile.toPath(), configFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    MoneyMod.LOGGER.info("[{}] Restored config from backup", MoneyMod.MOD_ID);
                } catch (IOException restoreError) {
                    MoneyMod.LOGGER.error("[{}] Failed to restore config from backup",
                            MoneyMod.MOD_ID, restoreError);
                }
            }
        }
    }
    private static Path getLegacyConfigPath(MinecraftServer server) {
        return server.getWorldPath(new LevelResource("data"))
                .resolve(CONFIG_FILE_NAME);
    }
    public String getServerCountryCode() {
        ensureLockInitialized();
        configLock.readLock().lock();
        try {
            return serverCountryCode;
        } finally {
            configLock.readLock().unlock();
        }
    }
    public void setServerCountryCode(String serverCountryCode) {
        ensureLockInitialized();
        configLock.writeLock().lock();
        try {
            this.serverCountryCode = serverCountryCode;
        } finally {
            configLock.writeLock().unlock();
        }
    }
    public String getAdminResetPassword() {
        ensureLockInitialized();
        configLock.readLock().lock();
        try {
            return adminResetPassword;
        } finally {
            configLock.readLock().unlock();
        }
    }
    public void setAdminResetPassword(String adminResetPassword) {
        ensureLockInitialized();
        configLock.writeLock().lock();
        try {
            this.adminResetPassword = adminResetPassword;
        } finally {
            configLock.writeLock().unlock();
        }
    }
    private static class ModConfigDeserializer implements JsonDeserializer<ModConfig> {
        @Override
        public ModConfig deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
                                     JsonDeserializationContext context) throws JsonParseException {
            ModConfig config = new Gson().fromJson(json, ModConfig.class);
            if (config != null) {
                config.initializeLock();
            }
            return config;
        }
    }
}