package tk.bubustein.money.util;

import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.bank.BankAccount;
import tk.bubustein.money.bank.BankAccountSavedData;
import tk.bubustein.money.bank.PendingTransfer;
import tk.bubustein.money.item.CardItem;

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

        for (PendingTransfer transfer : pending) {
            Optional<BankAccount> optAcc = data.getByIban(transfer.getTargetIban());

            if (optAcc.isEmpty()) {
                MoneyMod.LOGGER.error("[{}] Pending transfer target account not found: {}",
                        MoneyMod.MOD_ID, transfer.getTargetIban());
                failCount++;
                continue;
            }
            BankAccount acc = optAcc.get();
            if (!acc.isActive()) {
                MoneyMod.LOGGER.warn("[{}] Pending transfer target account inactive: {}",
                        MoneyMod.MOD_ID, transfer.getTargetIban());
                failCount++;
                continue;
            }
            acc.deposit(transfer.getAmount());
            player.sendSystemMessage(Component.translatable(
                    "message.bubusteinmoneymod.transfer.received",
                    CardItem.formatMoney(transfer.getAmount()),
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
