/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 ******************************************************************************/
package reika.reactorcraft.guis;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import reika.reactorcraft.base.ReactorGuiBase;
import reika.reactorcraft.container.MenuElectrolyzer;
import reika.reactorcraft.tileentities.processing.TileEntityElectrolyzer;

/**
 * 26.2 port of {@code GuiElectrolyzer}. Progress arrow has two source rows on the gui sheet depending
 * on whether the catalyst slot (0) is filled. Tank fills (heavy/light/input) are deferred (live fluid
 * sprite gap). Texture {@code electrolyzer.png}.
 */
public class ScreenElectrolyzer extends ReactorGuiBase<TileEntityElectrolyzer, MenuElectrolyzer> {

    public ScreenElectrolyzer(MenuElectrolyzer container, Inventory inv, Component title) {
        super(container, inv, title, 176, 175);
    }

    @Override
    protected String getGuiTexture() {
        return "electrolyzer";
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int j = (width - imageWidth) / 2;
        int k = (height - imageHeight) / 2;

        int prog = tile.getTimerScaled(66);
        int v = tile.getItem(0).isEmpty() ? 124 : 61;
        graphics.blit(RenderPipelines.GUI_TEXTURED, getTextureIdentifier(), j + 65, k + 17, 177, v, prog, 62, 256, 256);
    }
}
