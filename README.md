# SmartSupport AI — Customer Support & Complaint Management System

Built for **VCUBE HACK QUEST 1.0 (Problem 6: Customer Service)**.

## Technology Stack
- **Backend:** Java 21, Spring Boot 3.4.3, Spring Data JPA, Spring Security (JWT), Spring AI (`spring-ai-openai-spring-boot-starter`)
- **AI Engine:** Google Gemini (`gemini-2.0-flash` via OpenAI-compatible API) or Groq (`llama-3.3-70b-versatile`)
- **Database:** MySQL / TiDB Cloud (`users`, `categories`, `tickets`, `ticket_assignments`, `ticket_comments`)
- **Frontend:** React.js 18, Vite, Tailwind CSS, Axios, Lucide Icons

## 5 AI Features Implemented in 1 Backend Call
When a customer submits a complaint via `POST /api/tickets`, `AiTicketService` fetches live categories from MySQL and invokes Spring AI `ChatClient` to return:
1. **AI Complaint Classification** (matched to DB `categories`)
2. **Sentiment Analysis** (`POSITIVE`, `NEUTRAL`, `NEGATIVE`, `ANGRY`)
3. **Ticket Priority Prediction** (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
4. **Suggested Resolution Generator** (internal agent playbook)
5. **Customer Response Generator** (ready-to-send polite reply)

## Quick Local Run
1. **Database:** Create a MySQL database named `smart_support` (or set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
2. **Backend:**
   ```bash
   cd backend
   export AI_API_KEY="your-google-ai-studio-gemini-key"
   mvn spring-boot:run
   ```
3. **Frontend:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
4. **Demo Accounts (auto-seeded):**
   - Customer: `customer@demo.com` / `password123`
   - Agent: `agent1@demo.com` / `password123`

## Cloud Deployment (Render + TiDB Cloud + Vercel)
1. **Database:** Create a free MySQL-compatible cluster on TiDB Cloud Starter and copy the JDBC URL, username, and password.
2. **Backend (Render):** Create a new **Web Service** pointing to `backend/` (uses `backend/Dockerfile`). Add env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `AI_API_KEY`, `CORS_ORIGINS`.
3. **Frontend (Vercel / Cloudflare Pages):** Deploy `frontend/` with build command `npm run build`, output `dist`, and env var `VITE_API_URL=https://your-render-app.onrender.com/api`.
