package de.damcraft.serverseeker;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import de.damcraft.serverseeker.commands.ServerInfoCommand;
import de.damcraft.serverseeker.country.Countries;
import de.damcraft.serverseeker.country.Country;
import de.damcraft.serverseeker.country.CountrySetting;
import de.damcraft.serverseeker.hud.HistoricPlayersHud;
import de.damcraft.serverseeker.modules.BungeeSpoofModule;
import de.damcraft.serverseeker.utils.HistoricPlayersUpdater;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.minecraft.item.Items;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;

public class ServerSeeker extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("ServerSeeker", Items.SPYGLASS.getDefaultStack());
    public static final Map<String, Country> COUNTRY_MAP = new Object2ReferenceOpenHashMap<>();

    public static final Gson gson = new Gson();

    @Override
    public void onInitialize() {
        LOG.info("Loaded Meteor Corn Finder - uses cornbread2100's mass scan API!");

        // Load countries
        Countries.init();

        Modules.get().add(new BungeeSpoofModule());
        Hud.get().register(HistoricPlayersHud.INFO);
        Commands.add(new ServerInfoCommand());

        SettingsWidgetFactory.registerCustomFactory(CountrySetting.class, (theme) -> (table, setting) -> {
            CountrySetting.countrySettingW(table, (CountrySetting) setting, theme);
        });

        MeteorClient.EVENT_BUS.subscribe(HistoricPlayersUpdater.class);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "de.damcraft.serverseeker";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("ReigenArataka-jp", "meteor-corn-finder");
    }

    @Override
    public String getWebsite() {
        return "https://github.com/ReigenArataka-jp/meteor-corn-finder/";
    }

    @Override
    public String getCommit() {
        return Optional.ofNullable(FabricLoader
            .getInstance()
            .getModContainer("serverseeker")
            .orElseThrow().getMetadata()
            .getCustomValue("github:sha"))
            .map(CustomValue::getAsString)
            .map(String::trim)
            .orElse(null);
    }
}
