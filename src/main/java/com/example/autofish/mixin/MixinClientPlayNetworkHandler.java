package com.example.autofish.mixin;

import com.example.autofish.AutofishMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.*;
import org.spongepowered.asm.mixin.Mixin;
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

    @Inject(method = "onTitle", at = @At("HEAD"))
    public void onTitle(TitleS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isOnThread()) {
            AutofishMod.getInstance().handleTitle(packet);
        }
    }

    @Inject(method = "onOverlayMessage", at = @At("HEAD"))
    public void onOverlayMessage(OverlayMessageS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isOnThread()) {
            AutofishMod.getInstance().handleActionBar(packet);
        }
    }

    @Inject(method = "onBossBar", at = @At("HEAD"))
    public void onBossBar(BossBarS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isOnThread()) {
            AutofishMod.getInstance().handleBossBar(packet);
        }
    }
}