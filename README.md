
<div align="center">

<img src="https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=java&logoColor=white"/>
<img src="https://img.shields.io/badge/JavaFX-21.0.10-blue?style=for-the-badge&logo=java&logoColor=white"/>
<img src="https://img.shields.io/badge/SQLite-Database-green?style=for-the-badge&logo=sqlite&logoColor=white"/>
<img src="https://img.shields.io/badge/DAA-Algorithms-purple?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Status-Active-brightgreen?style=for-the-badge"/>

# 🎓 CampusXchange
### *Intelligent Campus Resource & Knowledge Exchange System*

> **A DAA-powered platform that eliminates resource waste on campus by connecting students who have resources with students who need them — through smart scheduling, graph-based swap detection, and persistent SQLite storage.**

[Features](#-features) • [DAA Concepts](#-daa-concepts) • [Architecture](#-architecture) • [Setup](#-setup) • [Demo](#-demo-flow) • [Team](#-team)

---

</div>

## 🚀 The Problem We Solve

Every semester, students buy resources — drafters, calculators, lab coats, textbooks — and once the semester ends, these resources collect dust. Meanwhile, the next batch of students buys the same things at full price.

**CampusXchange fixes this.**

```
Senior has unused Drafter → lists it for Rs.250
Junior needs a Drafter    → posts buy request Rs.300
System matches them       → contacts revealed
They meet in person       → deal done
```

No middleman. No online payment. Just smart matching.

---

## ✨ Features

### 🛒 Three Listing Types

| Type | Description | Algorithm Used |
|------|-------------|----------------|
| **SELL** | Permanent transfer after semester ends | Exchange Graph (DFS) |
| **RENT** | Short-term borrowing with time slots | Priority Queue + Interval Scheduling |
| **DIGITAL** | Free Google Drive link sharing | Direct access, no conflict |

### 🔑 Core Capabilities
- **Smart Buy Matching** — matches buyer budget against seller asking price
- **Conflict-Free Rent Scheduling** — no double-booking of physical resources
- **Mutual Swap Detection** — finds students who can exchange directly (no money needed)
- **Contact Privacy** — phone/WhatsApp revealed only when a match is made
- **Persistent Storage** — SQLite database survives app restarts
- **Pre-loaded Demo Data** — system seeds realistic data on first launch

---

## 🧠 DAA Concepts

This project is built around real algorithm implementations — not just theory.

### 1. Priority Queue (Max Heap) — Rent Scheduling
```
Higher offering price → processed first
Equal price → earlier end time wins (frees resource sooner)
Prevents starvation via wait-time aging

Score = (offeringPrice × 0.7) + (waitingMinutes × 0.3)

Time Complexity: O(n log n)
```

### 2. Interval Conflict Detection — No Double Booking
```
Request A: Calculator [1 → 3]
Request B: Calculator [2 → 5]  ← CONFLICT detected
Request C: Calculator [4 → 6]  ← No conflict with A

Overlap check: !(endA <= startB || startA >= endB)

Time Complexity: O(n²) worst case
```

### 3. Directed Graph + DFS Cycle Detection — Mutual Swaps
```
Nikshay wants Calculator (owned by Dhruv)  →  Edge: Nikshay → Dhruv
Dhruv wants Drafter (owned by Nikshay)     →  Edge: Dhruv → Nikshay

Cycle detected → both can swap directly → no money needed!

Time Complexity: O(V + E)
```

### 4. Greedy Scheduling — Activity Selection
```
Sort by priority score → greedily allocate non-conflicting requests
Maximizes number of satisfied requests in a time window
```

---

## 🏗 Architecture

```
SmartCampusAllocation/
│
├── src/
│   ├── MainApp.java              # JavaFX UI entry point
│   ├── DataSeeder.java           # Pre-loads demo data on first run
│   ├── Main.java                 # Console interface (testing)
│   │
│   ├── models/
│   │   ├── Student.java          # Student identity + contact info
│   │   ├── Resource.java         # SELL / RENT / DIGITAL listings
│   │   └── Request.java          # BUY / RENT requests with priority score
│   │
│   ├── algorithms/
│   │   ├── PriorityScheduler.java # Max Heap + Interval Scheduling
│   │   └── ExchangeGraph.java     # Directed Graph + DFS Cycle Detection
│   │
│   ├── engine/
│   │   └── AllocationEngine.java  # Central controller + DB integration
│   │
│   └── database/
│       └── DatabaseManager.java   # SQLite CRUD operations
│
├── lib/
│   └── sqlite-jdbc.jar           # SQLite JDBC driver
│
└── campus_exchange.db            # Auto-created on first run
```

### Layer Separation
```
Presentation  →  MainApp.java (JavaFX)
Controller    →  AllocationEngine.java
Algorithms    →  PriorityScheduler + ExchangeGraph
Models        →  Student, Resource, Request
Persistence   →  DatabaseManager (SQLite)
```

---

## ⚙ Setup

### Prerequisites
- Java 25+
- JavaFX SDK 21.0.10
- SQLite JDBC 3.36.0.3+

### 1. Clone the Repository
```bash
git clone https://github.com/Nikshayy08/ChronoWeave-Intelligent-Allocation-Engine.git
cd ChronoWeave-Intelligent-Allocation-Engine
```

### 2. Download SQLite JDBC
Download from:
```
https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.36.0.3/sqlite-jdbc-3.36.0.3.jar
```
Place as `lib/sqlite-jdbc.jar`

### 3. Download JavaFX SDK
Download JavaFX 21 LTS from https://gluonhq.com/products/javafx/
Extract to `C:\javafx-sdk-21\`

### 4. Compile
```bash
javac --module-path "C:\javafx-sdk-21\javafx-sdk-21.0.10\lib" \
      --add-modules javafx.controls,javafx.fxml \
      -cp "lib/sqlite-jdbc.jar" \
      -d bin \
      src/models/*.java src/algorithms/*.java src/database/*.java \
      src/engine/*.java src/DataSeeder.java src/MainApp.java src/Main.java
```

### 5. Run
```bash
java --module-path "C:\javafx-sdk-21\javafx-sdk-21.0.10\lib" \
     --add-modules javafx.controls,javafx.fxml \
     -cp "bin;lib/sqlite-jdbc.jar" \
     MainApp
```

---

## 🎬 Demo Flow

**On first launch**, the system auto-seeds 4 students and 6 resources:

| Student | Role |
|---------|------|
| Nikshay Joshi | Lists Drafter Set (SELL) + DS PYQ (DIGITAL) |
| Shagun Sharma | Lists Drawing Kit (SELL) + OOPs Notes (DIGITAL) |
| Dhruv Pathak | Lists Scientific Calculator (RENT) |
| Kailash Singh | Lists Lab Coat (RENT) |

### Full Demo Sequence

```
1. Dashboard    → See live stats (Students / Listings / Requests / Digital)

2. Students     → Register new student with contact + WhatsApp

3. Listings     → Add SELL / RENT / DIGITAL resource

4. Requests     → Post buy request (budget)
               → Post rent request (time slot + offering price)
               → Click "Run Buy Matching" → contacts revealed

5. Rent Match   → Click "Run Rent Matching"
               → See priority scheduling in action
               → Conflicts shown with next available slot

6. Buy Exchange → Click "Detect Exchange Cycle"
               → If cycle found → both contacts revealed
               → No money needed for direct swap

7. Digital      → Browse free notes/PYQs with Google Drive links
```

---

## 🖥 UI Screenshots

> Dark theme · Sidebar navigation · Live data tables · Monospace output areas

| Screen | Description |
|--------|-------------|
| Dashboard | Live counters + DB connection status |
| Students | Register form + sortable table |
| Listings | SELL/RENT/DIGITAL with type-aware fields |
| Requests | Buy + Rent forms + matching engine |
| Rent Match | Priority queue output with contact reveal |
| Buy Exchange | DFS cycle visualization with contacts |
| Digital | Card-based resource browser with links |

---

## 📊 Time Complexity Summary

| Algorithm | Operation | Complexity |
|-----------|-----------|------------|
| Priority Queue (Heap) | Insert / Extract | O(log n) |
| Heap Build | All n requests | O(n log n) |
| Interval Conflict Check | Per request | O(n) |
| DFS Cycle Detection | Full graph | O(V + E) |
| Buy Matching | Per request | O(m) — m = listings |

---

## 👥 Team

| Member | Role |
|--------|------|
| **Nikshay Joshi** | Team Lead · Backend · Algorithms · DB Integration · JavaFX |
| **Shagun Sharma** | UI Design · Leaderboard Screen |
| **Jatin** | Testing · Allocation Results Screen |
| **Kailash Singh** | Documentation · Demo Data · Presentation |

---

## 📁 Tech Stack

```
Language    →  Java 25
UI          →  JavaFX 21.0.10
Database    →  SQLite (via JDBC 3.36.0.3)
IDE         →  VS Code
Platform    →  Windows
Build       →  Manual javac (no Maven/Gradle)
```

---

## 📄 License

This project is built for academic purposes as part of a PBL (Project Based Learning) submission.

---

<div align="center">

**Built with Java · Powered by DAA · Designed for real campus use**

⭐ Star this repo if you found it useful!

</div>
