package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CacheSimulatorGUI extends JFrame {

    private CacheMemory cache;
    private Map<Integer, String> mainMemory;
    private StatsManager statsManager;

    private JTable cacheTable;
    private DefaultTableModel tableModel;
    private JTextArea logArea;
    private JLabel statsLabel;
    private JTextField addressField;

    private CacheCellRenderer renderer;

    private JComboBox<Integer> sizeCombo;
    private JComboBox<String> mappingCombo;
    private JComboBox<String> replacementCombo;
    private JComboBox<String> writeCombo;

    private final String LOG_FILE = "cache_log.txt";
    private final String STATS_FILE = "cache_statistics.txt";

    public CacheSimulatorGUI() {
        setTitle("Simulator Memorie Cache");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        cache = new CacheMemory(8);
        mainMemory = new HashMap<>();
        statsManager = new StatsManager();
        statsManager.setStatsFilePath(STATS_FILE);

        for (int i = 0; i < 50; i++) {
            mainMemory.put(i, "Date_" + i);
        }

        initComponents();
        updateCacheDisplay();
        updateStats();

        log("=== SIMULATOR MEMORIE CACHE ===");
        log("Cache inițializat: 8 linii, mapare directă");
        log("Fișier log: " + LOG_FILE);
        log("Fișier statistici: " + STATS_FILE);
        log("");

        setLocationRelativeTo(null);
    }

    private void initComponents() {

        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Control"));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));


        topRow.add(new JLabel("Adresă Memorie:"));
        addressField = new JTextField(10);
        topRow.add(addressField);

        JButton readButton = new JButton("Citire");
        readButton.setFont(new Font("Arial", Font.BOLD, 14));
        readButton.addActionListener(e -> readOperation());
        topRow.add(readButton);

        JButton writeButton = new JButton("Scriere");
        writeButton.setFont(new Font("Arial", Font.BOLD, 14));
        writeButton.addActionListener(e -> writeOperation());
        topRow.add(writeButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetCache());
        topRow.add(resetButton);

        JButton saveButton = new JButton("Salvare Stats");
        saveButton.addActionListener(e -> saveStatistics());
        topRow.add(saveButton);

        sizeCombo = new JComboBox<>(new Integer[]{4, 8, 16});
        sizeCombo.addActionListener(e -> changeCacheSize());
        bottomRow.add(new JLabel("Dimensiune cache:"));
        bottomRow.add(sizeCombo);

        mappingCombo = new JComboBox<>(new String[]{"Direct", "Set-Associative", "Fully Associative"});
        mappingCombo.addActionListener(e -> changeMappingType());
        bottomRow.add(new JLabel("Mapare:"));
        bottomRow.add(mappingCombo);

        replacementCombo = new JComboBox<>(new String[]{"LRU", "FIFO", "Random"});
        replacementCombo.addActionListener(e -> changeReplacementPolicy());
        bottomRow.add(new JLabel("Înlocuire:"));
        bottomRow.add(replacementCombo);

        writeCombo = new JComboBox<>(new String[]{"Write-Back", "Write-Through"});
        writeCombo.addActionListener(e -> changeWritePolicy());
        bottomRow.add(new JLabel("Scriere:"));
        bottomRow.add(writeCombo);

        controlPanel.add(topRow);
        controlPanel.add(bottomRow);
        add(controlPanel, BorderLayout.NORTH);

        String[] columns = {"Index", "Valid", "Tag", "Dirty", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        renderer = new CacheCellRenderer();
        cacheTable = new JTable(tableModel);
        cacheTable.setRowHeight(30);
        cacheTable.setFont(new Font("Monospaced", Font.PLAIN, 13));
        cacheTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        cacheTable.setDefaultRenderer(Object.class, renderer);

        JScrollPane tableScroll = new JScrollPane(cacheTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Memorie Cache"));
        add(tableScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        statsLabel = new JLabel("Hits: 0 | Misses: 0 | Total: 0 | Hit Rate: 0.00%");
        statsLabel.setBorder(BorderFactory.createTitledBorder("Statistici Performanță"));
        statsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statsLabel.setForeground(new Color(0, 100, 0));
        bottomPanel.add(statsLabel, BorderLayout.NORTH);

        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Log Operații"));
        bottomPanel.add(logScroll, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }


    private void readOperation() {
        try {
            int address = Integer.parseInt(addressField.getText());
            if (address < 0 || address > 49) {
                JOptionPane.showMessageDialog(this, "Adresa trebuie să fie între 0 și 49!");
                return;
            }

            log("─────────────────────────────");
            log("CITIRE din adresa: " + address);

            statsManager.recordRead();

            int hitIndex = cache.checkHit(address);

            if (hitIndex != -1) {
                statsManager.recordHit();
                renderer.updateHighlight(hitIndex, "hit");

                String data = cache.getData(address);
                log(" ✓ CACHE HIT la linia " + hitIndex);
                log("  Date găsite: " + data);
            } else {
                statsManager.recordMiss();

                String data = mainMemory.get(address);
                cache.placeLine(address, data, mainMemory);

                int newIndex = cache.checkHit(address);
                renderer.updateHighlight(newIndex, "miss");

                log(" ✗ CACHE MISS");
                log("  Date încărcate din RAM: " + data);
                if(newIndex != -1) {
                    log("  Plasate la linia: " + newIndex);
                }
            }

            updateCacheDisplay();
            updateStats();
            saveLogToFile();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Introduceți un număr valid!");
        }
    }


    private void writeOperation() {
        try {
            int address = Integer.parseInt(addressField.getText());
            if (address < 0 || address > 49) {
                JOptionPane.showMessageDialog(this, "Adresa trebuie să fie între 0 și 49!");
                return;
            }

            String newData = "Date_" + address + "_modificate";

            log("─────────────────────────────");
            log("SCRIERE la adresa: " + address);

            statsManager.recordWrite();

            boolean wasHit = cache.isHit(address);

            cache.writeLine(address, newData, mainMemory);

            if(wasHit) {
                statsManager.recordHit();
            } else {
                statsManager.recordMiss();
            }

            int lineIndex = cache.checkHit(address);
            renderer.updateHighlight(lineIndex, "hit");

            log(" Date scrise în cache: " + newData);
            if(lineIndex != -1) {
                log("  La linia: " + lineIndex);
            }

            updateCacheDisplay();
            updateStats();
            saveLogToFile();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Introduceți un număr valid!");
        }
    }


    private void resetCache() {
        int size = (Integer) sizeCombo.getSelectedItem();
        cache = new CacheMemory(size);
        statsManager.reset();
        renderer.updateHighlight(-1, "");

        changeMappingType();
        changeReplacementPolicy();
        changeWritePolicy();

        updateCacheDisplay();
        updateStats();

        log("=== CACHE RESETAT ===");
        log("Toate liniile au fost golite");
        log("Statistici resetate");
        log("");

        saveLogToFile();
    }


    private void changeCacheSize() {
        int newSize = (Integer) sizeCombo.getSelectedItem();
        cache = new CacheMemory(newSize);
        statsManager.reset();

        changeMappingType();
        changeReplacementPolicy();
        changeWritePolicy();

        updateCacheDisplay();
        log("Cache resetat la " + newSize + " linii");
        saveLogToFile();
    }


    private void changeMappingType() {
        String type = (String) mappingCombo.getSelectedItem();
        if (type.equals("Direct")) {
            cache.setMappingType(CacheMemory.MappingType.DIRECT, 1);
        } else if (type.equals("Set-Associative")) {
            cache.setMappingType(CacheMemory.MappingType.SET_ASSOCIATIVE, 2);
        } else {
            cache.setMappingType(CacheMemory.MappingType.FULLY_ASSOCIATIVE, 1);
        }
        log("Tip mapare: " + type);
        saveLogToFile();
    }

    private void changeReplacementPolicy() {
        String policy = (String) replacementCombo.getSelectedItem();
        if (policy.equals("LRU")) {
            cache.setReplacementPolicy(CacheMemory.ReplacementPolicy.LRU);
        } else if (policy.equals("FIFO")) {
            cache.setReplacementPolicy(CacheMemory.ReplacementPolicy.FIFO);
        } else {
            cache.setReplacementPolicy(CacheMemory.ReplacementPolicy.RANDOM);
        }
        log("Politica de înlocuire: " + policy);
        saveLogToFile();
    }


    private void changeWritePolicy() {
        String policy = (String) writeCombo.getSelectedItem();
        if (policy.equals("Write-Back")) {
            cache.setWritePolicy(CacheMemory.WritePolicy.WRITE_BACK);
        } else {
            cache.setWritePolicy(CacheMemory.WritePolicy.WRITE_THROUGH);
        }
        log("Politica scriere: " + policy);
        saveLogToFile();
    }


    private void updateCacheDisplay() {
        tableModel.setRowCount(0);
        CacheLine[] lines = cache.getCache();

        for (int i = 0; i < lines.length; i++) {
            CacheLine line = lines[i];
            tableModel.addRow(new Object[]{
                    i,
                    line.valid ? "✓" : "✗",
                    line.valid ? line.tag : "-",
                    line.dirty ? "D" : "-",
                    line.valid ? line.data : "(gol)"
            });
        }

        cacheTable.repaint();
    }


    private void updateStats() {
        statsLabel.setText(statsManager.getFormattedStats());
    }


    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }


    private void saveLogToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, false))) {
            writer.print(logArea.getText());
        } catch (IOException e) {
            System.err.println("Eroare la salvarea log-ului: " + e.getMessage());
        }
    }


    private void saveStatistics() {
        String mapping = (String) mappingCombo.getSelectedItem();
        String replacement = (String) replacementCombo.getSelectedItem();
        String write = (String) writeCombo.getSelectedItem();

        boolean success = statsManager.saveStats(mapping, replacement, write);

        if(success) {
            JOptionPane.showMessageDialog(this,
                    "Statistici salvate în " + STATS_FILE,
                    "Salvare Reușită",
                    JOptionPane.INFORMATION_MESSAGE);
            log("Statistici salvate în fișier: " + STATS_FILE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Eroare la salvarea statisticilor!",
                    "Eroare",
                    JOptionPane.ERROR_MESSAGE);
        }

        saveLogToFile();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CacheSimulatorGUI().setVisible(true));
    }
}