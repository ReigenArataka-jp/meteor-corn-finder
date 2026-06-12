package de.damcraft.serverseeker.commands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.damcraft.serverseeker.ServerSeeker;
import de.damcraft.serverseeker.ssapi.requests.ServersRequest;
import de.damcraft.serverseeker.ssapi.responses.PlayerHistoryResponse;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.command.CommandSource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import static de.damcraft.serverseeker.SmallHttp.get;

public class ServerInfoCommand extends Command {
    private static final SimpleCommandExceptionType SINGLEPLAYER_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Cannot run command in singleplayer."));

    public ServerInfoCommand() {
        super("serverInfo", "Shows player history for the current server using cornbread2100's API.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (mc.getCurrentServerEntry() == null) {
                throw SINGLEPLAYER_EXCEPTION.create();
            }

            String address = mc.getCurrentServerEntry().address;
            String[] addressParts = address.split(":");
            String ip = addressParts[0];
            int port = addressParts.length > 1 ? Integer.parseInt(addressParts[1]) : 25565;

            // Convert ip to long
            long ipLong;
            try {
                String[] parts = ip.split("\\.");
                ipLong = (Long.parseLong(parts[0]) << 24)
                       | (Long.parseLong(parts[1]) << 16)
                       | (Long.parseLong(parts[2]) << 8)
                       | Long.parseLong(parts[3]);
            } catch (Exception e) {
                error("Could not parse IP address: " + ip);
                return SINGLE_SUCCESS;
            }

            MeteorExecutor.execute(() -> {
                String url = ServersRequest.buildPlayerHistoryUrl(ipLong, port);
                String raw = get(url);

                mc.execute(() -> {
                    if (raw == null) {
                        error("Network error");
                        return;
                    }

                    PlayerHistoryResponse response = ServerSeeker.gson.fromJson(raw, PlayerHistoryResponse.class);
                    if (response == null || response.isError()) {
                        error(response != null ? response.error : "Failed to parse");
                        return;
                    }

                    List<PlayerHistoryResponse.PlayerEntry> players = response.data;
                    if (players == null || players.isEmpty()) {
                        warning("No player history for this server.");
                    } else {
                        info("-- Player History (" + players.size() + " players) --");
                        for (PlayerHistoryResponse.PlayerEntry player : players) {
                            String lastSeenFormatted = "Unknown";
                            if (player.lastSession != null) {
                                lastSeenFormatted = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                                    .format(Instant.ofEpochSecond(player.lastSession).atZone(ZoneId.systemDefault()).toLocalDateTime());
                            }
                            info("- (highlight)" + player.name + " (default)last seen: (highlight)" + lastSeenFormatted);
                        }
                    }
                });
            });

            return SINGLE_SUCCESS;
        });
    }
}
