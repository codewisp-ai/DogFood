import sys

with open('/Users/yash/Desktop/DogFood-export/services/judging/src/main/java/com/dogfood/judging/normalization/NormalizationEngine.java', 'r') as f:
    content = f.read()

old_block = """        List<Criterion> criteria = criterionRepository.findByEventId(eventId);
        if (criteria.isEmpty()) {
            log.warn("No criteria found for event={}. Cannot compute scores.", eventId);
            return;
        }

        boolean normalizationEnabled = rubricRepository.findByEventId(eventId)
                .map(com.dogfood.judging.entity.Rubric::getNormalizationEnabled)
                .orElse(true);"""

new_block = """        com.dogfood.judging.entity.Rubric rubric = rubricRepository.findByEventId(eventId).orElse(null);
        if (rubric == null) {
            log.warn("No rubric found for event={}. Cannot compute scores.", eventId);
            return;
        }
        
        List<Criterion> criteria = criterionRepository.findByRubricIdOrderBySortOrderAsc(rubric.getId());
        if (criteria.isEmpty()) {
            log.warn("No criteria found for event={}. Cannot compute scores.", eventId);
            return;
        }

        boolean normalizationEnabled = rubric.getNormalizationEnabled();"""

content = content.replace(old_block, new_block)

with open('/Users/yash/Desktop/DogFood-export/services/judging/src/main/java/com/dogfood/judging/normalization/NormalizationEngine.java', 'w') as f:
    f.write(content)
