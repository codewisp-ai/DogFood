package com.dogfood.event.dto;

import java.util.List;

public record EligibilityCheckResult(boolean eligible, List<String> violations) {}
