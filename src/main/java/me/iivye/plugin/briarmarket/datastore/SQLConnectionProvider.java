package me.iivye.plugin.briarmarket.datastore;

import java.io.Closeable;
import java.sql.Connection;
import java.util.function.Supplier;

// Code inspired by byteful (https://github.com/byteful/NightMarket/tree/master/src/main/java/me/byteful/plugin/nightmarket)

public interface SQLConnectionProvider extends Closeable, Supplier<Connection> {
    boolean isValid();
}
