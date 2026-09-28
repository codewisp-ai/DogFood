package com.dogfood.event.eligibility;

import com.dogfood.event.entity.Event;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EligibilityEngine {

    public List<String> evaluate(Event event, EligibilityContext context) {
        List<String> violations = new ArrayList<>();
        List<EligibilityRule> rules = parseRules(event.getEligibilityRules());
        
        for (EligibilityRule rule : rules) {
            if (!rule.evaluate(context)) {
                violations.add(rule.getViolationMessage());
            }
        }
        return violations;
    }

    private List<EligibilityRule> parseRules(List<Map<String, Object>> ruleConfigs) {
        List<EligibilityRule> rules = new ArrayList<>();
        if (ruleConfigs == null) return rules;

        for (Map<String, Object> config : ruleConfigs) {
            String type = (String) config.get("type");
            if (type == null) continue;
            
            switch (type) {
                case "MAX_TEAM_SIZE":
                    rules.add(new MaxTeamSizeRule(Integer.parseInt(config.get("value").toString())));
                    break;
                case "MIN_TEAM_SIZE":
                    rules.add(new MinTeamSizeRule(Integer.parseInt(config.get("value").toString())));
                    break;
                case "ONE_SUBMISSION_PER_TEAM":
                    rules.add(new OneSubmissionPerTeamRule());
                    break;
            }
        }
        return rules;
    }
}
