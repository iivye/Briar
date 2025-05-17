package me.iivye.plugin.briar.currency;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CurrencyEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CurrencyRegistry registry;

    public CurrencyEvent(CurrencyRegistry registry) {
        this.registry = registry;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public void register(Currency currency) {
        getRegistry().register(currency);
    }

    public CurrencyRegistry getRegistry() {
        return registry;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
