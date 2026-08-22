package com.zackey.teammod.client;

import com.zackey.teammod.network.SharedPlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

public class InGameHudRenderer {

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {

        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                Identifier.fromNamespaceAndPath("teamhud", "main_hud"),
                (guiGraphics, deltaTracker) -> {

                    // 初期
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null || mc.gui.hud.isHidden()) { // F1キー非表示判定
                        return;
                    }

                    // テキストコレクターインスタンスを取得
                    ActiveTextCollector hudTextCollector = guiGraphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.NONE);

                    // 全員分のデータを取得
                    List<SharedPlayerData.Position> positions = ClientDataStorage.getPositions();
                    List<SharedPlayerData.Status> statuses = ClientDataStorage.getStatuses();
                    List<SharedPlayerData.Inventory> inventories = ClientDataStorage.getInventories();

                    // 描画位置の指定（標準サイズ用のシンプルなピクセル数値）
                    int minX = 10;   // 画面左端から10ピクセル
                    int maxX = 220;  // 右側のスクロール限界限界
                    int startY = 10; // 画面上端から10ピクセル
                    int rowHeight = 11; // 改行の高さ（11ピクセル）

                    //サーバーから届いているプレイヤー分をループして描画
                    for (int i = 0; i < positions.size(); i++) {

                        //位置情報一括取得
                        SharedPlayerData.Position pos = positions.get(i);
                        String name = pos.name();

                        String posText = String.format(" XYZ: %.1f, %.1f, %.1f", pos.x(), pos.y(), pos.z());

                        // プレイヤー名（黄色 0xFFFF55）
                        MutableComponent nameComp = Component.literal("【" + name + "】").withStyle(s -> s.withColor(0xFFFF55));
                        guiGraphics.drawScrollingString(hudTextCollector, mc.font, nameComp, minX, maxX, startY);
                        startY += rowHeight;

                        // 座標（白 0xFFFFFF）
                        MutableComponent posComp = Component.literal(posText).withStyle(s -> s.withColor(0xFFFFFF));
                        guiGraphics.drawScrollingString(hudTextCollector, mc.font, posComp, minX, maxX, startY);
                        startY += rowHeight;

                    }

                    startY += 4;


                }
        );


    }
}
