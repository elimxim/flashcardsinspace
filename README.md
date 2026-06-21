<h1>
  <img src="src/main/vue/public/logo.svg" alt="Flashcards in Space Logo" width="30"/>
  Flashcards in Space
</h1>

[![Deploy](https://github.com/elimxim/flashcardsinspace/actions/workflows/deploy-release.yml/badge.svg)](https://github.com/elimxim/flashcardsinspace/actions/workflows/deploy-release.yml)
![GitHub Issues](https://img.shields.io/github/issues/elimxim/flashcardsinspace)
![GitHub Last Commit](https://img.shields.io/github/last-commit/elimxim/flashcardsinspace)
![GitHub Repo Size](https://img.shields.io/github/repo-size/elimxim/flashcardsinspace)
![GitHub Code Size](https://img.shields.io/github/languages/code-size/elimxim/flashcardsinspace)
![GitHub Top Language](https://img.shields.io/github/languages/top/elimxim/flashcardsinspace)
![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green)
![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-blue)

Flashcards in Space is a personal mission to make long-term learning feel less like a chore and more like an exploration. 
It combines the proven science of spaced repetition with a modern, space-themed interface designed 
to keep you curious and consistent.

<a href="https://flashcardsinspace.com"><img src="https://img.shields.io/badge/Visit-flashcardsinspace.com-033270?style=for-the-badge&labelColor=4091c9" alt="Website" height="40"/></a>

<p align="center">
  <a href="https://flashcardsinspace.com"><img src="src/main/vue/src/assets/rocket.svg" alt="Visit flashcardsinspace.com" width="150"/></a>
</p>

## 🎯 The Story Behind the Mission

Flashcards in Space wasn’t born from a business plan, but from a personal need and a bit of "what if?" curiosity. 
The project is built on three core pillars:

### 1. Reimagining the Ritual

I’ve always been a believer in the Leitner System, the simple but powerful idea of moving cards through boxes 
as you master them. However, I found myself constantly struggling with existing tools. 
They were either powerful but buried under "clunky"interfaces that felt like 90s spreadsheets, 
or beautiful but lacked the specific scheduling flexibility I needed. I wanted a place to learn that felt warm, 
intuitive, and fun to return to every morning.

### 2. Refining the Learning Rhythm

While the traditional Leitner system is powerful, its rigid intervals often lead to "review spikes", 
daunting days when hundreds of cards collide at once. Gabriel Wyner made incredible strides in smoothing this out 
to create a more manageable pace, and the Lightspeed Schedule takes that evolution to its natural conclusion. 
By intelligently shifting and distributing learning stages across the calendar, 
it eliminates those overwhelming peaks entirely. The result is a perfectly fluid, natural progression 
that ensures your daily ritual stays calm and consistent, no matter how large your deck grows.

### 3. The Philosophy: Low-Level Construction

This project is a laboratory for high-precision engineering. While "vibe coding" prioritizes speed through abstraction 
and "prompt-and-pray" generation, this codebase is built on technical sovereignty. 

I chose to build every single UI component from scratch using only a UI framework and a couple of base libs. 
I wanted to see how far a solo developer can push the limits of custom design when they have a powerful AI teammate at their side.

**The AI as a High-Precision Lathe, Not an Autopilot**

In this workflow, the AI isn't used to "skip" the work, but to scale it. It's not the pilot; it’s a high-precision lathe. 
By treating the AI as an Extreme Programming (XP) partner, I can mill individual building blocks to a micron-level 
of precision while I focus on the choreography of the architecture.

**This is the move from "vibing" to engineering:**
- Granular Implementation: Every `div`, `ref`, and CSS property is an intentional choice. 
  No "black box" components, no library bloat, and no wrestling with a UI kit’s baked-in styles.
- Deterministic Control: "Vibing" often leads to a "last mile" debugging nightmare. 
  By constructing the logic line-by-line, I maintain well-grained control over the entire system.
- Architectural Sovereignty: While the AI handles the heavy lifting of implementation, 
  I remain the sole architect of the system’s behavior and state.

**Why go from scratch?**

To prove that the "tax" of bespoke excellence has been eliminated. 
Traditionally, a solo dev uses a UI framework to survive. 
I chose low-level construction to achieve enterprise-grade custom architecture with a team of one.

This isn't a shortcut, it's a new era of productivity where the engineer maintains total command over the code. 

## 🚀 Getting Started

> ⚠️ **Note**: This project is shared for **educational purposes only**. You may clone and run it locally to learn from the code, but commercial use and redistribution are prohibited. See the [Licensing & Use](#%EF%B8%8F-licensing--use) section for details.

### Prerequisites
- Java 24+
- Node.js 22+
- PostgreSQL

### Local Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/elimxim/flashcardsinspace.git
   cd flashcardsinspace
   ```

2. Configure the database in `props/application.yaml`

3. Build and run:
   ```bash
   ./gradlew bootRun
   ```

4. For frontend development:
   ```bash
   cd src/main/vue
   npm install
   npm run dev
   ```

## 🤝 Contributing

Contributions are welcome! Because this project reserves all commercial rights, every
contribution must transfer its rights to the project owner — see [CONTRIBUTING.md](CONTRIBUTING.md)
for the contributor terms before opening a pull request. You're also welcome to:
- 🐛 Report bugs via [Issues](https://github.com/elimxim/flashcardsinspace/issues)
- 💡 Suggest features or improvements
- ⭐ Star the repo if you find it interesting!

## 📜 Licensing & Use

Thank you for your interest in Flashcards in Space. This project is **source-available**:
you can read, learn from, modify, and share the code for noncommercial purposes, but it
is **not open-source** — all commercial rights are reserved.

| | |
|---|---|
| ✅ **Allowed** | Viewing, running, modifying, and sharing the software for any **noncommercial** purpose (personal, study, hobby, nonprofits, schools) |
| ❌ **Prohibited** | Any **commercial use** — selling it, running it as a business, or building a paid product or service on it (without a separate commercial license) |
| 📄 **License** | [PolyForm Noncommercial License 1.0.0](LICENSE.md) — for a commercial license, contact elimxim@gmail.com |

Please see the [LICENSE.md](LICENSE.md) file for the complete terms and conditions.

---

<p align="center">
  ✨ Taking your memory to infinity and beyond ✨
</p>
