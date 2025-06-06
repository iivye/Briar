package me.iivye.plugin.briarmarket.util;


import me.iivye.plugin.briarmarket.Briar;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public final class UpdateChecker {
    private final Briar plugin;
    private String lastCheckedVersion = "N/A";

    public UpdateChecker(Briar plugin) {
        this.plugin = plugin;
    }

    public String getLastCheckedVersion() {
        return lastCheckedVersion;
    }

    public void check() {
        plugin.getLogger().info("Checking for updates...");
        final String resourceId = "%%__RESOURCE__%%";
        final String isBuiltByBit = "%%__BUILTBYBIT__%%";
        final String currentVersion = plugin.getDescription().getVersion();

        if (currentVersion.contains("BETA")) {
            plugin.getLogger().info("Update check was cancelled because you are running a beta build!");
            return;
        }

        if (resourceId.startsWith("%")) {
            plugin.getLogger().info("Update check was cancelled because you are not using a purchased plugin JAR!");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (InputStream inputStream = new URL("https://api.iivye.me/briar").openStream();
                 Scanner scanner = new Scanner(inputStream)) {

                if (!scanner.hasNext()) return;

                String latestVersion = scanner.next();
                if (currentVersion.equals(latestVersion)) {
                    plugin.getLogger().info("No new updates found.");
                } else {
                    plugin.getLogger().info("A new update was found. You are on " + currentVersion + " while the latest version is " + latestVersion + ".");
                    String downloadUrl = isBuiltByBit.equalsIgnoreCase("true") ?
                            "https://builtbybit.com/resources/" + resourceId :
                            "https://www.spigotmc.org/resources/" + resourceId;
                    plugin.getLogger().info("Please install this update from: " + downloadUrl);
                }
                lastCheckedVersion = latestVersion;

            } catch (IOException e) {
                plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
            }
        });
    }
}
