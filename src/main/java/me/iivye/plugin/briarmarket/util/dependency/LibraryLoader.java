package me.iivye.plugin.briarmarket.util.dependency;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import me.iivye.plugin.briarmarket.Briar;
import redempt.redlib.RedLib;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;


public final class LibraryLoader {
    private static final Supplier<URLClass> URL_INJECTOR = Suppliers.memoize(() ->
            URLClass.create((URLClassLoader) Briar.class.getClassLoader())
    );
    private static final List<String> USED_NAMES = Collections.synchronizedList(new ArrayList<>());

    public static IsolatedClass load(Briar plugin, String groupId, String artifactId, String version) {
        return load(plugin, groupId, artifactId, version, "https://repo1.maven.org/maven2");
    }

    public static IsolatedClass load(Briar plugin, String groupId, String artifactId, String version, String repoUrl) {
        return load(plugin, new Dependency(groupId, artifactId, version, repoUrl));
    }

    public static IsolatedClass load(Briar plugin, Dependency dependency) {
        if (RedLib.MID_VERSION >= 17) {
            // Spigot 1.17+ supports library loading natively
            return null;
        }

        final String name = dependency.getArtifactId() + "-" + dependency.getVersion();
        final File saveLocation = new File(getLibFolder(plugin), name + ".jar");

        download(plugin, dependency, saveLocation, name);

        try {
            IsolatedClass loader = new IsolatedClass(saveLocation.toURI().toURL());
            plugin.getLogger().info("Loaded dependency '" + name + "' successfully.");
            USED_NAMES.add(saveLocation.getName());
            return loader;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to load dependency: " + saveLocation, e);
        }
    }

    public static void loadWithInject(Briar plugin, String groupId, String artifactId, String version) {
        loadWithInject(plugin, groupId, artifactId, version, "https://repo1.maven.org/maven2");
    }

    public static void loadWithInject(Briar plugin, String groupId, String artifactId, String version, String repoUrl) {
        loadWithInject(plugin, new Dependency(groupId, artifactId, version, repoUrl));
    }

    public static void loadWithInject(Briar plugin, Dependency dependency) {
        if (RedLib.MID_VERSION >= 17) {
            return;
        }

        final String name = dependency.getArtifactId() + "-" + dependency.getVersion();
        final File saveLocation = new File(getLibFolder(plugin), name + ".jar");

        download(plugin, dependency, saveLocation, name);

        try {
            URL_INJECTOR.get().addURL(saveLocation.toURI().toURL());
            plugin.getLogger().info("Injected dependency '" + name + "' successfully.");
            USED_NAMES.add(saveLocation.getName());
        } catch (Exception e) {
            throw new RuntimeException("Unable to inject dependency: " + saveLocation, e);
        }
    }

    private static void download(Briar plugin, Dependency dependency, File saveLocation, String name) {
        plugin.getLogger().info(String.format("Loading dependency '%s:%s:%s'...", dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion()));

        if (!saveLocation.exists()) {
            try {
                plugin.getLogger().info("Dependency '" + name + "' not found locally. Attempting download...");
                final URL url = dependency.getUrl();

                try (InputStream is = url.openStream()) {
                    Files.copy(is, saveLocation.toPath());
                }

                plugin.getLogger().info("Dependency '" + name + "' successfully downloaded.");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to download dependency '" + name + "': " + e.getMessage());
                throw new RuntimeException("Unable to download dependency: " + dependency, e);
            }
        }

        if (!saveLocation.exists()) {
            throw new RuntimeException("Unable to download dependency: " + dependency);
        }
    }

    private static File getLibFolder(Briar plugin) {
        final File folder = new File(plugin.getDataFolder(), "libraries");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Could not create libraries folder: " + folder);
        }
        return folder;
    }

    /**
     * Deletes all .jar files in the libraries folder that are not in the USED_NAMES list.
     */
    public static void clearUnusedJars(Briar plugin) {
        final File folder = getLibFolder(plugin);
        File[] files = folder.listFiles();

        if (files == null) return;

        for (File file : files) {
            if (!file.getName().endsWith(".jar")) continue;

            if (!USED_NAMES.contains(file.getName())) {
                boolean deleted = file.delete();
                if (deleted) {
                    plugin.getLogger().info("Deleted unused library: " + file.getName());
                } else {
                    plugin.getLogger().warning("Failed to delete unused library: " + file.getName());
                }
            }
        }
    }

    public static final class Dependency {
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String repoUrl;

        public Dependency(String groupId, String artifactId, String version, String repoUrl) {
            this.groupId = Objects.requireNonNull(groupId, "groupId");
            this.artifactId = Objects.requireNonNull(artifactId, "artifactId");
            this.version = Objects.requireNonNull(version, "version");
            this.repoUrl = Objects.requireNonNull(repoUrl, "repoUrl");
        }

        public String getGroupId() { return groupId; }
        public String getArtifactId() { return artifactId; }
        public String getVersion() { return version; }
        public String getRepoUrl() { return repoUrl; }

        public URL getUrl() throws MalformedURLException {
            String base = repoUrl.endsWith("/") ? repoUrl : repoUrl + "/";
            String path = String.format("%s/%s/%s/%s-%s.jar",
                    groupId.replace('.', '/'), artifactId, version, artifactId, version);
            return new URL(base + path);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Dependency)) return false;
            Dependency other = (Dependency) o;
            return groupId.equals(other.groupId)
                    && artifactId.equals(other.artifactId)
                    && version.equals(other.version)
                    && repoUrl.equals(other.repoUrl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, artifactId, version, repoUrl);
        }

        @Override
        public String toString() {
            return String.format("Dependency(groupId=%s, artifactId=%s, version=%s, repoUrl=%s)",
                    groupId, artifactId, version, repoUrl);
        }
    }
}

