package me.iivye.plugin.briar.shop.player;

import me.iivye.plugin.briar.Briar;
import me.iivye.plugin.briar.shop.item.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerShopManager implements Listener {
    private final Map<UUID, PlayerShop> loadedShops = new ConcurrentHashMap<>();
    private final Map<String, Integer> globalPurchaseCount = new ConcurrentHashMap<>();
    private final Briar plugin;

    public PlayerShopManager(Briar plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Rotates all player shops asynchronously, then updates the global purchase counts.
     */
    public void rotateShops() {
        plugin.debug("Rotating all shops.");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (PlayerShop shop : plugin.getDataStoreProvider().getAllShops()) {
                shop.rotate(plugin.getShopItemRegistry());
                plugin.getDataStoreProvider().setPlayerShop(shop);

                // Cache loaded shops for online players only
                if (Bukkit.getPlayer(shop.getUniqueId()) != null) {
                    loadedShops.put(shop.getUniqueId(), shop);
                }
            }
            updateGlobalPurchaseCount();
        });
    }

    /**
     * Updates global purchase counts asynchronously.
     * Only runs if "other.global_purchase_limits" config is enabled.
     */
    public void updateGlobalPurchaseCount() {
        if (!plugin.getConfig().getBoolean("other.global_purchase_limits")) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final Map<String, Integer> copy = new HashMap<>();
            final Set<PlayerShop> shops = plugin.getDataStoreProvider().getAllShops();

            for (ShopItem item : plugin.getShopItemRegistry().getAll()) {
                int totalPurchases = shops.stream()
                        .mapToInt(shop -> shop.getPurchaseCount(item.getId()))
                        .sum();
                copy.put(item.getId(), totalPurchases);
            }

            // Update the concurrent map on the main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                globalPurchaseCount.clear();
                globalPurchaseCount.putAll(copy);
            });
        });
    }

    /**
     * Retrieves the PlayerShop for the given UUID.
     * If not loaded, creates and loads a new shop asynchronously.
     */
    public PlayerShop get(UUID uuid) {
        return loadedShops.computeIfAbsent(uuid, id -> {
            PlayerShop created = new PlayerShop(plugin.getShopItemRegistry(), id);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                    plugin.getDataStoreProvider().setPlayerShop(created)
            );
            return created;
        });
    }

    public int getGlobalPurchaseCount(ShopItem item) {
        return globalPurchaseCount.getOrDefault(item.getId(), 0);
    }

    public Map<String, Integer> getGlobalPurchaseCount() {
        return globalPurchaseCount;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> load(uuid));
    }

    /**
     * Loads a player's shop data into the loadedShops map.
     */
    public void load(UUID uuid) {
        plugin.debug("Loaded data: " + uuid);
        PlayerShop shop = plugin.getDataStoreProvider()
                .getPlayerShop(uuid)
                .orElseGet(() -> new PlayerShop(plugin.getShopItemRegistry(), uuid));
        loadedShops.put(uuid, shop);
        plugin.debug("Loaded shops count: " + loadedShops.size());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerShop shop = loadedShops.remove(uuid);
            if (shop == null) {
                shop = new PlayerShop(plugin.getShopItemRegistry(), uuid);
            }
            plugin.getDataStoreProvider().setPlayerShop(shop);
        });
    }
}

