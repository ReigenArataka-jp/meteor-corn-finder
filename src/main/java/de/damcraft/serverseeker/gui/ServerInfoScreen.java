package de.damcraft.serverseeker.gui;

import com.google.common.net.HostAndPort;
import de.damcraft.serverseeker.ServerSeeker;
import de.damcraft.serverseeker.ssapi.requests.ServersRequest;
import de.damcraft.serverseeker.ssapi.responses.PlayerHistoryResponse;
import de.damcraft.serverseeker.ssapi.responses.ServersResponse;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import static de.damcraft.serverseeker.SmallHttp.get;
import static de.damcraft.serverseeker.gui.FindNewServersScreen.intToIp;

public class ServerInfoScreen extends WindowScreen {
    private final ServersResponse.Server server;

    public ServerInfoScreen(ServersResponse.Server server) {
        super(GuiThemes.get(), "Server Info: " + intToIp(server.ip));
        this.server = server;
    }

    @Override
    public void initWidgets() {
        String ipStr = intToIp(server.ip) + ":" + server.port;
        final HostAndPort hap = HostAndPort.fromString(ipStr);

        final String versionStr = server.version != null ? server.version.name + " (protocol " + server.version.protocol + ")" : "Unknown";
        final String playersStr = server.players != null ? server.players.online + "/" + server.players.max : "?/?";
        String desc = server.description != null ? server.description : "";
        if (desc.length() > 100) desc = desc.substring(0, 100) + "...";
        final String description = desc;

        String seen = "Unknown";
        if (server.lastSeen != null) {
            try {
                long epoch = Long.parseLong(server.lastSeen);
                seen = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .format(Instant.ofEpochSecond(epoch).atZone(ZoneId.systemDefault()).toLocalDateTime());
            } catch (NumberFormatException ignored) {}
        }
        final String lastSeenStr = seen;

        final String crackedStr = server.cracked == null ? "Unknown" : server.cracked.toString();

        WTable dataTable = add(theme.table()).widget();

        dataTable.add(theme.label("IP: "));
        dataTable.add(theme.label(ipStr));
        dataTable.row();

        dataTable.add(theme.label("Version: "));
        dataTable.add(theme.label(versionStr));
        dataTable.row();

        dataTable.add(theme.label("Players: "));
        dataTable.add(theme.label(playersStr));
        dataTable.row();

        dataTable.add(theme.label("Cracked: "));
        dataTable.add(theme.label(crackedStr));
        dataTable.row();

        dataTable.add(theme.label("Description: "));
        dataTable.add(theme.label(description));
        dataTable.row();

        dataTable.add(theme.label("Last Seen: "));
        dataTable.add(theme.label(lastSeenStr));
        dataTable.row();

        if (server.org != null) {
            dataTable.add(theme.label("Org: "));
            dataTable.add(theme.label(server.org));
            dataTable.row();
        }

        if (server.geo != null) {
            dataTable.add(theme.label("Country: "));
            dataTable.add(theme.label(server.geo.country != null ? server.geo.country : "Unknown"));
            dataTable.row();
        }

        if (server.whitelisted != null) {
            dataTable.add(theme.label("Whitelisted: "));
            dataTable.add(theme.label(server.whitelisted.toString()));
            dataTable.row();
        }

        WButton joinServerButton = add(theme.button("Join this Server")).expandX().widget();
        joinServerButton.action = ()
            -> ConnectScreen.connect(new TitleScreen(), MinecraftClient.getInstance(),
                new ServerAddress(hap.getHost(), hap.getPort()),
                new ServerInfo("a", hap.toString(), ServerInfo.ServerType.OTHER), false, null);

        // Fetch and show player history
        add(theme.label(""));
        add(theme.label("Player History:"));
        add(theme.label("Loading...")).expandX();

        MeteorExecutor.execute(() -> {
            String url = ServersRequest.buildPlayerHistoryUrl(server.ip, server.port);
            String raw = get(url);

            this.client.execute(() -> {
                clear();
                initWidgetsStatic(hap, ipStr, versionStr, playersStr, description, lastSeenStr, crackedStr, raw);
            });
        });
    }

    private void initWidgetsStatic(HostAndPort hap, String ipStr, String versionStr, String playersStr,
                                    String description, String lastSeenStr, String crackedStr, String rawHistory) {
        WTable dataTable = add(theme.table()).widget();

        dataTable.add(theme.label("IP: "));
        dataTable.add(theme.label(ipStr));
        dataTable.row();
        dataTable.add(theme.label("Version: "));
        dataTable.add(theme.label(versionStr));
        dataTable.row();
        dataTable.add(theme.label("Players: "));
        dataTable.add(theme.label(playersStr));
        dataTable.row();
        dataTable.add(theme.label("Cracked: "));
        dataTable.add(theme.label(crackedStr));
        dataTable.row();
        dataTable.add(theme.label("Description: "));
        dataTable.add(theme.label(description));
        dataTable.row();
        dataTable.add(theme.label("Last Seen: "));
        dataTable.add(theme.label(lastSeenStr));
        dataTable.row();

        if (server.org != null) {
            dataTable.add(theme.label("Org: "));
            dataTable.add(theme.label(server.org));
            dataTable.row();
        }
        if (server.geo != null) {
            dataTable.add(theme.label("Country: "));
            dataTable.add(theme.label(server.geo.country != null ? server.geo.country : "Unknown"));
            dataTable.row();
        }
        if (server.whitelisted != null) {
            dataTable.add(theme.label("Whitelisted: "));
            dataTable.add(theme.label(server.whitelisted.toString()));
            dataTable.row();
        }

        WButton joinServerButton = add(theme.button("Join this Server")).expandX().widget();
        joinServerButton.action = ()
            -> ConnectScreen.connect(new TitleScreen(), MinecraftClient.getInstance(),
                new ServerAddress(hap.getHost(), hap.getPort()),
                new ServerInfo("a", hap.toString(), ServerInfo.ServerType.OTHER), false, null);

        add(theme.label(""));
        add(theme.label("Player History:"));

        if (rawHistory == null) {
            add(theme.label("Failed to fetch player history."));
            return;
        }

        PlayerHistoryResponse response = ServerSeeker.gson.fromJson(rawHistory, PlayerHistoryResponse.class);
        if (response == null || response.isError()) {
            add(theme.label("No player history available."));
            return;
        }

        List<PlayerHistoryResponse.PlayerEntry> players = response.data;
        if (players == null || players.isEmpty()) {
            add(theme.label("No players found in history."));
            return;
        }

        WTable playersTable = add(theme.table()).expandX().widget();
        playersTable.add(theme.label("Name ")).expandX();
        playersTable.add(theme.label("Last seen ")).expandX();
        playersTable.row();
        playersTable.add(theme.horizontalSeparator()).expandX();
        playersTable.row();

        for (PlayerHistoryResponse.PlayerEntry player : players) {
            String name = player.name;
            String lastSeenFormatted = "Unknown";
            if (player.lastSession != null) {
                lastSeenFormatted = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .format(Instant.ofEpochSecond(player.lastSession).atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            playersTable.add(theme.label(name + " ")).expandX();
            playersTable.add(theme.label(lastSeenFormatted + " ")).expandX();
            playersTable.row();
        }
    }
}
