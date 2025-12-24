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

    private ModConfig() {}

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
    public synchronized void load(MinecraftServer server) {
        Path configPath = getConfigPath(server);
        File configFile = configPath.toFile();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                ModConfig loadedConfig = GSON.fromJson(reader, ModConfig.class);
                if (loadedConfig != null) {
                    this.configVersion = loadedConfig.configVersion;
                    this.defaultCurrency = loadedConfig.defaultCurrency;
                    this.exchangeRates = loadedConfig.exchangeRates != null ? loadedConfig.exchangeRates : new HashMap<>();
                    this.lastRatesUpdateEpoch = loadedConfig.lastRatesUpdateEpoch;
                    this.lastRatesUpdateReadable = loadedConfig.lastRatesUpdateReadable;
                }
            } catch (Exception e) {
                MoneyMod.LOGGER.error("Failed to load config file; using defaults", e);
            }
        } else {
            this.configVersion = CURRENT_CONFIG_VERSION;
            save(server);
        }

        if (this.configVersion < CURRENT_CONFIG_VERSION) {
            MoneyMod.LOGGER.info("Config upgrade: {} → {}", this.configVersion, CURRENT_CONFIG_VERSION);
            this.configVersion = CURRENT_CONFIG_VERSION;
            save(server);
        }
        if (!ModItems.getCurrencyItems().containsKey(this.defaultCurrency)) {
            MoneyMod.LOGGER.warn("[{}] Default currency '{}' invalid after config load, reset to EUR",
                    MoneyMod.MOD_ID, this.defaultCurrency);
            this.defaultCurrency = "EUR";
            save(server);
        }
    }
    public synchronized void save(MinecraftServer server) {
        Path configPath = getConfigPath(server);
        File configFile = configPath.toFile();
        if (configFile.exists()) {
            File bakFile = new File(configFile.getAbsolutePath() + CONFIG_BAK_SUFFIX);
            try {
                Files.copy(configFile.toPath(), bakFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                MoneyMod.LOGGER.warn("Couldn't create backup config file: {}", bakFile, e);
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
        } catch (Exception e) {
            MoneyMod.LOGGER.error("Failed to save config file: {}", configFile, e);
        }
    }
    private static Path getConfigPath(MinecraftServer server) {
        Path dir = server.getServerDirectory().toAbsolutePath().resolve("config");
        return dir.resolve(CONFIG_FILE_NAME);
    }

    public synchronized String getDefaultCurrency() {
        return defaultCurrency;
    }
    public synchronized void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }
    public synchronized int getConfigVersion() {
        return configVersion;
    }
    public synchronized void setConfigVersion(int version) {
        this.configVersion = version;
    }

    public synchronized Map<String, Double> getExchangeRates() {
        return new HashMap<>(exchangeRates);
    }
    public synchronized void setExchangeRates(Map<String, Double> rates) {
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
    }
    public synchronized long getLastRatesUpdateEpoch() {
        return lastRatesUpdateEpoch;
    }

    public synchronized String getLastRatesUpdateReadable() {
        return lastRatesUpdateReadable;
    }

    public synchronized void loadWithMigration(MinecraftServer server) {
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
                    MoneyMod.LOGGER.error("Failed to migrate config from legacy location.", e);
                }
                save(server);
            }
        }
        load(server);
    }
    private static Path getLegacyConfigPath(MinecraftServer server) {
        return server.getWorldPath(new net.minecraft.world.level.storage.LevelResource("data"))
                .resolve(CONFIG_FILE_NAME);
    }
    public synchronized String getServerCountryCode() {
        return serverCountryCode;
    }

    public synchronized void setServerCountryCode(String serverCountryCode) {
        this.serverCountryCode = serverCountryCode;
    }
    public synchronized String getAdminResetPassword() {
        return adminResetPassword;
    }

    public synchronized void setAdminResetPassword(String adminResetPassword) {
        this.adminResetPassword = adminResetPassword;
    }
}