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
    private final Map<UUID, PlayerShop> loadedShops = new HashMap<>();
    private final Map<String, Integer> globalPurchaseCount = new ConcurrentHashMap<>();
    private final Briar plugin;

    public PlayerShopManager(Briar plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void rotateShops() {
        plugin.debug("Rotating all shops.");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (PlayerShop shop : plugin.getDataStoreProvider().getAllShops()) {
                shop.rotate(plugin.getShopItemRegistry());
                plugin.getDataStoreProvider().setPlayerShop(shop);
                if (Bukkit.getPlayer(shop.getUniqueId()) != null) loadedShops.put(shop.getUniqueId(), shop);
            }
            updateGlobalPurchaseCount();
        });
    }

    public void updateGlobalPurchaseCount() {
        if (!plugin.getConfig().getBoolean("other.global_purchase_limits")) return;

//    plugin.debug("Updating global purchase count.");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final Map<String, Integer> copy = new HashMap<>();
            final Set<PlayerShop> shops = plugin.getDataStoreProvider().getAllShops();
            for (ShopItem item : plugin.getShopItemRegistry().getAll()) {
                final int totalPurchases = shops.stream().mapToInt(shop -> shop.getPurchaseCount(item.getId())).sum();
                copy.put(item.getId(), totalPurchases);
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                globalPurchaseCount.clear();
                globalPurchaseCount.putAll(copy);
            });
        });
    }

    public PlayerShop get(UUID uuid) {
        if (loadedShops.containsKey(uuid)) return loadedShops.get(uuid);

        final PlayerShop created = new PlayerShop(plugin.getShopItemRegistry(), uuid);
        loadedShops.put(uuid, created);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getDataStoreProvider().setPlayerShop(created));

        return created;
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

    public void load(UUID uuid) {
        plugin.debug("Loaded data: " + uuid);
        loadedShops.put(uuid, plugin.getDataStoreProvider().getPlayerShop(uuid).orElseGet(() -> new PlayerShop(plugin.getShopItemRegistry(), uuid)));
        plugin.debug("Loaded: " + loadedShops.size());
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
