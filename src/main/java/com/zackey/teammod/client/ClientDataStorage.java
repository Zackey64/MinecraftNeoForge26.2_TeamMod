package com.zackey.teammod.client;

import com.zackey.teammod.network.SharedPlayerData.PlayerData;
import com.zackey.teammod.network.SharedPlayerData.Position;
import com.zackey.teammod.network.SharedPlayerData.Status;
import com.zackey.teammod.network.SharedPlayerData.Inventory;

import java.util.ArrayList;
import java.util.List;

public class ClientDataStorage {

    // サーバーから届いた一括プレイヤーデータを保管する1本のリスト
    private static List<PlayerData> playersData = new ArrayList<>();

    // データの更新用メソッド（ClientPayloadHandlerから呼ばれる）
    public static synchronized void updateAll(List<PlayerData> data) {
        playersData = data;
    }

    // HUD描画クラス（InGameHudRenderer）へ安全にデータを渡すための取得メソッド
    public static synchronized List<PlayerData> getPlayersData() {
        return new ArrayList<>(playersData);
    }

}
