package me.github.bigdiesel2m;

import net.runelite.api.GameObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.*;

public class PagelessThingsOverlay extends Overlay {
    private PagelessThingsPlugin pagelessThingsPlugin;

    @Override
    public Dimension render(Graphics2D graphics) {
        // get list of objects to highlight
        for (GameObject gameObject : pagelessThingsPlugin.getHighlightSet()) {
            Shape shape = gameObject.getConvexHull();
            if (shape != null) {
                OverlayUtil.renderPolygon(graphics, shape, Color.red);
            }
        }
        // for each object in list, highlight them
        return null;
    }

    public PagelessThingsOverlay(PagelessThingsPlugin pagelessThingsPlugin) {
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.DYNAMIC);
        this.pagelessThingsPlugin = pagelessThingsPlugin;
    }
}
