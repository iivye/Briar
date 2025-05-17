# 🌿 Briar | A Living Shop System for Minecraft

> “In the heart of the forest, where sunlight dapples through ancient leaves, a hidden market awakens at dusk...”

**Briar** is a beautifully crafted, rotating shop system for Minecraft servers inspired by the mystique of forest markets and fae bargains. Designed for immersive experiences and plugin developers alike, Briar lets you create dynamic, configurable shops powered by multiple currencies and stunning customization.

---

## 🌱 Features

- 🌀 **Rotating Items** – Like a mystical night market, your shop shifts inventory over time.
- 💱 **Multi-Currency Support** – Uses Vault, PlayerPoints, and EcoBits.
- 🍃 **Plant-Themed UI** – Designed with nature and fantasy aesthetics in mind. Rest assured, the UI can be customized to your own imagination.
- 🔧 **Fully Configurable** – YAML-based setup for full control over items, costs, and chances.
- 📆 **Per-Player Shops** – Give every player a unique shop, or keep it shared across all.
- 🛠️ **Developer Friendly** – API and custom adapter support (in progress).

---

## 📦 Requirements

- Minecraft `1.8 – 1.21`
- Java `17+`
- At least one supported currency plugin:
  - [Vault](https://www.spigotmc.org/resources/vault.34315/)
  - [PlayerPoints](https://www.spigotmc.org/resources/playerpoints.80745/)
  - [EcoBits](https://www.spigotmc.org/resources/ecobits.111168/)

---

## 📁 Installation

1. Download `Briar.jar` and place it into your `plugins/` folder.
2. Ensure you have at least one supported currency plugin installed and enabled.
3. Start your server to generate the default config.
4. Open and edit `plugins/Briar/config.yml` to customize your shop.
5. Use `/briaradmin reload` to apply changes without restarting.

---

## 🛒 Example Config Snippet

```yaml
currency_priority: ["VAULT", "PLAYERPOINTS"]

items:
  enchanted_apple:
    material: ENCHANTED_GOLDEN_APPLE
    display_name: "&6Enchanted Apple"
    lore:
      - "&fA rare fruit of forest legend."
      - "&7Restores ancient strength."
    cost: 250
    currency: VAULT
    chance: 10
```
## ⌨️ Commands

| Command         | Description                    |
| --------------- | ------------------------------ |
| `/briar`        | Opens the shop GUI             |
| `/briaradmin reload` | Reloads the configuration file |
| `/briaradmin forcerotate <player>` | Forcefully rotate provided player's market shop. |
| `briaradmin forcegobalrotate` | Forcefully rotates all shops. |
| `briaradmin debug` | Provides debug information for support (WIP). |

---

## 🧪 Configuration Tips

* `chance`: Determines the likelihood (%) that an item will appear in a shop rotation.
* `currency_priority`: Briar uses the first valid, loaded currency plugin from this list.
* `per_player`: If set to `true`, each player will see a unique shop lineup.

---

## 🐛 Troubleshooting

> **Skipped loading ShopItem...**
> This means Briar couldn’t find a working currency adapter. Ensure your specified `currency:` (e.g., `VAULT`) is in the loaded priority list and that the plugin is installed and active.

> **No shop items are showing up!**
> Double-check:

* Your `currency_priority` matches installed plugins
* Each item uses a valid currency from that list
* Config file is properly formatted (YAML-sensitive!)

---

## 🔮 Future Plans

* Custom item actions (e.g., commands, potion effects)
* GUI-based configuration
* Integration with MMOItems, ItemsAdder, Oraxen
* Developer API & custom currency adapters

---

## 🌼 Credits

* Plugin by **iivye**
* Inspired by fantasy, immersive economies, and the charm of nature-based design
* Thanks to the Minecraft plugin dev community for years of shared knowledge

---

## 🍂 Final Words

Let your players stumble upon the mysterious **Briar**, where no two visits are the same. With its rotating treasures, earthy visuals, and immersive mechanics, **Briar** helps your server economy bloom.

> 🌙 *“When the moon is high, and the fireflies glow\... the Briar opens.”*

---

## 💬 Get in Touch

Questions, ideas, or bugs?
📧 Contact coming soon
🗃️ GitHub Issues support planned
🌱 Discord server in the works!
