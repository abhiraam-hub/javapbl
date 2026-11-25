import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WasteManager {
    private static final String DATA_FILE = "smart_bins_data.csv";
    private final List<SmartBin> bins = new ArrayList<>();
    private final List<String> logs = new ArrayList<>();
    private final Random random = new Random();
    private int dispatchCount = 0;

    public WasteManager() {
        loadData();
    }

    public List<SmartBin> getBins() { return bins; }
    public List<String> getLogs() { return logs; }
    public int getDispatchCount() { return dispatchCount; }

    public boolean addBin(SmartBin bin) {
        for (SmartBin b : bins) {
            if (b.getBinId().equalsIgnoreCase(bin.getBinId())) {
                return false; // Duplicate ID
            }
        }
        bins.add(bin);
        addLog("Registered new bin [" + bin.getBinId() + "] at " + bin.getLocation() + " (" + bin.getWasteType() + ").");
        saveData();
        return true;
    }

    public void removeBin(String binId) {
        bins.removeIf(b -> b.getBinId().equals(binId));
        addLog("Removed bin [" + binId + "] from the network.");
        saveData();
    }

    public void simulateSensors() {
        for (SmartBin bin : bins) {
            int increase = random.nextInt(21) + 5; // +5% to +25%
            bin.setFillLevel(bin.getFillLevel() + increase);
        }
        addLog("IoT Sensors polled: Updated fill levels across " + bins.size() + " bins.");
        saveData();
    }

    public void emptyBin(String binId) {
        for (SmartBin bin : bins) {
            if (bin.getBinId().equals(binId)) {
                bin.setFillLevel(0);
                dispatchCount++;
                addLog("Manual Dispatch: Emptied bin [" + binId + "] at " + bin.getLocation() + ".");
                saveData();
                return;
            }
        }
    }

    public int dispatchCriticalTrucks() {
        List<SmartBin> criticalBins = new ArrayList<>();
        for (SmartBin bin : bins) {
            if (bin.getFillLevel() >= 80) {
                criticalBins.add(bin);
            }
        }

        if (criticalBins.isEmpty()) return 0;

        // Route optimization: prioritize highest fill levels first
        criticalBins.sort((a, b) -> Integer.compare(b.getFillLevel(), a.getFillLevel()));

        StringBuilder route = new StringBuilder("Smart Dispatch Route: ");
        for (int i = 0; i < criticalBins.size(); i++) {
            SmartBin b = criticalBins.get(i);
            route.append(b.getBinId()).append(" (").append(b.getFillLevel()).append("%)");
            if (i < criticalBins.size() - 1) route.append(" -> ");
            b.setFillLevel(0);
        }

        dispatchCount++;
        addLog(route.toString() + " [ALL EMPTIED]");
        saveData();
        return criticalBins.size();
    }

    public long getCriticalCount() {
        return bins.stream().filter(b -> b.getFillLevel() >= 80).count();
    }

    public double getAverageFillLevel() {
        if (bins.isEmpty()) return 0.0;
        int total = bins.stream().mapToInt(SmartBin::getFillLevel).sum();
        return (double) total / bins.size();
    }

    private void addLog(String message) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logs.add(0, "[" + time + "] " + message); // Newest on top
    }

    public void clearLogs() {
        logs.clear();
        addLog("Activity logs cleared.");
    }

    private void saveData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (SmartBin bin : bins) {
                writer.write(bin.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            addLog("Error saving data: " + e.getMessage());
        }
    }

    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            seedDefaultBins();
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                SmartBin bin = SmartBin.fromCsv(line);
                if (bin != null) bins.add(bin);
            }
            addLog("Loaded " + bins.size() + " smart bins from local storage.");
        } catch (IOException e) {
            seedDefaultBins();
        }
    }

    private void seedDefaultBins() {
        bins.add(new SmartBin("BIN-101", "Main Gate Entrance", "General", 45));
        bins.add(new SmartBin("BIN-102", "Central Cafeteria", "Organic", 88));
        bins.add(new SmartBin("BIN-103", "Academic Block A", "Plastic", 62));
        bins.add(new SmartBin("BIN-104", "Computer Lab Wing", "E-Waste", 30));
        bins.add(new SmartBin("BIN-105", "Sports Complex", "Plastic", 92));
        bins.add(new SmartBin("BIN-106", "Library Plaza", "Paper", 20));
        addLog("Initialized system with 6 default smart bins.");
        saveData();
    }
}
