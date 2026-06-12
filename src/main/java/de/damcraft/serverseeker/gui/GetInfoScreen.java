package de.damcraft.serverseeker.gui;

import de.damcraft.serverseeker.ServerSeeker;
import de.damcraft.serverseeker.ssapi.requests.ServersRequest;
import de.damcraft.serverseeker.ssapi.responses.PlayerHistoryResponse;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.Accounts;
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import static de.damcraft.serverseeker.SmallHttp.get;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GetInfoScreen extends WindowScreen {
    private final MultiplayerServerListWidget.Entry entry;

    public GetInfoScreen(MultiplayerScreen multiplayerScreen, MultiplayerServerListWidget.Entry entry) {
        super(GuiThemes.get(), "Get players");
        this.parent = multiplayerScreen;
        this.entry = entry;
    }

    @Override
    public void initWidgets() {
        if (entry == null || !(entry instanceof MultiplayerServerListWidget.ServerEntry)) {
            add(theme.label("No server selected"));
            return;
        }
        ServerInfo serverInfo = ((MultiplayerServerListWidget.ServerEntry) entry).getServer();
        String address = serverInfo.address;

        if (!address.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?::[0-9]{1,5})?$")) {
            try {
                InetAddress inetAddress = InetAddress.getByName(address);
                address = inetAddress.getHostAddress();
            } catch (UnknownHostException e) {
                add(theme.label("You can only get player info for servers with an IP address"));
                return;
            }
        }

        add(theme.label("Loading..."));

        String[] addressParts = address.split(":");
        String ipStr = addressParts[0];
        int port = addressParts.length > 1 ? Integer.parseInt(addressParts[1]) : 25565;

        // Convert dotted IP to integer
        long ipLong = ipToLong(ipStr);

        MeteorExecutor.execute(() -> {
            String url = ServersRequest.buildPlayerHistoryUrl(ipLong, port);
            String raw = get(url);

            MinecraftClient.getInstance().execute(() -> {
                clear();

                if (raw == null) {
                    add(theme.label("Network error")).expandX();
                    return;
                }

                PlayerHistoryResponse response = ServerSeeker.gson.fromJson(raw, PlayerHistoryResponse.class);

                if (response == null || response.isError()) {
                    add(theme.label(response != null ? response.error : "Failed to parse")).expandX();
                    return;
                }

                load(response);
            });
        });
    }

    private void load(PlayerHistoryResponse response) {
        List<PlayerHistoryResponse.PlayerEntry> players = response.data;
        if (players == null || players.isEmpty()) {
            add(theme.label("No records of players found.")).expandX();
            return;
        }
        String playersLabel = players.size() == 1 ? " player:" : " players:";
        add(theme.label("Found " + players.size() + playersLabel));

        WTable table = add(theme.table()).widget();
        table.add(theme.label("Name "));
        table.add(theme.label("Last seen "));
        table.add(theme.label("Login (cracked)"));
        table.row();
        table.add(theme.horizontalSeparator()).expandX();
        table.row();

        for (PlayerHistoryResponse.PlayerEntry player : players) {
            String name = player.name;
            Long lastSeen = player.lastSession;
            String lastSeenFormatted = lastSeen != null ? DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .format(Instant.ofEpochSecond(lastSeen).atZone(ZoneId.systemDefault()).toLocalDateTime()) : "Unknown";

            table.add(theme.label(name + " "));
            table.add(theme.label(lastSeenFormatted + " "));

            if (mc.getSession().getUsername().equals(name)) {
                table.add(theme.label("Logged in")).expandCellX();
            } else {
                WButton loginButton = table.add(theme.button("Login")).widget();
                if (mc.getSession().getUsername().equals(name)) {
                    loginButton.visible = false;
                }
                loginButton.action = () -> {
                    loginButton.visible = false;
                    if (this.client == null) return;
                    boolean exists = false;
                    for (Account<?> account : Accounts.get()) {
                        if (account instanceof CrackedAccount && account.getUsername().equals(name)) {
                            account.login();
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        CrackedAccount account = new CrackedAccount(name);
                        account.login();
                        Accounts.get().add(account);
                    }
                    close();
                };
            }
            table.row();
        }
    }

    private static long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        return (Long.parseLong(parts[0]) << 24)
             | (Long.parseLong(parts[1]) << 16)
             | (Long.parseLong(parts[2]) << 8)
             | Long.parseLong(parts[3]);
    }
}
