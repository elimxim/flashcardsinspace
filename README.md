<h1>
  <img src="src/main/vue/public/logo.svg" alt="Flashcards in Space Logo" width="30"/>
  Flashcards in Space
</h1>

[![Deploy](https://github.com/elimxim/flashcardsinspace/actions/workflows/cd.yml/badge.svg)](https://github.com/elimxim/flashcardsinspace/actions/workflows/cd.yml)
![GitHub Issues](https://img.shields.io/github/issues/elimxim/flashcardsinspace)
![GitHub Last Commit](https://img.shields.io/github/last-commit/elimxim/flashcardsinspace)
![GitHub Repo Size](https://img.shields.io/github/repo-size/elimxim/flashcardsinspace)
![GitHub Code Size](https://img.shields.io/github/languages/code-size/elimxim/flashcardsinspace)
![GitHub Top Language](https://img.shields.io/github/languages/top/elimxim/flashcardsinspace)
![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green)
![License](https://img.shields.io/badge/license-Proprietary-red)

<p align="center">
  <a href="https://flashcardsinspace.com"><img src="src/main/vue/src/assets/rocket.svg" alt="Visit flashcardsinspace.com" width="150"/></a>
</p>

<a href="https://flashcardsinspace.com"><img src="https://img.shields.io/badge/Visit-flashcardsinspace.com-033270?style=for-the-badge&labelColor=4091c9" alt="Website" height="40"/></a>

Flashcards in Space is a fun and efficient spaced repetition system that helps you learn vocabulary or any other concept you can put into flashcards. Built with a space-themed UI, humor and powered by a custom **Lightspeed Schedule** algorithm.

## 💡 Why Flashcards in Space?

Existing spaced repetition tools often prioritize function over form, resulting in dated interfaces that can make daily practice feel like a chore. Flashcards in Space was created to bridge this gap, combining proven learning science with a modern, engaging user experience.

At its core is the **Lightspeed Schedule**, a custom spaced repetition algorithm designed to optimize retention while keeping sessions efficient. Built on the foundational research of Hermann Ebbinghaus, Sebastian Leitner, and other pioneers of spaced repetition science.

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
