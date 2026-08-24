package com.zackey.teammod.network;

import com.zackey.teammod.network.SharedPlayerData.PlayerData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
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
        if (tickCounter < 10) {
            return;
        }
        tickCounter = 0;

        // 現在サーバーにログインしているプレイヤーのリストを取得
        List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }

        // 統合データ（PlayerData）を格納する1本のリストを作成
        List<PlayerData> playersDataList = new ArrayList<>();

        // プレイヤー分送る
        for (ServerPlayer player : players) {

            //名前
            String name = player.getName().getString();
            // 座標データ
            String worldType = player.level().dimension().toString();
            SharedPlayerData.Position pos = new SharedPlayerData.Position(worldType, player.getX(), player.getY(), player.getZ());
            // 2ステータスデータ
            SharedPlayerData.Status status = new SharedPlayerData.Status(player.getHealth(), player.getMaxHealth(), player.getFoodData().getFoodLevel(), player.experienceLevel);
            // ６アイテム
            List<SharedPlayerData.ItemData> items = new ArrayList<>();
            items.add(createItemData(player.getItemInHand(InteractionHand.MAIN_HAND)));
            items.add(createItemData(player.getItemInHand(InteractionHand.OFF_HAND)));
            items.add(createItemData(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)));
            items.add(createItemData(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST)));
            items.add(createItemData(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS)));
            items.add(createItemData(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET)));
            SharedPlayerData.Inventory inv = new SharedPlayerData.Inventory(items);

            playersDataList.add(new PlayerData(name, pos, status, inv));
        }

        // 統合された1本の大元リストをパケットに詰めて全プレイヤーへ一斉送信！
        PlayerDataPayload packet = new PlayerDataPayload(playersDataList);
        PacketDistributor.sendToAllPlayers(packet);

    }


    //
    private static SharedPlayerData.ItemData createItemData(ItemStack stack) {

        // アイテムの公式ID（"minecraft:diamond_sword"など）を取得
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        // 耐久値が設定されているアイテム（武器・防具・ツール）なら「残り耐久 / 最大耐久」の割合を計算。無ければ -1.0F
        float ratio = stack.isDamageableItem() ? (float)(stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage() : -1.0F;

        return new SharedPlayerData.ItemData(itemId, ratio);
    }

}
