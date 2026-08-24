package com.zackey.teammod.client;


import com.zackey.teammod.network.SharedPlayerData.ItemData;
import com.zackey.teammod.network.SharedPlayerData;
import com.zackey.teammod.network.SharedPlayerData.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Position;
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

                    Minecraft mc = Minecraft.getInstance();
                    // 1. F1キー非表示・プレイヤー不在時の判定
                    if (mc.player == null || mc.gui.hud.isHidden()) { // F1キー非表示判定
                        return;
                    }
                    // テキストコレクターインスタンスを取得
                    ActiveTextCollector hudTextCollector = guiGraphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.NONE);
                    // 保管庫から統合されたプレイヤーデータ一覧を取得
                    List<PlayerData> playersData = ClientDataStorage.getPlayersData();

                    // ----------------------------------------------------------------
                    // プレーヤー一覧
                    // ----------------------------------------------------------------

                    int bx = 4;   // 画面上端からの開始位置
                    int by = 112;   // 画面左端からの開始位置

                    //サーバーから届いているプレイヤー分をループして描画
                    for (PlayerData data : playersData) {

                        int x,y,w,h;

                        //メインハンド以外で詳細表示出ない場合はスキップ
                        if (KeyInputHandler.getHudMode() == 0){
                            continue;
                        }

                        SharedPlayerData.Status status = data.status();//ステータス情報一括取得
                        SharedPlayerData.Inventory inv = data.inventory();// アイテム情報一括取得

                        // 背景
                        x = 0; y = 0; w = 96; h = 16;
                        guiGraphics.fill(bx+x, by+y, bx+x+w, by+y+h, 0xcc333333);
                        // プレイヤーカラー
                        int color = getPlayerColor(data.name());
                        x = 0; y = 0; w = 2; h = 16;
                        guiGraphics.fill(bx+x, by+y, bx+x+w, by+y+h, color);
                        x = 94; y = 0; w = 2; h = 16;
                        guiGraphics.fill(bx+x, by+y, bx+x+w, by+y+h, color);

                        // プレイヤー名
                        x = 6; y = 2;
                        String name = data.name();
                        drawText(guiGraphics, name, bx+x, by+y, 0xFFFFFFFF, false);
                        // レベル数
                        x = 90; y = 2;
                        String level = status.level() + "";
                        drawText(guiGraphics, level, bx+x, by+y, 0xFF33FF33, true);

                        // 体力
                        x = 6; y = 12; w = 40; h = 2;
                        double v = Math.ceil(status.health()) / Math.ceil(status.maxHealth());
                        drawBar(guiGraphics, v,bx+x, by+y, w, h,0xFFFF3333, false);
                        // 満腹度
                        x = 50; y = 12; w = 40; h = 2;
                        v = (double)status.foodLevel() / 20;
                        drawBar(guiGraphics, v,bx+x, by+y, w, h,0xFFFFFF00, true);

                        // アイテム
                        for(int i = 0; i < inv.items().size(); i++){
                            //メインハンド以外で詳細表示出ない場合はスキップ
                            if (0<i && KeyInputHandler.getHudMode() != 2){
                                continue;
                            }
                            //
                            ItemData itemData = inv.items().get(i);
                            if (i==0) {
                                x = 100; y = 0;
                            } else if (i==1) {
                                x = 74; y = 16;
                            } else {
                                x = 6+(i-2)*16; y = 16;
                            }
                            drawItem(guiGraphics, itemData,bx+x, by+y);
                        }

                        //
                        if (KeyInputHandler.getHudMode() == 1){
                           by += 20;
                        } else if (KeyInputHandler.getHudMode() == 2){
                            by += 36;
                        }


                    }

                    // ----------------------------------------------------------------
                    // 位置情報
                    // ----------------------------------------------------------------

                    int map_x = 4;      // 画面上端からの開始位置
                    int map_y = 4;      // 画面左端からの開始位置
                    int map_size = 96; // おおきさ
                    float map_scale = 0.15f; // 表示スケール
                    int maxDistance = 256; //最大表示ブロック半径（それ以上は端に表示）
                    int center_x = map_x + map_size/2;
                    int center_y = map_y + map_size/2;

                    // 自身の情報
                    String myName = mc.player.getName().getString();
                    Position myPos = mc.player.position();
                    float myRot = (float) Math.toRadians(mc.player.getYRot());

                    // 背景と円の描画
                    guiGraphics.fill(map_x, map_y, map_x+map_size, map_y+map_size, 0xcc333333);
                    for (int i = 64; i < maxDistance; i += 64){
                        drawCircle(guiGraphics, center_x, center_y, (int)(map_scale *i), 0xFF666666);
                    }
                    drawCircle(guiGraphics, center_x, center_y, (int)(map_scale * maxDistance), 0xFFFFFFFF);

                    //方角
                    int padding = 0;
                    int north_x = (int) Math.round(center_x + Math.cos(-myRot + Math.PI / 2.0) * (map_scale*maxDistance + padding ));
                    int north_y = (int) Math.round(center_y + Math.sin(-myRot + Math.PI / 2.0) * (map_scale*maxDistance + padding ));
                    MutableComponent northComp = Component.literal("N").withStyle(s -> s.withColor(0xFFFFFFFF));
                    guiGraphics.drawScrollingString(hudTextCollector, mc.font, northComp, north_x-3, north_x+3, north_y-4);
                    int south_x = (int) Math.round(center_x + Math.cos(-myRot + Math.PI / 2.0 + Math.PI) * (map_scale*maxDistance + padding ));
                    int south_y = (int) Math.round(center_y + Math.sin(-myRot + Math.PI / 2.0 + Math.PI) * (map_scale*maxDistance + padding ));
                    MutableComponent southComp = Component.literal("S").withStyle(s -> s.withColor(0xFFFFFFFF));
                    guiGraphics.drawScrollingString(hudTextCollector, mc.font, southComp, south_x-3, south_x+3, south_y-4);
                    int east_x = (int) Math.round(center_x + Math.cos(-myRot + Math.PI / 2.0 + Math.PI / 2.0) * (map_scale * maxDistance + padding ));
                    int east_y = (int) Math.round(center_y + Math.sin(-myRot + Math.PI / 2.0 + Math.PI / 2.0) * (map_scale * maxDistance + padding ));
                    MutableComponent eastComp = Component.literal("E").withStyle(s -> s.withColor(0xFFFFFFFF));
                    guiGraphics.drawScrollingString(hudTextCollector, mc.font, eastComp, east_x - 3, east_x + 3, east_y - 4);
                    int west_x = (int) Math.round(center_x + Math.cos(-myRot + Math.PI / 2.0 - Math.PI / 2.0) * (map_scale * maxDistance + padding ));
                    int west_y = (int) Math.round(center_y + Math.sin(-myRot + Math.PI / 2.0 - Math.PI / 2.0) * (map_scale * maxDistance + padding ));
                    MutableComponent westComp = Component.literal("W").withStyle(s -> s.withColor(0xFFFFFFFF));
                    guiGraphics.drawScrollingString(hudTextCollector, mc.font, westComp, west_x - 3, west_x + 3, west_y - 4);

                    //サーバーから届いているプレイヤー分をループして描画
                    for (PlayerData data : playersData) {

                        String name = data.name();
                        SharedPlayerData.Position pos = data.position();//ポジション情報一括取得

                        if (!name.equals(myName)) {
                            // 味方の位置
                            double deltaX = pos.x() - myPos.x();
                            double deltaY = pos.y() - myPos.y();
                            double deltaZ = pos.z() - myPos.z();
                            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * map_scale;
                            double angle = Math.atan2(deltaZ, deltaX) - myRot + Math.PI;

                            //レーダー内の表示倍率（実世界での距離をレーダーの半径に縮小）
                            if (distance > maxDistance * map_scale) {
                                distance = maxDistance * map_scale;
                            }

                            int point_x = (int) Math.round(center_x + Math.cos(angle) * distance);
                            int point_y = (int) Math.round(center_y + Math.sin(angle) * distance);
                            int add_y = (int) Math.max(Math.min(deltaY,64),-64) / 2;

                            guiGraphics.fill(point_x, point_y, point_x+1, point_y - add_y, 0xFFcccccc);
                            guiGraphics.fill(point_x, point_y, point_x+1, point_y+1, 0xFFFFFFFF);
                            guiGraphics.fill(point_x - 1, point_y - add_y - 1, point_x + 2, point_y - add_y + 2, getPlayerColor(name));

                        }

                    }
                    // 自身の位置
                    guiGraphics.fill(center_x - 1, center_y - 1, center_x + 2, center_y + 2, getPlayerColor(myName));

                    // ----------------------------------------------------------------
                    // ここまで
                    // ----------------------------------------------------------------

                }
        );
    }

    //
    private static int getPlayerColor(String name) {
        // 1. 24色の綺麗なカラーパレット（お好みでカラーコードは自由に変更できます）
        int[] colorPalette = {
                0xFFFF3333, // 赤
                0xFFFFFF00, // 黄色
                0xFF33FF33, // 黄緑
                0xFF00FFFF, // 水色
                0xFF3333FF, // 青
                0xFFFF00FF, // ピンク
        };
        // 2. 名前の文字列を絶対に変らない数字（ハッシュコード）に変換
        int hash = name.hashCode();
        // 3. マイナスの数値を防ぐために絶対値（Math.abs）にし、パレットのサイズで割った余りを出す
        int index = Math.abs(hash) % colorPalette.length;
        // 割り当てられた固有の色を返す
        return colorPalette[index];
    }



    // --------

    private static void drawCircle(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int x, int y, int radius, int color) {
        // 360度、1度ずつ角度を進めながら円周上の点を計算してドット（小さな四角）を打つ
        for (int i = 0; i < 360; i++) {
            // 度数をラジアンに変換
            double angle = Math.toRadians(i);
            // 三角関数（コス・シン）を使って、中心点からのXとYのズレ（位置）を計算
            int dotX = (int) Math.round(x + Math.cos(angle) * radius);
            int dotY = (int) Math.round(y + Math.sin(angle) * radius);
            // 1x1ピクセルの極小の四角形（fill）を描画して、線のようにつなぎ合わせる
            graphics.fill(dotX, dotY, dotX + 1, dotY + 1, color);
        }
    }

    // 文字表示
    private static void drawText(net.minecraft.client.gui.GuiGraphicsExtractor graphics, String value, int x, int y, int color, boolean flip) {
        //
        Minecraft mc = Minecraft.getInstance();
        ActiveTextCollector hudTextCollector = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.NONE);
        //
        MutableComponent textComp = Component.literal(value).withStyle(s -> s.withColor(color));
        if(!flip){
            graphics.drawScrollingString(hudTextCollector, mc.font, textComp, x, x+mc.font.width(value), y);
        } else {
            graphics.drawScrollingString(hudTextCollector, mc.font, textComp, x-mc.font.width(value), x, y);
        }
    }

    // バー表示(valueは0~1の実数)
    private static void drawBar(net.minecraft.client.gui.GuiGraphicsExtractor graphics, double value, int x, int y, int w, int h, int color, boolean flip) {
        // 背景
        graphics.fill(x, y, x+w, y+h, 0xFF666666);
        // 値
        int int_value = (int)Math.round(value * w);
        if(!flip){
            graphics.fill(x, y, x+int_value, y+h, color);
        } else {
            graphics.fill(x+w-int_value, y, x+w, y+h, color);
        }
    }

    //
    private static void drawItem(net.minecraft.client.gui.GuiGraphicsExtractor graphics, ItemData itemData, int x, int y) {

        // 文字列のアイテムIDをIdentifierに解析
        Identifier res = Identifier.parse(itemData.itemId());
        // レジストリから Holder を経由して安全に Item オブジェクトを取得
        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(res)
                .map(net.minecraft.core.Holder.Reference::value)
                .orElse(net.minecraft.world.item.Items.AIR);
        // 描画エンジンに渡すためのItemStackオブジェクトに変換
        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item);

        if(!stack.isEmpty()) {
            int w = 16, h = 16;
            graphics.fill(x, y, x + w, y + h, 0x66333333);
            graphics.item(stack, x, y);
            if (itemData.durabilityRatio() < 1 && 0 < itemData.durabilityRatio()) {
                drawBar(graphics,itemData.durabilityRatio(),x+1, y+14, 14, 1,0xFF33FF33, false);
            }
        }
    }


}
