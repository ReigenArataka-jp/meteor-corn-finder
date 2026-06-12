package de.damcraft.serverseeker.ssapi.responses;

import java.util.List;

public class ServersResponse {
    public String error;
    public Integer credits;

    public static class Server {
        public long ip;
        public int port;
        public String discovered;
        public String lastSeen;
        public VersionInfo version;
        public String description;
        public String rawDescription;
        public PlayersInfo players;
        public boolean hasFavicon;
        public boolean hasForgeData;
        public Boolean enforcesSecureChat;
        public String org;
        public GeoInfo geo;
        public Boolean cracked;
        public Boolean whitelisted;
    }

    public static class VersionInfo {
        public String name;
        public int protocol;
    }

    public static class PlayersInfo {
        public int max;
        public int online;
        public boolean hasPlayerSample;
    }

    public static class GeoInfo {
        public String country;
        public String city;
        public Double lat;
        public Double lon;
    }

    public List<Server> data;

    public boolean isError() {
        return error != null;
    }
}
