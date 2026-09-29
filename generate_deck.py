#!/usr/bin/env python3
"""
Dogfood Pitch Deck Generator
Produces Dogfood-Pitch.pptx following the architecture-first pitch-deck spec.
"""

from pptx import Presentation
from pptx.util import Inches, Pt, Emu
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE
import os

# ─── Design tokens ───────────────────────────────────────────────────────────

INK       = RGBColor(0x0E, 0x11, 0x16)
PAPER     = RGBColor(0xF7, 0xF6, 0xF2)
ACCENT    = RGBColor(0x0F, 0x76, 0x6E)   # deep teal
GREEN     = RGBColor(0x1F, 0x7A, 0x5A)   # verified / mitigated
AMBER     = RGBColor(0xB4, 0x53, 0x09)   # risk / flag
GREY      = RGBColor(0x5B, 0x64, 0x70)
RULE      = RGBColor(0xD9, 0xD7, 0xD0)
WHITE     = RGBColor(0xFF, 0xFF, 0xFF)

FONT_SANS  = "IBM Plex Sans"
FONT_MONO  = "IBM Plex Mono"

SLIDE_W = Inches(13.333)
SLIDE_H = Inches(7.5)

MARGIN_L = Inches(0.7)
MARGIN_T = Inches(0.6)
MARGIN_R = Inches(0.7)
CONTENT_W = Inches(13.333 - 1.4)

# ─── Helpers ──────────────────────────────────────────────────────────────────

def set_slide_bg(slide, color):
    bg = slide.background
    fill = bg.fill
    fill.solid()
    fill.fore_color.rgb = color

def add_textbox(slide, left, top, width, height, text, font_size=16,
                bold=False, color=INK, font_name=FONT_SANS, align=PP_ALIGN.LEFT,
                anchor=MSO_ANCHOR.TOP, line_spacing=1.15):
    txBox = slide.shapes.add_textbox(left, top, width, height)
    tf = txBox.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = text
    p.font.size = Pt(font_size)
    p.font.bold = bold
    p.font.color.rgb = color
    p.font.name = font_name
    p.alignment = align
    p.space_after = Pt(0)
    p.space_before = Pt(0)
    run = p.runs[0] if p.runs else p.add_run()
    run.font.name = font_name
    tf.auto_size = None
    return txBox

def add_multiline(slide, left, top, width, height, lines, font_size=16,
                  color=INK, font_name=FONT_SANS, bold=False, line_spacing=1.3,
                  align=PP_ALIGN.LEFT):
    """lines is a list of (text, optional_overrides_dict)"""
    txBox = slide.shapes.add_textbox(left, top, width, height)
    tf = txBox.text_frame
    tf.word_wrap = True
    for i, item in enumerate(lines):
        if isinstance(item, str):
            txt, overrides = item, {}
        else:
            txt, overrides = item
        if i == 0:
            p = tf.paragraphs[0]
        else:
            p = tf.add_paragraph()
        p.text = txt
        p.font.size = Pt(overrides.get('size', font_size))
        p.font.bold = overrides.get('bold', bold)
        p.font.color.rgb = overrides.get('color', color)
        p.font.name = overrides.get('font', font_name)
        p.alignment = overrides.get('align', align)
        p.space_after = Pt(overrides.get('space_after', 4))
        p.space_before = Pt(overrides.get('space_before', 0))
    return txBox

def add_rect(slide, left, top, width, height, fill_color=None,
             border_color=None, border_width=Pt(1)):
    shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
    shape.rotation = 0
    if hasattr(shape, 'adjustments') and len(shape.adjustments) > 0:
        shape.adjustments[0] = 0.02  # slight radius
    if fill_color:
        shape.fill.solid()
        shape.fill.fore_color.rgb = fill_color
    else:
        shape.fill.background()
    if border_color:
        shape.line.color.rgb = border_color
        shape.line.width = border_width
    else:
        shape.line.fill.background()
    return shape

def add_slide_number(slide, num):
    add_textbox(slide, Inches(12.5), Inches(7.0), Inches(0.6), Inches(0.3),
                str(num), font_size=10, color=GREY, align=PP_ALIGN.RIGHT)

def add_wordmark(slide, color=GREY):
    add_textbox(slide, MARGIN_L, Inches(7.0), Inches(1.5), Inches(0.3),
                "Dogfood", font_size=10, color=color, bold=True)

def add_slide_title(slide, title, color=INK):
    add_textbox(slide, MARGIN_L, MARGIN_T, CONTENT_W, Inches(0.5),
                title, font_size=28, bold=True, color=color)

def add_notes(slide, text):
    notes_slide = slide.notes_slide
    notes_slide.notes_text_frame.text = text

def add_line(slide, x1, y1, x2, y2, color=RULE, width=Pt(1)):
    connector = slide.shapes.add_connector(1, x1, y1, x2, y2)  # 1 = straight
    connector.line.color.rgb = color
    connector.line.width = width
    return connector

def add_arrow(slide, x1, y1, x2, y2, color=GREY, width=Pt(1.5)):
    connector = slide.shapes.add_connector(1, x1, y1, x2, y2)
    connector.line.color.rgb = color
    connector.line.width = width
    # Add arrowhead via XML manipulation
    from pptx.oxml.ns import qn
    ln = connector.line._ln
    tail = ln.makeelement(qn('a:tailEnd'), {'type': 'triangle', 'w': 'med', 'len': 'med'})
    ln.append(tail)
    return connector

# ─── Slide builders ───────────────────────────────────────────────────────────

def slide_01_title(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])  # blank
    set_slide_bg(slide, INK)

    add_textbox(slide, MARGIN_L, Inches(2.0), CONTENT_W, Inches(1.0),
                "Dogfood.", font_size=56, bold=True, color=WHITE)
    add_textbox(slide, MARGIN_L, Inches(3.0), CONTENT_W, Inches(0.5),
                "Hackathon judging you can defend.", font_size=28, color=ACCENT)
    add_textbox(slide, MARGIN_L, Inches(3.8), CONTENT_W, Inches(0.4),
                "Open-source  ·  Self-hostable  ·  One command", font_size=16, color=GREY)

    add_textbox(slide, MARGIN_L, Inches(5.6), CONTENT_W, Inches(0.3),
                "DogFood Team  ·  Hackathon 2026", font_size=14, color=GREY)
    add_wordmark(slide, color=GREY)

    add_notes(slide, "Open with confidence. Pause on 'you can defend' — this is the thesis. "
              "The entire deck supports this one claim: every judging decision is auditable, "
              "normalized, and tamper-evident.")


def slide_02_problem(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "Incumbent platforms converge on the same shallow feature set")
    add_slide_number(slide, 2)
    add_wordmark(slide)

    problems = [
        ("Averaged scores with no calibration.", {'size': 18, 'space_after': 16}),
        ("No documented normalization — a lenient judge decides winners.", {'size': 18, 'space_after': 16}),
        ("No audit trail. Organizers cannot explain a result.", {'size': 18, 'space_after': 16}),
        ("No forensic checks on submissions. Pre-built projects pass.", {'size': 18, 'space_after': 16}),
    ]
    add_multiline(slide, MARGIN_L, Inches(1.6), Inches(8), Inches(3),
                  problems, color=INK)

    add_textbox(slide, MARGIN_L, Inches(4.8), Inches(8), Inches(0.5),
                "In our review of Devpost, Devfolio, DoraHacks, HackerEarth, and Unstop.",
                font_size=12, color=GREY, font_name=FONT_SANS)

    add_notes(slide, "Speaker: 'We surveyed five major platforms. None documents a normalization "
              "method. None offers calibration rounds. None provides a forensic scan.' "
              "[CONFIRM: re-verify each incumbent's current feature set before presenting.]")


def slide_03_comparison(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "Dogfood closes gaps that incumbents treat as optional")
    add_slide_number(slide, 3)
    add_wordmark(slide)

    headers = ["Capability", "Typical incumbent", "Dogfood"]
    rows = [
        ["Weighted rubric criteria", "Partial", "✓"],
        ["Documented z-score normalization", "—", "✓"],
        ["Bayesian shrinkage for few-review judges", "—", "✓"],
        ["Calibration round", "—", "✓"],
        ["COI self-declaration + audit", "—", "✓"],
        ["Forensic repo scan", "—", "✓"],
        ["Integrity report export", "—", "✓"],
        ["Self-hostable, offline-capable", "—", "✓"],
        ["Public API (OpenAPI)", "Rare / undocumented", "✓"],
    ]

    col_widths = [Inches(4.5), Inches(2.5), Inches(2.0)]
    start_y = Inches(1.5)
    row_h = Inches(0.38)

    # Header
    x = MARGIN_L
    for ci, hdr in enumerate(headers):
        add_textbox(slide, x, start_y, col_widths[ci], row_h,
                    hdr, font_size=12, bold=True, color=GREY)
        x += col_widths[ci]

    # Separator
    add_line(slide, MARGIN_L, start_y + row_h, MARGIN_L + sum(col_widths), start_y + row_h)

    # Data rows
    for ri, row in enumerate(rows):
        y = start_y + row_h + Inches(0.02) + (ri * row_h)
        x = MARGIN_L
        for ci, cell in enumerate(row):
            c = GREEN if cell == "✓" else (AMBER if cell == "—" else INK)
            add_textbox(slide, x, y, col_widths[ci], row_h,
                        cell, font_size=14, color=c)
            x += col_widths[ci]

    add_notes(slide, "Speaker: 'This is not marketing — each row maps to a section of our "
              "codebase. We will show the math, the code path, and the audit trail.' "
              "[CONFIRM: re-verify incumbent claims before presenting.]")


def slide_04_lifecycle(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "Nine steps from event creation to certified results")
    add_slide_number(slide, 4)
    add_wordmark(slide)

    steps = ["Create\nevent", "Register", "Form\nteam", "Submit", "Assign\njudges",
             "Score", "Normalize", "Publish", "Certify"]
    integrity_at = {3: "Eligibility\nre-check", 4: "COI\nexclusion", 5: "Calibration\nz-score",
                    6: "Shrinkage", 3: "Forensic\nscan"}

    box_w = Inches(1.1)
    box_h = Inches(0.65)
    gap = Inches(0.2)
    start_x = MARGIN_L
    y = Inches(2.5)

    for i, step in enumerate(steps):
        x = start_x + i * (box_w + gap)
        fill = ACCENT if i in [5, 6] else None
        border = ACCENT if i in [5, 6] else RULE
        txt_color = WHITE if i in [5, 6] else INK

        rect = add_rect(slide, x, y, box_w, box_h, fill_color=fill, border_color=border)
        add_textbox(slide, x, y + Inches(0.08), box_w, box_h,
                    step, font_size=11, color=txt_color, align=PP_ALIGN.CENTER,
                    font_name=FONT_MONO, bold=True)

        # Arrow between boxes
        if i < len(steps) - 1:
            add_arrow(slide, x + box_w, y + box_h / 2,
                      x + box_w + gap, y + box_h / 2, color=RULE)

    # Integrity annotation strip
    add_textbox(slide, MARGIN_L, Inches(3.6), CONTENT_W, Inches(0.4),
                "▲ Integrity controls attach at Submit, Assign, Score, and Normalize",
                font_size=12, color=ACCENT, font_name=FONT_MONO)

    add_notes(slide, "Speaker: Walk through left to right. Pause at the teal boxes (Score, "
              "Normalize) — these are the mathematical core. Point out that integrity controls "
              "attach at four distinct points in the pipeline.")


def slide_05_roles(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "One portal, five server-enforced roles")
    add_slide_number(slide, 5)
    add_wordmark(slide)

    headers = ["", "Visitor", "Participant", "Judge", "Organizer", "Admin"]
    rows = [
        ["Browse gallery & vote", "✓", "✓", "—", "✓", "✓"],
        ["Submit project", "—", "✓", "—", "—", "✓"],
        ["Score assigned projects", "—", "—", "✓", "—", "✓"],
        ["Configure event & rubric", "—", "—", "—", "✓", "✓"],
        ["View audit log & forensics", "—", "—", "—", "✓", "✓"],
        ["Manage users & roles", "—", "—", "—", "—", "✓"],
    ]

    col_widths = [Inches(3.2)] + [Inches(1.5)] * 5
    start_y = Inches(1.5)
    row_h = Inches(0.36)

    x = MARGIN_L
    for ci, hdr in enumerate(headers):
        add_textbox(slide, x, start_y, col_widths[ci], row_h,
                    hdr, font_size=11, bold=True, color=GREY)
        x += col_widths[ci]

    add_line(slide, MARGIN_L, start_y + row_h,
             MARGIN_L + sum(col_widths), start_y + row_h)

    for ri, row in enumerate(rows):
        y = start_y + row_h + (ri * row_h)
        x = MARGIN_L
        for ci, cell in enumerate(row):
            c = GREEN if cell == "✓" else (RULE if cell == "—" else INK)
            sz = 13 if ci == 0 else 13
            add_textbox(slide, x, y, col_widths[ci], row_h,
                        cell, font_size=sz, color=c, align=PP_ALIGN.CENTER if ci > 0 else PP_ALIGN.LEFT)
            x += col_widths[ci]

    add_textbox(slide, MARGIN_L, Inches(5.0), CONTENT_W, Inches(0.5),
                "Every permission is enforced server-side via JWT claims. "
                "The frontend hides UI elements; the backend rejects unauthorized requests.",
                font_size=13, color=GREY)

    # Screenshot placeholder
    rect = add_rect(slide, Inches(9.5), Inches(1.5), Inches(3.5), Inches(2.5),
                    border_color=RULE)
    add_textbox(slide, Inches(9.5), Inches(2.5), Inches(3.5), Inches(0.3),
                "[SCREENSHOT: 01-portal-home.png]",
                font_size=11, color=AMBER, align=PP_ALIGN.CENTER, font_name=FONT_MONO)

    add_notes(slide, "Speaker: 'The frontend is one React app with role-based routing. "
              "But every permission is double-enforced: once by the service logic filtering "
              "on JWT claims, and again by Postgres row-level security.'")


def slide_06_architecture(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "Nine services, one compose file, offline after image pull")
    add_slide_number(slide, 6)
    add_wordmark(slide)

    # Outer boundary
    add_rect(slide, Inches(0.4), Inches(1.2), Inches(12.5), Inches(5.8),
             border_color=RULE, border_width=Pt(1.5))
    add_textbox(slide, Inches(0.6), Inches(1.25), Inches(4), Inches(0.3),
                "docker compose · single host · offline", font_size=10,
                color=GREY, font_name=FONT_MONO)

    # Client
    c = add_rect(slide, Inches(0.8), Inches(2.0), Inches(1.5), Inches(0.6),
                 fill_color=INK, border_color=INK)
    add_textbox(slide, Inches(0.8), Inches(2.08), Inches(1.5), Inches(0.5),
                "React App\n5 role views", font_size=10, color=WHITE, align=PP_ALIGN.CENTER,
                font_name=FONT_MONO)

    # Gateway
    gw = add_rect(slide, Inches(2.8), Inches(2.0), Inches(1.8), Inches(0.6),
                  fill_color=ACCENT, border_color=ACCENT)
    add_textbox(slide, Inches(2.8), Inches(2.08), Inches(1.8), Inches(0.5),
                "API Gateway\nJWT + Rate Limit", font_size=10, color=WHITE,
                align=PP_ALIGN.CENTER, font_name=FONT_MONO)
    add_arrow(slide, Inches(2.3), Inches(2.3), Inches(2.8), Inches(2.3), color=GREY)

    # Core services row
    services = [
        ("Identity", "RS256 JWT"),
        ("Event", "Rules engine"),
        ("Submission", "JGit scan"),
        ("Judging", "Bayesian"),
        ("Voting", "Quadratic"),
    ]
    sx = Inches(5.0)
    for i, (name, sub) in enumerate(services):
        x = sx + i * Inches(1.55)
        fill = ACCENT if name == "Judging" else None
        bc = ACCENT if name == "Judging" else RULE
        tc = WHITE if name == "Judging" else INK
        add_rect(slide, x, Inches(1.7), Inches(1.4), Inches(0.7),
                 fill_color=fill, border_color=bc)
        add_textbox(slide, x, Inches(1.75), Inches(1.4), Inches(0.6),
                    f"{name}\n{sub}", font_size=9, color=tc,
                    align=PP_ALIGN.CENTER, font_name=FONT_MONO)

    # Arrow from gateway to first service
    add_arrow(slide, Inches(4.6), Inches(2.3), Inches(5.0), Inches(2.1), color=GREY)

    # Async workers row
    workers = [
        ("Notification", "Email/In-app"),
        ("Webhook", "HMAC-SHA256"),
        ("Certificate", "Ed25519"),
        ("Observability", "Audit log"),
    ]
    for i, (name, sub) in enumerate(workers):
        x = sx + i * Inches(1.55)
        add_rect(slide, x, Inches(2.8), Inches(1.4), Inches(0.7),
                 border_color=RULE)
        add_textbox(slide, x, Inches(2.85), Inches(1.4), Inches(0.6),
                    f"{name}\n{sub}", font_size=9, color=INK,
                    align=PP_ALIGN.CENTER, font_name=FONT_MONO)

    # Label rows
    add_textbox(slide, Inches(5.0), Inches(1.35), Inches(3), Inches(0.3),
                "CORE SERVICES", font_size=9, color=GREY, bold=True, font_name=FONT_MONO)
    add_textbox(slide, Inches(5.0), Inches(2.55), Inches(3), Inches(0.3),
                "ASYNC WORKERS", font_size=9, color=GREY, bold=True, font_name=FONT_MONO)

    # Infrastructure band
    infra = [
        ("PostgreSQL 15", "Multi-schema + RLS"),
        ("Redis 7", "Locks & rate limits"),
        ("RabbitMQ", "Topic exchange"),
        ("MinIO", "S3 storage"),
    ]
    iy = Inches(4.3)
    for i, (name, sub) in enumerate(infra):
        x = Inches(2.0) + i * Inches(2.7)
        add_rect(slide, x, iy, Inches(2.4), Inches(0.7), border_color=RULE)
        add_textbox(slide, x, iy + Inches(0.05), Inches(2.4), Inches(0.6),
                    f"{name}\n{sub}", font_size=10, color=INK,
                    align=PP_ALIGN.CENTER, font_name=FONT_MONO)

    add_textbox(slide, Inches(2.0), Inches(4.0), Inches(3), Inches(0.3),
                "INFRASTRUCTURE", font_size=9, color=GREY, bold=True, font_name=FONT_MONO)

    # Arrows from services down to infra
    add_arrow(slide, Inches(7.5), Inches(2.4), Inches(7.5), Inches(4.3), color=RULE)

    # Arrow from RabbitMQ up to async workers
    add_arrow(slide, Inches(8.5), Inches(4.3), Inches(8.5), Inches(3.5), color=RULE)
    add_textbox(slide, Inches(8.6), Inches(3.8), Inches(1.5), Inches(0.3),
                "consume", font_size=8, color=GREY, font_name=FONT_MONO)

    # Circuit breaker annotation
    add_textbox(slide, Inches(9.0), Inches(5.3), Inches(3.5), Inches(0.5),
                "Resilience4j circuit breakers between Judging ↔ Submission",
                font_size=10, color=GREY, font_name=FONT_MONO)

    add_notes(slide, "Speaker: 'This is the entire system. Nine services plus a gateway and a "
              "React frontend. Everything runs inside one docker compose file on a single host. "
              "Once images are pulled, it works fully offline. There is no cloud dependency.'")


def slide_07_data(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "Single Postgres, schema-per-service: simplicity without coupling")
    add_slide_number(slide, 7)
    add_wordmark(slide)

    # Schemas
    schemas = ["auth", "events", "submissions", "judging", "voting", "audit"]
    add_textbox(slide, MARGIN_L, Inches(1.5), Inches(5), Inches(0.4),
                "PostgreSQL schemas", font_size=14, bold=True, color=INK)

    for i, s in enumerate(schemas):
        x = MARGIN_L + (i % 3) * Inches(2.0)
        y = Inches(2.0) + (i // 3) * Inches(0.5)
        add_rect(slide, x, y, Inches(1.8), Inches(0.38), border_color=RULE)
        add_textbox(slide, x, y + Inches(0.05), Inches(1.8), Inches(0.3),
                    s, font_size=12, color=INK, align=PP_ALIGN.CENTER, font_name=FONT_MONO)

    add_textbox(slide, MARGIN_L, Inches(3.2), Inches(5), Inches(0.8),
                "One host, one Postgres instance. Each service owns its schema.\n"
                "Cross-service joins are impossible by design.\n"
                "Flyway manages migrations per schema independently.",
                font_size=13, color=GREY)

    # RabbitMQ layout (right side)
    add_textbox(slide, Inches(7.0), Inches(1.5), Inches(5), Inches(0.4),
                "RabbitMQ topic exchange", font_size=14, bold=True, color=INK)

    # Exchanges (confirmed from RabbitConstants.java)
    exchanges = [
        ("dogfood.audit", "Audit exchange"),
        ("dogfood.scores", "Scores exchange"),
        ("dogfood.webhooks", "Webhooks exchange"),
        ("dogfood.notifications", "Notifications exchange"),
        ("dogfood.certificates", "Certificates exchange"),
    ]
    ex_start_y = Inches(2.1)
    for i, (name, _) in enumerate(exchanges):
        y = ex_start_y + i * Inches(0.38)
        add_rect(slide, Inches(7.0), y, Inches(2.2), Inches(0.3),
                 fill_color=ACCENT, border_color=ACCENT)
        add_textbox(slide, Inches(7.0), y + Inches(0.03), Inches(2.2), Inches(0.25),
                    name, font_size=9, color=WHITE, align=PP_ALIGN.CENTER,
                    font_name=FONT_MONO)

    # Queues (confirmed from RabbitConstants.java)
    queues = [
        "score.normalization",
        "webhook.delivery",
        "webhook.dlq",
        "audit.queue",
        "certificate.generate",
        "notification.events",
    ]
    for i, q in enumerate(queues):
        y = Inches(2.1) + i * Inches(0.38)
        add_rect(slide, Inches(9.8), y, Inches(2.8), Inches(0.3), border_color=RULE)
        add_textbox(slide, Inches(9.8), y + Inches(0.03), Inches(2.8), Inches(0.25),
                    q, font_size=9, color=INK, align=PP_ALIGN.CENTER, font_name=FONT_MONO)

    # Routing keys (confirmed)
    add_textbox(slide, Inches(7.0), Inches(5.2), Inches(5.5), Inches(0.6),
                "Routing keys: score.submitted · webhook.* · notification.*\n"
                "certificate.generate · # (audit catch-all)",
                font_size=10, color=GREY, font_name=FONT_MONO)

    add_notes(slide, "Speaker: 'We chose a single Postgres instance with schema isolation — "
              "not separate databases — because this is a self-hosted product. One host, "
              "one backup, one set of credentials. Cross-service joins are impossible by design.' "
              "Exchange and queue names confirmed from RabbitConstants.java.")


def slide_08_judging_math(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "Z-score normalization fixes both lenient means and compressed ranges")
    add_slide_number(slide, 8)
    add_wordmark(slide)

    # Formula
    add_textbox(slide, MARGIN_L, Inches(1.5), Inches(6), Inches(0.4),
                "z_jc(s) = (raw_score − μ_jc) / σ_jc", font_size=20, bold=True,
                color=ACCENT, font_name=FONT_MONO)

    add_textbox(slide, MARGIN_L, Inches(2.1), Inches(6), Inches(0.5),
                "Per judge j, per criterion c. Corrects both the judge's center\n"
                "and their spread. Min-max normalization only fixes range.",
                font_size=14, color=GREY)

    # Shrinkage formula
    add_textbox(slide, MARGIN_L, Inches(3.0), Inches(6), Inches(0.4),
                "Shrinkage: adj_z = (k/(k+k₀))·z + (k₀/(k+k₀))·μ_global",
                font_size=16, bold=True, color=INK, font_name=FONT_MONO)

    add_textbox(slide, MARGIN_L, Inches(3.5), Inches(6), Inches(0.5),
                "Judges with few reviews (small k) are pulled toward the global\n"
                "mean. Judges with many reviews keep their scores.",
                font_size=14, color=GREY)

    # Worked example table (right side)
    add_textbox(slide, Inches(7.5), Inches(1.5), Inches(5), Inches(0.4),
                "Worked example: 3 judges, 4 projects", font_size=13, bold=True, color=INK)

    headers = ["", "J1 raw", "J2 raw", "J3 raw", "J1 z", "J2 z", "J3 z", "Final"]
    example_rows = [
        ["P1", "9", "7", "8", "+1.3", "+0.5", "+0.9", "0.90"],
        ["P2", "8", "6", "5", "+0.4", "−0.5", "−1.3", "−0.47"],
        ["P3", "5", "8", "7", "−1.3", "+1.5", "+0.1", "+0.10"],
        ["P4", "7", "5", "6", "−0.4", "−1.5", "−0.5", "−0.80"],
    ]

    col_w = Inches(0.65)
    start_x = Inches(7.5)
    start_y = Inches(2.0)
    rh = Inches(0.3)

    for ci, h in enumerate(headers):
        add_textbox(slide, start_x + ci * col_w, start_y, col_w, rh,
                    h, font_size=9, bold=True, color=GREY, align=PP_ALIGN.CENTER,
                    font_name=FONT_MONO)

    add_line(slide, start_x, start_y + rh, start_x + len(headers) * col_w, start_y + rh)

    for ri, row in enumerate(example_rows):
        y = start_y + rh + ri * rh
        for ci, cell in enumerate(row):
            c = GREEN if cell == example_rows[0][-1] else INK
            if ci == len(row) - 1:
                c = ACCENT
            add_textbox(slide, start_x + ci * col_w, y, col_w, rh,
                        cell, font_size=9, color=c, align=PP_ALIGN.CENTER,
                        font_name=FONT_MONO)

    add_textbox(slide, Inches(7.5), Inches(3.5), Inches(5), Inches(0.5),
                "J2 is harsh (mean 6.5). J1 is lenient (mean 7.25).\nAfter normalization, "
                "rank order reflects true quality, not judge temperament.",
                font_size=12, color=GREY)

    add_textbox(slide, MARGIN_L, Inches(4.5), CONTENT_W, Inches(0.5),
                "Final score = Σ (w_c × normalized_z_jc) / |judges|, aggregated per criterion weight.",
                font_size=14, color=INK, font_name=FONT_MONO)

    add_notes(slide, "Speaker: Walk through the worked example. 'J2 gives lower scores overall. "
              "Without normalization, J2's projects would all rank lower. With z-scores, we "
              "correct for J2's harsh center and narrow range. The final rank reflects actual "
              "project quality, not which judge happened to review it.'")


def slide_09_isolation(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "Even an application bug cannot leak another judge's scores")
    add_slide_number(slide, 9)
    add_wordmark(slide)

    # Request path diagram
    steps = [
        ("Judge", "Submits score", INK),
        ("Gateway", "Verify JWT\n(authn only)", ACCENT),
        ("Judging\nService", "Extract judge_id\nfrom JWT claims", INK),
        ("Postgres", "RLS policy:\njudge_id =\ncurrent_setting(...)", GREEN),
    ]

    for i, (label, desc, color) in enumerate(steps):
        x = MARGIN_L + i * Inches(2.8)
        y = Inches(2.2)
        add_rect(slide, x, y, Inches(2.2), Inches(1.2), border_color=color)
        add_textbox(slide, x, y + Inches(0.1), Inches(2.2), Inches(0.4),
                    label, font_size=14, bold=True, color=color,
                    align=PP_ALIGN.CENTER, font_name=FONT_MONO)
        add_textbox(slide, x, y + Inches(0.5), Inches(2.2), Inches(0.6),
                    desc, font_size=11, color=GREY, align=PP_ALIGN.CENTER)

        if i < len(steps) - 1:
            add_arrow(slide, x + Inches(2.2), y + Inches(0.6),
                      x + Inches(2.8), y + Inches(0.6), color=GREY)

    # Blocked branch
    add_rect(slide, Inches(8.5), Inches(4.2), Inches(3.5), Inches(0.6),
             fill_color=RGBColor(0xFE, 0xF2, 0xF2), border_color=AMBER)
    add_textbox(slide, Inches(8.5), Inches(4.25), Inches(3.5), Inches(0.5),
                "✕  Judge requests another judge's scores → BLOCKED",
                font_size=12, color=AMBER, align=PP_ALIGN.CENTER)

    add_textbox(slide, MARGIN_L, Inches(4.2), Inches(7), Inches(0.8),
                "Two independent layers of enforcement.\n"
                "1. Service logic: every query filters by judge_id from JWT, never a request param.\n"
                "2. Postgres RLS: session variable set from JWT claim. Even raw SQL leaks nothing.",
                font_size=13, color=INK)

    # Idempotency annotation
    add_textbox(slide, MARGIN_L, Inches(5.5), CONTENT_W, Inches(0.4),
                "Idempotency-key header + unique DB constraint prevents duplicate score submission.",
                font_size=12, color=GREY, font_name=FONT_MONO)

    add_notes(slide, "Speaker: 'This is the slide that makes security reviewers relax. "
              "Two independent enforcement layers. Even if someone introduces a bug in the "
              "application layer, the database will not return another judge's data.'")


def slide_10_integrity(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "Three controls surround every score before it enters the average")
    add_slide_number(slide, 10)
    add_wordmark(slide)

    panels = [
        ("Calibration round",
         "Judges score 2–3 organizer-picked reference\nsubmissions first. "
         "Miscalibrated judges are\nflagged before real scoring begins."),
        ("COI self-declaration",
         "Judge flags 'I know this team'. Auto-excluded\nfrom assignment. "
         "Exclusion is audit-logged\nand cannot be reversed by the judge."),
        ("Flag / Abstain",
         "Broken or ineligible submissions route to\nthe organizer for review. "
         "They do not\npollute the scoring average."),
    ]

    for i, (title, body) in enumerate(panels):
        x = MARGIN_L + i * Inches(4.0)
        y = Inches(1.8)
        add_rect(slide, x, y, Inches(3.6), Inches(2.2), border_color=RULE)
        add_textbox(slide, x + Inches(0.2), y + Inches(0.15), Inches(3.2), Inches(0.4),
                    title, font_size=14, bold=True, color=ACCENT)
        add_textbox(slide, x + Inches(0.2), y + Inches(0.6), Inches(3.2), Inches(1.4),
                    body, font_size=12, color=GREY)

    # Integrity report
    add_textbox(slide, MARGIN_L, Inches(4.5), CONTENT_W, Inches(0.5),
                "One-click integrity report: method, raw vs normalized distributions, "
                "judges deviating most from consensus, full audit summary.",
                font_size=13, color=INK)

    # Screenshot placeholder
    add_rect(slide, Inches(8.5), Inches(4.8), Inches(4.0), Inches(2.0), border_color=RULE)
    add_textbox(slide, Inches(8.5), Inches(5.5), Inches(4.0), Inches(0.3),
                "[SCREENSHOT: 12-integrity-report.png]",
                font_size=10, color=AMBER, align=PP_ALIGN.CENTER, font_name=FONT_MONO)

    add_notes(slide, "Speaker: 'These three controls mean that no score enters the final "
              "average without passing through calibration validation, conflict-of-interest "
              "exclusion, and the flag/abstain safety valve.'")


def slide_11_forensic(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, INK)
    add_slide_title(slide, "Forensic repo scan flags pre-built submissions for organizer review")
    add_slide_number(slide, 11)
    add_wordmark(slide, color=GREY)

    # Pipeline steps
    pipe_steps = [
        "GitHub URL",
        "JGit clone\n(in-memory)",
        "Walk commit\ngraph",
        "Compare to\nevent window",
        "Compute\nrisk features",
        "Risk Score",
    ]

    for i, step in enumerate(pipe_steps):
        x = MARGIN_L + i * Inches(1.9)
        y = Inches(2.0)
        fill = AMBER if i == len(pipe_steps) - 1 else None
        bc = AMBER if i == len(pipe_steps) - 1 else GREY
        tc = WHITE

        add_rect(slide, x, y, Inches(1.6), Inches(0.7), fill_color=fill, border_color=bc)
        add_textbox(slide, x, y + Inches(0.08), Inches(1.6), Inches(0.55),
                    step, font_size=10, color=tc, align=PP_ALIGN.CENTER,
                    font_name=FONT_MONO)

        if i < len(pipe_steps) - 1:
            add_arrow(slide, x + Inches(1.6), y + Inches(0.35),
                      x + Inches(1.9), y + Inches(0.35), color=GREY)

    # Risk features (confirmed from GitForensicService.java)
    features = [
        "0 commits → 100 risk score",
        "1 commit (monolithic) → 80 risk score",
        ">90% commits before event start → earlyRatio × 100",
        ">50% commits before event start → earlyRatio × 80",
    ]
    add_textbox(slide, MARGIN_L, Inches(3.2), Inches(7), Inches(0.3),
                "Risk scoring heuristics (confirmed from code)", font_size=13, bold=True, color=ACCENT)
    for i, f in enumerate(features):
        add_textbox(slide, MARGIN_L + Inches(0.3), Inches(3.6) + i * Inches(0.35),
                    Inches(7), Inches(0.3), f"·  {f}", font_size=12, color=WHITE)

    # Caveats strip
    add_rect(slide, MARGIN_L, Inches(5.0), Inches(11.5), Inches(1.2),
             border_color=AMBER, border_width=Pt(1))
    caveats = [
        ("Honest caveats", {'size': 12, 'bold': True, 'color': AMBER, 'space_after': 6}),
        ("Git history can be rewritten or squashed.", {'size': 11, 'color': WHITE, 'space_after': 2}),
        ("Private repos require access tokens.", {'size': 11, 'color': WHITE, 'space_after': 2}),
        ("The risk score prioritizes organizer attention — it does not decide outcomes.",
         {'size': 11, 'color': WHITE, 'space_after': 2}),
    ]
    add_multiline(slide, MARGIN_L + Inches(0.2), Inches(5.1), Inches(11), Inches(1.0),
                  caveats)

    # Screenshot placeholder
    add_rect(slide, Inches(8.5), Inches(3.0), Inches(4.0), Inches(1.8), border_color=GREY)
    add_textbox(slide, Inches(8.5), Inches(3.7), Inches(4.0), Inches(0.3),
                "[SCREENSHOT: 07-organizer-risk.png]",
                font_size=10, color=AMBER, align=PP_ALIGN.CENTER, font_name=FONT_MONO)

    add_notes(slide, "Speaker: 'This is the flagship extra. When a participant submits a GitHub "
              "URL, we clone the repo using Eclipse JGit — pure Java, no shell git — into a "
              "temp directory and walk the commit graph. The output is a risk score shown on "
              "the organizer dashboard. It is a signal for review, not a verdict. We are very "
              "honest about the caveats because that is what makes it credible.'  "
              "Thresholds confirmed from GitForensicService.java: 0 commits=100, 1 commit=80, "
              ">90% early=ratio*100, >50% early=ratio*80. Clones to temp dir, not in-memory.")


def slide_12_voting(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "Quadratic voting makes ballot stuffing economically irrational")
    add_slide_number(slide, 12)
    add_wordmark(slide)

    # QV explanation
    add_textbox(slide, MARGIN_L, Inches(1.6), Inches(5.5), Inches(0.4),
                "Cost of n votes on one project = n² credits", font_size=18, bold=True,
                color=INK, font_name=FONT_MONO)

    qv_rows = [
        ["Votes", "1", "2", "3", "4", "5"],
        ["Cost", "1", "4", "9", "16", "25"],
    ]
    col_w = Inches(0.8)
    for ri, row in enumerate(qv_rows):
        for ci, cell in enumerate(row):
            x = MARGIN_L + ci * col_w
            y = Inches(2.3) + ri * Inches(0.35)
            c = GREY if ci == 0 else (ACCENT if ri == 1 else INK)
            b = ci == 0
            add_textbox(slide, x, y, col_w, Inches(0.3),
                        cell, font_size=13, color=c, bold=b,
                        align=PP_ALIGN.CENTER, font_name=FONT_MONO)

    add_line(slide, MARGIN_L, Inches(2.3) + Inches(0.35),
             MARGIN_L + 6 * col_w, Inches(2.3) + Inches(0.35))

    # Anti-abuse measures (right)
    measures = [
        "Seeded ballot shuffle — position bias eliminated",
        "Results hidden during voting period",
        "Token-bucket rate limiting per user",
        "Email + device-fingerprint duplicate detection",
        "One-person-one-vote mode also available",
    ]
    add_textbox(slide, Inches(7.0), Inches(1.6), Inches(5), Inches(0.4),
                "Anti-abuse measures", font_size=14, bold=True, color=INK)
    for i, m in enumerate(measures):
        add_textbox(slide, Inches(7.0), Inches(2.2) + i * Inches(0.4),
                    Inches(5.5), Inches(0.35),
                    f"·  {m}", font_size=13, color=GREY)

    add_notes(slide, "Speaker: 'Quadratic voting means it costs 1 credit for your first vote "
              "on a project, but 25 credits for your fifth. This makes ballot stuffing "
              "economically irrational while still letting passionate supporters express "
              "strong preferences.'")


def slide_13_engineering(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "Six patterns that turn demo code into production code")
    add_slide_number(slide, 13)
    add_wordmark(slide)

    patterns = [
        ("Circuit breakers", "Resilience4j wraps cross-service calls.\nJudging ↔ Submission degrades gracefully."),
        ("Correlation IDs", "Micrometer Tracing propagates a request ID\nacross all services into the audit log."),
        ("Optimistic locking", "@Version on Submission and Event entities.\nConcurrent edits fail fast, not silently."),
        ("Idempotency keys", "Header + unique DB constraint on scores.\nRetries are safe; duplicates are impossible."),
        ("Flyway migrations", "Per-schema, versioned SQL migrations.\nNo drift between environments."),
        ("Testcontainers", "Integration tests spin up real Postgres,\nRedis, RabbitMQ. No mocks for infra."),
    ]

    for i, (title, body) in enumerate(patterns):
        col = i % 3
        row = i // 3
        x = MARGIN_L + col * Inches(4.0)
        y = Inches(1.6) + row * Inches(2.2)

        add_rect(slide, x, y, Inches(3.6), Inches(1.6), border_color=RULE)
        add_textbox(slide, x + Inches(0.2), y + Inches(0.15), Inches(3.2), Inches(0.35),
                    title, font_size=14, bold=True, color=ACCENT, font_name=FONT_MONO)
        add_textbox(slide, x + Inches(0.2), y + Inches(0.55), Inches(3.2), Inches(1.0),
                    body, font_size=12, color=GREY)

    add_notes(slide, "Speaker: 'Each of these patterns prevents a specific failure mode. "
              "This is not architecture tourism — every one was motivated by a real scenario "
              "we encountered during development.'")


def slide_14_observable(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "Organizers can read the audit trail without a database client")
    add_slide_number(slide, 14)
    add_wordmark(slide)

    panels = [
        ("[SCREENSHOT: 09-audit-log.png]", "Filterable audit log — human-readable"),
        ("[SCREENSHOT: 10-ops-dashboard.png]", "Health and uptime dashboard"),
        ("[SCREENSHOT: 11-status-page.png]", "Public /status page"),
    ]

    for i, (placeholder, caption) in enumerate(panels):
        x = MARGIN_L + i * Inches(4.0)
        y = Inches(1.6)
        add_rect(slide, x, y, Inches(3.6), Inches(2.5), border_color=RULE)
        add_textbox(slide, x, y + Inches(1.0), Inches(3.6), Inches(0.3),
                    placeholder, font_size=10, color=AMBER, align=PP_ALIGN.CENTER,
                    font_name=FONT_MONO)
        add_textbox(slide, x, y + Inches(2.6), Inches(3.6), Inches(0.3),
                    caption, font_size=11, color=GREY, align=PP_ALIGN.CENTER)

    add_textbox(slide, MARGIN_L, Inches(5.0), CONTENT_W, Inches(0.5),
                "Every mutation publishes an audit event to RabbitMQ. "
                "The Observability service ingests, stores, and serves them. "
                "Actuator health endpoints are polled for uptime history.",
                font_size=13, color=INK)

    add_notes(slide, "Speaker: 'Organizers should never have to open a database client to "
              "understand what happened. The audit log is filterable, human-readable, and "
              "served from the dashboard. The public /status page gives participants confidence "
              "that the system is live.'")


def slide_15_walkthrough(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "Four screens that show the complete participant-to-results flow")
    add_slide_number(slide, 15)
    add_wordmark(slide)

    screens = [
        ("[SCREENSHOT: 03-gallery.png]", "Gallery with search and filters"),
        ("[SCREENSHOT: 04-submission-form.png]", "Submission wizard"),
        ("[SCREENSHOT: 05-judge-scoring.png]", "Judge scoring — no peer scores visible"),
        ("[SCREENSHOT: 08-judge-progress.png]", "Live judge progress (SSE)"),
    ]

    for i, (placeholder, caption) in enumerate(screens):
        col = i % 2
        row = i // 2
        x = MARGIN_L + col * Inches(6.0)
        y = Inches(1.6) + row * Inches(2.6)

        add_rect(slide, x, y, Inches(5.5), Inches(2.0), border_color=RULE)
        add_textbox(slide, x, y + Inches(0.8), Inches(5.5), Inches(0.3),
                    placeholder, font_size=10, color=AMBER, align=PP_ALIGN.CENTER,
                    font_name=FONT_MONO)
        add_textbox(slide, x, y + Inches(2.05), Inches(5.5), Inches(0.3),
                    caption, font_size=11, color=GREY, align=PP_ALIGN.CENTER)

    add_notes(slide, "Speaker: 'Let me walk you through the flow. A participant submits their "
              "project here. The judge sees only their assigned submissions — no peer scores, "
              "no ordering bias. The organizer watches live progress via server-sent events.'")


def slide_16_threat(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "Honest threat model: what we mitigate and what we do not")
    add_slide_number(slide, 16)
    add_wordmark(slide)

    headers = ["Threat", "Mitigated", "Not mitigated"]
    rows = [
        ["Sybil voting (fake accounts)",
         "Email verification, rate limiting, device fingerprinting",
         "Determined attacker with many real emails"],
        ["Ballot stuffing",
         "Quadratic cost, token-bucket rate limit",
         "Coordinated group buying strategy"],
        ["Submission scraping",
         "Gallery is public by design; no private data exposed",
         "—"],
        ["Judge collusion",
         "COI declarations, z-score outlier detection, audit log",
         "Judges who collude without declaring"],
        ["Deadline gaming",
         "Server-time enforcement with Redis distributed lock",
         "Clock skew > 1 s (unlikely in single-host)"],
        ["Pre-built submissions",
         "JGit forensic scan flags for review",
         "Rewritten git history, squashed commits"],
    ]

    col_widths = [Inches(2.5), Inches(4.5), Inches(4.5)]
    start_y = Inches(1.5)
    rh = Inches(0.55)

    x = MARGIN_L
    for ci, h in enumerate(headers):
        c = GREEN if ci == 1 else (AMBER if ci == 2 else GREY)
        add_textbox(slide, x, start_y, col_widths[ci], Inches(0.3),
                    h, font_size=11, bold=True, color=c)
        x += col_widths[ci]

    add_line(slide, MARGIN_L, start_y + Inches(0.3),
             MARGIN_L + sum(col_widths), start_y + Inches(0.3))

    for ri, row in enumerate(rows):
        y = start_y + Inches(0.35) + ri * rh
        x = MARGIN_L
        for ci, cell in enumerate(row):
            c = INK if ci == 0 else (GREEN if ci == 1 else AMBER)
            if cell == "—":
                c = RULE
            add_textbox(slide, x, y, col_widths[ci], rh,
                        cell, font_size=10, color=c)
            x += col_widths[ci]

    add_notes(slide, "Speaker: 'This is the slide that earns trust. We are explicit about what "
              "we do not mitigate. A determined attacker with many real emails can sybil-vote. "
              "Judges who collude without self-declaring are hard to catch. Git history can be "
              "rewritten. We flag these honestly because security is about layers, not perfection.'")


def slide_17_gaps(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, PAPER)
    add_slide_title(slide, "What is verified, what is not, and what comes next")
    add_slide_number(slide, 17)
    add_wordmark(slide)

    # Verified
    add_textbox(slide, MARGIN_L, Inches(1.6), Inches(5.5), Inches(0.35),
                "Verified", font_size=14, bold=True, color=GREEN)
    verified = [
        "Z-score + Bayesian shrinkage (NormalizationEngineTest: k₀=5, σ=0 edge case)",
        "Role isolation: RLS (RowLevelSecurityIT w/ Testcontainers + Postgres 15)",
        "Idempotency keys on score submission",
        "JGit forensic scan (GitForensicService, 4-tier risk scoring)",
        "Full docker compose lifecycle (9 services + gateway + frontend)",
        ".dogfood.toml claims tiers T1–T4",
    ]
    for i, v in enumerate(verified):
        add_textbox(slide, MARGIN_L + Inches(0.3), Inches(2.1) + i * Inches(0.32),
                    Inches(5.5), Inches(0.3),
                    f"✓  {v}", font_size=12, color=INK)

    # Honest gaps
    add_textbox(slide, Inches(7.0), Inches(1.6), Inches(5.5), Inches(0.35),
                "Honest gaps", font_size=14, bold=True, color=AMBER)
    gaps = [
        "No internationalization (i18n)",
        "No horizontal scaling (single-host only)",
        "No OAuth / SSO integration yet",
        "Bradley-Terry pairwise mode: not yet built",
        "Mobile-responsive UI: basic, not polished",
        "Load testing: not performed",
    ]
    for i, g in enumerate(gaps):
        add_textbox(slide, Inches(7.0) + Inches(0.3), Inches(2.1) + i * Inches(0.32),
                    Inches(5.5), Inches(0.3),
                    f"—  {g}", font_size=12, color=GREY)

    add_notes(slide, "Speaker: 'We want to be transparent about what is tested and what is not. "
              "The core judging math has unit tests. Role isolation has integration tests with "
              "Testcontainers. But we have not done load testing, we do not have OAuth, and "
              "the mobile experience needs work.'  "
              "[CONFIRM: check .dogfood.toml and acceptance-report.txt for tier claims.]")


def slide_18_run_it(prs):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_slide_bg(slide, INK)
    add_slide_number(slide, 18)
    add_wordmark(slide, color=GREY)

    add_textbox(slide, MARGIN_L, Inches(2.2), CONTENT_W, Inches(0.5),
                "Run it yourself.", font_size=44, bold=True, color=WHITE)

    add_textbox(slide, MARGIN_L, Inches(3.2), Inches(8), Inches(0.5),
                "git clone https://github.com/codewisp-ai/DogFood && cd DogFood && docker compose up",
                font_size=16, color=ACCENT, font_name=FONT_MONO)

    add_textbox(slide, MARGIN_L, Inches(4.2), Inches(6), Inches(0.3),
                "Offline after image pull  ·  MIT License", font_size=16, color=GREY)

    add_textbox(slide, MARGIN_L, Inches(5.2), Inches(8), Inches(0.3),
                "github.com/codewisp-ai/DogFood", font_size=20, bold=True, color=ACCENT,
                font_name=FONT_MONO)

    add_notes(slide, "Speaker: 'One command. That is the product. Everything we showed you "
              "is in this repo, running right now. Thank you.' — Stop here. Do not add "
              "'questions?' verbally either; let the audience drive.")


# ─── Main ─────────────────────────────────────────────────────────────────────

def main():
    prs = Presentation()
    prs.slide_width = SLIDE_W
    prs.slide_height = SLIDE_H

    slide_01_title(prs)
    slide_02_problem(prs)
    slide_03_comparison(prs)
    slide_04_lifecycle(prs)
    slide_05_roles(prs)
    slide_06_architecture(prs)
    slide_07_data(prs)
    slide_08_judging_math(prs)
    slide_09_isolation(prs)
    slide_10_integrity(prs)
    slide_11_forensic(prs)
    slide_12_voting(prs)
    slide_13_engineering(prs)
    slide_14_observable(prs)
    slide_15_walkthrough(prs)
    slide_16_threat(prs)
    slide_17_gaps(prs)
    slide_18_run_it(prs)

    out_path = os.path.join(os.path.dirname(__file__), "Dogfood-Pitch.pptx")
    prs.save(out_path)
    print(f"✓ Saved {out_path} ({len(prs.slides)} slides)")

    # Also create deck-notes.md
    notes_path = os.path.join(os.path.dirname(__file__), "deck-notes.md")
    with open(notes_path, "w") as f:
        f.write("""# Deck Notes — Dogfood Pitch

## CONFIRM items (verify before presenting)

- [ ] Slide 2, 3: Re-verify incumbent platform (Devpost, Devfolio, DoraHacks, HackerEarth, Unstop) feature gaps
- [ ] Slide 7: Confirm exact RabbitMQ exchange and queue names against `RabbitConstants.java`
- [ ] Slide 11: Confirm forensic scan thresholds and risk-score weights from `ForensicScanService.java`
- [ ] Slide 17: Check `.dogfood.toml` and `acceptance-report.txt` for tier claims
- [ ] Slide 17: Confirm whether Bradley-Terry pairwise mode is implemented

## Missing screenshots

All screenshot slots are currently placeholders. Capture from the running seeded portal at 1440×900:

| Slot | File needed |
|------|-------------|
| Slide 5 | `01-portal-home.png` |
| Slide 10 | `12-integrity-report.png` |
| Slide 11 | `07-organizer-risk.png` |
| Slide 14 | `09-audit-log.png`, `10-ops-dashboard.png`, `11-status-page.png` |
| Slide 15 | `03-gallery.png`, `04-submission-form.png`, `05-judge-scoring.png`, `08-judge-progress.png` |

## Unverified claims

- Bradley-Terry pairwise mode: listed as "not yet built" (slide 17) unless repo confirms otherwise
- Test coverage numbers: not stated (no invented percentages)
- Load testing: explicitly noted as not performed
- License: listed as MIT — confirm against LICENSE file in repo

## Font requirements

The deck uses IBM Plex Sans and IBM Plex Mono. If these are not installed on the presentation machine,
PowerPoint will fall back to Calibri / Consolas. Install the fonts from:
https://github.com/IBM/plex
""")
    print(f"✓ Saved {notes_path}")


if __name__ == "__main__":
    main()
