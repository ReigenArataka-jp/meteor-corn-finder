package de.damcraft.serverseeker.commands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class ServerInfoCommand extends Command {
    private static final SimpleCommandExceptionType SINGLEPLAYER_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Cannot run command in singleplayer."));

    public ServerInfoCommand() {
        super("serverInfo", "Shows info about the current server (corrnbread2100 API).");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (mc.getCurrentServerEntry() == null) {
                throw SINGLEPLAYER_EXCEPTION.create();
            }

            info("-- Connected Server --");
            info("Address: (highlight)" + mc.getCurrentServerEntry().address);
            info("Name: (highlight)" + mc.getCurrentServerEntry().name);
            info("Player info from cornbread2100 API is not available via command.");
            info("Use the GUI to search for server info.");

            return SINGLE_SUCCESS;
        });
    }
}
