package com.mcf.davidee.msc.gui.edit;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.mcf.davidee.guilib.basic.FocusedContainer;
import com.mcf.davidee.guilib.basic.Label;
import com.mcf.davidee.guilib.core.Button;
import com.mcf.davidee.guilib.core.Checkbox;
import com.mcf.davidee.guilib.core.Container;
import com.mcf.davidee.guilib.core.Scrollbar;
import com.mcf.davidee.guilib.focusable.FocusableLabel;
import com.mcf.davidee.guilib.focusable.FocusableWidget;
import com.mcf.davidee.guilib.vanilla.ButtonVanilla;
import com.mcf.davidee.guilib.vanilla.CheckboxVanilla;
import com.mcf.davidee.guilib.vanilla.ScrollbarVanilla;
import com.mcf.davidee.guilib.vanilla.sliders.IntSlider;
import com.mcf.davidee.msc.MobSpawnControls;
import com.mcf.davidee.msc.gui.MSCScreen;
import com.mcf.davidee.msc.packet.MSCPacket;
import com.mcf.davidee.msc.packet.MSCPacket.PacketType;
import com.mcf.davidee.msc.packet.settings.EntitySettingPacket;
import com.mcf.davidee.msc.packet.settings.EntitySettingPacket.BiomeEntry;

public class EditEntityScreen extends MSCScreen {

    public static final int ENABLED_COLOR = 0x49EC65, ENABLED_HOVER = 0x3CD5BC, ENABLED_FOCUS = 0xFF00;
    public static final int DIS_COLOR = 0xFA3333, DIS_HOVER = 0xEB489F, DIS_FOCUS = 0xFF0000;

    private Label title, subTitle;
    private Button save, close;
    private Scrollbar scrollbar;
    private Checkbox enabled;
    private IntSlider weight, min, max;

    private Container masterContainer, labelContainer;
    private FocusableWidget lastFocused;

    private EntitySettingPacket packet;

    public EditEntityScreen(EntitySettingPacket packet, GuiScreen parent) {
        super(parent);

        this.packet = packet;
    }

    @Override
    public void drawBackground() {
        super.drawBackground();
        drawRect(
            labelContainer.left(),
            labelContainer.top(),
            labelContainer.right() - 10,
            labelContainer.bottom(),
            0x44444444);
    }

    @Override
    public void updateScreen() {
        if (lastFocused == labelContainer.getFocusedWidget()) updateEntrySettings();
        else updateGuiSettings();
        super.updateScreen();
    }

    @Override
    protected void unhandledKeyTyped(char c, int code) {
        if (code == Keyboard.KEY_RETURN) {
            if (labelContainer.getFocusedWidget() == lastFocused) {
                enabled.setChecked(!enabled.isChecked());
                updateEntrySettings();
            }
        }
        super.unhandledKeyTyped(c, code);
    }

    private void updateGuiSettings() {
        lastFocused = labelContainer.getFocusedWidget();
        FocusableLabel label = (FocusableLabel) lastFocused;
        BiomeEntry e = (BiomeEntry) label.getUserData();
        enabled.setChecked(e.weight > 0);
        weight.setIntValue(enabled.isChecked() ? e.weight : 4);
        min.setIntValue(e.min);
        max.setIntValue(e.max);
    }

    private void updateEntrySettings() {
        FocusableLabel label = (FocusableLabel) labelContainer.getFocusedWidget();
        BiomeEntry e = (BiomeEntry) label.getUserData();
        if (enabled.isChecked()) {
            e.weight = weight.getIntValue();
            e.min = min.getIntValue();
            e.max = max.getIntValue();
            label.setColor(ENABLED_COLOR);
            label.setHoverColor(ENABLED_HOVER);
            label.setFocusColor(ENABLED_FOCUS);
        } else {
            e.weight = 0;
            e.min = min.getIntValue();
            e.max = max.getIntValue();
            label.setColor(DIS_COLOR);
            label.setHoverColor(DIS_HOVER);
            label.setFocusColor(DIS_FOCUS);
        }
    }

    @Override
    protected void revalidateGui() {
        close.setPosition(width / 2 + 6, height / 4 + 135);
        save.setPosition(width / 2 - 57, height / 4 + 135);
        title.setPosition(width / 2, height / 4 - 48);
        subTitle.setPosition(width / 2, height / 4 - 32);

        enabled.setPosition(width / 2 + 70, height / 4 + 15);

        weight.setPosition(width / 2 + 50, height / 4 + 30);
        min.setPosition(width / 2 + 50, height / 4 + 55);
        max.setPosition(width / 2 + 50, height / 4 + 80);

        List<FocusableWidget> groups = labelContainer.getFocusableWidgets();
        for (int i = 0; i < groups.size(); ++i) groups.get(i)
            .setPosition(width / 2 - 50, height / 4 - 11 + i * 14);

        scrollbar.setPosition(width / 2 + 20, height / 4 - 13);
        labelContainer.revalidate(width / 2 - 120, height / 4 - 13, 150, 143);
        masterContainer.revalidate(0, 0, width, height);
    }

    @Override
    protected void createGui() {
        BiomeEntry[] arr = packet.getOrderedEntries();

        masterContainer = new Container();
        title = new Label(packet.mod + " Controls");
        subTitle = new Label(packet.entity);
        close = new ButtonVanilla(50, 20, "Cancel", new CloseHandler());
        save = new ButtonVanilla(50, 20, "Save", this);

        enabled = new CheckboxVanilla("Enabled", true);

        weight = new IntSlider(100, 20, "Weight %d", 1, 1, 100);
        min = new IntSlider(100, 20, "Min %d", 1, 1, 10);
        max = new IntSlider(100, 20, "Max %d", 1, 1, 15);

        masterContainer.addWidgets(title, subTitle, enabled, weight, min, max, save, close);

        scrollbar = new ScrollbarVanilla(10);
        labelContainer = new FocusedContainer(scrollbar, 14, 4);

        FocusableLabel[] labels = new FocusableLabel[arr.length];
        for (int i = 0; i < labels.length; ++i) {
            BiomeEntry e = arr[i];
            if (e.weight != 0) // enabled
                labels[i] = new FocusableLabel(e.biome, ENABLED_COLOR, ENABLED_HOVER, ENABLED_FOCUS);
            else // disabled
                labels[i] = new FocusableLabel(e.biome, DIS_COLOR, DIS_HOVER, DIS_FOCUS);
            labels[i].setUserData(e);
        }

        labelContainer.addWidgets(labels);
        updateGuiSettings();

        containers.add(labelContainer);
        containers.add(masterContainer);

        selectedContainer = labelContainer;
    }

    @Override
    public void buttonClicked(Button b) {
        if (b == save) {
            save();
            close();
        }
    }

    private void save() {
        List<BiomeEntry> entries = new ArrayList<BiomeEntry>();
        List<String> empty = new ArrayList<String>();

        for (FocusableWidget w : labelContainer.getFocusableWidgets()) {
            BiomeEntry e = (BiomeEntry) ((FocusableLabel) w).getUserData();
            if (e.weight > 0) {
                if (e.min > e.max) {
                    int tmp = e.min;
                    e.min = e.max;
                    e.max = tmp;
                }
                entries.add(e);
            } else empty.add(e.biome);
        }
        MobSpawnControls.DISPATCHER.sendToServer(
            MSCPacket.getPacket(
                PacketType.ENTITY_SETTING,
                packet.mod,
                packet.entity,
                entries.toArray(new BiomeEntry[0]),
                empty.toArray(new String[0])));
    }
}
