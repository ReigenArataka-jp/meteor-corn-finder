package de.damcraft.serverseeker.utils;

import de.damcraft.serverseeker.hud.HistoricPlayersHud;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.orbit.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class HistoricPlayersUpdater {
    @EventHandler
    private static void onGameJoinEvent(GameJoinedEvent ignoredEvent) {
        new Thread(HistoricPlayersUpdater::update).start();
    }

    public static void update() {
        List<HistoricPlayersHud> huds = new ArrayList<>();
        for (HudElement hudElement : Hud.get()) {
            if (hudElement instanceof HistoricPlayersHud && hudElement.isActive()) {
                huds.add((HistoricPlayersHud) hudElement);
            }
        }
        if (huds.isEmpty()) return;

        // cornbread2100 API doesn't have a server_info/player history endpoint
        // Clear previous data on join
        for (HistoricPlayersHud hud : huds) {
            hud.players = List.of();
            hud.isCracked = false;
        }
    }
}
