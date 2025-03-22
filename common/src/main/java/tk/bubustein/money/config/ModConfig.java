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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class ModConfig {
    private static final String CONFIG_FILE_NAME = "bubusteinmoneymod-config.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String defaultCurrency = "EUR";
    private static ModConfig instance;
    private ModConfig() {}
    public static ModConfig getInstance() {
        if (instance == null) {
            instance = new ModConfig();
        }
        return instance;
    }
    public void load(MinecraftServer server) {
        Path configPath = getConfigPath(server);
        File configFile = configPath.toFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                ModConfig loadedConfig = GSON.fromJson(reader, ModConfig.class);
                this.defaultCurrency = loadedConfig.defaultCurrency;
            } catch (IOException e) {
                MoneyMod.LOGGER.error("Failed to load config file", e);
            }
        } else {
            save(server);
        }
    }
    public void save(MinecraftServer server) {
        Path configPath = getConfigPath(server);
        File configFile = configPath.toFile();
        try {
            File parentDir = configFile.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
                }
            }
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config file ", e);
        }
    }
    private static Path getConfigPath(MinecraftServer server) {
        return server.getWorldPath(new LevelResource("data")).resolve(CONFIG_FILE_NAME);
    }
    public String getDefaultCurrency() {
        return defaultCurrency;
    }
    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }
}