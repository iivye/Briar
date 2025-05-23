package me.iivye.plugin.briar.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import redempt.redlib.misc.FormatUtils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

public class Text {

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#.##");

    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String color(String str) {
        return FormatUtils.color(str, true);
    }

    public static List<String> color(List<String> list) {
        return list.stream().map(Text::color).collect(Collectors.toList());
    }

    public static String format(OfflinePlayer context, String string) {
        if (context != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(context, string);
        }
        return string;
    }

    public static String replacements(String str, String... replacements) {
        if (replacements.length % 2 != 0) return str;
        for (int i = 0; i < replacements.length; i += 2) {
            str = str.replace(replacements[i], replacements[i + 1]);
        }
        return str;
    }

    private static String formatPlaceholders(Player player, String str, String... replacements) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            str = PlaceholderAPI.setPlaceholders(player, str);
        }
        return replacements(str, replacements);
    }

    public static ItemStack specializeItem(Player player, ItemStack item, String... replacements) {
        item = item.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (meta.hasDisplayName()) {
            meta.setDisplayName(formatPlaceholders(player, meta.getDisplayName(), replacements));
        }
        if (meta.hasLore()) {
            meta.setLore(meta.getLore().stream()
                    .map(line -> formatPlaceholders(player, line, replacements))
                    .collect(Collectors.toList()));
        }
        item.setItemMeta(meta);
        return item;
    }
}
