package de.damcraft.serverseeker.gui;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;

public class GetInfoScreen extends WindowScreen {
    public GetInfoScreen(MultiplayerScreen multiplayerScreen, MultiplayerServerListWidget.Entry entry) {
        super(GuiThemes.get(), "Get players");
        this.parent = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        add(theme.label("Player info is not available with the cornbread2100 API.")).expandX();
        add(theme.label("Use the server search to browse servers instead.")).expandX();
        add(theme.button("Back")).expandX().widget().action = this::close;
    }
}
