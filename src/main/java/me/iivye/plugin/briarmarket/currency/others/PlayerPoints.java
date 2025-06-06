package me.iivye.plugin.briarmarket.currency.others;

import me.iivye.plugin.briarmarket.Briar;
import me.iivye.plugin.briarmarket.currency.Currency;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;

import java.util.UUID;

public class PlayerPoints implements Currency {
    private final Briar plugin;
    private PlayerPointsAPI eco;

    // Constructor
    public PlayerPoints(Briar plugin) {
        this.plugin = plugin;
    }

    // Currency Interface Implementations
    @Override
    public String getId() {
        return "playerpoints";
    }

    @Override
    public void load() {
        this.eco = org.black_ixx.playerpoints.PlayerPoints.getInstance().getAPI();
    }

    @Override
    public boolean canLoad() {
        return Bukkit.getPluginManager().isPluginEnabled("PlayerPoints");
    }

    @Override
    public boolean canPlayerAfford(UUID player, double price) {
        return eco.look(player) >= price;
    }

    @Override
    public void withdraw(UUID player, double amount) {
        eco.take(player, (int) amount);
    }

    @Override
    public void deposit(UUID player, double amount) {

    }

    @Override
    public String getName(double amount) {
        return amount == 1
                ? plugin.getConfig().getString("default_currencies.playerpoints.name.singular", "Token")
                : plugin.getConfig().getString("default_currencies.playerpoints.name.plural", "Tokens");
    }
}

