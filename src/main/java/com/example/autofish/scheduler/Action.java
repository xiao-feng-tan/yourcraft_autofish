package com.example.autofish.scheduler;

import net.minecraft.util.Util;

public class Action {
    private ActionType actionType;
    private long delay;
    private long timeToComplete;
    private Runnable runnable;

    public Action(ActionType actionType, long delay, Runnable runnable) {
        this.actionType = actionType;
        this.delay = delay;
        this.timeToComplete = Util.getMeasuringTimeMs() + delay;
        this.runnable = runnable;
    }

    public boolean tick() {
        if (Util.getMeasuringTimeMs() >= timeToComplete) {
            runnable.run();
            return true;
        }
        return false;
    }

    public ActionType getActionType() {
        return actionType;
    }
}
