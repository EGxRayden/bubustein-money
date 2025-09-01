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

package tk.bubustein.money.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.block.ModBlocks;
import tk.bubustein.money.item.ModItems;
import tk.bubustein.money.recipe.BankMachineRecipeShapedBuilder;
import tk.bubustein.money.recipe.BankMachineRecipeShapelessBuilder;
import java.util.concurrent.CompletableFuture;

public class MoneyRecipeDataGen extends FabricRecipeProvider {
    public MoneyRecipeDataGen(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture){
        super(output, registriesFuture);
    }
    public static void fiveItems(RecipeOutput recipeOutput, ItemLike output, ItemLike input) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output)
                .define('#', input)
                .pattern("# #")
                .pattern("###")
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput);
    }
    public static void twoItems(RecipeOutput recipeOutput, ItemLike output, ItemLike input) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output)
                .define('#', input)
                .pattern("##")
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput);
    }
    public static void fourItems(RecipeOutput recipeOutput, ItemLike output, ItemLike input) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output)
                .define('#', input)
                .pattern("##")
                .pattern("##")
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput);
    }
    public static void oneToOneConversionRecipe(RecipeOutput recipeOutput, ItemLike output, ItemLike input, String group, int count) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, output, count)
                .requires(input)
                .group(group)
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID,getConversionRecipeName(output, input)));
    }
    public static void fiveItems1(RecipeOutput recipeOutput, ItemLike output, ItemLike input) {
        BankMachineRecipeShapedBuilder.shaped(output)
                .define('#', input)
                .pattern("# #")
                .pattern("###")
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput);
    }
    public static void twoItems1(RecipeOutput recipeOutput, ItemLike output, ItemLike input) {
        BankMachineRecipeShapedBuilder.shaped(output)
                .define('#', input)
                .pattern("##")
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput);
    }
    public static void fourItems1(RecipeOutput recipeOutput, ItemLike output, ItemLike input) {
        BankMachineRecipeShapedBuilder.shaped(output)
                .define('#', input)
                .pattern("##")
                .pattern("##")
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput);
    }
    public static void conversionRecipe1(RecipeOutput recipeOutput, ItemLike output, ItemLike input, String group, int count) {
        BankMachineRecipeShapelessBuilder.shapeless(output, count)
                .requires(input)
                .group(group)
                .unlockedBy(getHasName(input), has(input))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID,getConversionRecipeName(output, input)));
    }
    @Override
    public void buildRecipes(RecipeOutput exporter){
        // EUR
        oneToOneConversionRecipe(exporter, ModItems.Euro100.get(), ModItems.Euro500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Euro100.get(), ModItems.Euro200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Euro50.get(), ModItems.Euro100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Euro10.get(), ModItems.Euro50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Euro10.get(), ModItems.Euro20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Euro5.get(), ModItems.Euro10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Euro1.get(), ModItems.Euro5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Euro1.get(), ModItems.Euro2.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Ecent50.get(), ModItems.Euro1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Ecent10.get(), ModItems.Ecent50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Ecent10.get(), ModItems.Ecent20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Ecent5.get(), ModItems.Ecent10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Ecent1.get(), ModItems.Ecent5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Ecent1.get(), ModItems.Ecent2.get(), "", 2);
        twoItems(exporter, ModItems.Ecent2.get(), ModItems.Ecent1.get());
        fiveItems(exporter, ModItems.Ecent5.get(), ModItems.Ecent1.get());
        twoItems(exporter, ModItems.Ecent10.get(), ModItems.Ecent5.get());
        twoItems(exporter, ModItems.Ecent20.get(), ModItems.Ecent10.get());
        fiveItems(exporter, ModItems.Ecent50.get(), ModItems.Ecent10.get());
        twoItems(exporter, ModItems.Euro1.get(), ModItems.Ecent50.get());
        twoItems(exporter, ModItems.Euro2.get(), ModItems.Euro1.get());
        fiveItems(exporter, ModItems.Euro5.get(), ModItems.Euro1.get());
        twoItems(exporter, ModItems.Euro10.get(), ModItems.Euro5.get());
        twoItems(exporter, ModItems.Euro20.get(), ModItems.Euro10.get());
        fiveItems(exporter, ModItems.Euro50.get(), ModItems.Euro10.get());
        twoItems(exporter, ModItems.Euro100.get(), ModItems.Euro50.get());
        twoItems(exporter, ModItems.Euro200.get(), ModItems.Euro100.get());
        fiveItems(exporter, ModItems.Euro500.get(), ModItems.Euro100.get());

        // USD
        oneToOneConversionRecipe(exporter, ModItems.Dollar50.get(), ModItems.Dollar100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Dollar10.get(), ModItems.Dollar50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Dollar10.get(), ModItems.Dollar20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Dollar5.get(), ModItems.Dollar10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Dollar1.get(), ModItems.Dollar5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Cent50.get(), ModItems.Dollar1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Cent25.get(), ModItems.Cent50.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Cent5.get(), ModItems.Cent25.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Cent5.get(), ModItems.Cent10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Cent1.get(), ModItems.Cent5.get(), "", 5);
        fiveItems(exporter, ModItems.Cent5.get(), ModItems.Cent1.get());
        twoItems(exporter, ModItems.Cent10.get(), ModItems.Cent5.get());
        fiveItems(exporter, ModItems.Cent25.get(), ModItems.Cent5.get());
        twoItems(exporter, ModItems.Cent50.get(), ModItems.Cent25.get());
        twoItems(exporter, ModItems.Dollar1.get(), ModItems.Cent50.get());
        fiveItems(exporter, ModItems.Dollar5.get(), ModItems.Dollar1.get());
        twoItems(exporter, ModItems.Dollar10.get(), ModItems.Dollar5.get());
        twoItems(exporter, ModItems.Dollar20.get(), ModItems.Dollar10.get());
        fiveItems(exporter, ModItems.Dollar50.get(), ModItems.Dollar10.get());
        twoItems(exporter, ModItems.Dollar100.get(), ModItems.Dollar50.get());

        // RON
        oneToOneConversionRecipe(exporter, ModItems.Lei100.get(), ModItems.Lei500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Lei100.get(), ModItems.Lei200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Lei50.get(), ModItems.Lei100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Lei10.get(), ModItems.Lei50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Lei10.get(), ModItems.Lei20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Lei5.get(), ModItems.Lei10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Leu1.get(), ModItems.Lei5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Bani50.get(), ModItems.Leu1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Bani10.get(), ModItems.Bani50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Bani5.get(), ModItems.Bani10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Ban1.get(), ModItems.Bani5.get(), "", 5);
        fiveItems(exporter, ModItems.Bani5.get(), ModItems.Ban1.get());
        twoItems(exporter, ModItems.Bani10.get(), ModItems.Bani5.get());
        fiveItems(exporter, ModItems.Bani50.get(), ModItems.Bani10.get());
        twoItems(exporter, ModItems.Leu1.get(), ModItems.Bani50.get());
        fiveItems(exporter, ModItems.Lei5.get(), ModItems.Leu1.get());
        twoItems(exporter, ModItems.Lei10.get(), ModItems.Lei5.get());
        twoItems(exporter, ModItems.Lei20.get(), ModItems.Lei10.get());
        fiveItems(exporter, ModItems.Lei50.get(), ModItems.Lei10.get());
        twoItems(exporter, ModItems.Lei100.get(), ModItems.Lei50.get());
        twoItems(exporter, ModItems.Lei200.get(), ModItems.Lei100.get());
        fiveItems(exporter, ModItems.Lei500.get(), ModItems.Lei100.get());

        // GBP
        oneToOneConversionRecipe(exporter, ModItems.Pound10.get(), ModItems.Pound50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Pound10.get(), ModItems.Pound20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Pound5.get(), ModItems.Pound10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Pound1.get(), ModItems.Pound5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Pound1.get(), ModItems.Pound2.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Pence50.get(), ModItems.Pound1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Pence10.get(), ModItems.Pence50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Pence10.get(), ModItems.Pence20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Pence5.get(), ModItems.Pence10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Pence1.get(), ModItems.Pence5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Pence1.get(), ModItems.Pence2.get(), "", 2);
        twoItems(exporter, ModItems.Pence2.get(), ModItems.Pence1.get());
        fiveItems(exporter, ModItems.Pence5.get(), ModItems.Pence1.get());
        twoItems(exporter, ModItems.Pence10.get(), ModItems.Pence5.get());
        twoItems(exporter, ModItems.Pence20.get(), ModItems.Pence10.get());
        fiveItems(exporter, ModItems.Pence50.get(), ModItems.Pence10.get());
        twoItems(exporter, ModItems.Pound1.get(), ModItems.Pence50.get());
        twoItems(exporter, ModItems.Pound2.get(), ModItems.Pound1.get());
        fiveItems(exporter, ModItems.Pound5.get(), ModItems.Pound1.get());
        twoItems(exporter, ModItems.Pound10.get(), ModItems.Pound5.get());
        twoItems(exporter, ModItems.Pound20.get(), ModItems.Pound10.get());
        fiveItems(exporter, ModItems.Pound50.get(), ModItems.Pound10.get());

        // CAD
        oneToOneConversionRecipe(exporter, ModItems.DollarC50.get(), ModItems.DollarC100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.DollarC10.get(), ModItems.DollarC50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.DollarC10.get(), ModItems.DollarC20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.DollarC5.get(), ModItems.DollarC10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Loonie.get(), ModItems.DollarC5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Loonie.get(), ModItems.Toonie.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.CCent25.get(), ModItems.Loonie.get(), "", 4);
        oneToOneConversionRecipe(exporter, ModItems.CCent5.get(), ModItems.CCent25.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.CCent5.get(), ModItems.CCent10.get(), "", 2);
        twoItems(exporter, ModItems.CCent10.get(), ModItems.CCent5.get());
        fiveItems(exporter, ModItems.CCent25.get(), ModItems.CCent5.get());
        fourItems(exporter, ModItems.Loonie.get(), ModItems.CCent25.get());
        twoItems(exporter, ModItems.Toonie.get(), ModItems.Loonie.get());
        fiveItems(exporter, ModItems.DollarC5.get(), ModItems.Loonie.get());
        twoItems(exporter, ModItems.DollarC10.get(), ModItems.DollarC5.get());
        twoItems(exporter, ModItems.DollarC20.get(), ModItems.DollarC10.get());
        fiveItems(exporter, ModItems.DollarC50.get(), ModItems.DollarC10.get());
        twoItems(exporter, ModItems.DollarC100.get(), ModItems.DollarC50.get());

        // MDL
        oneToOneConversionRecipe(exporter, ModItems.LeiMD500.get(), ModItems.LeiMD1000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.LeiMD100.get(), ModItems.LeiMD500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.LeiMD100.get(), ModItems.LeiMD200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.LeiMD50.get(), ModItems.LeiMD100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.LeiMD10.get(), ModItems.LeiMD50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.LeiMD10.get(), ModItems.LeiMD20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.LeiMD5.get(), ModItems.LeiMD10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.LeuMD1.get(), ModItems.LeiMD5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.LeuMD1.get(), ModItems.LeuMD2.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.BaniMD50.get(), ModItems.LeuMD1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.BaniMD25.get(), ModItems.BaniMD50.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.BanMD5.get(), ModItems.BaniMD25.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.BanMD5.get(), ModItems.BaniMD10.get(), "", 2);
        twoItems(exporter, ModItems.BaniMD10.get(), ModItems.BanMD5.get());
        fiveItems(exporter, ModItems.BaniMD25.get(), ModItems.BanMD5.get());
        twoItems(exporter, ModItems.BaniMD50.get(), ModItems.BaniMD25.get());
        twoItems(exporter, ModItems.LeuMD1.get(), ModItems.BaniMD50.get());
        twoItems(exporter, ModItems.LeuMD2.get(), ModItems.LeuMD1.get());
        fiveItems(exporter, ModItems.LeiMD5.get(), ModItems.LeuMD1.get());
        twoItems(exporter, ModItems.LeiMD10.get(), ModItems.LeiMD5.get());
        twoItems(exporter, ModItems.LeiMD20.get(), ModItems.LeiMD10.get());
        fiveItems(exporter, ModItems.LeiMD50.get(), ModItems.LeiMD10.get());
        twoItems(exporter, ModItems.LeiMD100.get(), ModItems.LeiMD50.get());
        twoItems(exporter, ModItems.LeiMD200.get(), ModItems.LeiMD100.get());
        fiveItems(exporter, ModItems.LeiMD500.get(), ModItems.LeiMD100.get());
        twoItems(exporter, ModItems.LeiMD1000.get(), ModItems.LeiMD500.get());

        // CHF
        oneToOneConversionRecipe(exporter, ModItems.Franc200.get(), ModItems.Franc1000.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Franc100.get(), ModItems.Franc200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Franc50.get(), ModItems.Franc100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Franc10.get(), ModItems.Franc50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Franc10.get(), ModItems.Franc20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Franc5.get(), ModItems.Franc10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Franc1.get(), ModItems.Franc5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Franc1.get(), ModItems.Franc2.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.HalfFranc.get(), ModItems.Franc1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Centimes10.get(), ModItems.HalfFranc.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Centimes10.get(), ModItems.Centimes20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Centimes5.get(), ModItems.Centimes10.get(), "", 2);
        twoItems(exporter, ModItems.Centimes10.get(), ModItems.Centimes5.get());
        twoItems(exporter, ModItems.Centimes20.get(), ModItems.Centimes10.get());
        fiveItems(exporter, ModItems.HalfFranc.get(), ModItems.Centimes10.get());
        twoItems(exporter, ModItems.Franc1.get(), ModItems.HalfFranc.get());
        twoItems(exporter, ModItems.Franc2.get(), ModItems.Franc1.get());
        fiveItems(exporter, ModItems.Franc5.get(), ModItems.Franc1.get());
        twoItems(exporter, ModItems.Franc10.get(), ModItems.Franc5.get());
        twoItems(exporter, ModItems.Franc20.get(), ModItems.Franc10.get());
        fiveItems(exporter, ModItems.Franc50.get(), ModItems.Franc10.get());
        twoItems(exporter, ModItems.Franc100.get(), ModItems.Franc50.get());
        twoItems(exporter, ModItems.Franc200.get(), ModItems.Franc100.get());
        fiveItems(exporter, ModItems.Franc1000.get(), ModItems.Franc200.get());

        // AUD
        oneToOneConversionRecipe(exporter, ModItems.DollarA50.get(), ModItems.DollarA100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.DollarA10.get(), ModItems.DollarA50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.DollarA10.get(), ModItems.DollarA20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.DollarA5.get(), ModItems.DollarA10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.DollarA1.get(), ModItems.DollarA5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.DollarA1.get(), ModItems.DollarA2.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ACent50.get(), ModItems.DollarA1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ACent10.get(), ModItems.ACent50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.ACent10.get(), ModItems.ACent20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ACent5.get(), ModItems.ACent10.get(), "", 2);
        twoItems(exporter, ModItems.ACent10.get(), ModItems.ACent5.get());
        twoItems(exporter, ModItems.ACent20.get(), ModItems.ACent10.get());
        fiveItems(exporter, ModItems.ACent50.get(), ModItems.ACent10.get());
        twoItems(exporter, ModItems.DollarA1.get(), ModItems.ACent50.get());
        twoItems(exporter, ModItems.DollarA2.get(), ModItems.DollarA1.get());
        fiveItems(exporter, ModItems.DollarA5.get(), ModItems.DollarA1.get());
        twoItems(exporter, ModItems.DollarA10.get(), ModItems.DollarA5.get());
        twoItems(exporter, ModItems.DollarA20.get(), ModItems.DollarA10.get());
        fiveItems(exporter, ModItems.DollarA50.get(), ModItems.DollarA10.get());
        twoItems(exporter, ModItems.DollarA100.get(), ModItems.DollarA50.get());

        // JPY
        oneToOneConversionRecipe(exporter, ModItems.Yen5000.get(), ModItems.Yen10000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Yen1000.get(), ModItems.Yen5000.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Yen500.get(), ModItems.Yen1000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Yen100.get(), ModItems.Yen500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Yen50.get(), ModItems.Yen100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Yen10.get(), ModItems.Yen50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Yen5.get(), ModItems.Yen10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Yen1.get(), ModItems.Yen5.get(), "", 5);
        fiveItems(exporter, ModItems.Yen5.get(), ModItems.Yen1.get());
        twoItems(exporter, ModItems.Yen10.get(), ModItems.Yen5.get());
        fiveItems(exporter, ModItems.Yen50.get(), ModItems.Yen10.get());
        twoItems(exporter, ModItems.Yen100.get(), ModItems.Yen50.get());
        fiveItems(exporter, ModItems.Yen500.get(), ModItems.Yen100.get());
        twoItems(exporter, ModItems.Yen1000.get(), ModItems.Yen500.get());
        fiveItems(exporter, ModItems.Yen5000.get(), ModItems.Yen1000.get());
        twoItems(exporter, ModItems.Yen10000.get(), ModItems.Yen5000.get());

        // CZK
        oneToOneConversionRecipe(exporter, ModItems.CZkr1000.get(), ModItems.CZkr5000.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.CZkr1000.get(), ModItems.CZkr2000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.CZkr500.get(), ModItems.CZkr1000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.CZkr100.get(), ModItems.CZkr500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.CZkr100.get(), ModItems.CZkr200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.CZkr50.get(), ModItems.CZkr100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.CZkr10.get(), ModItems.CZkr50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.CZkr10.get(), ModItems.CZkr20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.CZkr5.get(), ModItems.CZkr10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.CZkr1.get(), ModItems.CZkr5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.CZkr1.get(), ModItems.CZkr2.get(), "", 2);
        twoItems(exporter, ModItems.CZkr2.get(), ModItems.CZkr1.get());
        fiveItems(exporter, ModItems.CZkr5.get(), ModItems.CZkr1.get());
        twoItems(exporter, ModItems.CZkr10.get(), ModItems.CZkr5.get());
        twoItems(exporter, ModItems.CZkr20.get(), ModItems.CZkr10.get());
        fiveItems(exporter, ModItems.CZkr50.get(), ModItems.CZkr10.get());
        twoItems(exporter, ModItems.CZkr100.get(), ModItems.CZkr50.get());
        twoItems(exporter, ModItems.CZkr200.get(), ModItems.CZkr100.get());
        fiveItems(exporter, ModItems.CZkr500.get(), ModItems.CZkr100.get());
        twoItems(exporter, ModItems.CZkr1000.get(), ModItems.CZkr500.get());
        twoItems(exporter, ModItems.CZkr2000.get(), ModItems.CZkr1000.get());
        fiveItems(exporter, ModItems.CZkr5000.get(), ModItems.CZkr1000.get());

        // NOK
        oneToOneConversionRecipe(exporter, ModItems.NOkr500.get(), ModItems.NOkr1000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.NOkr100.get(), ModItems.NOkr500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.NOkr100.get(), ModItems.NOkr200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.NOkr50.get(), ModItems.NOkr100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.NOkr10.get(), ModItems.NOkr50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.NOkr10.get(), ModItems.NOkr20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.NOkr5.get(), ModItems.NOkr10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.NOkr1.get(), ModItems.NOkr5.get(), "", 5);
        fiveItems(exporter, ModItems.NOkr5.get(), ModItems.NOkr1.get());
        twoItems(exporter, ModItems.NOkr10.get(), ModItems.NOkr5.get());
        twoItems(exporter, ModItems.NOkr20.get(), ModItems.NOkr10.get());
        fiveItems(exporter, ModItems.NOkr50.get(), ModItems.NOkr10.get());
        twoItems(exporter, ModItems.NOkr100.get(), ModItems.NOkr50.get());
        twoItems(exporter, ModItems.NOkr200.get(), ModItems.NOkr100.get());
        fiveItems(exporter, ModItems.NOkr500.get(), ModItems.NOkr100.get());
        twoItems(exporter, ModItems.NOkr1000.get(), ModItems.NOkr500.get());

        // DKK
        oneToOneConversionRecipe(exporter, ModItems.DKkr500.get(), ModItems.DKkr1000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.DKkr100.get(), ModItems.DKkr500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.DKkr100.get(), ModItems.DKkr200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.DKkr50.get(), ModItems.DKkr100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.DKkr10.get(), ModItems.DKkr50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.DKkr10.get(), ModItems.DKkr20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.DKkr5.get(), ModItems.DKkr10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.DKkr1.get(), ModItems.DKkr5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.DKkr1.get(), ModItems.DKkr2.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.DKAere50.get(), ModItems.DKkr1.get(), "", 2);
        twoItems(exporter, ModItems.DKkr1.get(), ModItems.DKAere50.get());
        twoItems(exporter, ModItems.DKkr2.get(), ModItems.DKkr1.get());
        fiveItems(exporter, ModItems.DKkr5.get(), ModItems.DKkr1.get());
        twoItems(exporter, ModItems.DKkr10.get(), ModItems.DKkr5.get());
        twoItems(exporter, ModItems.DKkr20.get(), ModItems.DKkr10.get());
        fiveItems(exporter, ModItems.DKkr50.get(), ModItems.DKkr10.get());
        twoItems(exporter, ModItems.DKkr100.get(), ModItems.DKkr50.get());
        twoItems(exporter, ModItems.DKkr200.get(), ModItems.DKkr100.get());
        fiveItems(exporter, ModItems.DKkr500.get(), ModItems.DKkr100.get());
        twoItems(exporter, ModItems.DKkr1000.get(), ModItems.DKkr500.get());

        // HUF
        oneToOneConversionRecipe(exporter, ModItems.Ft10000.get(), ModItems.Ft20000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Ft5000.get(), ModItems.Ft10000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Ft1000.get(), ModItems.Ft5000.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Ft1000.get(), ModItems.Ft2000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Ft500.get(), ModItems.Ft1000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Ft100.get(), ModItems.Ft500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Ft100.get(), ModItems.Ft200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Ft50.get(), ModItems.Ft100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Ft10.get(), ModItems.Ft50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Ft10.get(), ModItems.Ft20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Ft5.get(), ModItems.Ft10.get(), "", 2);
        twoItems(exporter, ModItems.Ft10.get(), ModItems.Ft5.get());
        twoItems(exporter, ModItems.Ft20.get(), ModItems.Ft10.get());
        fiveItems(exporter, ModItems.Ft50.get(), ModItems.Ft10.get());
        twoItems(exporter, ModItems.Ft100.get(), ModItems.Ft50.get());
        twoItems(exporter, ModItems.Ft200.get(), ModItems.Ft100.get());
        fiveItems(exporter, ModItems.Ft500.get(), ModItems.Ft100.get());
        twoItems(exporter, ModItems.Ft1000.get(), ModItems.Ft500.get());
        twoItems(exporter, ModItems.Ft2000.get(), ModItems.Ft1000.get());
        fiveItems(exporter, ModItems.Ft5000.get(), ModItems.Ft1000.get());
        twoItems(exporter, ModItems.Ft10000.get(), ModItems.Ft5000.get());
        twoItems(exporter, ModItems.Ft20000.get(), ModItems.Ft10000.get());

        // PLN
        oneToOneConversionRecipe(exporter, ModItems.Zloty100.get(), ModItems.Zloty500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Zloty100.get(), ModItems.Zloty200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Zloty50.get(), ModItems.Zloty100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Zloty10.get(), ModItems.Zloty50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Zloty10.get(), ModItems.Zloty20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Zloty5.get(), ModItems.Zloty10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Zloty1.get(), ModItems.Zloty5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Zloty1.get(), ModItems.Zloty2.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Grosz50.get(), ModItems.Zloty1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Grosz10.get(), ModItems.Grosz50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Grosz10.get(), ModItems.Grosz20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Grosz5.get(), ModItems.Grosz10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Grosz1.get(), ModItems.Grosz5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Grosz1.get(), ModItems.Grosz2.get(), "", 2);
        twoItems(exporter, ModItems.Grosz2.get(), ModItems.Grosz1.get());
        fiveItems(exporter, ModItems.Grosz5.get(), ModItems.Grosz1.get());
        twoItems(exporter, ModItems.Grosz10.get(), ModItems.Grosz5.get());
        twoItems(exporter, ModItems.Grosz20.get(), ModItems.Grosz10.get());
        fiveItems(exporter, ModItems.Grosz50.get(), ModItems.Grosz10.get());
        twoItems(exporter, ModItems.Zloty1.get(), ModItems.Grosz50.get());
        twoItems(exporter, ModItems.Zloty2.get(), ModItems.Zloty1.get());
        fiveItems(exporter, ModItems.Zloty5.get(), ModItems.Zloty1.get());
        twoItems(exporter, ModItems.Zloty10.get(), ModItems.Zloty5.get());
        twoItems(exporter, ModItems.Zloty20.get(), ModItems.Zloty10.get());
        fiveItems(exporter, ModItems.Zloty50.get(), ModItems.Zloty10.get());
        twoItems(exporter, ModItems.Zloty100.get(), ModItems.Zloty50.get());
        twoItems(exporter, ModItems.Zloty200.get(), ModItems.Zloty100.get());
        fiveItems(exporter, ModItems.Zloty500.get(), ModItems.Zloty100.get());

        // RSD
        oneToOneConversionRecipe(exporter, ModItems.RSD1000.get(), ModItems.RSD5000.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.RSD1000.get(), ModItems.RSD2000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.RSD500.get(), ModItems.RSD1000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.RSD100.get(), ModItems.RSD500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.RSD100.get(), ModItems.RSD200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.RSD50.get(), ModItems.RSD100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.RSD10.get(), ModItems.RSD50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.RSD10.get(), ModItems.RSD20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.RSD5.get(), ModItems.RSD10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.RSD1.get(), ModItems.RSD5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.RSD1.get(), ModItems.RSD2.get(), "", 2);
        twoItems(exporter, ModItems.RSD2.get(), ModItems.RSD1.get());
        fiveItems(exporter, ModItems.RSD5.get(), ModItems.RSD1.get());
        twoItems(exporter, ModItems.RSD10.get(), ModItems.RSD5.get());
        twoItems(exporter, ModItems.RSD20.get(), ModItems.RSD10.get());
        fiveItems(exporter, ModItems.RSD50.get(), ModItems.RSD10.get());
        twoItems(exporter, ModItems.RSD100.get(), ModItems.RSD50.get());
        twoItems(exporter, ModItems.RSD200.get(), ModItems.RSD100.get());
        fiveItems(exporter, ModItems.RSD500.get(), ModItems.RSD100.get());
        twoItems(exporter, ModItems.RSD1000.get(), ModItems.RSD500.get());
        twoItems(exporter, ModItems.RSD2000.get(), ModItems.RSD1000.get());
        fiveItems(exporter, ModItems.RSD5000.get(), ModItems.RSD1000.get());

        // SEK
        oneToOneConversionRecipe(exporter, ModItems.SEkr500.get(), ModItems.SEkr1000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.SEkr100.get(), ModItems.SEkr500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.SEkr100.get(), ModItems.SEkr200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.SEkr50.get(), ModItems.SEkr100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.SEkr10.get(), ModItems.SEkr50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.SEkr10.get(), ModItems.SEkr20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.SEkr5.get(), ModItems.SEkr10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.SEkr1.get(), ModItems.SEkr5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.SEkr1.get(), ModItems.SEkr2.get(), "", 2);
        twoItems(exporter, ModItems.SEkr2.get(), ModItems.SEkr1.get());
        fiveItems(exporter, ModItems.SEkr5.get(), ModItems.SEkr1.get());
        twoItems(exporter, ModItems.SEkr10.get(), ModItems.SEkr5.get());
        twoItems(exporter, ModItems.SEkr20.get(), ModItems.SEkr10.get());
        fiveItems(exporter, ModItems.SEkr50.get(), ModItems.SEkr10.get());
        twoItems(exporter, ModItems.SEkr100.get(), ModItems.SEkr50.get());
        twoItems(exporter, ModItems.SEkr200.get(), ModItems.SEkr100.get());
        fiveItems(exporter, ModItems.SEkr500.get(), ModItems.SEkr100.get());
        twoItems(exporter, ModItems.SEkr1000.get(), ModItems.SEkr500.get());

        // ISK
        oneToOneConversionRecipe(exporter, ModItems.ISkr5000.get(), ModItems.ISkr10000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ISkr1000.get(), ModItems.ISkr5000.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.ISkr1000.get(), ModItems.ISkr2000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ISkr500.get(), ModItems.ISkr1000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ISkr100.get(), ModItems.ISkr500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.ISkr50.get(), ModItems.ISkr100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ISkr10.get(), ModItems.ISkr50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.ISkr5.get(), ModItems.ISkr10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ISkr1.get(), ModItems.ISkr5.get(), "", 5);
        fiveItems(exporter, ModItems.ISkr5.get(), ModItems.ISkr1.get());
        twoItems(exporter, ModItems.ISkr10.get(), ModItems.ISkr5.get());
        fiveItems(exporter, ModItems.ISkr50.get(), ModItems.ISkr10.get());
        twoItems(exporter, ModItems.ISkr100.get(), ModItems.ISkr50.get());
        fiveItems(exporter, ModItems.ISkr500.get(), ModItems.ISkr100.get());
        twoItems(exporter, ModItems.ISkr1000.get(), ModItems.ISkr500.get());
        twoItems(exporter, ModItems.ISkr2000.get(), ModItems.ISkr1000.get());
        fiveItems(exporter, ModItems.ISkr5000.get(), ModItems.ISkr1000.get());
        twoItems(exporter, ModItems.ISkr10000.get(), ModItems.ISkr5000.get());

        // INR
        oneToOneConversionRecipe(exporter, ModItems.INr100.get(), ModItems.INr500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.INr100.get(), ModItems.INr200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.INr50.get(), ModItems.INr100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.INr10.get(), ModItems.INr50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.INr10.get(), ModItems.INr20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.INr5.get(), ModItems.INr10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.INr1.get(), ModItems.INr5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.INr1.get(), ModItems.INr2.get(), "", 2);
        twoItems(exporter, ModItems.INr2.get(), ModItems.INr1.get());
        fiveItems(exporter, ModItems.INr5.get(), ModItems.INr1.get());
        twoItems(exporter, ModItems.INr10.get(), ModItems.INr5.get());
        twoItems(exporter, ModItems.INr20.get(), ModItems.INr10.get());
        fiveItems(exporter, ModItems.INr50.get(), ModItems.INr10.get());
        twoItems(exporter, ModItems.INr100.get(), ModItems.INr50.get());
        twoItems(exporter, ModItems.INr200.get(), ModItems.INr100.get());
        fiveItems(exporter, ModItems.INr500.get(), ModItems.INr100.get());

        // KRW
        oneToOneConversionRecipe(exporter, ModItems.Won10000.get(), ModItems.Won50000.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Won5000.get(), ModItems.Won10000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Won1000.get(), ModItems.Won5000.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Won500.get(), ModItems.Won1000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Won100.get(), ModItems.Won500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.Won50.get(), ModItems.Won100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.Won10.get(), ModItems.Won50.get(), "", 5);
        fiveItems(exporter, ModItems.Won50.get(), ModItems.Won10.get());
        twoItems(exporter, ModItems.Won100.get(), ModItems.Won50.get());
        fiveItems(exporter, ModItems.Won500.get(), ModItems.Won100.get());
        twoItems(exporter, ModItems.Won1000.get(), ModItems.Won500.get());
        fiveItems(exporter, ModItems.Won5000.get(), ModItems.Won1000.get());
        twoItems(exporter, ModItems.Won10000.get(), ModItems.Won5000.get());
        fiveItems(exporter, ModItems.Won50000.get(), ModItems.Won10000.get());

        // CNY
        oneToOneConversionRecipe(exporter, ModItems.CNYuan50.get(), ModItems.CNYuan100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.CNYuan10.get(), ModItems.CNYuan50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.CNYuan10.get(), ModItems.CNYuan20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.CNYuan5.get(), ModItems.CNYuan10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.CNYuan1.get(), ModItems.CNYuan5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.CNJiao5.get(), ModItems.CNYuan1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.CNJiao1.get(), ModItems.CNJiao5.get(), "", 5);
        fiveItems(exporter, ModItems.CNJiao5.get(), ModItems.CNJiao1.get());
        twoItems(exporter, ModItems.CNYuan1.get(), ModItems.CNJiao5.get());
        fiveItems(exporter, ModItems.CNYuan5.get(), ModItems.CNYuan1.get());
        twoItems(exporter, ModItems.CNYuan10.get(), ModItems.CNYuan5.get());
        twoItems(exporter, ModItems.CNYuan20.get(), ModItems.CNYuan10.get());
        fiveItems(exporter, ModItems.CNYuan50.get(), ModItems.CNYuan10.get());
        twoItems(exporter, ModItems.CNYuan100.get(), ModItems.CNYuan50.get());

        // BRL
        oneToOneConversionRecipe(exporter, ModItems.BRReal100.get(), ModItems.BRReal200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.BRReal50.get(), ModItems.BRReal100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.BRReal10.get(), ModItems.BRReal50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.BRReal10.get(), ModItems.BRReal20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.BRReal5.get(), ModItems.BRReal10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.BRReal1.get(), ModItems.BRReal5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.BRReal1.get(), ModItems.BRReal2.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.BRCentavo50.get(), ModItems.BRReal1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.BRCentavo25.get(), ModItems.BRCentavo50.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.BRCentavo5.get(), ModItems.BRCentavo25.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.BRCentavo5.get(), ModItems.BRCentavo10.get(), "", 2);
        twoItems(exporter, ModItems.BRCentavo10.get(), ModItems.BRCentavo5.get());
        fiveItems(exporter, ModItems.BRCentavo25.get(), ModItems.BRCentavo5.get());
        twoItems(exporter, ModItems.BRCentavo50.get(), ModItems.BRCentavo25.get());
        twoItems(exporter, ModItems.BRReal1.get(), ModItems.BRCentavo50.get());
        twoItems(exporter, ModItems.BRReal2.get(), ModItems.BRReal1.get());
        fiveItems(exporter, ModItems.BRReal5.get(), ModItems.BRReal1.get());
        twoItems(exporter, ModItems.BRReal10.get(), ModItems.BRReal5.get());
        twoItems(exporter, ModItems.BRReal20.get(), ModItems.BRReal10.get());
        fiveItems(exporter, ModItems.BRReal50.get(), ModItems.BRReal10.get());
        twoItems(exporter, ModItems.BRReal100.get(), ModItems.BRReal50.get());
        twoItems(exporter, ModItems.BRReal200.get(), ModItems.BRReal100.get());

        // MXN
        oneToOneConversionRecipe(exporter, ModItems.MXPeso500.get(), ModItems.MXPeso1000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.MXPeso100.get(), ModItems.MXPeso500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.MXPeso100.get(), ModItems.MXPeso200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.MXPeso50.get(), ModItems.MXPeso100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.MXPeso10.get(), ModItems.MXPeso50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.MXPeso10.get(), ModItems.MXPeso20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.MXPeso5.get(), ModItems.MXPeso10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.MXPeso1.get(), ModItems.MXPeso5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.MXPeso1.get(), ModItems.MXPeso2.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.MXCentavo50.get(), ModItems.MXPeso1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.MXCentavo10.get(), ModItems.MXCentavo50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.MXCentavo10.get(), ModItems.MXCentavo20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.MXCentavo5.get(), ModItems.MXCentavo10.get(), "", 2);
        twoItems(exporter, ModItems.MXCentavo10.get(), ModItems.MXCentavo5.get());
        twoItems(exporter, ModItems.MXCentavo20.get(), ModItems.MXCentavo10.get());
        fiveItems(exporter, ModItems.MXCentavo50.get(), ModItems.MXCentavo10.get());
        twoItems(exporter, ModItems.MXPeso1.get(), ModItems.MXCentavo50.get());
        twoItems(exporter, ModItems.MXPeso2.get(), ModItems.MXPeso1.get());
        fiveItems(exporter, ModItems.MXPeso5.get(), ModItems.MXPeso1.get());
        twoItems(exporter, ModItems.MXPeso10.get(), ModItems.MXPeso5.get());
        twoItems(exporter, ModItems.MXPeso20.get(), ModItems.MXPeso10.get());
        fiveItems(exporter, ModItems.MXPeso50.get(), ModItems.MXPeso10.get());
        twoItems(exporter, ModItems.MXPeso100.get(), ModItems.MXPeso50.get());
        twoItems(exporter, ModItems.MXPeso200.get(), ModItems.MXPeso100.get());
        fiveItems(exporter, ModItems.MXPeso500.get(), ModItems.MXPeso100.get());
        twoItems(exporter, ModItems.MXPeso1000.get(), ModItems.MXPeso500.get());

        // ZAR
        oneToOneConversionRecipe(exporter, ModItems.ZARand100.get(), ModItems.ZARand200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ZARand50.get(), ModItems.ZARand100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ZARand10.get(), ModItems.ZARand50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.ZARand10.get(), ModItems.ZARand20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ZARand5.get(), ModItems.ZARand10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ZARand1.get(), ModItems.ZARand5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.ZARand1.get(), ModItems.ZARand2.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ZACent50.get(), ModItems.ZARand1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.ZACent10.get(), ModItems.ZACent50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.ZACent10.get(), ModItems.ZACent20.get(), "", 2);
        twoItems(exporter, ModItems.ZACent20.get(), ModItems.ZACent10.get());
        fiveItems(exporter, ModItems.ZACent50.get(), ModItems.ZACent10.get());
        twoItems(exporter, ModItems.ZARand1.get(), ModItems.ZACent50.get());
        twoItems(exporter, ModItems.ZARand2.get(), ModItems.ZARand1.get());
        fiveItems(exporter, ModItems.ZARand5.get(), ModItems.ZARand1.get());
        twoItems(exporter, ModItems.ZARand10.get(), ModItems.ZARand5.get());
        twoItems(exporter, ModItems.ZARand20.get(), ModItems.ZARand10.get());
        fiveItems(exporter, ModItems.ZARand50.get(), ModItems.ZARand10.get());
        twoItems(exporter, ModItems.ZARand100.get(), ModItems.ZARand50.get());
        twoItems(exporter, ModItems.ZARand200.get(), ModItems.ZARand100.get());

        // TRY
        oneToOneConversionRecipe(exporter, ModItems.TRl100.get(), ModItems.TRl200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.TRl50.get(), ModItems.TRl100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.TRl10.get(), ModItems.TRl50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.TRl10.get(), ModItems.TRl20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.TRl5.get(), ModItems.TRl10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.TRl1.get(), ModItems.TRl5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.TRk50.get(), ModItems.TRl1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.TRk25.get(), ModItems.TRk50.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.TRk5.get(), ModItems.TRk25.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.TRk5.get(), ModItems.TRk10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.TRk1.get(), ModItems.TRk5.get(), "", 5);
        fiveItems(exporter, ModItems.TRk5.get(), ModItems.TRk1.get());
        twoItems(exporter, ModItems.TRk10.get(), ModItems.TRk5.get());
        fiveItems(exporter, ModItems.TRk25.get(), ModItems.TRk5.get());
        twoItems(exporter, ModItems.TRk50.get(), ModItems.TRk25.get());
        twoItems(exporter, ModItems.TRl1.get(), ModItems.TRk50.get());
        fiveItems(exporter, ModItems.TRl5.get(), ModItems.TRl1.get());
        twoItems(exporter, ModItems.TRl10.get(), ModItems.TRl5.get());
        twoItems(exporter, ModItems.TRl20.get(), ModItems.TRl10.get());
        fiveItems(exporter, ModItems.TRl50.get(), ModItems.TRl10.get());
        twoItems(exporter, ModItems.TRl100.get(), ModItems.TRl50.get());
        twoItems(exporter, ModItems.TRl200.get(), ModItems.TRl100.get());

        // NZD
        oneToOneConversionRecipe(exporter, ModItems.NZD50.get(), ModItems.NZD100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.NZD10.get(), ModItems.NZD50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.NZD10.get(), ModItems.NZD20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.NZD5.get(), ModItems.NZD10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.NZD1.get(), ModItems.NZD5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.NZD1.get(), ModItems.NZD2.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.NZCent50.get(), ModItems.NZD1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.NZCent10.get(), ModItems.NZCent50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.NZCent10.get(), ModItems.NZCent20.get(), "", 2);
        twoItems(exporter, ModItems.NZCent20.get(), ModItems.NZCent10.get());
        fiveItems(exporter, ModItems.NZCent50.get(), ModItems.NZCent10.get());
        twoItems(exporter, ModItems.NZD1.get(), ModItems.NZCent50.get());
        twoItems(exporter, ModItems.NZD2.get(), ModItems.NZD1.get());
        fiveItems(exporter, ModItems.NZD5.get(), ModItems.NZD1.get());
        twoItems(exporter, ModItems.NZD10.get(), ModItems.NZD5.get());
        twoItems(exporter, ModItems.NZD20.get(), ModItems.NZD10.get());
        fiveItems(exporter, ModItems.NZD50.get(), ModItems.NZD10.get());
        twoItems(exporter, ModItems.NZD100.get(), ModItems.NZD50.get());

        // PHP
        oneToOneConversionRecipe(exporter, ModItems.PHP500.get(), ModItems.PHP1000.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.PHP100.get(), ModItems.PHP500.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.PHP100.get(), ModItems.PHP200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.PHP50.get(), ModItems.PHP100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.PHP10.get(), ModItems.PHP50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.PHP10.get(), ModItems.PHP20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.PHP5.get(), ModItems.PHP10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.PHP1.get(), ModItems.PHP5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.PHS5.get(), ModItems.PHS25.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.PHS1.get(), ModItems.PHS5.get(), "", 5);
        fiveItems(exporter, ModItems.PHS5.get(), ModItems.PHS1.get());
        fiveItems(exporter, ModItems.PHS25.get(), ModItems.PHS5.get());
        fourItems(exporter, ModItems.PHP1.get(), ModItems.PHS25.get());
        fiveItems(exporter, ModItems.PHP5.get(), ModItems.PHP1.get());
        twoItems(exporter, ModItems.PHP10.get(), ModItems.PHP5.get());
        twoItems(exporter, ModItems.PHP20.get(), ModItems.PHP10.get());
        fiveItems(exporter, ModItems.PHP50.get(), ModItems.PHP10.get());
        twoItems(exporter, ModItems.PHP100.get(), ModItems.PHP50.get());
        twoItems(exporter, ModItems.PHP200.get(), ModItems.PHP100.get());
        fiveItems(exporter, ModItems.PHP500.get(), ModItems.PHP100.get());
        twoItems(exporter, ModItems.PHP1000.get(), ModItems.PHP500.get());

        // EGP
        oneToOneConversionRecipe(exporter, ModItems.EGPound100.get(), ModItems.EGPound200.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.EGPound50.get(), ModItems.EGPound100.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.EGPound10.get(), ModItems.EGPound50.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.EGPound10.get(), ModItems.EGPound20.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.EGPound5.get(), ModItems.EGPound10.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.EGPound1.get(), ModItems.EGPound5.get(), "", 5);
        oneToOneConversionRecipe(exporter, ModItems.EGPiastre50.get(), ModItems.EGPound1.get(), "", 2);
        oneToOneConversionRecipe(exporter, ModItems.EGPiastre25.get(), ModItems.EGPiastre50.get(), "", 2);
        twoItems(exporter, ModItems.EGPiastre50.get(), ModItems.EGPiastre25.get());
        twoItems(exporter, ModItems.EGPound1.get(), ModItems.EGPiastre50.get());
        fiveItems(exporter, ModItems.EGPound5.get(), ModItems.EGPound1.get());
        twoItems(exporter, ModItems.EGPound10.get(), ModItems.EGPound5.get());
        twoItems(exporter, ModItems.EGPound20.get(), ModItems.EGPound10.get());
        fiveItems(exporter, ModItems.EGPound50.get(), ModItems.EGPound10.get());
        twoItems(exporter, ModItems.EGPound100.get(), ModItems.EGPound50.get());
        twoItems(exporter, ModItems.EGPound200.get(), ModItems.EGPound100.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BANK_MACHINE.get())
                .pattern("DII")
                .pattern("GPP")
                .pattern("EPP")
                .unlockedBy(getHasName(Items.EMERALD), has(Items.EMERALD))
                .define('D', Items.DIAMOND)
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GOLD_INGOT)
                .define('E', Items.EMERALD)
                .define('P', ItemTags.PLANKS)
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.Key.get())
                .pattern("GGD")
                .pattern("G  ")
                .pattern("   ")
                .unlockedBy(getHasName(Items.DIAMOND), has(Items.DIAMOND))
                .define('G', Items.GOLD_INGOT)
                .define('D', Items.DIAMOND)
                .save(exporter);
        // BANK MACHINE RECIPES
        BankMachineRecipeShapedBuilder.shaped(ModItems.Euro5.get(), 6)
                .pattern("L##")
                .pattern("RPA")
                .pattern("IGD")
                .define('#', Items.GOLD_NUGGET)
                .define('L', Items.LAPIS_LAZULI)
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_NUGGET)
                .define('P', ModItems.SpecialPaper.get())
                .define('D', Items.LIGHT_GRAY_DYE)
                .define('G', Items.GRAY_DYE)
                .define('A', Items.GLASS_PANE)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.Euro5.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.Pound5.get(), 6)
                .pattern("L##")
                .pattern("RPA")
                .pattern("IGD")
                .define('#', Items.GOLD_NUGGET)
                .define('L', Items.LAPIS_LAZULI)
                .define('R', Items.GLASS_PANE)
                .define('I', Items.IRON_INGOT)
                .define('P', ModItems.PolymerSheet.get())
                .define('D', Items.LIGHT_BLUE_DYE)
                .define('G', Items.CYAN_DYE)
                .define('A', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.PolymerSheet.get()), has(ModItems.PolymerSheet.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.Pound5.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.Dollar1.get(), 6)
                .pattern("RPG")
                .pattern("GEG")
                .pattern("GPG")
                .define('R', Items.REDSTONE)
                .define('P', Items.GOLD_NUGGET)
                .define('G', Items.GREEN_DYE)
                .define('E', ModItems.SpecialPaper.get())
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.Dollar1.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.DollarC5.get(), 6)
                .pattern("NOB")
                .pattern("RDG")
                .pattern("NCC")
                .define('N', Items.IRON_NUGGET)
                .define('R', Items.REDSTONE)
                .define('D', ModItems.PolymerSheet.get())
                .define('O', Items.GOLD_NUGGET)
                .define('C', Items.CYAN_DYE)
                .define('G', Items.RED_STAINED_GLASS_PANE)
                .define('B', Items.BLUE_DYE)
                .unlockedBy(getHasName(ModItems.PolymerSheet.get()), has(ModItems.PolymerSheet.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.DollarC5.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.Leu1.get(), 6)
                .pattern("#GG")
                .pattern("SPR")
                .pattern("*GG")
                .define('#', Items.GOLD_NUGGET)
                .define('*', Items.IRON_NUGGET)
                .define('S', Items.WHITE_STAINED_GLASS_PANE)
                .define('G', Items.LIME_DYE)
                .define('P', ModItems.PolymerSheet.get())
                .define('R', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.PolymerSheet.get()), has(ModItems.PolymerSheet.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.Leu1.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.LeiMD20.get(), 6)
                .pattern("G*#")
                .pattern("RPG")
                .pattern("I*#")
                .define('#', Items.GREEN_DYE)
                .define('*', Items.LIME_DYE)
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_NUGGET)
                .define('G', Items.GOLD_NUGGET)
                .define('P', ModItems.SpecialPaper.get())
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.LeiMD20.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.Franc10.get(), 6)
                .pattern(" DD")
                .pattern(" ND")
                .pattern(" RP")
                .define('N', Items.COPPER_INGOT)
                .define('D', Items.ORANGE_DYE)
                .define('R', Items.REDSTONE)
                .define('P', ModItems.SpecialPaper.get())
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.Franc10.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.DollarA5.get(), 6)
                .pattern("LDD")
                .pattern("GBR")
                .pattern("OCC")
                .define('L', Items.LAPIS_LAZULI)
                .define('D', Items.MAGENTA_DYE)
                .define('G', Items.WHITE_STAINED_GLASS_PANE)
                .define('B', ModItems.PolymerSheet.get())
                .define('R', Items.REDSTONE)
                .define('O', Items.ORANGE_DYE)
                .define('C', Items.GOLD_NUGGET)
                .unlockedBy(getHasName(ModItems.PolymerSheet.get()), has(ModItems.PolymerSheet.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.DollarA5.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.Yen1000.get(), 6)
                .pattern("IGL")
                .pattern("RPW")
                .pattern("WWW")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GOLD_INGOT)
                .define('P', ModItems.SpecialPaper.get())
                .define('W', Items.BLUE_DYE)
                .define('L', Items.LAPIS_LAZULI)
                .define('R', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.Yen1000.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.CZkr100.get(), 6)
                .pattern("RGG")
                .pattern("RP#")
                .pattern("RDD")
                .define('#', Items.GOLD_INGOT)
                .define('D', Items.LIME_DYE)
                .define('G', Items.CYAN_DYE)
                .define('P', ModItems.SpecialPaper.get())
                .define('R', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.CZkr100.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.DKkr50.get(), 6)
                .pattern("ABC")
                .pattern("DPE")
                .pattern("FCC")
                .define('A', Items.REDSTONE)
                .define('B', Items.BLACK_DYE)
                .define('C', Items.GRAY_DYE)
                .define('P', ModItems.SpecialPaper.get())
                .define('D', Items.GOLD_INGOT)
                .define('E', Items.PURPLE_DYE)
                .define('F', Items.LAPIS_LAZULI)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.DKkr50.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.Ft500.get(), 6)
                .pattern("CBC")
                .pattern("RPI")
                .pattern("CBC")
                .define('R', Items.REDSTONE)
                .define('B', Items.ORANGE_DYE)
                .define('C', Items.RED_DYE)
                .define('P', ModItems.SpecialPaper.get())
                .define('I', Items.IRON_NUGGET)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.Ft500.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.NOkr50.get(), 6)
                .pattern("R#E")
                .pattern("IPG")
                .pattern("LE#")
                .define('#', Items.LIME_DYE)
                .define('E', Items.GREEN_DYE)
                .define('P', ModItems.SpecialPaper.get())
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_NUGGET)
                .define('G', Items.GOLD_NUGGET)
                .define('L', Items.LAPIS_LAZULI)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.NOkr50.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.Zloty10.get(), 6)
                .pattern("R#E")
                .pattern("IPG")
                .pattern("LE#")
                .define('#', Items.GRAY_DYE)
                .define('E', Items.BROWN_DYE)
                .define('P', ModItems.SpecialPaper.get())
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_NUGGET)
                .define('G', Items.WHITE_DYE)
                .define('L', Items.LAPIS_LAZULI)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.Zloty10.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.RSD10.get(), 6)
                .pattern("R##")
                .pattern("IP#")
                .pattern("LEG")
                .define('#', Items.YELLOW_DYE)
                .define('E', Items.GRAY_DYE)
                .define('P', ModItems.SpecialPaper.get())
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_NUGGET)
                .define('G', Items.WHITE_DYE)
                .define('L', Items.LAPIS_LAZULI)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.RSD10.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.SEkr20.get(), 6)
                .pattern("R##")
                .pattern("IP#")
                .pattern("LEG")
                .define('#', Items.PURPLE_DYE)
                .define('E', Items.GRAY_DYE)
                .define('P', ModItems.SpecialPaper.get())
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_NUGGET)
                .define('G', Items.WHITE_DYE)
                .define('L', Items.LAPIS_LAZULI)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.SEkr20.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.ISkr500.get(), 6)
                .pattern("LO#")
                .pattern("RPI")
                .pattern("G#O")
                .define('#', Items.RED_DYE)
                .define('P', ModItems.SpecialPaper.get())
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('O', Items.ORANGE_DYE)
                .define('L', Items.LAPIS_LAZULI)
                .define('G', Items.GOLD_NUGGET)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.ISkr500.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.INr50.get(), 6)
                .pattern("L##")
                .pattern("RP#")
                .pattern("I##")
                .define('#', Items.CYAN_DYE)
                .define('P', ModItems.SpecialPaper.get())
                .define('I', Items.IRON_NUGGET)
                .define('R', Items.REDSTONE)
                .define('L', Items.LAPIS_LAZULI)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.INr50.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.Won1000.get(), 6)
                .pattern("R##")
                .pattern("LP#")
                .pattern("I##")
                .define('#', Items.BLUE_DYE)
                .define('P', ModItems.SpecialPaper.get())
                .define('I', Items.GOLD_NUGGET)
                .define('R', Items.REDSTONE)
                .define('L', Items.LAPIS_LAZULI)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.Won1000.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.CNYuan1.get(), 6)
                .pattern("#O#")
                .pattern("RPI")
                .pattern("O#O")
                .define('#', Items.GREEN_DYE)
                .define('P', ModItems.SpecialPaper.get())
                .define('I', Items.IRON_NUGGET)
                .define('R', Items.REDSTONE)
                .define('O', Items.YELLOW_DYE)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.CNYuan1.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.BRReal2.get(), 6)
                .pattern("DDN")
                .pattern("DPG")
                .pattern("LRN")
                .define('P', ModItems.SpecialPaper.get())
                .define('G', Items.GOLD_INGOT)
                .define('N', Items.IRON_NUGGET)
                .define('L', Items.LAPIS_LAZULI)
                .define('D', Items.LIGHT_BLUE_DYE)
                .define('R', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.BRReal2.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.MXPeso20.get(), 6)
                .pattern("NGD")
                .pattern("LPL")
                .pattern("GRD")
                .define('P', ModItems.SpecialPaper.get())
                .define('G', Items.GOLD_NUGGET)
                .define('N', Items.IRON_NUGGET)
                .define('L', Items.RED_DYE)
                .define('D', Items.LIME_DYE)
                .define('R', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.MXPeso20.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.ZARand10.get(), 6)
                .pattern("DDN")
                .pattern("LPG")
                .pattern("LRN")
                .define('P', ModItems.SpecialPaper.get())
                .define('G', Items.GOLD_NUGGET)
                .define('N', Items.IRON_NUGGET)
                .define('L', Items.GREEN_DYE)
                .define('D', Items.LIME_DYE)
                .define('R', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.ZARand10.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.TRl5.get(), 6)
                .pattern("LGG")
                .pattern("OPR")
                .pattern("NDG")
                .define('D', Items.LIGHT_GRAY_DYE)
                .define('G', Items.GRAY_DYE)
                .define('L', Items.LAPIS_LAZULI)
                .define('N', Items.IRON_NUGGET)
                .define('O', Items.ORANGE_DYE)
                .define('P', ModItems.SpecialPaper.get())
                .define('R', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.TRl5.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.NZD5.get(), 6)
                .pattern("OCO")
                .pattern("OPR")
                .pattern("LGO")
                .define('C', Items.CYAN_DYE)
                .define('G', Items.GOLD_NUGGET)
                .define('L', Items.LAPIS_LAZULI)
                .define('O', Items.ORANGE_DYE)
                .define('P', ModItems.PolymerSheet.get())
                .define('R', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.PolymerSheet.get()), has(ModItems.PolymerSheet.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.NZD5.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.PHP20.get(), 6)
                .pattern("RLC")
                .pattern("CPC")
                .pattern("NGC")
                .define('C', Items.ORANGE_DYE)
                .define('G', Items.LIGHT_GRAY_DYE)
                .define('L', Items.LAPIS_LAZULI)
                .define('N', Items.IRON_NUGGET)
                .define('P', ModItems.SpecialPaper.get())
                .define('R', Items.REDSTONE)
                .unlockedBy(getHasName(ModItems.SpecialPaper.get()), has(ModItems.SpecialPaper.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.PHP20.get()) + "_1"));
        BankMachineRecipeShapedBuilder.shaped(ModItems.EGPiastre25.get(), 6)
                .pattern("NGG")
                .pattern("RPB")
                .pattern("IBG")
                .define('N', Items.GOLD_NUGGET)
                .define('G', Items.LIGHT_GRAY_DYE)
                .define('R', Items.REDSTONE)
                .define('P', ModItems.PolymerSheet.get())
                .define('B', Items.LIGHT_BLUE_DYE)
                .define('I', Items.IRON_NUGGET)
                .unlockedBy(getHasName(ModItems.PolymerSheet.get()), has(ModItems.PolymerSheet.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.EGPiastre25.get()) + "_1"));

        BankMachineRecipeShapedBuilder.shaped(ModItems.RustyCard.get())
                .pattern(" D ")
                .pattern("NCD")
                .pattern(" W ")
                .define('D', Items.BROWN_DYE)
                .define('C', ModItems.PlasticCard.get())
                .define('N', Items.GOLD_NUGGET)
                .define('W', Items.WHITE_DYE)
                .unlockedBy(getHasName(ModItems.PlasticCard.get()), has(ModItems.PlasticCard.get()))
                .save(exporter);
        BankMachineRecipeShapedBuilder.shaped(ModItems.Card.get())
                .pattern("RLL")
                .pattern("GCD")
                .pattern("DDY")
                .define('R', Items.REDSTONE)
                .define('L', Items.LAPIS_LAZULI)
                .define('G', Items.GOLD_NUGGET)
                .define('C', ModItems.RustyCard.get())
                .define('D', Items.RED_DYE)
                .define('Y', Items.YELLOW_DYE)
                .unlockedBy(getHasName(ModItems.RustyCard.get()), has(ModItems.RustyCard.get()))
                .save(exporter);
        BankMachineRecipeShapedBuilder.shaped(ModItems.GoldCard.get())
                .pattern("YDY")
                .pattern("NCB")
                .pattern("III")
                .define('Y', Items.YELLOW_DYE)
                .define('D', Items.DIAMOND)
                .define('N', Items.GOLD_NUGGET)
                .define('C', ModItems.Card.get())
                .define('I', Items.GOLD_INGOT)
                .define('B', Items.BLACK_DYE)
                .unlockedBy(getHasName(ModItems.Card.get()), has(ModItems.Card.get()))
                .save(exporter);
        BankMachineRecipeShapedBuilder.shaped(ModItems.SteelCard.get())
                .pattern("GGG")
                .pattern("NCW")
                .pattern("DDD")
                .define('G', Items.GRAY_DYE)
                .define('D', Items.DIAMOND)
                .define('N', Items.GOLD_NUGGET)
                .define('W', Items.WHITE_DYE)
                .define('C', ModItems.GoldCard.get())
                .unlockedBy(getHasName(ModItems.GoldCard.get()), has(ModItems.GoldCard.get()))
                .save(exporter);
        BankMachineRecipeShapedBuilder.shaped(ModItems.SupremeCard.get())
                .pattern("NSN")
                .pattern("BCB")
                .pattern("EGE")
                .define('C', ModItems.SteelCard.get())
                .define('S', Items.NETHERITE_INGOT)
                .define('N', Items.CYAN_DYE)
                .define('B', Items.DIAMOND)
                .define('E', Items.EMERALD)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .save(exporter);
        BankMachineRecipeShapedBuilder.shaped(ModItems.Polymer.get(), 4)
                .pattern("P P")
                .pattern("BCB")
                .pattern("GSG")
                .define('P', Items.BLAZE_POWDER)
                .define('B', Items.CLAY_BALL)
                .define('C', ItemTags.COALS)
                .define('S', ItemTags.SAND)
                .define('G', Items.WATER_BUCKET)
                .unlockedBy(getHasName(Items.BLAZE_POWDER), has(Items.BLAZE_POWDER))
                .save(exporter);
        BankMachineRecipeShapedBuilder.shaped(ModItems.Plastic.get(), 4)
                .pattern("PP")
                .pattern("PP")
                .define('P', ModItems.Polymer.get())
                .unlockedBy(getHasName(ModItems.Polymer.get()), has(ModItems.Polymer.get()))
                .save(exporter);
        BankMachineRecipeShapedBuilder.shaped(ModItems.PolymerSheet.get(), 3)
                .pattern("PPP")
                .pattern("LLL")
                .pattern("PPP")
                .define('P', ModItems.Plastic.get())
                .define('L', ModItems.Polymer.get())
                .unlockedBy(getHasName(ModItems.Plastic.get()), has(ModItems.Plastic.get()))
                .save(exporter);
        BankMachineRecipeShapedBuilder.shaped(ModItems.SpecialPaper.get(),1)
                .unlockedBy(getHasName(Items.PAPER), has(Items.PAPER))
                .pattern("RLG")
                .pattern("APG")
                .pattern("WWG")
                .define('P', Items.PAPER)
                .define('G', Items.GOLD_NUGGET)
                .define('R', Items.REDSTONE)
                .define('L', Items.LAPIS_LAZULI)
                .define('A', Items.LIGHT_GRAY_DYE)
                .define('W', Items.WHITE_DYE)
                .save(exporter);
        BankMachineRecipeShapedBuilder.shaped(ModItems.PlasticCard.get())
                .pattern("PPP")
                .pattern("WWW")
                .define('P', ModItems.Plastic.get())
                .define('W', ItemTags.WOODEN_SLABS)
                .unlockedBy(getHasName(ModItems.Plastic.get()), has(ModItems.Plastic.get()))
                .save(exporter);
        BankMachineRecipeShapedBuilder.shaped(ModBlocks.ATM.get())
                .pattern("ION")
                .pattern("WDW")
                .pattern("WCW")
                .define('W', Items.IRON_BLOCK)
                .define('C', Items.COMPARATOR)
                .define('D', Items.DISPENSER)
                .define('O', Items.COMPASS)
                .define('I', Items.DIAMOND)
                .define('N', Items.EMERALD)
                .unlockedBy(getHasName(Items.DISPENSER), has(Items.DISPENSER))
                .save(exporter);
        // ROL
        conversionRecipe1(exporter, ModItems.L50.get(), ModItems.L100.get(), "", 2);
        conversionRecipe1(exporter, ModItems.L25.get(), ModItems.L50.get(), "", 2);
        conversionRecipe1(exporter, ModItems.L5.get(), ModItems.L25.get(), "", 5);
        conversionRecipe1(exporter, ModItems.L5.get(), ModItems.L20.get(), "", 4);
        conversionRecipe1(exporter, ModItems.L1.get(), ModItems.L5.get(), "", 5);
        conversionRecipe1(exporter, ModItems.L1.get(), ModItems.L2.get(), "", 2);
        conversionRecipe1(exporter, ModItems.B50.get(), ModItems.L1.get(), "", 2);
        conversionRecipe1(exporter, ModItems.B10.get(), ModItems.B50.get(), "", 5);
        conversionRecipe1(exporter, ModItems.B10.get(), ModItems.B20.get(), "", 2);
        conversionRecipe1(exporter, ModItems.B5.get(), ModItems.B10.get(), "", 2);
        conversionRecipe1(exporter, ModItems.B1.get(), ModItems.B5.get(), "", 5);
        conversionRecipe1(exporter, ModItems.B1.get(), ModItems.B2.get(), "", 2);
        twoItems1(exporter, ModItems.B2.get(), ModItems.B1.get());
        fiveItems1(exporter, ModItems.B5.get(), ModItems.B1.get());
        twoItems1(exporter, ModItems.B10.get(), ModItems.B5.get());
        twoItems1(exporter, ModItems.B20.get(), ModItems.B10.get());
        fiveItems1(exporter, ModItems.B50.get(), ModItems.B10.get());
        twoItems1(exporter, ModItems.L1.get(), ModItems.B50.get());
        twoItems1(exporter, ModItems.L2.get(), ModItems.L1.get());
        fiveItems1(exporter, ModItems.L5.get(), ModItems.L1.get());
        BankMachineRecipeShapedBuilder.shaped(ModItems.L12.get(),1)
                .pattern("GH")
                .pattern("##")
                .define('#', ModItems.L5.get())
                .define('G', ModItems.L2.get())
                .define('H', ModItems.B50.get())
                .unlockedBy(getHasName(ModItems.L5.get()), has(ModItems.L5.get()))
                .save(exporter);
        fourItems1(exporter, ModItems.L20.get(), ModItems.L5.get());
        fiveItems1(exporter, ModItems.L25.get(), ModItems.L5.get());
        BankMachineRecipeShapedBuilder.shaped(ModItems.L25.get())
                .pattern("##")
                .define('#', ModItems.L12.get())
                .unlockedBy(getHasName(ModItems.L12.get()), has(ModItems.L12.get()))
.save(exporter, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, getItemName(ModItems.L25.get()) + "_1"));
        twoItems1(exporter, ModItems.L50.get(), ModItems.L25.get());
        twoItems1(exporter, ModItems.L100.get(), ModItems.L50.get());
    }
}