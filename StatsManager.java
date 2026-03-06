package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatsManager {

    private static final double CACHE_ACCESS_TIME = 1.0;
    private static final double RAM_ACCESS_TIME = 100.0;

    private int totalHits;
    private int totalMisses;
    private int totalReads;
    private int totalWrites;

    private String statsFilePath = "cache_statistics.txt";

    public StatsManager() {
        reset();
    }

    public void reset() {
        totalHits = 0;
        totalMisses = 0;
        totalReads = 0;
        totalWrites = 0;
    }

    public void recordHit() {
        totalHits++;
    }

    public void recordMiss() {
        totalMisses++;
    }

    public void recordRead() {
        totalReads++;
    }

    public void recordWrite() {
        totalWrites++;
    }

    public int getTotalHits() {
        return totalHits;
    }

    public int getTotalMisses() {
        return totalMisses;
    }

    public int getTotalOperations() {
        return totalHits + totalMisses;
    }

    public int getTotalReads() {
        return totalReads;
    }

    public int getTotalWrites() {
        return totalWrites;
    }


    public double getHitRate() {
        int total = getTotalOperations();
        return total > 0 ? (totalHits * 100.0 / total) : 0.0;
    }


    public double getMissRate() {
        int total = getTotalOperations();
        return total > 0 ? (totalMisses * 100.0 / total) : 0.0;
    }


    public double getAverageAccessTime() {
        double missRate = getMissRate() / 100.0;
        return CACHE_ACCESS_TIME + (missRate * RAM_ACCESS_TIME);
    }

    public String getFormattedStats() {
        return String.format(
                "Hits: %d | Misses: %d | Total: %d | Hit Rate: %.2f%% | Miss Rate: %.2f%% | Avg Time: %.2f ns",
                totalHits, totalMisses, getTotalOperations(),
                getHitRate(), getMissRate(), getAverageAccessTime()
        );
    }

    public String getDetailedStats(String mappingType, String replacementPolicy, String writePolicy) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("STATISTICI CACHE - ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())).append("\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append(String.format("Configurație:\n"));
        sb.append(String.format("  - Tip mapare: %s\n", mappingType));
        sb.append(String.format("  - Politică înlocuire: %s\n", replacementPolicy));
        sb.append(String.format("  - Politică scriere: %s\n", writePolicy));
        sb.append("\n");
        sb.append(String.format("Operații:\n"));
        sb.append(String.format("  - Total citiri: %d\n", totalReads));
        sb.append(String.format("  - Total scrieri: %d\n", totalWrites));
        sb.append(String.format("  - Total operații: %d\n", getTotalOperations()));
        sb.append("\n");
        sb.append(String.format("Performanță:\n"));
        sb.append(String.format("  - Cache Hits: %d\n", totalHits));
        sb.append(String.format("  - Cache Misses: %d\n", totalMisses));
        sb.append(String.format("  - Hit Rate: %.2f%%\n", getHitRate()));
        sb.append(String.format("  - Miss Rate: %.2f%%\n", getMissRate()));
        sb.append("\n");
        sb.append(String.format("Timing (estimat):\n"));
        sb.append(String.format("  - Timp acces cache: %.2f ns\n", CACHE_ACCESS_TIME));
        sb.append(String.format("  - Timp acces RAM: %.2f ns\n", RAM_ACCESS_TIME));
        sb.append(String.format("  - Timp mediu acces: %.2f ns\n", getAverageAccessTime()));
        sb.append("=".repeat(60)).append("\n\n");

        return sb.toString();
    }


    public boolean saveStats(String mappingType, String replacementPolicy, String writePolicy) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(statsFilePath, true))) {
            writer.print(getDetailedStats(mappingType, replacementPolicy, writePolicy));
            return true;
        } catch (IOException e) {
            System.err.println("Eroare la salvarea statisticilor: " + e.getMessage());
            return false;
        }
    }


    public void setStatsFilePath(String path) {
        this.statsFilePath = path;
    }
}