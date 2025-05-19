package me.iivye.plugin.briar.shop.item;

import me.iivye.plugin.briar.Briar;
import me.iivye.plugin.briar.parser.ShopItemParser;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShopItemRegistry {
    private final Map<String, ShopItem> items = new HashMap<>();
    private final Briar plugin;

    public ShopItemRegistry(Briar plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.getConfig().getConfigurationSection("items").getValues(false).forEach((id, data) -> {
            ShopItem parsed;
            try {
                parsed = ShopItemParser.parse(plugin.getLogger(), plugin.getCurrencyRegistry(), (ConfigurationSection) data);
            } catch (Exception e) {
                plugin.getLogger().warning("Skipped loading ShopItem: " + id + " (" + e.getMessage() + ")");
                return;
            }

            register(parsed);
            plugin.getLogger().info("Registered item: " + id);
        });
    }

    public ShopItem get(String id) {
        return items.get(id);
    }

    public Collection<ShopItem> getAll() {
        return items.values();
    }

    public void register(ShopItem item) {
        items.put(item.getId(), item);
    }

    public int getMaxItems() {
        return plugin.getParsedGUI().getItemSlots().size();
    }

}
