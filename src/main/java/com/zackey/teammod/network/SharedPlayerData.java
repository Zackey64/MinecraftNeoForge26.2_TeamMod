package com.zackey.teammod.network;

// ========================================
// クライアント側でデータを受信・保存するクラス
// ========================================

import java.util.List;

public class SharedPlayerData {

    // 座標系のデータ構造
    public record Position(
            String worldType,
            double x,
            double y,
            double z
    ) {}
    // ステータス系のデータ構造（体力、満腹度）
    public record Status(
            float health,
            float maxHealth,
            int foodLevel,
            int level
    ) {}
    // 持ち物系のデータ構造（メイン手持ち、オフハンド、防具4種）
    public record ItemData(String itemId, float durabilityRatio) {}
    public record Inventory(
            List<ItemData> items
    ) {}
    // 名前用のデータを加えて、上記の3つを内包する全4つの構造
    public record PlayerData(
            String name,
            Position position,
            Status status,
            Inventory inventory
    ) {}


}