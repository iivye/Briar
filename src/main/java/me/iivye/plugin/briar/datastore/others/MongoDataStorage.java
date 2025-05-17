package me.iivye.plugin.briar.datastore.others;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import me.iivye.plugin.briar.Briar;
import me.iivye.plugin.briar.datastore.DataStorage;
import me.iivye.plugin.briar.shop.player.PlayerShop;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class MongoDataStorage implements DataStorage {
    private final MongoClient client;
    private final MongoDatabase db;

    public MongoDataStorage(Briar plugin) {
        this.client = new MongoClient(new MongoClientURI(plugin.getConfig().getString("datastore.mongo.uri")));
        this.db = this.client.getDatabase(plugin.getConfig().getString("datastore.mongo.database"));
    }

    @Override
    public void setPlayerShop(PlayerShop shop) {
        final MongoCollection<Document> col = getCollection();
        final Bson eq = Filters.eq("_id", shop.getUniqueId().toString());
        final Document doc = Document.parse(DataStorage.GSON.toJson(shop));
        if (col.find(eq).cursor().hasNext()) {
            col.replaceOne(eq, doc);
        } else {
            col.insertOne(doc);
        }
    }

    @Override
    public Optional<PlayerShop> getPlayerShop(UUID player) {
        final MongoCollection<Document> col = getCollection();
        final Bson eq = Filters.eq("_id", player.toString());
        final Document doc = col.find(eq).first();
        if (doc == null) {
            return Optional.empty();
        }

        return Optional.of(DataStorage.GSON.fromJson(doc.toJson(JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build()), PlayerShop.class));
    }

    @Override
    public Set<PlayerShop> getAllShops() {
        final Set<PlayerShop> set = new HashSet<>();

        getCollection().find().forEach((Consumer<? super Document>) doc -> set.add(DataStorage.GSON.fromJson(doc.toJson(JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build()), PlayerShop.class)));

        return set;
    }

    @Override
    public boolean test() {
        try {
            getCollection().find(Filters.eq("test", "test")).first();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private MongoCollection<Document> getCollection() {
        return db.getCollection("Briar");
    }

    @Override
    public void close() {
        client.close();
    }
}
