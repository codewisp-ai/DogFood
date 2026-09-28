package com.dogfood.event.eligibility;

public class MaxTeamSizeRule implements EligibilityRule {
    private final int maxSize;

    public MaxTeamSizeRule(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public String getType() {
        return "MAX_TEAM_SIZE";
    }

    @Override
    public boolean evaluate(EligibilityContext context) {
        return context.teamSize() <= maxSize;
    }

    @Override
    public String getViolationMessage() {
        return "Team size exceeds maximum allowed size of " + maxSize;
    }
}
