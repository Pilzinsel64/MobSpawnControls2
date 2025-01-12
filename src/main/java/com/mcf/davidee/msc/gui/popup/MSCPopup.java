package com.mcf.davidee.msc.gui.popup;

import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.mcf.davidee.guilib.basic.BasicScreen;
import com.mcf.davidee.guilib.basic.OverlayScreen;

public abstract class MSCPopup extends OverlayScreen {

    private static final ResourceLocation TEXTURE = new ResourceLocation("msc2", "textures/gui/window.png");

    public static final int WIDTH = 256, HEIGHT = 129;

    public MSCPopup(BasicScreen bg) {
        super(bg);

    }

    @Override
    protected void unhandledKeyTyped(char c, int code) {
        if (code == 1) close();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawBackground() {
        super.drawBackground();
        drawGradientRect(0, 0, width, height, -1072689136, -804253680);
        int x = (width - WIDTH) / 2, y = (height - HEIGHT) / 2;
        mc.renderEngine.bindTexture(TEXTURE);
        GL11.glColor4f(1, 1, 1, 1);
        drawTexturedModalRect(x, y, 0, 0, WIDTH, HEIGHT);
    }

}
