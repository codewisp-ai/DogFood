import sys

with open('/Users/yash/Desktop/DogFood-export/services/judging/src/main/java/com/dogfood/judging/repository/NormalizedScoreRepository.java', 'r') as f:
    content = f.read()

if "void deleteByEventId" not in content:
    content = content.replace("List<NormalizedScore> findByEventId(UUID eventId);", "List<NormalizedScore> findByEventId(UUID eventId);\n\n    void deleteByEventId(UUID eventId);")

with open('/Users/yash/Desktop/DogFood-export/services/judging/src/main/java/com/dogfood/judging/repository/NormalizedScoreRepository.java', 'w') as f:
    f.write(content)
