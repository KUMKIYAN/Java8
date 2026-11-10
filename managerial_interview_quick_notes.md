# Managerial Interview – Quick Revision Notes

**Target:** Manager / Technical Manager  
**Core message:** Demonstrate business ownership, people leadership, sound decisions, clear communication, and delivery accountability.

## 1. Production incident management

**Framework: Assess → Prioritize → Delegate → Communicate → Resolve → Prevent**

1. **Assess:** Identify business impact, affected services, customers, payment channels, and transaction states.
2. **Prioritize:** Classify severity using the organization's incident process (e.g., P1/P2/P3).
3. **Delegate:** Assign clear owners based on expertise.
4. **Communicate:** Share the incident number, known facts, mitigation plan, and next update time.
5. **Resolve:** Investigate, mitigate, test, deploy safely, and validate business functionality.
6. **Prevent:** Complete RCA and track corrective/preventive actions.

**Payment incident checklist**
- Are all payment methods affected, or only specific channels?
- Are transactions failed, pending, successful-but-unacknowledged, or duplicated?
- Is the issue in our service or a downstream dependency?
- Protect transaction integrity; use idempotency and reconciliation before replaying transactions.

## 2. Team and resource management

**Example allocation for 8 developers during a critical incident**
- 4 experienced developers: investigate and restore the affected service.
- 2 developers: identify impacted transactions and prepare reconciliation.
- 2 developers: continue critical sprint work and support other incidents.
- Manager: coordinate the response, remove blockers, manage escalations, and communicate with stakeholders.

Adjust allocation to incident severity and skills. Don't assign people solely by headcount.

## 3. Client and stakeholder communication

- Acknowledge the issue and its business impact.
- Share the incident/ticket number and verified initial findings.
- Explain what the team is doing and any known risks.
- Give a realistic ETA only when supported by evidence.
- If the ETA is unknown, commit to a specific **next update time** (for example, in 30 minutes).
- Continue updates at the agreed cadence until service is stable.

**Useful line:** “I won't commit to an unverified resolution time, but I will provide a clear progress update at the agreed interval.”

## 4. Sprint and delivery management

- Treat critical production incidents as the immediate priority.
- Protect essential sprint work where possible.
- Identify which stories can continue and which must pause.
- Discuss scope and delivery impact with the Product Owner / stakeholders early.
- Re-plan commitments transparently; don't sacrifice quality to preserve a date.
- Capture interrupted work and agree on revised owners and timelines.

## 5. Conflict resolution and technical disagreement

1. Bring the relevant stakeholders together.
2. Ask each person to explain the evidence, assumptions, benefits, and risks.
3. Compare options against business impact, data integrity, security, reliability, and reversibility.
4. Make and communicate a clear decision; don't force consensus if the evidence points one way.
5. Record the decision and assign actions.

**Golden rule:** A temporary workaround must be tested, reversible, monitored, and safe. Never accept an unmitigated risk of duplicate payments or data corruption just to restore service faster.

## 6. Technical decision-making checklist

Evaluate:
- Business impact and urgency
- Technical feasibility and complexity
- Security and compliance
- Data consistency and transaction integrity
- Performance and scalability
- Reliability and rollback options
- Maintainability and operational support
- Cost and delivery impact

## 7. Safe production deployment

Before deployment:
- Peer review and required approvals
- Unit, integration, and regression tests
- Smoke/sanity checks
- Database migration compatibility and backup/rollback plan
- Controlled deployment (rolling/canary where appropriate)
- Monitoring and alerting
- Business validation: payment success rate, errors, latency, and reconciliation

After deployment:
- Verify critical user journeys and transaction states.
- Watch metrics and logs closely; use an agreed observation window.
- Roll back or mitigate if error rates or business impact increase.

**Note:** Code coverage is useful, but coverage alone does not prove the fix is correct.

## 8. Root Cause Analysis (RCA)

Include:
1. Incident summary and severity
2. Business/customer impact
3. Timeline
4. Root cause and contributing factors
5. Immediate mitigation and resolution
6. Corrective and preventive actions
7. Owner and due date for each action
8. Follow-up verification

Don't stop at fixing the symptom. Track actions to completion and verify they prevent recurrence.

## 9. Common managerial scenarios

| Scenario | Strong approach |
|---|---|
| Developer misses a deadline | Understand blockers, agree on recovery actions, and communicate impact early. |
| Team member underperforms | Have a private discussion, identify gaps, coach, and set measurable expectations. |
| Client escalation | Acknowledge, assess impact, share an action plan, and update regularly. |
| Production vs. sprint | Prioritize business-critical recovery and re-plan sprint scope with the Product Owner. |
| Two engineers disagree | Compare evidence and risks; make a reasoned decision. |
| Scope changes mid-sprint | Assess impact, clarify priority, and agree on trade-offs. |
| Resource becomes unavailable | Identify backup, redistribute work, and manage dependencies. |
| Repeated production defects | Perform RCA and improve design, testing, automation, and monitoring. |
| Tight deadline | Prioritize essential scope, surface risks, and agree on realistic milestones. |

## 10. Managerial metrics to know

| Area | Example metrics |
|---|---|
| Delivery | Sprint goal completion, cycle time, predictability |
| Quality | Escaped defects, change failure rate |
| Production | MTTR, incident frequency, availability |
| Performance | Latency, throughput, error rate |
| Reliability | SLO/SLA attainment, alert response |
| Team | Capacity, workload balance, retention |
| Client | Escalations, satisfaction, SLA adherence |

Use metrics to understand trends and improve outcomes—not to blame individuals.

## 11. Strong interview phrases

- “My immediate priority is business continuity and customer impact.”
- “I would make an evidence-based, risk-aware decision.”
- “I would delegate based on expertise and establish clear ownership.”
- “I would communicate verified facts and the next update time.”
- “I would not compromise transaction integrity for a faster workaround.”
- “I would balance production priorities with transparent sprint re-planning.”
- “I would focus on immediate mitigation as well as long-term prevention.”
- “I would assign an owner and due date to every preventive action.”

## 12. Answer structure to remember

For most managerial questions, use:

**Situation → Impact → Options → Decision → Ownership → Communication → Outcome → Prevention**

Keep your answer structured:
1. State your priority or decision first.
2. Explain the reasoning.
3. Describe who does what.
4. Explain how you communicate and measure success.
5. Close with prevention or learning.

## Last-minute reminder

**Assess → Delegate → Communicate → Decide → Deliver → Prevent**

Don't answer only as the person fixing the code. Answer as the person accountable for the business outcome, the team, the client, and safe delivery.
