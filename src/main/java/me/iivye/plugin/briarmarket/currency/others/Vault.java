package me.iivye.plugin.briarmarket.currency.others;

import me.iivye.plugin.briarmarket.Briar;
import me.iivye.plugin.briarmarket.currency.Currency;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.UUID;

public class Vault implements Currency {
    private final Briar plugin;
    private Economy eco;

    // Constructor
    public Vault(Briar plugin) {
        this.plugin = plugin;
    }

    // Currency Interface Implementations
    @Override
    public String getId() {
        return "vault";
    }

    @Override
    public void load() {
        this.eco = Objects.requireNonNull(
                Bukkit.getServicesManager().getRegistration(Economy.class),
                "Failed to find a valid Vault currency adapter."
        ).getProvider();
    }

    @Override
    public boolean canLoad() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault")
                && Bukkit.getServicesManager().getRegistration(Economy.class) != null;
    }

    @Override
    public boolean canPlayerAfford(UUID player, double price) {
        return eco.has(Bukkit.getOfflinePlayer(player), Math.abs(price));
    }

    @Override
    public void withdraw(UUID player, double amount) {
        eco.withdrawPlayer(Bukkit.getOfflinePlayer(player), Math.abs(amount));
    }

    @Override
    public void deposit(UUID player, double amount) {

    }

    @Override
    public String getName(double amount) {
        return amount == 1
                ? plugin.getConfig().getString("default_currencies.vault.name.singular", "Dollar")
                : plugin.getConfig().getString("default_currencies.vault.name.plural", "Dollars");
    }
}

