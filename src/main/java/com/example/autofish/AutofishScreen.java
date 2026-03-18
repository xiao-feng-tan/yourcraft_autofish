package com.example.autofish;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AutofishScreen extends Screen {
    private final AutofishMod mod;
    private final AutofishConfig config;

    // 控件
    private ButtonWidget toggleButton;
    private SliderWidget catchDelaySlider;
    private SliderWidget randomPercentSlider;
    private SliderWidget compensationDelaySlider;
    private ButtonWidget resetCountButton; // 新增归零按钮

    protected AutofishScreen(AutofishMod mod) {
        super(Text.literal("自动钓鱼设置"));
        this.mod = mod;
        this.config = mod.getConfig();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;

        // 总开关按钮
        toggleButton = ButtonWidget.builder(
                        getToggleText(),
                        button -> {
                            config.setEnabled(!config.isEnabled());
                            button.setMessage(getToggleText());
                        })
                .dimensions(centerX - 75, y, 150, 20)
                .build();
        addDrawableChild(toggleButton);

        y += 30;

        // 收杆延迟滑块 (1~2000ms)
        catchDelaySlider = new SliderWidget(centerX - 100, y, 200, 20,
                Text.literal("收杆延迟: " + config.getCatchDelay() + " ms"),
                config.getCatchDelay() / 2000.0) {
            @Override
            protected void updateMessage() {
                int value = (int) (this.value * 2000);
                if (value < 1) value = 1;
                this.setMessage(Text.literal("收杆延迟: " + value + " ms"));
            }

            @Override
            protected void applyValue() {
                int value = (int) (this.value * 2000);
                if (value < 1) value = 1;
                config.setCatchDelay(value);
            }
        };
        addDrawableChild(catchDelaySlider);
        addTooltip(catchDelaySlider, "收到第一个标题包后延迟右键的时间。设为1ms则立即执行。");

        y += 25;

        // 随机百分比滑块 (1~100%)
        randomPercentSlider = new SliderWidget(centerX - 100, y, 200, 20,
                Text.literal("随机波动: " + config.getRandomPercent() + "%"),
                (config.getRandomPercent() - 1) / 99.0) {
            @Override
            protected void updateMessage() {
                int value = (int) (1 + this.value * 99);
                this.setMessage(Text.literal("随机波动: " + value + "%"));
            }

            @Override
            protected void applyValue() {
                int value = (int) (1 + this.value * 99);
                config.setRandomPercent(value);
            }
        };
        addDrawableChild(randomPercentSlider);
        addTooltip(randomPercentSlider, "收杆延迟的随机波动范围。例如1500ms±30% → 1050~1950ms");

        y += 30;

        // 补偿延迟滑块 (1000~7000ms)
        compensationDelaySlider = new SliderWidget(centerX - 100, y, 200, 20,
                Text.literal("补偿延迟: " + config.getCompensationDelay() + " ms"),
                (config.getCompensationDelay() - 1000) / 6000.0) {
            @Override
            protected void updateMessage() {
                int value = (int) (1000 + this.value * 6000);
                this.setMessage(Text.literal("补偿延迟: " + value + " ms"));
            }

            @Override
            protected void applyValue() {
                int value = (int) (1000 + this.value * 6000);
                config.setCompensationDelay(value);
            }
        };
        addDrawableChild(compensationDelaySlider);
        addTooltip(compensationDelaySlider, "最后一个标题包发出后等待此时间，若仍未收到奖励则执行10次右键补偿。");

        y += 30;

        // 显示当前钓鱼次数
        Text countText = Text.literal("当前钓鱼次数: " + config.getCatchCount()).formatted(Formatting.GOLD);
        ButtonWidget dummyButton = ButtonWidget.builder(countText, button -> {})
                .dimensions(centerX - 75, y, 150, 20)
                .build();
        dummyButton.active = false; // 不可点击
        addDrawableChild(dummyButton);

        y += 25;

        // 归零按钮
        resetCountButton = ButtonWidget.builder(
                        Text.literal("归零").formatted(Formatting.RED),
                        button -> {
                            config.resetCatchCount();
                            // 刷新界面文本（重建界面简单方式：重新初始化）
                            this.clearChildren();
                            this.init();
                        })
                .dimensions(centerX - 50, y, 100, 20)
                .build();
        addDrawableChild(resetCountButton);

        y += 40;

        // 关闭按钮
        addDrawableChild(ButtonWidget.builder(
                Text.literal("关闭"),
                button -> this.close()
        ).dimensions(centerX - 50, y, 100, 20).build());
    }

    private Text getToggleText() {
        return Text.literal("自动钓鱼: " + (config.isEnabled() ? "开启" : "关闭"))
                .copy().formatted(config.isEnabled() ? Formatting.GREEN : Formatting.RED);
    }

    private void addTooltip(SliderWidget widget, String tooltip) {
        // 简化，不做悬停提示，可后续添加
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}