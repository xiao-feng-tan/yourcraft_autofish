package com.example.autofish;

import com.example.autofish.scheduler.AutofishScheduler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import org.lwjgl.glfw.GLFW;

public class AutofishMod implements ClientModInitializer {
    private static AutofishMod instance;
    private Autofish autofish;
    private AutofishScheduler scheduler;
    private AutofishConfig config;
    private KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        instance = this;

        // 加载配置
        config = AutofishConfig.load();

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autofish.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                "category.autofish"
        ));

        this.scheduler = new AutofishScheduler(this);
        this.autofish = new Autofish(this);
        System.out.println("[Autofish] 模组初始化");

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient client) {
        while (openGuiKey.wasPressed()) {
            client.setScreen(new AutofishScreen(this));
        }
        autofish.tick(client);
        scheduler.tick(client);
    }

    // Mixin回调
    public void handleChat(String message) {
        autofish.handleChat(message);
    }

    public void handleTitle(TitleS2CPacket packet) {
        autofish.handleTitle(packet);
    }

    public void handleActionBar(OverlayMessageS2CPacket packet) {
        autofish.handleActionBar(packet);
    }

    public void handleBossBar(BossBarS2CPacket packet) {
        autofish.handleBossBar(packet);
    }

    public static AutofishMod getInstance() {
        return instance;
    }

    public Autofish getAutofish() {
        return autofish;
    }

    public AutofishScheduler getScheduler() {
        return scheduler;
    }

    public AutofishConfig getConfig() {
        return config;
    }
}