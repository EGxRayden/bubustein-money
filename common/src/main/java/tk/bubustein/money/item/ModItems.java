package tk.bubustein.money.item;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import tk.bubustein.money.MoneyExpectPlatform;
import tk.bubustein.money.MoneyMod;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
@SuppressWarnings("UnstableApiUsage")
public class ModItems {
    public static void init(){}
    public static Supplier<Item> registerItem(String name, RegistrySupplier<CreativeModeTab> itemgroup){
        return MoneyExpectPlatform.registerItem(name, () -> new Item(new Item.Properties()
                .arch$tab(itemgroup)
                .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, name)))));
    }
    public static Supplier<Item> registerItem(String name, RegistrySupplier<CreativeModeTab> itemgroup, Rarity rarity){
        return MoneyExpectPlatform.registerItem(name, () -> new Item(new Item.Properties()
                .arch$tab(itemgroup).rarity(rarity)
                .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, name)))));
    }
    public static Supplier<Item> registerItem(String name, RegistrySupplier<CreativeModeTab> itemgroup, Rarity rarity, boolean addGlint){
        return MoneyExpectPlatform.registerItem(name, () -> new Item(new Item.Properties()
                .arch$tab(itemgroup).rarity(rarity).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, addGlint)
                .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, name)))));
    }
    /*
     * ////////////////////////////////////////////////////
     * //////////////// COINS TAB ///////////////////////
     * /////////////// BUBUSTEIN's MONEY MOD //////////////
     * ////////////////////////////////////////////////////
     * ////////////////////////////////////////////////////
     * */
    public static final Supplier<Item> Ecent1 = registerItem("one_ecent", MoneyMod.COINS);
    public static final Supplier<Item> Ecent2 = registerItem("two_ecents", MoneyMod.COINS);
    public static final Supplier<Item> Ecent5 = registerItem("five_ecents", MoneyMod.COINS);
    public static final Supplier<Item> Ecent10 = registerItem("ten_ecents", MoneyMod.COINS);
    public static final Supplier<Item> Ecent20 = registerItem("twenty_ecents",MoneyMod.COINS);
    public static final Supplier<Item> Ecent50 = registerItem("fifty_ecents", MoneyMod.COINS);
    public static final Supplier<Item> Euro1 = registerItem("one_euro", MoneyMod.COINS);
    public static final Supplier<Item> Euro2 = registerItem("two_euros", MoneyMod.COINS);

    public static final Supplier<Item> Pence1 = registerItem("one_pence", MoneyMod.COINS);
    public static final Supplier<Item> Pence2 = registerItem("two_pence", MoneyMod.COINS);
    public static final Supplier<Item> Pence5 = registerItem("five_pence", MoneyMod.COINS);
    public static final Supplier<Item> Pence10 = registerItem("ten_pence", MoneyMod.COINS);
    public static final Supplier<Item> Pence20 = registerItem("twenty_pence", MoneyMod.COINS);
    public static final Supplier<Item> Pence50 = registerItem("fifty_pence", MoneyMod.COINS);
    public static final Supplier<Item> Pound1 = registerItem("one_pound", MoneyMod.COINS);
    public static final Supplier<Item> Pound2 = registerItem("two_pounds", MoneyMod.COINS);

    public static final Supplier<Item> Cent1 = registerItem("one_cent", MoneyMod.COINS);
    public static final Supplier<Item> Cent5 = registerItem("five_cents", MoneyMod.COINS);
    public static final Supplier<Item> Cent10 = registerItem("ten_cents", MoneyMod.COINS);
    public static final Supplier<Item> Cent25 = registerItem("twentyfive_cents", MoneyMod.COINS);
    public static final Supplier<Item> Cent50 = registerItem("fifty_cents", MoneyMod.COINS);

    public static final Supplier<Item> CCent5 = registerItem("five_ccents", MoneyMod.COINS);
    public static final Supplier<Item> CCent10 = registerItem("ten_ccents", MoneyMod.COINS);
    public static final Supplier<Item> CCent25 = registerItem("twentyfive_ccents", MoneyMod.COINS);
    public static final Supplier<Item> Loonie = registerItem("loonie", MoneyMod.COINS);
    public static final Supplier<Item> Toonie = registerItem("toonie", MoneyMod.COINS);

    public static final Supplier<Item> Ban1 = registerItem("un_ban", MoneyMod.COINS);
    public static final Supplier<Item> Bani5 = registerItem("cinci_bani", MoneyMod.COINS);
    public static final Supplier<Item> Bani10 = registerItem("zece_bani", MoneyMod.COINS);
    public static final Supplier<Item> Bani50 = registerItem("cincizeci_bani", MoneyMod.COINS);

    public static final Supplier<Item> BanMD5 = registerItem("cinci_bani_md", MoneyMod.COINS);
    public static final Supplier<Item> BaniMD10 = registerItem("zece_bani_md", MoneyMod.COINS);
    public static final Supplier<Item> BaniMD25 = registerItem("douazecicinci_bani_md", MoneyMod.COINS);
    public static final Supplier<Item> BaniMD50 = registerItem("cincizeci_bani_md", MoneyMod.COINS);
    public static final Supplier<Item> LeuMD1 = registerItem("un_leu_md", MoneyMod.COINS);
    public static final Supplier<Item> LeuMD2 = registerItem("doi_lei_md", MoneyMod.COINS);
    public static final Supplier<Item> LeiMD5 = registerItem("cinci_lei_md", MoneyMod.COINS);
    public static final Supplier<Item> LeiMD10 = registerItem("zece_lei_md", MoneyMod.COINS);

    public static final Supplier<Item> Centimes5 = registerItem("five_centimes", MoneyMod.COINS);
    public static final Supplier<Item> Centimes10 = registerItem("ten_centimes", MoneyMod.COINS);
    public static final Supplier<Item> Centimes20 = registerItem("twenty_centimes", MoneyMod.COINS);
    public static final Supplier<Item> HalfFranc = registerItem("half_franc", MoneyMod.COINS);
    public static final Supplier<Item> Franc1 = registerItem("one_franc", MoneyMod.COINS);
    public static final Supplier<Item> Franc2 = registerItem("two_francs", MoneyMod.COINS);
    public static final Supplier<Item> Franc5 = registerItem("five_francs", MoneyMod.COINS);

    public static final Supplier<Item> ACent5 = registerItem("five_acents", MoneyMod.COINS);
    public static final Supplier<Item> ACent10 = registerItem("ten_acents", MoneyMod.COINS);
    public static final Supplier<Item> ACent20 = registerItem("twenty_acents", MoneyMod.COINS);
    public static final Supplier<Item> ACent50 = registerItem("fifty_acents", MoneyMod.COINS);
    public static final Supplier<Item> DollarA1 = registerItem("one_adollar", MoneyMod.COINS);
    public static final Supplier<Item> DollarA2 = registerItem("two_adollars", MoneyMod.COINS);

    public static final Supplier<Item> Yen1 = registerItem("one_yen", MoneyMod.COINS);
    public static final Supplier<Item> Yen5 = registerItem("five_yen", MoneyMod.COINS);
    public static final Supplier<Item> Yen10 = registerItem("ten_yen", MoneyMod.COINS);
    public static final Supplier<Item> Yen50 = registerItem("fifty_yen", MoneyMod.COINS);
    public static final Supplier<Item> Yen100 = registerItem("hundred_yen", MoneyMod.COINS);
    public static final Supplier<Item> Yen500 = registerItem("five_hundred_yen", MoneyMod.COINS);

    public static final Supplier<Item> Stotinka1 = registerItem("one_stotinka", MoneyMod.COINS);
    public static final Supplier<Item> Stotinka2 = registerItem("two_stotinki", MoneyMod.COINS);
    public static final Supplier<Item> Stotinka5 = registerItem("five_stotinki", MoneyMod.COINS);
    public static final Supplier<Item> Stotinka10 = registerItem("ten_stotinki", MoneyMod.COINS);
    public static final Supplier<Item> Stotinka20 = registerItem("twenty_stotinki", MoneyMod.COINS);
    public static final Supplier<Item> Stotinka50 = registerItem("fifty_stotinki", MoneyMod.COINS);
    public static final Supplier<Item> Leva1 = registerItem("one_lev", MoneyMod.COINS);
    public static final Supplier<Item> Leva2 = registerItem("two_leva", MoneyMod.COINS);

    public static final Supplier<Item> CZkr1 = registerItem("one_cz_krone", MoneyMod.COINS);
    public static final Supplier<Item> CZkr2 = registerItem("two_cz_krone", MoneyMod.COINS);
    public static final Supplier<Item> CZkr5 = registerItem("five_cz_krone", MoneyMod.COINS);
    public static final Supplier<Item> CZkr10 = registerItem("ten_cz_krone", MoneyMod.COINS);
    public static final Supplier<Item> CZkr20 = registerItem("twenty_cz_krone", MoneyMod.COINS);
    public static final Supplier<Item> CZkr50 = registerItem("fifty_cz_krone", MoneyMod.COINS);

    public static final Supplier<Item> DKAere50 = registerItem("fifty_aere_dk", MoneyMod.COINS);
    public static final Supplier<Item> DKkr1 = registerItem("one_dk_krone", MoneyMod.COINS);
    public static final Supplier<Item> DKkr2 = registerItem("two_dk_krone", MoneyMod.COINS);
    public static final Supplier<Item> DKkr5 = registerItem("five_dk_krone", MoneyMod.COINS);
    public static final Supplier<Item> DKkr10 = registerItem("ten_dk_krone", MoneyMod.COINS);
    public static final Supplier<Item> DKkr20 = registerItem("twenty_dk_krone", MoneyMod.COINS);

    public static final Supplier<Item> Ft5 = registerItem("five_ft", MoneyMod.COINS);
    public static final Supplier<Item> Ft10 = registerItem("ten_ft", MoneyMod.COINS);
    public static final Supplier<Item> Ft20 = registerItem("twenty_ft", MoneyMod.COINS);
    public static final Supplier<Item> Ft50 = registerItem("fifty_ft", MoneyMod.COINS);
    public static final Supplier<Item> Ft100 = registerItem("hundred_ft", MoneyMod.COINS);
    public static final Supplier<Item> Ft200 = registerItem("two_hundred_ft", MoneyMod.COINS);

    public static final Supplier<Item> NOkr1 = registerItem("one_no_krone", MoneyMod.COINS);
    public static final Supplier<Item> NOkr5 = registerItem("five_no_krone", MoneyMod.COINS);
    public static final Supplier<Item> NOkr10 = registerItem("ten_no_krone", MoneyMod.COINS);
    public static final Supplier<Item> NOkr20 = registerItem("twenty_no_krone", MoneyMod.COINS);

    public static final Supplier<Item> Grosz1 = registerItem("one_grosz", MoneyMod.COINS);
    public static final Supplier<Item> Grosz2 = registerItem("two_grosze", MoneyMod.COINS);
    public static final Supplier<Item> Grosz5 = registerItem("five_groszy", MoneyMod.COINS);
    public static final Supplier<Item> Grosz10 = registerItem("ten_groszy", MoneyMod.COINS);
    public static final Supplier<Item> Grosz20 = registerItem("twenty_groszy", MoneyMod.COINS);
    public static final Supplier<Item> Grosz50 = registerItem("fifty_groszy", MoneyMod.COINS);
    public static final Supplier<Item> Zloty1 = registerItem("one_zloty", MoneyMod.COINS);
    public static final Supplier<Item> Zloty2 = registerItem("two_zloty", MoneyMod.COINS);
    public static final Supplier<Item> Zloty5 = registerItem("five_zloty", MoneyMod.COINS);

    public static final Supplier<Item> RSD1 = registerItem("one_rs_dinar", MoneyMod.COINS);
    public static final Supplier<Item> RSD2 = registerItem("two_rs_dinar", MoneyMod.COINS);
    public static final Supplier<Item> RSD5 = registerItem("five_rs_dinar", MoneyMod.COINS);

    public static final Supplier<Item> SEkr1 = registerItem("one_se_krone", MoneyMod.COINS);
    public static final Supplier<Item> SEkr2 = registerItem("two_se_krone", MoneyMod.COINS);
    public static final Supplier<Item> SEkr5 = registerItem("five_se_krone", MoneyMod.COINS);
    public static final Supplier<Item> SEkr10 = registerItem("ten_se_krone", MoneyMod.COINS);

    public static final Supplier<Item> ISkr1 = registerItem("one_is_krone", MoneyMod.COINS);
    public static final Supplier<Item> ISkr5 = registerItem("five_is_krone", MoneyMod.COINS);
    public static final Supplier<Item> ISkr10 = registerItem("ten_is_krone", MoneyMod.COINS);
    public static final Supplier<Item> ISkr50 = registerItem("fifty_is_krone", MoneyMod.COINS);
    public static final Supplier<Item> ISkr100 = registerItem("hundred_is_krone", MoneyMod.COINS);

    public static final Supplier<Item> INr1 = registerItem("one_in_rupee", MoneyMod.COINS);
    public static final Supplier<Item> INr2 = registerItem("two_in_rupees", MoneyMod.COINS);
    public static final Supplier<Item> INr5 = registerItem("five_in_rupees", MoneyMod.COINS);
    public static final Supplier<Item> INr10 = registerItem("ten_in_rupees", MoneyMod.COINS);
    public static final Supplier<Item> INr20 = registerItem("twenty_in_rupees", MoneyMod.COINS);

    public static final Supplier<Item> Won10 = registerItem("ten_kr_won", MoneyMod.COINS);
    public static final Supplier<Item> Won50 = registerItem("fifty_kr_won", MoneyMod.COINS);
    public static final Supplier<Item> Won100 = registerItem("hundred_kr_won", MoneyMod.COINS);
    public static final Supplier<Item> Won500 = registerItem("five_hundred_kr_won", MoneyMod.COINS);

    public static final Supplier<Item> CNJiao1 = registerItem("one_cn_jiao", MoneyMod.COINS);
    public static final Supplier<Item> CNJiao5 = registerItem("five_cn_jiao", MoneyMod.COINS);

    public static final Supplier<Item> BRCentavo5 = registerItem("five_br_centavos", MoneyMod.COINS);
    public static final Supplier<Item> BRCentavo10 = registerItem("ten_br_centavos", MoneyMod.COINS);
    public static final Supplier<Item> BRCentavo25 = registerItem("twentyfive_br_centavos", MoneyMod.COINS);
    public static final Supplier<Item> BRCentavo50 = registerItem("fifty_br_centavos", MoneyMod.COINS);
    public static final Supplier<Item> BRReal1 = registerItem("one_br_real", MoneyMod.COINS);

    public static final Supplier<Item> MXCentavo5 = registerItem("five_mx_centavos", MoneyMod.COINS);
    public static final Supplier<Item> MXCentavo10 = registerItem("ten_mx_centavos", MoneyMod.COINS);
    public static final Supplier<Item> MXCentavo20 = registerItem("twenty_mx_centavos", MoneyMod.COINS);
    public static final Supplier<Item> MXCentavo50 = registerItem("fifty_mx_centavos", MoneyMod.COINS);
    public static final Supplier<Item> MXPeso1 = registerItem("one_mx_peso", MoneyMod.COINS);
    public static final Supplier<Item> MXPeso2 = registerItem("two_mx_pesos", MoneyMod.COINS);
    public static final Supplier<Item> MXPeso5 = registerItem("five_mx_pesos", MoneyMod.COINS);
    public static final Supplier<Item> MXPeso10 = registerItem("ten_mx_pesos", MoneyMod.COINS);

    public static final Supplier<Item> ZACent10 = registerItem("ten_za_cents", MoneyMod.COINS);
    public static final Supplier<Item> ZACent20 = registerItem("twenty_za_cents", MoneyMod.COINS);
    public static final Supplier<Item> ZACent50 = registerItem("fifty_za_cents", MoneyMod.COINS);
    public static final Supplier<Item> ZARand1 = registerItem("one_za_rand", MoneyMod.COINS);
    public static final Supplier<Item> ZARand2 = registerItem("two_za_rand", MoneyMod.COINS);
    public static final Supplier<Item> ZARand5 = registerItem("five_za_rand", MoneyMod.COINS);

    public static final Supplier<Item> TRk1 = registerItem("one_kurus", MoneyMod.COINS);
    public static final Supplier<Item> TRk5 = registerItem("five_kurus", MoneyMod.COINS);
    public static final Supplier<Item> TRk10 = registerItem("ten_kurus", MoneyMod.COINS);
    public static final Supplier<Item> TRk25 = registerItem("twenty_five_kurus", MoneyMod.COINS);
    public static final Supplier<Item> TRk50 = registerItem("fifty_kurus", MoneyMod.COINS);
    public static final Supplier<Item> TRl1 = registerItem("one_tr_lira", MoneyMod.COINS);

    public static final Supplier<Item> NZCent10 = registerItem("ten_nz_cents", MoneyMod.COINS);
    public static final Supplier<Item> NZCent20 = registerItem("twenty_nz_cents", MoneyMod.COINS);
    public static final Supplier<Item> NZCent50 = registerItem("fifty_nz_cents", MoneyMod.COINS);
    public static final Supplier<Item> NZD1 = registerItem("one_nz_dollar", MoneyMod.COINS);
    public static final Supplier<Item> NZD2 = registerItem("two_nz_dollars", MoneyMod.COINS);

    public static final Supplier<Item> PHS1 = registerItem("one_ph_sentimo", MoneyMod.COINS);
    public static final Supplier<Item> PHS5 = registerItem("five_ph_sentimo", MoneyMod.COINS);
    public static final Supplier<Item> PHS25 = registerItem("twenty_five_ph_sentimo", MoneyMod.COINS);
    public static final Supplier<Item> PHP1 = registerItem("one_ph_piso", MoneyMod.COINS);
    public static final Supplier<Item> PHP5 = registerItem("five_ph_piso", MoneyMod.COINS);
    public static final Supplier<Item> PHP10 = registerItem("ten_ph_piso", MoneyMod.COINS);
    /*
     * ////////////////////////////////////////////////////
     * //////////////// BANKNOTES TAB ///////////////////////
     * /////////////// BUBUSTEIN's MONEY MOD //////////////
     * ////////////////////////////////////////////////////
     * ////////////////////////////////////////////////////
     * */
    public static final Supplier<Item> Euro5 = registerItem("five_euros", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Euro10 = registerItem("ten_euros", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Euro20 = registerItem("twenty_euros", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Euro50 = registerItem("fifty_euros", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Euro100 = registerItem("hundred_euros", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Euro200 = registerItem("two_hundred_euros", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Euro500 = registerItem("five_hundred_euros", MoneyMod.BANKNOTES);

    public static final Supplier<Item> Pound5 = registerItem("five_pounds", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Pound10 = registerItem("ten_pounds", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Pound20 = registerItem("twenty_pounds", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Pound50 = registerItem("fifty_pounds", MoneyMod.BANKNOTES);

    public static final Supplier<Item> Dollar1 = registerItem("one_dollar", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Dollar5 = registerItem("five_dollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Dollar10 = registerItem("ten_dollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Dollar20 = registerItem("twenty_dollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Dollar50 = registerItem("fifty_dollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Dollar100 = registerItem("hundred_dollars", MoneyMod.BANKNOTES);

    public static final Supplier<Item> DollarC5 = registerItem("five_cdollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> DollarC10 = registerItem("ten_cdollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> DollarC20 = registerItem("twenty_cdollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> DollarC50 = registerItem("fifty_cdollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> DollarC100 = registerItem("hundred_cdollars", MoneyMod.BANKNOTES);

    public static final Supplier<Item> Leu1 = registerItem("un_leu", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Lei5 = registerItem("cinci_lei", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Lei10 = registerItem("zece_lei", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Lei20 = registerItem("douazeci_lei", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Lei50 = registerItem("cincizeci_lei", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Lei100 = registerItem("suta_lei", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Lei200 = registerItem("doua_sute_lei", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Lei500 = registerItem("cinci_sute_lei", MoneyMod.BANKNOTES);

    public static final Supplier<Item> LeiMD20 = registerItem("douazeci_lei_md", MoneyMod.BANKNOTES);
    public static final Supplier<Item> LeiMD50 = registerItem("cincizeci_lei_md", MoneyMod.BANKNOTES);
    public static final Supplier<Item> LeiMD100 = registerItem("suta_lei_md", MoneyMod.BANKNOTES);
    public static final Supplier<Item> LeiMD200 = registerItem("doua_sute_lei_md", MoneyMod.BANKNOTES);
    public static final Supplier<Item> LeiMD500 = registerItem("cinci_sute_lei_md", MoneyMod.BANKNOTES);
    public static final Supplier<Item> LeiMD1000 = registerItem("mie_lei_md", MoneyMod.BANKNOTES);

    public static final Supplier<Item> Franc10 = registerItem("ten_francs", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Franc20 = registerItem("twenty_francs", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Franc50 = registerItem("fifty_francs", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Franc100 = registerItem("hundred_francs", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Franc200 = registerItem("two_hundred_francs", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Franc1000 = registerItem("thousand_francs", MoneyMod.BANKNOTES);

    public static final Supplier<Item> DollarA5 = registerItem("five_adollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> DollarA10 = registerItem("ten_adollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> DollarA20 = registerItem("twenty_adollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> DollarA50 = registerItem("fifty_adollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> DollarA100 = registerItem("hundred_adollars", MoneyMod.BANKNOTES);

    public static final Supplier<Item> Yen1000 = registerItem("thousand_yen", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Yen5000 = registerItem("five_thousand_yen", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Yen10000 = registerItem("ten_thousand_yen", MoneyMod.BANKNOTES);

    public static final Supplier<Item> Leva5 = registerItem("five_leva", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Leva10 = registerItem("ten_leva", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Leva20 = registerItem("twenty_leva", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Leva50 = registerItem("fifty_leva", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Leva100 = registerItem("hundred_leva", MoneyMod.BANKNOTES);

    public static final Supplier<Item> CZkr100 = registerItem("hundred_cz_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> CZkr200 = registerItem("two_hundred_cz_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> CZkr500 = registerItem("five_hundred_cz_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> CZkr1000 = registerItem("thousand_cz_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> CZkr2000 = registerItem("two_thousand_cz_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> CZkr5000 = registerItem("five_thousand_cz_krone", MoneyMod.BANKNOTES);

    public static final Supplier<Item> DKkr50 = registerItem("fifty_dk_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> DKkr100 = registerItem("hundred_dk_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> DKkr200 = registerItem("two_hundred_dk_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> DKkr500 = registerItem("five_hundred_dk_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> DKkr1000 = registerItem("thousand_dk_krone", MoneyMod.BANKNOTES);

    public static final Supplier<Item> Ft500 = registerItem("five_hundred_ft", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Ft1000 = registerItem("thousand_ft", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Ft2000 = registerItem("two_thousand_ft", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Ft5000 = registerItem("five_thousand_ft", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Ft10000 = registerItem("ten_thousand_ft", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Ft20000 = registerItem("twenty_thousand_ft", MoneyMod.BANKNOTES);

    public static final Supplier<Item> NOkr50 = registerItem("fifty_no_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> NOkr100 = registerItem("hundred_no_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> NOkr200 = registerItem("two_hundred_no_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> NOkr500 = registerItem("five_hundred_no_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> NOkr1000 = registerItem("thousand_no_krone", MoneyMod.BANKNOTES);

    public static final Supplier<Item> Zloty10 = registerItem("ten_zloty", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Zloty20 = registerItem("twenty_zloty", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Zloty50 = registerItem("fifty_zloty", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Zloty100 = registerItem("hundred_zloty", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Zloty200 = registerItem("two_hundred_zloty", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Zloty500 = registerItem("five_hundred_zloty", MoneyMod.BANKNOTES);

    public static final Supplier<Item> RSD10 = registerItem("ten_rs_dinar", MoneyMod.BANKNOTES);
    public static final Supplier<Item> RSD20 = registerItem("twenty_rs_dinar", MoneyMod.BANKNOTES);
    public static final Supplier<Item> RSD50 = registerItem("fifty_rs_dinar", MoneyMod.BANKNOTES);
    public static final Supplier<Item> RSD100 = registerItem("hundred_rs_dinar", MoneyMod.BANKNOTES);
    public static final Supplier<Item> RSD200 = registerItem("two_hundred_rs_dinar", MoneyMod.BANKNOTES);
    public static final Supplier<Item> RSD500 = registerItem("five_hundred_rs_dinar", MoneyMod.BANKNOTES);
    public static final Supplier<Item> RSD1000 = registerItem("thousand_rs_dinar", MoneyMod.BANKNOTES);
    public static final Supplier<Item> RSD2000 = registerItem("two_thousand_rs_dinar", MoneyMod.BANKNOTES);
    public static final Supplier<Item> RSD5000 = registerItem("five_thousand_rs_dinar", MoneyMod.BANKNOTES);

    public static final Supplier<Item> SEkr20 = registerItem("twenty_se_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> SEkr50 = registerItem("fifty_se_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> SEkr100 = registerItem("hundred_se_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> SEkr200 = registerItem("two_hundred_se_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> SEkr500 = registerItem("five_hundred_se_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> SEkr1000 = registerItem("thousand_se_krone", MoneyMod.BANKNOTES);

    public static final Supplier<Item> ISkr500 = registerItem("five_hundred_is_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> ISkr1000 = registerItem("thousand_is_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> ISkr2000 = registerItem("two_thousand_is_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> ISkr5000 = registerItem("five_thousand_is_krone", MoneyMod.BANKNOTES);
    public static final Supplier<Item> ISkr10000 = registerItem("ten_thousand_is_krone", MoneyMod.BANKNOTES);

    public static final Supplier<Item> INr50 = registerItem("fifty_in_rupees", MoneyMod.BANKNOTES);
    public static final Supplier<Item> INr100 = registerItem("hundred_in_rupees", MoneyMod.BANKNOTES);
    public static final Supplier<Item> INr200 = registerItem("two_hundred_in_rupees", MoneyMod.BANKNOTES);
    public static final Supplier<Item> INr500 = registerItem("five_hundred_in_rupees", MoneyMod.BANKNOTES);

    public static final Supplier<Item> Won1000 = registerItem("thousand_kr_won", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Won5000 = registerItem("five_thousand_kr_won", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Won10000 = registerItem("ten_thousand_kr_won", MoneyMod.BANKNOTES);
    public static final Supplier<Item> Won50000 = registerItem("fifty_thousand_kr_won", MoneyMod.BANKNOTES);

    public static final Supplier<Item> CNYuan1 = registerItem("one_cn_yuan", MoneyMod.BANKNOTES);
    public static final Supplier<Item> CNYuan5 = registerItem("five_cn_yuan", MoneyMod.BANKNOTES);
    public static final Supplier<Item> CNYuan10 = registerItem("ten_cn_yuan", MoneyMod.BANKNOTES);
    public static final Supplier<Item> CNYuan20 = registerItem("twenty_cn_yuan", MoneyMod.BANKNOTES);
    public static final Supplier<Item> CNYuan50 = registerItem("fifty_cn_yuan", MoneyMod.BANKNOTES);
    public static final Supplier<Item> CNYuan100 = registerItem("hundred_cn_yuan", MoneyMod.BANKNOTES);

    public static final Supplier<Item> BRReal2 = registerItem("two_br_reais", MoneyMod.BANKNOTES);
    public static final Supplier<Item> BRReal5 = registerItem("five_br_reais", MoneyMod.BANKNOTES);
    public static final Supplier<Item> BRReal10 = registerItem("ten_br_reais", MoneyMod.BANKNOTES);
    public static final Supplier<Item> BRReal20 = registerItem("twenty_br_reais", MoneyMod.BANKNOTES);
    public static final Supplier<Item> BRReal50 = registerItem("fifty_br_reais", MoneyMod.BANKNOTES);
    public static final Supplier<Item> BRReal100 = registerItem("hundred_br_reais", MoneyMod.BANKNOTES);
    public static final Supplier<Item> BRReal200 = registerItem("two_hundred_br_reais", MoneyMod.BANKNOTES);

    public static final Supplier<Item> MXPeso20 = registerItem("twenty_mx_pesos", MoneyMod.BANKNOTES);
    public static final Supplier<Item> MXPeso50 = registerItem("fifty_mx_pesos", MoneyMod.BANKNOTES);
    public static final Supplier<Item> MXPeso100 = registerItem("hundred_mx_pesos", MoneyMod.BANKNOTES);
    public static final Supplier<Item> MXPeso200 = registerItem("two_hundred_mx_pesos", MoneyMod.BANKNOTES);
    public static final Supplier<Item> MXPeso500 = registerItem("five_hundred_mx_pesos", MoneyMod.BANKNOTES);
    public static final Supplier<Item> MXPeso1000 = registerItem("thousand_mx_pesos", MoneyMod.BANKNOTES);

    public static final Supplier<Item> ZARand10 = registerItem("ten_za_rand", MoneyMod.BANKNOTES);
    public static final Supplier<Item> ZARand20 = registerItem("twenty_za_rand", MoneyMod.BANKNOTES);
    public static final Supplier<Item> ZARand50 = registerItem("fifty_za_rand", MoneyMod.BANKNOTES);
    public static final Supplier<Item> ZARand100 = registerItem("hundred_za_rand", MoneyMod.BANKNOTES);
    public static final Supplier<Item> ZARand200 = registerItem("two_hundred_za_rand", MoneyMod.BANKNOTES);

    public static final Supplier<Item> TRl5 = registerItem("five_tr_lira", MoneyMod.BANKNOTES);
    public static final Supplier<Item> TRl10 = registerItem("ten_tr_lira", MoneyMod.BANKNOTES);
    public static final Supplier<Item> TRl20 = registerItem("twenty_tr_lira", MoneyMod.BANKNOTES);
    public static final Supplier<Item> TRl50 = registerItem("fifty_tr_lira", MoneyMod.BANKNOTES);
    public static final Supplier<Item> TRl100 = registerItem("hundred_tr_lira", MoneyMod.BANKNOTES);
    public static final Supplier<Item> TRl200 = registerItem("two_hundred_tr_lira", MoneyMod.BANKNOTES);

    public static final Supplier<Item> NZD5 = registerItem("five_nz_dollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> NZD10 = registerItem("ten_nz_dollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> NZD20 = registerItem("twenty_nz_dollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> NZD50 = registerItem("fifty_nz_dollars", MoneyMod.BANKNOTES);
    public static final Supplier<Item> NZD100 = registerItem("hundred_nz_dollars", MoneyMod.BANKNOTES);

    public static final Supplier<Item> PHP20 = registerItem("twenty_ph_piso", MoneyMod.BANKNOTES);
    public static final Supplier<Item> PHP50 = registerItem("fifty_ph_piso", MoneyMod.BANKNOTES);
    public static final Supplier<Item> PHP100 = registerItem("hundred_ph_piso", MoneyMod.BANKNOTES);
    public static final Supplier<Item> PHP200 = registerItem("two_hundred_ph_piso", MoneyMod.BANKNOTES);
    public static final Supplier<Item> PHP500 = registerItem("five_hundred_ph_piso", MoneyMod.BANKNOTES);
    public static final Supplier<Item> PHP1000 = registerItem("thousand_ph_piso", MoneyMod.BANKNOTES);
    /*
     * ////////////////////////////////////////////////////
     * //////////////// SPECIAL TAB ///////////////////////
     * /////////////// BUBUSTEIN's MONEY MOD //////////////
     * ////////////////////////////////////////////////////
     * ////////////////////////////////////////////////////
     * */
    public static final Supplier<Item> B1 = registerItem("un_ban_1900", MoneyMod.SPECIAL, Rarity.UNCOMMON);
    public static final Supplier<Item> B2 = registerItem("doi_bani_1900", MoneyMod.SPECIAL, Rarity.UNCOMMON);
    public static final Supplier<Item> B5 = registerItem("cinci_bani_1900", MoneyMod.SPECIAL, Rarity.UNCOMMON);
    public static final Supplier<Item> B10 = registerItem("zece_bani_1900", MoneyMod.SPECIAL, Rarity.UNCOMMON);
    public static final Supplier<Item> B20 = registerItem("douazeci_bani_1900", MoneyMod.SPECIAL, Rarity.UNCOMMON);
    public static final Supplier<Item> B50 = registerItem("cincizeci_bani_1900", MoneyMod.SPECIAL, Rarity.UNCOMMON);
    public static final Supplier<Item> L1 = registerItem("un_leu_1900", MoneyMod.SPECIAL, Rarity.UNCOMMON);
    public static final Supplier<Item> L2 = registerItem("doi_lei_1900", MoneyMod.SPECIAL, Rarity.UNCOMMON);
    public static final Supplier<Item> L5 = registerItem("cinci_lei_1900", MoneyMod.SPECIAL, Rarity.RARE);
    public static final Supplier<Item> L12 = registerItem("douasprezece_lei_1900", MoneyMod.SPECIAL, Rarity.RARE);
    public static final Supplier<Item> L20 = registerItem("douazeci_lei_1900", MoneyMod.SPECIAL, Rarity.RARE);
    public static final Supplier<Item> L25 = registerItem("douazeci_cinci_lei_1900", MoneyMod.SPECIAL, Rarity.RARE, true);
    public static final Supplier<Item> L50 = registerItem("cincizeci_lei_1900", MoneyMod.SPECIAL, Rarity.EPIC, true);
    public static final Supplier<Item> L100 = registerItem("suta_lei_1900", MoneyMod.SPECIAL, Rarity.EPIC, true);

    public static final Supplier<Item> VisaClassic = MoneyExpectPlatform.registerItem("card_classic",
            () -> new CardItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).arch$tab(MoneyMod.SPECIAL)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "card_classic")))));
    public static final Supplier<Item> VisaGold = MoneyExpectPlatform.registerItem("card_gold",
            () -> new CardItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).arch$tab(MoneyMod.SPECIAL)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "card_gold")))));
    public static final Supplier<Item> VisaSteel = MoneyExpectPlatform.registerItem("card_steel",
            () -> new CardItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).arch$tab(MoneyMod.SPECIAL)
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MoneyMod.MOD_ID, "card_steel")))));

    public static final Supplier<Item> SpecialPaper = registerItem("special_paper", MoneyMod.SPECIAL);

    public static final Map<String, Double> EXCHANGE_RATES = new HashMap<>();
    public static void registerExchangeRates() {
        // EXCHANGE RATES FROM EUR TO OTHER - LAST UPDATED ON 26th DECEMBER
        EXCHANGE_RATES.put("EUR", 1.00);
        EXCHANGE_RATES.put("USD", 1.04);
        EXCHANGE_RATES.put("GBP", 0.83);
        EXCHANGE_RATES.put("CAD", 1.50);
        EXCHANGE_RATES.put("RON", 4.98);
        EXCHANGE_RATES.put("MDL", 19.18);
        EXCHANGE_RATES.put("CHF", 0.94);
        EXCHANGE_RATES.put("AUD", 1.67);
        EXCHANGE_RATES.put("JPY", 163.92);
        EXCHANGE_RATES.put("BGN", 1.96);
        EXCHANGE_RATES.put("CZK", 25.11);
        EXCHANGE_RATES.put("NOK", 11.84);
        EXCHANGE_RATES.put("DKK", 7.46);
        EXCHANGE_RATES.put("SEK", 11.5);
        EXCHANGE_RATES.put("HUF", 409.77);
        EXCHANGE_RATES.put("PLN", 4.26);
        EXCHANGE_RATES.put("RSD", 116.96);
        EXCHANGE_RATES.put("ISK", 145.1);
        EXCHANGE_RATES.put("CNY", 7.59);
        EXCHANGE_RATES.put("INR",88.66);
        EXCHANGE_RATES.put("KRW", 1523.05);
        EXCHANGE_RATES.put("BRL", 6.42);
        EXCHANGE_RATES.put("MXN", 21.00);
        EXCHANGE_RATES.put("ZAR", 19.36);
        EXCHANGE_RATES.put("TRY", 36.61);
        EXCHANGE_RATES.put("NZD", 1.84);
        EXCHANGE_RATES.put("PHP", 60.28);
    }
    public static final Map<String, TreeMap<Double, Item>> CURRENCY_ITEMS = new HashMap<>();
    public static void registerCurrencyItems(){
        // Euro
        TreeMap<Double, Item> euroItems = new TreeMap<>(Comparator.naturalOrder());
        euroItems.put(500.0, Euro500.get());
        euroItems.put(200.0, Euro200.get());
        euroItems.put(100.0, Euro100.get());
        euroItems.put(50.0, Euro50.get());
        euroItems.put(20.0, Euro20.get());
        euroItems.put(10.0, Euro10.get());
        euroItems.put(5.0, Euro5.get());
        euroItems.put(2.0, Euro2.get());
        euroItems.put(1.0, Euro1.get());
        euroItems.put(0.5, Ecent50.get());
        euroItems.put(0.2, Ecent20.get());
        euroItems.put(0.1, Ecent10.get());
        euroItems.put(0.05, Ecent5.get());
        euroItems.put(0.02, Ecent2.get());
        euroItems.put(0.01, Ecent1.get());
        CURRENCY_ITEMS.put("EUR", euroItems);
        // USD
        TreeMap<Double, Item> usdItems = new TreeMap<>(Comparator.naturalOrder());
        usdItems.put(100.0, Dollar100.get());
        usdItems.put(50.0, Dollar50.get());
        usdItems.put(20.0, Dollar20.get());
        usdItems.put(10.0, Dollar10.get());
        usdItems.put(5.0, Dollar5.get());
        usdItems.put(1.0, Dollar1.get());
        usdItems.put(0.5, Cent50.get());
        usdItems.put(0.25, Cent25.get());
        usdItems.put(0.1, Cent10.get());
        usdItems.put(0.05, Cent5.get());
        usdItems.put(0.01, Cent1.get());
        CURRENCY_ITEMS.put("USD", usdItems);
        // RON
        TreeMap<Double, Item> ronItems = new TreeMap<>(Comparator.naturalOrder());
        ronItems.put(500.0, Lei500.get());
        ronItems.put(200.0, Lei200.get());
        ronItems.put(100.0, Lei100.get());
        ronItems.put(50.0, Lei50.get());
        ronItems.put(20.0, Lei20.get());
        ronItems.put(10.0, Lei10.get());
        ronItems.put(5.0, Lei5.get());
        ronItems.put(1.0, Leu1.get());
        ronItems.put(0.5, Bani50.get());
        ronItems.put(0.1, Bani10.get());
        ronItems.put(0.05, Bani5.get());
        ronItems.put(0.01, Ban1.get());
        CURRENCY_ITEMS.put("RON", ronItems);
        // GBP
        TreeMap<Double, Item> gbpItems = new TreeMap<>(Comparator.naturalOrder());
        gbpItems.put(50.0, Pound50.get());
        gbpItems.put(20.0, Pound20.get());
        gbpItems.put(10.0, Pound10.get());
        gbpItems.put(5.0, Pound5.get());
        gbpItems.put(2.0, Pound2.get());
        gbpItems.put(1.0, Pound1.get());
        gbpItems.put(0.5, Pence50.get());
        gbpItems.put(0.2, Pence20.get());
        gbpItems.put(0.1, Pence10.get());
        gbpItems.put(0.05, Pence5.get());
        gbpItems.put(0.02, Pence2.get());
        gbpItems.put(0.01, Pence1.get());
        CURRENCY_ITEMS.put("GBP", gbpItems);
        // CAD
        TreeMap<Double, Item> cadItems = new TreeMap<>(Comparator.naturalOrder());
        cadItems.put(100.0, DollarC100.get());
        cadItems.put(50.0, DollarC50.get());
        cadItems.put(20.0, DollarC20.get());
        cadItems.put(10.0, DollarC10.get());
        cadItems.put(5.0, DollarC5.get());
        cadItems.put(2.0, Toonie.get());
        cadItems.put(1.0, Loonie.get());
        cadItems.put(0.25, CCent25.get());
        cadItems.put(0.1, CCent10.get());
        cadItems.put(0.05, CCent5.get());
        CURRENCY_ITEMS.put("CAD", cadItems);
        // MDL
        TreeMap<Double, Item> mdlItems = new TreeMap<>(Comparator.naturalOrder());
        mdlItems.put(1000.0, LeiMD1000.get());
        mdlItems.put(500.0, LeiMD500.get());
        mdlItems.put(200.0, LeiMD200.get());
        mdlItems.put(100.0, LeiMD100.get());
        mdlItems.put(50.0, LeiMD50.get());
        mdlItems.put(20.0, LeiMD20.get());
        mdlItems.put(10.0, LeiMD10.get());
        mdlItems.put(5.0, LeiMD5.get());
        mdlItems.put(2.0, LeuMD2.get());
        mdlItems.put(1.0, LeuMD1.get());
        mdlItems.put(0.5, BaniMD50.get());
        mdlItems.put(0.25, BaniMD25.get());
        mdlItems.put(0.1, BaniMD10.get());
        mdlItems.put(0.05, BanMD5.get());
        CURRENCY_ITEMS.put("MDL", mdlItems);
        // CHF
        TreeMap<Double, Item> chfItems = new TreeMap<>(Comparator.naturalOrder());
        chfItems.put(1000.0, Franc1000.get());
        chfItems.put(200.0, Franc200.get());
        chfItems.put(100.0, Franc100.get());
        chfItems.put(50.0, Franc50.get());
        chfItems.put(20.0, Franc20.get());
        chfItems.put(10.0, Franc10.get());
        chfItems.put(5.0, Franc5.get());
        chfItems.put(2.0, Franc2.get());
        chfItems.put(1.0, Franc1.get());
        chfItems.put(0.5, HalfFranc.get());
        chfItems.put(0.2, Centimes20.get());
        chfItems.put(0.1, Centimes10.get());
        chfItems.put(0.05, Centimes5.get());
        CURRENCY_ITEMS.put("CHF", chfItems);
        // AUD
        TreeMap<Double, Item> audItems = new TreeMap<>(Comparator.naturalOrder());
        audItems.put(100.0, DollarA100.get());
        audItems.put(50.0, DollarA50.get());
        audItems.put(20.0, DollarA20.get());
        audItems.put(10.0, Dollar10.get());
        audItems.put(5.0, DollarA5.get());
        audItems.put(2.0, DollarA2.get());
        audItems.put(1.0, DollarA1.get());
        audItems.put(0.5, ACent50.get());
        audItems.put(0.2, ACent20.get());
        audItems.put(0.1, ACent10.get());
        audItems.put(0.05, ACent5.get());
        CURRENCY_ITEMS.put("AUD", audItems);
        // JPY
        TreeMap<Double, Item> jpyItems = new TreeMap<>(Comparator.naturalOrder());
        jpyItems.put(10000.0, Yen10000.get());
        jpyItems.put(5000.0, Yen5000.get());
        jpyItems.put(1000.0, Yen1000.get());
        jpyItems.put(500.0, Yen500.get());
        jpyItems.put(100.0, Yen100.get());
        jpyItems.put(50.0, Yen50.get());
        jpyItems.put(10.0, Yen10.get());
        jpyItems.put(5.0, Yen5.get());
        jpyItems.put(1.0, Yen1.get());
        CURRENCY_ITEMS.put("JPY", jpyItems);
        // BGN
        TreeMap<Double, Item> bgnItems = new TreeMap<>(Comparator.naturalOrder());
        bgnItems.put(100.0, Leva100.get());
        bgnItems.put(50.0, Leva50.get());
        bgnItems.put(20.0, Leva20.get());
        bgnItems.put(10.0, Leva10.get());
        bgnItems.put(5.0, Leva5.get());
        bgnItems.put(2.0, Leva2.get());
        bgnItems.put(1.0, Leva1.get());
        bgnItems.put(0.5, Stotinka50.get());
        bgnItems.put(0.2, Stotinka20.get());
        bgnItems.put(0.1, Stotinka10.get());
        bgnItems.put(0.05, Stotinka5.get());
        bgnItems.put(0.02, Stotinka2.get());
        bgnItems.put(0.01, Stotinka1.get());
        CURRENCY_ITEMS.put("BGN", bgnItems);
        // CZK
        TreeMap<Double, Item> czkItems = new TreeMap<>(Comparator.naturalOrder());
        czkItems.put(5000.0, CZkr5000.get());
        czkItems.put(2000.0, CZkr2000.get());
        czkItems.put(1000.0, CZkr1000.get());
        czkItems.put(500.0, CZkr500.get());
        czkItems.put(200.0, CZkr200.get());
        czkItems.put(100.0, CZkr100.get());
        czkItems.put(50.0, CZkr50.get());
        czkItems.put(20.0, CZkr20.get());
        czkItems.put(10.0, CZkr10.get());
        czkItems.put(5.0, CZkr5.get());
        czkItems.put(2.0, CZkr2.get());
        czkItems.put(1.0, CZkr1.get());
        CURRENCY_ITEMS.put("CZK", czkItems);
        // NOK
        TreeMap<Double, Item> nokItems = new TreeMap<>(Comparator.naturalOrder());
        nokItems.put(1000.0, NOkr1000.get());
        nokItems.put(500.0, NOkr500.get());
        nokItems.put(200.0, NOkr200.get());
        nokItems.put(100.0, NOkr100.get());
        nokItems.put(50.0, NOkr50.get());
        nokItems.put(20.0, NOkr20.get());
        nokItems.put(10.0, NOkr10.get());
        nokItems.put(5.0, NOkr5.get());
        nokItems.put(1.0, NOkr1.get());
        CURRENCY_ITEMS.put("NOK", nokItems);
        // DKK
        TreeMap<Double, Item> dkkItems = new TreeMap<>(Comparator.naturalOrder());
        dkkItems.put(1000.0, DKkr1000.get());
        dkkItems.put(500.0, DKkr500.get());
        dkkItems.put(200.0, DKkr200.get());
        dkkItems.put(100.0, DKkr100.get());
        dkkItems.put(50.0, DKkr50.get());
        dkkItems.put(20.0, DKkr20.get());
        dkkItems.put(10.0, DKkr10.get());
        dkkItems.put(5.0, DKkr5.get());
        dkkItems.put(2.0, DKkr2.get());
        dkkItems.put(1.0, DKkr1.get());
        dkkItems.put(0.5, DKAere50.get());
        CURRENCY_ITEMS.put("DKK", dkkItems);
        // HUF
        TreeMap<Double, Item> hufItems = new TreeMap<>(Comparator.naturalOrder());
        hufItems.put(20000.0, Ft20000.get());
        hufItems.put(10000.0, Ft10000.get());
        hufItems.put(5000.0, Ft5000.get());
        hufItems.put(2000.0, Ft2000.get());
        hufItems.put(1000.0, Ft1000.get());
        hufItems.put(500.0, Ft500.get());
        hufItems.put(200.0, Ft200.get());
        hufItems.put(100.0, Ft100.get());
        hufItems.put(50.0, Ft50.get());
        hufItems.put(20.0, Ft20.get());
        hufItems.put(10.0, Ft10.get());
        hufItems.put(5.0, Ft5.get());
        CURRENCY_ITEMS.put("HUF", hufItems);
        // PLN
        TreeMap<Double, Item> plnItems = new TreeMap<>(Comparator.naturalOrder());
        plnItems.put(500.0, Zloty500.get());
        plnItems.put(200.0, Zloty200.get());
        plnItems.put(100.0, Zloty100.get());
        plnItems.put(50.0, Zloty50.get());
        plnItems.put(20.0, Zloty20.get());
        plnItems.put(10.0, Zloty10.get());
        plnItems.put(5.0, Zloty5.get());
        plnItems.put(2.0, Zloty2.get());
        plnItems.put(1.0, Zloty1.get());
        plnItems.put(0.5, Grosz50.get());
        plnItems.put(0.2, Grosz20.get());
        plnItems.put(0.1, Grosz10.get());
        plnItems.put(0.05, Grosz5.get());
        plnItems.put(0.02, Grosz2.get());
        plnItems.put(0.01, Grosz1.get());
        CURRENCY_ITEMS.put("PLN", plnItems);
        // RSD
        TreeMap<Double, Item> rsdItems = new TreeMap<>(Comparator.naturalOrder());
        rsdItems.put(5000.0, RSD5000.get());
        rsdItems.put(2000.0, RSD2000.get());
        rsdItems.put(1000.0, RSD1000.get());
        rsdItems.put(500.0, RSD500.get());
        rsdItems.put(200.0, RSD200.get());
        rsdItems.put(100.0, RSD100.get());
        rsdItems.put(50.0, RSD50.get());
        rsdItems.put(20.0, RSD20.get());
        rsdItems.put(10.0, RSD10.get());
        rsdItems.put(5.0, RSD5.get());
        rsdItems.put(2.0, RSD2.get());
        rsdItems.put(1.0, RSD1.get());
        CURRENCY_ITEMS.put("RSD", rsdItems);
        // SEK
        TreeMap<Double, Item> sekItems = new TreeMap<>(Comparator.naturalOrder());
        sekItems.put(1000.0, SEkr1000.get());
        sekItems.put(500.0, SEkr500.get());
        sekItems.put(200.0, SEkr200.get());
        sekItems.put(100.0, SEkr100.get());
        sekItems.put(50.0, SEkr50.get());
        sekItems.put(20.0, SEkr20.get());
        sekItems.put(10.0, SEkr10.get());
        sekItems.put(5.0, SEkr5.get());
        sekItems.put(2.0, SEkr2.get());
        sekItems.put(1.0, SEkr1.get());
        CURRENCY_ITEMS.put("SEK", sekItems);
        // ISK
        TreeMap<Double, Item> iskItems = new TreeMap<>(Comparator.naturalOrder());
        iskItems.put(10000.0, ISkr10000.get());
        iskItems.put(5000.0, ISkr5000.get());
        iskItems.put(2000.0, ISkr2000.get());
        iskItems.put(1000.0, ISkr1000.get());
        iskItems.put(500.0, ISkr500.get());
        iskItems.put(100.0, ISkr100.get());
        iskItems.put(50.0, ISkr50.get());
        iskItems.put(10.0, ISkr10.get());
        iskItems.put(5.0, ISkr5.get());
        iskItems.put(1.0, ISkr1.get());
        CURRENCY_ITEMS.put("ISK", iskItems);
        // INR
        TreeMap<Double, Item> inrItems = new TreeMap<>(Comparator.naturalOrder());
        inrItems.put(500.0, INr500.get());
        inrItems.put(200.0, INr200.get());
        inrItems.put(100.0, INr100.get());
        inrItems.put(50.0, INr50.get());
        inrItems.put(20.0, INr20.get());
        inrItems.put(10.0, INr10.get());
        inrItems.put(5.0, INr5.get());
        inrItems.put(2.0, INr2.get());
        inrItems.put(1.0, INr1.get());
        CURRENCY_ITEMS.put("INR", inrItems);
        // KRW
        TreeMap<Double, Item> krwItems = new TreeMap<>(Comparator.naturalOrder());
        krwItems.put(50000.0, Won50000.get());
        krwItems.put(10000.0, Won10000.get());
        krwItems.put(5000.0, Won5000.get());
        krwItems.put(1000.0, Won1000.get());
        krwItems.put(500.0, Won500.get());
        krwItems.put(100.0, Won100.get());
        krwItems.put(50.0, Won50.get());
        krwItems.put(10.0, Won10.get());
        CURRENCY_ITEMS.put("KRW", krwItems);
        // CNY
        TreeMap<Double, Item> cnyItems = new TreeMap<>(Comparator.naturalOrder());
        cnyItems.put(100.0, CNYuan100.get());
        cnyItems.put(50.0, CNYuan50.get());
        cnyItems.put(20.0, CNYuan20.get());
        cnyItems.put(10.0, CNYuan10.get());
        cnyItems.put(5.0, CNYuan5.get());
        cnyItems.put(1.0, CNYuan1.get());
        cnyItems.put(0.5, CNJiao5.get());
        cnyItems.put(0.1, CNJiao1.get());
        CURRENCY_ITEMS.put("CNY", cnyItems);
        // BRL
        TreeMap<Double, Item> brlItems = new TreeMap<>(Comparator.naturalOrder());
        brlItems.put(200.0, BRReal200.get());
        brlItems.put(100.0, BRReal100.get());
        brlItems.put(50.0, BRReal50.get());
        brlItems.put(20.0, BRReal20.get());
        brlItems.put(10.0, BRReal10.get());
        brlItems.put(5.0, BRReal5.get());
        brlItems.put(2.0, BRReal2.get());
        brlItems.put(1.0, BRReal1.get());
        brlItems.put(0.5, BRCentavo50.get());
        brlItems.put(0.25, BRCentavo25.get());
        brlItems.put(0.1, BRCentavo10.get());
        brlItems.put(0.05, BRCentavo5.get());
        CURRENCY_ITEMS.put("BRL", brlItems);
        // MXN
        TreeMap<Double, Item> mxnItems = new TreeMap<>(Comparator.naturalOrder());
        mxnItems.put(1000.0, MXPeso1000.get());
        mxnItems.put(500.0, MXPeso500.get());
        mxnItems.put(200.0, MXPeso200.get());
        mxnItems.put(100.0, MXPeso100.get());
        mxnItems.put(50.0, MXPeso50.get());
        mxnItems.put(20.0, MXPeso20.get());
        mxnItems.put(10.0, MXPeso10.get());
        mxnItems.put(5.0, MXPeso5.get());
        mxnItems.put(2.0, MXPeso2.get());
        mxnItems.put(1.0, MXPeso1.get());
        mxnItems.put(0.5, MXCentavo50.get());
        mxnItems.put(0.2, MXCentavo20.get());
        mxnItems.put(0.1, MXCentavo10.get());
        mxnItems.put(0.05, MXCentavo5.get());
        CURRENCY_ITEMS.put("MXN", mxnItems);
        // ZAR
        TreeMap<Double, Item> zarItems = new TreeMap<>(Comparator.naturalOrder());
        zarItems.put(200.0, ZARand200.get());
        zarItems.put(100.0, ZARand100.get());
        zarItems.put(50.0, ZARand50.get());
        zarItems.put(20.0, ZARand20.get());
        zarItems.put(10.0, ZARand10.get());
        zarItems.put(5.0, ZARand5.get());
        zarItems.put(2.0, ZARand2.get());
        zarItems.put(1.0, ZARand1.get());
        zarItems.put(0.5, ZACent50.get());
        zarItems.put(0.2, ZACent20.get());
        zarItems.put(0.1, ZACent10.get());
        CURRENCY_ITEMS.put("ZAR", zarItems);
        // TRY
        TreeMap<Double, Item> tryItems = new TreeMap<>(Comparator.naturalOrder());
        tryItems.put(200.0, TRl200.get());
        tryItems.put(100.0, TRl100.get());
        tryItems.put(50.0, TRl50.get());
        tryItems.put(20.0, TRl20.get());
        tryItems.put(10.0, TRl10.get());
        tryItems.put(5.0, TRl5.get());
        tryItems.put(1.0, TRl1.get());
        tryItems.put(0.5, TRk50.get());
        tryItems.put(0.25, TRk25.get());
        tryItems.put(0.1, TRk10.get());
        tryItems.put(0.05, TRk5.get());
        tryItems.put(0.01, TRk1.get());
        CURRENCY_ITEMS.put("TRY", tryItems);
        // NZD
        TreeMap<Double, Item> nzdItems = new TreeMap<>(Comparator.naturalOrder());
        nzdItems.put(100.0, NZD100.get());
        nzdItems.put(50.0, NZD50.get());
        nzdItems.put(20.0, NZD20.get());
        nzdItems.put(10.0, NZD10.get());
        nzdItems.put(5.0, NZD5.get());
        nzdItems.put(2.0, NZD2.get());
        nzdItems.put(1.0, NZD1.get());
        nzdItems.put(0.5, NZCent50.get());
        nzdItems.put(0.2, NZCent20.get());
        nzdItems.put(0.1, NZCent10.get());
        CURRENCY_ITEMS.put("NZD", nzdItems);
        // PHP
        TreeMap<Double, Item> phpItems = new TreeMap<>(Comparator.naturalOrder());
        phpItems.put(1000.0, PHP1000.get());
        phpItems.put(500.0, PHP500.get());
        phpItems.put(200.0, PHP200.get());
        phpItems.put(100.0, PHP100.get());
        phpItems.put(50.0, PHP50.get());
        phpItems.put(20.0, PHP20.get());
        phpItems.put(10.0, PHP10.get());
        phpItems.put(5.0, PHP5.get());
        phpItems.put(1.0, PHP1.get());
        phpItems.put(0.25, PHS25.get());
        phpItems.put(0.05, PHS5.get());
        phpItems.put(0.01, PHS1.get());
        CURRENCY_ITEMS.put("PHP", phpItems);
    }
}