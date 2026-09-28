package com.dogfood.event.eligibility;

public interface EligibilityRule {
    String getType();
    boolean evaluate(EligibilityContext context);
    String getViolationMessage();
}
