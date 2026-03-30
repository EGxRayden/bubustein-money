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

import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.bank.BankAccount;
import tk.bubustein.money.bank.BankAccountSavedData;
import tk.bubustein.money.bank.PendingTransfer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PlayerJoinHandler {

    public static void register() {
        PlayerEvent.PLAYER_JOIN.register(PlayerJoinHandler::onPlayerJoin);
    }

    private static void onPlayerJoin(ServerPlayer player) {
        BankAccountSavedData data = BankAccountSavedData.get(Objects.requireNonNull(player.getServer()));
        List<PendingTransfer> pending = data.getPendingTransfers(player.getUUID());
        if (pending.isEmpty()) {
            return;
        }
        MoneyMod.LOGGER.info("[{}] Processing {} pending transfer(s) for player {}",
                MoneyMod.MOD_ID, pending.size(), player.getGameProfile().getName());
        int successCount = 0;
        int failCount = 0;

        List<PendingTransfer> failedTransfers = new java.util.ArrayList<>();

        for (PendingTransfer transfer : pending) {
            Optional<BankAccount> optAcc = data.getByIban(transfer.getTargetIban());

            if (optAcc.isEmpty()) {
                MoneyMod.LOGGER.error("[{}] Pending transfer target account not found: {}",
                        MoneyMod.MOD_ID, transfer.getTargetIban());
                failedTransfers.add(transfer);
                failCount++;
                continue;
            }
            BankAccount acc = optAcc.get();
            if (!acc.isActive()) {
                MoneyMod.LOGGER.warn("[{}] Pending transfer target account inactive: {}",
                        MoneyMod.MOD_ID, transfer.getTargetIban());
                failedTransfers.add(transfer);
                failCount++;
                continue;
            }
            try {
                acc.deposit(transfer.getAmount());
            } catch (IllegalArgumentException e) {
                MoneyMod.LOGGER.error("[{}] Failed to deposit pending transfer {} {} to {}: {}",
                        MoneyMod.MOD_ID, transfer.getAmount(), transfer.getCurrency(),
                        transfer.getTargetIban(), e.getMessage());
                failedTransfers.add(transfer);
                failCount++;
                continue;
            }
            player.sendSystemMessage(Component.translatable(
                    "message.bubusteinmoneymod.transfer.received",
                    CardUtils.formatMoney(transfer.getAmount()),
                    transfer.getCurrency(),
                    transfer.getSenderName(),
                    transfer.getTargetIban()
            ).withStyle(ChatFormatting.GREEN));

            successCount++;

            MoneyMod.LOGGER.info("[{}] Processed pending transfer: {} {} to {} from {}",
                    MoneyMod.MOD_ID, transfer.getAmount(), transfer.getCurrency(),
                    transfer.getTargetIban(), transfer.getSenderName());
        }

        data.clearPendingTransfers(player.getUUID());
        for (PendingTransfer failed : failedTransfers) {
            data.addPendingTransfer(failed);
        }
        if (successCount > 0) {
            player.sendSystemMessage(Component.translatable(
                    "message.bubusteinmoneymod.transfer.processedsummary",
                    successCount
            ).withStyle(ChatFormatting.GOLD));
        }

        if (failCount > 0) {
            player.sendSystemMessage(Component.translatable(
                    "message.bubusteinmoneymod.transfer.failedcount",
                    failCount
            ).withStyle(ChatFormatting.RED));
        }
    }
}
