import sys

# 1. Update Repository
with open('services/identity/src/main/java/com/dogfood/identity/repository/UserEventRoleRepository.java', 'r') as f:
    repo_content = f.read()
repo_content = repo_content.replace('List<UserEventRole> findByEventIdAndRole(UUID eventId, String role);', 'List<UserEventRole> findByEventIdAndRole(UUID eventId, String role);\n    List<UserEventRole> findByEventId(UUID eventId);')
with open('services/identity/src/main/java/com/dogfood/identity/repository/UserEventRoleRepository.java', 'w') as f:
    f.write(repo_content)

# 2. Update Controller
with open('services/identity/src/main/java/com/dogfood/identity/controller/AuthController.java', 'r') as f:
    ctrl_content = f.read()

new_method = """    @Operation(summary = "Export users for an event")
    @GetMapping(value = "/events/{eventId}/users/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportEventUsers(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {
        
        if (rolesHeader == null || !rolesHeader.contains("ORGANIZER")) {
            return ResponseEntity.status(403).build();
        }

        List<UserEventRole> roles = userEventRoleRepository.findByEventId(eventId);
        StringBuilder csv = new StringBuilder();
        csv.append("UserId,Email,DisplayName,Role,AssignedTrackIds\\n");

        for (UserEventRole r : roles) {
            User u = r.getUser();
            csv.append(String.format("%s,%s,%s,%s,%s\\n",
                    u.getId(),
                    escapeCsv(u.getEmail()),
                    escapeCsv(u.getDisplayName()),
                    r.getRole(),
                    r.getAssignedTrackIds() != null ? r.getAssignedTrackIds().toString().replace(",", ";") : ""
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
    }
}"""
ctrl_content = ctrl_content.replace("}\n", new_method + "\n")
with open('services/identity/src/main/java/com/dogfood/identity/controller/AuthController.java', 'w') as f:
    f.write(ctrl_content)

