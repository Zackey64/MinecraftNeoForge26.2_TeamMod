package com.zackey.teammod;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    //
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    //
    public static final ModConfigSpec.BooleanValue SHOW_RADAR = BUILDER
            .comment("レーダーの表示")
            .define("ShowRadar", true);
    public static final ModConfigSpec.IntValue RADAR_SIZE = BUILDER
            .comment("レーダーのサイズ")
            .defineInRange("RadarSize", 256, 64, 256);
    public static final ModConfigSpec.IntValue RADAR_MODE = BUILDER
            .comment("レーダーの表示モード")
            .defineInRange("RadarMode", 0, 0, 2);
    public static final ModConfigSpec.BooleanValue RADAR_TRACK = BUILDER
            .comment("トラックの表示")
            .define("RadarTrack", true);

    //
    public static final ModConfigSpec.BooleanValue SHOW_LIST = BUILDER
            .comment("リストの表示")
            .define("ShowList", true);
    public static final ModConfigSpec.IntValue LIST_MODE = BUILDER
            .comment("リストの表示モード")
            .defineInRange("ListMode", 0, 0, 1);



    static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }


}
