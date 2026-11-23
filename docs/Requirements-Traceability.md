# Requirements Traceability — Escape Protocol: The Last Exit

Version 2.4 — updated for Module 05 on 2026-11-23. Rows not listed in §10, §11, §12 or §13 are unchanged from Version 2.0 (Module 00 Final Revision).

**Status values:** `PLANNED` · `IN PROGRESS` · `VERIFIED` · `BLOCKED`. `IN PROGRESS` means part of the evidence exists and was run, and the rest is still planned.
**Rule:** nothing is `VERIFIED` until the implementation exists *and* its test/demonstration has actually been run with evidence recorded in `docs/Testing.md` or `docs/Development-Log.md`. At Module 00 every implementation row is `PLANNED`. Class/test names below are *planned names* and may be refined in Module 01.

IDs match `docs/SRS.md`. Module numbers match `docs/Development-Plan.md`.

---

## 1. Official Assignment Requirements (AR)

Source: `Assignment - 2.pdf`.

| ID | Official requirement | Planned evidence | Module(s) | Status |
|---|---|---|---|---|
| AR-01 | Analyze the problem and identify its computing challenges | Proposal §3 (CC-1…CC-9) | M00 | PLANNED |
| AR-02 | Prepare a detailed project proposal and SRS | `docs/Proposal.md`, `docs/SRS.md` | M00 | PLANNED |
| AR-03 | Design architecture, database and UML diagrams | `docs/Architecture.md`, `docs/Design.md`, ER diagram (SRS §12 → M01), UML set (§7 below) | M01 | PLANNED |
| AR-04 | Develop a functional application using Java and appropriate tools | Maven project, Swing game, JAR (**M02:** Maven project, Swing window and JAR exist; placeholder only, no game yet) | M02–M07 **M04:** headless game engine exists; no UI, real puzzles or timer yet. **M05:** the four real puzzles exist; still no UI or timer. | IN PROGRESS |
| AR-05 | Implement required modules and demonstrate the relevant course concepts | Concept table (§2) | M03–M10 | PLANNED |
| AR-06 | Apply suitable data structures, algorithms, OOP and design principles | `docs/Design.md` data-structure table; packages/interfaces | M01, M03–M10 | PLANNED |
| AR-07 | Handle concurrent requests and ensure thread safety | Leaderboard server, timer, locks, concurrency tests | M07, M09 | PLANNED |
| AR-08 | Comprehensive software testing and debugging | JUnit suites, manual playthroughs, debugging log (**M02:** 5 JUnit tests pass, `MainTest` 3 and `SmokeGrammarTest` 2; no game tests yet) | M11 (all modules) **M04:** 170 tests passed locally (student-reported); `GameEngineTest` passed (199 tests in total). **M05:** 104 more tests delivered, local run pending. | IN PROGRESS |
| AR-09 | Maintain source code using Git and GitHub | Commit history, branches, per-member commits | All | PLANNED |
| AR-10 | Deploy the application and prepare complete technical documentation | Runnable JAR, README, docs set (**M02:** starter `README.md`; JAR packaged and launched, placeholder window only) | M12, M13 | IN PROGRESS |

**Important requirements from the PDF**

| ID | Requirement | Evidence | Status |
|---|---|---|---|
| AI-01 | Functional, not static-screen prototype | FR-UI-05; full-playthrough test AC-10 | PLANNED |
| AI-02 | Students demonstrate own implementation and explain code | Viva prep, per-member checklist (§9) | PLANNED |
| AI-03 | All major functionalities tested | AC-02, planned tests (§6) | PLANNED |
| AI-04 | Source original and documented | Javadoc (NFR-MT-02), review log | PLANNED |
| AI-05 | Each member demonstrates contribution in presentation/viva | Contribution matrix (Development-Plan §9), §9 | PLANNED |
| AI-06 | Final submission: GitHub repository + live demonstration | D-01, D-12 | PLANNED |

---

## 2. Official SCD Concepts (C-01 … C-26)

| ID | Exact concept name | Planned module / class | Planned implementation evidence | Planned test or demonstration | Status |
|---|---|---|---|---|---|
| C-01 | Static Checking | `pom.xml` (M02); whole code base | `--release 21`, `-Xlint:all`, strong typing, generics, `Optional`, `final`; optional extra static analysis tool only if it works on the installed JDK | Demo `docs/Static-Checking-Demo.md`: deliberately introduce a type error, record the compiler message; clean `-Xlint:all` build log **M02:** `--release 21` and `-Xlint:all` configured; builds succeeded. Lint review and demo document pending. | IN PROGRESS |
| C-02 | Basic Java | All packages | Classes, objects, control flow, collections, exceptions throughout | Code review; whole test suite **M02:** `Main` class only. | IN PROGRESS |
| C-03 | Testing | `src/test/java` | JUnit 5 unit + integration tests (§6) | `mvn clean test` output in `docs/Testing.md` **M02:** 5 JUnit 5 tests pass; no game tests yet. **M04:** 170 tests passed locally (student-reported) including 106 Module 04 tests; `GameEngineTest` (29) passed; 199 tests in total. **M05:** 104 more tests delivered (expected total 303), local run pending. | IN PROGRESS |
| C-04 | Code Review | `docs/Code-Review-Log.md` (+ checklist) | Cross-member reviews per module; issues and fixes recorded | Review log entries with reviewer ≠ author | PLANNED |
| C-05 | Version Control | Git/GitHub | Meaningful commits per module, branches where useful, all members commit | `git log`, GitHub history screenshot **M04:** Modules 00–03 committed and pushed (student-reported; Module 02 is `1210f63`); no `git log` evidence file yet; Module 04 not committed. | IN PROGRESS |
| C-06 | Specifications | Javadoc on `game`, `puzzles`, `inventory`, `networking` | Preconditions/postconditions/`@throws` on public methods | Tests derived from specs (`GameEngineTest`, `PuzzleContractTest`) **M04:** Javadoc contracts (requires, effects, exceptions) on `game` and `puzzles`; tests derived from them in `GameSessionTest`, `ScoreCalculatorTest`, `HintServiceTest`. | IN PROGRESS |
| C-07 | Designing Specifications | `docs/Design.md` spec section; Module 04/05 | Specs written before code for engine and `Puzzle` interface; testable statements | Spec-to-test mapping table in `docs/Design.md` **M04:** contracts written together with the code; the formal spec-to-test table in `docs/Design.md` does not exist yet. | IN PROGRESS |
| C-08 | Avoiding Debugging | Validators, `checkRep()`, assertions, defensive copies | Fail-fast validation at boundaries; `Objects.requireNonNull`; invariants checked after mutations | `ValidatorsTest`, `InventoryRepInvariantTest` **M03:** `Validators` and `Inventory.checkRep()`; tests passed (student-reported, 64 tests in total). **M04:** `GameSession.checkRep()` after every mutation, `requireNonNull` and range checks at boundaries, defensive copies (tests passed). **M05:** total `submit` methods and input validation at the boundary instead of exceptions (tests delivered, local run pending). | IN PROGRESS |
| C-09 | Mutability and Immutability | `inventory.Item`, `puzzles.PuzzleResult`, `leaderboard.ScoreEntry`, `lang.Expr`; mutable `GameSession` | Immutable value types (`final`, records, `List.copyOf`); mutable state confined to engine | `ItemImmutabilityTest` (defensive-copy and unmodifiable-view tests) **M03:** `Item`, `Room`, `Player` immutable; `ItemImmutabilityTest`, `RoomTest`; tests passed (student-reported, 64 tests in total). **M04:** immutable records `SubmissionResult`, `HintResult`, `PuzzleResult`, `UnlockContext`, `SessionSnapshot` (copies, unmodifiable views); mutable `GameSession` confined under its lock (tests passed). **M05:** puzzles are stateless final classes with unmodifiable hint lists and item sets. | IN PROGRESS |
| C-10 | Recursion | `lang.ExprEvaluator`, `clues.ClueNode` | Recursive evaluation, depth, referenced-id collection, clue-dependency traversal | `ExprEvaluatorTest`, `ClueTreeTest` | PLANNED |
| C-11 | Debugging | `docs/Development-Log.md` §6 | Real bug records: reproduction, root cause, fix, regression test (only real bugs) | Regression tests named after issue IDs | PLANNED |
| C-12 | Abstract Data Types | `inventory.Inventory`, `networking.Leaderboard` (and `GameSession`) | Operations-defined types hiding representation | `InventoryTest`, `LeaderboardTest` **M03:** `Inventory` and `InventoryTest`; tests passed (student-reported, 64 tests in total). **M04:** `GameSession` is an ADT with operations hiding its representation (`GameSessionTest` passed); `Leaderboard` is M09. | IN PROGRESS |
| C-13 | Abstraction Functions and Representation Invariants | Javadoc + `checkRep()` in `Inventory` and `Leaderboard` | Written AF (what the rep means) and RI (what must always hold) | RI tests; `checkRep()` called in tests after operations **M03:** AF/RI in Javadoc of `Inventory` (also `Item`, `Room`, `Player`); `InventoryRepInvariantTest`; tests passed (student-reported, 64 tests in total). **M04:** AF, RI and `checkRep()` documented and implemented for `GameSession`; RI violations are not injected by tests (unlike `Inventory`). | IN PROGRESS |
| C-14 | Interfaces | `puzzles.Puzzle`, `game.UnlockRule`, `storage.SaveRepository`, `lang.Expr` | Multiple implementations (4+ puzzles; 2+ unlock rules) | `PuzzleContractTest` runs same checks on every implementation **M04:** interfaces `Puzzle`, `UnlockRule`, `GameListener`; one `UnlockRule` implementation and test-only puzzle doubles; real puzzles M05, ERL rule M10. **M05:** four real `Puzzle` implementations (tests delivered, local run pending). | IN PROGRESS |
| C-15 | Equality | `inventory.Item`, `ScoreEntry`, `Expr` | Correct `equals`/`hashCode`/`toString`; behaviour in `Set`/`Map` | `ItemEqualityTest` (reflexive, symmetric, transitive, hash contract, HashSet behaviour) **M03:** equality in `Item` (by id), `Player`, `Room`; `ItemEqualityTest`, `PlayerTest`, `RoomTest`; tests passed (student-reported, 64 tests in total). **M04:** record value equality (`SessionSnapshot`, `HintResult`, `SubmissionResult`, `UnlockContext`) used in tests; `ScoreEntry` and `Expr` are later. | IN PROGRESS |
| C-16 | Recursive Data Types | `lang.Expr` AST (Has/Solved/And/Or/Not); `clues.ClueNode` | Immutable recursive types with structural recursion | `ExprStructureTest`, `ClueTreeTest` | PLANNED |
| C-17 | Regular Expressions and Grammars | `util.Validators` (regex); `Escape.g4` (grammar) | Regex for name, year, code, final answer, protocol; written grammar for ERL | `ValidatorsTest`; grammar documented in `docs/Design.md` **M05:** centralized regular expressions in `Validators` for the player name (M03) and the puzzle inputs (M05); the ERL grammar is M10. | IN PROGRESS |
| C-18 | Parser Generators | `src/main/antlr4/.../Escape.g4`, build plugin | ANTLR 4 generates lexer/parser during `mvn` build; visitor builds AST (**separate from regex**) | `RuleParserTest`; demo of generated sources in `target/generated-sources` **M02:** ANTLR plugin and runtime configured; `Smoke.g4` generated and compiled; `SmokeGrammarTest` 2/2 pass. `Escape.g4` not yet written (M10). | IN PROGRESS |
| C-19 | Concurrency | `concurrency.GameTimer`, `networking.LeaderboardServer` | Scheduled executor ticking; thread pool for clients | Timer tests; multi-client test **M04:** multi-thread tests of `GameSession` only; the timer (M07) and the client thread pool (M09) are not implemented. | IN PROGRESS |
| C-20 | Thread Safety | `GameSession`, `Leaderboard`, server | Shared data identified (session state, leaderboard map/list, queue) each with a documented guard (lock/atomic/confinement) | `SessionRaceTest`, `LeaderboardConcurrencyTest` (20+ threads) **M04:** `GameSession` has one documented lock guarding all mutable state including `Inventory`; listener called after unlock; concurrency tests passed; leaderboard and server are M09. | IN PROGRESS |
| C-21 | Sockets and Networking | `networking.LeaderboardServer`, `LeaderboardClient` | Real TCP server/client, text protocol | `SocketIntegrationTest` (real localhost sockets) | PLANNED |
| C-22 | Queues and Message-Passing | `networking.RequestQueue` (`BlockingQueue`) | Producer–consumer: handlers enqueue, worker consumes | `RequestQueueTest`, ordering under load | PLANNED |
| C-23 | Locks and Synchronization | `Leaderboard` (`ReadWriteLock`), `GameSession` (`ReentrantLock`) | Explicit locks with documented order, no I/O under lock | Stress tests + code review of lock use | PLANNED |
| C-24 | Graphical User Interfaces | `ui` package | Swing screens, EDT discipline | Manual playthrough checklists MT-UI-xx **M02:** basic Swing window via `SwingUtilities.invokeLater`; no game screens yet. | IN PROGRESS |
| C-25 | Map, Filter, Reduce | `game.GameStats`, `inventory` views, leaderboard stats | Streams on real game data (FR-AD-02) | `GameStatsTest` | PLANNED |
| C-26 | Little Languages | Escape Rule Language (ERL), `rules/game.erl` | DSL for unlock rules, parsed by ANTLR, evaluated by engine | `RuleParserTest`, `UnlockRuleIntegrationTest`; live demo editing a rule | PLANNED |

> If a concept cannot be implemented as planned, its row becomes `BLOCKED` with the reason and a smaller honest demonstration — never silently dropped.

---

## 3. Functional Requirements (FR)

| ID | Module | Planned source | Planned test | Status |
|---|---|---|---|---|
| FR-MN-01 | M06 | `ui.MainMenuPanel` | MT-UI-01 (manual) | PLANNED |
| FR-MN-02 | M05/M06 | `util.Validators`, `ui.NameEntryPanel` | `ValidatorsTest` + MT-UI-02 **M03:** `util.Validators` and `player.Player`; `ValidatorsTest`, `PlayerTest`; tests passed (student-reported, 64 tests in total). | IN PROGRESS |
| FR-MN-03 | M06 | `ui.InstructionsPanel` | MT-UI-03 | PLANNED |
| FR-MN-04 | M06/M07 | `app.Main`, `GameTimer.shutdown` | MT-UI-04, `GameTimerTest` | PLANNED |
| FR-MN-05 | M08 | `ui.MainMenuPanel`, `storage.SaveRepository` | MT-UI-05, `SaveLoadTest` | PLANNED |
| FR-MN-06 | M09 | `ui.LeaderboardPanel` | MT-UI-06 | PLANNED |
| FR-GM-01 | M04 | `game.GameEngine`, `game.GameSession` | `GameEngineTest` **M04:** `GameEngine.newGame` creates and starts a fresh session; initial values tested in `GameSessionTest` (passed); `GameEngineTest` (29 tests) passed (199 tests in total, student-reported); the 1200 s timer is M07. | IN PROGRESS |
| FR-GM-02 | M03 | `rooms.RoomId`, `rooms.Room` | `RoomOrderTest` **M03:** `RoomId`, `Room`; `RoomOrderTest`, `RoomTest`; tests passed (student-reported, 64 tests in total). **M04:** also used by `GameCatalog` (`GameCatalogTest` passed). | VERIFIED |
| FR-GM-03 | M04 | `game.UnlockRule`, `GameEngine` | `ProgressionTest` **M04:** `HardCodedUnlockRule`, `ProgressionTest` and `GameSessionTest` passed (170 tests, student-reported); engine path: `GameEngineTest` (29 tests) passed (199 tests in total, student-reported). | VERIFIED |
| FR-GM-04 | M04 | `game.GameState` | `GameStateTransitionTest` **M03:** `GameState.canMoveTo`; `GameStateTransitionTest`; tests passed (student-reported, 64 tests in total). **M04:** session transitions and terminal states in `GameSessionTest` passed (170 tests, student-reported). | VERIFIED |
| FR-GM-05 | M04 | `GameEngine` | `GameEngineTest` **M04:** victory and final score tested in `GameSessionTest` (passed); stopping the timer is M07; engine path: `GameEngineTest` (29 tests) passed (199 tests in total, student-reported). | IN PROGRESS |
| FR-GM-06 | M07 | `GameTimer`, `GameEngine` | `GameTimerTest` **M04:** `GameSession.expire()` (RUNNING to GAME_OVER) and refusals afterwards tested (passed); the timer that calls it is M07. | IN PROGRESS |
| FR-GM-07 | M04/M06 | `GameEngine.restart` | `GameEngineTest`, MT-UI-07 **M04:** `GameEngine.restart` creates a fresh session; `GameEngineTest` (29 tests) passed (199 tests in total, student-reported); the confirmation dialog is M06. | IN PROGRESS |
| FR-GM-08 | M07 | `GameTimer`, `GameEngine` | `PauseResumeTest` | PLANNED |
| FR-PZ-01 | M05 | `puzzles.Puzzle` | `PuzzleContractTest` **M05:** implemented; tests delivered, local run pending. The four puzzles implement `Puzzle` and return `PuzzleResult`. | IN PROGRESS |
| FR-PZ-02 | M05 | `puzzles.OfficePuzzle` | `OfficePuzzleTest` **M05:** implemented; tests delivered, local run pending. | IN PROGRESS |
| FR-PZ-03 | M05 | `OfficePuzzle`, `GameEngine` | `OfficePuzzleTest`, `ProgressionTest` **M05:** implemented; tests delivered, local run pending. The key is granted by the session (`ResubmitTest`). | IN PROGRESS |
| FR-PZ-04 | M05 | `puzzles.LibraryPuzzle` | `LibraryPuzzleTest` **M05:** implemented; tests delivered, local run pending. | IN PROGRESS |
| FR-PZ-05 | M05 | `LibraryPuzzle` | `LibraryPuzzleTest` **M05:** implemented; tests delivered, local run pending. Reward `code_7394` granted by the session. | IN PROGRESS |
| FR-PZ-06 | M05 | `puzzles.SequencePuzzle` | `SequencePuzzleTest` **M05:** implemented; tests delivered, local run pending. | IN PROGRESS |
| FR-PZ-07 | M05 | `puzzles.SymbolPuzzle` | `SymbolPuzzleTest` **P2, DEFERRED in M05 (decision DEC-15):** not implemented and not in `GameCatalog`; `lab_sequence` alone completes the Laboratory. If implemented later, it becomes required for Laboratory completion. | PLANNED |
| FR-PZ-08 | M05 | `SequencePuzzle`/`SymbolPuzzle`, `GameEngine` | `ProgressionTest` **M05:** implemented; tests delivered, local run pending. `SequencePuzzle` alone completes the Laboratory because the symbol puzzle is deferred (`GameCatalogTest`, `ResubmitTest`). | IN PROGRESS |
| FR-PZ-09 | M05 | `puzzles.FinalExitPuzzle` | `FinalExitPuzzleTest` **M05:** implemented; tests delivered, local run pending. Required clues `office_key`, `code_7394`, `lab_clue_30` declared; the session checks them (`PenaltyRulesTest`). | IN PROGRESS |
| FR-PZ-10 | M05 | `FinalExitPuzzle` | `FinalExitPuzzleTest` **M05:** implemented; tests delivered, local run pending. Feedback gives only the number of correct components. | IN PROGRESS |
| FR-PZ-11 | M04 | `GameEngine.submit` | `ProgressionTest` (bypass attempts) **M04:** refusals when not RUNNING or locked tested in `GameSessionTest` and `ProgressionTest` (passed); engine path: `GameEngineTest` (29 tests) passed (199 tests in total, student-reported). | VERIFIED |
| FR-PZ-12 | M05 | `GameEngine` | `ResubmitTest` **M05:** implemented; tests delivered, local run pending. `ResubmitTest` (real puzzles through `GameEngine`). | IN PROGRESS |
| FR-PZ-13 | M05 | `PuzzleResult`, `GameEngine` | `PenaltyRulesTest` **M05:** implemented; tests delivered, local run pending. `PenaltyRulesTest` (real puzzles through `GameEngine`). | IN PROGRESS |
| FR-IV-01 | M03 | `inventory.Inventory` | `InventoryTest` **M03:** `Inventory`, `Item`; `InventoryTest`, `ItemEqualityTest`; tests passed (student-reported, 64 tests in total). **M04:** `GameSession` adds each reward once (`GameSessionTest` passed). | VERIFIED |
| FR-IV-02 | M04 | `GameEngine` | `ProgressionTest` **M04:** items are added only by the session when a room is completed (`GameSessionTest` passed); engine path: `GameEngineTest` (29 tests) passed (199 tests in total, student-reported). | VERIFIED |
| FR-IV-03 | M06 | `ui.InventoryPanel` | MT-UI-08 | PLANNED |
| FR-IV-04 | M04/M05 | `game.HintService` | `HintServiceTest` **M04:** `HintServiceTest` and `GameSessionTest` passed (3 hints, 50 points, free re-view); the hint texts of the real puzzles are M05. **M05:** every real puzzle returns exactly 3 hints (`PuzzleContractTest`, `PenaltyRulesTest`); local run pending. | IN PROGRESS |
| FR-IV-05 | M04 | `HintService` | `HintServiceTest` **M04:** `HintServiceTest` and the hint-refusal tests in `GameSessionTest` passed (student-reported). | VERIFIED |
| FR-IV-06 | M10 | `clues.ClueNode` | `ClueTreeTest` | PLANNED |
| FR-TS-01 | M07 | `GameTimer`, `ui.TimerLabel` | `GameTimerTest`, MT-UI-09 | PLANNED |
| FR-TS-02 | M07 | `GameTimer`, `ui.EdtUtil` | Code review R-EDT item, MT-UI-09 | PLANNED |
| FR-TS-03 | M07 | `GameTimer` with `Clock` | `GameTimerTest` (fake clock), MT-TIM-01 | PLANNED |
| FR-TS-04 | M04 | `game.ScoreCalculator` | `ScoreCalculatorTest` **M04:** `ScoreCalculatorTest` and the penalty tests in `GameSessionTest` passed (student-reported). | VERIFIED |
| FR-TS-05 | M04 | `ScoreCalculator` | `ScoreCalculatorTest` **M04:** `ScoreCalculatorTest` (including the 3400 maximum and the overflow cap) and victory tests in `GameSessionTest` passed. | VERIFIED |
| FR-TS-06 | M07 | `GameTimer` | `GameTimerTest` | PLANNED |
| FR-TS-07 | M06 | `ui.GamePanel` | MT-UI-10 | PLANNED |
| FR-TS-08 | M07 | `GameSession` lock | `SessionRaceTest` **M04:** a 100-round race of a final answer against `expire()` in `GameSessionTest` passed; the real timer and `SessionRaceTest` are M07. | IN PROGRESS |
| FR-UI-01 | M06 | `ui.Theme` | MT-UI-11 | PLANNED |
| FR-UI-02 | M06 | `ui.*Panel` | MT-UI-12 | PLANNED |
| FR-UI-03 | M06 | `ui.GamePanel` | MT-UI-10 | PLANNED |
| FR-UI-04 | M06 | `ui.RoomNavigator` | MT-UI-13 | PLANNED |
| FR-UI-05 | M06 | all `ui` | MT-UI-14 (click-every-control checklist) | PLANNED |
| FR-UI-06 | M06 | `ui.FeedbackLabel` | MT-UI-15 | PLANNED |
| FR-UI-07 | M06/M07 | `ui.EdtUtil` | Code review, MT-UI-16 | PLANNED |
| FR-UI-08 | M06 | `ui.VictoryPanel`, `GameOverPanel` | MT-UI-17 | PLANNED |
| FR-UI-09 | M06 | `ui.MainFrame` | MT-UI-18 | PLANNED |
| FR-PS-01 | M08 | `storage.FileSaveRepository` | `SaveLoadTest` | PLANNED |
| FR-PS-02 | M08 | `FileSaveRepository`, `GameEngine` | `SaveLoadTest` | PLANNED |
| FR-PS-03 | M08 | `FileSaveRepository` | `CorruptSaveTest` | PLANNED |
| FR-PS-04 | M08 | `FileSaveRepository` | `SaveLoadTest` | PLANNED |
| FR-PS-05 | M08/M09 | `storage.LeaderboardFile` | `LeaderboardPersistenceTest` | PLANNED |
| FR-PS-06 | M08 | `GameSession.restore` + `checkRep` | `TamperedSaveTest` | PLANNED |
| FR-LB-01 | M09 | `networking.LeaderboardServer` | `SocketIntegrationTest` | PLANNED |
| FR-LB-02 | M09 | `LeaderboardServer` | `MultiClientTest` (≥ 20) | PLANNED |
| FR-LB-03 | M09 | `networking.Protocol` | `ProtocolTest` | PLANNED |
| FR-LB-04 | M09 | `Protocol`, `Validators` | `ProtocolTest` | PLANNED |
| FR-LB-05 | M09 | `Protocol`, handler | `MalformedRequestTest` | PLANNED |
| FR-LB-06 | M09 | `networking.Leaderboard` | `LeaderboardConcurrencyTest` | PLANNED |
| FR-LB-07 | M09 | `networking.RequestQueue` | `RequestQueueTest` | PLANNED |
| FR-LB-08 | M09 | `networking.LeaderboardClient` | `ClientTimeoutTest` | PLANNED |
| FR-LB-09 | M09 | `ClientHandler` | `DisconnectTest` | PLANNED |
| FR-LB-10 | M09 | `LeaderboardServer.stop` | `ServerShutdownTest` | PLANNED |
| FR-LB-11 | M09 | `ui.LeaderboardPanel` | MT-UI-06 | PLANNED |
| FR-LB-12 | M09 | `ui.VictoryPanel` | MT-UI-17, `ClientTimeoutTest` | PLANNED |
| FR-LB-13 | M09 | `LeaderboardServer` | MT-NET-01 (console log check) | PLANNED |
| FR-LL-01 | M10 | `Escape.g4`, `rules/game.erl` | `RuleParserTest` | PLANNED |
| FR-LL-02 | M10 | `pom.xml` ANTLR plugin | Build produces generated parser **M02:** parser generated from smoke grammar `Smoke.g4`; `Escape.g4` pending (M10). | IN PROGRESS |
| FR-LL-03 | M10 | `lang.Expr`, `lang.AstBuilder` | `ExprStructureTest` | PLANNED |
| FR-LL-04 | M10 | `lang.ExprEvaluator` | `ExprEvaluatorTest` | PLANNED |
| FR-LL-05 | M10 | `lang.RuleLoader` | `RuleSyntaxErrorTest` | PLANNED |
| FR-LL-06 | M10 | `game.ErlUnlockRule` | `UnlockRuleIntegrationTest` | PLANNED |
| FR-LL-07 | M05 | `util.Validators` | `ValidatorsTest` **M05:** all regular expressions are constants in `Validators` (name from M03; year, Library order, sequence answer, Final Exit code from M05; `ValidatorsTest` extended); local run pending. | IN PROGRESS |
| FR-AD-01 | M10 | `clues.ClueNode` | `ClueTreeTest` | PLANNED |
| FR-AD-02 | M10 | `game.GameStats` | `GameStatsTest` | PLANNED |

---

## 4. Non-Functional Requirements (NFR) and Cross-Cutting (CON / ERR / SEC / DEP)

| ID | Module | Planned evidence | Status |
|---|---|---|---|
| NFR-PF-01 | M06/M07 | Code review (no I/O on EDT), MT-UI-19 responsiveness check | PLANNED |
| NFR-PF-02 | M13 | MT-DEP-01 launch-time check | PLANNED |
| NFR-PF-03 | M09/M11 | `MultiClientTest` timing assertion | PLANNED |
| NFR-RL-01 | M09 | `MalformedRequestTest` | PLANNED |
| NFR-RL-02 | M08 | `CorruptSaveTest` | PLANNED |
| NFR-US-01 | M06 | MT-UI-11 | PLANNED |
| NFR-US-02 | M11 | MT-PLAY-01 playthrough with hints | PLANNED |
| NFR-MT-01 | M01/M11 | Package review; import check in code review | PLANNED |
| NFR-MT-02 | M03–M10 | Javadoc review checklist | PLANNED |
| NFR-MT-03 | M02/M11 | `mvn clean compile` log with `-Xlint:all` **M02:** `-Xlint:all` configured; build succeeded; warning review pending. | IN PROGRESS |
| NFR-TS-01 | M02 onward | `mvn clean test` log **M02:** `mvn clean test` BUILD SUCCESS, 5 tests (student-reported). | IN PROGRESS |
| NFR-TS-02 | M11 | Coverage report if a tool is added | PLANNED |
| NFR-PT-01 | M13 | MT-DEP-01 | PLANNED |
| NFR-DP-01 | M13 | `mvn clean package` output **M02:** `mvn clean package` succeeded; JAR launched. | IN PROGRESS |
| NFR-VC-01 | All | `git log` | PLANNED |
| NFR-DC-01 | M12 | Docs checklist | PLANNED |
| CON-01 | M07/M09 | Javadoc guard notes; review **M04:** the guard of `GameSession` (and `GameEngine`) is documented in Javadoc; `GameTimer` and `Leaderboard` come later. | IN PROGRESS |
| CON-02 | M07 | `SessionRaceTest` **M04:** single `ReentrantLock` in `GameSession`; three multi-thread tests in `GameSessionTest` passed; timer interaction (`SessionRaceTest`) is M07. | IN PROGRESS |
| CON-03 | M09 | `LeaderboardConcurrencyTest` | PLANNED |
| CON-04 | M06/M07 | Review + MT-UI-16 | PLANNED |
| CON-05 | M07/M09 | Lock-order doc + review | PLANNED |
| CON-06 | M07/M09 | `ServerShutdownTest`, thread-leak check | PLANNED |
| CON-07 | M11 | Concurrency test suite repeated runs | PLANNED |
| ERR-01 | M05–M10 | Invalid-input tests across validators/protocol/save/ERL | PLANNED |
| ERR-02 | M06 | MT-UI-20 (forced error dialog check) | PLANNED |
| ERR-03 | M08/M09 | Review: try-with-resources | PLANNED |
| SEC-01 | M05/M09 | `ValidatorsTest`, `ProtocolTest` | PLANNED |
| SEC-02 | M09 | `MalformedRequestTest` (oversize line), connection-limit test | PLANNED |
| SEC-03 | All | `.gitignore` + `git status` review before every push | PLANNED |
| SEC-04 | M08 | `CorruptSaveTest`, `TamperedSaveTest` | PLANNED |
| DEP-01…04 | M13 | JAR build + launch evidence | PLANNED |

---

## 5. Project Deliverables (D)

| ID | Deliverable | Location | Module | Status |
|---|---|---|---|---|
| D-01 | GitHub repository with full history | github.com/MuhammadJoyo/Escape-Protocol | All | PLANNED |
| D-02 | Proposal | `docs/Proposal.md` | M00 | PLANNED |
| D-03 | SRS | `docs/SRS.md` | M00 | PLANNED |
| D-04 | Architecture, data model, UML | `docs/Architecture.md`, `docs/Design.md` | M01 | PLANNED |
| D-05 | Working game | `src/main/java` | M02–M07 | PLANNED |
| D-06 | Socket leaderboard | `networking` package | M09 | PLANNED |
| D-07 | Test suite + testing report | `src/test/java`, `docs/Testing.md` | M11 | PLANNED |
| D-08 | Code-review log and debugging log | `docs/Code-Review-Log.md`, `docs/Development-Log.md` | M11 | PLANNED |
| D-09 | Concept mapping with evidence | `docs/SCD-Concept-Mapping.md`, this file | M12 | PLANNED |
| D-10 | README, user/developer guide, limitations | `README.md`, `docs/Limitations.md` (**M02:** starter `README.md` only) | M12 | IN PROGRESS |
| D-11 | Runnable JAR + launch instructions | `target/`, README (**M02:** placeholder JAR built and launched) | M13 | IN PROGRESS |
| D-12 | Live demo script, viva Q&A, per-member checklist | `docs/Demo-and-Viva.md` | M13 | PLANNED |

---

## 6. Planned Tests Catalogue

**Automated (JUnit 5)** — all `PLANNED`:
`ValidatorsTest`, `RoomOrderTest`, `InventoryTest`, `InventoryRepInvariantTest`, `ItemEqualityTest`, `ItemImmutabilityTest`, `GameEngineTest`, `GameStateTransitionTest`, `ProgressionTest`, `ResubmitTest`, `PenaltyRulesTest`, `ScoreCalculatorTest`, `HintServiceTest`, `PuzzleContractTest`, `OfficePuzzleTest`, `LibraryPuzzleTest`, `SequencePuzzleTest`, `SymbolPuzzleTest`, `FinalExitPuzzleTest`, `GameTimerTest`, `PauseResumeTest`, `SessionRaceTest`, `SaveLoadTest`, `CorruptSaveTest`, `TamperedSaveTest`, `LeaderboardPersistenceTest`, `LeaderboardTest`, `LeaderboardConcurrencyTest`, `RequestQueueTest`, `ProtocolTest`, `MalformedRequestTest`, `SocketIntegrationTest`, `MultiClientTest`, `ClientTimeoutTest`, `DisconnectTest`, `ServerShutdownTest`, `ExprStructureTest`, `ExprEvaluatorTest`, `RuleParserTest`, `RuleSyntaxErrorTest`, `UnlockRuleIntegrationTest`, `ClueTreeTest`, `GameStatsTest`, plus regression tests per logged defect.

**Executed in Module 02 (student-reported, all passed):** `MainTest` (3 tests) and `SmokeGrammarTest` (2 tests), 5 tests in total, 0 failures, 0 errors, 0 skipped. These two classes are not part of the planned catalogue above; they verify the build set-up only. Every test in the planned catalogue remains `PLANNED`.

**Module 03 additions (names added to the plan; all passed locally, student-reported):** `PlayerTest`, `RoomTest`. The other seven Module 03 test classes (`ValidatorsTest`, `RoomOrderTest`, `InventoryTest`, `InventoryRepInvariantTest`, `ItemEqualityTest`, `ItemImmutabilityTest`, `GameStateTransitionTest`) are already in the planned catalogue above. All remain `PLANNED` until the student reports passing results.

**Module 04 test classes (actual, not yet in the catalogue above):** `GameEngineTest` (29), `GameSessionTest` (40), `ProgressionTest` (11), `ScoreCalculatorTest` (12), `HintServiceTest` (13), `GameCatalogTest` (17), `UnlockContextTest` (5), `ResultTypesTest` (5), `PuzzleResultTest` (3); test-only helper `FakePuzzle`. All passed locally in the 199-test run (student-reported); Module 04 is committed as `c3740b9`.

**Module 05 test classes (delivered, not yet run by the student):** `OfficePuzzleTest` (11), `LibraryPuzzleTest` (14), `SequencePuzzleTest` (12), `FinalExitPuzzleTest` (17), `PuzzleContractTest` (13), `ResubmitTest` (7), `PenaltyRulesTest` (11), plus tests appended to `ValidatorsTest` (12) and `GameCatalogTest` (7): 104 new tests, expected total 303. `SymbolPuzzleTest` is not written because the symbol puzzle is deferred.

**Manual** — checklists `MT-UI-01…20`, `MT-TIM-01`, `MT-NET-01`, `MT-PLAY-01` (full playthrough), `MT-PLAY-02` (timeout), `MT-DEP-01` (JAR launch). Results are recorded only after execution.

---

## 7. UML / Design Evidence

| ID | Artifact | Location | Module | Status |
|---|---|---|---|---|
| U-01 | Use-case diagram | `docs/Architecture.md` | M01 | PLANNED |
| U-02 | Class diagram (domain + puzzles + networking) | `docs/Design.md` | M01 | PLANNED |
| U-03 | Activity diagram (gameplay flow) | `docs/Design.md` | M01 | PLANNED |
| U-04 | Sequence diagram: submit answer | `docs/Design.md` | M01 | PLANNED |
| U-05 | Sequence diagram: submit score to server | `docs/Design.md` | M01 | PLANNED |
| U-06 | State diagram: game lifecycle (draft in SRS §8) | `docs/Design.md` | M01 | PLANNED |
| U-07 | Component/architecture diagram | `docs/Architecture.md` | M01 | PLANNED |
| U-08 | ER diagram (draft in SRS §12) | `docs/Architecture.md` | M01 | PLANNED |
| U-09 | Concurrency and threading diagram | `docs/Architecture.md` | M01 | PLANNED |
| U-10 | ERL grammar and AST diagram | `docs/Design.md` | M01/M10 | PLANNED |

---

## 8. Source-Code Evidence (planned packages, base `com.escapeprotocol`)

| ID | Package | Purpose | Concepts | Status |
|---|---|---|---|---|
| S-01 | `app` | Entry points (`Main`, `ServerMain`) (**M02:** `Main` only; `ServerMain` pending) | C-02, C-24 | IN PROGRESS |
| S-02 | `game` | Engine, session, state, score, hints, unlock rules, stats | C-06, C-07, C-09, C-14, C-20, C-25 (**M04:** `GameEngine`, `GameSession`, `SessionSnapshot`, `GameCatalog`, `UnlockRule`, `UnlockContext`, `HardCodedUnlockRule`, `ScoreCalculator`, `HintService`, `GameListener`, result types, `GameState`; `GameStats` is M10) | IN PROGRESS |
| S-03 | `player` | Player name/profile | C-08, C-17 (**M03:** class is `Player`, not `PlayerName`) | VERIFIED |
| S-04 | `rooms` | Room ids and definitions | C-02 (**M03:** `RoomId`, `Room`) | VERIFIED |
| S-05 | `puzzles` | `Puzzle` interface + implementations (**M04:** `Puzzle` and `PuzzleResult` only; implementations are M05) | C-14 (**M05:** `OfficePuzzle`, `LibraryPuzzle`, `SequencePuzzle`, `FinalExitPuzzle` implemented; local run pending) | IN PROGRESS |
| S-06 | `inventory` | `Inventory`, `Item` | C-09, C-12, C-13, C-15 (**M03:** `Inventory`, `Item`, `ItemKind`) | VERIFIED |
| S-07 | `ui` | Swing screens | C-24 | PLANNED |
| S-08 | `storage` | Save/leaderboard files, repositories | C-14 | PLANNED |
| S-09 | `concurrency` | `GameTimer`, EDT helper | C-19, C-20 | PLANNED |
| S-10 | `networking` | Server, client, protocol, queue, leaderboard | C-12, C-20–C-23 | PLANNED |
| S-11 | `lang` | ERL grammar, AST, evaluator, loader | C-10, C-16–C-18, C-26 | PLANNED |
| S-12 | `clues` | Recursive clue tree | C-10, C-16 | PLANNED |
| S-13 | `util` | `Validators` (regex) | C-08, C-17 (**M03:** `Validators` with the player-name rule only) (**M05:** `Validators` also holds the puzzle input rules) | IN PROGRESS |

---

> **Module 02 note:** only the `app` package contains a class (`Main`). The other `com.escapeprotocol` packages exist as placeholders (`package-info.java`), and the `lang` package folder also holds the smoke-test grammar `Smoke.g4` (under `src/main/antlr4`). No package other than `app` is marked `IN PROGRESS`.

## 9. Presentation / Viva Evidence

| ID | Evidence | Owner | Status |
|---|---|---|---|
| V-01 | Live end-to-end demo (menu → victory; one timeout) | `[TBA]` | PLANNED |
| V-02 | Live two-client leaderboard demo with server up, then server down | `[TBA]` | PLANNED |
| V-03 | Demonstration of each of the 26 concepts: opening the file, explaining, running its test | each member for their areas | PLANNED |
| V-04 | Live edit of an ERL rule and observing changed unlock behaviour | `[TBA]` | PLANNED |
| V-05 | Git history walkthrough showing each member's commits | all | PLANNED |
| V-06 | Per-member contribution statement + ability to explain any class | each member | PLANNED |
| V-07 | Viva Q&A preparation sheet (concept questions, "why did you design it this way") | all | PLANNED |

---

## 10. Module 02 Update Record (2026-11-22)

Evidence is recorded in `docs/Development-Log.md` §5a and §7a (student-reported results).

| Row | Previous status | New status | Why |
|---|---|---|---|
| AR-04, AR-08, AR-10 | PLANNED | IN PROGRESS | Maven project, JUnit run and JAR packaging exist; the game itself and comprehensive tests do not |
| C-01 Static Checking | PLANNED | IN PROGRESS | `-Xlint:all` and `--release 21` configured; lint review and demo pending |
| C-02 Basic Java | PLANNED | IN PROGRESS | `Main` class only |
| C-03 Testing | PLANNED | IN PROGRESS | 5 passing tests; no game-logic tests |
| C-18 Parser Generators | PLANNED | IN PROGRESS | ANTLR generation verified with a smoke grammar; real grammar pending |
| C-24 Graphical User Interfaces | PLANNED | IN PROGRESS | Basic window only |
| FR-LL-02 | PLANNED | IN PROGRESS | Generation verified for `Smoke.g4`, not `Escape.g4` |
| NFR-MT-03, NFR-TS-01, NFR-DP-01 | PLANNED | IN PROGRESS | Build, test and package succeeded once; they must be repeated through M11/M13 |
| D-10, D-11, S-01 | PLANNED | IN PROGRESS | Starter README, placeholder JAR, `Main` only |

**No row is `VERIFIED` after Module 02.** The other 21 concepts, all other FR/NFR/CON/ERR/SEC/DEP rows, all deliverables not listed above, all planned tests and all Module 03+ work remain `PLANNED`. C-05 (Version Control) and SEC-03 are unchanged because Module 02 is not yet committed.

---

## 11. Module 03 Update Record (2026-11-22; completed, see §12)

Source and tests for the domain model and the Inventory ADT have been delivered to the student, but the student has **not yet run them**. Therefore **every row below stays `PLANNED`**; only a note was added to each. Rows are changed to `IN PROGRESS` or `VERIFIED` only after the student reports the test results (recorded in `docs/Development-Log.md`).

| Row | Note added | Status |
|---|---|---|
| C-08, C-09, C-12, C-13, C-15 | Classes and tests written, not yet run | PLANNED |
| FR-MN-02, FR-GM-02, FR-GM-04, FR-IV-01 | Classes and tests written, not yet run | PLANNED |
| S-02, S-03, S-04, S-06, S-13 | Which classes now exist | PLANNED |

**Naming resolution:** the player class is `Player` (not `PlayerName`), following the committed `Architecture.md` and `Design.md`. The tests catalogue (§6) gains `PlayerTest` and `RoomTest`.

> **Update (2026-11-23):** the student reported that Module 03 passed (64 tests, committed and pushed). The notes in the rows above were updated; the resulting statuses are in §12.

---

## 12. Module 04 Update Record (2026-11-23)

Evidence is in `docs/Development-Log.md` §5c and §7a. Module 04 Parts 1–3 passed locally (170 tests, student-reported). Part 4 (`GameEngineTest`, 29 tests) is delivered and **not yet run**, so rows that depend on the engine stay `IN PROGRESS`. No row is marked `VERIFIED` from documentation alone.

| Row | Previous | New | Evidence |
|---|---|---|---|
| FR-GM-04 | PLANNED | VERIFIED | `GameStateTransitionTest`, `GameSessionTest` |
| FR-IV-05, FR-TS-04, FR-TS-05 | PLANNED | VERIFIED | `HintServiceTest`, `ScoreCalculatorTest`, `GameSessionTest` |
| FR-GM-02, FR-IV-01 (Module 03 rows) | PLANNED | VERIFIED | `RoomOrderTest`, `RoomTest`, `InventoryTest`, `ItemEqualityTest` (64-test run) and `GameSessionTest` |
| FR-GM-01, FR-GM-03, FR-GM-05, FR-GM-06, FR-GM-07, FR-PZ-11, FR-IV-02, FR-IV-04, FR-TS-08, CON-01, CON-02 | PLANNED | IN PROGRESS | session-level tests passed; the engine path, timer or real puzzles are still pending |
| FR-MN-02 | PLANNED | IN PROGRESS | `Validators`, `Player` tested; the name-entry screen is M06 |
| C-03, C-05, C-06, C-07, C-08, C-09, C-12, C-13, C-14, C-15, C-19, C-20 | mixed | IN PROGRESS | see the notes in §2; no concept is `VERIFIED` |
| AR-04, AR-08 | IN PROGRESS | IN PROGRESS | notes updated |
| S-03, S-04, S-06 | PLANNED | VERIFIED | `Player`, `RoomId`/`Room`, `Inventory`/`Item`/`ItemKind` tested |
| S-02, S-05, S-13 | PLANNED | IN PROGRESS | `game` engine classes, `Puzzle` contract, `Validators` (name rule only) |

**Unchanged:** C-04 (no review log entries yet), C-10, C-11, C-16, C-17, C-21 to C-23, C-25, C-26, and everything for Modules 05 to 13 stay `PLANNED`. Concepts are never marked complete from Module 04 alone; the 26-concept assignment is not complete.

**Open conflicts recorded:** the Design lists no `start()` on `GameEngine` (DEC-13); the unlock-rule RI clause of `GameSession` must be revisited in Module 10 (OI-06).

> **Update (2026-11-23):** the student reported that Module 04 is complete (199 tests passed, `mvn clean package` BUILD SUCCESS, committed as `c3740b9`). The "pending" notes above were updated; FR-GM-03, FR-PZ-11 and FR-IV-02 are now `VERIFIED` because `GameEngineTest` passed.

---

## 13. Module 05 Update Record (2026-11-23)

Module 05 was delivered in one archive. Nothing from it has been run by the student yet (expected total 303 tests), so **no Module 05 row is `VERIFIED`**. A row becomes `VERIFIED` only after the student reports passing Maven results, which are then recorded in `docs/Development-Log.md`.

| Row | Previous | New | Evidence delivered (not yet run) |
|---|---|---|---|
| FR-PZ-01 | PLANNED | IN PROGRESS | `PuzzleContractTest` |
| FR-PZ-02, FR-PZ-03 | PLANNED | IN PROGRESS | `OfficePuzzleTest`, `ResubmitTest` |
| FR-PZ-04, FR-PZ-05 | PLANNED | IN PROGRESS | `LibraryPuzzleTest`, `ResubmitTest` |
| FR-PZ-06, FR-PZ-08 | PLANNED | IN PROGRESS | `SequencePuzzleTest`, `GameCatalogTest`, `ResubmitTest` |
| FR-PZ-09, FR-PZ-10 | PLANNED | IN PROGRESS | `FinalExitPuzzleTest`, `PenaltyRulesTest` |
| FR-PZ-12 | PLANNED | IN PROGRESS | `ResubmitTest` |
| FR-PZ-13 | PLANNED | IN PROGRESS | `PenaltyRulesTest` |
| FR-IV-04 | IN PROGRESS | IN PROGRESS | `PuzzleContractTest`, `PenaltyRulesTest` |
| FR-LL-07 | PLANNED | IN PROGRESS | `ValidatorsTest` (appended tests) |
| **FR-PZ-07** | PLANNED | **PLANNED (P2, deferred)** | none: SymbolPuzzle is not implemented (DEC-15); the SRS text is unchanged |
| C-17 | PLANNED | IN PROGRESS | `Validators`, `ValidatorsTest` |
| C-03, C-08, C-09, C-14 | IN PROGRESS | IN PROGRESS | notes updated |
| FR-GM-03, FR-PZ-11, FR-IV-02 | IN PROGRESS | VERIFIED | Module 04 evidence (199 tests, student-reported) |
| AR-04, AR-08, S-05, S-13 | IN PROGRESS | IN PROGRESS | notes updated |

**Unchanged:** every other row, all concepts not listed, and all work for Modules 06 to 13. No concept is `VERIFIED`; the 26-concept assignment is not complete. No instructor approval is recorded for the deferral of the symbol puzzle or for any other item.
