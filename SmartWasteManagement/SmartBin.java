public class SmartBin {
    private String binId;
    private String location;
    private String wasteType;
    private int fillLevel; // 0 to 100

    public SmartBin(String binId, String location, String wasteType, int fillLevel) {
        this.binId = binId;
        this.location = location;
        this.wasteType = wasteType;
        this.fillLevel = Math.max(0, Math.min(100, fillLevel));
    }

    public String getBinId() { return binId; }
    public String getLocation() { return location; }
    public String getWasteType() { return wasteType; }
    public int getFillLevel() { return fillLevel; }

    public void setFillLevel(int fillLevel) {
        this.fillLevel = Math.max(0, Math.min(100, fillLevel));
    }

    public String getStatus() {
        if (fillLevel >= 80) return "CRITICAL";
        if (fillLevel >= 50) return "WARNING";
        return "NORMAL";
    }

    public String toCsv() {
        return binId + "," + location.replace(",", ";") + "," + wasteType + "," + fillLevel;
    }

    public static SmartBin fromCsv(String line) {
        String[] parts = line.split(",");
        if (parts.length != 4) return null;
        return new SmartBin(
            parts[0].trim(),
            parts[1].trim(),
            parts[2].trim(),
            Integer.parseInt(parts[3].trim())
        );
    }
}
