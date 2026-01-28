---
title: ❓ FAQ
sidebar_position: 9
---

# FAQ - Frequently Asked Questions

This page answers common questions about NotablyMD. If you don't find the answer you're looking for, please check the [GitHub issues](https://github.com/tsyche/NotablyMD/issues) or create a new issue.

## General Questions

### What is NotablyMD?

NotablyMD is a minimalistic yet powerful note-taking app for Android. It's an extended version of the original Notally app, with additional features and improvements. The app focuses on providing a clean, intuitive interface while offering rich functionality.

### Is NotablyMD free?

Yes, NotablyMD is completely free and open-source. There are no in-app purchases, subscriptions, or advertisements.

### What devices does NotablyMD support?

NotablyMD supports Android devices running Lollipop (Android 5.0) and above.

### Where can I download NotablyMD?

You can download NotablyMD from:
- [Google Play Store](https://play.google.com/store/apps/details?id=com.tsyche.notablymd)
- [F-Droid](https://f-droid.org/en/packages/com.tsyche.notablymd)
- [IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/com.tsyche.notablymd)
- [GitHub Releases](https://github.com/tsyche/NotablyMD/releases)

## Data and Privacy

### Where are my notes stored?

Your notes are stored locally on your device. NotablyMD does not upload your data to any cloud service unless you explicitly use a cloud storage service to back up your notes.

### Does NotablyMD collect any data about me?

No, NotablyMD does not collect any personal data or usage statistics. Your notes remain private on your device.
For the up-to-date privacy policy, see [Privacy-Policy](https://github.com/tsyche/NotablyMD/blob/main/Privacy-Policy.md)

### What happens to my notes if I uninstall the app?

If you uninstall NotablyMD, your notes will be deleted unless you've created a backup. We recommend creating a backup before uninstalling the app.

### Can I transfer my notes to a new device?

Yes, you can use the backup and restore feature to transfer your notes to a new device:
1. Create a backup on your old device
2. Transfer the backup file to your new device
3. Install NotablyMD on your new device
4. Use the import backup and settings feature to import your notes

## Features and Usage

### Can I sync my notes across multiple devices?

NotablyMD doesn't include built-in sync functionality. However, you can use third-party cloud storage services to sync your backup files across devices.
See [Settings](settings.mdx)

### Can I recover deleted notes?

When you delete a note it first only moved into the "Deleted" notes. If you deleted notes from the "Deleted" notes they are permanently gone.
If you've accidentally deleted a note, you can restore it from a backup if you have one. Otherwise, deleted notes cannot be recovered.

### Why are my checked list items moving to the bottom?

By default, NotablyMD automatically sorts checked items to the bottom of lists. You can disable this feature in "Settings" > "Behavior" >  "Sort List items".

### Can I use NotablyMD for password storage?

While you can lock the app itself, I don't recommend using NotablyMD as a password manager. Dedicated password management apps offer more security features specifically designed for storing sensitive credentials.

### How do I create a reminder for a note?

1. Inside a Note, tap the '...' button in the bottom right corner
2. Tap "Reminders"
3. Select a date and time for the reminder
4. Select if the reminder should repeated and if yes in what interval
5. Tap "Save"
6. When the configured point in time is reached, you will get a notification for the note

### Can I change the font in notes?

Currently, NotablyMD uses the system font and doesn't support changing to different fonts. However, you can use change the text size in "Settings" > "Appearance" > "Text Size"

## Troubleshooting

### The app crashed. What should I do?

If NotablyMD crashes, you'll see a crash screen that allows you to report the issue directly via `Report bug with crash log`.
If the app crashed and you did not see a crash screen:
1. Re-open the app (if possible)
2. From the Overview, tap the sidebar menu icon
3. Tap "Settings" > In "About" section > "Send Feedback" > "Report Bug"
4. This will open a browser to the Github create Issue site
5. If not already logged in, please login or create a new Github account
6. A bug report template will be pre filled with useful information for the developers
7. Please fill in the missing fields and provide a detailed description of how the bug/crash occurred

Please use this feature to help improve the app.

### My notes disappeared. How can I recover them?

If your notes have disappeared, try the following:
1. Restart the app
2. Check if you're viewing a specific label (check the sidebar)
3. Try searching for your notes
4. Restore from a backup if available

### Why can't I create or edit notes?

This could be due to:
1. Storage permission issues - check that NotablyMD has storage permissions
2. Storage space - ensure your device has available storage
3. App data corruption - try clearing the app cache (not data) in your device settings

### The app is slow. How can I improve performance?

If you're experiencing performance issues:
1. Reduce the number of notes by archiving or deleting unnecessary ones
2. Disable auto-sort for checked items if you have very long lists
3. Ensure your device has sufficient free storage

## Beta Testing and Development

### How can I try new features before they're released?

You can join the beta program:
1. Download the most recent BETA release [from GitHub](https://github.com/tsyche/NotablyMD/releases/tag/beta)
2. Install the BETA version (it will be installed as a separate app called "NotablyMD BETA")
3. Provide feedback on new features

#### Will the beta version affect my regular NotablyMD data?

No, the beta version uses a separate data store and won't affect your regular NotablyMD app data.

### How can I contribute to NotablyMD?

See our [Contribution Guidelines](contributing.md) for information on how to contribute to the project.

### I found a bug. How do I report it?

- From inside the app: Tap "Settings" > In "About" section > "Send Feedback" > "Report Bug" (this will prefill the last crash logs and other useful information)
- You can also report bugs directly by [creating a new issue](https://github.com/tsyche/NotablyMD/issues/new/choose) on GitHub. Please include as much detail as possible, including steps to reproduce the issue.
