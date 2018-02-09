package org.schors.demu.client.ui;

public class Usage {
    private int ratingGroup;
    private String serviceId;
    private UsageType usageType;
    private long usage;

    public Usage() {
    }

    public Usage(UsageType usageType, long usage) {
        this.usageType = usageType;
        this.usage = usage;
    }

    public Usage(int ratingGroup, UsageType usageType, long usage) {
        this.ratingGroup = ratingGroup;
        this.usageType = usageType;
        this.usage = usage;
    }

    public Usage(String serviceId, UsageType usageType, long usage) {
        this.serviceId = serviceId;
        this.usageType = usageType;
        this.usage = usage;
    }

    public Usage(int ratingGroup, String serviceId, UsageType usageType, long usage) {
        this.ratingGroup = ratingGroup;
        this.serviceId = serviceId;
        this.usageType = usageType;
        this.usage = usage;
    }

    public int getRatingGroup() {
        return ratingGroup;
    }

    public void setRatingGroup(int ratingGroup) {
        this.ratingGroup = ratingGroup;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public UsageType getUsageType() {
        return usageType;
    }

    public void setUsageType(UsageType usageType) {
        this.usageType = usageType;
    }

    public long getUsage() {
        return usage;
    }

    public void setUsage(long usage) {
        this.usage = usage;
    }

    @Override
    public String toString() {
        return "Usage{" +
                "ratingGroup=" + ratingGroup +
                ", serviceId='" + serviceId + '\'' +
                ", usageType=" + usageType +
                ", usage=" + usage +
                '}';
    }
}
