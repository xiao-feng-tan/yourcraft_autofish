package com.example.autofish;


import com.example.autofish.scheduler.AutofishScheduler;
import com.google.common.util.concurrent.AbstractScheduledService;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;

public class AutofishMod implements ClientModInitializer {
    private static AutofishMod instance;
    private Autofish autofish;
    private AutofishScheduler scheduler;

    @Override
    public void onInitializeClient() {
        instance = this;

        // 创建调度器
        this.scheduler = new AutofishScheduler(this);

        // 创建自动钓鱼逻辑
        this.autofish = new Autofish(this);
        System.out.println("[Autofish] 模组初始化");

        // 注册客户端Tick事件
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient client) {
        autofish.tick(client);
        scheduler.tick(client);
    }

    // Mixin回调 - 聊天消息（用于奖励监控）
    public void handleChat(String message) {
        autofish.handleChat(message);
    }

    // Mixin回调 - 处理速度包（多人游戏检测）
    public void handleVelocityPacket(EntityVelocityUpdateS2CPacket packet) {
        autofish.handleVelocityPacket(packet);
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
}