package com.example.autofish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutofishConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autofish.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private boolean enabled = true;
    private int catchDelay = 1500;
    private int randomPercent = 30;
    private int compensationDelay = 2000;
    private int catchCount = 0; // 新增：钓鱼次数

    public static AutofishConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                return GSON.fromJson(json, AutofishConfig.class);
            } catch (IOException e) {
                System.err.println("[Autofish] 无法读取配置文件，使用默认值");
                e.printStackTrace();
            }
        }
        return new AutofishConfig();
    }

    public void save() {
        try {
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            System.err.println("[Autofish] 无法保存配置文件");
            e.printStackTrace();
        }
    }

    // Getter/Setter with auto-save
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public int getCatchDelay() {
        return catchDelay;
    }

    public void setCatchDelay(int catchDelay) {
        this.catchDelay = Math.max(1, Math.min(2000, catchDelay));
        save();
    }

    public int getRandomPercent() {
        return randomPercent;
    }

    public void setRandomPercent(int randomPercent) {
        this.randomPercent = Math.max(1, Math.min(100, randomPercent));
        save();
    }

    public int getCompensationDelay() {
        return compensationDelay;
    }

    public void setCompensationDelay(int compensationDelay) {
        this.compensationDelay = Math.max(1000, Math.min(7000, compensationDelay));
        save();
    }

    public int getCatchCount() {
        return catchCount;
    }

    public void setCatchCount(int catchCount) {
        this.catchCount = Math.max(0, catchCount);
        save();
    }

    public void incrementCatchCount() {
        this.catchCount++;
        save();
    }

    public void resetCatchCount() {
        this.catchCount = 0;
        save();
    }
}