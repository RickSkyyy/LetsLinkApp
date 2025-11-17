**🚀 LetsLink**

**Meet LetsLink: Your All-in-One Social Command Center**

Tired of endless "what should we do?" group chats? _LetsLink_ cuts through the noise - turning indecision into action while keeping everyone safe and connected.

It's the only app you need to **organize your crew, discover nearby events, vote on plans, and watch each other's backs** - all in one place.

**Plan smarter. Discover more. Stay safer. Never miss a moment together.**

🎥 _App Overview Video:_ \[<https://youtu.be/TjffNua59_w?feature=shared\>]  
🔗 _API Overview Video:_ <https://youtu.be/oXj7fFQ-EiA>

---

## 🏷️ Custom Features for Marking
For assessment purposes, the following **key features** are highlighted for lecturer evaluation:

1. **Real-Time Group Chat**  
   - Fully functional chat system integrated into every group.  
   - Supports text messages, emojis, and notifications via Firebase Cloud Messaging (FCM).  
   - Ensures chat history is synced across all group members in **real-time**.

2. **Ticketmaster Event Puller**  
   - Dynamically fetches **Ticketmaster events** relevant to users’ location.  
   - Allows users to discover concerts, shows, and events nearby.  
   - Integrates seamlessly with group voting and planning workflow.  

These are the **two main features we would like the lecturer to mark**.
---


**🧩 Table of Contents**

- [Features](#-features)
- [Custom Features (API Integrated)](#-custom-features-api-integrated)
- [Technology Stack](#-technology-stack)
- [Installation & Setup](#-installation--setup)
- [API Integration](#-api-integration)
- [Security Highlights](#-security-highlights)
- [Contributors](#-contributors)
- [License](#-license)

**✨ Features**

**🔍 Discover Local Events**

- Explore **concerts, festivals, parties, and casual hangouts** happening nearby.
- Vote on events with your group to decide what's most exciting.
- Stay updated on popular events in real-time.

**🔐 User Registration & Authentication**

- Secure onboarding with **email and password** (min. 8 characters, at least one digit).
- **Single Sign-On (SSO)** via Google and Facebook using OAuth 2.0.
- **Biometric Authentication** (fingerprint & face unlock) with Android's BiometricPrompt API.
- Smooth, secure login and registration across all Android devices.

**👥 Group Creation & Management**

- Create **temporary groups** (for one-time events) or **permanent groups** (for ongoing social circles).
- Invite members via email, username, or phone number.
- Each group includes:
  - Real-time group chat
  - Event voting
  - Collaborative to-do lists
  - Emergency alerts and safety notifications

Groups act as a **central hub** for communication, planning, and safety.

**🗳️ Event Voting with Swipe Interface**

- **Tinder-style swipe UI** for fast, fun voting on events.
- Swipe **right** for Yes, **left** for No.
- Event cards display:
  - Name
  - Description
  - Location
  - Date and time
  - Live vote count
- Votes update **in real-time** for transparent group decision-making.

**🎉 Custom Event Creation**

- Create **personalized events** like picnics, braais, or meetups.
- Add **name, location, date, time, description, and event image**.
- Events appear in the group for **voting and coordination**.

**💬 Real-Time Group Chat**

- Built-in **messaging system** for all groups.
- Supports **text messages and emojis** (attachments/media supported in future updates).
- Push notifications via **Firebase Cloud Messaging (FCM)** keep users updated.
- Chat history is synced across devices in real-time.

**📝 Collaborative To-Do Lists**

- Share **group-based task lists** for better event planning.
- Tasks include:
  - Name
  - Optional due date/time
  - Optional member assignment
- All members can mark tasks **complete/incomplete**, promoting shared responsibility.

**🛡️ Emergency Alerts & Safety Features**

- **Quick-access emergency buttons** for pre-defined alerts like:
  - "I'm going home"
  - "I need help"
  - "Going to the bathroom"
- Alerts notify **group members and a trusted external contact** instantly.
- **Live location sharing** ensures everyone can stay safe during events.

**📶 Offline Functionality**

- Access previously loaded events, chats, and to-do lists **without internet**.
- Actions performed offline **sync automatically** when connectivity is restored.

**🔧 Custom Features (API Integrated)**

LetsLink's backend API powers interactive features for seamless collaboration:

- **Group Management** - API endpoints handle invitations, group type, and membership tracking.
- **Event Creation** - Users push new events (name, location, description, image, date/time) via API.
- **Collaborative To-Do Lists** - API endpoints manage creation, updates, and real-time syncing of tasks.
- **Real-Time Event Discovery** - API enables fetching popular events near users dynamically.

📺 _Watch the API video:_ <https://youtu.be/oXj7fFQ-EiA>

**⚙️ Technology Stack**

| **Component** | **Technology** |
| --- | --- |
| Frontend | Kotlin + XML (Android Studio) |
| Backend | Firebase Realtime Database |
| Authentication | Firebase Auth, OAuth 2.0 (Google & Facebook) |
| Notifications | Firebase Cloud Messaging (FCM) |
| API Integration | Custom REST endpoints with Firebase |
| Security | Android BiometricPrompt API |
| Version Control | Git & GitHub |

**🧠 API Integration**

- **Realtime Syncing:** Chat, event votes, and to-do lists update instantly.
- **Authentication:** Secure login/registration with Firebase Auth & OAuth 2.0.
- **Cloud Messaging:** FCM delivers real-time notifications for chats, events, and emergency alerts.
- **Biometric Integration:** Native fingerprint & face unlock for device-level security.

**🔒 Security Highlights**

- **Password Policy:** Minimum 8 characters, one numeric digit.
- **OAuth 2.0 Compliance:** Safe, verified login through Google & Facebook.
- **Biometric Security:** Fingerprint and face unlock supported.
- **Encrypted Communication:** All Firebase data encrypted in transit and at rest.
- **Secure Tokens:** Authentication tokens managed to prevent unauthorized access.

**📲 Installation & Setup**

- **Clone this repository:**

git clone \[repo link\]

- **Open in Android Studio:**
  - Open project folder
  - Sync Gradle dependencies
- **Configure Firebase:**
  - Add google-services.json
  - Enable Firebase Authentication, Realtime Database, and Cloud Messaging
- **Build & Run:**
  - Compile and install on a physical device or emulator

**🏆 Contributors**

**ST10258321** - Zalano Poole

**ST10408316** - Mpho Tlokotsi

**ST10303522** - Derrick Mungwira

**ST10268411** - Neo Phalama

