package de.damcraft.serverseeker.gui;

import com.google.common.net.HostAndPort;
import de.damcraft.serverseeker.ServerSeeker;
import de.damcraft.serverseeker.country.Country;
import de.damcraft.serverseeker.country.CountrySetting;
import de.damcraft.serverseeker.ssapi.requests.ServersRequest;
import de.damcraft.serverseeker.ssapi.responses.ServersResponse;
import de.damcraft.serverseeker.utils.MCVersionUtil;
import de.damcraft.serverseeker.utils.MultiplayerScreenUtil;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;

import java.util.List;

import static de.damcraft.serverseeker.SmallHttp.get;

public class FindNewServersScreen extends WindowScreen {
    public static NbtCompound savedSettings;
    private int timer;
    public WButton findButton;
    private boolean threadHasFinished;
    private String threadError;
    private List<ServersResponse.Server> threadServers;

    public enum Cracked {
        Any,
        Yes,
        No;

        public Boolean toBoolOrNull() {
            return switch (this) {
                case Any -> null;
                case Yes -> true;
                case No -> false;
            };
        }
    }

    public enum Version {
        Current,
        Any,
        Protocol,
        VersionString;

        @Override
        public String toString() {
            return switch (this) {
                case Current -> "Current";
                case Any -> "Any";
                case Protocol -> "Protocol";
                case VersionString -> "Version String";
            };
        }
    }

    public enum NumRangeType {
        Any,
        Equals,
        AtLeast,
        AtMost,
        Between;
        @Override
        public String toString() {
            return switch (this) {
                case Any -> "Any";
                case Equals -> "Equal To";
                case AtLeast -> "At Least";
                case AtMost -> "At Most";
                case Between -> "Between";
            };
        }
    }

    public enum GeoSearchType {
        None,
        Country
    }

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();
    WContainer settingsContainer;

    private final Setting<Cracked> crackedSetting = sg.add(new EnumSetting.Builder<Cracked>()
        .name("cracked")
        .description("Whether the server should be cracked or not")
        .defaultValue(Cracked.Any)
        .build()
    );

    private final Setting<NumRangeType> onlinePlayersNumTypeSetting = sg.add(new EnumSetting.Builder<NumRangeType>()
        .name("online-players-range")
        .description("The type of number range for the online players")
        .defaultValue(NumRangeType.Any)
        .build()
    );

    private final Setting<Integer> equalsOnlinePlayersSetting = sg.add(new IntSetting.Builder()
            .name("online-players")
            .description("The amount of online players the server should have")
            .defaultValue(2)
            .min(0)
            .visible(() -> onlinePlayersNumTypeSetting.get().equals(NumRangeType.Equals))
            .noSlider()
            .build()
    );

    private final Setting<Integer> atLeastOnlinePlayersSetting = sg.add(new IntSetting.Builder()
        .name("minimum-online-players")
        .description("The minimum amount of online players the server should have")
        .defaultValue(1)
        .min(0)
        .visible(() -> onlinePlayersNumTypeSetting.get().equals(NumRangeType.AtLeast) || onlinePlayersNumTypeSetting.get().equals(NumRangeType.Between))
        .noSlider()
        .build()
    );

    private final Setting<Integer> atMostOnlinePlayersSetting = sg.add(new IntSetting.Builder()
        .name("maximum-online-players")
        .description("The maximum amount of online players the server should have")
        .defaultValue(20)
        .min(0)
        .visible(() -> onlinePlayersNumTypeSetting.get().equals(NumRangeType.AtMost) || onlinePlayersNumTypeSetting.get().equals(NumRangeType.Between))
        .noSlider()
        .build()
    );

    private final Setting<String> descriptionSetting = sg.add(new StringSetting.Builder()
        .name("MOTD")
        .description("What the MOTD of the server should contain (empty for any)")
        .defaultValue("")
        .build()
    );

    private final Setting<Version> versionSetting = sg.add(new EnumSetting.Builder<Version>()
        .name("version")
        .description("The version the servers should have")
        .defaultValue(Version.Current)
        .build()
    );

    private final Setting<Integer> protocolVersionSetting = sg.add(new IntSetting.Builder()
        .name("protocol")
        .description("The protocol version the servers should have")
        .defaultValue(SharedConstants.getProtocolVersion())
        .visible(() -> versionSetting.get() == Version.Protocol)
        .min(0)
        .noSlider()
        .build()
    );

    private final Setting<String> versionStringSetting = sg.add(new StringSetting.Builder()
        .name("version-string")
        .description("The version string (e.g. 1.21.4) of the protocol version the server should have. Must be at least 1.7.1")
        .defaultValue("1.21.1")
        .visible(() -> versionSetting.get() == Version.VersionString)
        .build()
    );

    private final Setting<Integer> serverLimitSetting = sg.add(new IntSetting.Builder()
        .name("server-limit")
        .description("Maximum number of servers to fetch")
        .defaultValue(50)
        .min(1)
        .max(100)
        .noSlider()
        .build()
    );

    private final Setting<GeoSearchType> geoSearchTypeSetting = sg.add(new EnumSetting.Builder<GeoSearchType>()
        .name("geo-search-type")
        .description("Whether to filter by country")
        .defaultValue(GeoSearchType.None)
        .build()
    );

    private final Setting<Country> countrySetting = sg.add(new CountrySetting.Builder()
        .name("country")
        .description("The country the server should be located in")
        .defaultValue(ServerSeeker.COUNTRY_MAP.get("UN"))
        .visible(() -> geoSearchTypeSetting.get() == GeoSearchType.Country)
        .build()
    );

    MultiplayerScreen multiplayerScreen;

    public FindNewServersScreen(MultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Find new servers");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        loadSettings();
        onClosed(this::saveSettings);
        settingsContainer = add(theme.verticalList()).widget();
        settingsContainer.add(theme.settings(settings));
        add(theme.button("Reset all")).expandX().widget().action = this::resetSettings;
        findButton = add(theme.button("Find")).expandX().widget();
        findButton.action = () -> {
            ServersRequest request = new ServersRequest();
            request.setLimit(serverLimitSetting.get());

            request.setCracked(crackedSetting.get().toBoolOrNull());
            if (!descriptionSetting.get().isEmpty()) {
                request.setDescription(descriptionSetting.get());
            }

            switch (versionSetting.get()) {
                case Protocol -> request.setProtocolVersion(protocolVersionSetting.get());
                case VersionString -> {
                    int protocol = MCVersionUtil.versionToProtocol(versionStringSetting.get());
                    if (protocol == -1) {
                        clear();
                        add(theme.label("Unknown version string"));
                        return;
                    }
                    request.setProtocolVersion(protocol);
                }
                case Current -> request.setProtocolVersion(SharedConstants.getProtocolVersion());
            }

            if (geoSearchTypeSetting.get() == GeoSearchType.Country) {
                if (!countrySetting.get().name.equalsIgnoreCase("any")) {
                    request.setCountryCode(countrySetting.get().code);
                }
            }

            this.locked = true;
            this.threadHasFinished = false;
            this.threadError = null;
            this.threadServers = null;

            MeteorExecutor.execute(() -> {
                String url = request.buildUrl();
                String rawResponse = get(url);

                if (rawResponse == null) {
                    this.threadError = "Network error";
                    this.threadHasFinished = true;
                    return;
                }

                ServersResponse response = ServerSeeker.gson.fromJson(rawResponse, ServersResponse.class);

                if (response == null) {
                    this.threadError = "Failed to parse response";
                    this.threadHasFinished = true;
                    return;
                }

                if (response.isError()) {
                    this.threadError = response.error;
                    this.threadHasFinished = true;
                    return;
                }

                // Filter by online player count locally (API doesn't support player count filters)
                if (onlinePlayersNumTypeSetting.get() != NumRangeType.Any && response.data != null) {
                    response.data = response.data.stream().filter(s -> {
                        int online = s.players != null ? s.players.online : 0;
                        return switch (onlinePlayersNumTypeSetting.get()) {
                            case Equals -> online == equalsOnlinePlayersSetting.get();
                            case AtLeast -> online >= atLeastOnlinePlayersSetting.get();
                            case AtMost -> online <= atMostOnlinePlayersSetting.get();
                            case Between -> online >= atLeastOnlinePlayersSetting.get() && online <= atMostOnlinePlayersSetting.get();
                            default -> true;
                        };
                    }).toList();
                }

                this.threadServers = response.data;
                this.threadHasFinished = true;
            });
        };
    }

    @Override
    public void tick() {
        super.tick();
        settings.tick(settingsContainer, theme);

        if (threadHasFinished) handleThreadFinish();

        if (locked) {
            if (timer > 2) {
                findButton.set(getNext(findButton));
                timer = 0;
            } else {
                timer++;
            }
        } else if (!findButton.getText().equals("Find")) {
            findButton.set("Find");
        }
    }

    @Override
    protected void onClosed() {
        ServerSeeker.COUNTRY_MAP.values().forEach(Country::dispose);
    }

    private String getNext(WButton add) {
        return switch (add.getText()) {
            case "Find", "oo0" -> "ooo";
            case "ooo" -> "0oo";
            case "0oo" -> "o0o";
            case "o0o" -> "oo0";
            default -> "Find";
        };
    }

    private void handleThreadFinish() {
        this.threadHasFinished = false;
        this.locked = false;
        if (this.threadError != null) {
            clear();
            add(theme.label(this.threadError)).expandX();
            WButton backButton = add(theme.button("Back")).expandX().widget();
            backButton.action = this::reload;
            this.locked = false;
            return;
        }
        clear();
        List<ServersResponse.Server> servers = this.threadServers;

        if (servers == null || servers.isEmpty()) {
            add(theme.label("No servers found")).expandX();
            WButton backButton = add(theme.button("Back")).expandX().widget();
            backButton.action = this::reload;
            this.locked = false;
            return;
        }
        add(theme.label("Found " + servers.size() + " servers")).expandX();
        WButton addAllButton = add(theme.button("Add all")).expandX().widget();
        addAllButton.action = () -> {
            for (ServersResponse.Server server : servers) {
                String ip = intToIp(server.ip) + ":" + server.port;
                MultiplayerScreenUtil.addNameIpToServerList(multiplayerScreen, "CornFinder " + ip, ip, false);
            }
            MultiplayerScreenUtil.saveList(multiplayerScreen);
            MultiplayerScreenUtil.reloadServerList(multiplayerScreen);
            if (this.client == null) return;
            client.setScreen(this.multiplayerScreen);
        };

        WTable table = add(theme.table()).widget();
        table.add(theme.label("Server IP"));
        table.add(theme.label("Version"));
        table.add(theme.label("Players"));
        table.row();
        table.add(theme.horizontalSeparator()).expandX();
        table.row();

        for (ServersResponse.Server server : servers) {
            String serverIP = intToIp(server.ip) + ":" + server.port;
            String serverVersion = server.version != null ? server.version.name : "Unknown";
            String players = server.players != null ? server.players.online + "/" + server.players.max : "?/?";

            table.add(theme.label(serverIP));
            table.add(theme.label(serverVersion));
            table.add(theme.label(players));

            WButton addServerButton = theme.button("Add Server");
            addServerButton.action = () -> {
                ServerInfo info = new ServerInfo("CornFinder " + serverIP, serverIP, ServerInfo.ServerType.OTHER);
                MultiplayerScreenUtil.addInfoToServerList(multiplayerScreen, info);
                addServerButton.visible = false;
            };

            WButton joinServerButton = theme.button("Join Server");
            HostAndPort hap = HostAndPort.fromString(serverIP);
            joinServerButton.action = ()
                -> ConnectScreen.connect(new TitleScreen(), MinecraftClient.getInstance(), new ServerAddress(hap.getHost(), hap.getPort()), new ServerInfo("a", hap.toString(), ServerInfo.ServerType.OTHER), false, null);

            WButton serverInfoButton = theme.button("Server Info");
            serverInfoButton.action = () -> this.client.setScreen(new ServerInfoScreen(server));

            table.add(addServerButton);
            table.add(joinServerButton);
            table.add(serverInfoButton);
            table.row();
        }

        this.locked = false;
    }

    public static String intToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
    }

    public void saveSettings() {
        savedSettings = sg.toTag();
    }

    public void loadSettings() {
        if (savedSettings == null) return;
        sg.fromTag(savedSettings);
    }

    public void resetSettings() {
        for (Setting<?> setting : sg) {
            setting.reset();
        }
        saveSettings();
        reload();
    }
}
