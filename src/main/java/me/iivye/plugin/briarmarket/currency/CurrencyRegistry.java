package me.iivye.plugin.briarmarket.currency;

import me.iivye.plugin.briarmarket.Briar;
import me.iivye.plugin.briarmarket.currency.others.EcoBits;
import me.iivye.plugin.briarmarket.currency.others.PlayerPoints;
import me.iivye.plugin.briarmarket.currency.others.Vault;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class CurrencyRegistry {
    private final Map<String, Currency> currencies = new HashMap<>();
    private final Briar plugin;
    private boolean isLoaded = false;

    // Constructor
    public CurrencyRegistry(Briar plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskLater(plugin, this::load, 20L);
    }

    // Initialization
    private void load() {
        plugin.getLogger().info("Loading BriarMarket currency adapters...");

        // Allow external plugins to register their currencies
        Bukkit.getPluginManager().callEvent(new CurrencyEvent(this));

        // Register internal currencies
        register(new Vault(plugin));
        register(new PlayerPoints(plugin));

        // Register all EcoBits currencies
        if (!new EcoBits.EcoBitsCurrencyHandler(this).registerAll()) {
            plugin.getLogger().warning("Skipped loading currency adapter: ecobits");
        }

        isLoaded = true;
        plugin.getLogger().info("Done loading currencies!");

        // Load dependent systems
        plugin.getShopItemRegistry().load();
        plugin.getRotateScheduleManager().load();
        Bukkit.getOnlinePlayers().forEach(p -> plugin.getPlayerShopManager().load(p.getUniqueId()));
    }

    // Currency Management
    public void register(Currency currency) {
        if (currency.canLoad()) {
            currency.load();
            plugin.getLogger().info("Registered currency adapter: " + currency.getId());
        } else {
            plugin.getLogger().warning("Skipped loading currency adapter: " + currency.getId());
        }
        currencies.put(currency.getId().toLowerCase(), currency);
    }

    public void unregister(Currency currency) {
        unregister(currency.getId());
    }

    public void unregister(String id) {
        currencies.remove(id.toLowerCase());
    }

    public Currency get(String id) {
        return currencies.get(id.toLowerCase());
    }

    // Accessors
    public Map<String, Currency> getCurrencies() {
        return currencies;
    }

    public boolean isLoaded() {
        return isLoaded;
    }
}

