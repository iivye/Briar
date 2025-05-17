package me.iivye.plugin.briar.util.dependency;

import java.net.URL;
import java.net.URLClassLoader;

// Influenced by lucko's LuckPerms IsolatedClass.
public class IsolatedClass extends URLClassLoader {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    public IsolatedClass(URL... urls) {
        super(urls, ClassLoader.getSystemClassLoader().getParent());
    }
}
