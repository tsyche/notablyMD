# NotablyMD Shipped Roadmap Items

**TL;DR:** Compact index of completed roadmap work followed by the original
entries, retained as project history rather than cluttering the active roadmap.

## Shipped index

- [x] 2026-06-11 — Complete markdown write-back
- [x] 2026-06-11 — Complete bidirectional sync
- [x] 2026-06-11 — Search autofocus
- [x] 2026-01-31 — Voice-to-Note Widget
- [x] 2026-01-30 — Phase 3 UI: Sync + Conflict + Migration

## Archived entries

### 2026-06-11 — Complete markdown write-back

**Complete markdown write-back** (`EnhancedMarkdownManager`) —
`convertToMarkdown`/`parseBodyAndSpansFromMarkdown` wired in; GFM task list
serialization/deserialization for Type.LIST notes.

### 2026-06-11 — Complete bidirectional sync

**Complete bidirectional sync** (`BidirectionalSync`) — `syncFromFileToDatabase`
and `handleFileDeleted` implemented using DAO; file→DB path now functional.

### 2026-06-11 — Search autofocus

**Search autofocus** (`SearchFragment`) — keyboard auto-shows on initial
navigation to search screen via `post {}` fix.

### 2026-01-31 — Voice-to-Note Widget

**Voice-to-Note Widget** — one-tap recording from home screen, speech-to-text,
auto note creation, 850+ lines of test coverage.

### 2026-01-30 — Phase 3 UI: Sync + Conflict + Migration

**Phase 3 UI: Sync + Conflict + Migration** — sync status toolbar indicators,
conflict resolution dialog, migration utility with batch processing.
