# Flashcards, guidelines for creating decks

---

## What this file is

This file explains how to create your own flashcard decks for Mudita Flashcards. You can create decks manually or with help of an AI tool, just paste this file as the instructions.

---

## CSV file structure

Each deck is a single CSV file. Structure:

```
Deck name that appears in the app
Text of card 1, side A;Text of card 1, side B
Text of card 2, side A;Text of card 2, side B
```

**Line 1:** the deck name. This is the text shown in the deck list inside the app. It can differ from the file name.

**Lines 2 and beyond:** one card per line. Side A and side B separated by a semicolon (`;`).

**Important, semicolons:** do not use a semicolon inside card content (neither side A nor side B). If you need to separate elements within a card, use a comma or colon instead.

### Minimal example

File: `chinese_basics.csv`

```
Chinese, Basics
你好;Hello (Ni hao)
谢谢;Thank you (Xiexie)
再见;Goodbye (Zaijian)
对不起;Sorry (Duibuqi)
是;Yes (Shi)
不;No (Bu)
```

---

## File and folder organization

Place CSV files in the `Flashcards` folder on the device. You can create subfolders to any depth. The app scans the whole tree and finds every deck.

```
Flashcards/
├── Biology/
│   ├── Chapter-1/
│   │   ├── mitosis.csv
│   │   └── ecology.csv
│   └── Chapter-2/
│       └── genetics.csv
├── Vocabulary/
│   ├── business.csv
│   └── restaurants.csv
└── Sailing/
    ├── navigation.csv
    └── rules.csv
```

The app shows the folder hierarchy as navigation. You can move and rename folders through Windows Explorer (with the phone connected via USB-C), the app reflects the changes after refreshing the list.

**Naming files and folders:** the app itself handles any characters in file and folder names — Polish diacritics, Chinese, spaces, all work. For maximum portability when moving files between operating systems and MTP clients, ASCII letters, digits, underscores and hyphens are recommended. In card content (front and back), you can always use any characters, including any language.

Recommended (most portable):
- files: `mitosis.csv`, `biology-cell.csv`, `chapter_01.csv`
- folders: `Biology`, `Chapter-1`, `Zeglarz_Jachtowy`

Also works, slightly less portable:
- `Chapter 1.csv` (space — some Linux tools need quoting)
- `Żeglarz.csv` (non-ASCII — some older MTP clients may rename on copy)

---

## Readability limits

Mudita Kompakt displays apps in portrait orientation. Effective working area is 480 pixels wide by 800 pixels tall. For good readability, stay within these limits:

**Side A (front of the card, question, concept, word):**
- maximum 80 characters
- best as a single word, a short sentence, or a question
- avoid lists and bullet points, this is the question side
- **on the deck preview list, side A is shown on a single line and truncated with `...` if it does not fit** — this is intentional, the goal is to keep as many cards visible at once as possible, even when some fronts are long. The full text is always readable inside a session.

**Side B (back of the card, answer, definition, translation):**
- maximum 300 characters
- you can use a few sentences, keep it concise
- if you have a lot to say, consider splitting it into multiple cards

**What happens if you exceed the limit:** the app will not reject the card. It truncates the content to the limit and appends three dots (`...`) at the end. That is a signal to you that the card should be shortened. Staying within limits guarantees that all of the text will be readable.

---

## Instructions for AI

If you want to use an AI tool (e.g., ChatGPT or Claude) to generate a deck, paste this file into the chat and write something like:

> "Generate a flashcard deck about [YOUR TOPIC]. Deck name: [NAME]. Use English. Follow the format described in this guidelines file."

The AI will return text in a block. To save it as a CSV file ready to upload to Kompakt:

1. Copy the entire returned text (from the first line with the deck name down to the last card).
2. Open a text editor on your computer (Notepad on Windows, TextEdit on macOS, gedit or similar on Linux).
3. Paste the copied text.
4. Use "Save as" with these settings:
   - **File name:** your choice, with the `.csv` extension, no spaces and no non-ASCII characters (e.g., `chinese_basics.csv`)
   - **File type:** All Files (`*.*`), not the text type of the editor (would append `.txt`)
   - **Encoding:** UTF-8 (without it, non-ASCII characters may render incorrectly)
5. Upload the saved file to Kompakt through Windows Explorer (phone connected via USB-C, treated as any Android device).

**Tips for better results:**
- specify the number of cards you want (e.g., "30 cards")
- specify difficulty level (e.g., "beginner A1")
- for foreign languages, ask for pronunciation in parentheses after the translation
- remind the AI to stay within the 80 and 300 character limits and not to use semicolons inside cards

---

## Updating decks

To update a deck, copy the CSV file through Windows Explorer to your computer, make your changes, and upload it back. The app reads the updated version after refreshing the list.

---

## Note about this file

The app restores `how_to_create_decks.md` to its original contents on every launch. If you delete it or modify it by accident, just reopen the app, the file comes back. Your own decks are never touched.
