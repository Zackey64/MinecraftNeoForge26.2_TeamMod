package com.zackey.teammod.client;

import com.zackey.teammod.network.PlayerDataPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    // サーバーからパケットが届いたときに自動で実行される処理
    public static void handleData(final PlayerDataPayload payload, final IPayloadContext context) {
        // マイクラのメインスレッド（安全な領域）で処理を実行する
        context.enqueueWork(() -> {
            // 統合された1本のリストをそのまま保管庫へ更新
            ClientDataStorage.updateAll(
                    payload.playersData(),
                    payload.myBedPosition()
            );
        });
    }

}
