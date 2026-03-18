package com.example.autofish;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AutofishOverlay {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static AutofishConfig config;

    public static void init(AutofishMod mod) {
        config = mod.getConfig();

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (client.player == null || client.world == null) return;
            renderHUD(drawContext);
        });
    }

    private static void renderHUD(DrawContext context) {
        int count = config.getCatchCount();
        Text text = Text.literal("钓鱼次数: " + count).formatted(Formatting.GOLD);
        TextRenderer textRenderer = client.textRenderer;
        int x = client.getWindow().getScaledWidth() - textRenderer.getWidth(text) - 5;
        int y = 5;
        context.drawText(textRenderer, text, x, y, 0xFFFFFF, true);
    }
}