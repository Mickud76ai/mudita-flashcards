# Mudita Flashcards

![License](https://img.shields.io/badge/license-MIT-black)
![Platform](https://img.shields.io/badge/platform-Mudita%20Kompakt-black)
![MMD](https://img.shields.io/badge/MMD-1.0.1-black)

A small flashcards app for the [Mudita Kompakt](https://mudita.com/products/phones/mudita-kompakt/), an E Ink phone designed for digital well-being. Built with the [Mudita Mindful Design (MMD)](https://github.com/mudita/MMD) framework.

> Spokojna nauka bez presji. *Calm learning, no pressure.*

### [Download the APK (v1.0.1, 64 MB)](https://github.com/Mickud76ai/mudita-flashcards/releases/download/v1.0.1/mudita-flashcards-1.0-debug.apk)

## What it is

A reader for flashcard decks kept on the phone as CSV files. No timers, no streaks, no notifications, no points. Sessions have no end; you stop when you want. The role is a quiet study notebook for the moments you choose to learn, not a game that tries to keep you coming back.

## Install

Sideload the APK onto a Kompakt:

- via [Mudita Center](https://mudita.com/products/software-apps/mudita-center/): drag the APK onto the connected phone.
- via [WebADB](https://app.webadb.com/) in Chrome: use *Install APK*.

On first launch, grant *All files access* in the permission screen.

Older versions and changelogs live on the [Releases page](https://github.com/Mickud76ai/mudita-flashcards/releases).

## How to use

1. Open the app. The home screen shows the `Flashcards/` folder. Folders are prefixed `▸`, decks `≡`.
2. Tap a deck, then *Start Session*.
3. In a session:
   - tap *Flip the card* (or tap the card body) to see the back,
   - tap the round ✗ button to mark a card as hard,
   - tap the round ✓ button to mark it as known,
   - leave with × in the top bar whenever you want.
4. The gear icon (⚙) on the home screen opens Settings: persistence between sessions, card order, deep refresh, delete decks.

A few starter decks install on first launch: Polish sailing terminology (`Zeglarz_Jachtowy/`), English irregular verbs and phrasal verbs (`Angielski/`), digestive enzymes.

## Create your own decks

**The fastest path is an AI.** The app keeps a file called `how_to_create_decks.md` inside the `Flashcards` folder on the phone. That file is written for ChatGPT or Claude, not for you to read.

Five steps to a new deck:

1. Connect the phone to a computer over USB-C.
2. Open the `Flashcards` folder, copy `how_to_create_decks.md` to the computer.
3. Paste the file into an AI chat.
4. Tell the AI what topic you want a deck on. It returns a CSV file in the correct Mudita Flashcards format.
5. Copy the CSV back into `Flashcards/`. The app picks it up on the next refresh.

If `how_to_create_decks.md` ever gets deleted, the app restores it on the next launch. There is no way to lock yourself out of the instructions.

### Manual format

For writing a deck by hand: line 1 is the deck name, then `front;back` per line.

```
Chinese, Basics
你好;Hello (Ni hao)
谢谢;Thank you (Xiexie)
再见;Goodbye (Zaijian)
```

Place files anywhere inside `Flashcards/`. Sub-folders to any depth become the navigation tree in the app.

## Build from source

```bash
git clone https://github.com/Mickud76ai/mudita-flashcards.git
cd mudita-flashcards
./gradlew assembleDebug
```

Prerequisites: Android Studio (Ladybug or newer), JDK 17, Android SDK with API 36. The APK appears at `app/build/outputs/apk/debug/mudita-flashcards-1.0-debug.apk`.

## Screenshots

Photographs of a physical Mudita Kompakt (4.3" E Ink, 800x480, portrait), cropped to the screen area.

| | | |
|---|---|---|
| ![Deck browser](docs/screenshots/s1_browser.png) | ![Deck preview](docs/screenshots/s2_deck_preview.png) | ![Card back, English](docs/screenshots/s3_card_session_en.png) |
| Deck browser | Deck preview | Card back (English) |
| ![Card back, Polish](docs/screenshots/s4_card_back.png) | ![Settings](docs/screenshots/s6_settings.png) | ![How to create a deck](docs/screenshots/s6_how_to_create_deck.png) |
| Card back (Polish) | Settings | How to create a deck |

## Licence

[MIT](LICENSE). Built on the [Mudita Mindful Design](https://github.com/mudita/MMD) framework.
