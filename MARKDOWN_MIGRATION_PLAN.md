# NotablyMD Markdown-First Migration Plan (NotallyX Edition)

## Overview

Transform NotallyX from SQLite-first to **markdown-first storage** leveraging existing CommonMark support and advanced import/export capabilities.

## Architecture Goals

### Primary Objectives
- ✅ **Markdown-first**: Notes stored as portable .md files
- ✅ **Cross-device sync**: Compatible with Syncthing/Dropbox  
- ✅ **Enhanced security**: Leverage existing SQLCipher encryption
- ✅ **Rich import/export**: Build on existing Evernote/Google Keep importers

---

## Phase 1: Leverage Existing Infrastructure (Week 1)

### 1.1 Existing Assets Analysis ✅

#### Already Available in NotallyX:
- ✅ **CommonMark Processing**: `org.commonmark:commonmark:0.27.0`
- ✅ **Markdown Import/Export**: `MarkdownUtils.kt` with full CommonMark + GFM support
- ✅ **Advanced Importers**: Evernote, Google Keep, JSON, Plain Text
- ✅ **Security Framework**: SQLCipher encryption, Biometric lock
- ✅ **Backup System**: Auto-backups with password protection
- ✅ **Rich Media Handling**: Advanced image/audio support

#### Missing Components:
- 🔄 **File Watching**: Need external file change detection
- 🔄 **Bidirectional Sync**: DB ↔ Markdown synchronization
- 🔄 **Conflict Resolution**: Multi-device conflict handling

### 1.2 Enhanced MarkdownManager

Extend existing `MarkdownUtils.kt`:

```kotlin
class EnhancedMarkdownManager {
    // Leverage existing parseBodyAndSpansFromMarkdown()
    suspend fun readNote(file: File): Result<BaseNote>
    suspend fun writeNote(note: BaseNote, file: File): Result<Unit>
    suspend fun generateYAMLFrontmatter(note: BaseNote): String
    suspend fun parseYAMLFrontmatter(content: String): Result<NoteMetadata>
}
```

### 1.3 File Structure Design

```
/UserSelectedLocation/
├── Notes/
│   ├── 2024-01-15-my-note-title.md
│   └── ...
├── Images/
├── Audio/
└── .notablymd/
    ├── index.json
    ├── cache/
    ├── sync.log
    └── encryption.key
```

---

## Phase 2: Sync & Migration (Week 2)

### 2.1 FileWatcher Implementation

```kotlin
class FileWatcher {
    fun watchDirectory(directory: File): Flow<FileChangeEvent>
    fun startWatching()
    fun stopWatching()
}
```

### 2.2 SyncEngine

```kotlin
class SyncEngine {
    suspend fun syncMarkdownToDB(): Result<SyncResult>
    suspend fun syncDBToMarkdown(): Result<SyncResult>
    suspend fun detectConflicts(): Result<List<Conflict>>
}
```

### 2.3 Migration Utility

Leverage existing importers:

```kotlin
class MigrationUtility {
    suspend fun migrateDatabaseToMarkdown(database: NotallyDatabase)
    suspend fun importExistingBackups()
    // Use existing NotesImporter framework
}
```

---

## Phase 3: UI Integration (Week 3)

### 3.1 Enhanced Settings

Build on existing preferences:
- **Storage Location Picker**: Folder browser
- **Sync Configuration**: Frequency, conflict handling
- **Security Integration**: Encrypt markdown files

### 3.2 Sync Status Indicators

Add to existing UI:
- Sync status in main toolbar
- Conflict notifications
- Progress indicators

### 3.3 Conflict Resolution UI

New dialog components:
- Auto-merge vs manual resolution
- Side-by-side diff viewer
- Version history

---

## Phase 4: Advanced Features (Week 4)

### 4.1 Enhanced Security

- Encrypt individual markdown files
- Secure key management with existing biometrics
- Protected backup integration

### 4.2 Performance Optimization

- Leverage existing caching systems
- Incremental indexing
- Smart preloading

### 4.3 Collaboration Features

- Device attribution in frontmatter
- Change tracking
- Selective sync by labels/folders

---

## Implementation Advantages with NotallyX

### ✅ **Head Start Benefits**
1. **CommonMark Integration**: Already implemented and tested
2. **Import Framework**: Support for 5+ formats
3. **Security Foundation**: SQLCipher + biometrics ready
4. **Backup System**: Auto-backups with encryption
5. **Rich Media**: Advanced image/audio handling
6. **Testing Framework**: Comprehensive test coverage

### 🔄 **Development Acceleration**
- **50% less work** - Markdown parsing already done
- **Security ready** - Encryption framework exists  
- **Import/Export** - Rich format support built-in
- **UI Foundation** - Settings and preferences mature

---

## Updated Timeline

| Week | Phase | Key Deliverables |
|------|-------|------------------|
| 1 | Phase 1 | Enhanced MarkdownManager, file structure |
| 2 | Phase 2 | FileWatcher, SyncEngine, migration utility |
| 3 | Phase 3 | Settings UI, sync indicators, conflict UI |
| 4 | Phase 4 | Security integration, performance optimization |

**Total: 4 weeks (vs 6 weeks originally)**

---

## Next Steps

1. **Analyze existing MarkdownUtils.kt** in detail
2. **Design file watching integration** 
3. **Plan sync engine architecture**
4. **Create enhanced settings screens**
5. **Implement migration wizard**

---

*This updated plan leverages NotallyX's significant existing infrastructure to accelerate markdown-first development by 50% while adding enterprise-grade security and import capabilities.*
