package me.github.bigdiesel2m;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.*;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;

@Slf4j
@PluginDescriptor(
        name = "Pageless Things"
)
public class PagelessThingsPlugin extends Plugin {
    private static final String DB_URL = "https://github.com/bigdiesel2m/pageless-things-scraper/blob/db/object_ids.h2.mv.db";
    private static final Path DB_PATH = new File(RuneLite.RUNELITE_DIR, "object_ids.h2.mv.db").toPath();
    private H2Manager h2Manager;
    @Inject
    private Client client;

    @Inject
    private PagelessThingsConfig config;

    @Inject
    private OkHttpClient httpClient;

    @Override
    protected void startUp() throws Exception {
        log.info("Pageless Things started!");
        downloadDatabase();
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Pageless Things stopped!");
    }

    void downloadDatabase() {
        Request dbGet = new Request.Builder()
                .get()
                .url(DB_URL)
                .build();
        httpClient.newCall(dbGet).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //TODO
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body();
                     InputStream dbByteStream = responseBody.byteStream()) {
                    Files.copy(
                            dbByteStream,
                            DB_PATH,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                    h2Manager = new H2Manager(DB_PATH);
                }
            }
        });
    }



    @Provides
    PagelessThingsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PagelessThingsConfig.class);
    }
}
