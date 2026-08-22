package com.zackey.teammod.network;


import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

// サーバー側のイベント（ゲームの進行タイマー）を自動で受け取る設定
@EventBusSubscriber(modid = "teammod")
public class ServerNetworkHandler {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        // 時間処理
        tickCounter++;
        if (tickCounter < 20) {
            return;
        }
        tickCounter = 0;

        // 現在サーバーにログインしているプレイヤーのリストを取得
        List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }

        List<SharedPlayerData.Position> posList = new ArrayList<>();
        List<SharedPlayerData.Status> statusList = new ArrayList<>();
        List<SharedPlayerData.Inventory> invList = new ArrayList<>();

        // プレイヤー分送る
        for (ServerPlayer player : players) {

            //名前
            String name = player.getName().getString();
            // 座標データ
            posList.add(new SharedPlayerData.Position(name, player.getX(), player.getY(), player.getZ()));
            // 2ステータスデータ
            statusList.add(new SharedPlayerData.Status(name, player.getHealth(), player.getMaxHealth(), player.getFoodData().getFoodLevel()));
            // メインハンドとオフハンド
            String mainHand = getItemName(player.getMainHandItem());
            String offHand = getItemName(player.getOffhandItem());
            invList.add(new SharedPlayerData.Inventory(name, mainHand, offHand));
        }

        // パケットの箱にすべてのデータを詰め込む
        PlayerDataPayload packet = new PlayerDataPayload(posList, statusList, invList);
        PacketDistributor.sendToAllPlayers(packet);

    }


    private static String getItemName(net.minecraft.world.item.ItemStack stack) {
        // もし手が空っぽ（空気ブロック）なら「空気」と返す
        if (stack.isEmpty()) {
            return "空気";
        }
        // アイテム名（例: ダイヤモンドの剣）を取得して文字列として返す
        return stack.getHoverName().getString();
    }
}
