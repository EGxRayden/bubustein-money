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

package tk.bubustein.money.block.custom;

import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import tk.bubustein.money.item.KeyItem;
import tk.bubustein.money.screen.BankMachineMenu;

public class BankMachine extends Block {
    public static Component TITLE = Component.translatable("block.bubusteinmoneymod.bank_machine");
    public BankMachine() {
        super(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).strength(2.5f).requiresCorrectToolForDrops());
    }
    @Override
    public @NotNull ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack stack = player.getItemInHand(interactionHand);
        if(!level.isClientSide){
            if(stack.getItem() instanceof KeyItem){
                stack.hurtAndBreak(1, player,
                        interactionHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                MenuRegistry.openExtendedMenu((ServerPlayer) player, blockState.getMenuProvider(level, blockPos), friendlyByteBuf -> {});
                return ItemInteractionResult.CONSUME;
            } else {
                player.sendSystemMessage(Component.translatable("message.bubusteinmoneymod.missing_key").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                return ItemInteractionResult.FAIL;
            }
        }
        return ItemInteractionResult.SUCCESS;
    }
    @Override
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        return new SimpleMenuProvider((i, inventory, player) ->
                new BankMachineMenu(i, inventory, ContainerLevelAccess.create(level, blockPos)), TITLE);
    }
}