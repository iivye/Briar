package me.iivye.plugin.briarmarket.currency;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CurrencyEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CurrencyRegistry registry;

    // Constructor
    public CurrencyEvent(CurrencyRegistry registry) {
        this.registry = registry;
    }

    // Accessors
    public CurrencyRegistry getRegistry() {
        return registry;
    }

    public void register(Currency currency) {
        registry.register(currency);
    }

    // Event handling
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

