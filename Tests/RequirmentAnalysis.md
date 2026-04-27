# VOCALYX - Requirement Notes Document

**Project:** VOCALYX - AI-Powered Sales Call Analysis Platform  
**Document Type:** Requirement Analysis  
**Date:** 2026-04-26  
**QA Engineer:** [Your Name]  
**Version:** 1.0

---

## 1. PROJECT OVERVIEW

### 1.1 Purpose
VOCALYX is an AI-powered sales call analysis platform that analyzes sales call recordings, generates transcripts, performs sentiment analysis, and provides insights to sales teams.

### 1.2 Users
- **Sales Representatives:** Upload and review their own calls
- **Sales Managers:** Monitor team performance (scope unclear - see Section 7)
- **Admins:** Manage workspaces and user invitations

### 1.3 Tech Stack
- **Backend:** Spring Boot (REST APIs)
- **Frontend:** React
- **Database:** MySQL
- **AI Processing:** N8N workflow integration
- **Authentication:** JWT (24-hour tokens)

---

## 2. FUNCTIONAL REQUIREMENTS

### 2.1 File Upload & Processing

#### FR-001: Audio File Upload
- **Description:** System shall accept audio file uploads for processing
- **Supported Formats:** Any MIME type starting with `audio/*` (MP3, WAV, OGG, etc.) or `application/octet-stream`
- **Source:** `FileUploadController.java` - `isValidAudioFile()` method
- **Constraints:**
    - Maximum file size: **50MB** (hard limit)
    - One file upload at a time (no batch upload)
- **Validation:** Returns `400 Bad Request` with message "Invalid file type. Only audio files are allowed." if format is unsupported

#### FR-002: Call Status Tracking
- **Description:** System shall track processing status of uploaded calls
- **States:**
    - `PENDING` - Initial upload state
    - `PROCESSING` - While N8N is analyzing
    - `COMPLETED` - After webhook returns results
- **Source:** `Call.java` entity - status field

#### FR-003: Transcript Generation
- **Description:** System shall automatically generate transcripts from audio files
- **Workflow:**
    1. Audio file stored locally
    2. Triggers N8N workflow via POST to `n8nWebhookUrl`
    3. N8N processes audio and generates transcript
    4. N8N calls back to `/api/calls/public-webhook/{callId}`
    5. Transcript stored in `Call.transcript` field
- **Source:** `CallController.createCall()`, `N8nWebhookService`

---

### 2.2 Sentiment Analysis

#### FR-004: Sentiment Scoring
- **Description:** System shall analyze sentiment of call transcripts
- **Output:**
    - **Sentiment Percentage:** Integer (0-100)
    - **Sentiment Label:** Text label (e.g., "Extremely Positive", "POSITIVE", "Negative")
- **Scope:** One sentiment score per entire call (not segmented by speaker/time)
- **Source:** `Call.java`, `CallResponse.java`, `WebhookResponse.sentimentLabel`

**Note:** Sentiment labels are dynamic and come from N8N response (not predefined in backend)

---

### 2.3 Summary Generation

#### FR-005: Call Summary
- **Description:** System shall generate text summary of calls
- **Constraint:** Maximum 1000 characters
- **Source:** `Call.summary` field, `WebhookResponse.summary`
- **Generation:** Powered by N8N workflow

---

### 2.4 Dashboard & Reporting

#### FR-006: Call History View
- **Endpoint:** `/api/calls/history`
- **Data Displayed:**
    - Call title, date/time, direction
    - Summary, sentiment score, sentiment type
    - Contact info, Order ID
- **Source:** `CallController.getAllCallsLatestFirst()`, `CallDto.java`

#### FR-007: Goals Dashboard
- **Endpoint:** `/api/goals/dashboard/summary`
- **Metrics:**
    - Total goals, achieved goals, pending goals
    - Average progress
- **Source:** `GoalController.getDashboardSummary()`, `DashboardSummary.java`

#### FR-008: Sales Metrics
- **Endpoints:**
    - `/api/goal-sales/total-revenue` - Total revenue
    - `/api/goal-sales/total-sales-count` - Sales count
    - `/api/goal-sales/recent-sales` - Recent sales list
- **Source:** `GoalSalesController.java`, `GoalSalesIntegrationService.java`

---

### 2.5 Contact Management

#### FR-009: Contact CRUD Operations
- **Description:** Full create, read, update, delete for contacts
- **Contact Fields:**
    - Salutation, name, job title
    - Email, phone
    - Department, company
- **Source:** `ContactController.java`, `ContactService.java`

---

### 2.6 Order Tracking

#### FR-010: Call-to-Order Linking
- **Description:** System shall link multiple calls to one order
- **Auto-Creation:** Automatically creates order if none exists for contact
- **Source:** `OrderService.java`, `CallService.saveCall()`

---

### 2.7 Sales Goal Management

#### FR-011: Goal Setting
- **Description:** Users can set revenue targets with date ranges
- **Auto-Calculation:** Progress calculated from sales logs
- **Status Values:**
    - "Not Started"
    - "In Progress"
    - "Completed"
- **Source:** `GoalService.java`, `Goal.java`

---

### 2.8 Sales Logging

#### FR-012: Deal Tracking
- **Description:** Track closed deals with amounts, dates, links to calls/contacts/orders
- **Calculations:**
    - Total revenue
    - Sales count
- **Source:** `SalesLogService.java`, `SalesLog.java`

---

### 2.9 Chat Q&A Feature

#### FR-013: Conversational Query
- **Description:** Users can ask questions about orders/calls
- **Implementation:** Uses N8N to answer based on call transcripts
- **Endpoint:** `ChatController.java`
- **Source:** `ChatService.java`, `N8nWebhookService.triggerForOrder()`

**Note:** This is manual Q&A, not automated follow-up suggestions

---

### 2.10 Filtering & Search

#### FR-014: Basic Call Filtering
- **Available Filters:**
    - Calls by Order ID: `/api/calls/history/{orderId}`
    - Calls by Contact: `/api/calls/contact/{contactId}`
    - Sales logs by date range, user, contact, order

**Limitation:** No general search by customer name or keyword at dashboard level

---

## 3. NON-FUNCTIONAL REQUIREMENTS

### 3.1 Security & Authentication

#### NFR-001: JWT Authentication
- **Description:** System uses JWT-based authentication
- **Token Lifetime:** 24 hours
- **Features:**
    - User registration/login
    - Password reset
    - User invitation
- **Source:** `UserService.java`, `JwtTokenProvider.java`, `SecurityConfig.java`

#### NFR-002: Role-Based Access Control
- **Roles:**
    - `USER` - Regular sales representative
    - `ADMIN` - Can invite users, create workspaces
- **Source:** `Role.java` enum

#### NFR-003: Data Isolation
- **User-Scoped Data:** Most services use `getCurrentUser()` from security context
    - Example: `GoalService.getAllGoals()` returns only authenticated user's goals
- **Workspace Isolation:** Users have `workspaceId` for multi-tenant separation
- **Source:** `GoalService.java`, `UserService.java`, `SecurityConfig.java`

---

### 3.2 Performance

#### NFR-004: File Size Limit
- **Constraint:** Maximum 50MB per audio file upload
- **Reason:** Prevent resource exhaustion and ensure reasonable processing times

#### NFR-005: Summary Length Limit
- **Constraint:** Maximum 1000 characters for call summaries
- **Purpose:** UI display and database optimization

---

### 3.3 Integration

#### NFR-006: N8N Workflow Integration
- **Description:** System depends on external N8N service for:
    - Audio-to-text transcription
    - Sentiment analysis
    - Summary generation
    - Chat Q&A responses
- **Communication:** Asynchronous webhook-based
- **Callback Endpoint:** `/api/calls/public-webhook/{callId}`

---

## 4. ASSUMPTIONS

1. **N8N Availability:** We assume N8N service is always available and responsive
2. **Audio Quality:** We assume uploaded audio files are of sufficient quality for transcription
3. **Network Stability:** File uploads complete without interruption
4. **Single User Session:** Users access the system from one device at a time
5. **Browser Support:** React frontend supports modern browsers (Chrome, Firefox, Safari, Edge)

---

## 5. GAPS & CLARIFICATIONS NEEDED

### 5.1 Processing Failures
**Question:** How does the system handle N8N processing failures?
- No explicit error handling visible in code
- `webhookResponse` field stores debugging info, but user-facing error flow is unclear
- **Action Required:** Clarify with dev team on retry logic, timeout handling, user notifications

### 5.2 Transcript Accuracy
**Question:** How is transcript quality validated?
- No confidence scoring or quality metrics visible
- **Action Required:** Clarify if low-quality transcripts affect sentiment analysis or if there's a minimum confidence threshold

### 5.3 Manager Visibility
**Question:** Can Sales Managers view all team members' calls?
- Admin role exists but cross-user data visibility is unclear
- No "view all team calls" feature found in backend
- **Action Required:** Confirm manager permissions and team-level reporting requirements

### 5.4 Processing Time Expectations
**Question:** What is the expected processing time for calls?
- Processing time depends on N8N performance (not defined in code)
- **Action Required:** Define SLA for transcript generation (seconds? minutes?)

### 5.5 Workspace Admin Scope
**Question:** What can workspace admins do beyond inviting users?
- Admin role defined but full permission scope unclear
- **Action Required:** Document all admin-only features

---

## 6. OUT OF SCOPE (Features NOT Implemented)

### 6.1 File Management
- ❌ No delete/re-upload functionality for calls
- ❌ No bulk file upload (only one file at a time)
- ❌ No direct transcript upload (audio only)

### 6.2 Transcript Editing
- ❌ Users cannot edit generated transcripts
- ❌ No manual correction of transcription errors

### 6.3 Keyword/Intent Detection
- ❌ No keyword tracking or highlighting
- ❌ No intent detection
- ❌ No custom keyword configuration
- ❌ No keyword frequency analysis

**Note:** Only full transcript storage and sentiment analysis exist

### 6.4 Sentiment Editing
- ❌ Users cannot override or correct sentiment results
- ❌ No manual sentiment adjustment

### 6.5 Follow-up Suggestions
- ❌ No email template generation
- ❌ No action items list
- ❌ No "mark as done" tracking
- ❌ No customization or export of suggestions

**Note:** Only basic `Call.summary` field exists (max 1000 chars)

### 6.6 Dashboard Filters
- ❌ No general search by customer name
- ❌ No keyword search in transcripts
- ❌ No advanced filtering UI in backend

### 6.7 Data Export
- ❌ No CSV/PDF/Excel export functionality
- ❌ No bulk data export from dashboard

### 6.8 Charts & Visualizations
- ❌ No charts/graphs in backend (only data endpoints)
- **Note:** Frontend may render charts from API data

---

## 7. WORKSPACE & MULTI-TENANCY

### 7.1 Workspace Management
- **Description:** Admins can create workspaces
- **Workspace Fields:**
    - Company name
    - Industry
    - Address
- **User Assignment:** Users belong to workspaces via `workspaceId`
- **Source:** `WorkspaceService.java`, `Workspace.java`

### 7.2 Data Isolation
- **Tenant Separation:** All user data scoped to workspace
- **Security:** Users cannot access data from other workspaces

---

## 8. EXISTING TEST COVERAGE

### 8.1 Current Testing
- **Framework:** BDD using Cucumber
- **Scenario Count:** 34 scenarios
- **Focus:** Basic functional validation

### 8.2 Known Issues
- Some bugs exist but not fully documented
- Edge cases not fully covered
- No structured defect tracking

---

## 9. NEXT STEPS (For Test Planning)

1. **Map Requirements to Test Cases** (RTM)
2. **Identify High-Risk Areas:**
    - N8N integration failures
    - Large file uploads
    - Concurrent user access
    - Webhook timeout scenarios
3. **Define Test Data Requirements:**
    - Sample audio files (various formats, sizes)
    - Invalid file samples
    - Test users with different roles
4. **Prioritize Testing:**
    - P0: Core upload + sentiment flow
    - P1: Dashboard, goals, sales tracking
    - P2: Edge cases, error handling

---

## 10. DOCUMENT HISTORY

| Version | Date       | Author         | Changes |
|---------|------------|----------------|---------|
| 1.0 | 2026-04-27 | Rivindu Perera | Initial requirement analysis based on code review |

---

**End of Document**