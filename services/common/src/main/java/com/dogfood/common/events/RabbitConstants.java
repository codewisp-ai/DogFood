package com.dogfood.common.events;

/**
 * RabbitMQ exchange and routing key constants shared across all services.
 * Using a single source of truth prevents routing key drift between publishers and consumers.
 */
public final class RabbitConstants {
    private RabbitConstants() {}

    // ── Exchanges (Topic) ───────────────────────────────────────
    public static final String AUDIT_EXCHANGE = "dogfood.audit";
    public static final String SCORES_EXCHANGE = "dogfood.scores";
    public static final String WEBHOOKS_EXCHANGE = "dogfood.webhooks";
    public static final String NOTIFICATIONS_EXCHANGE = "dogfood.notifications";
    public static final String CERTIFICATES_EXCHANGE = "dogfood.certificates";

    // ── Queues ──────────────────────────────────────────────────
    public static final String AUDIT_QUEUE = "audit.events";
    public static final String SCORE_NORMALIZATION_QUEUE = "score.normalization";
    public static final String WEBHOOK_DELIVERY_QUEUE = "webhook.delivery";
    public static final String WEBHOOK_DLQ = "webhook.dlq";
    public static final String NOTIFICATION_QUEUE = "notification.events";
    public static final String CERTIFICATE_QUEUE = "certificate.generate";

    // ── Routing Keys ────────────────────────────────────────────
    public static final String AUDIT_ALL = "#";
    public static final String SCORE_SUBMITTED = "score.submitted";
    public static final String NOTIFICATION_ALL = "notification.*";
    public static final String CERTIFICATE_GENERATE = "certificate.generate";
    public static final String WEBHOOK_ALL = "#";
}
