package com.example.autofish.scheduler;

import com.example.autofish.AutofishMod;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AutofishScheduler {
    private AutofishMod mod;
    private List<Action> queuedActions = new ArrayList<>();
    private List<Action> actionsToAdd = new ArrayList<>();

    public AutofishScheduler(AutofishMod mod) {
        this.mod = mod;
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            queuedActions.clear();
            actionsToAdd.clear();
            return;
        }

        // 使用迭代器安全地遍历列表
        Iterator<Action> iterator = queuedActions.iterator();
        while (iterator.hasNext()) {
            Action action = iterator.next();
            if (action.tick()) {
                iterator.remove();
            }
        }

        // 添加新任务
        if (!actionsToAdd.isEmpty()) {
            queuedActions.addAll(actionsToAdd);
            actionsToAdd.clear();
        }
    }

    public void scheduleAction(ActionType actionType, long delay, Runnable runnable) {
        // 将新任务添加到临时列表，避免在迭代时修改
        actionsToAdd.add(new Action(actionType, delay, runnable));
    }

}