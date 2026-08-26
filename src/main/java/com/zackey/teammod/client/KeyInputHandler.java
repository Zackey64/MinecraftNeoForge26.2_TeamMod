package com.zackey.teammod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "teammod", value = Dist.CLIENT)
public class KeyInputHandler {

    // 🟢 HUDの表示・非表示を切り替えるグローバルなフラグ（初期値は表示オン）
    private static int hudMode = 1;
    private static int mapMode = 1;
    // キーマッピングの登録用インスタンス
    public static KeyMapping toggleHudKey;
    public static KeyMapping toggleMapKey;
    // キーをシステムに登録する（MODバス対応）
    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        Identifier categoryId = Identifier.fromNamespaceAndPath("teammod", "main_category");
        KeyMapping.Category customCategory = new KeyMapping.Category(categoryId);
        event.registerCategory(customCategory);
        //
        toggleHudKey = new KeyMapping(
                "ＨＵＤ表示切替",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                customCategory
        );
        event.register(toggleHudKey);
        //
        toggleMapKey = new KeyMapping(
                "マップ表示切替",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                customCategory
        );
        event.register(toggleMapKey);
    }

    // クライアントの毎ティック処理でキー入力を監視する（GAMEバス対応）
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // キーが押された瞬間を確実にキャッチする
        if (toggleHudKey != null && toggleHudKey.consumeClick()) {
            // フラグのオン・オフを反転
            hudMode ++;
            if (hudMode > 2) {hudMode = 0;}
        }
        //
        if (toggleMapKey != null && toggleMapKey.consumeClick()) {
            // フラグのオン・オフを反転
            mapMode ++;
            if (mapMode > 2) {mapMode = 0;}
        }
    }

    // 外部（InGameHudRenderer）からフラグの状態を確認するための取得メソッド
    public static int getHudMode() {
        return hudMode;
    }
    public static int getMapMode() {
        return mapMode;
    }

}