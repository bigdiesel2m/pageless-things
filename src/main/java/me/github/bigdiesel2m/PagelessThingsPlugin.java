package me.github.bigdiesel2m;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

@Slf4j
@PluginDescriptor(
	name = "Pageless Things"
)
public class PagelessThingsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PagelessThingsConfig config;

	@Inject
	private OkHttpClient httpClient;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Pageless Things started!");
		downloadDatabase();
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Pageless Things stopped!");
	}

	void downloadDatabase()
	{
		Request myRequest = new Request.Builder()
				.get()
				.url("https://github.com/bigdiesel2m/pageless-things-scraper/blob/db/object_ids.h2.mv.db")
				.build();
		Call myCall = httpClient.newCall(myRequest);
        try (Response myResponse = myCall.execute();
			 ResponseBody myBody = myResponse.body();
			 InputStream myStream = myBody.byteStream()) {
			Files.copy(
					myStream,
					new File(RuneLite.RUNELITE_DIR, "object_ids.h2.mv.db").toPath(),
					StandardCopyOption.REPLACE_EXISTING
			);
		} catch (IOException e) {
            //TODO
        }
    }

	@Provides
	PagelessThingsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PagelessThingsConfig.class);
	}
}
