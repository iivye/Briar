package me.iivye.plugin.briarmarket;

import me.iivye.plugin.briarmarket.shop.player.PlayerShop;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redempt.redlib.commandmanager.CommandHook;
import java.io.IOException;

public class CMDHooks {
    private final Briar plugin;

    public CMDHooks(Briar plugin) {
        this.plugin = plugin;
    }

    @CommandHook("open")
    public void onOpen(Player sender) {
        if (!sender.hasPermission("briarmarket.use")) {
            sender.sendMessage(plugin.getMessage(sender, "no_permission"));
            return;
        }

        if (!plugin.getAccessScheduleManager().isShopOpen()) {
            sender.sendMessage(plugin.getMessage(sender, "shop_not_open"));
            return;
        }

        if (!plugin.getCurrencyRegistry().isLoaded()) {
            sender.sendMessage(plugin.getMessage(sender, "shop_loading"));
            return;
        }

        final PlayerShop shop = plugin.getPlayerShopManager().get(sender.getUniqueId());
        plugin.getParsedGUI().build(shop, plugin).open(sender);
    }

    @CommandHook("reload")
    public void onReload(CommandSender sender) {
        if (!sender.hasPermission("briarmarket.admin")) {
            sender.sendMessage(plugin.getMessage(null, "no_permission"));
            return;
        }

        plugin.getParsedGUI().close();
        plugin.getRotateScheduleManager().getScheduler().shutdownNow();

        try {
            plugin.getDataStoreProvider().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        plugin.reloadConfig();
        plugin.reloadMessages();
        plugin.reloadCurrencyManager();
        plugin.reloadRotateSchedules();
        plugin.reloadAccessSchedules();
        plugin.reloadParsedGUI();
        plugin.reloadShopItems();
        plugin.loadDataStore();
        plugin.reloadUpdateChecker();

        sender.sendMessage(plugin.getMessage(null, "reload_success"));
    }

    @CommandHook("rotate")
    public void onRotate(CommandSender sender, Player player) {
        if (!sender.hasPermission("briarmarket.admin")) {
            sender.sendMessage(plugin.getMessage(null, "no_permission"));
            return;
        }

        plugin.getPlayerShopManager().get(player.getUniqueId()).rotate(plugin.getShopItemRegistry());
        sender.sendMessage(plugin.getMessage(null, "rotated").replace("{player}", player.getName()));
    }

    @CommandHook("globalrotate")
    public void onGlobalRotate(CommandSender sender) {
        if (!sender.hasPermission("briarmarket.admin")) {
            sender.sendMessage(plugin.getMessage(null, "no_permission"));
            return;
        }

        plugin.getPlayerShopManager().rotateShops();
        sender.sendMessage(plugin.getMessage(null, "rotated_all"));
    }
}
