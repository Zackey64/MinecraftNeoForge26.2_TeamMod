package com.zackey.teammod.network;

// ========================================
// クライアント側でデータを受信・保存するクラス
// ========================================

public class SharedPlayerData {

    // 座標系のデータ構造
    public record Position(String name, double x, double y, double z) {}
    // ステータス系のデータ構造（体力、満腹度）
    public record Status(String name, float health, float maxHealth, int foodLevel) {}
    // 持ち物系のデータ構造（メイン手持ち、オフハンド、防具4種）
    public record Inventory(String name, String mainHandItem, String offHandItem) {}

}