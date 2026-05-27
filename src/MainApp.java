import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;

import engine.AllocationEngine;
import models.Request;
import models.Resource;
import models.Student;
import database.DatabaseManager;

import java.util.List;

import algorithms.ExchangeGraph;

public class MainApp extends Application {

    private static final String BG_DARK      = "#0f0f13";
    private static final String BG_CARD      = "#1a1a24";
    private static final String BG_SIDEBAR   = "#13131a";
    private static final String BG_INPUT     = "#22222e";
    private static final String ACCENT       = "#6c63ff";
    private static final String TEXT_PRIMARY = "#ffffff";
    private static final String TEXT_MUTED   = "#8888aa";
    private static final String SUCCESS      = "#22c55e";
    private static final String WARNING      = "#f59e0b";
    private static final String DANGER       = "#ef4444";
    private static final String BORDER       = "#2a2a3a";

    private DatabaseManager  db     = new DatabaseManager();
    private AllocationEngine engine = new AllocationEngine(db);
    private StackPane contentArea   = new StackPane();

    @Override
    public void start(Stage stage) {

        // Load persisted data from DB
        engine.loadFromDatabase();

        // Seed demo data only if DB is empty
        DataSeeder.seed(engine, db);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + BG_DARK + ";");
        root.setLeft(buildSidebar());
        root.setCenter(contentArea);
        contentArea.setStyle("-fx-background-color:" + BG_DARK + ";");
        contentArea.setAlignment(Pos.TOP_LEFT);
        showDashboard();

        Scene scene = new Scene(root, 1100, 700);
        stage.setTitle("Smart Campus Resource Exchange");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        // Close DB connection on exit
        stage.setOnCloseRequest(e -> db.close());

        stage.show();
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sb = new VBox(0);
        sb.setPrefWidth(210);
        sb.setStyle("-fx-background-color:" + BG_SIDEBAR + "; -fx-border-color:" + BORDER + "; -fx-border-width:0 1 0 0;");

        VBox logo = new VBox(4);
        logo.setPadding(new Insets(24, 20, 20, 20));
        logo.setStyle("-fx-border-color:" + BORDER + "; -fx-border-width:0 0 1 0;");
        Label t = new Label("CampusXchange");
        t.setStyle("-fx-text-fill:" + TEXT_PRIMARY + "; -fx-font-size:17px; -fx-font-weight:bold;");
        Label s = new Label("Resource Exchange System");
        s.setStyle("-fx-text-fill:" + TEXT_MUTED + "; -fx-font-size:10px;");
        logo.getChildren().addAll(t, s);

        VBox nav = new VBox(2);
        nav.setPadding(new Insets(12, 8, 8, 8));
        nav.getChildren().addAll(
            navBtn("  Dashboard",    () -> showDashboard()),
            navBtn("  Students",     () -> showStudents()),
            navBtn("  Listings",     () -> showListings()),
            navBtn("  Requests",     () -> showRequests()),
            navBtn("  Rent Match",   () -> showRentMatching()),
            navBtn("  Buy Exchange", () -> showExchangeCycle()),
            navBtn("  Digital",      () -> showDigital())
        );

        VBox bottom = new VBox();
        bottom.setPadding(new Insets(10, 8, 16, 8));
        Button exit = new Button("  Exit");
        exit.setMaxWidth(Double.MAX_VALUE);
        exit.setStyle("-fx-background-color:" + DANGER + "; -fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:13px; -fx-padding:10 20 10 20; -fx-background-radius:6; -fx-cursor:hand;");
        exit.setOnAction(e -> { db.close(); Platform.exit(); });
        bottom.getChildren().add(exit);

        VBox.setVgrow(nav, Priority.ALWAYS);
        sb.getChildren().addAll(logo, nav, bottom);
        return sb;
    }

    private Button navBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 16, 10, 16));
        String normal = "-fx-background-color:transparent; -fx-text-fill:" + TEXT_MUTED + "; -fx-font-size:13px; -fx-cursor:hand; -fx-background-radius:6;";
        String hover  = "-fx-background-color:" + BG_CARD + "; -fx-text-fill:" + TEXT_PRIMARY + "; -fx-font-size:13px; -fx-cursor:hand; -fx-background-radius:6;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(normal));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    // ── Page builder ──────────────────────────────────────────────────────────

    private VBox buildPage(String title, javafx.scene.Node... nodes) {
        VBox header = new VBox(6);
        header.setPadding(new Insets(28, 30, 16, 30));
        header.setStyle("-fx-background-color:" + BG_DARK + ";");
        Label tl = new Label(title);
        tl.setStyle("-fx-text-fill:" + TEXT_PRIMARY + "; -fx-font-size:22px; -fx-font-weight:bold;");
        Rectangle bar = new Rectangle(60, 3, Color.web(ACCENT));
        header.getChildren().addAll(tl, bar);

        VBox content = new VBox(16);
        content.setPadding(new Insets(16, 30, 30, 30));
        content.setStyle("-fx-background-color:" + BG_DARK + ";");
        content.getChildren().addAll(nodes);

        VBox full = new VBox(0);
        full.setStyle("-fx-background-color:" + BG_DARK + ";");
        full.setFillWidth(true);
        full.getChildren().addAll(header, content);
        return full;
    }

    private void show(javafx.scene.Node n) {
        ScrollPane sp = new ScrollPane(n);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setStyle("-fx-background:transparent; -fx-background-color:transparent;");
        sp.setMaxWidth(Double.MAX_VALUE);
        sp.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(sp, Priority.ALWAYS);
        contentArea.getChildren().setAll(sp);
    }

    // ── UI Components ─────────────────────────────────────────────────────────

    private VBox card(String title) {
        VBox c = new VBox(12);
        c.setPadding(new Insets(20));
        c.setStyle("-fx-background-color:" + BG_CARD + "; -fx-background-radius:10; -fx-border-color:" + BORDER + "; -fx-border-radius:10; -fx-border-width:1;");
        if (title != null) {
            Label l = new Label(title);
            l.setStyle("-fx-text-fill:" + TEXT_MUTED + "; -fx-font-size:11px; -fx-font-weight:bold;");
            c.getChildren().add(l);
        }
        return c;
    }

    private TextField input(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color:" + BG_INPUT + "; -fx-text-fill:" + TEXT_PRIMARY + "; -fx-prompt-text-fill:" + TEXT_MUTED + "; -fx-border-color:" + BORDER + "; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:8 12 8 12; -fx-font-size:13px;");
        return tf;
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + TEXT_MUTED + "; -fx-font-size:12px;");
        return l;
    }

    private Label status() {
        Label l = new Label("");
        l.setStyle("-fx-font-size:12px;");
        l.setWrapText(true);
        return l;
    }

    private void ok(Label l, String msg)  { l.setStyle("-fx-text-fill:" + SUCCESS + "; -fx-font-size:12px;"); l.setText(msg); }
    private void err(Label l, String msg) { l.setStyle("-fx-text-fill:" + DANGER  + "; -fx-font-size:12px;"); l.setText(msg); }

    private Button btn(String text, String color) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("-fx-background-color:" + color + "; -fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:13px; -fx-padding:10 20 10 20; -fx-background-radius:6; -fx-cursor:hand;");
        return b;
    }

    private TextArea outputBox(String textColor) {
        TextArea ta = new TextArea();
        ta.setEditable(false);
        ta.setPrefHeight(300);
        ta.setStyle(
            "-fx-control-inner-background:#0a0a12;" +
            "-fx-text-fill:" + textColor + ";" +
            "-fx-font-family:monospace;" +
            "-fx-font-size:13px;" +
            "-fx-background-color:#0a0a12;" +
            "-fx-border-color:" + BORDER + ";" +
            "-fx-border-radius:6;" +
            "-fx-background-radius:6;"
        );
        return ta;
    }

    // ── Styled TableView ──────────────────────────────────────────────────────

    private <T> TableView<T> styledTable() {
        TableView<T> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setStyle(
            "-fx-background-color:" + BG_INPUT + ";" +
            "-fx-table-cell-border-color:" + BORDER + ";" +
            "-fx-control-inner-background:" + BG_INPUT + ";"
        );
        tv.widthProperty().addListener((obs, oldVal, newVal) -> {
            javafx.scene.Node header = tv.lookup("TableHeaderRow");
            if (header != null) header.setStyle("-fx-background-color:" + BG_CARD + ";");
        });
        tv.setPrefHeight(300);
        tv.setRowFactory(rv -> {
            TableRow<T> row = new TableRow<>();
            row.setStyle("-fx-background-color:" + BG_INPUT + ";");
            row.selectedProperty().addListener((obs, was, is) -> {
                if (is) row.setStyle("-fx-background-color:" + ACCENT + ";");
                else    row.setStyle("-fx-background-color:" + BG_INPUT + ";");
            });
            return row;
        });
        Label empty = new Label("No data yet.");
        empty.setStyle("-fx-text-fill:" + TEXT_MUTED + ";");
        tv.setPlaceholder(empty);
        return tv;
    }

    @SuppressWarnings("unchecked")
    private <T> TableColumn<T, Object> col(String title, String prop, double w) {
        TableColumn<T, Object> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w);
        c.setStyle("-fx-text-fill:#ffffff; -fx-font-weight:bold; -fx-font-size:13px;");
        c.setCellFactory(col2 -> new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color:" + BG_INPUT + ";");
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill:white; -fx-font-size:13px; -fx-background-color:" + BG_INPUT + ";");
                }
            }
        });
        return c;
    }

    // ── 1. Dashboard ──────────────────────────────────────────────────────────

    private void showDashboard() {
        HBox stats = new HBox(16);
        stats.getChildren().addAll(
            statCard("Students",  String.valueOf(engine.getAllStudents().size()),       ACCENT),
            statCard("Listings",  String.valueOf(engine.getAllResources().size()),       SUCCESS),
            statCard("Requests",  String.valueOf(engine.getAllRequests().size()),        WARNING),
            statCard("Digital",   String.valueOf(engine.getDigitalResources().size()),  "#06b6d4")
        );

        VBox info = card("HOW IT WORKS");
        String[] steps = {
            "1.  Register students with contact details",
            "2.  List resources for SELL, RENT or DIGITAL",
            "3.  Post buy or rent requests",
            "4.  Run matching — contacts revealed on match",
            "5.  Students meet in person to complete deal"
        };
        for (String step : steps) {
            Label l = new Label(step);
            l.setStyle("-fx-text-fill:" + TEXT_PRIMARY + "; -fx-font-size:13px;");
            info.getChildren().add(l);
        }

        VBox dbStatus = card("DATABASE STATUS");
        Label dbLabel = new Label(db.isConnected()
            ? "SQLite Connected — data persists between sessions"
            : "Database offline — data resets on close");
        dbLabel.setStyle("-fx-text-fill:" + (db.isConnected() ? SUCCESS : WARNING) + "; -fx-font-size:13px;");
        dbStatus.getChildren().add(dbLabel);

        show(buildPage("Dashboard", stats, info, dbStatus));
    }

    private VBox statCard(String label, String value, String color) {
        VBox c = new VBox(6);
        c.setPadding(new Insets(20));
        c.setPrefWidth(180);
        c.setStyle("-fx-background-color:" + BG_CARD + "; -fx-background-radius:10; -fx-border-color:" + color + "; -fx-border-radius:10; -fx-border-width:1;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill:" + color + "; -fx-font-size:28px; -fx-font-weight:bold;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:" + TEXT_MUTED + "; -fx-font-size:12px;");
        c.getChildren().addAll(val, lbl);
        return c;
    }

    // ── 2. Students ───────────────────────────────────────────────────────────

    private void showStudents() {
        VBox form = card("REGISTER STUDENT");
        form.setPrefWidth(300);
        form.setMinWidth(280);

        TextField idF  = input("Student ID  e.g. S001");
        TextField nmF  = input("Full Name");
        TextField phF  = input("Contact Number");
        TextField waF  = input("WhatsApp Number");
        Label msg      = status();
        Button addBtn  = btn("Register Student", ACCENT);

        addBtn.setOnAction(e -> {
            if (idF.getText().trim().isEmpty() || nmF.getText().trim().isEmpty()) {
                err(msg, "ID and Name are required."); return;
            }
            engine.addStudent(new Student(
                idF.getText().trim(), nmF.getText().trim(),
                phF.getText().trim(), waF.getText().trim()
            ));
            ok(msg, "Registered: " + nmF.getText().trim());
            idF.clear(); nmF.clear(); phF.clear(); waF.clear();
            showStudents();
        });

        form.getChildren().addAll(lbl("Student ID"), idF, lbl("Full Name"), nmF,
            lbl("Contact"), phF, lbl("WhatsApp"), waF, addBtn, msg);

        VBox tableCard = card("ALL STUDENTS");
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        TableView<Student> tv = styledTable();
        tv.getColumns().addAll(
            col("ID",       "studentId",      90),
            col("Name",     "name",           150),
            col("Contact",  "contactNumber",  130),
            col("WhatsApp", "whatsappNumber", 130),
            col("Listings", "totalListings",  80),
            col("Requests", "totalRequests",  80)
        );
        tv.getItems().addAll(engine.getAllStudents().values());
        tableCard.getChildren().add(tv);

        HBox layout = new HBox(20, form, tableCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        show(buildPage("Students", layout));
    }

    // ── 3. Listings ───────────────────────────────────────────────────────────

    private void showListings() {
        VBox form = card("ADD LISTING");
        form.setPrefWidth(300);
        form.setMinWidth(280);

        TextField ridF   = input("Resource ID  e.g. R001");
        TextField rnF    = input("Resource Name");
        TextField ownerF = input("Owner (student name)");
        TextField priceF = input("Asking Price (Rs.)");
        TextField linkF  = input("Google Drive Link");
        linkF.setDisable(true);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("SELL", "RENT", "DIGITAL");
        typeBox.setPromptText("Select Type");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setStyle("-fx-background-color:" + BG_INPUT + "; -fx-text-fill:" + TEXT_PRIMARY + "; -fx-border-color:" + BORDER + "; -fx-border-radius:6; -fx-background-radius:6;");
        typeBox.setOnAction(e -> {
            boolean dig = "DIGITAL".equals(typeBox.getValue());
            priceF.setDisable(dig);
            linkF.setDisable(!dig);
        });

        Label msg     = status();
        Button addBtn = btn("Add Listing", SUCCESS);
        addBtn.setOnAction(e -> {
            if (ridF.getText().trim().isEmpty() || rnF.getText().trim().isEmpty()
                || ownerF.getText().trim().isEmpty() || typeBox.getValue() == null) {
                err(msg, "All fields required."); return;
            }
            Resource.ListingType lt = Resource.ListingType.valueOf(typeBox.getValue());
            Resource.ResourceType rt = lt == Resource.ListingType.DIGITAL
                ? Resource.ResourceType.DIGITAL : Resource.ResourceType.PHYSICAL;
            Resource res = new Resource(ridF.getText().trim(), rnF.getText().trim(), rt, lt, ownerF.getText().trim());
            if (lt != Resource.ListingType.DIGITAL) {
                try { res.setAskingPrice(Double.parseDouble(priceF.getText().trim())); } catch (Exception ex) {}
            } else {
                res.setDownloadLink(linkF.getText().trim());
            }
            engine.addResource(res);
            ok(msg, "Listed: " + rnF.getText().trim());
            ridF.clear(); rnF.clear(); ownerF.clear(); priceF.clear(); linkF.clear(); typeBox.setValue(null);
            showListings();
        });

        form.getChildren().addAll(lbl("Resource ID"), ridF, lbl("Name"), rnF,
            lbl("Type"), typeBox, lbl("Owner"), ownerF,
            lbl("Price (Rs.)"), priceF, lbl("Drive Link (Digital only)"), linkF,
            addBtn, msg);

        VBox tableCard = card("ALL LISTINGS");
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        TableView<Resource> tv = styledTable();
        tv.getColumns().addAll(
            col("ID",     "resourceId",   70),
            col("Name",   "resourceName", 140),
            col("Type",   "listingType",  80),
            col("Owner",  "ownedBy",      120),
            col("Price",  "askingPrice",  80),
            col("Link",   "downloadLink", 160),
            col("Status", "available",    70)
        );
        tv.getItems().addAll(engine.getAllResources().values());
        tableCard.getChildren().add(tv);

        HBox layout = new HBox(20, form, tableCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        show(buildPage("Resource Listings", layout));
    }

    // ── 4. Requests ───────────────────────────────────────────────────────────

    private void showRequests() {
        // Buy form
        VBox buyCard = card("POST BUY REQUEST");
        buyCard.setPrefWidth(280);
        buyCard.setMinWidth(260);
        TextField bn = input("Your Name");
        TextField br = input("Resource Wanted");
        TextField bb = input("Budget (Rs.)");
        Label bMsg = status();
        Button bBtn = btn("Post Buy Request", ACCENT);
        bBtn.setOnAction(e -> {
            if (bn.getText().trim().isEmpty() || br.getText().trim().isEmpty() || bb.getText().trim().isEmpty()) {
                err(bMsg, "All fields required."); return;
            }
            try {
                boolean ok = engine.addRequest(new Request(
                    bn.getText().trim(), br.getText().trim(), Double.parseDouble(bb.getText().trim())));
                if (ok) { ok(bMsg, "Buy request posted."); bn.clear(); br.clear(); bb.clear(); showRequests(); }
                else    { err(bMsg, "Student not found. Register first."); }
            } catch (NumberFormatException ex) { err(bMsg, "Invalid budget."); }
        });
        buyCard.getChildren().addAll(lbl("Your Name"), bn, lbl("Resource Wanted"), br, lbl("Budget (Rs.)"), bb, bBtn, bMsg);

        // Rent form
        VBox rentCard = card("POST RENT REQUEST");
        rentCard.setPrefWidth(280);
        rentCard.setMinWidth(260);
        TextField rn = input("Your Name");
        TextField rr = input("Resource Wanted");
        TextField ro = input("Offering (Rs.)");
        TextField rs = input("Start Time (int)");
        TextField re = input("End Time (int)");
        Label rMsg = status();
        Button rBtn = btn("Post Rent Request", WARNING);
        rBtn.setOnAction(e -> {
            if (rn.getText().trim().isEmpty() || rr.getText().trim().isEmpty()
                || ro.getText().trim().isEmpty() || rs.getText().trim().isEmpty() || re.getText().trim().isEmpty()) {
                err(rMsg, "All fields required."); return;
            }
            try {
                boolean ok = engine.addRequest(new Request(
                    rn.getText().trim(), rr.getText().trim(),
                    Request.RequestType.NEED_TO_RENT,
                    Double.parseDouble(ro.getText().trim()),
                    Integer.parseInt(rs.getText().trim()),
                    Integer.parseInt(re.getText().trim())));
                if (ok) { ok(rMsg, "Rent request posted."); rn.clear(); rr.clear(); ro.clear(); rs.clear(); re.clear(); showRequests(); }
                else    { err(rMsg, "Student not found. Register first."); }
            } catch (NumberFormatException ex) { err(rMsg, "Invalid number."); }
        });
        rentCard.getChildren().addAll(lbl("Your Name"), rn, lbl("Resource Wanted"), rr,
            lbl("Offering (Rs.)"), ro, lbl("Start Time"), rs, lbl("End Time"), re, rBtn, rMsg);

        // Requests table
        VBox tableCard = card("ALL REQUESTS");
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        TableView<Request> tv = styledTable();
        tv.getColumns().addAll(
            col("Student",  "studentName",   120),
            col("Resource", "resourceName",  130),
            col("Type",     "requestType",   110),
            col("Offering", "offeringPrice", 90),
            col("Start",    "startTime",     60),
            col("End",      "endTime",       60),
            col("Status",   "status",        100)
        );
        tv.getItems().addAll(engine.getAllRequests());
        tableCard.getChildren().add(tv);

        // Buy matching result
        VBox matchCard = card("BUY MATCHING RESULT");
        TextArea buyOut = outputBox(SUCCESS);
        buyOut.setText("Click 'Run Buy Matching' to find sellers for all pending buy requests...");
        Button buyMatchBtn = btn("Run Buy Matching", SUCCESS);
        buyMatchBtn.setOnAction(e -> {
            List<String> results = engine.runBuyMatching();
            StringBuilder sb = new StringBuilder("=== BUY MATCHING RESULTS ===\n\n");
            for (String r : results) sb.append(r).append("\n");
            buyOut.setText(sb.toString());
        });
        matchCard.getChildren().addAll(buyMatchBtn, buyOut);

        VBox forms = new VBox(16, buyCard, rentCard);
        HBox layout = new HBox(20, forms, tableCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        show(buildPage("Requests", forms, tableCard, matchCard));
    }

    // ── 5. Rent Matching ──────────────────────────────────────────────────────

    private void showRentMatching() {
        VBox card = card("RUN MATCHING ENGINE");
        Label info = new Label("Matches RENT requests using Priority Queue (Max Heap).\nHigher offering price = higher priority. Conflicts auto-detected.");
        info.setStyle("-fx-text-fill:" + TEXT_MUTED + "; -fx-font-size:13px;");
        info.setWrapText(true);

        TextArea out = outputBox(SUCCESS);
        out.setText("Press 'Run Rent Matching' to process all rent requests...");

        Button runBtn = btn("Run Rent Matching", SUCCESS);
        runBtn.setOnAction(e -> {
            List<Request> results = engine.runRentAllocation();
            if (results.isEmpty()) { out.setText("No rent requests to process."); return; }
            StringBuilder sb = new StringBuilder("=== RENT MATCHING RESULTS ===\n\n");
            for (Request r : results) {
                sb.append(r).append("\n");
                if (r.getStatus() == Request.Status.MATCHED) {
                    for (Resource res : engine.getAllResources().values()) {
                        if (res.getResourceName().equals(r.getResourceName())) {
                            Student owner = engine.getStudent(res.getOwnedBy());
                            if (owner != null) {
                                sb.append("  -> MATCH FOUND\n");
                                sb.append("     Owner    : ").append(owner.getName()).append("\n");
                                sb.append("     Phone    : ").append(owner.getContactNumber()).append("\n");
                                sb.append("     WhatsApp : ").append(owner.getWhatsappNumber()).append("\n");
                                sb.append("     Action   : Meet in person.\n");
                            }
                            break;
                        }
                    }
                } else if (r.getStatus() == Request.Status.WAITLISTED) {
                    int next = engine.suggestNextAvailableSlot(r.getResourceName());
                    sb.append("  -> WAITLISTED. Next free slot from time: ").append(next).append("\n");
                }
                sb.append("\n");
            }
            out.setText(sb.toString());
        });

        card.getChildren().addAll(info, runBtn, out);
        show(buildPage("Rent Matching", card));
    }

    // ── 6. Exchange Cycle ─────────────────────────────────────────────────────

    private void showExchangeCycle() {
        VBox card = card("MUTUAL SWAP DETECTION");
        Label info = new Label("Detects if two or more students can swap resources directly.\nUses DFS cycle detection on directed graph. No money needed for swaps.");
        info.setStyle("-fx-text-fill:" + TEXT_MUTED + "; -fx-font-size:13px;");
        info.setWrapText(true);

        TextArea out = outputBox(ACCENT);
        out.setText("Press 'Detect Exchange Cycle' to run graph analysis...");

        Button detectBtn = btn("Detect Exchange Cycle", ACCENT);
        detectBtn.setOnAction(e -> {
            // boolean cycle = engine.checkExchangeCycle();
            


            ExchangeGraph graph = engine.getExchangeGraph();
            boolean cycle = graph.hasCycle();



            StringBuilder sb = new StringBuilder("=== EXCHANGE CYCLE DETECTION ===\n\n");
            if (!cycle) {
                sb.append("No exchange cycle found.\n\nNo mutual swaps possible with current requests.");
            } else {
                // List<String> nodes = engine.getExchangeGraph().getDetectedCycle();

                List<String> nodes = graph.getDetectedCycle();


                sb.append("CYCLE DETECTED!\n");
                sb.append("Path: ").append(String.join(" -> ", nodes)).append("\n\n");
                sb.append("These students can swap directly — no money needed.\n\n");
                sb.append("=== CONTACTS ===\n\n");
                for (String sname : nodes) {
                    Student s = engine.getStudent(sname);
                    if (s != null) {
                        sb.append("Name     : ").append(s.getName()).append("\n");
                        sb.append("Phone    : ").append(s.getContactNumber()).append("\n");
                        sb.append("WhatsApp : ").append(s.getWhatsappNumber()).append("\n\n");
                    }
                }
            }
            out.setText(sb.toString());
        });

        card.getChildren().addAll(info, detectBtn, out);
        show(buildPage("Buy Exchange Cycle", card));
    }

    // ── 7. Digital ────────────────────────────────────────────────────────────

    private void showDigital() {
        VBox listCard = card("AVAILABLE DIGITAL RESOURCES");
        List<Resource> digital = engine.getDigitalResources();

        if (digital.isEmpty()) {
            Label empty = new Label("No digital resources listed yet. Add one via Listings → DIGITAL type.");
            empty.setStyle("-fx-text-fill:" + TEXT_MUTED + "; -fx-font-size:13px;");
            listCard.getChildren().add(empty);
        } else {
            for (Resource r : digital) {
                HBox row = new HBox(16);
                row.setPadding(new Insets(14));
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color:" + BG_INPUT + "; -fx-background-radius:8;");

                VBox info = new VBox(4);
                HBox.setHgrow(info, Priority.ALWAYS);
                Label name  = new Label(r.getResourceName());
                name.setStyle("-fx-text-fill:" + TEXT_PRIMARY + "; -fx-font-size:14px; -fx-font-weight:bold;");
                Label owner = new Label("Uploaded by: " + r.getOwnedBy());
                owner.setStyle("-fx-text-fill:" + TEXT_MUTED + "; -fx-font-size:12px;");
                info.getChildren().addAll(name, owner);

                Label link = new Label(r.getDownloadLink() != null ? r.getDownloadLink() : "No link provided");
                link.setStyle("-fx-text-fill:#06b6d4; -fx-font-size:12px;");

                row.getChildren().addAll(info, link);
                listCard.getChildren().add(row);
            }
        }

        Label note = new Label("Free access — copy the link and open in browser. No payment needed.");
        note.setStyle("-fx-text-fill:" + TEXT_MUTED + "; -fx-font-size:13px;");
        show(buildPage("Digital Resources", note, listCard));
    }

    public static void main(String[] args) { launch(args); }
}