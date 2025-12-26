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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.item.ModItems;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int CURRENT_CONFIG_VERSION = 1;

    private String serverCountryCode = "RO";
    private String adminResetPassword = "";
    private int configVersion = CURRENT_CONFIG_VERSION;
    private String defaultCurrency = "EUR";
    private Map<String, Double> exchangeRates = new HashMap<>();
    private long lastRatesUpdateEpoch = 0;
    private String lastRatesUpdateReadable = "Never";

    private static volatile ModConfig instance;
    private static final Object INSTANCE_LOCK = new Object();

    private final ReadWriteLock configLock = new ReentrantReadWriteLock();

    private ModConfig() {}
    public static ModConfig getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_LOCK) {
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
        configLock.writeLock().lock();
        try {
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
                    }
                } catch (Exception e) {
                    MoneyMod.LOGGER.error("[{}] Failed to load config file; using defaults", MoneyMod.MOD_ID, e);
                }
            } else {
                this.configVersion = CURRENT_CONFIG_VERSION;
                save(server);
            }
            if (this.configVersion < CURRENT_CONFIG_VERSION) {
                MoneyMod.LOGGER.info("[{}] Config upgrade: {} → {}",
                        MoneyMod.MOD_ID, this.configVersion, CURRENT_CONFIG_VERSION);
                this.configVersion = CURRENT_CONFIG_VERSION;
                save(server);
            }
            if (!ModItems.getCurrencyItems().containsKey(this.defaultCurrency)) {
                MoneyMod.LOGGER.warn("[{}] Default currency '{}' invalid after config load, reset to EUR",
                        MoneyMod.MOD_ID, this.defaultCurrency);
                this.defaultCurrency = "EUR";
                save(server);
            }
        } finally {
            configLock.writeLock().unlock();
        }
    }
    public void save(MinecraftServer server) {
        if (server == null) {
            MoneyMod.LOGGER.warn("[{}] Tried to save config with null server, skipping", MoneyMod.MOD_ID);
            return;
        }
        configLock.readLock().lock();
        try {
            Path configPath = getConfigPath(server);
            File configFile = configPath.toFile();
            if (configFile.exists()) {
                File bakFile = new File(configFile.getAbsolutePath() + CONFIG_BAK_SUFFIX);
                try {
                    Files.copy(configFile.toPath(), bakFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
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

                try (FileWriter writer = new FileWriter(configFile)) {
                    GSON.toJson(this, writer);
                }
                MoneyMod.LOGGER.debug("[{}] Config saved successfully", MoneyMod.MOD_ID);
            } catch (Exception e) {
                MoneyMod.LOGGER.error("[{}] Failed to save config file: {}",
                        MoneyMod.MOD_ID, configFile, e);
                File bakFile = new File(configFile.getAbsolutePath() + CONFIG_BAK_SUFFIX);
                if (bakFile.exists()) {
                    try {
                        Files.copy(bakFile.toPath(), configFile.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        MoneyMod.LOGGER.info("[{}] Restored config from backup", MoneyMod.MOD_ID);
                    } catch (IOException restoreError) {
                        MoneyMod.LOGGER.error("[{}] Failed to restore config from backup",
                                MoneyMod.MOD_ID, restoreError);
                    }
                }
            }
        } finally {
            configLock.readLock().unlock();
        }
    }
    private static Path getConfigPath(MinecraftServer server) {
        Path dir = server.getServerDirectory().toAbsolutePath().resolve("config");
        return dir.resolve(CONFIG_FILE_NAME);
    }
    public String getDefaultCurrency() {
        configLock.readLock().lock();
        try {
            return defaultCurrency;
        } finally {
            configLock.readLock().unlock();
        }
    }
    public void setDefaultCurrency(String defaultCurrency) {
        configLock.writeLock().lock();
        try {
            this.defaultCurrency = defaultCurrency;
        } finally {
            configLock.writeLock().unlock();
        }
    }
    public int getConfigVersion() {
        configLock.readLock().lock();
        try {
            return configVersion;
        } finally {
            configLock.readLock().unlock();
        }
    }
    public void setConfigVersion(int version) {
        configLock.writeLock().lock();
        try {
            this.configVersion = version;
        } finally {
            configLock.writeLock().unlock();
        }
    }
    public Map<String, Double> getExchangeRates() {
        configLock.readLock().lock();
        try {
            return new HashMap<>(exchangeRates);
        } finally {
            configLock.readLock().unlock();
        }
    }
    public void setExchangeRates(Map<String, Double> rates) {
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
        configLock.readLock().lock();
        try {
            return lastRatesUpdateEpoch;
        } finally {
            configLock.readLock().unlock();
        }
    }
    public String getLastRatesUpdateReadable() {
        configLock.readLock().lock();
        try {
            return lastRatesUpdateReadable;
        } finally {
            configLock.readLock().unlock();
        }
    }
    public void loadWithMigration(MinecraftServer server) {
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
                        MoneyMod.LOGGER.error("[{}] Failed to migrate config from legacy location.", MoneyMod.MOD_ID, e);
                    }
                }
                save(server);
            }
        } finally {
            configLock.writeLock().unlock();
        }
        load(server);
    }
    private static Path getLegacyConfigPath(MinecraftServer server) {
        return server.getWorldPath(new LevelResource("data"))
                .resolve(CONFIG_FILE_NAME);
    }
    public String getServerCountryCode() {
        configLock.readLock().lock();
        try {
            return serverCountryCode;
        } finally {
            configLock.readLock().unlock();
        }
    }
    public void setServerCountryCode(String serverCountryCode) {
        configLock.writeLock().lock();
        try {
            this.serverCountryCode = serverCountryCode;
        } finally {
            configLock.writeLock().unlock();
        }
    }
    public String getAdminResetPassword() {
        configLock.readLock().lock();
        try {
            return adminResetPassword;
        } finally {
            configLock.readLock().unlock();
        }
    }
    public void setAdminResetPassword(String adminResetPassword) {
        configLock.writeLock().lock();
        try {
            this.adminResetPassword = adminResetPassword;
        } finally {
            configLock.writeLock().unlock();
        }
    }
}