import sys

with open('services/voting/src/main/java/com/dogfood/voting/repository/VoteRepository.java', 'r') as f:
    repo_content = f.read()

repo_content = repo_content.replace('List<Vote> findByEventIdAndVoterId', 'List<Vote> findByEventId(UUID eventId);\n    List<Vote> findByEventIdAndVoterId')

with open('services/voting/src/main/java/com/dogfood/voting/repository/VoteRepository.java', 'w') as f:
    f.write(repo_content)

with open('services/voting/src/main/java/com/dogfood/voting/controller/VotingController.java', 'r') as f:
    ctrl_content = f.read()

ctrl_content = ctrl_content.replace("private final VotingService votingService;", "private final VotingService votingService;\n    private final com.dogfood.voting.repository.VoteRepository voteRepository;")
ctrl_content = ctrl_content.replace("this.votingService = votingService;", "this.votingService = votingService;") # leave as is
ctrl_content = ctrl_content.replace("public VotingController(VotingService votingService, BallotService ballotService) {", "public VotingController(VotingService votingService, BallotService ballotService, com.dogfood.voting.repository.VoteRepository voteRepository) {")
ctrl_content = ctrl_content.replace("this.ballotService = ballotService;", "this.ballotService = ballotService;\n        this.voteRepository = voteRepository;")

new_method = """    @GetMapping(value = "/{eventId}/votes/export.csv", produces = "text/csv")
    public org.springframework.http.ResponseEntity<String> exportVotes(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (roles == null || !roles.contains("ORGANIZER")) {
            return org.springframework.http.ResponseEntity.status(403).build();
        }
        
        List<Vote> votes = voteRepository.findByEventId(eventId);
        StringBuilder csv = new StringBuilder();
        csv.append("Id,VoterId,SubmissionId,CreditsSpent,IpAddress,CreatedAt\\n");
        for (Vote v : votes) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s\\n",
                    v.getId(), v.getVoterId(), v.getSubmissionId(),
                    v.getCreditsSpent(), v.getIpAddress(), v.getCreatedAt()
            ));
        }
        return org.springframework.http.ResponseEntity.ok(csv.toString());
    }
}"""

ctrl_content = ctrl_content.replace("}\n", new_method + "\n")

with open('services/voting/src/main/java/com/dogfood/voting/controller/VotingController.java', 'w') as f:
    f.write(ctrl_content)

