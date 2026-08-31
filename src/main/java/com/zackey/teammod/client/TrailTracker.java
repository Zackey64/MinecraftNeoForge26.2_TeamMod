package com.zackey.teammod.client;

import com.zackey.teammod.network.SharedPlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "teammod", value = Dist.CLIENT)
public final class TrailTracker {

    // 次の地点を保存するまでの距離
    private static final double POINT_DISTANCE = 4.0;

    // 前回保存した地点
    private static double lastX = 0 ;
    private static double lastY = 0 ;
    private static double lastZ = 0 ;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        // プレイヤーが存在しない場合
        if (player == null) {
            return;
        }

        // 現在位置
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        // 前回保存地点からの距離
        double dx = x - lastX;
        double dy = y - lastY;
        double dz = z - lastZ;
        double distanceSq = dx * dx + dy * dy + dz * dz;

        // 一定距離以上移動した
        if (distanceSq >= POINT_DISTANCE * POINT_DISTANCE) {
            ClientDataStorage.addTrailPoint(
                    new SharedPlayerData.Position(
                            player.level().dimension().toString(),
                            x,
                            y,
                            z
                    )
            );
            lastX = x;
            lastY = y;
            lastZ = z;
        }

    }
}
