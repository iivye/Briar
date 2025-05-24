package me.iivye.plugin.briarmarket.util.dependency;

import java.net.URL;
import java.net.URLClassLoader;

public class IsolatedClass extends URLClassLoader {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    public IsolatedClass(URL... urls) {
        super(urls, ClassLoader.getSystemClassLoader().getParent());
    }
}
