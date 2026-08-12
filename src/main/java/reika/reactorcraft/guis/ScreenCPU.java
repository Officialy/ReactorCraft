/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.guis;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.entity.BlockEntity;
import reika.dragonapi.instantiable.gui.ImagedGuiButton;
import reika.dragonapi.instantiable.io.PacketTarget;
import reika.dragonapi.libraries.io.ReikaPacketHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.auxiliary.ReactorControlLayout;
import reika.reactorcraft.base.ReactorGuiBase;
import reika.reactorcraft.container.MenuCPU;
import reika.reactorcraft.registry.ReactorPackets;
import reika.reactorcraft.tileentities.fission.TileEntityCPU;
import reika.reactorcraft.tileentities.fission.TileEntityControlRod;

/**
 * 26.2 port of GuiCPU — the reactor control-rod management panel. Shows a top-down colour grid of the
 * control rods on the current Y layer (via {@link ReactorControlLayout#getDisplayColorAtRelativePosition});
 * click a cell to toggle that rod ({@code CPUTOGGLE} packet), the Retract/Insert All buttons send
 * {@code CPURAISE}/{@code CPULOWER}, and the side buttons scroll the displayed Y layer.
 *
 * The layout is synced to the client via TileEntityCPU.writeSyncTag (the tile inits its layout in the
 * constructor so the client has it to populate). Packets are handled by ReactorPacketCore.
 */
public class ScreenCPU extends ReactorGuiBase<TileEntityCPU, MenuCPU> {

    private static final int BUTTON_SIZE = 3;
    private static final int BUTTON_SPACE = 5;
    private static final Identifier BUTTONS = Identifier.fromNamespaceAndPath("rotarycraft", "textures/screen/buttons.png");

    private int offsetY = 0;

    public ScreenCPU(MenuCPU container, Inventory inv, Component title) {
        super(container, inv, title, 176, 210);
    }

    @Override
    protected String getGuiTexture() {
        return "control2";
    }

    @Override
    protected void init() {
        super.init();
        int j = leftPos;
        int k = topPos;
        addRenderableWidget(Button.builder(Component.literal("Retract All"),
                b -> sendCPU(ReactorPackets.CPURAISE.ordinal(), tile)).bounds(j + 8, k + 18, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Insert All"),
                b -> sendCPU(ReactorPackets.CPULOWER.ordinal(), tile)).bounds(j + 96, k + 18, 72, 20).build());
        addRenderableWidget(new ImagedGuiButton(2, j + 7, k + 84, 12, 48, 90, 60, BUTTONS, b -> {
            ReactorControlLayout l = tile.getLayout();
            if (l != null && l.getMinY() < offsetY)
                offsetY--;
        }));
        addRenderableWidget(new ImagedGuiButton(3, j + 157, k + 84, 12, 48, 90, 108, BUTTONS, b -> {
            ReactorControlLayout l = tile.getLayout();
            if (l != null && l.getMaxY() > offsetY)
                offsetY++;
        }));
    }

    private void sendCPU(int ordinal, BlockEntity target) {
        ReikaPacketHelper.sendUpdatePacket(ReactorCraft.packetChannel, ordinal, target, PacketTarget.server);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        ReactorControlLayout layout = tile.getLayout();
        Level world = tile.getLevel();
        if (layout == null || world == null)
            return;
        int r = BUTTON_SIZE;
        int s = BUTTON_SPACE;
        int ox = 1 + imageWidth / 2 - s / 2 - 1;
        int oy = imageHeight / 2 - s / 2 + 5;
        for (int a = layout.getMinX(); a <= layout.getMaxX(); a++) {
            for (int b = layout.getMinZ(); b <= layout.getMaxZ(); b++) {
                if (a != 0 || b != 0) {
                    int c = layout.getDisplayColorAtRelativePosition(world, a, offsetY, b);
                    int x = ox + a * s;
                    int y = oy + b * s;
                    graphics.fill(x, y, x + r, y + r, 0xff000000 | c);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        ReactorControlLayout layout = tile.getLayout();
        Level world = tile.getLevel();
        if (layout != null && world != null) {
            int s = BUTTON_SPACE;
            int ox = leftPos + 1 + imageWidth / 2 - s / 2 - 1;
            int oy = topPos + imageHeight / 2 - s / 2 + 5;
            int a = Mth.floor((event.x() - ox) / (double) s);
            int b = Mth.floor((event.y() - oy) / (double) s);
            TileEntityControlRod rod = layout.getControlRodAtRelativePosition(world, a, offsetY, b);
            if (rod != null) {
                sendCPU(ReactorPackets.CPUTOGGLE.ordinal(), rod);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
