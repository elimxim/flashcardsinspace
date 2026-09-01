<h1>
  <img src="src/main/vue/public/logo.svg" alt="Flashcards in Space Logo" width="30"/>
  Flashcards in Space
</h1>

[![Deploy](https://github.com/elimxim/flashcardsinspace/actions/workflows/deploy-release.yml/badge.svg)](https://github.com/elimxim/flashcardsinspace/actions/workflows/deploy-release.yml)
![GitHub Issues](https://img.shields.io/github/issues/elimxim/flashcardsinspace)
![Closed Issues](https://img.shields.io/github/issues-closed/elimxim/flashcardsinspace)
![GitHub Last Commit](https://img.shields.io/github/last-commit/elimxim/flashcardsinspace)
![GitHub Repo Size](https://img.shields.io/github/repo-size/elimxim/flashcardsinspace)
![GitHub Code Size](https://img.shields.io/github/languages/code-size/elimxim/flashcardsinspace)
![GitHub Top Language](https://img.shields.io/github/languages/top/elimxim/flashcardsinspace)
![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green)
![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-blue)

Flashcards in Space is a personal mission to make long-term learning feel less like a chore and more 
like an exploration. It combines the proven science of spaced repetition with a modern, space-themed 
interface designed to keep you curious and consistent.

<a href="https://flashcardsinspace.com"><img src="https://img.shields.io/badge/Visit-flashcardsinspace.com-033270?style=for-the-badge&labelColor=4091c9" alt="Website" height="40"/></a>

<p align="center">
  <a href="https://flashcardsinspace.com"><img src="src/main/vue/public/images/rockets/original.svg" alt="Visit flashcardsinspace.com" width="150"/></a>
</p>

## 🎯 The Story Behind the Mission

Flashcards in Space wasn’t born from a business plan, but from a personal need and a bit 
of "what if?" curiosity. The project is built on three core pillars:

### 1. Reimagining the Ritual

We’ve always been a believer in the Leitner System, the simple but powerful idea of moving 
cards through boxes as you master them. However, We found outselves constantly struggling with 
existing tools. They were either powerful but buried under "clunky" interfaces that felt 
like 90s spreadsheets, or beautiful but lacked the specific functionality we needed. We wanted 
a place to learn that felt warm, intuitive, and fun to return to every morning.

### 2. Refining the Learning Rhythm

While the traditional Leitner system is powerful, its rigid intervals often lead to 
"review spikes", daunting days when hundreds of cards collide at once. Gabriel Wyner 
made incredible strides in smoothing this out to create a more manageable pace, and 
the Lightspeed Schedule takes that evolution to its natural conclusion. By intelligently 
shifting and distributing learning stages across the calendar, it eliminates those 
overwhelming peaks entirely. The result is a perfectly fluid, natural progression that 
ensures your daily ritual stays calm and consistent, no matter how large your deck grows.

### 3. Exploring New Realms

Our goal is to keep everything you need in one place, making the learning process fast, 
simple, and of course fun — no juggling separate tools, no friction between deciding to 
study and actually doing it. We stay on top of the ideas that make the most sense and keep 
exploring new ones, always refining the rough edges and charting new realms to make your 
daily ritual a little better every time you return.

## 🚀 Getting Started

> ⚠️ **Note**: This project is shared for **educational purposes only**. 
> You may clone and run it locally to learn from the code, but commercial 
> use and redistribution are prohibited. See the 
> [Licensing & Use](#%EF%B8%8F-licensing--use) section for details.

### Prerequisites
- Java 24+
- Node.js 26+
- PostgreSQL
- Docker

### Tech Stack

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white)
![Vue.js](https://img.shields.io/badge/Vue.js-4FC08D?logo=vuedotjs&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?logo=typescript&logoColor=white)
![Axios](https://img.shields.io/badge/Axios-5A29E4?logo=axios&logoColor=white)
![Pinia](https://img.shields.io/badge/Pinia-FFD859?logo=pinia&logoColor=black)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?logo=hibernate&logoColor=white)
![Liquibase](https://img.shields.io/badge/Liquibase-2962FF?logo=liquibase&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-646CFF?logo=vite&logoColor=white)

### Local Setup

Clone the repository:

```bash
git clone https://github.com/elimxim/flashcardsinspace.git
cd flashcardsinspace
```

#### Quick setup

Build and run the app and database:

```bash
docker compose up -d
```

#### Dev setup

1. Database.
   ```bash
   docker compose up db -d
   ```

2. Create and place your app properties file with settings to override:
   <details>
   <summary>Example of props/application-dev.yaml</summary>
   
   ```yaml
   server:
     port: 8442 # this port is used in vite.config.ts to proxy API calls to the server over HTTP
   spring:
     datasource: # DB is required for the app
       url: jdbc:postgresql://localhost:5432/flashcardsinspace?sessionTimezone=UTC
       username: fins
       password: fins

   app:
     security: 
       jwt:
         secret: <random base-64 key>
         access-token-expiration-ms: 144000000 # 40 hours
         refresh-token-expiration-ms: 6912000000 # 80 days
       cors:
         allowed-origins:
           - http://localhost:5174
       verification-tokens:
         registration-request:
           length: 4
           max-age: 86400 # 1 day
         password-reset-request:
           length: 4
           max-age: 3600 # 1 hour
         password-reset-access:
           length: 4
           max-age: 600 # 10 minutes
     mail: # optional
       enabled: false # change to true if you have Brevo API key and want to send/receive emails 
       api-key: <Brevo API key>
       sender-name: <sender name>
       sender-domain: <sender domain>
       reply-to-email: <your email>
   ```
   </details>

3. Backend. To run/debug through IDEA, the cfg file is in `.run`. Otherwise:
   ```bash  
   ./gradlew bootRun
    ```

4. Frontend.
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

|                  |                                                                                                                                                    |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| ✅ **Allowed**    | Viewing, running, modifying, and sharing the software for any **noncommercial** purpose (personal, study, hobby, nonprofits, schools)              |
| ❌ **Prohibited** | Any **commercial use** — selling it, running it as a business, or building a paid product or service on it (without a separate commercial license) |
| 📄 **License**   | [PolyForm Noncommercial License 1.0.0](LICENSE.md) — for a commercial license, contact elimxim@gmail.com                                           |

Please see the [LICENSE.md](LICENSE.md) file for the complete terms and conditions.

---

<p align="center">
  ✨ Taking your memory to infinity and beyond ✨
</p>
