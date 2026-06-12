package de.damcraft.serverseeker.utils;

import de.damcraft.serverseeker.ServerSeeker;
import de.damcraft.serverseeker.hud.HistoricPlayersHud;
import de.damcraft.serverseeker.ssapi.requests.ServersRequest;
import de.damcraft.serverseeker.ssapi.responses.PlayerHistoryResponse;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.orbit.EventHandler;

import java.util.ArrayList;
import java.util.List;

import static de.damcraft.serverseeker.SmallHttp.get;
import static meteordevelopment.meteorclient.MeteorClient.mc;

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

        if (mc.getNetworkHandler() == null) return;

        String address = mc.getNetworkHandler().getConnection().getAddress().toString();
        String[] addressParts = address.split("/");
        if (addressParts.length < 2) return;
        addressParts = addressParts[1].split(":");

        String ip = addressParts[0];
        int port = Integer.parseInt(addressParts[1]);
        long ipLong = 0;
        try {
            String[] ipParts = ip.split("\\.");
            ipLong = (Long.parseLong(ipParts[0]) << 24)
                   | (Long.parseLong(ipParts[1]) << 16)
                   | (Long.parseLong(ipParts[2]) << 8)
                   | Long.parseLong(ipParts[3]);
        } catch (Exception e) {
            return;
        }

        String url = ServersRequest.buildPlayerHistoryUrl(ipLong, port);
        String raw = get(url);
        if (raw == null) return;

        PlayerHistoryResponse response = ServerSeeker.gson.fromJson(raw, PlayerHistoryResponse.class);
        if (response == null || response.isError() || response.data == null) return;

        // Convert to the format HistoricPlayersHud expects
        List<HistoricPlayersHud.PlayerInfo> players = response.data.stream()
            .map(p -> new HistoricPlayersHud.PlayerInfo(p.name, p.id, p.lastSession))
            .toList();

        for (HistoricPlayersHud hud : huds) {
            hud.players = players;
            hud.isCracked = true; // Unknown from playerHistory alone, assume cracked
        }
    }
}
