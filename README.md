# Flash Cards in Space

## TODOS

- Functionality:
  - Flashcard creation loop (enabled by default -> enable if it's asked to)
- UX:
  - Flashcard
    - Copy button
    - Delete button
    - Front & back styles customization
    - Removing animation
  - Auth
    - Password reset functionality (sending emails)
    - Link from Login page to Password reset page
    - JWT refresh
    - Remember if signed up using cookie (on/off refreshing JWT)
    - Welcoming emails after signing up
  - Responsive UI
  - Indexable for robots (searchable improvements)
  - Light and Dark themes
  - New design of Menu bar
  - User page functionality: 
    - changing password, email, username
    - showing username, email, registration date
  - Mobile friendly
  - Include User.id in the logging context
- DevOps:
  - Notifications if the app is down
  - Notifications on errors in logs
  - CI + test coverage
- Security:
  - auth journal (for login and signup attempts)
  - auth journal analysis
  - ROLE-based access control
