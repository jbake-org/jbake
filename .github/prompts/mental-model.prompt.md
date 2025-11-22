---
description: Build Mental Model Documentation (Peter Naur)
---

Analyze this software project and create comprehensive mental model documentation following Peter Naur's "Programming as Theory Building" principles. Use arc42 as the structural framework and follow the docs-as-code approach according to Ralf D. Müller. you will find the arc42 template in the `docs/arc42` folder. Use plantUML C4 diagrams where applicable.

## Your Task

Build documentation that enables new senior developers to understand not just WHAT the system does, but WHY it exists and HOW to think about it.

## Documentation Structure

Create the following arc42-based structure:

### arc42/01-introduction.md
- System vision and purpose
- Core quality goals (top 3)
- Stakeholders
- **ADD:** Technical roadmap (next 12 months)
- **ADD:** Deprecation plans

### arc42/03-context.md
- System boundaries (what's IN vs OUT)
- External interfaces
- Neighbor systems with rationale

### arc42/04-solution-strategy.md
⭐ **CRITICAL:** Document the top 5 architecture decisions
For each: Decision + Rationale + Consequences + Alternatives considered

### arc42/05-building-blocks.md
- Component landscape
- For each component: Role, Responsibility, Key constraint
- Dependency relationships

### arc42/08-concepts.md
⭐ **CRITICAL:** Mental Model Map

Include:
1. **Core Metaphor:** Central analogy for the system
2. **Must-Understand Concepts:** 3-5 fundamental concepts with:
   - Why it matters
   - Impact on development
   - Common mistakes newcomers make
3. **Unwritten Rules:** Team conventions not in code
4. **Failed Experiments:** "We tried X, didn't work because Y"
5. **Cross-cutting concerns:** Error handling, logging, security patterns

### arc42/09-decisions/
⭐ **CRITICAL:** Architecture Decision Records (ADRs)

Follow Michael Nygard's ADR format with these extensions:

**Required sections:**
- Status, Date, Context, Decision, Consequences
- **ADD:** Problem Statement (what issue are we solving?)
- **ADD:** Pugh Matrix for alternatives evaluation

**Pugh Matrix format:**
```
| Criterion        | Baseline | Alt 1 | Alt 2 |
|-----------------|----------|-------|-------|
| [Criterion 1]   | 0        | -2/+2 | -2/+2 |
| [Criterion 2]   | 0        | -2/+2 | -2/+2 |
| Total Score     | 0        | X     | Y     |
```
Scale: -2 (much worse) to +2 (much better) vs baseline

Write each ADR to its own file.

**ADD:** README.md with ADR timeline showing evolution phases

### arc42/11-risks.md
**ADD:** Post-mortems section
- Major incidents
- Root causes
- Lessons learned
- Why certain rules exist now

### onboarding/journey-map.md
4-week learning path:
- Week 1: Overview (goals + validation questions)
- Week 2: Fundamentals (goals + validation questions)
- Week 3: Deep dive (goals + validation questions)
- Week 4: Independence (goals + validation questions)

### onboarding/development-workflow.md
- Feature lifecycle (design → implement → review → deploy)
- Review checklist
- When to write ADRs
- Deployment process

### onboarding/team-structure.md
- Knowledge map (who knows what)
- Code ownership
- Decision processes
- Escalation paths

### onboarding/common-issues.md
Troubleshooting patterns in format:
**Symptom** → **Common Cause** → **Debugging Steps** → **Prevention**

### llm/knowledge-graph.md
⭐ **FOR LLM USAGE:** Structured knowledge

```yaml
concepts:
  - name: [Concept Name]
    level: [0=Fundamental, 1=Architecture, 2=Implementation]
    priority: [CRITICAL/HIGH/MEDIUM/LOW]
    prerequisites: [list]
    enables: [list]
    learning_time: [estimate]
    common_mistakes:
      - mistake: [description]
        why: [root cause]
        correct: [right approach]
    validation: [question to verify understanding]
    code_locations: [where to find examples]
```

### C4 diagrams

Example:

```plantuml
!include <C4/C4_Context>

title System Context diagram for Internet Banking System

Person(customer, "Banking Customer", "A customer of the bank, with personal bank accounts.")
System(banking_system, "Internet Banking System", "Allows customers to check their accounts.")

System_Ext(mail_system, "E-mail system", "The internal Microsoft Exchange e-mail system.")
System_Ext(mainframe, "Mainframe Banking System", "Stores all of the core banking information.")

Rel(customer, banking_system, "Uses")
Rel_Back(customer, mail_system, "Sends e-mails to")
Rel_Neighbor(banking_system, mail_system, "Sends e-mails", "SMTP")
Rel(banking_system, mainframe, "Uses")
```

### llm/antipatterns.md
Document what NOT to do:
```
❌ Antipattern: [Name]
Why wrong: [explanation]
✅ Correct approach: [solution]
Code example: [side-by-side comparison]
Related: [ADR/concept links]
```

## Critical Requirements

1. **Document the "Why"** - Not just what exists, but why decisions were made
2. **Make implicit explicit** - Capture "everyone knows" tribal knowledge
3. **Show evolution** - How the system got here (phases, pivots, migrations)
4. **Include failures** - What didn't work and lessons learned
5. **Hierarchy over timeline** - Structure concepts by dependency, not sequence
6. **Validate understanding** - Include questions to test comprehension
7. **Link everything** - Connect ADRs, code, concepts, runbooks

## Open Questions Report

**CRITICAL:** Create `open-questions.md` documenting:
- Missing information you need
- Ambiguities found in code/docs
- Unclear design decisions
- Assumptions you had to make
- Areas needing clarification from team
- Inconsistencies discovered

Format:
```markdown
## [Category]
**Question:** [specific question]
**Context:** [why this matters]
**Impact:** [what's blocked without answer]
**Found in:** [file/location]
**Assumption made:** [if you proceeded anyway]
```

## Quality Criteria

Documentation is complete when a new senior developer can:
- ✅ Understand design decisions without asking team
- ✅ Know why the system is built this way
- ✅ Develop features consistent with architecture
- ✅ Recognize violations of unwritten rules
- ✅ Debug issues using documented patterns
- ✅ Avoid common pitfalls that trapped others

## Process

1. Analyze codebase structure, dependencies, patterns
2. Review existing docs (if any)
3. Identify core architectural decisions
4. Extract implicit knowledge from code patterns
5. Document in arc42 structure in asciidoc format with plantuml diagrams
6. Build LLM knowledge graph
7. Create onboarding journey
8. **Document all open questions continuously**

Focus on Peter Naur's insight: The real program is the theory in developers' minds, not the code itself. Your job is to externalize that theory.