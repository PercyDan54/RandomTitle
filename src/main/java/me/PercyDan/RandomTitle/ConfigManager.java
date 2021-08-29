package me.percydan.RandomTitle;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class ConfigManager {
    private final Logger LOGGER = LogManager.getLogger("RandomTitle");
    File configFile = new File("title.yml");
    Map<String, Object> config;
    Map<String, Object> defaultConfig;

    public ConfigManager() {
        LOGGER.info("RandomTitle by PercyDan https://github.com/PercyDan54/RandomTitle");
        defaultConfig = new Yaml().load(this.getClass().getResourceAsStream("/title.yml"));
        if (!configFile.exists()) {
            LOGGER.info("Config file not found, creating default");
            createDefault();
        }
        try {
            config = new Yaml().load(new FileInputStream(configFile));
        } catch (Exception e) {
            LOGGER.error("Failed to load config!", e);
            config = defaultConfig;
        }
    }

    public void createDefault() {
        InputStream inputStream = this.getClass().getResourceAsStream("/title.yml");
        try {
            if (!configFile.exists()) {
                OutputStream outputStream = new FileOutputStream("title.yml");
                byte[] b = new byte[1024];
                int len;
                while ((len = inputStream.read(b)) != -1) {
                    outputStream.write(b, 0, len);
                }
                outputStream.close();
            }
        } catch (Throwable e) {
            LOGGER.error("Failed to create default config!", e);
        }
    }

    public <T> T Get(String key) {
        T value = (T) config.get(key);
        if (value == null) return (T) defaultConfig.get(key);
        return value;
    }

    private String getTitleFromList() {
        String title = "";
        try {
            List<String> titles = Get("title");
            title = titles.get(new Random().nextInt(titles.size()));
        } catch (Throwable e) {
            LOGGER.error("Failed to get title from config!", e);
        }
        return title;
    }

    private String getTitleFromHitokoto() {
        LOGGER.info("Getting title from Hitokoto API");
        String title;
        String response;
        try {
            response = EntityUtils.toString(HttpClients.createDefault().execute(new HttpGet("https://v1.hitokoto.cn/")).getEntity());
            LOGGER.info("Hitokoto Response String: " + response);
            JsonObject json = new JsonParser().parse(response).getAsJsonObject();
            String from = json.get("from").getAsString();
            String sentence = json.get("hitokoto").getAsString();
            String type = json.get("type").getAsString();
            title = sentence + "   —— ";
            switch (type) {
                case "e":
                    title += json.get("creator").getAsString() + " 原创";
                    break;
                case "f":
                    title += "来自网络";
                    break;
                default:
                    title += from;
            }

        } catch (Throwable e) {
            LOGGER.error("Failed to get title from API!", e);
            return getTitleFromList();
        }
        return title;
    }

    public String getTitle() {
        int mode = Get("mode");

        switch (mode) {
            case 0:
                return getTitleFromHitokoto();
            case 1:
                return getTitleFromList();
            case 2:
                boolean use = new Random().nextBoolean();
                if (use) {
                    return getTitleFromList();
                } else {
                    return getTitleFromHitokoto();
                }
        }
        return getTitleFromList();
    }

}

