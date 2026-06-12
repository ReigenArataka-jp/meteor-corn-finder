package de.damcraft.serverseeker.ssapi.requests;

/**
 * Builds a GET URL for the cornbread2100 mass scan API.
 * No API key required - it's a public API.
 *
 * Full filter support for the /v1/servers endpoint:
 * sort, descending, limit, skip, playerCount, minPlayers, maxPlayers,
 * playerLimit, full, onlinePlayer, onlineUuid, playerHistory, uuidHistory,
 * version, protocol, hasFavicon, description, hasPlayerSample,
 * seenAfter, seenBefore, ip, port, country, org, cracked, whitelisted,
 * vanilla, forge, enforcesSecureChat
 */
public class ServersRequest {
    private String baseUrl = "https://api.cornbread2100.com/v1/servers";

    private String sort = "lastSeen";
    private boolean descending = true;
    private int limit = 50;
    private int skip = 0;

    // Filters
    private Integer protocol;
    private Boolean cracked;
    private String countryCode;
    private String description;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer playerCount;
    private Boolean whitelisted;
    private Boolean hasFavicon;
    private Boolean vanilla;
    private Boolean forge;
    private Boolean full;
    private String playerHistory;
    private String onlinePlayer;
    private Long seenAfter;

    public void setSort(String sort) { this.sort = sort; }
    public void setDescending(boolean descending) { this.descending = descending; }
    public void setLimit(int limit) { this.limit = limit; }
    public void setSkip(int skip) { this.skip = skip; }
    public void setProtocolVersion(Integer version) { this.protocol = version; }
    public void setCracked(Boolean cracked) { this.cracked = cracked; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public void setDescription(String description) { this.description = description; }
    public void setPlayerCount(Integer count) { this.playerCount = count; }
    public void setMinPlayers(Integer min) { this.minPlayers = min; }
    public void setMaxPlayers(Integer max) { this.maxPlayers = max; }
    public void setWhitelisted(Boolean whitelisted) { this.whitelisted = whitelisted; }
    public void setHasFavicon(Boolean hasFavicon) { this.hasFavicon = hasFavicon; }
    public void setVanilla(Boolean vanilla) { this.vanilla = vanilla; }
    public void setForge(Boolean forge) { this.forge = forge; }
    public void setFull(Boolean full) { this.full = full; }
    public void setPlayerHistory(String playerName) { this.playerHistory = playerName; }
    public void setOnlinePlayer(String playerName) { this.onlinePlayer = playerName; }
    public void setSeenAfter(Long unixTimestamp) { this.seenAfter = unixTimestamp; }

    public String buildUrl() {
        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?sort=").append(sort);
        url.append("&descending=").append(descending);
        url.append("&limit=").append(limit);
        url.append("&skip=").append(skip);

        if (protocol != null) url.append("&protocol=").append(protocol);
        if (cracked != null) url.append("&cracked=").append(cracked);
        if (countryCode != null && !countryCode.isEmpty()) url.append("&country=").append(countryCode);
        if (description != null && !description.isEmpty()) url.append("&description=").append(description);
        if (playerCount != null) url.append("&playerCount=").append(playerCount);
        if (minPlayers != null) url.append("&minPlayers=").append(minPlayers);
        if (maxPlayers != null) url.append("&maxPlayers=").append(maxPlayers);
        if (whitelisted != null) url.append("&whitelisted=").append(whitelisted);
        if (hasFavicon != null) url.append("&hasFavicon=").append(hasFavicon);
        if (vanilla != null) url.append("&vanilla=").append(vanilla);
        if (forge != null) url.append("&forge=").append(forge);
        if (full != null) url.append("&full=").append(full);
        if (playerHistory != null && !playerHistory.isEmpty()) url.append("&playerHistory=").append(playerHistory);
        if (onlinePlayer != null && !onlinePlayer.isEmpty()) url.append("&onlinePlayer=").append(onlinePlayer);
        if (seenAfter != null) url.append("&seenAfter=").append(seenAfter);

        return url.toString();
    }

    /**
     * Builds a playerHistory URL for a specific server
     */
    public static String buildPlayerHistoryUrl(long ip, int port) {
        return "https://api.cornbread2100.com/v1/playerHistory?ip=" + ip + "&port=" + port;
    }
}
