/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package reika.reactorcraft.auxiliary;

import java.util.Set;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.items.ItemIronFinder;
import reika.reactorcraft.registry.ReactorItems;

/**
 * Magnetic Ore Finder HUD: while the finder is held, nearby magnetic ores (range 8) are marked on
 * screen -- a translucent box with the ore's icon when the ore is in view, or an edge arrow +
 * icon pointing left/right when it is behind/off-screen. 26.2 port of the legacy
 * RenderGameOverlayEvent hook (now a {@link GuiLayer}); the screen-projection trig (compass angle
 * phi / elevation theta against head yaw+pitch, fov-scaled) is copied from the original. The
 * Mimicry-mod overlay-icon special case is dropped (mod interop not ported); icons come from the
 * block's item sprite via {@code renderItem} instead of terrain-atlas IIcons.
 */
@EventBusSubscriber(modid = ReactorCraft.MODID, value = Dist.CLIENT)
public final class IronFinderOverlay implements GuiLayer {

    public static final IronFinderOverlay instance = new IronFinderOverlay();
    private static final Identifier ID = Identifier.fromNamespaceAndPath(ReactorCraft.MODID, "iron_finder");

    private static final int FILL = 0x60ffffff;   // legacy alpha-96 white
    private static final int FILL_FAINT = 0x20ffffff; // legacy alpha-32 white
    private static final int OUTLINE = 0xffffffff;

    private IronFinderOverlay() {}

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, ID, instance);
    }

    @Override
    public void render(GuiGraphicsExtractor gui, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player ep = mc.player;
        if (ep == null || mc.level == null)
            return;
        if (!ep.getMainHandItem().is(ReactorItems.IRON_FINDER.get()))
            return;

        int h = gui.guiHeight() / 2;
        float yaw = ep.getYHeadRot() % 360;
        float pitch = ep.getXRot() + 90;
        if (yaw < 0)
            yaw += 360;
        int fov = mc.options.fov().get();
        double dfv = fov / 70F;

        Set<BlockPos> map = ItemIronFinder.getOreNearby(ep, 8);
        for (BlockPos c : map) {
            double dx = c.getX() + 0.5 - ep.getX();
            double dy = c.getY() + 0.5 - ep.getY();
            double dz = c.getZ() + 0.5 - ep.getZ();

            Block b = ep.level().getBlockState(c).getBlock();
            if (b == Blocks.AIR)
                continue;
            ItemStack icon = new ItemStack(b);

            double dl = Math.sqrt(dx * dx + dz * dz);
            double arel = -Math.toDegrees(Math.atan2(dx, dz));
            double prel = 90 - Math.toDegrees(Math.atan2(dy, dl));
            if (arel < 0)
                arel += 360;
            double phi = arel - yaw;
            double theta = prel - pitch;
            if (phi < 0)
                phi += 360;
            int cy = h + (int) (h * 2 * Math.sin(Math.toRadians(theta))) + (int) (dfv * 20);

            if (phi >= 180 && 360 - fov > phi) {
                // Behind-left: left-pointing edge arrow + ore icon.
                int cx = 10;
                arrow(gui, cx, cy, true);
                if (!icon.isEmpty())
                    gui.item(icon, cx + 10, cy - 8);
            }
            else if (phi < 180 && phi > fov) {
                // Behind-right: right-pointing edge arrow + ore icon.
                int cx = gui.guiWidth() - 10;
                arrow(gui, cx, cy, false);
                if (!icon.isEmpty())
                    gui.item(icon, cx - 26, cy - 8);
            }
            else {
                // In view: faint box with the ore icon at the projected screen position.
                double w = gui.guiWidth() / 2D * dfv;
                int cx = (int) (w + w * Math.sin(Math.toRadians(phi)));
                gui.fill(cx - 8, cy - 8, cx + 8, cy + 8, FILL_FAINT);
                outline(gui, cx - 8, cy - 8, cx + 8, cy + 8);
                if (!icon.isEmpty())
                    gui.item(icon, cx - 8, cy - 8);
            }
        }
    }

    /** 10px translucent triangle pointing off the screen edge ({@code left}: apex at cx pointing left). */
    private static void arrow(GuiGraphicsExtractor gui, int cx, int cy, boolean left) {
        for (int i = 0; i <= 10; i++) {
            int x = left ? cx + i : cx - i - 1;
            gui.fill(x, cy - i, x + 1, cy + i + 1, FILL);
        }
        int edge = left ? cx + 10 : cx - 11;
        gui.fill(edge, cy - 10, edge + 1, cy + 11, OUTLINE);
    }

    private static void outline(GuiGraphicsExtractor gui, int x0, int y0, int x1, int y1) {
        gui.fill(x0, y0, x1, y0 + 1, OUTLINE);
        gui.fill(x0, y1 - 1, x1, y1, OUTLINE);
        gui.fill(x0, y0, x0 + 1, y1, OUTLINE);
        gui.fill(x1 - 1, y0, x1, y1, OUTLINE);
    }

}
