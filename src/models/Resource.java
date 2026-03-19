package models;

import java.util.ArrayList;
import java.util.List;

/*
 * Resource.java
 *
 * Represents a resource in the system — either physical or digital.
 *
 * Physical: Drafter, lab instrument, calculator → one user at a time
 * Digital : Notes, PDFs, PYQs                  → multiple users simultaneously
 *
 * Key Design:
 *  - Physical resources use interval conflict detection
 *  - Digital resources skip conflict detection entirely
 *  - fileHash enables duplicate detection for digital uploads (SHA-256)
 *
 * DAA Relevance:
 *  - Drives the bifurcated scheduling logic in PriorityScheduler
 *  - Tags enable Trie-based search (future enhancement)
 */

public class Resource {

    // ─── Enum ─────────────────────────────────────────────────────────────────

    public enum ResourceType {
        PHYSICAL,
        DIGITAL
    }

    // ─── Fields ───────────────────────────────────────────────────────────────

    private String resourceId;
    private String resourceName;
    private ResourceType type;
    private String ownedBy;         // Student name who owns or uploaded this
    private boolean available;
    private List<String> tags;      // e.g. ["CSE", "3rd-sem", "lab"]
    private String fileHash;        // SHA-256 hash — prevents duplicate digital uploads
    private double averageRating;   // Peer rating after use (1.0–5.0)
    private int ratingCount;        // Number of ratings received

    // ─── Constructor ──────────────────────────────────────────────────────────

    public Resource(String resourceId, String resourceName,
                    ResourceType type, String ownedBy) {

        this.resourceId    = resourceId;
        this.resourceName  = resourceName;
        this.type          = type;
        this.ownedBy       = ownedBy;
        this.available     = true;
        this.tags          = new ArrayList<>();
        this.fileHash      = null;
        this.averageRating = 0.0;
        this.ratingCount   = 0;
    }

    // ─── Rating System ────────────────────────────────────────────────────────

    /*
     * addRating(stars)
     *
     * Running average — no need to store all ratings.
     * newAvg = ((oldAvg * count) + newRating) / (count + 1)
     */
    public void addRating(double stars) {
        if (stars < 1.0 || stars > 5.0) return;
        averageRating = ((averageRating * ratingCount) + stars) / (ratingCount + 1);
        ratingCount++;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String getResourceId()       { return resourceId; }
    public String getResourceName()     { return resourceName; }
    public ResourceType getType()       { return type; }
    public String getOwnedBy()          { return ownedBy; }
    public boolean isAvailable()        { return available; }
    public List<String> getTags()       { return tags; }
    public String getFileHash()         { return fileHash; }
    public double getAverageRating()    { return averageRating; }
    public int getRatingCount()         { return ratingCount; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setAvailable(boolean available) { this.available = available; }
    public void setFileHash(String hash)        { this.fileHash = hash; }
    public void addTag(String tag)              { this.tags.add(tag); }

    // ─── Display ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("[%s] %s | Owner: %s | %s | Rating: %.1f (%d reviews) | Tags: %s",
                type, resourceName, ownedBy,
                available ? "✓ Available" : "✗ In Use",
                averageRating, ratingCount, tags);
    }
}
