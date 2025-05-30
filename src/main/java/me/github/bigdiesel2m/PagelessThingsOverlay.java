package me.github.bigdiesel2m;

import net.runelite.api.*;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.*;

public class PagelessThingsOverlay extends Overlay {
    private PagelessThingsPlugin pagelessThingsPlugin;
    private PagelessThingsConfig pagelessThingsConfig;
    private Client client;

    @Override
    public Dimension render(Graphics2D graphics) {
        // get list of objects to highlight
        if (pagelessThingsConfig.highlightObjects()) {
            Shape shape = null;
            for (TileObject tileObject : pagelessThingsPlugin.getObjectHighlightSet()) {
                if (tileObject instanceof GameObject) {
                    shape = ((GameObject) tileObject).getConvexHull();
                } else if (tileObject instanceof GroundObject) {
                    shape = ((GroundObject) tileObject).getConvexHull();
                } else if (tileObject instanceof DecorativeObject) {
                    shape = ((DecorativeObject) tileObject).getConvexHull();
                } else if (tileObject instanceof WallObject) {
                    shape = ((WallObject) tileObject).getConvexHull();
                }

                if (shape != null && client.getLocalPlayer().getWorldView().getPlane() == tileObject.getPlane()) {
                    OverlayUtil.renderPolygon(graphics, shape, Color.red);
                }
            }
        }
        if (pagelessThingsConfig.highlightNPCs()) {
            for (NPC npc : pagelessThingsPlugin.getNpcHighlightSet()) {
                Shape shape = npc.getConvexHull();
                if (shape != null) {
                    OverlayUtil.renderPolygon(graphics, shape, Color.red);
                }
            }
        }
        return null;
    }

    public PagelessThingsOverlay(PagelessThingsPlugin pagelessThingsPlugin, PagelessThingsConfig pagelessThingsConfig, Client client) {
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.DYNAMIC);
        this.pagelessThingsPlugin = pagelessThingsPlugin;
        this.pagelessThingsConfig = pagelessThingsConfig;
        this.client = client;
    }
}
