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

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import tk.bubustein.money.screen.ATMMenu;

public class ATM extends HorizontalDirectionalBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public static final MapCodec<ATM> CODEC = ATM.simpleCodec(ATM::new);
    public ATM(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER));
    }
    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
        builder.add(FACING, HALF);
    }
    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(HALF, DoubleBlockHalf.LOWER);
    }
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        if(world.getBlockState(pos.above()).getBlock() != Blocks.AIR){
            world.removeBlockEntity(pos);
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        world.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(FACING, state.getValue(FACING)), 3);
        world.blockUpdated(pos, this);
    }

    // ── ATM GUI ───────────────────────────────────────────────────────────
    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                        Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
            ContainerLevelAccess access = ContainerLevelAccess.create(level, lowerPos);
            MenuProvider menuProvider = new SimpleMenuProvider(
                    (id, inv, p) -> new ATMMenu(id, inv, access),
                    Component.translatable("block.bubusteinmoneymod.atm"));
            MenuRegistry.openExtendedMenu(serverPlayer, menuProvider, buf -> {});
        }
        return InteractionResult.CONSUME;
    }

    @SuppressWarnings("deprecated")
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
    @SuppressWarnings("deprecated")
    @Override
    public @NotNull BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player playerEntity) {
        BlockPos blockpos = pos.below();
        BlockState blockState = world.getBlockState(blockpos);
        if(state.getBlock() == this && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            world.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
            world.levelEvent(playerEntity, LevelEvent.PARTICLES_DESTROY_BLOCK, blockpos, Block.getId(blockState));
        } else if(state.getBlock() == this && state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            blockpos = pos.above();
            blockState = world.getBlockState(blockpos);
            world.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
            world.levelEvent(playerEntity, LevelEvent.PARTICLES_DESTROY_BLOCK, blockpos, Block.getId(blockState));
        }
        super.playerWillDestroy(world, pos, state, playerEntity);
        return blockState;
    }
}
