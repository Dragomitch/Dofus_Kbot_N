# Agent Coordination Guide
## How to Execute the Implementation Book with Multiple Agents

**Version:** 1.0
**Date:** 2025-11-08
**Purpose:** Operational guide for launching and coordinating multiple agents

---

## Quick Start

### Step 1: Review the Implementation Book

Read `IMPLEMENTATION_BOOK.md` to understand:
- **Agent Profiles** (A1-A11) - Who does what
- **Task Catalog** - What needs to be done
- **Dependencies** - What blocks what
- **Parallel Execution Plan** - How to maximize parallelism

### Step 2: Identify Your Starting Point

**If starting from scratch:**
```
Week 1, Day 1: Launch A1, A2, A11 simultaneously
```

**If project structure exists:**
```
Skip to Week 2: Launch A4a-A4j for packet implementation
```

### Step 3: Launch Agents

Use the agent launch commands below.

---

## Agent Launch Commands

### Week 1: Foundation

#### Agent A1 - Infrastructure Architect

**Launch Command:**
```
Launch Agent A1 with task: T-001, T-002, T-003, T-004

Context:
- Create Maven multi-module project for Dofus packet decoder
- Configure Spring Boot modules
- Set up Docker Compose
- Refer to IMPLEMENTATION_BOOK.md tasks T-001 through T-004
- Deliverables defined in Implementation Book
```

**Expected Duration:** 1-2 days

**Completion Criteria:**
- [ ] `mvn clean install` succeeds
- [ ] `docker-compose up` starts all services
- [ ] Spring Boot application starts successfully

---

#### Agent A2 - Database Architect (PARALLEL)

**Launch Command:**
```
Launch Agent A2 with tasks: T-200, T-201, T-202, T-203

Context:
- Design database schema for Dofus packet decoder
- Create JPA entities
- Implement repository interfaces
- Set up Flyway migrations
- Refer to IMPLEMENTATION_BOOK.md tasks T-200 through T-203
- Can work completely independently in parallel with A1
```

**Expected Duration:** 1-2 days

**Completion Criteria:**
- [ ] ER diagram created
- [ ] JPA entities compile
- [ ] Repository interfaces defined
- [ ] Flyway migrations run successfully

---

#### Agent A11 - Documentation Writer (PARALLEL)

**Launch Command:**
```
Launch Agent A11 with task: Write architecture documentation

Context:
- Write architecture documentation based on PRD_JAVA_DOFUS_PACKET_DECODER.md
- Create README.md for project
- Document module structure
- Create diagrams (architecture, deployment)
- Can work completely independently
```

---

### Week 1: Network Layer (After A1 Completes)

#### Agent A3 - Network Engineer

**Launch Command:**
```
Launch Agent A3 with tasks: T-005, T-006

Prerequisites:
- Wait for A1 to complete T-002 (Spring Boot configuration)
- Verify project structure exists

Context:
- Implement MITM proxy using Netty
- Create packet capture mechanism
- Refer to IMPLEMENTATION_BOOK.md tasks T-005, T-006
- This is on the critical path - blocks A4
```

**Expected Duration:** 1-2 days

**Completion Criteria:**
- [ ] Netty proxy accepts connections
- [ ] Packets captured and queued
- [ ] Integration test with mock client passes

---

### Week 1-2: Protocol Layer (After A3 Completes T-006)

#### Agent A4 - Protocol Specialist

**Launch Command:**
```
Launch Agent A4 with tasks: T-007, T-008

Prerequisites:
- Wait for A3 to complete T-006 (packet capture)
- Verify raw packets can be captured

Context:
- Define packet type hierarchy
- Implement PacketDecoder service
- Implement PacketEncoder service
- Create packet registry
- Refer to IMPLEMENTATION_BOOK.md tasks T-007, T-008
- This is on the critical path - blocks packet implementations
```

**Expected Duration:** 1 day

**Completion Criteria:**
- [ ] Packet base class defined
- [ ] PacketDecoder service functional
- [ ] Registry auto-populates from classpath
- [ ] Unit tests pass

---

### Week 2: MEGA PARALLEL SPRINT - Packet Implementations

**🎯 This is where we get MAXIMUM parallelization!**

Launch **10 agents simultaneously** to implement 100 packets in 4 hours instead of 40 hours.

#### Agent A4a - Authentication Packets

**Launch Command:**
```
Launch Agent A4a with tasks: T-009 to T-018 (10 authentication packets)

Prerequisites:
- A4 completed T-008 (PacketDecoder service exists)

Context:
- Implement authentication packet types: AA, AV, AT, AX, AS, Af, AH, AI, AK, AL
- Each packet must extend Packet base class
- Each packet must have @PacketId annotation
- Implement decode() and encode() methods
- Write unit tests for each packet
- Refer to IMPLEMENTATION_BOOK.md task T-009 to T-018
- Example implementation in PRD Appendix B

Deliverables (per packet):
- Packet class with @PacketId annotation
- decode() implementation
- encode() implementation
- Unit test with valid data
- Unit test with malformed data
- Javadoc with example
```

---

#### Agent A4b - Map Packets

**Launch Command:**
```
Launch Agent A4b with tasks: T-019 to T-033 (15 map packets)

Prerequisites:
- A4 completed T-008 (PacketDecoder service exists)

Context:
- Implement map packet types: GM, GDM, GDF, GC, GP, GI, GK, GV, etc.
- Focus on map data, map movement, map entities
- These packets are CRITICAL for navigation
- Refer to IMPLEMENTATION_BOOK.md tasks T-019 to T-033

Key packets to prioritize:
- GDM (GameDataMapPacket) - CRITICAL
- GP (GamePositionPacket) - CRITICAL
- GC (MapCreationPacket) - HIGH
```

---

#### Agent A4c - Combat Packets

**Launch Command:**
```
Launch Agent A4c with tasks: T-034 to T-048 (15 combat packets)

Prerequisites:
- A4 completed T-008

Context:
- Implement combat packet types: GS, GT, GE, GTS, GTF, GTR, GA (actions), etc.
- These packets are CRITICAL for combat system
- Refer to IMPLEMENTATION_BOOK.md tasks T-034 to T-048

Key packets to prioritize:
- GTS (GameTurnStartPacket) - CRITICAL
- GS (GameFightStartPacket) - CRITICAL
- GE (GameFightEndPacket) - HIGH
```

---

#### Agent A4d - Inventory Packets

**Launch Command:**
```
Launch Agent A4d with tasks: T-049 to T-058 (10 inventory packets)

Context:
- Implement inventory packet types: OT, OA, OR, OM, OQ, etc.
- Focus on item management
- Refer to IMPLEMENTATION_BOOK.md tasks T-049 to T-058
```

---

#### Agent A4e - Dialog Packets

**Launch Command:**
```
Launch Agent A4e with tasks: T-059 to T-068 (10 dialog packets)

Context:
- Implement dialog/NPC packet types: DC, DQ, DR, DV, etc.
- Focus on NPC interactions
- Refer to IMPLEMENTATION_BOOK.md tasks T-059 to T-068
```

---

#### Agent A4f - Stats & Spells Packets

**Launch Command:**
```
Launch Agent A4f with tasks: T-069 to T-078 (10 stats packets)

Context:
- Implement stats packet types: As, SL, SM, SK, SB, etc.
- Focus on character stats and spell management
- Refer to IMPLEMENTATION_BOOK.md tasks T-069 to T-078
```

---

#### Agent A4g - Chat Packets

**Launch Command:**
```
Launch Agent A4g with tasks: T-079 to T-088 (10 chat packets)

Context:
- Implement chat packet types: cC, cM, cMK, etc.
- Focus on messaging system
- Refer to IMPLEMENTATION_BOOK.md tasks T-079 to T-088
```

---

#### Agent A4h - Exchange Packets

**Launch Command:**
```
Launch Agent A4h with tasks: T-089 to T-098 (10 exchange packets)

Context:
- Implement exchange packet types: EA, EC, EK, EL, ER, EV, etc.
- Focus on trading system
- Refer to IMPLEMENTATION_BOOK.md tasks T-089 to T-098
```

---

#### Agent A4i - Movement Packets

**Launch Command:**
```
Launch Agent A4i with tasks: T-099 to T-104 (10 movement packets)

Context:
- Implement movement packet types: GA (movement), GAF, GK, etc.
- Focus on character movement
- Refer to IMPLEMENTATION_BOOK.md tasks T-099 to T-104
```

---

#### Agent A4j - Miscellaneous Packets

**Launch Command:**
```
Launch Agent A4j with tasks: T-105 to T-108 (remaining packets)

Context:
- Implement remaining packet types
- Focus on edge cases and less common packets
- Refer to IMPLEMENTATION_BOOK.md tasks T-105 to T-108
```

---

### Week 2-3: Business Logic (After Critical Packets)

#### Agent A5 - Game Logic Developer

**Launch Command:**
```
Launch Agent A5 with tasks: T-300 to T-305

Prerequisites:
- A4 completed T-008 (decoder exists)
- A4b completed critical map packets (GDM, GP)
- A4c completed critical combat packets (GTS, GS)
- Can start with mocked packets if needed

Context:
- Implement GameStateService
- Create domain models (Player, Map, Combat states)
- Build event-driven state updates
- Refer to IMPLEMENTATION_BOOK.md tasks T-300 to T-305
- This BLOCKS navigation and combat modules
```

**Expected Duration:** 2 days

---

#### Agent A6 - Navigation Specialist (PARALLEL with A5)

**Launch Command:**
```
Launch Agent A6 with tasks: T-400 to T-405

Prerequisites:
- A5 started T-300 (can use mocked GameStateService interface)

Context:
- Implement A* pathfinding algorithm
- Create map graph representation
- Build cell walkability detection
- Implement multi-map navigation
- Can work with mocked game state initially
- Refer to IMPLEMENTATION_BOOK.md tasks T-400 to T-405
```

**Expected Duration:** 2 days

---

#### Agent A7 - Combat System Developer (PARALLEL with A5, A6)

**Launch Command:**
```
Launch Agent A7 with tasks: T-500 to T-505

Prerequisites:
- A5 started T-300 (can use mocked GameStateService)

Context:
- Implement combat state machine
- Create spell system
- Build target selection algorithms
- Implement combat strategies (Strategy pattern)
- Can work with mocked game state initially
- Refer to IMPLEMENTATION_BOOK.md tasks T-500 to T-505
```

**Expected Duration:** 2-3 days

---

### Week 3-4: API Layer (PARALLEL)

#### Agent A8 - API Developer

**Launch Command:**
```
Launch Agent A8 with tasks: T-600 to T-610

Prerequisites:
- Service interfaces defined (can mock implementations)

Context:
- Design REST API endpoints
- Implement controllers and DTOs
- Set up WebSocket endpoints
- Create OpenAPI/Swagger documentation
- Use mocked services for initial development
- Refer to IMPLEMENTATION_BOOK.md tasks T-600 to T-610
```

**Expected Duration:** 2 days

---

### Week 4-6: Frontend (PARALLEL)

#### Agent A9 - Frontend Developer

**Launch Command:**
```
Launch Agent A9 with tasks: T-700 to T-750

Prerequisites:
- API endpoints defined (can use mocked API)

Context:
- Create React/Vue application
- Build packet viewer component
- Implement map visualizer
- Create configuration UI
- Integrate WebSocket for real-time updates
- Can work completely independently with mocked backend
- Refer to IMPLEMENTATION_BOOK.md tasks T-700 to T-750
```

**Expected Duration:** 3-4 days

---

### Continuous: Testing (SHADOWS ALL WORK)

#### Agent A10 - Testing & Quality

**Launch Command:**
```
Launch Agent A10 with continuous testing tasks

Context:
- Shadow all development work
- Write unit tests for each module as it's developed
- Create integration tests
- Set up Testcontainers
- Measure code coverage
- Report quality metrics
- Refer to IMPLEMENTATION_BOOK.md tasks T-800 to T-850

Follow this priority:
1. Test network layer (after A3)
2. Test packet decoder (after A4)
3. Test packet types (after A4a-A4j)
4. Test game state (after A5)
5. Integration tests (after A5, A6, A7)
6. E2E tests (final integration)
```

---

## Coordination Mechanisms

### 1. Task Status Board

Create a shared task board (GitHub Projects, Trello, or simple spreadsheet):

```
| Task ID | Agent | Status        | Progress | Blockers | ETA  |
|---------|-------|---------------|----------|----------|------|
| T-001   | A1    | ✅ COMPLETED  | 100%     | None     | -    |
| T-002   | A1    | ✅ COMPLETED  | 100%     | None     | -    |
| T-005   | A3    | 🟡 IN PROGRESS| 60%      | None     | 2h   |
| T-007   | A4    | ⚪ WAITING    | 0%       | T-005    | -    |
| T-200   | A2    | ✅ COMPLETED  | 100%     | None     | -    |
```

### 2. Dependency Checking

Before launching an agent, verify prerequisites:

```bash
# Check if T-002 is complete before launching A3
if task_completed("T-002"); then
    launch_agent("A3", "T-005, T-006")
else
    echo "Waiting for T-002 to complete..."
fi
```

### 3. Communication Protocol

**Agent Check-in Format:**
```
Agent: A3
Tasks: T-005, T-006
Status: IN PROGRESS
Progress:
  - T-005: 80% (Netty proxy implemented, testing in progress)
  - T-006: 20% (interface defined, implementation next)
Blockers: None
Next: Complete T-005 testing, then start T-006 implementation
ETA: T-005 in 1 hour, T-006 in 3 hours
```

### 4. Merge Coordination

**Before merging:**
1. Ensure CI passes
2. Ensure tests pass
3. Ensure no conflicts with `develop` branch
4. Get approval from at least 1 other agent (peer review)

**Merge order:**
- Infrastructure (A1) → Network (A3) → Protocol (A4) → Packets (A4a-j) → Business Logic (A5, A6, A7) → API (A8) → Frontend (A9)

---

## Parallel Execution Strategies

### Strategy 1: Wave Deployment

Launch agents in waves based on dependencies:

**Wave 1 (Day 1):**
```
A1 (Infrastructure) - CRITICAL PATH
A2 (Database) - PARALLEL
A11 (Docs) - PARALLEL
```

**Wave 2 (Day 2-3):**
```
A3 (Network) - CRITICAL PATH (waits for A1)
A4 (Protocol) - CRITICAL PATH (waits for A3)
```

**Wave 3 (Day 4-5):**
```
A4a, A4b, A4c, A4d, A4e, A4f, A4g, A4h, A4i, A4j (10 agents in parallel!)
A10 (Testing) - PARALLEL
```

**Wave 4 (Week 2-3):**
```
A5 (Game State) - CRITICAL PATH
A6 (Navigation) - PARALLEL (with mocks)
A7 (Combat) - PARALLEL (with mocks)
A8 (API) - PARALLEL (with mocks)
```

**Wave 5 (Week 3-4):**
```
A9 (Frontend) - PARALLEL
A10 (Integration testing)
```

---

### Strategy 2: Mock-First Development

Allow agents to start early with mocked dependencies:

**Example:**
```java
// A6 can start implementing pathfinding while A5 is building GameStateService
// by using a mocked interface:

@MockBean
private GameStateService gameStateService;

@Test
void testPathfinding() {
    // Mock the game state
    when(gameStateService.getCurrentMap()).thenReturn(mockMap);

    // Test pathfinding logic
    Path path = pathfindingService.findPath(start, end);
    assertNotNull(path);
}
```

---

### Strategy 3: Interface-First Design

Define interfaces early, implement later:

**Example:**
```java
// Week 1: A1 defines this interface
public interface PacketDecoder {
    Packet decode(byte[] rawData) throws PacketDecodingException;
}

// Week 2: A4 implements it
@Service
public class PacketDecoderImpl implements PacketDecoder {
    // Implementation
}

// Meanwhile, Week 1: A5 can code against the interface
@Service
public class GameStateService {
    private final PacketDecoder packetDecoder; // Uses interface

    public void processPacket(byte[] rawData) {
        Packet packet = packetDecoder.decode(rawData);
        // ...
    }
}
```

---

## Troubleshooting

### Problem: Agent Blocked

**Symptom:**
```
Agent A4 cannot start because T-006 is not complete
```

**Solution:**
1. Check if blocker (T-006) is truly needed or if mocking is possible
2. If blocking is necessary, reassign agent to different task
3. If no other tasks, agent waits (or helps with testing/docs)

---

### Problem: Merge Conflict

**Symptom:**
```
Agent A4b's packet implementations conflict with A4c's
```

**Solution:**
1. Clear module boundaries should prevent this
2. If conflict occurs:
   - Latest merge wins
   - Other agent rebases and resolves conflicts
   - Re-run tests

---

### Problem: Incompatible Changes

**Symptom:**
```
Agent A5 changed GameStateService interface, breaking A6's navigation code
```

**Solution:**
1. Version interfaces (use @Deprecated for old methods)
2. Coordinate breaking changes via communication channel
3. Update dependent code before merging

---

## Progress Metrics

### Daily Metrics

Track these daily:
- **Tasks Completed**: Count of ✅ COMPLETED tasks
- **Tasks In Progress**: Count of 🟡 IN PROGRESS tasks
- **Blockers**: Count of tasks waiting on dependencies
- **Velocity**: Tasks completed / day

**Example:**
```
Day 1: 3 tasks completed (T-001, T-002, T-200)
Day 2: 5 tasks completed (T-003, T-005, T-201, T-202, T-203)
Day 3: 12 tasks completed (T-006, T-007, T-008, A4a finished 9 packets)
```

### Weekly Milestones

**Week 1:**
- [ ] Project structure created
- [ ] Network layer operational
- [ ] Packet decoder framework ready

**Week 2:**
- [ ] 100+ packets implemented
- [ ] Database operational
- [ ] 30% test coverage

**Week 3:**
- [ ] Game state tracking works
- [ ] Navigation functional
- [ ] Combat system operational

**Week 4:**
- [ ] API complete
- [ ] Frontend integrated
- [ ] 80% test coverage

---

## Example: First Day Execution

### Morning (Hours 0-4)

**Launch simultaneously:**

```bash
# Terminal 1
launch_agent A1 "T-001: Create Maven multi-module project"

# Terminal 2
launch_agent A2 "T-200: Design database schema"

# Terminal 3
launch_agent A11 "Write architecture documentation"
```

**Expected progress:**
- Hour 2: A1 completes T-001
- Hour 4: A1 completes T-002
- Hour 4: A2 completes T-200, T-201

---

### Afternoon (Hours 4-8)

**Launch next wave:**

```bash
# Terminal 4
launch_agent A3 "T-005: Implement Netty TCP proxy"
# (can start because A1 finished T-002)

# A2 continues with T-202, T-203
# A11 continues documentation
```

**Expected progress:**
- Hour 8: A2 completes T-202, T-203 (database fully done!)
- Hour 8: A3 at 50% on T-005

---

### Evening (Optional)

**Continue critical path:**

```bash
# A3 continues T-005
# Target: Complete T-005 by end of Day 1
```

---

## Example: Week 2 Mega Sprint

### Monday Morning: Launch 10 Packet Agents

```bash
# Launch all 10 agents simultaneously at 9 AM
launch_agent A4a "T-009 to T-018: Authentication packets"
launch_agent A4b "T-019 to T-033: Map packets"
launch_agent A4c "T-034 to T-048: Combat packets"
launch_agent A4d "T-049 to T-058: Inventory packets"
launch_agent A4e "T-059 to T-068: Dialog packets"
launch_agent A4f "T-069 to T-078: Stats packets"
launch_agent A4g "T-079 to T-088: Chat packets"
launch_agent A4h "T-089 to T-098: Exchange packets"
launch_agent A4i "T-099 to T-104: Movement packets"
launch_agent A4j "T-105 to T-108: Misc packets"
```

**Target:** All 100 packets implemented by Monday 1 PM (4 hours)

**Reality Check:**
- If agents work at different speeds, completion might be by Monday EOD
- Still 10x faster than single agent (4-8 hours vs 40 hours)

---

## Final Checklist Before Launch

Before launching agents, ensure:

- [ ] PRD reviewed and understood
- [ ] Implementation Book reviewed
- [ ] Agent profiles understood
- [ ] Task dependencies mapped
- [ ] Communication protocol established
- [ ] Merge strategy agreed upon
- [ ] CI/CD pipeline ready
- [ ] Branch strategy defined
- [ ] Task tracking board created

---

## Communication Templates

### Agent Launch Prompt Template

```
You are [Agent Name] ([Agent ID]), a specialized [Agent Role].

Your mission: Complete tasks [Task IDs] as defined in IMPLEMENTATION_BOOK.md.

Prerequisites:
- [List any dependencies or things to check first]

Context:
- [High-level description of what to build]
- [Why it's important]
- [How it fits into the overall system]

Deliverables:
- [Specific code files, tests, documentation]

Acceptance Criteria:
- [How to know when done]

References:
- IMPLEMENTATION_BOOK.md tasks [Task IDs]
- PRD_JAVA_DOFUS_PACKET_DECODER.md section [X]

Please proceed with implementation. Report progress every 2 hours.
```

---

### Progress Report Template

```
Agent: [Agent ID]
Date: [YYYY-MM-DD HH:MM]
Tasks: [Task IDs]

Progress:
- Task [ID]: [X]% complete
  - [What's done]
  - [What's remaining]

Blockers:
- [None / List blockers]

Questions:
- [Any questions for coordination]

Next Steps:
- [What you'll work on next]

ETA:
- Task [ID]: [X] hours remaining
```

---

## Success!

When all agents complete their tasks and the project is integrated:

**Final Integration:**
1. Merge all feature branches to `develop`
2. Run full integration test suite
3. Deploy to staging environment
4. Manual testing with real Dofus client
5. Merge `develop` to `main`
6. Deploy to production
7. 🎉 Celebrate!

**Result:**
- ✅ Full Dofus packet decoder operational
- ✅ Game state tracking working
- ✅ Navigation and combat systems functional
- ✅ REST API and WebSocket live
- ✅ Web dashboard deployed
- ✅ 80%+ test coverage
- ✅ Complete documentation

**Timeline:**
- Single agent: ~15 weeks
- Multi-agent (11 agents): ~3-4 weeks
- **Time saved: 75%!**

---

**END OF COORDINATION GUIDE**
