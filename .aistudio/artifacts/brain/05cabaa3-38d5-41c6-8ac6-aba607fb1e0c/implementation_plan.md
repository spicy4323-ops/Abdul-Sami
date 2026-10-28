# AYESHA AI — Premium Personal AI Assistant

A comprehensive, production-grade Android personal AI assistant built with Jetpack Compose, featuring real-time speech interaction, an audio-reactive 3D neural energy orb, native Android device action automation, persistent memory and reminders, and deep cyberpunk-luxury aesthetics.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following technical and design decisions were established during clarification and will guide the build:

- **Confirmed Mode**: Launch directly into the **Personal Voice Companion** experience, showcasing the interactive 3D neural energy orb, real-time voice microphone triggers, and instantaneous device action quick-chips.
- **Confirmed Visual Direction**: **Abstract 3D reactive neural energy orb and particle network** with deep black (#0B0F19) and midnight navy backgrounds, electric cyan (#00F5FF) highlights, subtle violet (#8B5CF6) accents, and glassmorphic HUD panels.
- **Confirmed Voice Architecture**: **Hybrid voice pipeline** using Android's native `SpeechRecognizer` and `TextToSpeech` engines (with two selectable voice profiles: Ayesha warm female voice and an alternative male voice, pitch/rate controls, and multilingual support for English, Hindi, and Urdu) seamlessly integrated with Gemini 3.8 Flash intelligence.
- **Confirmed Action Security**: Safe Android Intent execution with user confirmation dialogs for sensitive actions (sending messages, modifying system alerts) and graceful fallbacks for uninstalled applications.

---

### 1. Overview & Core Concept

- **What It Does**: AYESHA AI serves as an intelligent on-device personal companion. Users can talk naturally with voice or type queries. Ayesha answers questions, remembers key preferences, sets alarms and reminders, takes notes, opens installed apps (YouTube, Chrome, Camera, Settings, Maps), drafts business communications, and provides visual status feedback through an interactive neural core that reacts dynamically to voice states (Listening, Thinking, Speaking, Tool Executing).
- **Target Audience / Persona**: Users desiring a responsive, trustworthy, futuristic personal assistant for daily productivity, hands-free device control, and creative brainstorming.
- **Key Value**: Combines cutting-edge generative AI comprehension with native Android OS capabilities and local data privacy, without frustrating mockups or static stubs.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Home Experience**:
   - Top Bar: AYESHA AI branding, status indicator ("Ready to assist"), language selector (EN / HI / UR), and navigation drawer/sheet access.
   - Hero Section: High-fidelity Canvas-rendered **3D Reactive Neural Energy Core** with orbital particle rings, harmonic audio waveform bars, and real-time state badge (Idle, Listening, Thinking, Speaking, Executing).
   - Interaction Dock: Prominent pulsating circular microphone button, speech stop/interrupt control, and quick-action chips ("Open YouTube", "Take Note", "Remind me tomorrow", "Draft Email", "Search Web").
   - Live Conversation Preview: Most recent exchange with typing/streaming indicator and expandable history card.
2. **Real-Time Voice & Conversation Screen**:
   - Fullscreen focused voice session with enlarged neural audio visualizer, real-time speech transcription stream, live spoken response output, and instant interruption touch target.
3. **Task & Device Action Execution**:
   - Assistant parses intents: e.g. "Open Chrome", "Open Camera", "Remind me to call John at 5 PM", "Take a note".
   - Structured confirmation modal displays destination app/action details before launch.
4. **Memory & Knowledge Hub**:
   - User-controlled persistent memory list (facts, preferences, interaction history) with individual delete, edit, and toggle options.
5. **Reminders & Notes Dashboard**:
   - Manage pending and completed reminders with native Android alarms/notifications and rich text notes.
6. **Voice & Personality Settings**:
   - Toggle between **Ayesha Voice** (warm, professional female) and **Alternative Voice** (calm, clear male).
   - Sliders for speech rate and pitch, language selection (English, Hindi, Urdu), and low-effects performance toggle for battery saving.

#### Visual Identity & Theme
- **Background**: Deep void black (`#070B12`) blending into midnight cosmic navy (`#0B132B`).
- **Primary / Electric Cyan**: `#00F5FF` (neon energy glow, active speech waveforms, primary actions).
- **Secondary / Cyber Violet**: `#8B5CF6` (neural processing trails, thinking state, memory chips).
- **Surface & Cards**: Glassmorphic dark slate (`#131B2E` with 70% opacity, 1dp frosted border `#1E293B`).
- **Text & Contrast**: High-contrast crisp white (`#F8FAFC`) for headings, soft silver (`#94A3B8`) for body.
- **Typography**: Clean, tech-forward sans-serif hierarchy with bold monospace badges for system states.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native SpeechRecognizer + TTS Hybrid Pipeline**
  - *Chosen Approach*: Leverage Android's native `android.speech.SpeechRecognizer` for zero-latency live voice transcription, coupled with `android.speech.tts.TextToSpeech` with customizable locale/voice pitches, and stream prompts directly to Gemini 3.8 Flash.
  - *Why*: Ensures zero-cost, immediate microphone and speech synthesis response on all Android devices, without requiring external paid telephony or WebRTC streaming infrastructure.
  - *Alternatives Considered*: Cloud-only streaming audio, which risks high latency and complete failure without external credentials.
- **Decision 2: Room SQLite Engine for Local Memory & Data Privacy**
  - *Chosen Approach*: Use Room Database for `MemoryItem`, `NoteItem`, `ReminderItem`, and `ChatMessage` history.
  - *Why*: Guarantees user privacy, instant offline retrieval, and zero cloud lock-in for personal notes and reminders.
- **Decision 3: Safe Android Action Intent Dispatcher**
  - *Chosen Approach*: Standard Android `Intent` system with package manager validation to launch YouTube, Chrome, Settings, Camera, Maps, Dialer, and search providers.
  - *Why*: Complies with Google Play policy and Android sandbox restrictions while delivering real automation capabilities.

---

### 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                           AYESHA AI                             │
│                     Jetpack Compose UI                          │
├────────────────────────────────┬────────────────────────────────┤
│    HomeScreen / VoiceHUD       │     Memory & Notes Screens     │
│  - 3D Reactive Neural Canvas   │  - Memory Management & Delete  │
│  - Mic Button & Audio Waves    │  - Reminders & Task Checklists │
│  - Quick Action Chips          │  - Business Drafting Studio    │
└───────────────▲────────────────┴────────────────▲───────────────┘
                │                                 │
┌───────────────┴─────────────────────────────────┴───────────────┐
│                      AyeshaViewModel                            │
│  - StateFlow<AssistantUiState> (Voice State, Messages, Actions) │
│  - Audio Amplitude Tracker (Waveform visualization)             │
│  - Assistant Mode: Companion / Business / Organizer             │
└───────────────▲────────────────┬────────────────▲───────────────┘
                │                │                │
┌───────────────┴──────┐ ┌───────┴──────┐ ┌───────┴───────────────┐
│ Voice & Speech Engine│ │ Gemini AI    │ │ Device Action Engine  │
│ - SpeechRecognizer   │ │  Service     │ │ - Intent Dispatcher   │
│ - TextToSpeech (TTS) │ │ - Gemini 3.8 │ │ - Alarm/Reminder Mgr  │
│ - Audio Visualizer   │ │   Flash      │ │ - Package Validator   │
└──────────────────────┘ └──────────────┘ └───────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │      AppDatabase        │
                    │   (Room SQLite Engine)  │
                    │ - MemoryDao             │
                    │ - NotesDao              │
                    │ - RemindersDao          │
                    │ - ChatHistoryDao        │
                    └─────────────────────────┘
```

#### Entities & Schema
- `MemoryEntity`: `id`, `key`, `value`, `category`, `createdAt`, `isUserConfirmed`
- `NoteEntity`: `id`, `title`, `content`, `createdAt`, `updatedAt`, `tags`
- `ReminderEntity`: `id`, `title`, `dueTimestamp`, `isCompleted`, `priority`
- `ChatMessageEntity`: `id`, `sender` (USER/AYESHA), `text`, `timestamp`, `actionType`

#### Interactive Component & State Mapping
- **Mic Button Tap**: Triggers `startListening()` -> requests `RECORD_AUDIO` if missing -> transitions state to `Listening` -> speech recognition streams text -> updates audio amplitude for canvas rings.
- **Speech End / Prompt Send**: Transitions state to `Thinking` -> calls Gemini AI with chat context + user memory -> streams response tokens -> triggers TTS in `Speaking` state.
- **Command Parser**: Inspects queries for action patterns ("open youtube", "remind me to...", "save note...") -> dispatches to `DeviceActionEngine` -> prompts user confirmation if sensitive -> opens targeted native Android Intent or creates Room record.
- **Voice Profile Switcher**: Instantly reconfigures TTS pitch, speech rate, and language (English, Hindi, Urdu) across all playback.
