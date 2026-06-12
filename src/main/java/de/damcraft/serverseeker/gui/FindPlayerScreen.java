package de.damcraft.serverseeker.gui;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

public class FindPlayerScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;

    public FindPlayerScreen(MultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Find Players");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        add(theme.label("Player search is not available with the cornbread2100 API.")).expandX();
        add(theme.label("Use the server search to find servers instead.")).expandX();
        add(theme.button("Back")).expandX().widget().action = () -> {
            if (client != null) client.setScreen(multiplayerScreen);
        };
    }

    @Override
    public void tick() {
        super.tick();
    }
}
