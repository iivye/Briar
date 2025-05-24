package me.iivye.plugin.briar.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    private static final int VERSION = getVersion();
    private static final boolean SUPPORTS_RGB = VERSION >= 16 || VERSION == -1;

    private static final long CACHE_UPDATE_INTERVAL_TICKS = 20L * 60; // 1 minute

    private static final int BATCH_SIZE = 100; // Number of players per chunk
    private static final int BATCH_DELAY_TICKS = 2; // Delay between batches

    public static final Map<String, Integer> unlockedTagCache = new ConcurrentHashMap<>();

    private static final Pattern p1 = Pattern.compile("\\{#([0-9A-Fa-f]{6})\\}");
    private static final Pattern p2 = Pattern.compile("&#([A-Fa-f0-9]){6}");
    private static final Pattern p3 = Pattern.compile("#([A-Fa-f0-9]){6}");
    private static final Pattern p4 = Pattern.compile("<#([A-Fa-f0-9])>{6}");
    private static final Pattern p5 = Pattern.compile("<#&([A-Fa-f0-9])>{6}");

    private static final Pattern g1 = Pattern.compile("<gradient:([0-9A-Fa-f]{6})>(.*?)</gradient:([0-9A-Fa-f]{6})>");
    private static final Pattern g2 = Pattern.compile("<gradient:#([A-Fa-f0-9]{6})>(.*?)</gradient:#([A-Fa-f0-9]{6})>");
    private static final Pattern g3 = Pattern.compile("<gradient:&#([A-Fa-f0-9]{6})>(.*?)</gradient:&#([A-Fa-f0-9]{6})>");

    private static final Pattern g4 = Pattern.compile("<g:&#([A-Fa-f0-9]){6}>(.*?)</g:&#([A-Fa-f0-9]){6}");
    private static final Pattern g5 = Pattern.compile("<g:&#([A-Fa-f0-9]){6}>(.*?)</g:&#([A-Fa-f0-9]){6}");
    private static final Pattern g6 = Pattern.compile("<g:&#([A-Fa-f0-9]){6}>(.*?)</g:&#([A-Fa-f0-9]){6}");

    private static final Pattern rainbow1 = Pattern.compile("<rainbow>(.*?)</rainbow>");
    private static final Pattern rainbow2 = Pattern.compile("<r>(.*?)</r>");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.#");

    public static String format(String message) {
        if (!SUPPORTS_RGB) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            return message;
        } else {
            message = ChatColor.translateAlternateColorCodes('&', message);

            message = applyGradients(message, g1);
            message = applyGradients(message, g2);
            message = applyGradients(message, g3);
            message = applyGradients(message, g4);
            message = applyGradients(message, g5);
            message = applyGradients(message, g6);
            message = applyRainbow(message);

            Matcher match = p1.matcher(message);
            while (match.find()) {
                getRGB(message);
            }

            Matcher hexMatcher = p1.matcher(message);
            while (hexMatcher.find()) {
                message = message.replace(hexMatcher.group(), ChatColor.of(hexMatcher.group().substring(1)).toString());
            }

            hexMatcher = p2.matcher(message);
            while (hexMatcher.find()) {
                message = message.replace(hexMatcher.group(), ChatColor.of(hexMatcher.group().substring(1)).toString());
            }

            hexMatcher = p3.matcher(message);
            while (hexMatcher.find()) {
                message = message.replace(hexMatcher.group(), ChatColor.of(hexMatcher.group()).toString());
            }

            hexMatcher = p4.matcher(message);
            while (hexMatcher.find()) {
                String hexColor = hexMatcher.group().substring(2, 8);
                message = message.replace(hexMatcher.group(), ChatColor.of(hexColor).toString());
            }

            hexMatcher = p5.matcher(message);
            while (hexMatcher.find()) {
                String hexColor = hexMatcher.group().substring(3, 9);
                message = message.replace(hexMatcher.group(), ChatColor.of(hexColor).toString());
            }

            message = message.replace("<black>", "§0")
                    .replace("<dark_blue>", "§1")
                    .replace("<dark_green>", "§2")
                    .replace("<dark_aqua>", "§3")
                    .replace("<dark_red>", "§4")
                    .replace("<dark_purple>", "§5")
                    .replace("<gold>", "§6")
                    .replace("<gray>", "§7")
                    .replace("<dark_gray>", "§8")
                    .replace("<blue>", "§9")
                    .replace("<green>", "§a")
                    .replace("<aqua>", "§b")
                    .replace("<red>", "§c")
                    .replace("<light_purple>", "§d")
                    .replace("<yellow>", "§e")
                    .replace("<white>", "§f")
                    .replace("<obfuscated>", "§k")
                    .replace("<bold>", "§l")
                    .replace("<strikethrough>", "§m")
                    .replace("<underlined>", "§n")
                    .replace("<italic>", "§o")
                    .replace("<reset>", "§r");

            return message;
        }
    }

    private static String applyGradients(String message, Pattern gradientPattern) {
        Matcher gradientMatcher = gradientPattern.matcher(message);
        while (gradientMatcher.find()) {
            // Extract the gradient definition and text
            String[] parts = gradientMatcher.group(1).split(":");
            String text = gradientMatcher.group(2);

            // Extract colors and formatting from the gradient tag
            List<String> colors = new ArrayList<>();
            StringBuilder formatting = new StringBuilder();
            for (String part : parts) {
                if (part.matches("^[0-9A-Fa-f]{6}$")) { // Hex color
                    colors.add(part);
                } else if (part.equalsIgnoreCase("BOLD") || part.equalsIgnoreCase("ITALIC") || part.equalsIgnoreCase("UNDERLINE")) {
                    formatting.append("§").append(part.charAt(0)); // Translate to Minecraft code
                }
            }

            String gradientText = createMultiGradientText(text, colors, formatting.toString());
            message = message.replace(gradientMatcher.group(), gradientText);
        }
        return message;
    }

    private static String createMultiGradientText(String text, List<String> colors, String formatting) {
        int length = text.length();
        int colorCount = colors.size();

        if (colorCount < 2) throw new IllegalArgumentException("At least two colors are required for gradients.");

        StringBuilder gradientBuilder = new StringBuilder();

        // Split the text into segments for each color pair
        int segmentLength = length / (colorCount - 1);
        for (int i = 0; i < colorCount - 1; i++) {
            Color start = Color.decode("#" + colors.get(i));
            Color end = Color.decode("#" + colors.get(i + 1));
            for (int j = 0; j < segmentLength && i * segmentLength + j < length; j++) {
                int charIndex = i * segmentLength + j;
                float ratio = (float) j / segmentLength;

                int red = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
                int green = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
                int blue = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));

                String hexColor = String.format("%02X%02X%02X", red, green, blue);
                gradientBuilder.append(toMinecraftHex(hexColor)).append(formatting).append(text.charAt(charIndex));
            }
        }

        // Handle any leftover characters due to rounding
        for (int i = (colorCount - 1) * segmentLength; i < length; i++) {
            String lastColor = colors.get(colorCount - 1);
            gradientBuilder.append(toMinecraftHex(lastColor)).append(formatting).append(text.charAt(i));
        }

        return gradientBuilder.toString();
    }

    private static String applyRainbow(String message) {
        Matcher rainbowMatcher1 = rainbow1.matcher(message);
        while (rainbowMatcher1.find()) {
            String[] parts = rainbowMatcher1.group(1).split(":");
            String text = rainbowMatcher1.group(2);

            StringBuilder formatting = new StringBuilder();
            for (String part : parts) {
                if (part.equalsIgnoreCase("BOLD") || part.equalsIgnoreCase("ITALIC") || part.equalsIgnoreCase("UNDERLINE")) {
                    formatting.append("§").append(part.charAt(0)); // Translate to Minecraft code
                }
            }

            String rainbowText = createRainbowTextWithFormatting(text, formatting.toString());
            message = message.replace(rainbowMatcher1.group(), rainbowText);
        }

        Matcher rainbowMatcher2 = rainbow2.matcher(message);
        while (rainbowMatcher2.find()) {
            String[] parts = rainbowMatcher2.group(1).split(":");
            String text = rainbowMatcher2.group(2);

            StringBuilder formatting = new StringBuilder();
            for (String part : parts) {
                if (part.equalsIgnoreCase("BOLD") || part.equalsIgnoreCase("ITALIC") || part.equalsIgnoreCase("UNDERLINE")) {
                    formatting.append("§").append(part.charAt(0)); // Translate to Minecraft code
                }
            }

            String rainbowText = createRainbowTextWithFormatting(text, formatting.toString());
            message = message.replace(rainbowMatcher2.group(), rainbowText);
        }
        return message;
    }

    private static String createRainbowTextWithFormatting(String text, String formatting) {
        int length = text.length();
        Color[] rainbowColors = {
                Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA
        };
        int colorCount = rainbowColors.length;

        StringBuilder rainbowBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char currentChar = text.charAt(i);

            // Calculate the color for this character
            Color color = rainbowColors[i % colorCount];
            String hexColor = String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());

            rainbowBuilder.append(toMinecraftHex(hexColor)).append(formatting).append(currentChar);
        }

        return rainbowBuilder.toString();
    }

    private static String toMinecraftHex(String hexColor) {
        StringBuilder minecraftHex = new StringBuilder("§x");
        for (char c : hexColor.toCharArray()) {
            minecraftHex.append("§").append(c);
        }
        return minecraftHex.toString();
    }


    public static boolean isValidVersion(String version) {
        return version.matches("\\d+(\\.\\d+)*"); // Matches version strings like "1", "1.2", "1.2.3", etc.
    }

    public static boolean isVersionLessThan(String version) {
        String serverVersion = Bukkit.getVersion();
        String[] serverParts = serverVersion.split(" ")[2].split("\\.");
        String[] targetParts = version.split("\\.");

        for (int i = 0; i < Math.min(serverParts.length, targetParts.length); i++) {
            if (!isValidVersion(serverParts[i]) || !isValidVersion(targetParts[i])) {
                return false;
            }

            int serverPart = Integer.parseInt(serverParts[i]);
            int targetPart = Integer.parseInt(targetParts[i]);

            if (serverPart < targetPart) {
                return true;
            } else if (serverPart > targetPart) {
                return false;
            }
        }
        return serverParts.length < targetParts.length;
    }

    /**
     * Gets a simplified major version (..., 9, 10, ..., 14).
     * In most cases, you shouldn't be using this method.
     *
     * @return the simplified major version, or -1 for bungeecord
     * @since 1.0.0
     */
    private static int getVersion() {
        if (!classExists("org.bukkit.Bukkit") && classExists("net.md_5.bungee.api.ChatColor")) {
            return -1;
        }

        String version = Bukkit.getVersion();
        Validate.notEmpty(version, "Cannot get major Minecraft version from null or empty string");

        // getVersion()
        int index = version.lastIndexOf("MC:");
        if (index != -1) {
            version = version.substring(index + 4, version.length() - 1);
        } else if (version.endsWith("SNAPSHOT")) {
            // getBukkitVersion()
            index = version.indexOf('-');
            version = version.substring(0, index);
        }
        // 1.13.2, 1.14.4, etc...
        int lastDot = version.lastIndexOf('.');
        if (version.indexOf('.') != lastDot) version = version.substring(0, lastDot);

        return Integer.parseInt(version.substring(2));
    }

    /**
     * Checks if a class exists in the current server
     *
     * @param path The path of that class
     * @return true if the class exists, false if it doesn't
     * @since 1.0.7
     */
    private static boolean classExists(final String path) {
        try {
            Class.forName(path);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

//    public static String parseMiniMessage(String message) {
//        // Handle MiniMessage parsing and legacy conversion
//        Component legacy = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
//        String miniMessage = MiniMessage.miniMessage().serialize(legacy).replace("\\", "");
//        Component component = MiniMessage.miniMessage().deserialize(miniMessage);
//        return LegacyComponentSerializer.legacySection().serialize(component);
//    }

    public static String colorizeRGB(String input) {
        Matcher matcher = p1.matcher(input);
        String color;
        while (matcher.find()) {
            color = matcher.group(1);
            if (color == null) {
                color = matcher.group(2);
            }
            input = input.replace(matcher.group(), ChatColor.of(color) + "");
        }
        return input;
    }

    public static String deformat(String str) {
        return ChatColor.stripColor(format(str));
    }

    public static void msgPlayer(Player player, String... str) {
        for (String msg : str) {
            player.sendMessage(format(msg));
        }
    }

    public static void msgPlayer(CommandSender player, String... str) {
        for (String msg : str) {
            player.sendMessage(format(msg));
        }
    }

    public static void titlePlayer(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(format(title), format(subtitle), fadeIn, stay, fadeOut);
    }

    public static void soundPlayer(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static List<String> color(List<String> lore) {
        return lore.stream().map(Utils::format).collect(Collectors.toList());
    }

    private static final Pattern rgbPat = Pattern.compile("(?:#|0x)(?:[a-f0-9]{3}|[a-f0-9]{6})\\b|(?:rgb|hsl)a?\\([^\\)]*\\)");

    public static String getRGB(String msg) {
        String temp = msg;
        try {

            String status = "none";
            String r = "";
            String g = "";
            String b = "";
            Matcher match = rgbPat.matcher(msg);
            while (match.find()) {
                String color = msg.substring(match.start(), match.end());
                for (char character : msg.substring(match.start(), match.end()).toCharArray()) {
                    switch (character) {
                        case '(':
                            status = "r";
                            continue;
                        case ',':
                            switch (status) {
                                case "r":
                                    status = "g";
                                    continue;
                                case "g":
                                    status = "b";
                                    continue;
                                default:
                                    break;
                            }
                        default:
                            switch (status) {
                                case "r":
                                    r = r + character;
                                    continue;
                                case "g":
                                    g = g + character;
                                    continue;
                                case "b":
                                    b = b + character;
                                    continue;
                            }
                            break;
                    }


                }
                b = b.replace(")", "");
                Color col = new Color(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
                temp = temp.replaceFirst("(?:#|0x)(?:[a-f0-9]{3}|[a-f0-9]{6})\\b|(?:rgb|hsl)a?\\([^\\)]*\\)", ChatColor.of(col) + "");
                r = "";
                g = "";
                b = "";
                status = "none";
            }
        } catch (Exception e) {
            return msg;
        }
        return temp;
    }

    public static String replacePlaceholders(Player user, String base) {
        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) return "";

        return PlaceholderAPI.setPlaceholders(user, base);
    }

//    public static String globalPlaceholders(Player user, String message) {
//        message = replacePlaceholders(user, message);
//        if (Bukkit.getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
//            message = FontImageWrapper.replaceFontImages(message);
//        }
//
//        return message;
//    }

    public static int compareVersions(String version1, String version2) {
        String[] splitVersion1 = version1.split("\\.");
        String[] splitVersion2 = version2.split("\\.");

        int length = Math.max(splitVersion1.length, splitVersion2.length);

        for (int i = 0; i < length; i++) {
            int v1 = i < splitVersion1.length ? Integer.parseInt(splitVersion1[i]) : 0;
            int v2 = i < splitVersion2.length ? Integer.parseInt(splitVersion2[i]) : 0;
            if (v1 < v2) {
                return -1;
            }
            if (v1 > v2) {
                return 1;
            }
        }
        return 0; // versions are equal
    }

    private static String formatLarge(double n, int iteration) {
        double f = n / 1000.0D;
        return f < 1000 || iteration >= getNumberFormat().length - 1 ?
                DECIMAL_FORMAT.format(f) + getNumberFormat()[iteration] : formatLarge(f, iteration + 1);
    }

    public static String formatNumber(double value) {
        return value < 1000 ? DECIMAL_FORMAT.format(value) : formatLarge(value, 0);
    }

    private static String[] getNumberFormat() {
        return "k;M;B;T;Q;QQ;S;SS;OC;N;D;UN;DD;TR;QT;QN;SD;SPD;OD;ND;VG;UVG;DVG;TVG;QTV;QNV;SEV;SPV;OVG;NVG;TG".split(";");
    }

//    public static ItemStack getItemWithIA(String id) {
//        if (CustomStack.isInRegistry(id)) {
//            CustomStack stack = CustomStack.getInstance(id);
//            if (stack != null) {
//                return stack.getItemStack();
//            }
//        }
//
//        return null;
//    }

    public static boolean isPaperVersionAtLeast(int major, int minor, int patch) {
        String version = Bukkit.getVersion(); // Example: git-Paper-441 (MC: 1.21.5)
        Pattern pattern = Pattern.compile("\\(MC: (\\d+)\\.(\\d+)(?:\\.(\\d+))?\\)");
        Matcher matcher = pattern.matcher(version);

        if (matcher.find()) {
            int majorVer = Integer.parseInt(matcher.group(1));
            int minorVer = Integer.parseInt(matcher.group(2));
            int patchVer = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;

            if (majorVer > major) return true;
            if (majorVer == major && minorVer > minor) return true;
            if (majorVer == major && minorVer == minor && patchVer >= patch) return true;
        }

        return false;
    }
}