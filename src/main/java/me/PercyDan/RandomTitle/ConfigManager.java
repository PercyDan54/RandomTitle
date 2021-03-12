package me.PercyDan.RandomTitle;

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
    Map config;
    File configFile = new File("title.yml");

    public ConfigManager() {
        if (!configFile.exists()) {
            LOGGER.info("Config file not found, creating default");
            createDefault();
            config = new Yaml().load(this.getClass().getResourceAsStream("/title.yml"));
        }
        try {
            config = new Yaml().load(new FileInputStream(configFile));
        } catch (Exception e) {
            LOGGER.error("Failed to load config!", e);
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


    public String getTitleFromList() {
        String title = "";
        try {
            List titles = (List) config.get("title");
            title = titles.get(new Random().nextInt(titles.size())).toString();
        } catch (Throwable e) {
            LOGGER.error("Failed to get title from config!", e);
        }
        return title;
    }

    public String getTitleFromHitokoto() {
        LOGGER.info("Getting title from hitokoto");
        String title;
        String response;
        try {
            response = EntityUtils.toString(HttpClients.createDefault().execute(new HttpGet("https://v1.hitokoto.cn/")).getEntity());
            LOGGER.info("Response String: " + response);
            JsonObject json = new JsonParser().parse(response).getAsJsonObject();
            String from = json.get("from").getAsString();
            String sentence = json.get("hitokoto").getAsString();
            title = sentence + "   ——" + from;
        } catch (Throwable e) {
            LOGGER.error("Failed to get title from API!", e);
            return getTitleFromList();
        }
        return title;
    }

    public String getTitle() {
        int mode = (int) config.get("mode");
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

    public String getPrefix() {
        List prefixes = (List) config.get("prefix");
        return prefixes.get((new Random()).nextInt(prefixes.size())).toString();
    }

    public String getFormat() {
        return (String) config.get("format");
    }

    public String getDateFormat() {

        return (String) config.get("dateformat");
    }

}

