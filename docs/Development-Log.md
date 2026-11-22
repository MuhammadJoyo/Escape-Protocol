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
| Target compatibility | Java 21 (`--release 21`) — configured in `pom.xml`; compilation succeeded in Module 02 (student-reported) |

## 2. Current Repository State (as reported by the student)

*Snapshot recorded during Module 00. It is kept as history. The current Git status is in §8.*

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
| DEC-07 | ANTLR 4 chosen as the planned parser generator for the Escape Rule Language | Module 00 | **Proposed** by this plan. ANTLR 4 plugin generation and a smoke test succeeded in Module 02 on JDK 27 with `--release 21` (student-reported). Instructor acceptance is still open (Q-03) |
| DEC-08 | Initial game constants (1200 s timer, base score 1000, −25 per wrong attempt, −50 per hint, +2 per remaining second, final answer format `YYYY-CCCC-N`, port 5050) | Module 00 | **Proposed**; to be confirmed in M04/M05/M09 |

## 4. Module 00 Revision History

| Rev | Content | Notes |
|---|---|---|
| 1 | Five Module 00 files generated earlier | Earlier text was **not available** when Revision 2 was produced |
| 2 | Final Revision: all five files rewritten from the "Module 00 Final Revision" brief and the official assignment PDF | Student should compare with their existing files in VS Code and keep any useful extra content from Rev 1 |
| 2.1 | Targeted correction of `Development-Plan.md` and `Development-Log.md` only. Plan: removed duplicated/concatenated M02/M03 content (reported in the student's copy), each of M00–M13 appears once in the roadmap and once in the module details, roadmap "Class" column added, M09 and M10 stated as required deliverables, dependency diagram checked against roadmap, effort shares total 100 %. Log: setup and Git facts updated. | The other three files were not changed. The corrected files must replace the staged versions and be re-staged. |
| 3 | Module 02 completion update (2026-11-22): §1, §2 note, §3 (DEC-07), §5, new §5a, §7 (Q-04, OI-03, OI-04), §7a, §8, §9 | Only the listed sections changed; all other content is unchanged |

Corrections/omissions addressed in Rev 2 (per brief): socket leaderboard retained with queue/lock/multi-client requirements; database design with ER diagram and instructor-confirmation item; all 26 concepts and all 10 requirements traced; parser generator separated from regex; little language defined; group placeholders; no invented dates, names, results or approvals.

## 5. Current Progress

| Module | Status |
|---|---|
| M00 Requirements, Proposal, SRS | Completed and committed (student-reported in the Module 02 brief). Commit hash not recorded here. |
| M01 Architecture and Design | Completed and committed (student-reported in the Module 02 brief). `docs/Architecture.md` and `docs/Design.md` exist. Commit hash not recorded here. |
| M02 Maven Project Foundation | **Completed and verified by the student on 2026-11-22** (see §5a). Ready for commit; **not yet committed or pushed**. |
| M03–M13 | Not started (planned) |

Concept rows in `docs/Requirements-Traceability.md`: none `VERIFIED`. After Module 02, C-01, C-02, C-03, C-18 and C-24 are `IN PROGRESS`; the other 21 concepts remain `PLANNED`.

## 5a. Module 02 Completion Entry — 2026-11-22

**Module:** M02 — Maven Project Foundation. **Status: completed and verified by the student (student-reported results).**

**Objective:** Create a Maven project that targets Java 21, runs JUnit 5 tests, applies compiler static checks, generates an ANTLR 4 parser, opens a basic Swing window and packages a runnable JAR. No gameplay was implemented.

**Work completed**
- `pom.xml`: Maven project configured; compiler `release 21`; `-Xlint:all`; JUnit 5; Surefire; JAR plugin with `com.escapeprotocol.app.Main` as the manifest main class; ANTLR 4 Maven plugin and runtime; exec plugin for running with Maven.
- `src/main/java/com/escapeprotocol/app/Main.java`: basic Swing window opened with `SwingUtilities.invokeLater`.
- `src/test/java/com/escapeprotocol/app/MainTest.java` (3 tests).
- `src/main/antlr4/com/escapeprotocol/lang/Smoke.g4` (smoke-test grammar) and `src/test/java/com/escapeprotocol/lang/SmokeGrammarTest.java` (2 tests).
- Package skeleton created for the `com.escapeprotocol` sub-packages defined in `docs/Architecture.md`.
- `README.md` starter created; `.gitignore` reviewed.

**Verification results (reported by the student)**

| Check | Result |
|---|---|
| `mvn clean test` | BUILD SUCCESS |
| `MainTest` | 3 tests passed |
| `SmokeGrammarTest` | 2 tests passed |
| Total tests | 5 run, 0 failures, 0 errors, 0 skipped |
| `mvn clean package` | Succeeded |
| Swing application launched through Maven | Window opened |
| Packaged JAR launched | Window opened |
| ANTLR generated sources and compiled classes | Verified present |
| `target/` ignored by Git | Confirmed |

**Not recorded:** test coverage percentage (no coverage tool used), review of compiler lint warnings, deployment on any other machine, instructor approval of any item.

**Known limitation:** The packaged JAR does not bundle the ANTLR runtime. It launches now because `Main` does not use ANTLR. A shading or assembly step will be required once the game uses ANTLR (planned for Module 10 or earlier if needed).

**Scope boundary:** The ANTLR work is a smoke test only. The Escape Rule Language grammar (`Escape.g4`) and everything in Module 03 and later remain planned.

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
| Q-04 | Is a Java 21-targeted JAR run on a newer JDK (27) acceptable for the demonstration machine? Which Java version will the lab use? | Deployment | Open. The Module 02 JAR launched on the student's machine (JDK 27); the lab's Java version is still unknown |
| Q-05 | Has "Escape Protocol — The Last Exit" been accepted as the project idea (the PDF required the idea by email by 06-11-2026)? | Risk of non-approved topic | Open — **status unknown to this process** |
| Q-06 | Are group size (2 or 3) and member list final? Does the viva require each member to demonstrate specific modules? | Contribution matrix | Open |
| Q-07 | Is a game with a leaderboard accepted as satisfying "handle concurrent requests" (AR-07)? | AR-07 | Open |

| ID | Other open item | Status |
|---|---|---|
| OI-01 | Member names and roll numbers | Open |
| OI-02 | Responsibility assignment (Development-Plan §5) | Open |
| OI-03 | `.gitignore` inspected by the student (expected exclusions present); re-verify when Maven `target/` exists | Done in Module 02: student confirmed `target/` is ignored by Git (re-check before each commit) |
| OI-04 | Verify ANTLR plugin works with installed JDK | Closed in Module 02: ANTLR 4 generation and smoke test succeeded on JDK 27 with `--release 21` (student-reported) |

## 7a. Test Status

Student-reported results from Module 02 (2026-11-22):

| Test class | Tests | Result |
|---|---|---|
| `MainTest` | 3 | Passed |
| `SmokeGrammarTest` | 2 | Passed |
| **Total** | **5** | **0 failures, 0 errors, 0 skipped** |

`mvn clean test` reported BUILD SUCCESS. These tests cover only the window title/messages and a smoke grammar. No game-logic tests exist yet. No coverage figure has been measured.

## 8. Git Status

- Branch `main`, remote `origin` → `https://github.com/MuhammadJoyo/Escape-Protocol.git`.
- **Module 00 and Module 01:** completed and committed (student-reported in the Module 02 brief). Commit hashes and push confirmation are not recorded in this log.
- **Module 02:** all verification checks passed (§5a). The work is **ready for commit**. It has **not been committed and not been pushed**.
- Before committing, the student should run `git status` and `git diff` and confirm that `target/` is not listed.
- Planned commit message: `chore: set up Maven project foundation` (to be run after the student confirms).

## 9. Log Entries

| Date | Entry |
|---|---|
| `[student to fill]` | Module 00 Rev 2 documents received and copied to `docs\` |
| `[student to fill]` | `.gitignore` inspected; `git add .gitignore docs` run successfully (staged, not committed) |
| `[student to fill]` | Rev 2.1 corrections to Development-Plan and Development-Log received; review pending |
| 2026-11-22 | Module 02 completed and verified by the student: 5 tests passed, `mvn clean test` and `mvn clean package` succeeded, Swing window launched via Maven and via the JAR, ANTLR generation verified. Ready for commit, not yet committed or pushed |