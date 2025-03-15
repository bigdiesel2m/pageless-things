package me.github.bigdiesel2m;

import com.google.common.collect.Sets;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import okhttp3.*;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Slf4j
@PluginDescriptor(name = "Pageless Things")
public class PagelessThingsPlugin extends Plugin {
    private static final String DB_URL = "https://github.com/bigdiesel2m/pageless-things-scraper/blob/db/object_ids.h2.mv.db";
    private static final Path DB_PATH = new File(RuneLite.RUNELITE_DIR, "object_ids.h2.mv.db").toPath();

    private H2Manager h2Manager;
    private PagelessThingsOverlay pagelessThingsOverlay;
    @Getter
    private Set<GameObject> highlightSet = Sets.newIdentityHashSet();

    @Inject
    private Client client;

    @Inject
    private PagelessThingsConfig config;

    @Inject
    private OkHttpClient httpClient;

    @Inject
    private OverlayManager overlayManager;

    @Override
    protected void startUp() throws Exception {
        log.info("Pageless Things started!");
        downloadDatabase();
        pagelessThingsOverlay = new PagelessThingsOverlay(this);
        overlayManager.add(pagelessThingsOverlay);
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Pageless Things stopped!");
        highlightSet.clear();
        overlayManager.remove(pagelessThingsOverlay);
    }

    void downloadDatabase() {
        Request dbGet = new Request.Builder().get().url(DB_URL).build();
        httpClient.newCall(dbGet).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //TODO
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body(); InputStream dbByteStream = responseBody.byteStream()) {
                    Files.copy(dbByteStream, DB_PATH, StandardCopyOption.REPLACE_EXISTING);
                    h2Manager = new H2Manager(DB_PATH);
                }
            }
        });
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned) {
        GameObject gameObject = gameObjectSpawned.getGameObject();
        if (!h2Manager.hasPage(gameObject.getId())) {
            highlightSet.add(gameObject);
            // TODO implement non-null checking
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned) {
        GameObject gameObject = gameObjectDespawned.getGameObject();
        highlightSet.remove(gameObject);
    }

    @Provides
    PagelessThingsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PagelessThingsConfig.class);
    }
}
