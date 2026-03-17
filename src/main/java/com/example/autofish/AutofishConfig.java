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
    private int catchDelay = 1500;          // 收杆延迟（毫秒），1~2000
    private int randomPercent = 30;          // 随机百分比 1~100
    private int compensationDelay = 2000;    // 补偿延迟（毫秒），1000~7000

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

    // getters and setters with auto-save
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
}