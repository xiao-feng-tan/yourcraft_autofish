package com.example.autofish;

import com.example.autofish.scheduler.ActionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;

import java.util.Random;

public class Autofish {
    private MinecraftClient client;
    private AutofishMod mod;
    private AutofishConfig config;

    // 标题序列状态
    private boolean isTitleSequenceActive = false;
    private long lastTitleTime = 0;
    private boolean rewardReceivedDuringTitle = false;
    private boolean rodUseScheduled = false; // 防止重复调度收杆

    // 通用时间戳
    public long timeMillis = 0L;

    private final Random random = new Random();

    public Autofish(AutofishMod mod) {
        this.mod = mod;
        this.client = MinecraftClient.getInstance();
        this.config = mod.getConfig();
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        timeMillis = Util.getMeasuringTimeMs();

        // 标题序列超时检测（使用配置的补偿延迟）
        if (isTitleSequenceActive) {
            long idleTime = timeMillis - lastTitleTime;
            if (idleTime > config.getCompensationDelay()) {
                if (!rewardReceivedDuringTitle) {
                    performCompensation();
                }
                isTitleSequenceActive = false;
                rodUseScheduled = false;
            }
        }
    }

    // 补偿操作
    private void performCompensation() {
        scheduleNextClick();
    }

    // 处理标题包
    public void handleTitle(TitleS2CPacket packet) {
        client.execute(() -> {
            if (!config.isEnabled()) return;
            onEmptyTitle();
        });
    }

    private void onEmptyTitle() {
        timeMillis = Util.getMeasuringTimeMs();

        if (!isTitleSequenceActive) {
            // 第一个标题包：开始序列
            isTitleSequenceActive = true;
            lastTitleTime = timeMillis;
            rewardReceivedDuringTitle = false;
            rodUseScheduled = false;

            int baseDelay = config.getCatchDelay();
            if (baseDelay <= 1) {
                useRod();
            } else {
                int range = (int) (baseDelay * (config.getRandomPercent() / 100.0));
                int actualDelay = baseDelay + (range > 0 ? random.nextInt(2 * range + 1) - range : 0);
                actualDelay = Math.max(1, actualDelay);
                final long delayMs = actualDelay;

                rodUseScheduled = true;
                mod.getScheduler().scheduleAction(ActionType.CLICK_ROD, delayMs, () -> {
                    if (isTitleSequenceActive && !rewardReceivedDuringTitle) {
                        useRod();
                    }
                    rodUseScheduled = false;
                });
            }
        } else {
            lastTitleTime = timeMillis;
        }
    }

    public void handleChat(String message) {
        client.execute(() -> {
            if (!config.isEnabled()) return;
            if (message.contains("您获得了")) {
                config.incrementCatchCount();

                if (isTitleSequenceActive) {
                    rewardReceivedDuringTitle = true;
                    isTitleSequenceActive = false;
                    rodUseScheduled = false;
                    System.out.println("[Autofish] 标题序列期间收到奖励，序列结束");
                }
            } else if (message.contains("鱼群发生了变动")) {
                // 延迟0.5秒检查浮漂并重新抛竿
                mod.getScheduler().scheduleAction(ActionType.CHECK_HOOK, 500, this::checkFishHookAndCast);
            }
        });
    }

    // 其他处理器（保留空实现，避免 Mixin 报错）
    public void handleActionBar(OverlayMessageS2CPacket packet) {}
    public void handleBossBar(BossBarS2CPacket packet) {} // 需导入，但此处未导入，需补全

    // ========== 基础工具方法 ==========

    private int clickCounter = 0;

    private void scheduleNextClick() {
        if (clickCounter >= 10) {
            clickCounter = 0;
            mod.getScheduler().scheduleAction(ActionType.CHECK_HOOK, 500, this::checkFishHookAndCast);
            return;
        }

        mod.getScheduler().scheduleAction(ActionType.CLICK_ROD, 200, () -> {
            useRod();
            clickCounter++;
            scheduleNextClick();
        });
    }

    private void checkFishHookAndCast() {
        if (client.player == null || client.world == null) return;
        if (client.player.fishHook == null) {
            useRod();
        }
    }

    public void useRod() {
        if (client.player == null || client.world == null) return;

        // 检查是否持有钓鱼竿
        if (!isHoldingFishingRod()) {
            return;
        }

        Hand hand = getCorrectHand();
        ActionResult actionResult = client.interactionManager.interactItem(client.player, hand);

        if (actionResult.isAccepted()) {
            if (actionResult.shouldSwingHand()) {
                client.player.swingHand(hand);
            }
        }
    }

    private Hand getCorrectHand() {
        if (isItemFishingRod(client.player.getOffHandStack().getItem())) {
            return Hand.OFF_HAND;
        }
        return Hand.MAIN_HAND;
    }

    private boolean isHoldingFishingRod() {
        ItemStack mainHand = client.player.getMainHandStack();
        ItemStack offHand = client.player.getOffHandStack();
        return isItemFishingRod(mainHand.getItem()) || isItemFishingRod(offHand.getItem());
    }

    private boolean isItemFishingRod(Item item) {
        return item == Items.FISHING_ROD || item instanceof FishingRodItem;
    }
}