# MAS Platform

A small **multi-agent system (MAS)** written in Java for the *Foundation of Agentic AI* course.

The repo has two layers:

1. **`platform`** — a reusable runtime: agents, environment, actions, and mailbox messaging.
2. **`diningphilosophers`** — a simulation of Dijkstra’s dining philosophers problem built on that platform.

Agents run as threads. Each cycle they perceive, decide, then act. They can also send speech-act style messages through the environment instead of grabbing shared resources blindly.

---

## What this repo is for

The classic dining philosophers setup is five people around a table and five forks. Each person needs **both** neighbouring forks to eat. If everyone picks up the left fork and waits for the right one, the system deadlocks.

This project models that as a MAS:

- The **table** is the environment (forks plus seated philosophers).
- Each **philosopher** is an autonomous agent with hunger, fork ownership, and a simple protocol for asking a neighbour for a fork.
- Coordination is done with **messages** (`can I please have the fork?`, `yes of course!`, `thank you!`), not only with mutexes.

The platform itself is domain-agnostic. A new scenario would subclass `Agent` and `Environment` the same way.

---

## Project structure

```
mas-platform/
├── README.md
└── src/
    ├── platform/                 # generic MAS runtime
    │   ├── Action.java           # something an agent can execute in the environment
    │   ├── Agent.java            # thread + perceive / deliberate / act loop + mailbox
    │   ├── Environment.java      # agent registry and message delivery
    │   ├── Message.java          # sender, receivers, performative, optional content
    │   └── Runtime.java          # start / stop a set of agents
    └── diningphilosophers/       # dining philosophers scenario
        ├── Main.java             # five named philosophers at a table of 5
        ├── DPAgent.java          # philosopher behaviour and fork protocol
        └── DPEnvironment.java    # forks, think counter, lookup of neighbours
```

There is no Maven/Gradle build file. Compile the `src` tree with `javac`.

---

## Architecture

```
Runtime
  └── starts each Agent as a Thread
        │
        │  loop: perceive → deliberate → act → sleep 300ms
        │
        ├── Environment (shared)
        │     • register agents
        │     • deliver messages to receivers’ mailboxes
        │     • domain state (in DP: forks)
        │
        └── Mailbox (per agent)
              incoming Message objects drained during deliberate()
```

### Agent cycle

Every agent extends `Thread` and runs this loop (`Agent.run`):

1. **Perceive** — update internal state from the last action’s boolean result (for example: did `takeLeft` succeed?).
2. **Deliberate** — read the mailbox, run decision logic, return an `Action`.
3. **Act** — `action.act(env)` mutates the environment.
4. **Sleep** — `DELAY = 300` ms so the console is readable.

`Runtime` holds the list of agents, starts them together, and can ask them to stop.

### Environment

`Environment` keeps agents in a name map and forwards each `Message` to every listed receiver via `receiveMessage`. Subclasses add the world state. `DPEnvironment` stores:

- one boolean per fork (`true` = free)
- philosophers by seat index
- a global thought counter

Fork `take` / `drop` are `synchronized` so two threads cannot pick the same fork at once.

### Messages

`Message` is a lightweight speech act:

| Field | Role |
| --- | --- |
| `sender` | the `Agent` who created the message |
| `performative` | the intent string (the protocol’s vocabulary) |
| `receivers` | who should get a copy |
| `content` | optional extra text |

Sending always goes through the environment: `agent.sendMessage(message)` → `env.sendMessage(message)` → each receiver’s mailbox.

---

## Dining philosophers scenario

`Main` builds a table of **5** philosophers:

| Seat | Name | Right neighbour |
| ---: | --- | --- |
| 0 | Kashir | Umair |
| 1 | Umair | Farrukh |
| 2 | Farrukh | Asad |
| 3 | Asad | Ahad |
| 4 | Ahad | Kashir |

Fork *i* sits between philosopher *i* and philosopher *(i+1) mod 5*. For agent `position`:

- **left fork** = `position`
- **right fork** = `rightPos`

### Internal state (`DPAgent`)

| Field | Meaning |
| --- | --- |
| `hunger` | rises while thinking; eat when it reaches `maxHunger` (5) |
| `left` / `right` | whether this agent currently holds that fork |
| `eating` | currently consuming a meal |
| `waitingAgree` | asked the right neighbour for a fork; waiting for `yes of course!` |
| `waitingThanks` | agreed to give a fork; waiting for `thank you!` |

### Actions

| Action | Effect |
| --- | --- |
| `think` | increment the table’s idea counter and print it |
| `takeLeft` / `takeRight` | try to pick a fork; success is perceived next cycle |
| `dropLeft` / `dropRight` | put the fork back |
| `eat` | print that the agent is eating; hunger decreases |
| `doNothing` | idle while waiting on the message protocol |
| `wait` | defined but unused in the current decision tree |

### Decision policy

Rough order in `deliberate()`:

1. Handle the **first** mailbox message (see protocol below).
2. If waiting for agreement or thanks → `doNothing`.
3. If eating and still hungry → `eat`.
4. If eating is finished → drop left, then right.
5. If not hungry enough → `think` and increase hunger.
6. If hungry and no left fork → `takeLeft`.
7. If hungry, has left, no right → **ask the right neighbour**, then `doNothing`.
8. If both forks are held → start eating.

### Fork request protocol

Used when an agent has the left fork and needs the right one. Messages go to the **right-hand neighbour** (the other claimant of that fork).

```
Requester                         Neighbour
    |                                  |
    |  "can I please have the fork?"   |
    |--------------------------------->|
    |                                  |  may drop left fork
    |       "yes of course!"           |
    |<---------------------------------|
    |                                  |
    |         "thank you!"             |
    |--------------------------------->|
    |  takeRight                       |  waitingThanks := false
```

- After asking, the requester sets `waitingAgree` and idles until `"yes of course!"`.
- After agreeing, the neighbour sets `waitingThanks` and idles until `"thank you!"`.
- If the neighbour was also waiting for *their* right fork, they cancel that wait and thank the requester as well (so two waiting agents can unstick each other).

Performative strings must match exactly. If they do not, agents stay on `doNothing` forever.

---

## Requirements

- **JDK 8+** (plain Java, no extra libraries)
- A terminal

---

## Build and run

From the repository root:

```bash
mkdir -p out
javac -d out src/platform/*.java src/diningphilosophers/*.java
java -cp out diningphilosophers.Main
```

The simulation runs until you stop it (`Ctrl+C`). Optional timed shutdown is commented in `Main.java`.

### Example console output

You should see a mix of:

- `Kashir produced idea # 3` — thinking
- `Left Picked` — a left fork was taken
- `Doing Nothing` — waiting on the neighbour protocol
- `Umair is eating.` — both forks held
- `Fork dropped` — a fork returned to the table

---

## How to extend it

**New scenario on the same platform**

1. Subclass `Environment` with your world state.
2. Subclass `Agent` and implement `perceive` and `deliberate`.
3. Write a `Main` that constructs the environment, agents, and a `Runtime`.

**New agent behaviour**

Add `Action` instances inside the agent (as `DPAgent` does) and return them from `deliberate()`. Keep environment mutations inside `act()`, and keep beliefs (hunger, forks, protocol flags) on the agent.

**New messages**

Create a `Message` with a performative, `addReceiver(...)`, optionally `addContent(...)`, then `sendMessage(...)`. Handle incoming mail in `deliberate()` **before** falling through to idle/wait flags.

---

## Course context

This codebase is a student MAS platform: agents as threads, a shared environment, and explicit messaging. The dining philosophers instance is the first concrete society running on it — useful for talking about autonomy, resource conflict, deadlock, and communication as coordination.
