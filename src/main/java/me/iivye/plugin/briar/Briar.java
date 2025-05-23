package me.iivye.plugin.briar;

import me.iivye.plugin.briar.currency.CurrencyRegistry;
import me.iivye.plugin.briar.datastore.DataStorage;
import me.iivye.plugin.briar.datastore.others.JSONDataStorage;
import me.iivye.plugin.briar.datastore.others.MongoDataStorage;
import me.iivye.plugin.briar.datastore.others.MySQLDataStorage;
import me.iivye.plugin.briar.datastore.others.SQLiteDataStorage;
import me.iivye.plugin.briar.parser.GUIParser;
import me.iivye.plugin.briar.schedule.access.AccessScheduleManager;
import me.iivye.plugin.briar.schedule.rotate.RotateScheduleManager;
import me.iivye.plugin.briar.shop.item.ShopItemRegistry;
import me.iivye.plugin.briar.shop.player.PlayerShopManager;
import me.iivye.plugin.briar.util.UpdateChecker;
import me.iivye.plugin.briar.util.dependency.IsolatedClass;
import me.iivye.plugin.briar.util.dependency.LibraryLoader;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import redempt.redlib.commandmanager.CommandParser;
import redempt.redlib.commandmanager.Messages;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

public final class Briar extends JavaPlugin {
    private static Briar instance;

    private final UpdateChecker updateChecker = new UpdateChecker(this);

    private CurrencyRegistry currencyRegistry;
    private DataStorage dataStorage;
    private PlayerShopManager playerShopManager;
    private RotateScheduleManager rotateScheduleManager;
    private AccessScheduleManager accessScheduleManager;
    private ShopItemRegistry shopItemRegistry;
    private GUIParser.ParsedGUI parsedGUI;
    private Messages messages;
    private BukkitTask updateCheckingTask;
    private ZoneId timezone;
    private Metrics metrics;

    public static Briar getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        getLogger().info("Loaded config...");
        reloadMessages();
        getLogger().info("Loaded messages...");
        reloadCurrencyManager();
        getLogger().info("Loaded currencies...");
        playerShopManager = new PlayerShopManager(this);
        getLogger().info("Loaded player shops...");
        reloadRotateSchedules();
        getLogger().info("Loaded rotate schedules...");
        reloadAccessSchedules();
        getLogger().info("Loaded access schedules...");
        reloadParsedGUI();
        getLogger().info("Loaded GUI...");
        reloadShopItems();
        getLogger().info("Loaded shop items...");
        loadDataStore();
        reloadUpdateChecker();

        if (getConfig().getBoolean("other.global_purchase_limits")) {
            getServer().getScheduler().runTaskTimer(this, () -> playerShopManager.updateGlobalPurchaseCount(), 1L, 20L);
        }

        new CommandParser(getResource("commands.rdcml")).parse().register(this, "briar", new CMDHooks(this));
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new BriarPH(this).register();
        }

        metrics = new Metrics(this, 22301);
        getLogger().info("Successfully started " + getDescription().getFullName() + "!");
    }

    void reloadUpdateChecker() {
        if (updateCheckingTask != null) {
            updateCheckingTask.cancel();
            updateCheckingTask = null;
        }

        if (getConfig().getBoolean("other.update")) {
            updateCheckingTask = Bukkit.getScheduler().runTaskTimer(
                    this,
                    updateChecker::check,
                    0L,
                    20L * TimeUnit.DAYS.toSeconds(1)
            );
        }
    }

    void loadDataStore() {
        switch (getConfig().getString("datastore.type").toLowerCase().trim().replace(" ", "_")) {
            case "mysql":
            case "mariadb":
                LibraryLoader.loadWithInject(this, "com.mysql", "mysql-connector-j", "8.4.0");
                dataStorage = new MySQLDataStorage(this);
                getLogger().info("Detected data store type: MySQL-remote");
                break;

            case "mongo":
            case "mongodb":
                LibraryLoader.loadWithInject(this, "org.mongodb", "mongo-java-driver", "3.12.14");
                dataStorage = new MongoDataStorage(this);
                getLogger().info("Detected data store type: MongoDB-remote");
                break;

            case "json":
            case "file":
                dataStorage = new JSONDataStorage(this);
                getLogger().info("Detected data store type: JSON-file");
                break;

            case "sqlite":
            case "flatfile":
                final IsolatedClass loader = LibraryLoader.load(this, "org.xerial", "sqlite-jdbc", "3.42.0.0");
                dataStorage = new SQLiteDataStorage(loader, this);
                getLogger().info("Detected data store type: SQLite-file");
                break;
        }

        if (!dataStorage.test()) {
            getLogger().info("Failed DataStore testing... Plugin shutting down.");
            dataStorage = null;
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        LibraryLoader.clearUnusedJars(this);
        getLogger().info("Loaded data store...");
    }

    @Override
    public void onDisable() {
        if (metrics != null) {
            metrics.shutdown();
        }

        if (rotateScheduleManager != null) {
            getRotateScheduleManager().getScheduler().shutdownNow();
        }
        if (dataStorage != null) {
            try {
                getDataStoreProvider().close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        instance = null;
    }

    public RotateScheduleManager getRotateScheduleManager() {
        return rotateScheduleManager;
    }

    public AccessScheduleManager getAccessScheduleManager() {
        return accessScheduleManager;
    }

    public DataStorage getDataStoreProvider() {
        return dataStorage;
    }

    public CurrencyRegistry getCurrencyRegistry() {
        return currencyRegistry;
    }

    public PlayerShopManager getPlayerShopManager() {
        return playerShopManager;
    }

    public ShopItemRegistry getShopItemRegistry() {
        return shopItemRegistry;
    }

    public GUIParser.ParsedGUI getParsedGUI() {
        return parsedGUI;
    }

    public String getMessage(Player context, String key) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && context != null) {
            return PlaceholderAPI.setPlaceholders(context, messages.get(key));
        }
        return messages.get(key);
    }

    void reloadMessages() {
        messages = Messages.load(this);
    }

    void reloadParsedGUI() {
        parsedGUI = GUIParser.parse(getConfig().getConfigurationSection("gui"));
    }

    void reloadRotateSchedules() {
        final String read = getConfig().getString("timezone");
        timezone = (read == null || read.isEmpty()) ? ZoneOffset.systemDefault() : ZoneOffset.of(read, ZoneOffset.SHORT_IDS);
        rotateScheduleManager = new RotateScheduleManager(this);
    }

    void reloadAccessSchedules() {
        accessScheduleManager = new AccessScheduleManager(this);
    }

    void reloadShopItems() {
        shopItemRegistry = new ShopItemRegistry(this);
    }

    void reloadCurrencyManager() {
        currencyRegistry = new CurrencyRegistry(this);
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    public void debug(String message) {
        if (getConfig().getBoolean("other.debug", false)) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    public ZoneId getTimezone() {
        return timezone;
    }
}

