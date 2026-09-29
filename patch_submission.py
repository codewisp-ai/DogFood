import sys

with open('services/submission/src/main/java/com/dogfood/submission/controller/SubmissionController.java', 'r') as f:
    content = f.read()

old_method = """    @GetMapping("/events/{eventId}/submissions/export")
    public ResponseEntity<List<SubmissionResponse>> exportSubmissions(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        // TODO: check ORGANIZER role
        GalleryFilter filter = new GalleryFilter(null, null, null);
        Page<SubmissionResponse> page = submissionService.searchGallery(eventId, filter, Pageable.unpaged());
        return ResponseEntity.ok(page.getContent());
    }"""

new_method = """    @GetMapping(value = "/events/{eventId}/submissions/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportSubmissionsCsv(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (roles == null || !roles.contains("ORGANIZER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        GalleryFilter filter = new GalleryFilter(null, null, null);
        Page<SubmissionResponse> page = submissionService.searchGallery(eventId, filter, Pageable.unpaged());
        
        StringBuilder csv = new StringBuilder();
        csv.append("Id,Title,TrackId,Status,RepoUrl,DemoUrl,RiskScore,ForensicStatus\\n");
        for (SubmissionResponse sub : page.getContent()) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s\\n",
                    sub.id(),
                    escapeCsv(sub.title()),
                    sub.trackId() != null ? sub.trackId() : "",
                    sub.status(),
                    sub.repoUrl() != null ? escapeCsv(sub.repoUrl()) : "",
                    sub.demoVideoUrl() != null ? escapeCsv(sub.demoVideoUrl()) : "",
                    sub.riskScore() != null ? sub.riskScore() : "",
                    sub.forensicStatus() != null ? sub.forensicStatus() : ""
            ));
        }
        return ResponseEntity.ok(csv.toString());
    }

    private String escapeCsv(String data) {
        if (data == null) return "";
        String escaped = data.replaceAll("\\"", "\\"\\"");
        if (escaped.contains(",") || escaped.contains("\\n") || escaped.contains("\\"")) {
            return "\\"" + escaped + "\\"";
        }
        return escaped;
    }"""

content = content.replace(old_method, new_method)

with open('services/submission/src/main/java/com/dogfood/submission/controller/SubmissionController.java', 'w') as f:
    f.write(content)
