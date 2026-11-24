> 🇫🇷 [Version française](README_fr.md)

# Chrono-Tactics — CodinGame Multiplayer SDK Project

A complete multiplayer algorithmic bot-programming puzzle built with the **CodinGame Game Engine (`4.7.7`)**, structured into **3 progressive divisions** (**Wood 3 → Wood 2 → Wood 1**) using assets from `codingame-sdk-assets`.

---

## 1. Quick Commands

### Launch Local Web Viewer (`http://localhost:8888/test.html`)
```bash
cd chrono-tactics-sdk
mvn test-compile exec:java -Dexec.mainClass="Main" -Dexec.classpathScope="test"
```
Then open **[http://localhost:8888/test.html](http://localhost:8888/test.html)** in your browser to inspect interactive replays (`TooltipModule`, grid debug toggle `ToggleModule`, `EndScreenModule`, and the **Export** button to generate `.zip` uploads for [CodinGame Contribute](https://www.codingame.com/contribute/)).

### Run Batch Simulation (Headless)
```bash
cd chrono-tactics-sdk
mvn test-compile exec:java -Dexec.mainClass="Main" -Dexec.classpathScope="test" -Dexec.args="--simulate"
```

---

## 2. Project Structure

* [`pom.xml`](pom.xml): Maven configuration with `core`, `module-entities`, `module-tooltip`, `module-endscreen`, `module-toggle`, and `runner` (`4.7.7`).
* [`config/config.ini`](config/config.ini): Game settings (`type=multi`, `min_players=2`, `max_players=2`).
* [`config/stub.txt`](config/stub.txt): Multi-language I/O stub generator.
* [`config/statement_en.html.tpl`](config/statement_en.html.tpl) & [`config/statement_fr.html.tpl`](config/statement_fr.html.tpl): League-gated HTML statements per division.
* `config/level1/Boss.java`, `config/level2/Boss.java`, `config/level3/Boss.java`: Progressive league Boss bots *(kept local-only and excluded via `.gitignore`)*.
* [`src/main/java/com/codingame/game/Referee.java`](src/main/java/com/codingame/game/Referee.java): Full referee, simultaneous turn resolution, multi-phase animations (`commitWorldState`), Chrono-Storm warning/impact overlays, tooltips, and early-victory detection.
* [`src/main/java/com/codingame/game/Grid.java`](src/main/java/com/codingame/game/Grid.java): Rotationally symmetric 11 × 11 procedural arena generator and BFS pathfinding.
* [`src/test/java/Agent1.java`](src/test/java/Agent1.java): Advanced tactical test AI (BFS, movement anticipation, fragile tile avoidance, lethal `PUSH` and `BOMB`).

---

## 3. Links

* **CodinGame Contribution**: https://www.codingame.com/contribute/view/15861375695065563529e288291b702e8df659
* **Play in CodinGame IDE**: https://www.codingame.com/ide/demo/160565706ec1b3064e2be42d9cc50401b0ae417
* **6-Puzzle Showcase Hub (GitHub Pages)**: https://cgphilrg.github.io/codingame-sdk-puzzles/
