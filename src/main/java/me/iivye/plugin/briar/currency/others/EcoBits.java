package me.iivye.plugin.briar.currency.others;

import com.willfp.ecobits.currencies.Currencies;
import com.willfp.ecobits.currencies.CurrencyUtils;
import me.iivye.plugin.briar.currency.Currency;
import me.iivye.plugin.briar.currency.CurrencyRegistry;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.util.UUID;

public class EcoBits implements Currency {
    private final com.willfp.ecobits.currencies.Currency adapter;

    // Constructor
    public EcoBits(com.willfp.ecobits.currencies.Currency adapter) {
        this.adapter = adapter;
    }

    // Currency Interface Implementations
    @Override
    public String getId() {
        return "ecobits:" + adapter.getId();
    }

    @Override
    public void load() {
        // No specific loading logic needed
    }

    @Override
    public boolean canLoad() {
        return adapter != null && Bukkit.getPluginManager().isPluginEnabled("EcoBits");
    }

    @Override
    public boolean canPlayerAfford(UUID player, double price) {
        return CurrencyUtils.getBalance(Bukkit.getOfflinePlayer(player), adapter).doubleValue() >= price;
    }

    @Override
    public void withdraw(UUID player, double amount) {
        CurrencyUtils.adjustBalance(Bukkit.getOfflinePlayer(player), adapter, BigDecimal.valueOf(-amount));
    }

    @Override
    public void deposit(UUID player, double amount) {

    }

    @Override
    public String getName(double amount) {
        return amount + " " + adapter.getName();
    }

    // Inner class for handling registration
    public static class EcoBitsCurrencyHandler {
        private final CurrencyRegistry registry;

        public EcoBitsCurrencyHandler(CurrencyRegistry registry) {
            this.registry = registry;
        }

        public boolean registerAll() {
            if (!Bukkit.getPluginManager().isPluginEnabled("EcoBits")) {
                return false;
            }

            registry.getCurrencies().keySet().removeIf(id -> id.startsWith("ecobits:"));
            boolean anyRegistered = false;

            for (com.willfp.ecobits.currencies.Currency adapter : Currencies.values()) {
                registry.register(new EcoBits(adapter));
                anyRegistered = true;
            }

            return anyRegistered;
        }
    }
}

