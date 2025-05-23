package me.iivye.plugin.briar.parser;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import redempt.redlib.RedLib;

import java.util.List;
import java.util.Objects;

import static me.iivye.plugin.briar.util.Text.color;

public class IconParser {

    public static ItemStack parse(ConfigurationSection config) {
        Preconditions.checkNotNull(config, "Config for icon needs to not be null!");

        final String name = config.getString("name");
        final List<String> lore = config.getStringList("lore");
        final int amount = config.getInt("amount", 1);
        final String materialName = config.getString("material");
        final String playerHeadTexture = config.getString("player_head");
        final Integer customModelData = config.contains("custom_model_data") ? config.getInt("custom_model_data") : null;

        if (playerHeadTexture != null && !playerHeadTexture.isEmpty()) {
            return parseHead(name, lore, amount, playerHeadTexture, customModelData);
        }

        if (materialName != null) {
            final ItemStack materialItem = XMaterial.matchXMaterial(materialName)
                    .orElseThrow(() -> new RuntimeException("Failed to parse material: " + materialName))
                    .parseItem();

            Preconditions.checkNotNull(materialItem, "Failed to parse material: " + materialName);
            Preconditions.checkArgument(materialItem.getType() != Material.AIR, "Material cannot be AIR!");

            return parseMaterial(name, lore, amount, materialItem, customModelData);
        }

        throw new RuntimeException("Failed to find either material or player head for: " + config.getName());
    }

    private static ItemStack parseMaterial(String name, List<String> lore, int amount, ItemStack item, Integer customModelData) {
        final ItemMeta meta = item.getItemMeta();
        Preconditions.checkNotNull(meta, "Failed to load item's meta.");
        applyMeta(name, lore, customModelData, meta);
        item.setItemMeta(meta);
        item.setAmount(amount);
        return item;
    }

    private static ItemStack parseHead(String name, List<String> lore, int amount, String texture, Integer customModelData) {
        final ItemStack item = Objects.requireNonNull(XMaterial.PLAYER_HEAD.parseItem(), "Failed to parse player head item.");
        final ItemMeta meta = item.getItemMeta();
        Preconditions.checkNotNull(meta, "Failed to load item's meta.");
        Preconditions.checkArgument(meta instanceof SkullMeta, "Item meta is not a SkullMeta!");

        SkullMeta skullMeta = (SkullMeta) meta;
        skullMeta = (SkullMeta) XSkull.of(skullMeta).profile(Profileable.detect(texture)).apply();

        applyMeta(name, lore, customModelData, skullMeta);
        item.setItemMeta(skullMeta);
        item.setAmount(amount);

        return item;
    }

    private static void applyMeta(String name, List<String> lore, Integer customModelData, ItemMeta meta) {
        meta.setDisplayName(color(name));
        meta.setLore(lore == null || lore.isEmpty() ? null : color(lore));

        if (customModelData != null && RedLib.MID_VERSION >= 14) {
            meta.setCustomModelData(customModelData);
        }
    }
}

