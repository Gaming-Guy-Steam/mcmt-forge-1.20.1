package dev.mcmt.forge.runtime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Eenvoudige logger voor MCMT.
 * Schrijft logregels naar een bestand en kan CSV exporteren.
 */
public class MCMTLogger {

    private static boolean running = false;
    private static final File LOG_FILE = new File("logs/mcmt.log");

    public static void start() {
        running = true;
        log("Logger started at " + LocalDateTime.now());
    }

    public static void stop() {
        log("Logger stopped at " + LocalDateTime.now());
        running = false;
    }

    public static void log(String message) {
        if (!running) return;
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(LocalDateTime.now() + " - " + message + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportCSV() {
        File csv = new File("logs/mcmt.csv");
        try (FileWriter fw = new FileWriter(csv)) {
            fw.write("timestamp,message\n");
            fw.write(LocalDateTime.now() + ",Example log entry\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
