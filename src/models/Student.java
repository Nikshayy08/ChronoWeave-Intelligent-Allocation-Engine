package models;

/*
 * Student.java
 *
 * Credit Table:
 *  +2  →  Upload a digital resource
 *  +3  →  Lend a physical resource
 *  +1  →  First contribution bonus (one-time only)
 *  +1  →  Bonus when your resource is rated 4 stars or above
 *  -1  →  Successful request fulfilled
 *  -2  →  No-show penalty
 *
 * Cold Start Fix:
 *  New students get 2 FREE trial credits on joining.
 *  After 2 requests credits hit 0 -> must contribute to continue.
 */

public class Student {

    private String studentId;
    private String name;
    private int karmaCredits;
    private int totalContributions;
    private int totalRequests;
    private int noShowCount;
    private boolean accessGated;
    private boolean firstContributionDone;

    public static final int CREDIT_DIGITAL_UPLOAD     =  2;
    public static final int CREDIT_PHYSICAL_LEND      =  3;
    public static final int CREDIT_FIRST_CONTRIBUTION =  1;
    public static final int CREDIT_RATING_BONUS       =  1;
    public static final int COST_REQUEST_FULFILLED    = -1;
    public static final int PENALTY_NO_SHOW           = -2;
    public static final int FREE_TRIAL_CREDITS        =  2;

    public Student(String studentId, String name) {
        this.studentId             = studentId;
        this.name                  = name;
        this.totalContributions    = 0;
        this.totalRequests         = 0;
        this.noShowCount           = 0;
        this.firstContributionDone = false;
        this.karmaCredits          = FREE_TRIAL_CREDITS;
        this.accessGated           = false;
    }

    public void contributeDigital() {
        karmaCredits += CREDIT_DIGITAL_UPLOAD;
        applyFirstContributionBonus();
        totalContributions++;
        accessGated = false;
    }

    public void contributePhysical() {
        karmaCredits += CREDIT_PHYSICAL_LEND;
        applyFirstContributionBonus();
        totalContributions++;
        accessGated = false;
    }

    private void applyFirstContributionBonus() {
        if (!firstContributionDone) {
            karmaCredits          += CREDIT_FIRST_CONTRIBUTION;
            firstContributionDone  = true;
            System.out.println("First contribution bonus! +"
                + CREDIT_FIRST_CONTRIBUTION + " extra credit awarded to " + name);
        }
    }

    public void receiveRatingBonus() {
        karmaCredits += CREDIT_RATING_BONUS;
    }

    public boolean chargeForRequest() {
        if (karmaCredits <= 0) return false;
        karmaCredits += COST_REQUEST_FULFILLED;
        totalRequests++;
        if (karmaCredits == 0) accessGated = true;
        return true;
    }

    public void applyNoShowPenalty() {
        karmaCredits = Math.max(0, karmaCredits + PENALTY_NO_SHOW);
        noShowCount++;
        if (karmaCredits == 0) accessGated = true;
    }

    public void incrementRequests() {
        this.totalRequests++;
    }

    public boolean canRequest() {
        return !accessGated && karmaCredits > 0;
    }

    public String getStudentId()             { return studentId; }
    public String getName()                  { return name; }
    public int getKarmaCredits()             { return karmaCredits; }
    public int getTotalContributions()       { return totalContributions; }
    public int getTotalRequests()            { return totalRequests; }
    public int getNoShowCount()              { return noShowCount; }
    public boolean isAccessGated()           { return accessGated; }
    public boolean isFirstContributionDone() { return firstContributionDone; }

    @Override
    public String toString() {
        return String.format("%s | Credits: %d | Contributions: %d | Requests: %d | %s",
                name, karmaCredits, totalContributions, totalRequests,
                accessGated ? "Blocked" : "Active");
    }
}