# Mudita Flashcards

A calm, deliberate flashcards app for the [Mudita Kompakt](https://mudita.com/products/phones/mudita-kompakt/) — an E Ink phone designed for digital well-being. Built with the [Mudita Mindful Design (MMD)](https://github.com/mudita/MMD) framework as an entry to the **Mudita Mindful App Design Challenge** (May–June 2026).

> Spokojna nauka bez presji. — *Calm learning, no pressure.*

---

## Project Overview

### 1. The problem we are answering

Popular flashcard apps — Anki, Duolingo, Quizlet — are engineered to maximise engagement. They surround a simple act of learning with timers, streaks, points, badges, and notifications. These mechanics are well-documented Dark Patterns: they harvest attention and replace intrinsic motivation with anxiety about losing what the app has given you.

On a phone that was deliberately built to give attention back, those mechanics are not just unwelcome — they are antithetical to the device. Mudita Flashcards is a small answer to one specific question: *what does a flashcards app look like when it serves the user, not the other way around?*

### 2. Mindful angle — "calm learning, no pressure"

The app has no opinion on how much you should study today. It does not measure how long you spend on a card. It does not reward streaks. It does not notify. You open it when you decide to; you close it when you decide to. Stopping on a hard card is fine — that is part of learning, not a failure.

The paradigm is *notebook, not game*.

### 3. Bright Patterns — what we deliberately do not include, and why

| Dark Pattern (where it lives) | Why we reject it | What we do instead |
|---|---|---|
| Timer on each card (Duolingo, Quizlet) | Time pressure converts learning into a race and harms long-term retention | No timer, anywhere, ever |
| Streaks (Duolingo, Anki) | Punishes any break — sickness, travel, a busy week — and replaces motivation with fear of losing points | The app does not remember when it was last opened |
| Points, ranks, badges (Quizlet, Duolingo) | Builds external motivation that collapses when rewards stop being enough | Knowledge is the only reward; we offer no substitutes |
| Push notifications (Anki, Duolingo) | Interrupts focus, manufactures obligation toward the app | No notifications; the app has no notification channels |
| "You have 47 cards overdue" (Anki) | Cognitive debt that demotivates after any absence | No persistent SRS state — each session starts fresh |
| Progress bar within a session (Quizlet) | Pressure to finish; endowment effect | No progress bar, no "X of Y cards" counter |
| Card-flip animations (Quizlet) | Causes ghosting on E Ink and is gratuitous decoration | Instant content swap, no animations of any kind |
| Snackbar / popup after a correct answer (Duolingo) | Sudden interruption, infantilising of learning | Silence after every answer |
| Real-time statistics (Anki) | Pulls attention from content to metrics | No live statistics |
| Recommended decks (Quizlet) | Directs the user's attention to content the app wants them to see | The deck list is exactly your folder of CSV files, nothing more |

A few patterns we *do* embrace, because they shift power toward the user:

- **Sessions have no end.** You leave when you choose to. There is no "you completed the deck" screen.
- **The algorithm is silent.** Weighted random sampling biases toward less-seen and "hard"-marked cards, but no weights or counts are shown — the work is invisible.
- **"Still learning" is a navigation tool, not a verdict.** Marking a card as hard biases sampling within this session only; it is forgotten when you leave.
- **You own the content.** Decks are CSV files on your phone. You create them on a computer, with AI or by hand, and copy them over via USB. The app is a reader, not an editor or a curator.
- **Self-healing help.** `template.csv` and `instructions.md` are restored to their original contents on every launch — you cannot accidentally lock yourself out of the help by deleting it.
- **Grade-before-flip is allowed.** Both the "Still learning" (✗) and "Know" (✓) buttons are always visible — you can mark a card without flipping it if you already know the answer.

### 4. Architecture and technical implementation

- **Language and stack.** Kotlin, Jetpack Compose, single-Activity, no ViewModel, no Room, no DI. MMD 1.0.1 from Maven Central. JVM 17. Compose plugin 2.x. `minSdk = 31` (MuditaOS K = AOSP 12 = API 31), `targetSdk = 36`, `compileSdk = 36`.
- **UI framework.** Every visible UI element is a Mudita Mindful Design component (`ThemeMMD`, `TopAppBarMMD`, `TextMMD`, `ButtonMMD`, `OutlinedButtonMMD`, `HorizontalDividerMMD`, `LazyColumnMMD`). Only layout primitives (`Scaffold`, `Box`, `Column`, `Row`, `Spacer`) and the `IconButton`/`Icon` slots inside `TopAppBarMMD` use raw Material 3 — exactly as the MMD documentation itself does.
- **Colours.** `MaterialTheme.colorScheme` only, i.e. pure black (`#000000`) and pure white (`#FFFFFF`). No greys. No alpha for "subtlety". Hierarchy is communicated through font size, not colour.
- **Typography.** `MaterialTheme.typography` only — Lato Medium across the board. No raw `fontSize`.
- **Storage.** Decks are CSV files at `/storage/emulated/0/Flashcards/` (top-level Internal Shared Storage, sitting next to `Documents`, `Music`, `Pictures` etc.). The app requests `MANAGE_EXTERNAL_STORAGE` once on first launch; after that, you copy CSV files in and out through Windows Explorer (or Android File Transfer / mtpfs on macOS / Linux) over USB-C. No proprietary tool, no Mudita Center step. Decks survive uninstall.
- **No database.** No Room, no SQLite, no DataStore. Session state lives in RAM for the duration of a session and is discarded when you leave — by design.
- **CSV format.** Line 1 is the deck name; line 2 onward is `front;back`. Semicolons are reserved as the separator. UTF-8, optional BOM. Long sides (over 80 / 300 characters) are truncated with a visible `…` rather than rejected, so the user sees the signal without losing the card.
- **Weighted random sampling.** `weight = 1 / (timesShown + 1) × (2.0 if marked hard, else 1.0)`. Never-seen cards get the highest priority; hard cards get double weight against equally-shown easy cards; the last-shown card is excluded from the next pick to prevent immediate repetition.
- **Immutable session state.** `SessionState` is a `data class` mutated only through `.copy()`. Per-card session state (`timesShown`, `isHard`) lives in a `Map<Int, CardSessionMeta>` keyed by index — `FlashCard` itself is pure content from disk. Compose recomposition is predictable because every change replaces the whole state.
- **Self-healing managed assets.** `template.csv` and `instructions.md` are bundled as Android `assets/`. On every app launch and every return to the root folder, the app compares the SHA-256 hash of each file on disk with the bundled original and rewrites it if it has been deleted, edited or corrupted. The user's own decks are never touched.
- **On_resume rescan.** When the app returns to the foreground (after the user has uploaded files via USB), the deck list is rescanned automatically.

### 5. E Ink design decisions

- **Portrait, 480 × 800.** MuditaOS K renders applications in portrait by default; the 800 × 480 panel is rotated. The activity is locked to `portrait`.
- **Static layouts, no animations.** The only motion in the app is a *deep refresh flash* — a brief full-screen black/white sequence (~360 ms) used once, at the moment a session starts, to clear ghosting from the previous screen. This pattern was developed in the earlier TicTacToe project on the same device. There is no `animate*`, no `Crossfade`, no `AnimatedVisibility`.
- **Slot-based layout for the session screen.** S3 (front) and S4 (back) are one composable with five fixed-height slots: top bar, echo of the front, card content (the only `weight(1f)` zone), the flip control, and the bottom bar. The slots that "disappear" — the echo on S3, the flip button on S4 — are replaced by a `Spacer` of identical height rather than collapsed, so the card content's pixel position never shifts when you flip. On E Ink this is the difference between a clean transition and a smudge of ghosting in the centre of the screen.
- **Buttons always present.** The "Still learning" (✗) and "Know" (✓) buttons are pinned in the `Scaffold` bottom bar across both states of the session screen. You can grade a card without ever flipping it.
- **Step-scrolled lists.** `LazyColumnMMD` provides discrete-step scrolling instead of continuous panning, which is what E Ink wants.
- **Visible controls only.** The MMD guidance is explicit: gestures must be supported by visible controls. There is no tap-the-card-to-flip hidden gesture; instead, a full-width `OutlinedButtonMMD` labelled "Flip the card" sits between the card and the grade buttons.
- **No reliance on colour or alpha.** Front-of-card "echo" on the back-of-card screen is rendered smaller (`bodySmall`, 15sp) rather than dimmed.

### 6. Screens

Five screens in total:

| ID | Name | Purpose |
|---|---|---|
| S1 | Browser | Current folder's contents — sub-folders prefixed `▸`, decks prefixed `≡` |
| S2 | Deck preview | Deck name, card count, scrollable list of card fronts, pinned "Start Session" |
| S3/S4 | Card session | Single composable with `isFlipped`, slot-based, fixed-position grade buttons |
| S5 | Empty / error | Variants A (no decks), B (empty folder), C (broken CSV), D (zero cards), E (storage unavailable) |
| — | Permission | One-time prompt to grant `MANAGE_EXTERNAL_STORAGE` so the Flashcards folder can live at the top level of internal storage |

### 7. Creating decks

Decks are CSV files. The first line is the deck name; subsequent lines are `front;back`. Place them anywhere inside the `Flashcards` folder — sub-folders to arbitrary depth become the navigation tree in the app.

Example: `chinese_basics.csv`

```
Chinese, Basics
你好;Hello (Ni hao)
谢谢;Thank you (Xiexie)
再见;Goodbye (Zaijian)
```

Full guidelines, including the recommended AI-assisted workflow (paste `instructions.md` into ChatGPT or Claude and ask for a deck on a topic), are bundled with the app as `instructions.md` and restored on every launch.

---

## Building from source

Prerequisites: Android Studio (Ladybug or newer), JDK 17, Android SDK with API 36.

```bash
git clone https://github.com/Mickud76ai/mudita-flashcards.git
cd mudita-flashcards
./gradlew assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`. Sideload onto Kompakt via [WebADB](https://app.webadb.com/) or `adb install`.

On Windows, if you run Gradle outside Android Studio, set `JAVA_HOME` to the bundled JBR:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

### First-run setup

1. Install the APK on Kompakt.
2. Open the app — the first screen asks you to grant **All files access**. Tap *Open settings*, enable the toggle, return to the app.
3. The app creates `/storage/emulated/0/Flashcards/` with `template.csv` and `instructions.md` inside, and displays "No decks found".
4. Connect Kompakt to a computer via USB-C; the Flashcards folder is visible at the top of *Internal shared storage* in Windows Explorer. Copy your CSV decks (and sub-folders) into it.
5. Return to the app — the list refreshes automatically.

---

## Status

This is the initial MVP. The decks browser, deck preview, card session screen with the slot-based layout, self-healing assets, and the deep-refresh flash are all in place. Several details (button sizes for thumb comfort, divider density on long lists) will be tuned on the physical device.

## Future scope

Out of scope for the MVP but planned for later iterations:

- Persistent spaced repetition (SM-2 style) as an opt-in mode
- In-app deck editor for quick edits without a computer
- Import from Anki `.apkg` and Quizlet
- Search across decks
- Home-screen widget — a card of the day
- ZIP backup of all decks into `Flashcards/_backup/`

## Acknowledgements

- The [Mudita Mindful Design (MMD)](https://github.com/mudita/MMD) framework, open-sourced by Mudita in 2026, makes this app possible.
- The [Mudita Mindful App Design Challenge](https://mudita.com/community/blog/announcing-the-mudita-mindful-app-design-challenge/) is the reason this project exists.
- The community of Kompakt developers on [forum.mudita.com](https://forum.mudita.com) — especially the authors of CalmCast, einkMeditation, inkOS, KISS-eink and the many other E-Ink-first apps — set the bar for what a sideloaded app on this device can be.

## Licence

[MIT](LICENSE).
