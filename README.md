# Escape Protocol — The Last Exit

A Java desktop escape-room puzzle game. The player is trapped in a mysterious
facility and must solve puzzles, collect clues, unlock rooms (Office, Library,
Laboratory, Final Exit) and escape before the countdown reaches zero.

Built for the **Software Construction and Development (SCD)** course, SE V.

## Technology Stack

- Java (compiled for Java 21 compatibility)
- Java Swing (desktop GUI)
- Maven (build and dependency management)
- JUnit 5 (unit testing)
- ANTLR 4 (parser generator for the planned Escape Rule Language)
- Git and GitHub (version control)

## Prerequisites

- JDK 21 or newer (developed with Eclipse Temurin 27; code targets Java 21)
- Apache Maven 3.9 or newer
- Git

Check your setup:

```cmd
java -version
javac -version
mvn -version
```

## Build and Test

```cmd
mvn clean test
mvn clean package
```

## Run

With Maven:

```cmd
mvn compile exec:java
```

With the packaged JAR:

```cmd
java -jar target\escape-protocol.jar
```

## Project Structure

Base package: `com.escapeprotocol` with sub-packages `app`, `game`, `player`,
`rooms`, `puzzles`, `inventory`, `ui`, `storage`, `concurrency`, `networking`,
`lang`, `clues`, `util`. See `docs/Architecture.md` and `docs/Design.md`.

## Current Status

**Module 02 — Maven project foundation.** The project builds, tests run, ANTLR
generation is configured, and a basic Swing window opens. Gameplay is not
implemented yet.

## Documentation

See the `docs/` folder (Proposal, SRS, Requirements Traceability,
Development Plan, Architecture, Design, Development Log).