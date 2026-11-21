# Development Log — Escape Protocol: The Last Exit

This log records only facts that have actually happened or been reported by the student. Planned work is labelled as planned. No test result, commit, push or deployment is recorded here unless it was performed and reported.

---

## 1. Project Setup Facts (verified by the student via terminal output)

| Item | Value |
|---|---|
| Project directory | `D:\EscapeProtocol` |
| Operating system | Windows 10 |
| Java | Eclipse Temurin OpenJDK 27.0.1.0.2 |
| javac | 27.0.1.0.2 |
| Maven | 3.10.1 |
| Git | 2.54.0.windows.1 |
| Editor | VS Code |
| Shell | Windows CMD (unless PowerShell requested) |
| Target compatibility | Java 21 (`--release 21`) — **to be verified** in Module 02 |

## 2. Current Repository State (as reported by the student)

- Git repository initialized; current branch `main`.
- Remote `origin` → `https://github.com/MuhammadJoyo/Escape-Protocol.git`.
- **No commits yet.**
- `.gitignore` exists. The student inspected it and reported that it contains the expected exclusions (build output and IDE files). Its exact content is not reproduced here; it can be re-checked in Module 02.
- `docs/` directory exists.
- All five Module 00 Markdown files (`Proposal.md`, `SRS.md`, `Requirements-Traceability.md`, `Development-Plan.md`, `Development-Log.md`) were copied into `D:\EscapeProtocol\docs\` (student-reported).
- `git add .gitignore docs` was executed successfully (student-reported). The files are **staged**.
- **No commit and no push have happened.**
- No Maven project files, Java sources or tests exist yet (Module 00 deliberately contains no Java code).

## 3. Decisions Made

| ID | Decision | Date recorded | Status |
|---|---|---|---|
| DEC-01 | Project is **Escape Protocol — The Last Exit** (Java Swing escape-room game) | Module 00 | Confirmed by student |
| DEC-02 | Group project of 2–3 students | Module 00 | Confirmed by student; names pending |
| DEC-03 | Deadline extension granted; exact new date unknown | Module 00 | Date pending (Q-01) |
| DEC-04 | **Socket leaderboard retained** as a planned implementation deliverable (real TCP, multi-client) | Module 00 | Confirmed by student |
| DEC-05 | **No separate DBMS**; logical data model + ER diagram + file-based persistence | Module 00 | Student decision; **instructor approval NOT obtained** (Q-02) |
| DEC-06 | Stack: Java, Swing, Maven, JUnit 5, Git/GitHub | Module 00 | Confirmed |
| DEC-07 | ANTLR 4 chosen as the planned parser generator for the Escape Rule Language | Module 00 | **Proposed** by this plan; compatibility unverified |
| DEC-08 | Initial game constants (1200 s timer, base score 1000, −25 per wrong attempt, −50 per hint, +2 per remaining second, final answer format `YYYY-CCCC-N`, port 5050) | Module 00 | **Proposed**; to be confirmed in M04/M05/M09 |

## 4. Module 00 Revision History

| Rev | Content | Notes |
|---|---|---|
| 1 | Five Module 00 files generated earlier | Earlier text was **not available** when Revision 2 was produced |
| 2 | Final Revision: all five files rewritten from the "Module 00 Final Revision" brief and the official assignment PDF | Student should compare with their existing files in VS Code and keep any useful extra content from Rev 1 |
| 2.1 | Targeted correction of `Development-Plan.md` and `Development-Log.md` only. Plan: removed duplicated/concatenated M02/M03 content (reported in the student's copy), each of M00–M13 appears once in the roadmap and once in the module details, roadmap "Class" column added, M09 and M10 stated as required deliverables, dependency diagram checked against roadmap, effort shares total 100 %. Log: setup and Git facts updated. | The other three files were not changed. The corrected files must replace the staged versions and be re-staged. |

Corrections/omissions addressed in Rev 2 (per brief): socket leaderboard retained with queue/lock/multi-client requirements; database design with ER diagram and instructor-confirmation item; all 26 concepts and all 10 requirements traced; parser generator separated from regex; little language defined; group placeholders; no invented dates, names, results or approvals.

## 5. Current Progress

| Module | Status |
|---|---|
| M00 Requirements, Proposal, SRS | Five files copied to `docs\` and staged. **Not approved and not complete**: review of the corrected files is still pending, and no commit exists. |
| M01–M13 | Not started |

Concept rows in `docs/Requirements-Traceability.md`: all `PLANNED`; none `VERIFIED`.

## 6. Debugging Log

Real defects only. Template (copy a row per defect):

| Issue ID | Description | Steps to reproduce | Expected | Actual | Root cause | Fix | Regression test | Status |
|---|---|---|---|---|---|---|---|---|
| — | *No defects recorded yet* | | | | | | | |

## 7. Open Issues and Instructor-Confirmation Questions

| ID | Question / issue | Why it matters | Status |
|---|---|---|---|
| Q-01 | What is the exact revised submission/demo date? | Converts the relative plan into dates | Open |
| Q-02 | Do a documented logical data model + ER diagram + file-based persistence satisfy "design the database"? If a real DBMS is required, which one is accepted? | AR-03 compliance | Open — **not approved** |
| Q-03 | Is an external dependency (ANTLR 4) acceptable for the parser-generator requirement, and is a separate server `main` acceptable for the socket requirement? | C-18, C-21 | Open |
| Q-04 | Is a Java 21-targeted JAR run on a newer JDK (27) acceptable for the demonstration machine? Which Java version will the lab use? | Deployment | Open |
| Q-05 | Has "Escape Protocol — The Last Exit" been accepted as the project idea (the PDF required the idea by email by 06-11-2026)? | Risk of non-approved topic | Open — **status unknown to this process** |
| Q-06 | Are group size (2 or 3) and member list final? Does the viva require each member to demonstrate specific modules? | Contribution matrix | Open |
| Q-07 | Is a game with a leaderboard accepted as satisfying "handle concurrent requests" (AR-07)? | AR-07 | Open |

| ID | Other open item | Status |
|---|---|---|
| OI-01 | Member names and roll numbers | Open |
| OI-02 | Responsibility assignment (Development-Plan §5) | Open |
| OI-03 | `.gitignore` inspected by the student (expected exclusions present); re-verify when Maven `target/` exists | Partly done (re-check in Module 02) |
| OI-04 | Verify ANTLR plugin works with installed JDK | Open (Module 02) |

## 7a. Test Status

No tests have been written or run. **Test status: none.**

## 8. Git Status

- Branch `main`, remote `origin` (see §2).
- Staged: `.gitignore` and `docs` via `git add .gitignore docs` (student-reported success).
- Commits: **0**. Pushes: **0**.
- `Development-Plan.md` and `Development-Log.md` were corrected after staging (Rev 2.1). Once the corrected versions are copied over, run `git add docs` again so the staged content matches.
- Module 00 is **not** approved; no commit should be made until the student confirms the review.
- Planned commit after review: `docs: add module 00 requirements, SRS and plan`.

## 9. Log Entries

| Date | Entry |
|---|---|
| `[student to fill]` | Module 00 Rev 2 documents received and copied to `docs\` |
| `[student to fill]` | `.gitignore` inspected; `git add .gitignore docs` run successfully (staged, not committed) |
| `[student to fill]` | Rev 2.1 corrections to Development-Plan and Development-Log received; review pending |
