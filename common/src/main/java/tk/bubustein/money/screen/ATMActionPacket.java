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

package tk.bubustein.money.screen;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import tk.bubustein.money.MoneyMod;

public record ATMActionPacket(int action, double amount) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ATMActionPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "atm_action")
            );
    public static final StreamCodec<FriendlyByteBuf, ATMActionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,    ATMActionPacket::action,
                    ByteBufCodecs.DOUBLE, ATMActionPacket::amount,
                    ATMActionPacket::new
            );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void register() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                TYPE,
                STREAM_CODEC,
                (packet, context) -> context.queue(() -> {
                    Player player = context.getPlayer();
                    if (player instanceof ServerPlayer serverPlayer) {
                        if (serverPlayer.containerMenu instanceof ATMMenu atmMenu) {
                            if (packet.action() == 0) atmMenu.executeDeposit(packet.amount());
                            else if (packet.action() == 1) atmMenu.executeWithdraw(packet.amount());
                        }
                    }
                })
        );
    }
    public static void send(int action, double amount) {
        NetworkManager.sendToServer(new ATMActionPacket(action, amount));
    }
}