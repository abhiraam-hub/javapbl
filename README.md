# javapbl - Smart Waste Management System

A Java Swing-based desktop application designed for **Real-Time IoT Waste Bin Monitoring & Priority Route Optimization**. This project simulates an IoT-enabled smart city municipal waste management system that dynamically tracks bin levels, forecasts critical thresholds, and generates optimized truck dispatch routes.

---

## 🌟 Key Features

- **Live IoT Bin Monitoring**: Interactive dashboard displaying real-time fill levels ($0-100\%$) with color-coded progress bars and dynamic status badges:
  - 🟢 **NORMAL** ($< 50\%$)
  - 🟠 **WARNING** ($50\% - 79\%$)
  - 🔴 **CRITICAL** ($\ge 80\%$)
- **IoT Sensor Simulator**: Mimics ultrasonic waste-level telemetry with both manual on-demand polling and an automated periodic timer (polls every 3 seconds).
- **Smart Truck Dispatch (Priority Routing)**: Automatically identifies overloaded bins ($\ge 80\%$), optimizes the collection route by sorting bins from highest to lowest fill level, empties them, and logs the dispatch path.
- **Bin Network Management & Persistence**: Easily register new smart bins or remove obsolete ones; all network state changes persist automatically to `smart_bins_data.csv`.
- **KPI Summary Cards**: Real-time counter metrics highlighting Total Smart Bins, Critical Alerts, Network Average Fill Level, and Total Truck Dispatches.
- **Live System Activity Logs**: Comprehensive audit trail timestamping sensor updates, manual empties, truck routing events, and registrations.

---

## 📂 Project Structure

```plaintext
javapbl/
├── README.md
├── .gitignore
└── SmartWasteManagement/
    ├── SmartBin.java         # Data model: properties, clamping, CSV conversion
    ├── WasteManager.java     # Core logic, simulation, route optimization, persistence
    └── SmartWasteApp.java    # Swing GUI desktop interface & KPI dashboard
```

---

## 🛠️ Architecture Overview

| Component | Role | Description |
| :--- | :--- | :--- |
| **`SmartBin.java`** | **Model** | Encapsulates bin attributes (`binId`, `location`, `wasteType`, `fillLevel`), clamps values between $0$ and $100\%$, computes statuses, and handles CSV serialization. |
| **`WasteManager.java`** | **Controller / Service** | Manages the bin collection, seeds default locations, executes sensor simulation, handles collection routing algorithms, and manages CSV read/write persistence. |
| **`SmartWasteApp.java`** | **View / UI** | Java Swing frontend featuring tabbed workspaces, custom table cell renderers (`ProgressBarRenderer`, `StatusRenderer`), KPI metric cards, registration forms, and controls. |

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK) 17** or higher installed.
- Ensure `javac` and `java` are available on your system `PATH`.

### Compilation & Execution

1. **Clone the repository:**
   ```bash
   git clone https://github.com/abhiraam-hub/javapbl.git
   cd javapbl/SmartWasteManagement
   ```

2. **Compile the Java source files:**
   ```bash
   javac *.java
   ```

3. **Launch the application:**
   ```bash
   java SmartWasteApp
   ```

---

## 💾 Data Storage

All bin records and telemetry data are automatically saved locally in CSV format inside `smart_bins_data.csv`:
```csv
BIN-101,Main Gate Entrance,General,45
BIN-102,Central Cafeteria,Organic,88
BIN-103,Academic Block A,Plastic,62
BIN-104,Computer Lab Wing,E-Waste,30
BIN-105,Sports Complex,Plastic,92
BIN-106,Library Plaza,Paper,20
```

---

## 👤 Author

- GitHub: [@abhiraam-hub](https://github.com/abhiraam-hub)
