package de.damcraft.serverseeker.gui;

import com.google.common.net.HostAndPort;
import de.damcraft.serverseeker.ServerSeeker;
import de.damcraft.serverseeker.ssapi.requests.ServersRequest;
import de.damcraft.serverseeker.ssapi.responses.ServersResponse;
import de.damcraft.serverseeker.utils.MultiplayerScreenUtil;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.util.List;

import static de.damcraft.serverseeker.SmallHttp.get;
import static de.damcraft.serverseeker.gui.FindNewServersScreen.intToIp;

public class FindPlayerScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;

    public enum SearchType {
        Online,
        History;
        @Override public String toString() { return switch (this) {
            case Online -> "Currently Online";
            case History -> "Player History";
        };}
    }

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<SearchType> searchType = sg.add(new EnumSetting.Builder<SearchType>()
        .name("search-type")
        .description("Search for players currently online or in server history.")
        .defaultValue(SearchType.History)
        .build()
    );

    private final Setting<String> playerName = sg.add(new StringSetting.Builder()
        .name("player-name")
        .description("The player name to search for.")
        .defaultValue("")
        .build()
    );

    WContainer settingsContainer;

    public FindPlayerScreen(MultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Find Players");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        settingsContainer = add(theme.verticalList()).widget();
        settingsContainer.add(theme.settings(settings)).expandX();

        add(theme.button("Find Player")).expandX().widget().action = () -> {
            String name = playerName.get().trim();
            if (name.isEmpty()) {
                clear();
                add(theme.label("Enter a player name first.")).expandX();
                return;
            }

            ServersRequest request = new ServersRequest();
            request.setLimit(50);

            if (searchType.get() == SearchType.Online) {
                request.setOnlinePlayer(name);
            } else {
                request.setPlayerHistory(name);
            }

            MeteorExecutor.execute(() -> {
                String url = request.buildUrl();
                String rawResponse = get(url);

                MinecraftClient.getInstance().execute(() -> {
                    if (rawResponse == null) {
                        add(theme.label("Network error")).expandX();
                        return;
                    }

                    ServersResponse response = ServerSeeker.gson.fromJson(rawResponse, ServersResponse.class);
                    if (response == null || response.isError()) {
                        add(theme.label(response != null ? response.error : "Failed to parse")).expandX();
                        return;
                    }

                    clear();
                    List<ServersResponse.Server> data = response.data;
                    if (data == null || data.isEmpty()) {
                        add(theme.label("Not found")).expandX();
                        return;
                    }

                    add(theme.label("Found " + data.size() + " servers:"));
                    WTable table = add(theme.table()).widget();
                    WButton addAllButton = table.add(theme.button("Add all")).expandX().widget();
                    addAllButton.action = () -> addAllServers(data);

                    table.row();
                    table.add(theme.label("Server IP"));
                    table.add(theme.label("Version"));
                    table.add(theme.label("Players"));

                    table.row();
                    table.add(theme.horizontalSeparator()).expandX();
                    table.row();

                    for (ServersResponse.Server server : data) {
                        String serverIP = intToIp(server.ip) + ":" + server.port;
                        String serverVersion = server.version != null ? server.version.name : "Unknown";
                        String players = server.players != null ? server.players.online + "/" + server.players.max : "?/?";

                        table.add(theme.label(serverIP));
                        table.add(theme.label(serverVersion));
                        table.add(theme.label(players));

                        WButton addServerButton = theme.button("Add Server");
                        addServerButton.action = () -> {
                            ServerInfo info = new ServerInfo("CornFinder " + serverIP + " (Player: " + name + ")", serverIP, ServerInfo.ServerType.OTHER);
                            MultiplayerScreenUtil.addInfoToServerList(multiplayerScreen, info);
                            addServerButton.visible = false;
                        };

                        HostAndPort hap = HostAndPort.fromString(serverIP);
                        WButton joinServerButton = theme.button("Join Server");
                        joinServerButton.action = () -> ConnectScreen.connect(new TitleScreen(), MinecraftClient.getInstance(),
                            new ServerAddress(hap.getHost(), hap.getPort()),
                            new ServerInfo("a", hap.toString(), ServerInfo.ServerType.OTHER), false, null);

                        WButton serverInfoButton = theme.button("Server Info");
                        serverInfoButton.action = () -> this.client.setScreen(new ServerInfoScreen(server));

                        table.add(addServerButton);
                        table.add(joinServerButton);
                        table.add(serverInfoButton);
                        table.row();
                    }
                });
            });
        };
    }

    private void addAllServers(List<ServersResponse.Server> servers) {
        for (ServersResponse.Server server : servers) {
            String serverIP = intToIp(server.ip) + ":" + server.port;
            ServerInfo info = new ServerInfo("CornFinder " + serverIP + " (Player: " + playerName.get() + ")", serverIP, ServerInfo.ServerType.OTHER);
            MultiplayerScreenUtil.addInfoToServerList(multiplayerScreen, info, false);
        }
        MultiplayerScreenUtil.saveList(multiplayerScreen);
        if (client == null) return;
        client.setScreen(this.multiplayerScreen);
    }

    @Override
    public void tick() {
        super.tick();
        settings.tick(settingsContainer, theme);
    }
}
