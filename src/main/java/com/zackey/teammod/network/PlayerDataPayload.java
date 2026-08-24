package com.zackey.teammod.network;

import com.zackey.teammod.network.SharedPlayerData.Position;
import com.zackey.teammod.network.SharedPlayerData.Status;
import com.zackey.teammod.network.SharedPlayerData.Inventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record PlayerDataPayload (
        List<Position> positions,
        List<Status> statuses,
        List<Inventory> inventories
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlayerDataPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("teammod", "combined_player_data"));

    // シリアライザー
    public static final StreamCodec<FriendlyByteBuf, PlayerDataPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.positions().size());
                for (Position p : payload.positions()) {
                    buf.writeUtf(p.name());
                    buf.writeDouble(p.x());
                    buf.writeDouble(p.y());
                    buf.writeDouble(p.z());
                }
                buf.writeInt(payload.statuses().size());
                for (Status s : payload.statuses()) {
                    buf.writeUtf(s.name());
                    buf.writeFloat(s.health());
                    buf.writeFloat(s.maxHealth());
                    buf.writeInt(s.foodLevel());
                    buf.writeInt(s.level());
                }
                buf.writeInt(payload.inventories().size());
                for (Inventory i : payload.inventories()) {
                    buf.writeUtf(i.name());
                    buf.writeUtf(i.mainHandItem());
                    buf.writeUtf(i.offHandItem());
                }
            },
            buf -> {
                int posSize = buf.readInt();
                List<Position> posList = new ArrayList<>();
                for (int i = 0; i < posSize; i++) {
                    posList.add(new Position(buf.readUtf(), buf.readDouble(), buf.readDouble(), buf.readDouble()));
                }
                int statusSize = buf.readInt();
                List<Status> statusList = new ArrayList<>();
                for (int i = 0; i < statusSize; i++) {
                    statusList.add(new Status(buf.readUtf(), buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readInt()));
                }
                int invSize = buf.readInt();
                List<Inventory> invList = new ArrayList<>();
                for (int i = 0; i < invSize; i++) {
                    invList.add(new Inventory(buf.readUtf(), buf.readUtf(), buf.readUtf()));
                }
                return new PlayerDataPayload(posList, statusList, invList);
            }
    );

    @Override
    public CustomPacketPayload.Type<PlayerDataPayload> type() {
        return TYPE;
    }
}
