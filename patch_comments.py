import sys

with open('services/submission/src/main/java/com/dogfood/submission/controller/SubmissionController.java', 'r') as f:
    content = f.read()

# Add dependencies
content = content.replace("private final StorageService storageService;", "private final StorageService storageService;\n    private final com.dogfood.submission.repository.CommentRepository commentRepository;")

new_endpoints = """
    // --- Comments ---
    
    public record CreateCommentRequest(String content) {}
    
    @PostMapping("/submissions/{id}/comments")
    public com.dogfood.submission.entity.Comment postComment(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id") String userId,
            @RequestHeader(value = "X-User-Name", defaultValue = "Anonymous") String userName,
            @RequestBody CreateCommentRequest request) {
        
        com.dogfood.submission.entity.Comment comment = new com.dogfood.submission.entity.Comment();
        comment.setSubmissionId(id);
        comment.setAuthorId(UUID.fromString(userId));
        comment.setAuthorName(userName);
        comment.setContent(request.content());
        
        return commentRepository.save(comment);
    }
    
    @GetMapping("/submissions/{id}/comments")
    public List<com.dogfood.submission.entity.Comment> getComments(@PathVariable UUID id) {
        return commentRepository.findBySubmissionIdOrderByCreatedAtDesc(id);
    }
"""

content = content.replace("}\n", new_endpoints + "\n}\n")

with open('services/submission/src/main/java/com/dogfood/submission/controller/SubmissionController.java', 'w') as f:
    f.write(content)

