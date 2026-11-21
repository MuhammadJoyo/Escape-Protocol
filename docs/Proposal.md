# Project Proposal — Escape Protocol: The Last Exit

| Item | Value |
|---|---|
| Course | Software Construction and Development (SCD) |
| Instructor | Dr. Jawad Hussain Awan |
| Class | SE V, Sukkur IBA University |
| Project type | Group project (2–3 students) |
| Repository | https://github.com/MuhammadJoyo/Escape-Protocol |
| Document status | Module 00 — **DRAFT, awaiting student review** |
| Revision | 2 (Final Revision) |

---

## 1. Project Overview

**Escape Protocol — The Last Exit** is a Java Swing desktop escape-room game. The player is trapped in a mysterious facility and must solve puzzles, collect clues, unlock rooms, and escape before a 20-minute countdown ends.

The game has four rooms (Office → Library → Laboratory → Final Exit), an inventory, a hint system with penalties, scoring, and victory / game-over screens. A separate **TCP leaderboard server** lets several game clients submit and view scores concurrently.

The project is built to be a *functional, playable application* (not static screens) and to demonstrate **all 26 SCD concepts** and **all 10 numbered requirements** of the official assignment (`Assignment - 2.pdf`).

## 2. Problem Statement

The assignment asks the team to design, construct, test and deploy a self-chosen Java project that practically applies the whole SCD syllabus. A game is a good vehicle because it naturally needs:

- stateful logic with strict rules (progression, scoring, validation),
- a graphical interface,
- time-based behaviour (countdown),
- shared data between concurrent activities (timer, UI, network clients),
- a small rule language (unlock conditions) that can be parsed and evaluated.

The problem is therefore: *build a correct, thread-safe, well-tested, well-documented, networked Java game whose design visibly demonstrates the course concepts.*

## 3. Computing Challenges

| # | Challenge | Why it is hard | Planned answer |
|---|---|---|---|
| CC-1 | Correct state machine for game lifecycle | Illegal transitions (e.g., answering after game over) cause logic bugs | Explicit state enum, guarded transitions, specifications, unit tests |
| CC-2 | Progression that cannot be bypassed | Locked rooms, solved-once puzzles, duplicate rewards | Engine-owned unlock rules; UI only calls the engine; tests for bypass attempts |
| CC-3 | Countdown without freezing the GUI | Swing is single-threaded | `ScheduledExecutorService` for ticking; `SwingUtilities.invokeLater` for UI updates |
| CC-4 | Race between timer expiry and final answer | Two threads decide the terminal state | Single lock-protected state owner; exactly one terminal state; concurrency test |
| CC-5 | Concurrent leaderboard clients | Lost updates, corrupted ranking, blocked server | TCP server, `BlockingQueue` request pipeline, lock-protected leaderboard, multi-client tests |
| CC-6 | Untrusted input (UI, sockets, files) | Crashes and corrupted state | Central validation with regular expressions; bounded line lengths; fail-safe errors |
| CC-7 | Little language + parser generator | Grammar design, AST, error reporting, build integration | ANTLR 4 grammar for unlock rules, recursive AST, recursive evaluator |
| CC-8 | Network unavailability | Game must not hang or crash | Short connect timeout, background execution, clear messages |
| CC-9 | Scope vs. time | Many mandatory concepts | Priority tiers, module plan, playable core first, risk register |

## 4. Objectives

1. Deliver a fully playable four-room escape game with GUI, timer, scoring, hints, inventory.
2. Deliver a real TCP leaderboard (server + in-game client).
3. Demonstrate every one of the 26 official concepts with real code, tests, or clearly labelled demonstrations, each traceable in `docs/Requirements-Traceability.md`.
4. Produce proposal, SRS, architecture/database/UML, testing, and technical documentation.
5. Use Git/GitHub with meaningful commits from every group member.
6. Package a runnable JAR and prepare a live demonstration and viva.

## 5. Scope

### 5.1 In scope

**Priority 1 (must work):** Maven project; main menu with name entry; engine; four rooms; Office/Library/Laboratory/Final puzzles; inventory; hints; timer; scoring/penalties; victory/game-over; restart; Swing UI; core JUnit tests; GitHub + README.

**Priority 2 (planned, implemented after core works):** socket leaderboard (**retained and committed to**); `BlockingQueue` message passing; locks; recursive clue structure; Escape Rule Language with ANTLR; regex validators; map/filter/reduce; file-based save/resume; pause/resume; second laboratory puzzle.

**Priority 3 (stretch):** achievements, sound, extra polish.

### 5.2 Out of scope

- A separate DBMS (MySQL/SQLite etc.) — see §8; **pending instructor confirmation**.
- Online (internet) hosting of the leaderboard — the server runs on localhost / LAN.
- Multiplayer gameplay (only the leaderboard is shared).
- Authentication/accounts.

## 6. Four-Room Gameplay

| Room | Puzzle | Correct solution | Reward |
|---|---|---|---|
| 1 Office | Read documents dated 1998, 2004, 2012; identify the password | `2004` | Unlocks computer/drawer → Office Key + clue |
| 2 Library | Order books Red 1985, Green 1992, Blue 2001, Yellow 2010 oldest→newest | Red → Green → Blue → Yellow | Provisional code `7394` |
| 3 Laboratory | Sequence `2, 6, 12, 20, ?` (+ optional symbol-matching puzzle) | `30` | Final clue |
| 4 Final Exit | Combine collected codes and enter final solution | Office + Library + Laboratory values (exact format fixed in SRS FR-PZ-09) | Victory screen |

Rules: wrong attempts and hints cost points; timer expiry ends the game; rooms unlock strictly in order.

## 7. Socket Leaderboard

A real **TCP client–server** feature:

- `LeaderboardServer` (separate `main`) accepts many clients at once.
- Line-based text protocol: `SUBMIT`, `TOP`, `PING`, `QUIT`.
- Server validates names and scores; malformed requests get an error reply, connection stays safe.
- Client handler threads (producers) put requests on a `BlockingQueue`; a worker (consumer) updates a lock-protected leaderboard.
- The Swing game submits the final score and displays the top scores, using background threads so the GUI never blocks.
- If the server is down the game shows a clear message and keeps working.
- Tests: multiple clients, simultaneous submissions, invalid messages, server shutdown.

This is **not** a hardcoded or local-only list.

## 8. Database Design Approach

The assignment requires *designing the database*. The team has decided **not** to add a separate DBMS. Instead:

1. Produce a **logical data model** (entities, attributes, primary/foreign keys, relationships, constraints) and an **ER diagram** (first cut in `docs/SRS.md` §12, finalised in Module 01 `docs/Architecture.md`).
2. Implement **file-based persistence** (save file for resume; leaderboard file on the server) with validation.
3. Document clearly that a logical design is *not* a DBMS implementation.
4. Document the smallest change needed if a DBMS is later required: introduce a `Repository` interface per aggregate (already planned for storage), then add a JDBC implementation and schema script generated from the ER model.

> **INSTRUCTOR CONFIRMATION REQUIRED (Q-02):** Does a documented logical data model plus file-based persistence satisfy the "design the database" requirement? This has **not** been approved.

## 9. Technology Stack

| Area | Choice |
|---|---|
| Language | Java (compiled with `--release 21`; installed JDK is Temurin 27.0.1) |
| GUI | Java Swing |
| Build | Maven (3.10.1 installed) |
| Tests | JUnit 5 |
| Parser generator | ANTLR 4 (planned; compatibility with the installed JDK to be verified in Module 02/10) |
| Networking | `java.net` sockets |
| Concurrency | `java.util.concurrent` |
| Persistence | Plain text/properties/CSV files (format fixed in Module 08) |
| VCS | Git + GitHub |
| IDE | VS Code |

## 10. SCD Concepts (summary)

All 26 concepts are mapped with class, evidence, test, and status in **`docs/Requirements-Traceability.md` §2**. Headline mapping:

| Area | Where it appears |
|---|---|
| Static checking, basic Java, GUI | Maven/`-Xlint:all`, whole code base, Swing UI |
| Testing, debugging, code review, version control | JUnit 5 suites, debugging log, review log, Git history |
| Specifications, avoiding debugging | Javadoc contracts, `checkRep()`, input validation, assertions |
| Mutability, ADTs, AF/RI, equality, interfaces | `Item`, `Inventory`, `Leaderboard`, `Puzzle` interface |
| Recursion, recursive data types | Clue tree and rule-expression AST |
| Regex, grammars, parser generators, little languages | Validators; ANTLR grammar for the Escape Rule Language |
| Concurrency, thread safety, locks, queues, sockets | Timer, leaderboard server, request queue |
| Map/filter/reduce | Streams over inventory, puzzles, scores |

## 11. Group Structure and Proposed Responsibilities — **TO BE ASSIGNED**

| Member | Name / Roll No. |
|---|---|
| Member 1 | `[Name and Roll Number]` |
| Member 2 | `[Name and Roll Number]` |
| Member 3 | `[Name and Roll Number — if applicable]` |

| Responsibility area | Proposed owner (TO BE ASSIGNED) |
|---|---|
| Requirements, documentation, UML | `[TBA]` |
| Domain model / game engine | `[TBA]` |
| Puzzle system | `[TBA]` |
| Swing UI and rooms | `[TBA]` |
| Socket server/client, concurrency | `[TBA]` |
| Little language / ANTLR | `[TBA]` |
| Testing and debugging | `[TBA]` |
| Git/GitHub, integration, deployment, presentation | `[TBA]` |

Rule: ownership is for *leading* an area. **Every member must be able to explain the whole architecture, their own code, tests, and the SCD concepts in the viva.** Reviews are cross-member (author never reviews own code).

## 12. Risks and Mitigations

| ID | Risk | Likelihood / Impact | Mitigation |
|---|---|---|---|
| R-01 | Not enough time for all mandatory concepts | High / High | Playable core first; module plan; weekly checkpoints; cut only Priority 3 |
| R-02 | ANTLR/Maven plugin incompatible with JDK 27 | Medium / Medium | Verify in Module 02 with a tiny grammar; fall back to running ANTLR tool JAR manually or another generator, documented |
| R-03 | Concurrency bugs (races, deadlock) | Medium / High | Few shared objects, documented guard per object, stress tests, simple locking order |
| R-04 | Unequal member contribution | Medium / High | Contribution matrix, per-member commits, mini-viva practice |
| R-05 | Database requirement rejected | Low–Medium / Medium | Q-02 to instructor early; repository interface eases JDBC addition |
| R-06 | Extension date unknown | Certain / Medium | Plan in relative effort; fix calendar once date confirmed (Q-01) |
| R-07 | Idea-approval status unclear | Unknown / High | Confirm with instructor (Q-05) |
| R-08 | Manual file copying causes mistakes | Medium / Low | Complete files with exact paths; `mvn clean test` after every module |
| R-09 | Swing threading mistakes | Medium / Medium | UI only via EDT helper; code-review checklist item |
| R-10 | Port conflicts / firewall for server | Low / Low | Configurable port; localhost default |

## 13. Deliverables

1. GitHub repository with source, tests, docs, README.
2. Proposal, SRS, architecture/data/UML docs, testing report, technical docs, limitations.
3. Requirements traceability (assignment requirements, 26 concepts, FRs, NFRs).
4. Executable game JAR and leaderboard-server launch instructions.
5. Code-review log and debugging log.
6. Live demonstration script and viva preparation, with per-member demo checklist.

## 14. Development Strategy

- Documentation first (Modules 00–01), then foundation, domain, engine, puzzles, UI → **playable core early**.
- Then timer/concurrency, persistence, socket leaderboard, language/parser.
- Then full testing, review, documentation finalisation, deployment.
- One logical module per commit group; every commit follows a review; `mvn clean test` must pass before pushing.
- Details and dependencies: `docs/Development-Plan.md`.

## 15. Deployment Approach

- `mvn clean package` produces an executable JAR (`target/escape-protocol.jar`, name to be confirmed in Module 02).
- Launch on Windows 10 with a Java 21+ runtime: `java -jar target\escape-protocol.jar`.
- Leaderboard server launched separately (second `main`/second launch command), documented in README.
- Deployment is **not** claimed complete until the JAR is built and run (ideally on a second machine) with evidence.
