package com.dogfood.event.eligibility;

public class OneSubmissionPerTeamRule implements EligibilityRule {

    @Override
    public String getType() {
        return "ONE_SUBMISSION_PER_TEAM";
    }

    @Override
    public boolean evaluate(EligibilityContext context) {
        return context.existingSubmissionCount() == 0;
    }

    @Override
    public String getViolationMessage() {
        return "Team already has a submission.";
    }
}
