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

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import tk.bubustein.money.MoneyMod;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(MoneyMod.MOD_ID, Registries.RECIPE_SERIALIZER);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(MoneyMod.MOD_ID, Registries.RECIPE_TYPE);
    public static final RegistrySupplier<RecipeType<BankMachineRecipe>> BANK_MACHINE_RECIPE = RECIPE_TYPES.register("bank_machine", () -> new RecipeType<>() {
        @Override
        public int hashCode() {
            return super.hashCode();
        }
        @Override
        public String toString(){
            return super.toString();
        }
    });
    public static final RegistrySupplier<RecipeSerializer<BankMachineRecipeShaped>> BANK_MACHINE_SHAPED = SERIALIZERS.register("bank_machine_shaped", () -> BankMachineRecipeShaped.Serializer.INSTANCE);
    public static final RegistrySupplier<RecipeSerializer<BankMachineRecipeShapeless>> BANK_MACHINE_SHAPELESS = SERIALIZERS.register("bank_machine_shapeless", () -> BankMachineRecipeShapeless.Serializer.INSTANCE);
    public static void init(){
        SERIALIZERS.register();
        RECIPE_TYPES.register();
    }
}