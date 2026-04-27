# VOCALYX - Requirement Traceability Matrix (RTM)

**Project:** VOCALYX - AI-Powered Sales Call Analysis Platform  
**Document Type:** Requirement Traceability Matrix  
**Date:** 2026-04-26  
**QA Engineer:** [Your Name]  
**Version:** 1.0

---

## PURPOSE OF THIS DOCUMENT

The RTM ensures:
- ✅ Every requirement has corresponding test coverage
- ✅ No requirement is missed during testing
- ✅ Defects can be traced back to specific requirements
- ✅ Test coverage can be measured objectively

---

## 1. FUNCTIONAL REQUIREMENTS - FILE UPLOAD & PROCESSING

| Req ID | Requirement | Priority | Test Scenarios | Test Case IDs | Status | Defect IDs | Notes |
|--------|-------------|----------|----------------|---------------|--------|------------|-------|
| FR-001 | System shall accept audio file uploads (MP3, WAV, OGG, etc.) up to 50MB | P0 | 1. Upload valid MP3 file (10MB)<br>2. Upload valid WAV file (5MB)<br>3. Upload file at 50MB limit<br>4. Upload file exceeding 50MB<br>5. Upload unsupported format (.AVI, .MOV)<br>6. Upload non-audio file (.TXT, .PDF)<br>7. Upload empty file (0KB)<br>8. Upload corrupted audio file<br>9. Test upload with slow network<br>10. Test concurrent uploads | TBD | Not Tested | - | **Risk:** Large file handling, network interruption |
| FR-002 | System shall track call processing status (PENDING → PROCESSING → COMPLETED) | P0 | 1. Verify status is PENDING after upload<br>2. Verify status changes to PROCESSING when N8N starts<br>3. Verify status changes to COMPLETED when webhook returns<br>4. Check status transitions are sequential<br>5. Verify status persistence in database | TBD | Not Tested | - | **Dependency:** N8N integration |
| FR-003 | System shall automatically generate transcripts from audio files via N8N | P0 | 1. Upload clear audio, verify transcript accuracy<br>2. Upload audio with background noise<br>3. Upload audio with multiple speakers<br>4. Upload audio with accents/dialects<br>5. Verify transcript stored in DB<br>6. Test N8N webhook callback<br>7. Test transcript for 1-min audio<br>8. Test transcript for 30-min audio | TBD | Not Tested | - | **Clarification Needed:** What if N8N fails? Timeout handling? |

---

## 2. FUNCTIONAL REQUIREMENTS - SENTIMENT ANALYSIS

| Req ID | Requirement | Priority | Test Scenarios | Test Case IDs | Status | Defect IDs | Notes |
|--------|-------------|----------|----------------|---------------|--------|------------|-------|
| FR-004 | System shall analyze sentiment with percentage (0-100) and label | P0 | 1. Verify sentiment for positive call<br>2. Verify sentiment for negative call<br>3. Verify sentiment for neutral call<br>4. Test edge cases (0%, 100%)<br>5. Validate sentiment label format<br>6. Test sentiment for unclear transcript<br>7. Compare sentiment across similar calls<br>8. Verify sentiment stored correctly | TBD | Not Tested | - | **Clarification Needed:** Sentiment confidence threshold? |

---

## 3. FUNCTIONAL REQUIREMENTS - SUMMARY GENERATION

| Req ID | Requirement | Priority | Test Scenarios | Test Case IDs | Status | Defect IDs | Notes |
|--------|-------------|----------|----------------|---------------|--------|------------|-------|
| FR-005 | System shall generate call summary (max 1000 chars) | P1 | 1. Verify summary generated for short call<br>2. Verify summary generated for long call<br>3. Validate summary length ≤ 1000 chars<br>4. Test summary quality/relevance<br>5. Test summary for call with no clear topic<br>6. Verify summary stored in DB | TBD | Not Tested | - | **Note:** Summary quality is subjective, focus on length & presence |

---

## 4. FUNCTIONAL REQUIREMENTS - DASHBOARD & REPORTING

| Req ID | Requirement | Priority | Test Scenarios | Test Case IDs | Status | Defect IDs | Notes |
|--------|-------------|----------|----------------|---------------|--------|------------|-------|
| FR-006 | System shall display call history with title, date, summary, sentiment, contact, order ID | P1 | 1. Verify all fields displayed correctly<br>2. Test with 0 calls (empty state)<br>3. Test with 1 call<br>4. Test with 100+ calls (pagination)<br>5. Verify sorting (latest first)<br>6. Test call history API response format<br>7. Verify contact info linkage | TBD | Not Tested | - | **Risk:** Performance with large datasets |
| FR-007 | System shall display goals dashboard with total, achieved, pending, avg progress | P1 | 1. Verify dashboard with 0 goals<br>2. Verify metrics calculation accuracy<br>3. Test with partially completed goals<br>4. Test with 100% completed goals<br>5. Verify average progress calculation | TBD | Not Tested | - | - |
| FR-008 | System shall display sales metrics (total revenue, sales count, recent sales) | P1 | 1. Verify total revenue calculation<br>2. Verify sales count accuracy<br>3. Test recent sales list (chronological order)<br>4. Test with $0 sales<br>5. Test with large revenue numbers<br>6. Verify currency formatting | TBD | Not Tested | - | **Note:** May need to test with different currencies |

---

## 5. FUNCTIONAL REQUIREMENTS - CONTACT MANAGEMENT

| Req ID | Requirement | Priority | Test Scenarios | Test Case IDs | Status | Defect IDs | Notes |
|--------|-------------|----------|----------------|---------------|--------|------------|-------|
| FR-009 | System shall support Contact CRUD (Create, Read, Update, Delete) | P1 | 1. Create new contact with all fields<br>2. Create contact with minimal fields<br>3. Read/retrieve contact by ID<br>4. Update contact fields<br>5. Delete contact<br>6. Validate email format<br>7. Validate phone format<br>8. Test duplicate contact prevention<br>9. Test contact search | TBD | Not Tested | - | **Risk:** Data integrity, duplicate handling |

---

## 6. FUNCTIONAL REQUIREMENTS - ORDER TRACKING

| Req ID | Requirement | Priority | Test Scenarios | Test Case IDs | Status | Defect IDs | Notes |
|--------|-------------|----------|----------------|---------------|--------|------------|-------|
| FR-010 | System shall link calls to orders and auto-create orders for new contacts | P1 | 1. Upload call for existing contact with order<br>2. Upload call for new contact (verify auto-order creation)<br>3. Link multiple calls to same order<br>4. Verify order-call relationship in DB<br>5. Test order creation with missing contact data | TBD | Not Tested | - | **Dependency:** Contact management (FR-009) |

---

## 7. FUNCTIONAL REQUIREMENTS - SALES GOAL MANAGEMENT

| Req ID | Requirement | Priority | Test Scenarios | Test Case IDs | Status | Defect IDs | Notes |
|--------|-------------|----------|----------------|---------------|--------|------------|-------|
| FR-011 | System shall allow goal setting with date ranges and auto-calculate progress | P2 | 1. Create goal with target amount and dates<br>2. Verify progress calculation from sales logs<br>3. Test status transitions (Not Started → In Progress → Completed)<br>4. Test goal with past end date<br>5. Test goal with future start date<br>6. Test goal with overlapping date ranges<br>7. Update goal mid-progress | TBD | Not Tested | - | **Note:** Progress calculation logic needs validation |

---

## 8. FUNCTIONAL REQUIREMENTS - SALES LOGGING

| Req ID | Requirement | Priority | Test Scenarios | Test Case IDs | Status | Defect IDs | Notes |
|--------|-------------|----------|----------------|---------------|--------|------------|-------|
| FR-012 | System shall track closed deals with amounts, dates, links to calls/contacts/orders | P1 | 1. Log sale with all fields<br>2. Verify total revenue updates<br>3. Verify sales count increments<br>4. Test with $0 sale amount<br>5. Test with negative amount (should fail)<br>6. Test sale date validation<br>7. Link sale to contact/order/call<br>8. Test bulk sale entry | TBD | Not Tested | - | **Risk:** Revenue calculation accuracy critical |

---

## 9. FUNCTIONAL REQUIREMENTS - CHAT Q&A

| Req ID | Requirement | Priority | Test Scenarios | Test Case IDs | Status | Defect IDs | Notes |
|--------|-------------|----------|----------------|---------------|--------|------------|-------|
| FR-013 | System shall allow users to ask questions about orders/calls via N8N | P2 | 1. Ask question about existing order<br>2. Ask question about non-existent order<br>3. Test with vague questions<br>4. Test with specific data requests<br>5. Verify N8N response format<br>6. Test response time<br>7. Test with no transcript data | TBD | Not Tested | - | **Dependency:** N8N integration |

---

## 10. FUNCTIONAL REQUIREMENTS - FILTERING & SEARCH

| Req ID | Requirement | Priority | Test Scenarios | Test Case IDs | Status | Defect IDs | Notes |
|--------|-------------|----------|----------------|---------------|--------|------------|-------|
| FR-014 | System shall filter calls by Order ID and Contact ID | P2 | 1. Filter calls by valid Order ID<br>2. Filter calls by valid Contact ID<br>3. Filter with non-existent Order ID<br>4. Filter sales logs by date range<br>5. Test combined filters (user + contact + order)<br>6. Verify filtered results accuracy | TBD | Not Tested | - | **Gap:** No keyword search or customer name search |

---

## 11. NON-FUNCTIONAL REQUIREMENTS - SECURITY & AUTHENTICATION

| Req ID | Requirement | Priority | Test Scenarios | Test Case IDs | Status | Defect IDs | Notes |
|--------|-------------|----------|----------------|---------------|--------|------------|-------|
| NFR-001 | System shall use JWT authentication with 24-hour token lifetime | P0 | 1. Register new user<br>2. Login with valid credentials<br>3. Login with invalid credentials<br>4. Verify JWT token in response<br>5. Access protected endpoint with valid token<br>6. Access protected endpoint without token<br>7. Access with expired token (after 24h)<br>8. Test password reset flow<br>9. Test user invitation flow | TBD | Not Tested | - | **Security Critical:** Token expiration, password strength |
| NFR-002 | System shall implement RBAC with USER and ADMIN roles | P0 | 1. Create USER account, verify permissions<br>2. Create ADMIN account, verify permissions<br>3. Test USER cannot access admin endpoints<br>4. Test ADMIN can invite users<br>5. Test ADMIN can create workspaces<br>6. Verify role assignment on registration | TBD | Not Tested | - | **Clarification Needed:** Full scope of admin permissions? |
| NFR-003 | System shall isolate data by user and workspace | P0 | 1. User A creates goal, verify User B cannot see it<br>2. User A uploads call, verify User B cannot access it<br>3. Test workspace isolation (different workspaces)<br>4. Verify `getCurrentUser()` scoping<br>5. Test cross-workspace data leakage prevention | TBD | Not Tested | - | **Security Critical:** Data isolation must be bulletproof |

---

## 12. NON-FUNCTIONAL REQUIREMENTS - PERFORMANCE

| Req ID | Requirement | Priority | Test Scenarios | Test Case IDs | Status | Defect IDs | Notes |
|--------|-------------|----------|----------------|---------------|--------|------------|-------|
| NFR-004 | System shall enforce 50MB max file size limit | P0 | 1. Upload 49MB file (should succeed)<br>2. Upload 50MB file (should succeed)<br>3. Upload 51MB file (should fail with proper error)<br>4. Verify error message clarity | TBD | Not Tested | - | Already covered in FR-001 but critical for perf |
| NFR-005 | System shall limit call summary to 1000 characters | P1 | 1. Verify summary truncation at 1000 chars<br>2. Test with short call (summary < 1000 chars)<br>3. Test with long call (summary should truncate)<br>4. Verify no data loss in truncation | TBD | Not Tested | - | Already covered in FR-005 |

---

## 13. NON-FUNCTIONAL REQUIREMENTS - INTEGRATION

| Req ID | Requirement | Priority | Test Scenarios | Test Case IDs | Status | Defect IDs | Notes |
|--------|-------------|----------|----------------|---------------|--------|------------|-------|
| NFR-006 | System shall integrate with N8N for AI processing via webhooks | P0 | 1. Verify N8N webhook URL configuration<br>2. Test successful N8N callback<br>3. Test N8N timeout scenario<br>4. Test N8N error response<br>5. Test N8N unavailability<br>6. Verify webhook payload format<br>7. Test webhook authentication/security<br>8. Measure end-to-end processing time | TBD | Not Tested | - | **Clarification Needed:** Error handling for N8N failures |

---

## 14. GAPS & CLARIFICATIONS (TESTING BLOCKED UNTIL RESOLVED)

| Gap ID | Description | Impact on Testing | Priority | Status | Resolution |
|--------|-------------|-------------------|----------|--------|------------|
| GAP-001 | No error handling for N8N processing failures | Cannot test failure scenarios | P0 | Open | Awaiting dev team clarification |
| GAP-002 | No transcript quality validation or confidence scoring | Cannot test low-quality audio scenarios | P1 | Open | Awaiting product owner input |
| GAP-003 | Manager visibility to team calls is unclear | Cannot test manager role permissions | P1 | Open | Awaiting requirements clarification |
| GAP-004 | Processing time SLA not defined | Cannot validate performance requirements | P1 | Open | Awaiting business requirements |
| GAP-005 | Full admin permission scope unclear | Cannot test all admin features | P2 | Open | Awaiting documentation |

---

## 15. OUT-OF-SCOPE FEATURES (NOT TESTED)

| Feature | Reason | Future Testing? |
|---------|--------|-----------------|
| Delete/Re-upload calls | Feature not implemented | Yes - when feature is built |
| Bulk file upload | Feature not implemented | Yes - when feature is built |
| Direct transcript upload | Feature not implemented | Maybe - depends on roadmap |
| Transcript editing | Feature not implemented | Yes - when feature is built |
| Keyword/Intent detection | Feature not implemented | Yes - high priority for future |
| Sentiment override | Feature not implemented | Maybe - depends on product decision |
| Email template generation | Feature not implemented | Yes - when feature is built |
| Advanced dashboard filtering | Feature not implemented | Yes - when feature is built |
| Data export (CSV/PDF) | Feature not implemented | Yes - likely needed for compliance |
| Charts/graphs rendering | Backend only provides data | Frontend team responsibility |

---

## 16. TEST COVERAGE SUMMARY

| Category | Total Requirements | Test Scenarios Defined | Coverage % | Notes |
|----------|-------------------|------------------------|------------|-------|
| File Upload & Processing | 3 | 23 scenarios | 100% | High-risk area, thorough testing planned |
| Sentiment Analysis | 1 | 8 scenarios | 100% | - |
| Summary Generation | 1 | 6 scenarios | 100% | - |
| Dashboard & Reporting | 3 | 19 scenarios | 100% | Performance testing needed |
| Contact Management | 1 | 9 scenarios | 100% | - |
| Order Tracking | 1 | 5 scenarios | 100% | - |
| Sales Goals | 1 | 7 scenarios | 100% | - |
| Sales Logging | 1 | 8 scenarios | 100% | Critical for revenue accuracy |
| Chat Q&A | 1 | 7 scenarios | 100% | - |
| Filtering & Search | 1 | 6 scenarios | 100% | - |
| Security & Auth | 3 | 16 scenarios | 100% | Security-critical |
| Performance | 2 | 4 scenarios | 100% | - |
| Integration | 1 | 8 scenarios | 100% | N8N dependency risk |
| **TOTAL** | **19** | **126 scenarios** | **100%** | Ready for LAP 3: Test Case Development |

---

## 17. RISK ASSESSMENT

| Risk ID | Risk Description | Impact | Probability | Mitigation Strategy | Owner |
|---------|------------------|--------|-------------|---------------------|-------|
| RISK-001 | N8N service failure during testing | High | Medium | Mock N8N responses for testing, test error handling separately | QA Team |
| RISK-002 | Large file uploads causing timeout/crash | High | Medium | Test with various file sizes, monitor server resources | QA + DevOps |
| RISK-003 | Data isolation breach (security vulnerability) | Critical | Low | Thorough RBAC and workspace isolation testing | QA Lead |
| RISK-004 | Performance degradation with 1000+ calls | Medium | Medium | Load testing planned for LAP 4 | QA + Performance Team |
| RISK-005 | Unclear requirements blocking test execution | Medium | High | Daily standup with dev/product to clarify gaps | QA Lead |

---

## 18. TRACEABILITY METRICS 

| Metric | Target | Current | Status |
|--------|--------|---------|--------|
| Requirements with test coverage | 100% | 100% | ✅ On Track |
| Test cases executed | 100% | 0% | ⏳ Pending LAP 5 |
| Pass rate | ≥95% | - | ⏳ Pending LAP 5 |
| Critical defects | 0 | - | ⏳ Pending LAP 5 |
| Blocker issues | 0 | - | ⏳ Pending LAP 5 |

---

## 19. DOCUMENT HISTORY

| Version | Date       | Author         | Changes |
|---------|------------|----------------|---------|
| 1.0 | 2026-04-27 | Rivindu Perera | Initial RTM created based on requirement analysis |

---


**End of RTM**