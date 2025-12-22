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
package tk.bubustein.money.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.item.ModItems;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ExchangeRateFetcher {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ECB_XML_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

    public static CompletableFuture<Map<String, Double>> fetchRatesAsync(Map<String, Double> existingRates) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Double> mergedRates = new HashMap<>(getFallbackRates());
            if (existingRates != null && !existingRates.isEmpty()) {
                mergedRates.putAll(existingRates);
            }
            try {
                LOGGER.info("[{}] Fetching exchange rates from ECB...", MoneyMod.MOD_ID);
                HttpURLConnection conn = (HttpURLConnection) URI.create(ECB_XML_URL).toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("User-Agent", "MinecraftMod/BubusteinMoneyMod");

                if (conn.getResponseCode() != 200) {
                    LOGGER.warn("[{}] ECB API returned HTTP {}", MoneyMod.MOD_ID, conn.getResponseCode());
                    return filterValidCurrencies(mergedRates);
                }
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(conn.getInputStream());
                NodeList cubeNodes = doc.getElementsByTagName("Cube");
                int updatedCount = 0;
                int skippedCount = 0;
                mergedRates.put("EUR", 1.0);
                for (int i = 0; i < cubeNodes.getLength(); i++) {
                    Element cube = (Element) cubeNodes.item(i);
                    if (cube.hasAttribute("currency") && cube.hasAttribute("rate")) {
                        String currency = cube.getAttribute("currency");
                        if (ModItems.getCurrencyItems().containsKey(currency)) {
                            double rate = Double.parseDouble(cube.getAttribute("rate"));
                            mergedRates.put(currency, rate);
                            updatedCount++;
                        } else {
                            skippedCount++;
                        }
                    }
                }
                conn.disconnect();
                LOGGER.info("[{}] Successfully updated {} exchange rates from ECB ({} skipped, total: {} mod currencies)",
                        MoneyMod.MOD_ID, updatedCount, skippedCount, mergedRates.size());

            } catch (Exception e) {
                LOGGER.error("[{}] Failed to fetch exchange rates from ECB, using existing/fallback",
                        MoneyMod.MOD_ID, e);
            }

            return filterValidCurrencies(mergedRates);
        });
    }
    private static Map<String, Double> filterValidCurrencies(Map<String, Double> rates) {
        Map<String, Double> filtered = new HashMap<>();
        for (Map.Entry<String, Double> entry : rates.entrySet()) {
            if (ModItems.getCurrencyItems().containsKey(entry.getKey())) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }
    public static Map<String, Double> getFallbackRates() {
        Map<String, Double> fallback = new HashMap<>();
        fallback.put("EUR", 1.00);
        fallback.put("USD", 1.16);
        fallback.put("GBP", 0.86);
        fallback.put("CAD", 1.61);
        fallback.put("RON", 5.05);
        fallback.put("MDL", 19.74);      // Nu e în ECB
        fallback.put("CHF", 0.94);
        fallback.put("AUD", 1.80);
        fallback.put("JPY", 171.74);
        fallback.put("CZK", 24.48);
        fallback.put("NOK", 11.94);
        fallback.put("DKK", 7.46);
        fallback.put("SEK", 11.17);
        fallback.put("HUF", 394.74);
        fallback.put("PLN", 4.25);
        fallback.put("RSD", 117.38);  // Nu e in ECB
        fallback.put("ISK", 143.4);
        fallback.put("CNY", 8.35);
        fallback.put("INR", 101.27);
        fallback.put("KRW", 1627.91);
        fallback.put("BRL", 6.40);
        fallback.put("MXN", 21.86);
        fallback.put("ZAR", 20.59);
        fallback.put("TRY", 47.61);
        fallback.put("NZD", 1.99);
        fallback.put("PHP", 66.30);
        fallback.put("EGP", 55.72);    // Nu e în ECB
        return fallback;
    }
}