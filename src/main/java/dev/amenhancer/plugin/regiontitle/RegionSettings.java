package dev.amenhancer.plugin.regiontitle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

final class RegionSettings {
    private final File file;
    private final RegionProfile active;
    private volatile RegionProfile selected;

    RegionSettings(File dataDirectory) throws IOException {
        file = new File(dataDirectory, "region.properties");
        active = read();
        selected = active;
    }

    RegionProfile active() { return active; }
    RegionProfile selected() { return selected; }

    void select(RegionProfile profile) throws IOException {
        File parent = file.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Cannot create plugin data directory");
        }
        Properties properties = new Properties();
        properties.setProperty("region", profile.id());
        try (FileOutputStream output = new FileOutputStream(file, false);
             Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            properties.store(writer, null);
        }
        selected = profile;
    }

    private RegionProfile read() throws IOException {
        if (!file.isFile()) return RegionProfile.HONG_KONG;
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        }
        return RegionProfile.byId(properties.getProperty("region"));
    }
}
