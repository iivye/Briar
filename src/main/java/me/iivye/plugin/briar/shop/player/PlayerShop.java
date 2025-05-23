package me.iivye.plugin.briar.shop.player;

import me.iivye.plugin.briar.Briar;
import me.iivye.plugin.briar.shop.item.ShopItem;
import me.iivye.plugin.briar.shop.item.ShopItemRegistry;
import me.iivye.plugin.briar.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import redempt.redlib.misc.WeightedRandom;

import java.util.*;

public class PlayerShop {
  private final UUID uniqueId;
  private final Map<String, Integer> purchasedShopItems;
  private List<String> shopItems;

  public PlayerShop(ShopItemRegistry itemRegistry, UUID uniqueId) {
    this.uniqueId = uniqueId;
    this.shopItems = generateRandomShop(itemRegistry);
    this.purchasedShopItems = new HashMap<>();
  }

  public PlayerShop(UUID uniqueId, List<String> purchasedShopItems, List<String> shopItems) {
    this.uniqueId = uniqueId;
    this.purchasedShopItems = deserializePurchased(purchasedShopItems);
    this.shopItems = shopItems;
  }

  private static Map<String, Integer> deserializePurchased(List<String> list) {
    final Map<String, Integer> map = new HashMap<>();
    for (String data : list) {
      if (!data.contains(":")) {
        // Handle old data structure gracefully
        map.put(data, 1);
        continue;
      }
      String[] split = data.split(":");
      map.put(split[0], Integer.parseInt(split[1]));
    }
    return map;
  }

  public UUID getUniqueId() {
    return uniqueId;
  }

  public List<String> getShopItems() {
    return shopItems;
  }

  public void setShopItems(List<String> shopItems) {
    this.shopItems = shopItems;
  }

  public Map<String, Integer> getPurchasedShopItems() {
    return purchasedShopItems;
  }

  public List<String> getSerializedPurchasedShopItems() {
    final List<String> list = new ArrayList<>();
    purchasedShopItems.forEach((id, amt) -> list.add(id + ":" + amt));
    return list;
  }

  public int getPurchaseCount(String item) {
    return purchasedShopItems.getOrDefault(item, 0);
  }

  public void purchaseItem(ShopItem item) {
    purchasedShopItems.merge(item.getId(), 1, Integer::sum);
    Briar.getInstance().getPlayerShopManager().getGlobalPurchaseCount().merge(item.getId(), 1, Integer::sum);

    OfflinePlayer player = Bukkit.getOfflinePlayer(uniqueId);
    for (String cmd : item.getCommands()) {
      cmd = cmd.startsWith("/") ? cmd.substring(1) : cmd;
      cmd = cmd.replace("{player}", Objects.requireNonNull(player.getName()));
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Text.format(player, cmd));
    }

    item.getCurrency().withdraw(uniqueId, item.getAmount());
    Briar.getInstance().debug("Player purchased item: " + item.getId() + " (" + uniqueId + ")");
  }

  public void rotate(ShopItemRegistry registry) {
    Briar.getInstance().debug("Rotating player shop: " + uniqueId);
    Briar.getInstance().debug("Old shop: " + String.join(",", shopItems));
    shopItems = generateRandomShop(registry);
    Briar.getInstance().debug("New shop: " + String.join(",", shopItems));
    purchasedShopItems.clear();
  }

  private List<String> generateRandomShop(ShopItemRegistry registry) {
    Briar.getInstance().debug("Creating a new, random shop for: " + uniqueId);

    final int maxItems = registry.getMaxItems();
    if (registry.getAll().size() < maxItems) {
      throw new RuntimeException("Not enough items to generate shops! Add more items than slots in the GUI.");
    }

    final WeightedRandom<ShopItem> random = WeightedRandom.fromCollection(registry.getAll(), x -> x, ShopItem::getRarity);

    final List<String> generated = new ArrayList<>();
    for (int i = 0; i < maxItems; i++) {
      ShopItem item = random.roll();
      random.remove(item);
      generated.add(item.getId());
    }

    Briar.getInstance().debug("Created: " + String.join(",", generated));
    return generated;
  }
}

