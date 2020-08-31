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
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class RandomTitleUtil {
    private static final Logger LOGGER = LogManager.getLogger("RandomTitleUtil");
    static Map yaml;
    static File configFile = new File("title.yml");

    static {
        if (configFile.exists()) {
            try {
                yaml = new Yaml().load(new FileInputStream(configFile));
            } catch (Throwable e) {
                LOGGER.catching(e);
            }
        }
    }

    public static String getTitleFromList() {
        String title = null;
        try {
            List titles = (List) yaml.get("title");
            title = titles.get(new Random().nextInt(titles.size())).toString();
        } catch (Throwable e) {
            LOGGER.catching(e);
        }
        return title;
    }

    public static String getTitleFromHitokoto() {
        LOGGER.info("Getting title from hitokoto");
        String title;
        String response = null;
        try {
            response = EntityUtils.toString(HttpClients.createDefault().execute(new HttpGet("https://v1.hitokoto.cn/")).getEntity());
            LOGGER.info("Response String:\n" + response);
        } catch (IOException e) {
            LOGGER.catching(e);
        }
        JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
        String from = jsonObject.get("from").getAsString();
        String sentence = jsonObject.get("hitokoto").getAsString();
        if (from.contentEquals("网络")) {
            title = sentence + "   ——来自" + from;
        }
        if (from.contains("《") || from.contains("》") || from.contentEquals("原创")) {
            title = sentence + "   ——" + jsonObject.get("creator") + jsonObject.get("from").getAsString();
        } else {
            title = sentence + "   ——《" + jsonObject.get("from").getAsString() + "》";
        }
        return title;
    }

    public static String getTitle() {
        if (!configFile.exists()) {
            LOGGER.warn("Config file not found!");
            return getTitleFromHitokoto();
        }
        int mode = (int) yaml.get("mode");
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

    public static String getPrefix() {
        if (configFile.exists()) {
            List prefixes = (List) yaml.get("prefix");
            String prefix = prefixes.get((new Random()).nextInt(prefixes.size())).toString();
            return prefix;
        }
        return "Minecraft %version%";
    }

    public static String getFormat() {
        if (configFile.exists()) {
            return (String) yaml.get("format");
        }
        return "%prefix%  |  %title%";
    }

    public static String getDateFormat() {
        if (configFile.exists()) {
            return (String) yaml.get("dateformat");
        }
        return "yyyy年MM月dd日 HH:mm:ss";
    }

}

