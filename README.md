# Escape the Maze

A JavaFX maze game with procedurally generated levels, hazards, scoring, and multiple generation algorithms.

## Features

- Four difficulty levels with different maze sizes and generators
- DFS, Prim's, Kruskal's, and binary-tree maze generation
- Camera-following tile renderer with keyboard movement
- Gold, spikes, freeze, fog, health, timer, and high-score systems
- Automated tests for maze connectivity and generation rules

## Requirements

- JDK 22 or newer
- Maven 3.9 or newer

JavaFX is downloaded automatically by Maven from the dependencies in `pom.xml`.

## Run

From the project directory:

```bash
mvn javafx:run
```

On Windows PowerShell, first move into the folder containing `pom.xml`:

```powershell
cd C:\path\to\escape-the-maze
mvn javafx:run
```

## Test

```bash
mvn test
```

## Controls

- `WASD` to move
- `Space` to jump over hazards

Scores are stored locally in `score.txt`.
