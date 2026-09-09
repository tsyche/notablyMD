# NotablyMD Roadmap

**Shipped history:** [ROADMAP_SHIPPED.md](ROADMAP_SHIPPED.md)

## In Progress

- **Phase 5.5 manual testing** — hardware button triggers and voice assistant integration need device testing; automated tests pass but behavior depends on OS-level permissions

---

## Prioritized Backlog

### High Priority

1. **Unified Import/Export** (Phase 5.6)
   - Replace separate export/import menu items with single "Backup & Restore" flow
   - Auto-detect file type on import (.zip, .json, .md)
   - Clear naming convention (`NotablyMD_Backup_YYYYMMDD.zip`)
   - ~4-6h

### Medium Priority

2. **Markdown preview in note list**
   - Show rendered markdown snippet instead of raw `**bold**` text in list/grid view
   - High UX impact for markdown-first users
   - ~4-6h

3. **Single-note export as .md**
   - One-tap export of individual note to a `.md` file
   - Foundational for Obsidian/Logseq interop
   - ~2-3h

4. **Enhanced Security** (Phase 5.1)
   - Encrypt individual markdown files using existing SQLCipher/biometrics infrastructure
   - Key management integration
   - ~8-12h

### Lower Priority / Future

5. **Configurable Voice Assistant** (Phase 5.7)
   - Custom wake phrase training and detection
   - Feasibility on Android is uncertain (system assistants resist being overridden)
   - ~12h+, revisit after manual testing of 5.5 assistant integration

6. **Obsidian/Logseq compatibility audit**
   - Verify exported markdown opens correctly in popular editors
   - Check YAML frontmatter compatibility
   - ~2-3h

7. **Collaboration** (Phase 5.3)
   - Device attribution in frontmatter, change tracking, selective sync by label/folder
   - Stretch goal — depends on sync being solid first
   - ~16h+

---

## Recommended Next 3

1. **Unified Import/Export** — replace the fragmented export/import menu items; auto-detect on import; clear naming convention.
2. **Markdown preview in note list** — high-visibility UX improvement for the markdown-first audience.
3. **Obsidian/Logseq compatibility audit** — now that write-back works, verify the round-trip actually opens correctly in popular editors.

---

## Strategic Notes

- The markdown infrastructure (sync engine, conflict resolution, migration) is largely built — the gap is in the actual markdown serialization/deserialization being complete. Close that gap before adding more features on top of a shaky foundation.
- Voice recording features are feature-complete; focus should shift to core note fidelity.
- Hardware button and voice assistant triggers depend on OS-level permissions that vary by device — keep expectations realistic and document the limitations clearly.
