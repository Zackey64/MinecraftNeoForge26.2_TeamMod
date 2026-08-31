package com.zackey.teammod.client;

import com.zackey.teammod.Config;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TeamModConfigScreen extends Screen {

    private final Screen parent;

    public TeamModConfigScreen(Screen parent) {
        super(Component.literal("TeamMod 設定"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        // レーダー表示
        this.addRenderableWidget(
                Button.builder(
                        getRadarText(),
                        button -> {
                            Config.SHOW_RADAR.set(!Config.SHOW_RADAR.get());
                            button.setMessage(getRadarText());
                        }
                ).bounds(centerX - 100, 60, 200, 20).build()
        );
        this.addRenderableWidget(
                Button.builder(
                        getRadarModeText(),
                        button -> {
                            Config.RADAR_MODE.set( Config.RADAR_MODE.get()==2 ? 0 : Config.RADAR_MODE.get()+1 );
                            button.setMessage(getRadarModeText());
                        }
                ).bounds(centerX - 100, 80, 200, 20).build()
        );
        this.addRenderableWidget(
                Button.builder(
                        getRadarSizeText(),
                        button -> {
                            Config.RADAR_SIZE.set( Config.RADAR_SIZE.get()==256 ? 64 : Config.RADAR_SIZE.get()+64 );
                            button.setMessage(getRadarSizeText());
                        }
                ).bounds(centerX - 100, 100, 200, 20).build()
        );
        this.addRenderableWidget(
                Button.builder(
                        getRadarTrackText(),
                        button -> {
                            Config.RADAR_TRACK.set(!Config.RADAR_TRACK.get());
                            button.setMessage(getRadarTrackText());
                        }
                ).bounds(centerX - 100, 120, 200, 20).build()
        );

        // リスト表示
        this.addRenderableWidget(
                Button.builder(
                        getListText(),
                        button -> {
                            Config.SHOW_LIST.set(!Config.SHOW_LIST.get());
                            button.setMessage(getListText());
                        }
                ).bounds(centerX - 100, 160, 200, 20).build()
        );
        this.addRenderableWidget(
                Button.builder(
                        getListModeText(),
                        button -> {
                            Config.LIST_MODE.set( Config.LIST_MODE.get()==1 ? 0 : Config.LIST_MODE.get()+1 );
                            button.setMessage(getListModeText());
                        }
                ).bounds(centerX - 100, 180, 200, 20).build()
        );

    }

    private Component getRadarText() {
        return Component.literal(
                "レーダー表示: " + (Config.SHOW_RADAR.get() ? "ON" : "OFF")
        );
    }
    private Component getRadarModeText() {
        return Component.literal(
                switch (Config.RADAR_MODE.get()) {
                    case 0 -> "２Ｄ平面表示";
                    case 1 -> "３Ｄ立体表示";
                    case 2 -> "３Ｄ立体表示（視点連動）";
                    default -> "";
                }
        );
    }
    private Component getRadarSizeText() {
        return Component.literal(
                "表示領域: " + (Config.RADAR_SIZE.get()) + "ブロック"
        );
    }
    private Component getRadarTrackText() {
        return Component.literal(
                "トラック（自分の軌跡）表示: " + (Config.RADAR_TRACK.get() ? "ON" : "OFF")
        );
    }
    private Component getListText() {
        return Component.literal(
                "リスト表示: " +
                        (Config.SHOW_LIST.get() ? "ON" : "OFF")
        );
    }
    private Component getListModeText() {
        return Component.literal(
                switch (Config.LIST_MODE.get()) {
                    case 0 -> "簡易表示";
                    case 1 -> "詳細表示";
                    default -> "";
                }
        );
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }
}