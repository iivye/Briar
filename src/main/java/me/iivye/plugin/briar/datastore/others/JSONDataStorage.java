package me.iivye.plugin.briar.datastore.others;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.iivye.plugin.briar.Briar;
import me.iivye.plugin.briar.datastore.DataStorage;
import me.iivye.plugin.briar.shop.player.PlayerShop;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class JSONDataStorage implements DataStorage {
    private final JsonObject data;
    private final File file;

    // Constructor
    public JSONDataStorage(Briar plugin) {
        this.file = new File(plugin.getDataFolder(), "data.json");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.data = new JsonObject();
        } else {
            try {
                String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                this.data = DataStorage.GSON.fromJson(content, JsonObject.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!this.data.has("shops")) {
            this.data.add("shops", new JsonObject());
        }
    }

    // Data Manipulation
    @Override
    public void setPlayerShop(PlayerShop shop) {
        data.getAsJsonObject("shops").add(shop.getUniqueId().toString(), DataStorage.GSON.toJsonTree(shop));
        save();
    }

    @Override
    public Optional<PlayerShop> getPlayerShop(UUID player) {
        JsonElement shop = data.getAsJsonObject("shops").get(player.toString());
        if (shop == null) {
            return Optional.empty();
        }
        return Optional.of(DataStorage.GSON.fromJson(shop, PlayerShop.class));
    }

    @Override
    public Set<PlayerShop> getAllShops() {
        Set<PlayerShop> set = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : data.getAsJsonObject("shops").entrySet()) {
            set.add(DataStorage.GSON.fromJson(entry.getValue(), PlayerShop.class));
        }
        return set;
    }

    // Validation and Cleanup
    @Override
    public boolean test() {
        return file != null && data != null && data.has("shops");
    }

    @Override
    public void close() {
        save();
    }

    // Internal
    private void save() {
        try {
            Files.write(file.toPath(), DataStorage.GSON.toJson(data).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

