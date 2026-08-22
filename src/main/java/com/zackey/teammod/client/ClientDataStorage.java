package com.zackey.teammod.client;

import com.zackey.teammod.network.SharedPlayerData.Position;
import com.zackey.teammod.network.SharedPlayerData.Status;
import com.zackey.teammod.network.SharedPlayerData.Inventory;

import java.util.ArrayList;
import java.util.List;

public class ClientDataStorage {
    // サーバーから届いた最新の各データを個別に保管するリスト
    private static List<Position> positions = new ArrayList<>();
    private static List<Status> statuses = new ArrayList<>();
    private static List<Inventory> inventories = new ArrayList<>();

    // データの更新用メソッド（ClientPayloadHandlerから呼ばれる）
    public static synchronized void updateAll(List<Position> p, List<Status> s, List<Inventory> i) {
        positions = p;
        statuses = s;
        inventories = i;
    }

    // HUD描画クラス（InGameHudRenderer）へ安全にデータを渡すための取得メソッド
    public static synchronized List<Position> getPositions() {
        return new ArrayList<>(positions);
    }

    public static synchronized List<Status> getStatuses() {
        return new ArrayList<>(statuses);
    }

    public static synchronized List<Inventory> getInventories() {
        return new ArrayList<>(inventories);
    }
}
