# 🍽️ Restaurant Food App

This is a full-stack mobile-friendly application for browsing restaurants and their menus. Built using Android (Java/Kotlin) for the frontend and Node.js with MongoDB for the backend, it enables users to sign up, view restaurants, and explore available dishes.

---

## 🚀 Features

- 🔐 User Authentication (Signup/Login)
- 📍 Location Button (for future GPS features)
- 🍴 Restaurant List View with:
  - Name
  - Image
  - Rating
  - Location
- 📜 Detailed Menu Page for each restaurant
- 🔄 Automatic RecyclerView updates from MongoDB data
- 🌐 Secure API integration using Retrofit

---

## 🛠️ Tech Stack

| Layer      | Technology             |
|------------|------------------------|
| Frontend   | Android (Java)         |
| Backend    | Node.js, Express       |
| Database   | MongoDB (Mongoose)     |
| API Calls  | Retrofit (Android)     |
| Styling    | XML Layouts            |
| Auth       | JWT (planned)          |

---

## 🧾 Folder Structure

```markdown
Restaurant_Food_App/
├── app/ # Android frontend
│ ├── activities/
│ ├── adapters/
│ └── models/
├── backend_onlyrestaurant/ # Node.js backend
│ ├── routes/
│ ├── controllers/
│ └── models/
└── .env # Secrets (not committed)
```

---

## ⚙️ Setup Instructions

1. **Backend**
   ```bash
   cd backend_onlyrestaurant
   npm install
   node index.js
   ```

   ---
 2. Android App

- Open in Android Studio
- Update API base URL in Retrofit client if needed
- Run on emulator or real device

---

🔐 Security
- Secrets like Twilio keys are stored in .env files (excluded from Git).
- Uses dotenv and .gitignore to prevent leaks.
- Git history is reset to remove any accidental secrets.

---
📦 APIs Used
- Internal REST APIs (Node.js + Express)
- Optional Twilio integration for order notifications
---
👨‍💻 Author
**Gangdev Pooniya**

---
### User app repo
```bash
https://github.com/gangz-bot/My_Food_Application
```
