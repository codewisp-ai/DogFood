import sys

with open('services/voting/src/main/java/com/dogfood/voting/controller/VotingController.java', 'r') as f:
    content = f.read()

content = content.replace(
    "@RequestHeader(value = \"X-User-Id\") String userId",
    "@RequestHeader(value = \"X-User-Id\", required = false) String userId,\n            @RequestHeader(value = \"X-Forwarded-For\", required = false) String ip"
)
content = content.replace(
    "return ballotService.getRandomizedBallot(eventId, UUID.fromString(userId), submissionIds);",
    "UUID seedId = userId != null ? UUID.fromString(userId) : (ip != null ? UUID.nameUUIDFromBytes(ip.getBytes()) : UUID.randomUUID());\n        return ballotService.getRandomizedBallot(eventId, seedId, submissionIds);"
)

with open('services/voting/src/main/java/com/dogfood/voting/controller/VotingController.java', 'w') as f:
    f.write(content)
