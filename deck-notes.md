# Deck Notes — Dogfood Pitch

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
