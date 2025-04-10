package me.github.bigdiesel2m;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("pageless-things")
public interface PagelessThingsConfig extends Config {
    String GROUP = "pagelessthings";

    @ConfigItem(
            position = 1,
            keyName = "highlightObjects",
            name = "Highlight Objects",
            description = "Configures whether to highlight objects without wiki pages."
    )
    default boolean highlightObjects() {
        return true;
    }
//	@ConfigItem(
//			position = 2,
//			keyName = "hidePlayers2D",
//			name = "Hide others 2D",
//			description = "Configures whether or not other players 2D elements are hidden."
//	)
//	default boolean hideOthers2D()
//	{
//		return true;
//	}
}
