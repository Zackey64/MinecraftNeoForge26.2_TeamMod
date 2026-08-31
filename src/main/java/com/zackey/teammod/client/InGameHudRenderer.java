package com.zackey.teammod.client;


import com.zackey.teammod.Config;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.xml.sax.Locator;

import java.util.List;
import java.util.UUID;

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
                    // 保管庫から統合されたプレイヤーデータ一覧を取得
                    List<PlayerData> playersData = ClientDataStorage.getPlayersData();

                    // ----------------------------------------------------------------
                    // プレーヤー一覧
                    // ----------------------------------------------------------------
                    //表示モードが０（非表示）でない
                    if (Config.SHOW_LIST.get()) {

                        int bx = 4;   // 画面上端からの開始位置
                        int by = 4;   // 画面左端からの開始位置
                        int gap =20;
                        if (Config.SHOW_RADAR.get()) by = 128;
                        if (Config.LIST_MODE.get() == 1) gap = 38;

                        PlayerList l = new PlayerList(guiGraphics, bx, by,116,116);

                        int py=0;
                        //サーバーから届いているプレイヤー分をループして描画
                        for (PlayerData data : playersData) {

                            // 背景
                            l.drawColor(0x99000000, 6, py, 92, 16);
                            // プレイヤーカラー
                            int color = getPlayerColor(data.name());
                            l.drawColor(color, 0, py, 4, 16);
                            //
                            l.drawStatus(data, 10,2+py,84,12);
                            // アイテム
                            l.drawItemMain(data, 100,0+py);
                            if(Config.LIST_MODE.get() == 1){
                                l.drawItemOff(data, 82, 18+py);
                                l.drawItemArmor(data, 6, 18+py, 2);
                            }

                            //次のプレーヤー上方とのギャップ
                            py += gap;
                        }
                    }

                    // ----------------------------------------------------------------
                    // 位置情報
                    // ----------------------------------------------------------------
                    //表示モードが０（非表示）でない
                    if (Config.SHOW_RADAR.get()) {

                        // 自身の情報
                        String myName = mc.player.getName().getString();
                        String myWorld = mc.player.level().dimension().toString();
                        Position myPos = mc.player.position();
                        SharedPlayerData.Position myBed = ClientDataStorage.getMyBed();

                        Radar r = new Radar(guiGraphics, 4,4,116,116);
                        double rot = 0;
                        if (Config.RADAR_MODE.get()==0) rot = Math.PI/2;
                        if (Config.RADAR_MODE.get()==1) rot = Math.PI/5;
                        if (Config.RADAR_MODE.get()==2) rot = Math.toRadians( mc.player.getXRot() ) ;
                        r.setRadarSetting(mc.player, Config.RADAR_SIZE.get(), Config.RADAR_SIZE.get(), 51.2/Config.RADAR_SIZE.get(), 51.2/Config.RADAR_SIZE.get(), rot);
                        r.drawRadarRing(Config.RADAR_SIZE.get()/4);
                        r.drawRadarDirection(0);

                        // トラッキング
                        if(Config.RADAR_TRACK.get()) {
                            List<SharedPlayerData.Position> points = ClientDataStorage.getTrailPoints();
                            int size = points.size();
                            for (int i = 0; i < size; i++) {
                                SharedPlayerData.Position point = points.get(i);
                                if (myWorld.equals(point.worldType())) {
                                    int alpha = (int) (255.0 * (i + 1) / size);
                                    int color = (alpha << 24) | 0x00cccccc;
                                    r.drawPoint(point.x(), point.y(), point.z(), color, 2);
                                }
                            }
                        }

                        // 味方の位置
                        for (PlayerData data : playersData) {//サーバーから届いているプレイヤー分をループ
                            String name = data.name();
                            SharedPlayerData.Position pos = data.position();//ポジション情報一括取得
                            // 自分以外＋同世界
                            if (!name.equals(myName) && pos.worldType().equals(myWorld)) {
                                r.drawPoint(pos.x(), pos.y(), pos.z(), getPlayerColor(name), 0);
                            }
                        }

                        // リスポーン位置
                        if ((myBed != null) && myWorld.equals(myBed.worldType())) {
                            r.drawPoint(myBed.x(),myBed.y(),myBed.z(),0xFF00FF00 ,1);
                        }

                        // 死亡位置
                        java.util.Optional<net.minecraft.core.GlobalPos> lastDeathPosOpt = mc.player.getLastDeathLocation();
                        if (lastDeathPosOpt.isPresent()) {
                            net.minecraft.core.GlobalPos deathGlobalPos = lastDeathPosOpt.get();
                            String deathWorld = deathGlobalPos.dimension().toString();
                            if (deathWorld.equals(myWorld)) {
                                net.minecraft.core.BlockPos deathPos = deathGlobalPos.pos();
                                r.drawPoint(deathPos.getX(),deathPos.getY(),deathPos.getZ(),0xFFFF0000, 1);
                            }
                        }

                        // 自身の位置
                        r.drawPoint(myPos.x(), myPos.y(), myPos.z(), getPlayerColor(myName), 0);

                    }


                    // ----------------------------------------------------------------
                    // ここまで
                    // ----------------------------------------------------------------

                }
        );
    }

    //
    private static int getPlayerColor(String name) {

        // 2. 名前の文字列を絶対に変らない数字（ハッシュコード）に変換
        int hash = name.hashCode();
        RandomSource random = RandomSource.create(hash);

        float hue = random.nextFloat();
        float saturation = 0.75f; // 0.0(白っぽい) ～ 1.0(鮮やか)
        float brightness = 0.85f; // 0.0(暗い) ～ 1.0(明るい)

        // 割り当てられた固有の色を返す
        int rgb = Mth.hsvToRgb(hue, saturation, brightness) & 0xFFFFFF;
        // 5. 不透明100%のアルファ値（0xFF000000）を合成して、8桁（0xFFRRGGBB）にする
        return 0xFF000000 | rgb;
    }

    // --------
    // 描画用
    // --------

    // マル印(5x5)
    private static void drawPoint(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int x, int y, int color) {
        graphics.fill(x-2, y-1, x+2+1, y+1+1, color);
        graphics.fill(x-1, y-2, x+1+1, y+2+1, color);

    }

    // バツ印(5x5)
    private static void drawCross(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int x, int y, int color) {

        for (int i = -2; i <= 2; i++) {
            graphics.fill(x+i, y+i, x+i+1, y+i+1, color);
            graphics.fill(x+i, y-i, x+i+1, y-i+1, color);
        }
    }

    // 線
    private static void drawLine(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color) {
        int count = Math.max(Math.abs(x2 - x1),Math.abs(y2 - y1)); //縦横の長い方
        for (int i = 0; i < count; i++) {
            int x = x1 + Math.round((x2 - x1) * i/count);
            int y = y1 + Math.round((y2 - y1) * i/count);
            graphics.fill(x, y, x+1, y+1, color);
        }
    }

    //
    private static void drawCircle(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int x, int y, int rx, int ry, int color) {
        // 360度、1度ずつ角度を進めながら円周上の点を計算してドット（小さな四角）を打つ
        for (int i = 0; i < 360; i++) {
            // 度数をラジアンに変換
            double angle = Math.toRadians(i);
            // 三角関数（コス・シン）を使って、中心点からのXとYのズレ（位置）を計算
            int dotX = (int) Math.round(x + Math.cos(angle) * rx);
            int dotY = (int) Math.round(y + Math.sin(angle) * ry);
            // 1x1ピクセルの極小の四角形（fill）を描画して、線のようにつなぎ合わせる
            graphics.fill(dotX, dotY, dotX + 1, dotY + 1, color);
        }
    }

    // 文字表示
    private static void drawText(net.minecraft.client.gui.GuiGraphicsExtractor graphics, String value, int x, int y, int color, int type) {
        //
        Minecraft mc = Minecraft.getInstance();
        ActiveTextCollector hudTextCollector = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.NONE);
        //
        MutableComponent textComp = Component.literal(value).withStyle(s -> s.withColor(color));
        if(type == 1){
            graphics.drawScrollingString(hudTextCollector, mc.font, textComp, x, x+mc.font.width(value), y);
        } else if(type == -1) {
            graphics.drawScrollingString(hudTextCollector, mc.font, textComp, x-mc.font.width(value), x, y);
        } else if (type == 0) {
            graphics.drawScrollingString(hudTextCollector, mc.font, textComp, x-mc.font.width(value)/2, x+mc.font.width(value)/2, y-4);
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


    /*
    ================================================================
    プレーヤーリスト標示用クラス
    ================================================================
    */
    static class PlayerList{

        // 画面表示の設定値
        int List_x;  // 画面上端からの開始位置
        int List_y;  // 画面左端からの開始位置
        int List_w;  // おおきさ
        int List_h;  // おおきさ

        private net.minecraft.client.gui.GuiGraphicsExtractor graphics;

        // コンストラクタ
        public PlayerList(net.minecraft.client.gui.GuiGraphicsExtractor g, int x, int y, int w, int h) {
            this.graphics = g;
            this.List_x = x;
            this.List_y = y;
            this.List_w = w;
            this.List_h = h;
            //graphics.fill(x, y, x + w, y + h, 0x99000000);
            //graphics.outline(x, y, w, h, 0xFFFFFFFF);
        }

        public void drawColor(int color, int x, int y, int w, int h) {
            graphics.fill(List_x+x, List_y+y, List_x+x + w, List_y+y + h, color);
        }

        public void drawStatus(PlayerData data, int x, int y, int w, int h){
            SharedPlayerData.Status status = data.status();
            // プレイヤー名
            String name = data.name();
            drawText(graphics, name, List_x+x, List_y+y, 0xFFFFFFFF, 1);
            // レベル数
            String level = status.level() + "";
            drawText(graphics, level, List_x+x+w, List_y+y, 0xFF33FF33, -1);
            // 体力
            double v = Math.ceil(status.health()) / Math.ceil(status.maxHealth());
            drawBar(graphics, v, List_x+x, List_y+y+h-2, 40, 2, 0xFFFF3333, false);
            // 満腹度
            v = (double) status.foodLevel() / 20;
            drawBar(graphics, v, List_x+x+w-40, List_y+y+h-2, 40, 2, 0xFFFFFF00, true);
        }

        public void drawItemMain(PlayerData data, int x, int y) {
            SharedPlayerData.Inventory inv = data.inventory();
            ItemData itemData = inv.items().get(0);
            drawItem(graphics, itemData, List_x + x, List_y + y);
        }
        public void drawItemOff(PlayerData data, int x, int y) {
            SharedPlayerData.Inventory inv = data.inventory();
            ItemData itemData = inv.items().get(1);
            drawItem(graphics, itemData, List_x + x, List_y + y);
        }
        public void drawItemArmor(PlayerData data, int x, int y, int gap) {
            SharedPlayerData.Inventory inv = data.inventory();
            for (int i=0; i<4; i++) {
                ItemData itemData = inv.items().get(i+2);
                drawItem(graphics, itemData, List_x + x + i*(gap+16), List_y + y);
            }
        }
    }

    /*
    ================================================================
    座標レーダー標示用クラス
    ================================================================
    */

    //レーダークラス
    static class Radar{

        // 画面表示の設定値
        int radar_x;  // 画面上端からの開始位置
        int radar_y;  // 画面左端からの開始位置
        int radar_w;  // おおきさ
        int radar_h;  // おおきさ
        int radar_cx;
        int radar_cy;
        // 座標変換の設定値
        double map_scaleXZ = 0.2f; // 座標単位から画面単位への表示スケール
        double map_scaleY = 0.4f; // 座標単位から画面単位への表示スケール
        // レーダー表示の設定値（それ以上は端に表示）
        int map_limitXZ = 256; //平面での最大表示半径（ブロック単位）
        int map_limitY = 64; //縦での最大表示半径（ブロック単位）

        double XRot = 0;


        private net.minecraft.client.player.LocalPlayer player = null;
        private net.minecraft.client.gui.GuiGraphicsExtractor graphics;

        // コンストラクタ
        public Radar(net.minecraft.client.gui.GuiGraphicsExtractor g, int x, int y, int w, int h) {
            this.graphics = g;
            this.radar_x = x;
            this.radar_y = y;
            this.radar_w = w;
            this.radar_h = h;
            this.radar_cx = radar_x + radar_w / 2;
            this.radar_cy = radar_y + radar_h / 2;
            graphics.fill(x, y, x + w, y + h, 0x99000000);
            graphics.outline(x, y, w, h, 0xFFFFFFFF);
        }

        // 設定値
        public void setRadarSetting(net.minecraft.client.player.LocalPlayer p, int limitXZ, int limitY, double scaleXZ, double scaleY, double rot){
            this.player = p;
            this.map_limitXZ = limitXZ;
            this.map_limitY = limitY;
            this.map_scaleXZ = scaleXZ;
            this.map_scaleY = scaleY;
            this.XRot = rot;
            drawText(graphics, "Radar", radar_x+2, radar_y+2, 0xFFFFFFFF, 1);
            drawText(graphics, map_limitXZ+"", radar_x-2+radar_w, radar_y+2, 0xFFFFFFFF, -1);
        }

        // リング表示
        public void drawRadarRing(int subRingGap){
            double dx = map_scaleXZ * map_limitXZ;
            double dy = (map_scaleXZ * map_limitXZ) * Math.sin(this.XRot);
            int north_x = (int) Math.round(radar_cx + Math.cos(-Math.toRadians(player.getYRot()) + Math.PI / 2.0) * dx);
            int north_y = (int) Math.round(radar_cy + Math.sin(-Math.toRadians(player.getYRot()) + Math.PI / 2.0) * dy);
            int south_x = (int) Math.round(radar_cx + Math.cos(-Math.toRadians(player.getYRot()) + Math.PI / 2.0 + Math.PI) * dx);
            int south_y = (int) Math.round(radar_cy + Math.sin(-Math.toRadians(player.getYRot()) + Math.PI / 2.0 + Math.PI) * dy);
            int east_x = (int) Math.round(radar_cx + Math.cos(-Math.toRadians(player.getYRot()) + Math.PI / 2.0 + Math.PI / 2.0) * dx);
            int east_y = (int) Math.round(radar_cy + Math.sin(-Math.toRadians(player.getYRot()) + Math.PI / 2.0 + Math.PI / 2.0) * dy);
            int west_x = (int) Math.round(radar_cx + Math.cos(-Math.toRadians(player.getYRot()) + Math.PI / 2.0 - Math.PI / 2.0) * dx);
            int west_y = (int) Math.round(radar_cy + Math.sin(-Math.toRadians(player.getYRot()) + Math.PI / 2.0 - Math.PI / 2.0) * dy);
            drawLine(graphics, north_x, north_y, south_x, south_y, 0xFF666666);
            drawLine(graphics, east_x, east_y, west_x, west_y, 0xFF666666);

            int r;
            double rot = Math.sin(this.XRot);
            for (int i = subRingGap; i < map_limitXZ; i += subRingGap) {
                r = (int)(map_scaleXZ * i);
                drawCircle(graphics, radar_cx, radar_cy,  r, (int)(r * rot), 0xFF666666);
            }
            r = (int)(map_scaleXZ * map_limitXZ);
            InGameHudRenderer.drawCircle(graphics, radar_cx, radar_cy, r, (int)(r * rot), 0xFFFFFFFF);
        }

        // 方位表示
        public void drawRadarDirection(int padding){
            double dx = map_scaleXZ * map_limitXZ + padding;
            double dy = (map_scaleXZ * map_limitXZ + padding) * Math.sin(this.XRot);
            int north_x = (int) Math.round(radar_cx + Math.cos(-Math.toRadians(player.getYRot()) + Math.PI / 2.0) * dx);
            int north_y = (int) Math.round(radar_cy + Math.sin(-Math.toRadians(player.getYRot()) + Math.PI / 2.0) * dy);
            int south_x = (int) Math.round(radar_cx + Math.cos(-Math.toRadians(player.getYRot()) + Math.PI / 2.0 + Math.PI) * dx);
            int south_y = (int) Math.round(radar_cy + Math.sin(-Math.toRadians(player.getYRot()) + Math.PI / 2.0 + Math.PI) * dy);
            int east_x = (int) Math.round(radar_cx + Math.cos(-Math.toRadians(player.getYRot()) + Math.PI / 2.0 + Math.PI / 2.0) * dx);
            int east_y = (int) Math.round(radar_cy + Math.sin(-Math.toRadians(player.getYRot()) + Math.PI / 2.0 + Math.PI / 2.0) * dy);
            int west_x = (int) Math.round(radar_cx + Math.cos(-Math.toRadians(player.getYRot()) + Math.PI / 2.0 - Math.PI / 2.0) * dx);
            int west_y = (int) Math.round(radar_cy + Math.sin(-Math.toRadians(player.getYRot()) + Math.PI / 2.0 - Math.PI / 2.0) * dy);
            drawText(graphics, "N", north_x, north_y, 0xFFFFFFFF, 0);
            drawText(graphics, "S", south_x, south_y, 0xFFFFFFFF, 0);
            drawText(graphics, "E", east_x, east_y, 0xFFFFFFFF, 0);
            drawText(graphics, "W", west_x, west_y, 0xFFFFFFFF, 0);


        }


        // 位置表示
        public void drawPoint(double x, double y, double z, int color, int type){
            //
            double dx = x - player.position().x();
            double dy = y - player.position().y();
            double dz = z - player.position().z();
            double distance = Math.sqrt(dx * dx + dz * dz);
            double angle = Math.atan2(dz, dx) - Math.toRadians(player.getYRot()) + Math.PI;

            if (distance > map_limitXZ) distance = map_limitXZ;
            if (dy > map_limitY) dy = map_limitY;
            if (dy < -map_limitY) dy = -map_limitY;

            if ( type==2 && distance > map_limitXZ ) return;
            if ( type==2 && Math.abs(dy) > map_limitY ) return;

            double dX = Math.cos(angle) * distance;
            double dY = Math.sin(angle) * distance * Math.sin(this.XRot);
            double addY = Math.cos(this.XRot) * dy;

            int px = radar_cx + (int) Math.round(dX * map_scaleXZ);
            int py = radar_cy + (int) Math.round(dY * map_scaleXZ);
            int phy = radar_cy + (int) Math.round(dY * map_scaleXZ - addY * map_scaleY);
            if(type!=2 && addY!=0){
                graphics.fill(px, py, px+1, phy+1, 0xFFcccccc);
                graphics.fill(px, py, px+1, py+1, 0xFFFFFFFF);
            }
            if (type==0) InGameHudRenderer.drawPoint(graphics, px, phy, color);
            if (type==1) InGameHudRenderer.drawCross(graphics, px, phy, color);
            if (type==2 && distance < map_limitXZ ) graphics.fill(px, phy, px+1, phy+1, color);

        }

    }

    /*
    ================================================================
    ここまで
    ================================================================
    */

}

