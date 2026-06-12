package de.damcraft.serverseeker.ssapi.responses;

import java.util.List;

public class PlayerHistoryResponse {
    public String error;
    public Integer credits;

    public static class PlayerEntry {
        public String name;
        public String id;       // UUID
        public Long lastSession; // unix timestamp
    }

    public List<PlayerEntry> data;

    public boolean isError() {
        return error != null;
    }
}
