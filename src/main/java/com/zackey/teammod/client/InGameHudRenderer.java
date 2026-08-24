package com.zackey.teammod.client;

import com.zackey.teammod.network.SharedPlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
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

                    // ----------------------------------------------------------------
                    // プレーヤー一覧
                    // ----------------------------------------------------------------

                    int set_x = 4;   // 画面上端からの開始位置
                    int set_y = 120;   // 画面左端からの開始位置
                    int set_gap = 20; // 別PLの境界

                    //サーバーから届いているプレイヤー分をループして描画
                    for (int i = 0; i < positions.size(); i++) {

                        SharedPlayerData.Position pos = positions.get(i); //ポジション情報一括取得
                        SharedPlayerData.Status status = statuses.get(i); //ステータス情報一括取得
                        SharedPlayerData.Inventory inv = inventories.get(i); // アイテム情報一括取得
                        String name = pos.name();

                        // 描画エンジンに渡すためのItemStackオブジェクトに変換
                        Identifier mainResource = Identifier.parse(inv.mainHandItem());
                        Identifier offResource = Identifier.parse(inv.offHandItem());
                        net.minecraft.world.item.Item mainItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(mainResource)
                                .map(net.minecraft.core.Holder.Reference::value) // HolderからItemの実体を取り出す
                                .orElse(net.minecraft.world.item.Items.AIR);     // 見つからなければ「空気」にする
                        net.minecraft.world.item.Item offItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(offResource)
                                .map(net.minecraft.core.Holder.Reference::value)
                                .orElse(net.minecraft.world.item.Items.AIR);
                        net.minecraft.world.item.ItemStack mainStack = new net.minecraft.world.item.ItemStack(mainItem);
                        net.minecraft.world.item.ItemStack offStack = new net.minecraft.world.item.ItemStack(offItem);



                        int bx = set_x;
                        int by = set_y + set_gap * i;
                        int x,y,w,h;

                        // 背景
                        x = 0; y = 0; w = 72; h = 16;
                        guiGraphics.fill(bx+x, by+y, bx+x+w, by+y+h, 0xFF000000);

                        // プレイヤーカラー
                        x = 0; y = 0; w = 4; h = 16;
                        guiGraphics.fill(bx+x, by+y, bx+x+w, by+y+h, getPlayerColor(name));

                        // プレイヤー名
                        x = 6; y = 2; w = 64; h = 12;
                        MutableComponent nameComp = Component.literal(name).withStyle(s -> s.withColor(0xFFFFFFFF));
                        guiGraphics.drawScrollingString(hudTextCollector, mc.font, nameComp, bx+x, bx+x+w, by+y);

                        // 体力
                        x = 6; y = 12; w = 64; h = 2;
                        guiGraphics.fill(bx+x, by+y, bx+x+w, by+y+h, 0xFF333333);
                        w = (int) ( w * Math.ceil(status.health()) / Math.ceil(status.maxHealth()) );
                        guiGraphics.fill(bx+x, by+y, bx+x+w, by+y+h, 0xFFFFFFFF);

                        // アイテム
                        x = 72; y = 0; w = 32; h = 16;
                        guiGraphics.fill(bx+x, by+y, bx+x+w, by+y+h, 0x66000000);
                        x = 72; y = 0;
                        guiGraphics.item(offStack , bx+x, by+y);
                        x = 88; y = 0;
                        guiGraphics.item(mainStack, bx+x, by+y);


                    }

                    // ----------------------------------------------------------------
                    // 位置情報
                    // ----------------------------------------------------------------

                    int map_x = 4;      // 画面上端からの開始位置
                    int map_y = 4;      // 画面左端からの開始位置
                    int map_size = 104; // おおきさ
                    float map_scale = 0.15f; // 表示スケール
                    int maxDistance = 256; //最大表示ブロック半径（それ以上は端に表示）
                    int center_x = map_x + map_size/2;
                    int center_y = map_y + map_size/2;

                    // 自身の情報
                    String myName = mc.player.getName().getString();
                    Position myPos = mc.player.position();
                    float myRot = (float) Math.toRadians(mc.player.getYRot());

                    // 背景と円の描画
                    guiGraphics.fill(map_x, map_y, map_x+map_size, map_y+map_size, 0x66000000);
                    for (int i = 64; i < maxDistance; i += 64){
                        drawCircle(guiGraphics, center_x, center_y, (int)(map_scale *i), 0x66333333);
                    }
                    drawCircle(guiGraphics, center_x, center_y, (int)(map_scale * maxDistance), 0xFFFFFFFF);

                    //方角
                    int padding = 8;
                    int north_x = (int) Math.round(center_x + Math.cos(-myRot + Math.PI / 2.0) * (map_scale*maxDistance + padding ));
                    int north_y = (int) Math.round(center_y + Math.sin(-myRot + Math.PI / 2.0) * (map_scale*maxDistance + padding ));
                    MutableComponent northComp = Component.literal("N").withStyle(s -> s.withColor(0xFFFFFFFF));
                    guiGraphics.drawScrollingString(hudTextCollector, mc.font, northComp, north_x-3, north_x+3, north_y-4);
                    int south_x = (int) Math.round(center_x + Math.cos(-myRot + Math.PI / 2.0 + Math.PI) * (map_scale*maxDistance + padding ));
                    int south_y = (int) Math.round(center_y + Math.sin(-myRot + Math.PI / 2.0 + Math.PI) * (map_scale*maxDistance + padding ));
                    MutableComponent southComp = Component.literal("S").withStyle(s -> s.withColor(0xFF999999));
                    guiGraphics.drawScrollingString(hudTextCollector, mc.font, southComp, south_x-3, south_x+3, south_y-4);
                    int east_x = (int) Math.round(center_x + Math.cos(-myRot + Math.PI / 2.0 + Math.PI / 2.0) * (map_scale * maxDistance + padding ));
                    int east_y = (int) Math.round(center_y + Math.sin(-myRot + Math.PI / 2.0 + Math.PI / 2.0) * (map_scale * maxDistance + padding ));
                    MutableComponent eastComp = Component.literal("E").withStyle(s -> s.withColor(0xFF999999));
                    guiGraphics.drawScrollingString(hudTextCollector, mc.font, eastComp, east_x - 3, east_x + 3, east_y - 4);
                    int west_x = (int) Math.round(center_x + Math.cos(-myRot + Math.PI / 2.0 - Math.PI / 2.0) * (map_scale * maxDistance + padding ));
                    int west_y = (int) Math.round(center_y + Math.sin(-myRot + Math.PI / 2.0 - Math.PI / 2.0) * (map_scale * maxDistance + padding ));
                    MutableComponent westComp = Component.literal("W").withStyle(s -> s.withColor(0xFF999999));
                    guiGraphics.drawScrollingString(hudTextCollector, mc.font, westComp, west_x - 3, west_x + 3, west_y - 4);

                    //サーバーから届いているプレイヤー分をループして描画
                    for (int i = 0; i < positions.size(); i++) {

                        SharedPlayerData.Position pos = positions.get(i); //ポジション情報一括取得
                        String name = pos.name();

                        int point_x;
                        int point_y;

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

                            point_x = (int) Math.round(center_x + Math.cos(angle) * distance);
                            point_y = (int) Math.round(center_y + Math.sin(angle) * distance);
                            int add_y = (int) Math.max(Math.min(deltaY,64),-64) / 2;

                            guiGraphics.fill(point_x, point_y, point_x+1, point_y - add_y, 0xFF666666);
                            guiGraphics.fill(point_x, point_y, point_x+1, point_y+1, 0xFFFFFFFF);
                            guiGraphics.fill(point_x - 1, point_y - add_y - 1, point_x + 2, point_y - add_y + 2, getPlayerColor(name));

                        } else {
                            // 自身の位置
                            point_x = center_x;
                            point_y = center_y;
                            guiGraphics.fill(point_x - 1, point_y - 1, point_x + 2, point_y + 2, getPlayerColor(name));
                        }

                    }

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


}
