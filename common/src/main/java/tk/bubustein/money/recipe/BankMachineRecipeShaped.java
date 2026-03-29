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

package tk.bubustein.money.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import tk.bubustein.money.bank.*;
import tk.bubustein.money.item.CardItem;
import tk.bubustein.money.util.CardUtils;
import java.util.UUID;

public class BankMachineRecipeShaped implements BankMachineRecipe {
    final ShapedRecipePattern pattern;
    final ItemStack result;
    final String group;
    final boolean showNotification;
    public BankMachineRecipeShaped(String string, ShapedRecipePattern shapedRecipePattern, ItemStack itemStack, boolean bl) {
        this.group = string;
        this.pattern = shapedRecipePattern;
        this.result = itemStack;
        this.showNotification = bl;
    }
    public BankMachineRecipeShaped(String string, ShapedRecipePattern shapedRecipePattern, ItemStack itemStack) {
        this(string, shapedRecipePattern, itemStack, true);
    }
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.BANK_MACHINE_SHAPED.get();
    }
    public @NotNull String getGroup() {
        return this.group;
    }
    public @NotNull ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result;
    }
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.pattern.ingredients();
    }
    public boolean showNotification() {
        return this.showNotification;
    }
    public boolean canCraftInDimensions(int i, int j) {
        return i >= this.pattern.width() && j >= this.pattern.height();
    }
    public boolean matches(CraftingInput craftingInput, Level level) {
        return this.pattern.matches(craftingInput);
    }
    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        ItemStack result = this.getResultItem(provider).copy();
        if (!(result.getItem() instanceof CardItem)) {
            return result;
        }
        ItemStack source = input.getItem(4);
        if (!(source.getItem() instanceof CardItem)) {
            return result;
        }
        String iban = CardUtils.getIban(source);
        UUID owner = CardUtils.getOwner(source);
        String ownerName = CardUtils.getOwnerName(source);
        AccountKind kind = CardUtils.getAccountKind(source);

        if (iban == null || iban.isEmpty() || owner == null) {
            return result;
        }
        CardUtils.setIban(result, iban);
        CardUtils.setOwner(result, owner);
        CardUtils.setOwnerName(result, ownerName);
        CardUtils.setAccountKind(result, kind);
        Double sourceBalance = source.get(CardUtils.MONEY_COMPONENT.get());
        String sourceCurrency = source.get(CardUtils.CURRENCY_COMPONENT.get());
        if (sourceBalance != null) {
            result.set(CardUtils.MONEY_COMPONENT.get(), sourceBalance);
        }
        if (sourceCurrency != null) {
            result.set(CardUtils.CURRENCY_COMPONENT.get(), sourceCurrency);
        }
        if (result.getItem() instanceof CardItem) {
            String newTier = CardUtils.getDebitTierFromItem(result).name();
            String cardNameKey = "item.bubusteinmoneymod." + newTier.toLowerCase() + "_card.named";
            result.set(DataComponents.CUSTOM_NAME,
                    Component.translatable(cardNameKey, ownerName));
        }
        return result;
    }
    @Override
    public boolean isShapeless() {
        return false;
    }
    public int getWidth() {
        return this.pattern.width();
    }
    public int getHeight() {
        return this.pattern.height();
    }
    public boolean isIncomplete() {
        NonNullList<Ingredient> nonNullList = this.getIngredients();
        return nonNullList.isEmpty() || nonNullList.stream().filter((ingredient) -> !ingredient.isEmpty()).anyMatch((ingredient) -> ingredient.getItems().length == 0);
    }
    public static class Serializer implements RecipeSerializer<BankMachineRecipeShaped> {
        public static final Serializer INSTANCE = new Serializer();
        public static final MapCodec<BankMachineRecipeShaped> CODEC = RecordCodecBuilder.mapCodec((instance) ->
                instance.group(Codec.STRING.optionalFieldOf("group", "")
                        .forGetter((shapedRecipe) -> shapedRecipe.group),
                        ShapedRecipePattern.MAP_CODEC.forGetter((shapedRecipe) -> shapedRecipe.pattern),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter((shapedRecipe) -> shapedRecipe.result),
                        Codec.BOOL.optionalFieldOf("show_notification", true)
                                .forGetter((shapedRecipe) -> shapedRecipe.showNotification)).apply(instance, BankMachineRecipeShaped::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, BankMachineRecipeShaped> STREAM_CODEC = StreamCodec.of(BankMachineRecipeShaped.Serializer::toNetwork, BankMachineRecipeShaped.Serializer::fromNetwork);

        public Serializer() {
        }
        public @NotNull MapCodec<BankMachineRecipeShaped> codec() {
            return CODEC;
        }
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BankMachineRecipeShaped> streamCodec() {
            return STREAM_CODEC;
        }
        private static BankMachineRecipeShaped fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            String string = registryFriendlyByteBuf.readUtf();
            ShapedRecipePattern shapedRecipePattern = ShapedRecipePattern.STREAM_CODEC.decode(registryFriendlyByteBuf);
            ItemStack itemStack = ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
            boolean bl = registryFriendlyByteBuf.readBoolean();
            return new BankMachineRecipeShaped(string, shapedRecipePattern, itemStack, bl);
        }
        private static void toNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf, BankMachineRecipeShaped shapedRecipe) {
            registryFriendlyByteBuf.writeUtf(shapedRecipe.group);
            ShapedRecipePattern.STREAM_CODEC.encode(registryFriendlyByteBuf, shapedRecipe.pattern);
            ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, shapedRecipe.result);
            registryFriendlyByteBuf.writeBoolean(shapedRecipe.showNotification);
        }
    }
}