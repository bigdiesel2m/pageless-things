package me.github.bigdiesel2m;

import com.google.common.collect.Sets;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
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
    private static final String DB_URL = "https://github.com/bigdiesel2m/pageless-things-scraper/raw/refs/heads/db/page_ids.h2.mv.db";
    private static final Path DB_PATH = new File(RuneLite.RUNELITE_DIR, "page_ids.h2.mv.db").toPath();

    private H2Manager h2Manager;
    private PagelessThingsOverlay pagelessThingsOverlay;
    @Getter
    private Set<TileObject> objectHighlightSet = Sets.newIdentityHashSet();
    @Getter
    private Set<NPC> npcHighlightSet = Sets.newIdentityHashSet();

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
        pagelessThingsOverlay = new PagelessThingsOverlay(this, config, client);
        overlayManager.add(pagelessThingsOverlay);
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Pageless Things stopped!");
        objectHighlightSet.clear();
        npcHighlightSet.clear();
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

    // Set of four object onSpawned subscriptions, all feeding into onTileObjectSpawned
    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned) {
        onTileObjectSpawned(gameObjectSpawned.getGameObject());
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned) {
        onTileObjectSpawned(groundObjectSpawned.getGroundObject());
    }

    @Subscribe
    public void onDecorativeObjectSpawned(DecorativeObjectSpawned decorativeObjectSpawned) {
        onTileObjectSpawned(decorativeObjectSpawned.getDecorativeObject());
    }

    @Subscribe
    public void onWallObjectSpawned(WallObjectSpawned wallObjectSpawned) {
        onTileObjectSpawned(wallObjectSpawned.getWallObject());
    }

    private void onTileObjectSpawned(TileObject tileObject) {
        if (h2Manager == null) {
            return;
        }

        ObjectComposition comp = client.getObjectDefinition(tileObject.getId());
        String name = comp == null ? null : comp.getName();

        if (h2Manager.objectNeedsPage(tileObject.getId()) && name != null && !name.equals("null")) {
            objectHighlightSet.add(tileObject);
            // TODO something with multilocs?
        }
    }

    // Set of four object onDespawned subscriptions, all feeding into onTileObjectDespawned
    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned) {
        onTileObjectDespawned(gameObjectDespawned.getGameObject());
    }

    @Subscribe
    public void onGroundObjectDespawned(GroundObjectDespawned groundObjectDespawned) {
        onTileObjectDespawned(groundObjectDespawned.getGroundObject());
    }

    @Subscribe
    public void onDecorativeObjectDespawned(DecorativeObjectDespawned decorativeObjectDespawned) {
        onTileObjectDespawned(decorativeObjectDespawned.getDecorativeObject());
    }

    @Subscribe
    public void onWallObjectDespawned(WallObjectDespawned wallObjectDespawned) {
        onTileObjectDespawned(wallObjectDespawned.getWallObject());
    }

    private void onTileObjectDespawned(TileObject tileObject) {
        objectHighlightSet.remove(tileObject);
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        if (h2Manager == null) {
            return;
        }

        NPC npc = npcSpawned.getNpc();
        NPCComposition def = client.getNpcDefinition(npc.getId());
        String name = def == null ? null : def.getName();

        if (h2Manager.npcNeedsPage(npc.getId()) && name != null && !name.equals("null")) {
            npcHighlightSet.add(npc);
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        npcHighlightSet.remove(npc);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOADING) {
            objectHighlightSet.clear();
            npcHighlightSet.clear();
        }
    }

    @Provides
    PagelessThingsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PagelessThingsConfig.class);
    }
}
