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
 */

package tk.bubustein.money.bank;

import java.util.UUID;

public class PendingTransfer {
    private UUID recipientUuid;
    private String targetIban;
    private double amount;
    private String currency;
    private String senderName;
    private long timestamp;

    public PendingTransfer() {
    }

    public PendingTransfer(UUID recipientUuid, String targetIban, double amount,
                           String currency, String senderName) {
        this.recipientUuid = recipientUuid;
        this.targetIban = targetIban;
        this.amount = amount;
        this.currency = currency;
        this.senderName = senderName;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getRecipientUuid() {
        return recipientUuid;
    }

    public String getTargetIban() {
        return targetIban;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getSenderName() {
        return senderName;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
