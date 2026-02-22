package com.example.autofish;

import com.example.autofish.scheduler.ActionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;

public class Autofish {
    private MinecraftClient client;
    private AutofishMod mod;

    // 状态变量
    private boolean isFishing = false;  // 是否正在钓鱼
    private long castTime = 0;          // 当前抛竿时间戳（用于奖励监控）
    private long currentCastTime = 0;   // 当前正在等待奖励的抛竿时间戳（唯一标识）
    private int clickCounter = 0;        // 点击计数器
    private boolean waitingForReward = false; // 是否在等待奖励消息
    private boolean rewardReceived = false;   // 标记是否收到奖励

    // 服务器检测专用
    private long serverHookCastTime = 0;
    private boolean serverHasHitWater = false;
    private long serverBobberRiseTime = 0;

    public long timeMillis = 0L;

    public Autofish(AutofishMod mod) {
        this.mod = mod;
        this.client = MinecraftClient.getInstance();
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        timeMillis = Util.getMeasuringTimeMs();

        // 检查是否持有钓鱼竿
        if (isHoldingFishingRod()) {
            // 检查是否有鱼钩
            if (client.player.fishHook != null) {
                isFishing = true;

                // 服务器检测：检查鱼钩是否在水中
                checkIfHookInWater(client);
            } else {
                isFishing = false;
                // 重置服务器检测状态
                serverHasHitWater = false;
                serverBobberRiseTime = 0;
            }
        } else {
            isFishing = false;
            serverHasHitWater = false;
            serverBobberRiseTime = 0;
        }
    }

    // 检查鱼钩是否在水中（服务器检测用）
    private void checkIfHookInWater(MinecraftClient client) {
        if (client.player.fishHook != null && !serverHasHitWater) {
            // 假设鱼钩入水需要时间
            if (timeMillis - serverHookCastTime > 1000) {
                serverHasHitWater = true;
            }
        }
    }

    // 处理速度包（多人游戏）
    public void handleVelocityPacket(EntityVelocityUpdateS2CPacket packet) {
        client.execute(() -> {
            if (client.player == null || client.player.fishHook == null) return;

            // 检查是否是玩家的鱼钩
            if (packet.getEntityId() == client.player.fishHook.getId()) {
                // 转换速度值为原模组使用的整数格式
                double velocityY = packet.getVelocityY();
                double velocityX = packet.getVelocityX();
                double velocityZ = packet.getVelocityZ();

                int velocityYInt = (int)(velocityY * 8000.0);
                int velocityXInt = (int)(velocityX * 8000.0);
                int velocityZInt = (int)(velocityZ * 8000.0);

                if (serverHasHitWater && serverBobberRiseTime == 0 && velocityY > 0) {
                    // 鱼钩开始上浮
                    serverBobberRiseTime = timeMillis;
                }

                // 计算鱼钩在水中的时间
                long timeInWater = timeMillis - serverBobberRiseTime;

                // 鱼钩上浮1秒后，开始检测下沉
                if (serverHasHitWater && serverBobberRiseTime != 0 && timeInWater > 1000) {
                    // 原模组条件：X和Z速度为0，Y速度小于-350
                    if (velocityXInt == 0 && velocityZInt == 0 && velocityYInt < -350) {
                        catchFish();
                        // 重置状态
                        serverHasHitWater = false;
                        serverBobberRiseTime = 0;
                    }
                }
            }
        });
    }

    // 处理聊天消息（用于奖励监控）
    public void handleChat(String message) {
        // 如果在等待奖励消息
        if (waitingForReward) {
            // 检查是否包含"您获得了"
            if (message.contains("您获得了")) {
                rewardReceived = true;
                waitingForReward = false;
            }
        }
        if (isFishing && message.contains("鱼群发生了变动")) {
            mod.getScheduler().scheduleAction(ActionType.CHECK_HOOK, 500, () -> {
                checkFishHookAndCast();
            });
        }
    }

    // 收杆动作（统一处理）
    public void catchFish() {
        // 防止重复触发
        if (waitingForReward) {
            return;
        }

        // 右键收杆
        useRod();

        // 开始实时监听奖励消息
        startRewardMonitoring();
    }

    // 开始实时监听奖励消息
    private void startRewardMonitoring() {
        // 重置状态
        castTime = Util.getMeasuringTimeMs();
        currentCastTime = castTime;           // 设置当前唯一标识
        waitingForReward = true;
        rewardReceived = false;
        clickCounter = 0;

        // 设置7.5秒超时，并传递本次的标识
        long expectedCastTime = castTime;
        mod.getScheduler().scheduleAction(ActionType.CHECK_REWARD, 7500, () -> {
            checkRewardAfterTimeout(expectedCastTime);
        });
    }

    // 超时检查（7.9秒后如果没有收到奖励）
    private void checkRewardAfterTimeout(long expectedCastTime) {
        // 如果当前活跃的抛竿标识已经不是本次的，说明已被新抛竿替代，直接忽略
        if (currentCastTime != expectedCastTime) {
            return;
        }

        if (!waitingForReward) {
            return; // 已经处理过了
        }

        // 如果仍在等待且未收到奖励
        if (waitingForReward && !rewardReceived) {
            waitingForReward = false;
            clickCounter = 0;

            // 检查是否仍然持有鱼竿
            if (isHoldingFishingRod()) {
                // 执行10次右键
                scheduleNextClick();
            }
            // 否则不执行任何操作
        }
    }

    /**
     * 连续右键10次，完成后延迟1秒检测浮漂实体
     */
    private void scheduleNextClick() {
        if (clickCounter >= 10) {
            // 10次点击完成，重置计数器
            clickCounter = 0;

            // 延迟1秒，检查浮漂实体是否存在
            mod.getScheduler().scheduleAction(ActionType.CHECK_HOOK, 1000, () -> {
                checkFishHookAndCast();
            });
            return;
        }

        // 安排下一次右键（间隔200ms）
        mod.getScheduler().scheduleAction(ActionType.CLICK_ROD, 200, () -> {
            useRod();
            clickCounter++;
            scheduleNextClick(); // 递归调度
        });
    }

    /**
     * 检查玩家当前是否存在浮漂实体（fishHook）
     * - 若不存在，则执行一次右键（抛竿）
     * - 若已存在，则不做任何操作
     */
    private void checkFishHookAndCast() {
        if (client.player == null || client.world == null) {
            return;
        }

        if (client.player.fishHook == null) {
            useRod();
        }
        // 否则不做操作
    }

    // 记录抛竿时间（服务器检测用）
    public void recordCast() {
        serverHookCastTime = timeMillis;
        serverHasHitWater = false;
        serverBobberRiseTime = 0;
    }

    // 使用鱼竿（右键点击）
    public void useRod() {
        if (client.player == null || client.world == null) return;

        Hand hand = getCorrectHand();
        ActionResult actionResult = client.interactionManager.interactItem(client.player, hand);

        if (actionResult.isAccepted()) {
            if (actionResult.shouldSwingHand()) {
                client.player.swingHand(hand);
            }

            // 记录抛竿时间（用于服务器检测）
            recordCast();
        }
    }

    // 获取正确的手（主手或副手）
    private Hand getCorrectHand() {
        if (isItemFishingRod(client.player.getOffHandStack().getItem())) {
            return Hand.OFF_HAND;
        }
        return Hand.MAIN_HAND;
    }

    // 检查是否持有钓鱼竿
    private boolean isHoldingFishingRod() {
        ItemStack mainHand = client.player.getMainHandStack();
        ItemStack offHand = client.player.getOffHandStack();

        return isItemFishingRod(mainHand.getItem()) ||
                isItemFishingRod(offHand.getItem());
    }

    // 检查物品是否为钓鱼竿
    private boolean isItemFishingRod(Item item) {
        return item == Items.FISHING_ROD || item instanceof FishingRodItem;
    }
}