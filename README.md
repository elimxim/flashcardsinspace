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
![License](https://img.shields.io/badge/license-Proprietary-red)

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

### 3. A Laboratory for Modern Craft

This project serves as a laboratory for high-precision engineering. Rather than relying on modern UI libraries or 
Material Design frameworks, I chose to build every component from scratch to maintain well-grained control 
over the entire system. This approach allowed me to explore a new era of productivity: using AI to grow 
the individual building blocks of the interface while I focused on the choreography of the architecture. 
It wasn’t about shortcuts, but about seeing how far a solo developer can push the limits of custom design 
when they have an intelligent powerful co-pilot at their side.

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

This project is **not accepting pull requests** due to its proprietary license. However, you're welcome to:
- 🐛 Report bugs via [Issues](https://github.com/elimxim/flashcardsinspace/issues)
- 💡 Suggest features or improvements
- ⭐ Star the repo if you find it interesting!

## 📜 Licensing & Use

Thank you for your interest in Flashcards in Space. This project is shared so you can read and learn from the code,
but it is **not open-source**, and usage is subject to the license terms.

| | |
|---|---|
| ✅ **Allowed** | Cloning the repository, viewing the source, and running it locally for personal, non-commercial evaluation and learning |
| ❌ **Prohibited** | Commercial use, redistribution, modification, use in other projects, hosting as a service, or any production use (without a separate commercial license) |
| 📄 **License** | Your use of this repository is governed by the terms in the [LICENSE](LICENSE) file |

Please see the [LICENSE](LICENSE) file for the complete terms and conditions.

---

<p align="center">
  ✨ Taking your memory to infinity and beyond ✨
</p>
