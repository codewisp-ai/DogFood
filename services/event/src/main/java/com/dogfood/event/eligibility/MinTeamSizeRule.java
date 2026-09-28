package com.dogfood.event.eligibility;

public class MinTeamSizeRule implements EligibilityRule {
    private final int minSize;

    public MinTeamSizeRule(int minSize) {
        this.minSize = minSize;
    }

    @Override
    public String getType() {
        return "MIN_TEAM_SIZE";
    }

    @Override
    public boolean evaluate(EligibilityContext context) {
        return context.teamSize() >= minSize;
    }

    @Override
    public String getViolationMessage() {
        return "Team size is below minimum required size of " + minSize;
    }
}
