# NotablyMD Roadmap

## Recently Completed

1. **Widget Customization & Control** — icon selection, color themes, behavior config (record / open app / last note), quick settings tile themes
2. **Quick Voice Recording Triggers** — assistant integration, quick settings tile, hardware button combos (power + volume), device admin receiver, unified settings
3. **Bug fixes: widget & voice recorder** — FGS microphone error fixed, first-use permission flow, widget click response, settings screen crashes
4. **Voice-to-Note Widget** — one-tap recording from home screen, speech-to-text, auto note creation, 850+ lines of test coverage
5. **Phase 3 UI: Sync + Conflict + Migration** — sync status toolbar indicators, conflict resolution dialog, migration utility with batch processing

---

## In Progress

- **Phase 5.5 manual testing** — hardware button triggers and voice assistant integration need device testing; automated tests pass but behavior depends on OS-level permissions

---

## Prioritized Backlog

### High Priority

1. **Complete markdown write-back** (`EnhancedMarkdownManager`)
   - `MarkdownUtils.convertToMarkdown()` not yet implemented — write-back is stubbed
   - Attachments (images, audio, files, reminders) not parsed from markdown on read
   - Span→markdown conversion in `MigrationEngine` incomplete
   - Core of the markdown-first promise; nothing is truly portable without this
   - ~8-10h

2. **Complete bidirectional sync** (`BidirectionalSync`)
   - File→database sync not implemented (only DB→file works)
   - File deletion events not handled
   - Blocks sync being truly bidirectional
   - ~4-6h

3. **Unified Import/Export** (Phase 5.6)
   - Replace separate export/import menu items with single "Backup & Restore" flow
   - Auto-detect file type on import (.zip, .json, .md)
   - Clear naming convention (`NotablyMD_Backup_YYYYMMDD.zip`)
   - ~4-6h

### Medium Priority

4. **Search autofocus** (`SearchFragment`)
   - Keyboard doesn't auto-show when search opens — minor but noticeable friction
   - Quick win: ~1h

5. **Markdown preview in note list**
   - Show rendered markdown snippet instead of raw `**bold**` text in list/grid view
   - High UX impact for markdown-first users
   - ~4-6h

6. **Single-note export as .md**
   - One-tap export of individual note to a `.md` file
   - Foundational for Obsidian/Logseq interop
   - ~2-3h

7. **Enhanced Security** (Phase 5.1)
   - Encrypt individual markdown files using existing SQLCipher/biometrics infrastructure
   - Key management integration
   - ~8-12h

### Lower Priority / Future

8. **Configurable Voice Assistant** (Phase 5.7)
   - Custom wake phrase training and detection
   - Feasibility on Android is uncertain (system assistants resist being overridden)
   - ~12h+, revisit after manual testing of 5.5 assistant integration

9. **Obsidian/Logseq compatibility audit**
   - Verify exported markdown opens correctly in popular editors
   - Check YAML frontmatter compatibility
   - ~2-3h

10. **Collaboration** (Phase 5.3)
    - Device attribution in frontmatter, change tracking, selective sync by label/folder
    - Stretch goal — depends on sync being solid first
    - ~16h+

---

## Recommended Next 3

1. **Complete markdown write-back** — the entire markdown-first value prop is hollow until notes actually round-trip through `.md` files correctly. Unblocks sync, migration, and Obsidian compat.
2. **Complete bidirectional sync** — once write-back works, this makes sync genuinely two-way instead of one-directional.
3. **Search autofocus** — tiny effort, fixes something users will notice every time they search.

---

## Strategic Notes

- The markdown infrastructure (sync engine, conflict resolution, migration) is largely built — the gap is in the actual markdown serialization/deserialization being complete. Close that gap before adding more features on top of a shaky foundation.
- Voice recording features are feature-complete; focus should shift to core note fidelity.
- Hardware button and voice assistant triggers depend on OS-level permissions that vary by device — keep expectations realistic and document the limitations clearly.
