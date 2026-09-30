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

import reika.dragonapi.instantiable.gui.ImagedGuiButton;
import reika.dragonapi.instantiable.io.PacketTarget;
import reika.dragonapi.libraries.io.ReikaPacketHelper;
import reika.reactorcraft.ReactorCraft;
import reika.reactorcraft.base.ReactorGuiBase;
import reika.reactorcraft.container.MenuCPU;
import reika.reactorcraft.registry.ReactorPackets;
import reika.reactorcraft.tileentities.fission.TileEntityCPU;

/** Live server-supplied control-rod grid; works for local, distant and cross-dimension CPUs. */
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
                b -> sendCPU(ReactorPackets.CPURAISE.ordinal(), menu.getController().pos())).bounds(j + 8, k + 18, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Insert All"),
                b -> sendCPU(ReactorPackets.CPULOWER.ordinal(), menu.getController().pos())).bounds(j + 96, k + 18, 72, 20).build());
        addRenderableWidget(new ImagedGuiButton(2, j + 7, k + 84, 12, 48, 90, 60, BUTTONS, b -> {
            if (menu.minY() < offsetY)
                offsetY--;
        }));
        addRenderableWidget(new ImagedGuiButton(3, j + 157, k + 84, 12, 48, 90, 108, BUTTONS, b -> {
            if (menu.maxY() > offsetY)
                offsetY++;
        }));
    }

    private void sendCPU(int ordinal, net.minecraft.core.BlockPos target) {
        ReikaPacketHelper.sendUpdatePacket(ReactorCraft.packetChannel, ordinal, target.getX(), target.getY(), target.getZ(), PacketTarget.server);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        offsetY = Mth.clamp(offsetY, menu.minY(), menu.maxY());
        int ox = 1 + imageWidth / 2 - BUTTON_SPACE / 2 - 1;
        int oy = imageHeight / 2 - BUTTON_SPACE / 2 + 5;
        var cpu = menu.getController().pos();
        int minX = menu.getRods().keySet().stream().mapToInt(p -> p.getX() - cpu.getX()).min().orElse(0);
        int maxX = menu.getRods().keySet().stream().mapToInt(p -> p.getX() - cpu.getX()).max().orElse(0);
        int minZ = menu.getRods().keySet().stream().mapToInt(p -> p.getZ() - cpu.getZ()).min().orElse(0);
        int maxZ = menu.getRods().keySet().stream().mapToInt(p -> p.getZ() - cpu.getZ()).max().orElse(0);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x != 0 || z != 0) {
                    int color = menu.getRods().getOrDefault(cpu.offset(x, offsetY, z), 0x6a6a6a);
                    int px = ox + x * BUTTON_SPACE;
                    int py = oy + z * BUTTON_SPACE;
                    graphics.fill(px, py, px + BUTTON_SIZE, py + BUTTON_SIZE, 0xff000000 | color);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int ox = leftPos + 1 + imageWidth / 2 - BUTTON_SPACE / 2 - 1;
        int oy = topPos + imageHeight / 2 - BUTTON_SPACE / 2 + 5;
        int x = Mth.floor((event.x() - ox) / BUTTON_SPACE);
        int z = Mth.floor((event.y() - oy) / BUTTON_SPACE);
        var rod = menu.getController().pos().offset(x, offsetY, z);
        if (menu.getRods().containsKey(rod)) {
            sendCPU(ReactorPackets.CPUTOGGLE.ordinal(), rod);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
