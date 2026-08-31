package com.zackey.teammod;

import com.mojang.logging.LogUtils;
import com.zackey.teammod.client.ClientPayloadHandler;
import com.zackey.teammod.client.InGameHudRenderer;
import com.zackey.teammod.network.PlayerDataPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;


@Mod(TeamMod.MODID)
public class TeamMod {

    public static final String MODID = "teammod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TeamMod(IEventBus modEventBus, ModContainer modContainer) {

        // 設定GUI用
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        modEventBus.addListener(this::registerNetwork);
        // クライアント側（画面がある環境）のときだけHUDを設定する
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modEventBus.addListener(InGameHudRenderer::onRegisterGuiLayers);
        }

    }

    // パケットの登録システム
    private void registerNetwork(final RegisterPayloadHandlersEvent event) {
        // MOD ID "teammod" で登録機を作成
        final PayloadRegistrar registrar = event.registrar("teammod");

        // 26.2最新ルール：サーバーからクライアント(play)へ送るパケットとしてバインド
        registrar.playToClient(
                PlayerDataPayload.TYPE,         // 身分証明書 (TYPE)
                PlayerDataPayload.STREAM_CODEC, // シリアライザー (STREAM_CODEC)
                ClientPayloadHandler::handleData // 受信したときの処理窓口
        );
    }






}
