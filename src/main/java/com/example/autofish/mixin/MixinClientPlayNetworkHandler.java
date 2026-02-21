package com.example.autofish.mixin;

import com.example.autofish.AutofishMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    public void onChatMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isOnThread()) {
            String message = packet.content().getString();
            AutofishMod.getInstance().handleChat(message);
        }
    }

    // 关键：添加速度包检测（多人游戏）
    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"))
    public void onVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isOnThread()) {
            AutofishMod.getInstance().handleVelocityPacket(packet);
        }
    }
}