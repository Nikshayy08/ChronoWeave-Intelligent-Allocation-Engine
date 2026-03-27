package models;

/*
 * Resource.java
 *
 * Represents a resource listed in the Smart Campus Resource Exchange System.
 *
 * Three types of listings:
 *
 *  SELL    → Senior lists item for permanent sale
 *            Buyer posts request OR seller posts listing
 *            Deal made in person, real money exchanged
 *
 *  RENT    → Owner lists item for short-term use
 *            Needy student posts request with offering price
 *            Owner reaches out, meet in person, item returned after use
 *
 *  DIGITAL → Student uploads Google Drive link for notes/PYQs
 *            Anyone can access freely, no payment needed
 *
 * DAA Relevance:
 *  RENT resources → interval conflict detection (PriorityScheduler)
 *  SELL resources → exchange graph cycle detection (ExchangeGraph)
 *  DIGITAL        → no conflict, free access
 */

public class Resource {

    // ─── Enums ────────────────────────────────────────────────────────────────

    public enum ResourceType {
        PHYSICAL,   // Drafter, calculator, lab kit — one user at a time
        DIGITAL     // Notes, PYQs, PDFs — freely shareable
    }

    public enum ListingType {
        SELL,       // Permanent ownership transfer
        RENT,       // Short term, item returned after use
        DIGITAL     // Free download via link
    }

    // ─── Fields ───────────────────────────────────────────────────────────────

    private String resourceId;
    private String resourceName;
    private ResourceType resourceType;
    private ListingType listingType;
    private String ownedBy;           // Student name who owns or uploaded it
    private boolean available;

    // SELL / RENT specific
    private double askingPrice;       // In rupees — just displayed, not processed

    // DIGITAL specific
    private String downloadLink;      // Google Drive or any public URL

    // ─── Constructor ──────────────────────────────────────────────────────────

    public Resource(String resourceId, String resourceName,
                    ResourceType resourceType, ListingType listingType,
                    String ownedBy) {
        this.resourceId   = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.listingType  = listingType;
        this.ownedBy      = ownedBy;
        this.available    = true;
        this.askingPrice  = 0.0;
        this.downloadLink = null;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String getResourceId()         { return resourceId; }
    public String getResourceName()       { return resourceName; }
    public ResourceType getResourceType() { return resourceType; }
    public ListingType getListingType()   { return listingType; }
    public String getOwnedBy()            { return ownedBy; }
    public boolean isAvailable()          { return available; }
    public double getAskingPrice()        { return askingPrice; }
    public String getDownloadLink()       { return downloadLink; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setAvailable(boolean available)   { this.available = available; }
    public void setAskingPrice(double price)      { this.askingPrice = price; }
    public void setDownloadLink(String link)      { this.downloadLink = link; }

    // ─── Display ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        String base = String.format("[%s][%s] %s | Owner: %s | %s",
                listingType, resourceType, resourceName, ownedBy,
                available ? "Available" : "Unavailable");

        if (listingType == ListingType.SELL || listingType == ListingType.RENT) {
            base += String.format(" | Price: Rs.%.0f", askingPrice);
        }

        if (listingType == ListingType.DIGITAL && downloadLink != null) {
            base += " | Link: " + downloadLink;
        }

        return base;
    }
}