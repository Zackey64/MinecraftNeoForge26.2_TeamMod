package com.zackey.teammod.client;

import com.zackey.teammod.network.PlayerDataPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    // サーバーからパケットが届いたときに自動で実行される処理
    public static void handleData(final PlayerDataPayload payload, final IPayloadContext context) {
        // マイクラのメインスレッド（安全な領域）で処理を実行する
        context.enqueueWork(() -> {
            // 届いたパケットから「座標」「ステータス」「持ち物」のリストを取り出して、保管庫（Storage）を更新
            ClientDataStorage.updateAll(
                    payload.positions(),
                    payload.statuses(),
                    payload.inventories()
            );
        });
    }

}
