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

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import tk.bubustein.money.MoneyMod;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(MoneyMod.MOD_ID, Registries.MENU);
    public static final RegistrySupplier<MenuType<BankMachineMenu>> BANK_MACHINE_MENU = MENUS.register("bank_machine_menu",
            () -> MenuRegistry.ofExtended((id, inventory, buf) -> new BankMachineMenu(id, inventory)));
    public static final RegistrySupplier<MenuType<ATMMenu>> ATM_MENU = MENUS.register("atm_menu",
            () -> MenuRegistry.ofExtended((id, inventory, buf) -> new ATMMenu(id, inventory)));
    public static void init(){
        MENUS.register();
    }
}