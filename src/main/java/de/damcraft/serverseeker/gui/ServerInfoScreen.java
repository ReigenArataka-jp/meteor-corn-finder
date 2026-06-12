package de.damcraft.serverseeker.gui;

import com.google.common.net.HostAndPort;
import de.damcraft.serverseeker.ssapi.responses.ServersResponse;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class ServerInfoScreen extends WindowScreen {
    private final ServersResponse.Server server;

    public ServerInfoScreen(ServersResponse.Server server) {
        super(GuiThemes.get(), "Server Info: " + FindNewServersScreen.intToIp(server.ip));
        this.server = server;
    }

    @Override
    public void initWidgets() {
        String ipStr = FindNewServersScreen.intToIp(server.ip) + ":" + server.port;
        HostAndPort hap = HostAndPort.fromString(ipStr);

        String versionStr = server.version != null ? server.version.name + " (protocol " + server.version.protocol + ")" : "Unknown";
        String playersStr = server.players != null ? server.players.online + "/" + server.players.max : "?/?";
        String description = server.description != null ? server.description : "";
        if (description.length() > 100) description = description.substring(0, 100) + "...";

        String lastSeenStr = "Unknown";
        if (server.lastSeen != null) {
            try {
                long epoch = Long.parseLong(server.lastSeen);
                lastSeenStr = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .format(Instant.ofEpochSecond(epoch).atZone(ZoneId.systemDefault()).toLocalDateTime());
            } catch (NumberFormatException ignored) {}
        }

        String crackedStr = server.cracked == null ? "Unknown" : server.cracked.toString();

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
    }
}
