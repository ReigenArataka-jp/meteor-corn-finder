package de.damcraft.serverseeker.ssapi.requests;

/**
 * Builds a GET URL for the cornbread2100 mass scan API.
 * No API key required - it's a public API.
 */
public class ServersRequest {
    private String baseUrl = "https://api.cornbread2100.com/v1/servers";

    private String sort = "lastSeen";
    private boolean descending = true;
    private int limit = 50;
    private int skip = 0;

    private Integer protocol;
    private Boolean cracked;
    private String countryCode;
    private String description;
    private String software;

    public void setSort(String sort) {
        this.sort = sort;
    }

    public void setDescending(boolean descending) {
        this.descending = descending;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public void setProtocolVersion(Integer version) {
        this.protocol = version;
    }

    public void setCracked(Boolean cracked) {
        this.cracked = cracked;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public String buildUrl() {
        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?sort=").append(sort);
        url.append("&descending=").append(descending);
        url.append("&limit=").append(limit);
        url.append("&skip=").append(skip);

        if (protocol != null) {
            url.append("&protocol=").append(protocol);
        }
        if (cracked != null) {
            url.append("&cracked=").append(cracked);
        }
        if (countryCode != null && !countryCode.isEmpty()) {
            url.append("&country=").append(countryCode);
        }
        if (description != null && !description.isEmpty()) {
            url.append("&description=").append(description);
        }
        if (software != null && !software.isEmpty()) {
            url.append("&software=").append(software);
        }

        return url.toString();
    }
}
