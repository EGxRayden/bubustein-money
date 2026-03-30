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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import tk.bubustein.money.item.ModItems;
import tk.bubustein.money.util.CardUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

/**
 * Client-side ATM screen.
 * <p>
 * Layout (176 × 200):
 *   [0  – 15]  Dark-green header bar ("ATM" title)
 *   [16 – 42]  Card slot area  (slot rendered at 80, 20)
 *   [43 – 64]  Balance line
 *   [65 – 80]  Amount row  ( [−]  [amount box]  [+] )
 *   [81 – 100] Action buttons ( [DEPOSIT]  [WITHDRAW] )
 *   [101– 200] Player inventory (standard 3 rows + hotbar)
 * <p>
 * Colours (ARGB):
 *   Header        0xFF1A6B35  (dark green)
 *   Panel bg      0xFF2C2C2C  (dark charcoal)
 *   Slot bg       0xFF3C3C3C
 *   Amount box    0xFF1C1C1C
 *   Inv bg        0xFF3C3C3C
 *   Text white    0xFFFFFFFF
 *   Text green    0xFF4CAF50
 *   Text yellow   0xFFFFEB3B
 *   Text red      0xFFF44336
 */
@Environment(EnvType.CLIENT)
public class ATMScreen extends AbstractContainerScreen<ATMMenu> {

    // ── Layout constants ────────────────────────────────────────────────────
    private static final int GUI_W = 176;
    private static final int GUI_H = 210;

    // Slot position (must match ATMMenu)
    private static final int CARD_SLOT_X = 80;
    private static final int CARD_SLOT_Y = 20;

    // ── Colours ─────────────────────────────────────────────────────────────
    private static final int COL_HEADER      = 0xFF1A6B35;
    private static final int COL_PANEL       = 0xFF2C2C2C;
    private static final int COL_SLOT_BG     = 0xFF3C3C3C;
    private static final int COL_SLOT_BORDER = 0xFF555555;
    private static final int COL_AMOUNT_BOX  = 0xFF1C1C1C;
    private static final int COL_INV_BG      = 0xFF3C3C3C;
    private static final int COL_SLOT_ITEM   = 0xFF484848;

    private static final int COL_BTN_DEPOSIT  = 0xFF2E7D32; // green
    private static final int COL_BTN_WITHDRAW = 0xFFB71C1C; // red
    private static final int COL_BTN_DISABLED = 0xFF555555;
    private static final int COL_BTN_BORDER   = 0xFF000000;

    private static final int COL_WHITE  = 0xFFFFFFFF;
    private static final int COL_GREEN  = 0xFF4CAF50;
    private static final int COL_YELLOW = 0xFFFFEB3B;
    private static final int COL_RED    = 0xFFF44336;
    private static final int COL_GRAY   = 0xFFAAAAAA;

    // ── State ────────────────────────────────────────────────────────────────
    /** Currently selected withdrawal/deposit amount. */
    private double selectedAmount = 1.0;

    /** Denomination list built from the card's currency (or a safe default). */
    private List<Double> denominations = new ArrayList<>();
    private int denomIndex = 6; // index into denominations list

    /** Last known card currency, read from the card item's data component. */
    private String cardCurrency = "EUR";

    private Button depositButton;
    private Button withdrawButton;
    private Button decreaseButton;
    private Button increaseButton;

    // ── Constructor ──────────────────────────────────────────────────────────

    public ATMScreen(ATMMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth  = GUI_W;
        this.imageHeight = GUI_H;
        // Move the "Inventory" label so it sits just above the hotbar row
        this.inventoryLabelY = 111;
        buildDefaultDenominations();
    }

    // ── Denomination helpers ─────────────────────────────────────────────────

    private void buildDefaultDenominations() {
        denominations.clear();
        denominations.add(0.01);
        denominations.add(0.05);
        denominations.add(0.10);
        denominations.add(0.50);
        denominations.add(1.0);
        denominations.add(2.0);
        denominations.add(5.0);
        denominations.add(10.0);
        denominations.add(20.0);
        denominations.add(50.0);
        denominations.add(100.0);
        denominations.add(200.0);
        denominations.add(500.0);
        denominations.add(1000.0);
        denominations.add(5000.0);
        denominations.add(10000.0);
        // Default: select "1.0"
        denomIndex = denominations.indexOf(1.0);
        if (denomIndex < 0) denomIndex = 6;
        selectedAmount = denominations.get(denomIndex);
    }

    /**
     * Rebuild denomination list from the currency if {@link ModItems#getCurrencyItems()} contains it.
     * Falls back to the default list if the currency is not found.
     */
    private void refreshDenominationsForCurrency(String currency) {
        try {
            NavigableMap<Double, ?> currencyMap = ModItems.getCurrencyItems().get(currency);
            if (currencyMap != null && !currencyMap.isEmpty()) {
                List<Double> newList = new ArrayList<>(currencyMap.keySet());
                if (!newList.equals(denominations)) {
                    denominations = newList;
                    denomIndex = Math.min(denomIndex, denominations.size() - 1);
                    selectedAmount = denominations.get(denomIndex);
                }
                return;
            }
        } catch (Exception ignored) {}
        if (denominations.size() != 16) {
            buildDefaultDenominations();
        }
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();

        int lx = this.leftPos;
        int ty = this.topPos;

        // Decrease button "−"
        decreaseButton = Button.builder(
                        Component.literal("−"),
                        btn -> stepAmount(-1))
                .pos(lx + 20, ty + 65)
                .size(20, 16)
                .build();

        // Increase button "+"
        increaseButton = Button.builder(
                        Component.literal("+"),
                        btn -> stepAmount(1))
                .pos(lx + 136, ty + 65)
                .size(20, 16)
                .build();

        // DEPOSIT button
        depositButton = Button.builder(
                        Component.literal("DEPOSIT"),
                        btn -> onDeposit())
                .pos(lx + 10, ty + 86)
                .size(74, 20)
                .build();

        // WITHDRAW button
        withdrawButton = Button.builder(
                        Component.literal("WITHDRAW"),
                        btn -> onWithdraw())
                .pos(lx + 92, ty + 86)
                .size(74, 20)
                .build();

        this.addRenderableWidget(decreaseButton);
        this.addRenderableWidget(increaseButton);
        this.addRenderableWidget(depositButton);
        this.addRenderableWidget(withdrawButton);
    }

    // ── Button callbacks ─────────────────────────────────────────────────────

    private void stepAmount(int direction) {
        int newIndex = denomIndex + direction;
        if (newIndex < 0) newIndex = 0;
        if (newIndex >= denominations.size()) newIndex = denominations.size() - 1;
        denomIndex = newIndex;
        selectedAmount = denominations.get(denomIndex);
    }

    private void onDeposit() {
        if (menu.getStatus() < 1) return; // no valid card
        ATMActionPacket.send(0, selectedAmount);
    }

    private void onWithdraw() {
        if (menu.getStatus() < 1) return; // no valid card
        ATMActionPacket.send(1, selectedAmount);
    }

    // ── Update loop ──────────────────────────────────────────────────────────

    @Override
    protected void containerTick() {
        super.containerTick();

        boolean hasCard = menu.getStatus() == 1;
        if (hasCard) {
            ItemStack cardStack = menu.slots.getFirst().getItem();
            if (!cardStack.isEmpty()) {
                String currency = cardStack.get(CardUtils.CURRENCY_COMPONENT.get());
                if (currency != null && !currency.isEmpty() && !currency.equals(cardCurrency)) {
                    cardCurrency = currency;
                    refreshDenominationsForCurrency(cardCurrency);
                }
            }
        }

        // Enable/disable action buttons
        depositButton.active  = hasCard;
        withdrawButton.active = hasCard;
        decreaseButton.active = hasCard && denomIndex > 0;
        increaseButton.active = hasCard && denomIndex < denominations.size() - 1;
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int lx = this.leftPos;
        int ty = this.topPos;

        // ── Coordonate inventar (fixe, calculate de jos în sus) ─────────────────
        final int INV_START = 128;
        final int HOT_START = 186;
        final int SEP_Y     = 182;

        // ── Main panel ───────────────────────────────────────────────────────────
        gfx.fill(lx, ty, lx + GUI_W, ty + GUI_H, COL_PANEL);

        // ── Header bar ───────────────────────────────────────────────────────────
        gfx.fill(lx, ty, lx + GUI_W, ty + 16, COL_HEADER);

        // ── Card slot area ───────────────────────────────────────────────────────
        // Bordul exterior (2px pe fiecare parte față de slot-ul 16×16)
        gfx.fill(lx + CARD_SLOT_X - 2,      ty + CARD_SLOT_Y - 2,
                lx + CARD_SLOT_X + 16 + 2, ty + CARD_SLOT_Y + 16 + 2, COL_SLOT_BORDER);
        // Fundalul slot-ului (exact 16×16, aliniat cu hover-ul vanilla)
        gfx.fill(lx + CARD_SLOT_X,      ty + CARD_SLOT_Y,
                lx + CARD_SLOT_X + 16, ty + CARD_SLOT_Y + 16, COL_SLOT_BG);

        // ── Amount display box ───────────────────────────────────────────────────
        // Box: x=42..134, y=65..81 (între butoanele - și +)
        gfx.fill(lx + 42, ty + 65, lx + 134, ty + 81, COL_AMOUNT_BOX);
        // Border
        gfx.fill(lx + 41, ty + 64, lx + 135, ty + 65, COL_SLOT_BORDER); // top
        gfx.fill(lx + 41, ty + 81, lx + 135, ty + 82, COL_SLOT_BORDER); // bottom
        gfx.fill(lx + 41, ty + 64, lx + 42,  ty + 82, COL_SLOT_BORDER); // left
        gfx.fill(lx + 134, ty + 64, lx + 135, ty + 82, COL_SLOT_BORDER);// right

        // ── Linia separatoare între ATM și inventar ──────────────────────────────
        gfx.fill(lx + 7, ty + INV_START - 6,
                lx + GUI_W - 7, ty + INV_START - 5, 0xFF666666);

        // ── Background zona inventar ─────────────────────────────────────────────
        gfx.fill(lx + 7, ty + INV_START - 2,
                lx + GUI_W - 7, ty + HOT_START + 18 + 2, COL_INV_BG);

        // ── 3 rânduri inventar × 9 sloturi ──────────────────────────────────────
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = lx + 8 + col * 18;
                int sy = ty + INV_START + row * 18;
                gfx.fill(sx, sy, sx + 16, sy + 16, COL_SLOT_ITEM);
            }
        }

        // ── Linia separator hotbar ───────────────────────────────────────────────
        gfx.fill(lx + 7, ty + SEP_Y,
                lx + GUI_W - 7, ty + SEP_Y + 1, COL_SLOT_BORDER);

        // ── Hotbar × 9 sloturi ───────────────────────────────────────────────────
        for (int col = 0; col < 9; col++) {
            int sx = lx + 8 + col * 18;
            gfx.fill(sx, ty + HOT_START, sx + 16, ty + HOT_START + 16, COL_SLOT_ITEM);
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx, mouseX, mouseY, partialTick);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        ItemStack cardStack = menu.slots.getFirst().getItem();

        // ── Header title ─────────────────────────────────────────────────────
        String title = "ATM";
        int tw = this.font.width(title);
        gfx.drawString(this.font, title, (GUI_W - tw) / 2, 4, COL_WHITE, false);

        // ── Card slot label ──────────────────────────────────────────────────
        gfx.drawString(this.font, "Card:", CARD_SLOT_X - 28, CARD_SLOT_Y + 4, COL_GRAY, false);

        // ── Balance / status ─────────────────────────────────────────────────
        if (!cardStack.isEmpty()) {
            double shownBalance = cardStack.getOrDefault(CardUtils.MONEY_COMPONENT.get(), 0.0);
            String shownCurrency = cardStack.getOrDefault(CardUtils.CURRENCY_COMPONENT.get(), cardCurrency);

            String balanceStr = "Balance: " + CardUtils.formatMoney(shownBalance) + " " + shownCurrency;
            int bw = this.font.width(balanceStr);
            gfx.drawString(this.font, balanceStr, (GUI_W - bw) / 2, CARD_SLOT_Y + 24, COL_YELLOW, false);
        } else {
            String noCard = "Insert a card";
            int nw = this.font.width(noCard);
            gfx.drawString(this.font, noCard, (GUI_W - nw) / 2, CARD_SLOT_Y + 24, COL_GRAY, false);
        }

        // ── Amount ───────────────────────────────────────────────────────────
        String amountStr;
        if (!cardStack.isEmpty()) {
            amountStr = String.format("%.2f %s", selectedAmount, cardCurrency);
        } else {
            amountStr = String.format("%.2f", selectedAmount);
        }
        int aw = this.font.width(amountStr);
        int amountColor = !(cardStack.isEmpty()) ? COL_WHITE : COL_GRAY;

        gfx.drawString(this.font, amountStr, (GUI_W - aw) / 2, 68, amountColor, false);


        // ── Inventory label ───────────────────────────────────────────────────
        gfx.drawString(this.font,
                this.playerInventoryTitle,
                8,
                this.inventoryLabelY,
                COL_GRAY,
                false);
    }
}
