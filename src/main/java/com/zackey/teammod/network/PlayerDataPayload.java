package com.zackey.teammod.network;

import com.zackey.teammod.network.SharedPlayerData.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record PlayerDataPayload (
        List<PlayerData> playersData,
        Position myBedPosition
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlayerDataPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("teammod", "combined_player_data"));

    // シリアライザー
    public static final StreamCodec<FriendlyByteBuf, PlayerDataPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                // --- サーバーからの書き込み ---
                buf.writeInt(payload.playersData().size());
                for (PlayerData p : payload.playersData()) {

                    buf.writeUtf(p.name());

                    // 1. Position
                    buf.writeUtf(p.position().worldType());
                    buf.writeDouble(p.position().x());
                    buf.writeDouble(p.position().y());
                    buf.writeDouble(p.position().z());

                    // 2. Status
                    buf.writeFloat(p.status().health());
                    buf.writeFloat(p.status().maxHealth());
                    buf.writeInt(p.status().foodLevel());
                    buf.writeInt(p.status().level());

                    // 3. Inventory (6つのアイテムをループ書き込み)
                    buf.writeInt(p.inventory().items().size());
                    for (ItemData item : p.inventory().items()) {
                        buf.writeUtf(item.itemId());
                        buf.writeFloat(item.durabilityRatio());
                    }
                }
                //
                buf.writeUtf(payload.myBedPosition().worldType());
                buf.writeDouble(payload.myBedPosition().x());
                buf.writeDouble(payload.myBedPosition().y());
                buf.writeDouble(payload.myBedPosition().z());
            },
            buf -> {
                int size = buf.readInt();
                List<PlayerData> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    String name = buf.readUtf();

                    // 1. Position復元
                    Position pos = new Position(buf.readUtf(), buf.readDouble(), buf.readDouble(), buf.readDouble());

                    // 2. Status復元
                    Status status = new Status(buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readInt());

                    // 3. Inventory復元
                    int itemSize = buf.readInt();
                    List<ItemData> items = new ArrayList<>();
                    for (int j = 0; j < itemSize; j++) {
                        items.add(new ItemData(buf.readUtf(), buf.readFloat()));
                    }
                    Inventory inv = new Inventory(items);
                    //
                    list.add(new PlayerData(name, pos, status, inv));

                }
                Position myBed = new Position(buf.readUtf(), buf.readDouble(), buf.readDouble(), buf.readDouble());
                return new PlayerDataPayload(list, myBed);
            }
    );

    @Override
    public CustomPacketPayload.Type<PlayerDataPayload> type() {
        return TYPE;
    }
}
