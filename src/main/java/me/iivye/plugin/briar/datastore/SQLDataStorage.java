package me.iivye.plugin.briar.datastore;

import me.iivye.plugin.briar.shop.player.PlayerShop;
import me.iivye.plugin.briar.util.SQLUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class SQLDataStorage implements DataStorage {
    protected final SQLConnectionProvider connectionProvider;
    protected final String upsertClause;

    public SQLDataStorage(SQLConnectionProvider connectionProvider, String upsertClause) {
        this.connectionProvider = connectionProvider;
        this.upsertClause = upsertClause;
        createTable();
    }

    @Override
    public void setPlayerShop(PlayerShop shop) {
        final String sql = "INSERT INTO Briar (ID, Purchased, Items) VALUES (?, ?, ?) " + upsertClause + " Purchased=?, Items=?;";

        try (final Connection connection = connectionProvider.get(); final PreparedStatement statement = connection.prepareStatement(sql)) {

            final List<String> purchased = shop.getSerializedPurchasedShopItems();
            final List<String> items = shop.getShopItems();
            final String purchasedSerialized = SQLUtils.serializeList(purchased);
            final String itemsSerialized = SQLUtils.serializeList(items);

            statement.setBytes(1, SQLUtils.serializeUUID(shop.getUniqueId()));
            statement.setString(2, purchasedSerialized);
            statement.setString(3, itemsSerialized);
            statement.setString(4, purchasedSerialized);
            statement.setString(5, itemsSerialized);

            statement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Optional<PlayerShop> getPlayerShop(UUID player) {
        final String sql = "SELECT * FROM Briar WHERE ID=?;";

        try (final Connection connection = connectionProvider.get(); final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBytes(1, SQLUtils.serializeUUID(player));

            try (ResultSet set = statement.executeQuery()) {
                if (!set.next()) {
                    return Optional.empty();
                }
                final List<String> purchased = SQLUtils.deserializeList(set.getString("Purchased"));
                final List<String> items = SQLUtils.deserializeList(set.getString("Items"));

                return Optional.of(new PlayerShop(player, purchased, items));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Set<PlayerShop> getAllShops() {
        final Set<PlayerShop> set = new HashSet<>();
        final String sql = "SELECT * FROM Briar;";

        try (final Connection connection = connectionProvider.get(); final PreparedStatement statement = connection.prepareStatement(sql); final ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                final UUID uuid = SQLUtils.deserializeUUID(result.getBytes("ID"));
                final List<String> purchased = SQLUtils.deserializeList(result.getString("Purchased"));
                final List<String> items = SQLUtils.deserializeList(result.getString("Items"));

                set.add(new PlayerShop(uuid, purchased, items));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return set;
    }

    @Override
    public boolean test() {
        return connectionProvider.isValid();
    }

    private void createTable() {
        final String sql = "CREATE TABLE IF NOT EXISTS Briar (ID BINARY(16) NOT NULL, Purchased TEXT NOT NULL, Items TEXT NOT NULL, PRIMARY KEY (ID));";

        try (final Connection connection = connectionProvider.get(); final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        connectionProvider.close();
    }
}
