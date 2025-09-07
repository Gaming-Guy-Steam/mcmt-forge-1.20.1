package dev.mcmt.forge.runtime;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Eenvoudige config handler voor MCMT.
 * Slaat alleen maxThreads op in JSON.
 */
public class MCMTConfig {

    public int maxThreads = 0;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/mcmtconfig.json");

    public static MCMTConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, MCMTConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new MCMTConfig();
    }

    public static void save(MCMTConfig config) {
        CONFIG_FILE.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
