package me.iivye.plugin.briar.parser;

import me.iivye.plugin.briar.currency.Currency;
import me.iivye.plugin.briar.currency.CurrencyRegistry;
import me.iivye.plugin.briar.shop.item.ShopItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class ShopItemParser {
    public static ShopItem parse(Logger logger, CurrencyRegistry registry, ConfigurationSection config) {
        final ItemStack icon = IconParser.parse(config.getConfigurationSection("icon"));
        final List<String> commands = config.isList("command") ? config.getStringList("command") : Collections.singletonList(config.getString("command"));
        if (config.contains("multiple_purchase") && !config.contains("purchase_limit")) {
            logger.warning("Warning for ShopItem: " + config.getName());
            logger.warning("This ShopItem needs to be updated! Please do not use 'multiple_purchase' anymore. Instead, use 'purchase_limit' and set an integer value. 0 will allow infinite purchases, while any number greater than 0 will add a limit.");
            logger.warning("This item will continue to work, but will be available for unlimited purchase.");
        }
        final int purchaseLimit = config.getInt("purchase_limit", 0);
        final double amount = config.getDouble("price.amount");
        final double rarity = config.getDouble("rarity");
        final Currency currency = registry.get(config.getString("price.currency"));

        if (config.getName().contains(",")) {
            throw new RuntimeException("Item config '" + config.getName() + "' id/name CANNOT contain ',' characters.");
        }

        if (currency == null || !currency.canLoad()) {
            throw new RuntimeException("Failed to find a valid currency adapter for: " + config.getString("price.currency"));
        }

        return new ShopItem(config.getName(), icon, commands, currency, amount, rarity, purchaseLimit <= 0 ? Integer.MAX_VALUE : purchaseLimit);
    }
}
