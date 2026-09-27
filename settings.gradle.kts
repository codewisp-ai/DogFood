rootProject.name = "dogfood"

// Common shared library
include("services:common")

// Backend microservices
include("services:identity")
include("services:event")
include("services:submission")
include("services:judging")
include("services:voting")
include("services:notification")
include("services:webhook-dispatcher")
include("services:certificate")
include("services:observability")

// API Gateway
include("gateway")

// Integration tests
include("tests")
