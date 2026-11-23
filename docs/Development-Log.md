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
| DEC-09 | `com.escapeprotocol.player.Player` is the implementation class for the player name (not `PlayerName`), because committed `Architecture.md` and `Design.md` say `Player`. Only one class is created | Module 03 | Student instruction. The M03 entry in `Development-Plan.md` must be corrected by the student |
| DEC-10 | `RoomId` order indexes are numbered 1 to 4 (OFFICE = 1 … FINAL_EXIT = 4) | Module 03 | **Assumption** made while writing the code; `Architecture.md`/`Design.md` were not visible to the code generator. Verify against `Design.md` (see OI-05) |
| DEC-11 | Other Module 03 assumptions: `ItemKind` is a separate enum file in `inventory`; `Room` accepts an empty puzzle list; `Player` equality is case-sensitive; invalid player names throw `IllegalArgumentException`, null constructor arguments of `Item`/`Room` throw `NullPointerException` | Module 03 | **Assumptions**; verify against `Design.md` (see OI-05) |
| DEC-12 | Module 04 decisions D1–D8 approved by the student: D1 only `Puzzle` and `PuzzleResult` are added in M04 (puzzle implementations stay in M05; tests use the test-only `FakePuzzle`); D2 ids `office_year`, `library_order`, `lab_sequence`, `final_exit`, `office_key`, `code_7394`, `lab_clue_30`; D3 `Room` unchanged, `GameCatalog` owns the completion rewards; D4 remaining seconds are injected as an `IntSupplier`; D5 a new hint on a solved puzzle is REFUSED without penalty; D6 an unknown puzzle id throws `IllegalArgumentException`; D7 `GameSession.expire()` is implemented now; D8 `HardCodedUnlockRule` is item-based | Module 04 | Approved by the student |
| DEC-13 | `GameEngine` follows Design §6: `newGame(name)` creates and starts a fresh session, so there is no separate public `start()`; `restart()` reuses the last player and always creates a new session; the engine has no lock (atomic fields only; the session lock guards all game state); `shutdown()` drops the session and refuses later operations; the listener is passed to each session, which notifies it after releasing its lock | Module 04 | Implemented; the brief's separate `start()` was not added because the design has none |
| DEC-14 | Names and descriptions of the three reward items were written in Module 04 Part 2 because the SRS fixes none; each description contains the value needed for the final answer (2004, 7394, 30) | Module 04 | Editable text in `GameCatalog.standardCompletionItems()` |
| DEC-15 | **SymbolPuzzle (SRS FR-PZ-07, P2) is deferred.** It is not implemented in Module 05 and not added to `GameCatalog`; the Laboratory requires only `lab_sequence`. If it is implemented later it becomes required for Laboratory completion (SRS rule). The SRS requirement text was not changed | Module 05 | Decision recorded as the SRS requires; FR-PZ-07 stays a deferred P2 item |
| DEC-16 | Puzzle input rules live in `util.Validators` (FR-LL-07): `parseYear`, `parseLibraryOrder`, `parseSequenceAnswer`, `parseFinalExitCode` plus `isValidXxx` shortcuts. Every input is trimmed; null and empty are invalid; ASCII digits only. Sequence answers allow an optional minus sign and at most 9 digits (no overflow), leading zeros are accepted (`030` is 30). The Library order is four comma-separated words (regex), case-insensitive, then checked for known and distinct books. In the Final Exit code the year and the library code are compared as text and the number as an integer | Module 05 | Implemented; assumptions where the SRS is silent (leading zeros, negative integers, case) |
| DEC-17 | `GameCatalog.standardWithPuzzles()` is the only change to `GameCatalog`. The puzzle ids exist as constants in the puzzle classes and in `GameCatalog` (the `puzzles` package must not depend on `game`); tests keep them equal. `FinalExitPuzzle.REQUIRED_ITEM_IDS` likewise matches the catalog reward ids | Module 05 | Implemented; consistency covered by `GameCatalogTest` |
| DEC-18 | The Library prompt lists the books in a scrambled order with their years, so the player has to sort them | Module 05 | Implemented (text only) |

## 4. Module 00 Revision History

| Rev | Content | Notes |
|---|---|---|
| 1 | Five Module 00 files generated earlier | Earlier text was **not available** when Revision 2 was produced |
| 2 | Final Revision: all five files rewritten from the "Module 00 Final Revision" brief and the official assignment PDF | Student should compare with their existing files in VS Code and keep any useful extra content from Rev 1 |
| 2.1 | Targeted correction of `Development-Plan.md` and `Development-Log.md` only. Plan: removed duplicated/concatenated M02/M03 content (reported in the student's copy), each of M00–M13 appears once in the roadmap and once in the module details, roadmap "Class" column added, M09 and M10 stated as required deliverables, dependency diagram checked against roadmap, effort shares total 100 %. Log: setup and Git facts updated. | The other three files were not changed. The corrected files must replace the staged versions and be re-staged. |
| 3 | Module 02 completion update (2026-11-22): §1, §2 note, §3 (DEC-07), §5, new §5a, §7 (Q-04, OI-03, OI-04), §7a, §8, §9 | Only the listed sections changed; all other content is unchanged |
| 4 | Module 03 start update (2026-11-22): §3 (DEC-09 to DEC-11), §5, new §5b, §7 (OI-05), §7a, §8, §9 | Module 03 is recorded as in progress only; the Module 02 Git facts were updated from the student's report |
| 5 | Module 04 completion update (2026-11-23): §3 (DEC-12 to DEC-14), §5 and §5b (Module 03 recorded as completed), new §5c, §7 (OI-05 to OI-07), §7a, §8, §9 | Module 04 is recorded as implemented with local verification pending for Part 4 only |
| 6 | Module 05 update (2026-11-23): §3 (DEC-15 to DEC-18), §5, §5c (Module 04 recorded as completed), new §5d, §7a, §8, §9 | Module 05 is recorded as implemented with local verification pending |

Corrections/omissions addressed in Rev 2 (per brief): socket leaderboard retained with queue/lock/multi-client requirements; database design with ER diagram and instructor-confirmation item; all 26 concepts and all 10 requirements traced; parser generator separated from regex; little language defined; group placeholders; no invented dates, names, results or approvals.

## 5. Current Progress

| Module | Status |
|---|---|
| M00 Requirements, Proposal, SRS | Completed, committed and pushed (student-reported in the Module 03 brief). Commit hash not recorded here. |
| M01 Architecture and Design | Completed, committed and pushed (student-reported in the Module 03 brief). `docs/Architecture.md` and `docs/Design.md` exist. Commit hash not recorded here. |
| M02 Maven Project Foundation | **Completed and verified by the student on 2026-11-22** (see §5a). Committed and pushed as `1210f63` (student-reported). |
| M03 Domain Model and ADTs | **Completed** (see §5b). 64 tests passed locally; committed and pushed (student-reported). |
| M04 Game Engine and Progression | **Completed** (see §5c). 199 tests passed locally; committed and pushed as `c3740b9` (student-reported). |
| M05 Puzzle System | **Implemented, local verification pending** (see §5d). Delivered with 104 new tests (expected total 303); not yet run; not committed. |
| M06–M13 | Not started (planned) |

Concept rows in `docs/Requirements-Traceability.md`: none `VERIFIED`. After Module 05, 17 concepts are `IN PROGRESS` (C-01, C-02, C-03, C-05, C-06, C-07, C-08, C-09, C-12, C-13, C-14, C-15, C-17, C-18, C-19, C-20, C-24) and 9 remain `PLANNED` (C-04, C-10, C-11, C-16, C-21, C-22, C-23, C-25, C-26).

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

**Git:** Module 02 was committed as `1210f63` and pushed (student-reported).

## 5b. Module 03 — Domain Model and Abstract Data Types (COMPLETED)

**Objective:** Implement the basic domain classes and the Inventory ADT, with unit tests. No session, engine, puzzles, GUI, timer, networking or persistence.

**Delivered:**
- Source: `player/Player`, `util/Validators`, `rooms/RoomId`, `rooms/Room`, `inventory/Item`, `inventory/ItemKind`, `inventory/Inventory`, `game/GameState`.
- Tests: `ValidatorsTest`, `PlayerTest`, `RoomOrderTest`, `RoomTest`, `InventoryTest`, `InventoryRepInvariantTest`, `ItemEqualityTest`, `ItemImmutabilityTest`, `GameStateTransitionTest`.
- Naming: `Player` is used, not `PlayerName` (DEC-09). The M03 entry in `Development-Plan.md` was corrected.

**Verification (student-reported):** `mvn clean test` ran 64 tests (59 Module 03 tests plus 5 Module 02 tests) with 0 failures, 0 errors and 0 skipped; `mvn clean package` succeeded; Module 03 was committed and pushed. The commit hash is not recorded here. `RoomId` order indexes 1 to 4 (DEC-10) match the Design and SRS data model; see OI-05 for the other assumptions.

## 5c. Module 04 — Game Engine and Progression (COMPLETED)

**Objective:** Implement the game engine, the session, scoring, hints and room progression, with unit and integration tests, without Swing, real puzzles, timer, persistence, networking or ERL.

**Parts delivered**

| Part | Content | Tests added | Total tests after the part (student-reported) |
|---|---|---|---|
| 1 | `SubmissionResult`, `HintResult`, `GameListener`, `UnlockContext`, `UnlockRule`, `ScoreCalculator`, `HintService` | 35 | 99 |
| 2 | `Puzzle`, `PuzzleResult`, `HardCodedUnlockRule`, `GameCatalog`, `FakePuzzle` (test helper) | 31 | 130 |
| 3 | `SessionSnapshot`, `GameSession` | 40 | 170 |
| 4 | `GameEngine` | 29 | 199 |

**Implementation summary**
- **GameSession and GameEngine:** `GameSession` owns the player, state, score, inventory, solved puzzles, unlocked rooms and revealed hints. `GameEngine` is the facade: it looks puzzles up in `GameCatalog` and delegates every operation to the session. It does not duplicate scoring, hint costs, progression, unlocking or state transitions (DEC-13).
- **SessionSnapshot immutability:** a record whose collections are copied and unmodifiable; it holds no reference to the session.
- **Puzzle abstraction and catalog:** `Puzzle` and `PuzzleResult` are the contract only; `GameCatalog` holds the rooms, the reward items and the supplied puzzles and contains no puzzle implementation. Tests use the test-only `FakePuzzle`.
- **Scoring and hints:** score 1000; wrong valid answer −25; new hint −50; floor at 0; invalid format and re-viewing a hint cost nothing; victory adds 2 points per remaining second using `long` arithmetic capped at `Integer.MAX_VALUE`; at most 3 hints per puzzle.
- **State transitions and progression:** the five legal transitions of `GameState`; `expire()` moves RUNNING to GAME_OVER; Library, Laboratory and Final Exit unlock when the player owns `office_key`, `code_7394` and `lab_clue_30` (`HardCodedUnlockRule`); a correct Final Exit answer with no time left ends in GAME_OVER.
- **Locking and thread safety:** one `ReentrantLock` in `GameSession` guards all mutable session state, including the non-thread-safe `Inventory`. There is no second lock. The listener is called after the lock is released, and no I/O happens under the lock. `GameEngine` has no lock and keeps only atomic references.
- **Tests:** 40 `GameSessionTest` tests include real multi-thread tests (concurrent wrong answers, concurrent hint requests, and a 100-round race of a final answer against `expire()`); `GameEngineTest` has 29 tests using the real session, catalog and unlock rule.

**Results**
- Parts 1–3: `mvn clean test` passed with 99, 130 and 170 tests respectively, 0 failures, 0 errors, 0 skipped; `mvn clean package` BUILD SUCCESS and the JAR was generated (student-reported).
- Part 4: `mvn clean test` ran 199 tests with 0 failures, 0 errors and 0 skipped; `mvn clean package` BUILD SUCCESS (student-reported).

**Remaining limitations and deferred work**
- No timer: remaining seconds come from an injected `IntSupplier`; Module 07 supplies `GameTimer` and also stops it in restart and shutdown.
- No real puzzles: Module 05. `GameCatalog.missingPuzzleIds()` lists puzzles not yet supplied.
- The session's representation invariant includes "every unlocked room satisfies the unlock rule"; revisit it when ERL rules arrive in Module 10 (OI-06).
- No code-review entry has been recorded for Module 04, no `Testing.md`, and no coverage figure has been measured.
- Module 04 was committed and pushed as `c3740b9 feat: implement core game engine` (student-reported).

## 5d. Module 05 — Puzzle System (IMPLEMENTED; local verification pending)

**Objective:** Replace the test-only puzzle doubles with the four real P1 puzzles, centralize puzzle input validation, and integrate them with the existing catalog, session and engine, without changing the Module 04 logic.

**Implementation summary**
- **Four puzzles** (all in `puzzles`, all implementing the existing `Puzzle` and returning the existing `PuzzleResult`):
  - `OfficePuzzle` (`office_year`, OFFICE): the year `2004` is CORRECT, any other four-digit year INCORRECT, anything else INVALID_FORMAT.
  - `LibraryPuzzle` (`library_order`, LIBRARY): `RED,GREEN,BLUE,YELLOW` is CORRECT; the other 23 orders INCORRECT; missing, duplicate, unknown or extra books INVALID_FORMAT; spaces and case are ignored.
  - `SequencePuzzle` (`lab_sequence`, LABORATORY): `30` is CORRECT, other integers INCORRECT; non-integers and numbers over 9 digits INVALID_FORMAT.
  - `FinalExitPuzzle` (`final_exit`, FINAL_EXIT): `2004-7394-30` is CORRECT; a well-formed wrong code is INCORRECT and the feedback says only how many of the 3 components are right (never which); it declares the required items `office_key`, `code_7394` and `lab_clue_30`.
- **Puzzle contract:** each puzzle is a final class with only static final constants (no instance state), so `submit` is pure and total: it never throws for player input, never touches the session, the inventory or the score, and returns the same result for the same input. Rewards, penalties, missing-clue checks and victory remain in `GameSession`.
- **Validation:** `Validators` now holds all regular expressions (FR-LL-07) and the parse methods listed in DEC-16.
- **Progressive hints:** every puzzle returns exactly three distinct, non-blank, unmodifiable hints; hint cost, limit and state are unchanged (`HintService`, `GameSession`).
- **Catalog integration:** `GameCatalog.standardWithPuzzles()` builds the standard rooms and reward items with the four real puzzles; it has no missing puzzles. The Laboratory requires `lab_sequence` alone.
- **Rooms, rewards and progression:** unchanged `HardCodedUnlockRule`; the reward items `office_key`, `code_7394`, `lab_clue_30` are still granted by the session when a room is completed.
- **Resubmission and penalties:** unchanged session behavior is now tested with the real puzzles: `ALREADY_SOLVED` without penalty or second reward; INVALID_FORMAT free; INCORRECT costs 25 with a floor at 0.
- **Not changed:** `pom.xml`, `GameSession`, `GameEngine`, `SessionSnapshot`, `Puzzle`, `PuzzleResult`, `ScoreCalculator`, `HintService`, `HardCodedUnlockRule`, and every existing test (two existing test files had tests appended).

**Tests (delivered, not yet run by the student)**

| Test class | New tests |
|---|---|
| `OfficePuzzleTest` | 11 |
| `LibraryPuzzleTest` | 14 |
| `SequencePuzzleTest` | 12 |
| `FinalExitPuzzleTest` | 17 |
| `PuzzleContractTest` | 13 |
| `ResubmitTest` | 7 |
| `PenaltyRulesTest` | 11 |
| `ValidatorsTest` (appended) | 12 |
| `GameCatalogTest` (appended) | 7 |
| **Total new** | **104** |

Expected total after Module 05: 199 + 104 = **303**. This has **not been run locally**, so no Module 05 test or package result is recorded.

**Deferred and open**
- **SymbolPuzzle (FR-PZ-07, P2)** is deferred (DEC-15); this is the decision the SRS asks to record in Module 05.
- Assumptions where the SRS is silent (DEC-16): leading zeros (`030`), negative integers, letter case and surrounding spaces are accepted as described there.
- No code-review entry and no `Testing.md` exist yet for Module 05; no coverage figure was measured.
- The UI, timer, networking, persistence and ERL are not part of this module.

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
| OI-05 | Check the Module 03 assumptions (DEC-10, DEC-11) against `Design.md` and correct the code if they differ | Partly closed: `orderIndex` 1–4 matches the Design and SRS data model; the empty puzzle list, case-sensitive player equality and exception types are not specified by the Design and remain assumptions |
| OI-06 | Revisit the `GameSession` representation-invariant clause "unlocked rooms satisfy the rule" when ERL rules (possibly using `not has(...)`) are added | Open (Module 10) |
| OI-07 | `GameEngine.restart()` and `shutdown()` must also stop the timer once `GameTimer` exists | Open (Module 07) |

## 7a. Test Status

Student-reported results from Module 02 (2026-11-22):

| Test class | Tests | Result |
|---|---|---|
| `MainTest` | 3 | Passed |
| `SmokeGrammarTest` | 2 | Passed |
| **Total** | **5** | **0 failures, 0 errors, 0 skipped** |

`mvn clean test` reported BUILD SUCCESS. These tests cover only the window title/messages and a smoke grammar. No game-logic tests exist yet. No coverage figure has been measured.

**Module 03 (student-reported):** `mvn clean test` ran 64 tests (59 Module 03 + 5 Module 02), 0 failures, 0 errors, 0 skipped.

**Module 04 (student-reported through Part 3):**

| After | Tests run | Failures / errors / skipped | `mvn clean package` |
|---|---|---|---|
| Part 1 | 99 | 0 / 0 / 0 | BUILD SUCCESS |
| Part 2 | 130 | 0 / 0 / 0 | BUILD SUCCESS |
| Part 3 | 170 | 0 / 0 / 0 | BUILD SUCCESS |
| Part 4 | 199 | 0 / 0 / 0 | BUILD SUCCESS |

**Part 4 (student-reported):** 199 tests, 0 failures, 0 errors, 0 skipped; `mvn clean package` BUILD SUCCESS.

**Module 05:** 104 new tests were delivered (expected total 303). The student has **not yet run them**, so no Module 05 result is recorded.

## 8. Git Status

- Branch `main`, remote `origin` → `https://github.com/MuhammadJoyo/Escape-Protocol.git`.
- **Module 00 to Module 04:** committed and pushed (student-reported). Module 02 commit: `1210f63`. Module 04 commit: `c3740b9 feat: implement core game engine`. Other hashes are not recorded here.
- **Module 05:** delivered, not yet verified locally, and **not committed or pushed**.
- Planned commit message after Module 05 passes: `feat: implement puzzle system`.

## 9. Log Entries

| Date | Entry |
|---|---|
| `[student to fill]` | Module 00 Rev 2 documents received and copied to `docs\` |
| `[student to fill]` | `.gitignore` inspected; `git add .gitignore docs` run successfully (staged, not committed) |
| `[student to fill]` | Rev 2.1 corrections to Development-Plan and Development-Log received; review pending |
| 2026-11-22 | Module 02 completed and verified by the student: 5 tests passed, `mvn clean test` and `mvn clean package` succeeded, Swing window launched via Maven and via the JAR, ANTLR generation verified. Ready for commit, not yet committed or pushed |
| 2026-11-22 | Module 02 committed as `1210f63` and pushed (student-reported); Modules 00 and 01 also reported as pushed |
| 2026-11-22 | Module 03 started: source and tests delivered to the student; not yet run; `Player` chosen over `PlayerName` |
| 2026-11-22 | Module 03 completed: 64 tests passed locally, committed and pushed (student-reported) |
| 2026-11-23 | Module 04 decisions D1–D8 approved; Parts 1–3 delivered and verified locally by the student (99, 130 and 170 tests, all passing) |
| 2026-11-23 | Module 04 Part 4 (`GameEngine`, `GameEngineTest`, documentation updates) delivered; local verification pending; not committed |
| 2026-11-23 | Module 04 completed: 199 tests passed, `mvn clean package` BUILD SUCCESS, committed and pushed as `c3740b9` (student-reported) |
| 2026-11-23 | Module 05 (puzzle system) delivered in one archive: four puzzles, `Validators` extension, `GameCatalog.standardWithPuzzles()`, 104 new tests; local verification pending; not committed. SymbolPuzzle deferred (DEC-15) |
