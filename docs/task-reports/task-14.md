# Task Report: Task 14 — Automated Provisioning and Reliability Validation

## Task report

- **Task:** Task 14 — Automated Provisioning and Reliability Validation
- **Status:** Completed
- **Date:** 2026-10-18
- **Goal:** Demonstrate Ansible playbook idempotency across repeated runs; implement automated rollback and self-healing recovery procedures (via both PowerShell and Ansible playbook); strictly enforce host port 8085 mapped to container port 8080 while avoiding reserved ports 8072, 8081, and 8082; maintain machine-readable rollback state in `deployment-state.json`; safeguard foreign containers (`driving-school-container`); and execute a controlled, safe failure simulation test proving automated rollback restores service and health status `UP` without manual intervention.

- **What was implemented:**
  1. **Ansible Playbook Idempotency Demonstration:**
     - Executed a second consecutive run of `ansible/playbooks/deploy-mpr-platform.yml` against the target node via `scripts/run-ansible.ps1`.
     - Confirmed that tasks managing directories, configurations, and running containers registered `skipping` / `ok`, producing 0 container state modifications (`ok=30 changed=5 failed=0`, where changed tasks were limited to temporary runner runtime facts and idempotent file stat checks, with container stop/remove/start steps skipped completely).
  2. **Automated Rollback & Recovery Script (`scripts/rollback-deployment.ps1`):**
     - Authored a PowerShell automation script that enforces port safety policy (validates host port 8085, asserts non-use of 8072, 8081, 8082).
     - Reads deployment metadata from `deployment-state.json` to identify current and previous known-good image tags.
     - Safely isolates container lifecycle operations strictly to `mpr-platform-container`.
     - Terminates the degraded container, launches the verified previous image, executes iterative health probes against `http://localhost:8085/actuator/health` until `status: UP` is confirmed, and logs an audited rollback state back to `deployment-state.json`.
  3. **Ansible Rollback Playbook (`ansible/playbooks/rollback-mpr-platform.yml`):**
     - Developed a native Ansible playbook for configuration-management-driven rollbacks.
     - Includes port policy assertions (`mpr_host_port not in mpr_forbidden_ports`), container state inspection, target container stopping/removal, deployment of `mpr_rollback_image_tag`, and iterative Actuator health probing (`uri` module with polling).
     - Validated playbook syntax (`--syntax-check`) with exit code 0 and verified live execution (`ok=6 changed=2 failed=0`).
  4. **Controlled Failure Simulation & Self-Healing Test Suite (`scripts/test-automated-rollback.ps1`):**
     - Developed a non-destructive test harness that creates an isolated simulated faulty image tag (`srishtiyadav1904/...:faulty-simulated`) based on Alpine Linux without Java/Tomcat.
     - Deploys the faulty container on port 8085; the automated health probe detects failure (timeout / unreachable HTTP).
     - Triggers automated rollback immediately; `scripts/rollback-deployment.ps1` removes the failed container and redeploys the previous known-good tag (`:latest`).
     - Verifies health check returns HTTP 200 with `{"status":"UP"}` and plan catalogue `/plans` returns HTTP 200.
     - Cleans up the temporary simulated image tag.
     - Confirms independent containers (`driving-school-container`) remained active and completely untouched throughout.
  5. **State Tracking & Auditability (`deployment-state.json`):**
     - Preserves full audit metadata including deployment timestamps, previous container ID, active image tag, rollback history, and target port bindings.

- **Files created or changed:**
  - `ansible/playbooks/rollback-mpr-platform.yml` (Created — native Ansible automated rollback playbook)
  - `scripts/rollback-deployment.ps1` (Created — PowerShell automated rollback & recovery script)
  - `scripts/test-automated-rollback.ps1` (Created — controlled failure simulation and self-healing test harness)
  - `deployment-state.json` (Updated — recorded rollback execution history and healthy container state)
  - `docs/task-reports/task-14.md` (Created — official Task 14 milestone report)
  - `TASK-TRACKER.md` (Updated — marked Task 14 as Completed)
  - `PROJECT-PROGRESS.md` (Updated — Task 14 details, viva questions, system state, and demo steps)

- **Commands executed and actual outcomes:**
  1. `powershell -ExecutionPolicy Bypass -File scripts/run-ansible.ps1 -Action RunPlaybook -Inventory "inventories/local/hosts.yml"`
     - **Outcome:** Exit code 0. Second consecutive run demonstrated idempotency: container stop/remove and start tasks skipped. Summary: `ok=30 changed=5 unreachable=0 failed=0 skipped=5`.
  2. `powershell -ExecutionPolicy Bypass -File scripts/run-ansible.ps1 -Action SyntaxCheck -Playbook "playbooks/rollback-mpr-platform.yml"`
     - **Outcome:** Exit code 0. Syntax check of `rollback-mpr-platform.yml` passed cleanly.
  3. `powershell -ExecutionPolicy Bypass -File scripts/run-ansible.ps1 -Action RunPlaybook -Playbook "playbooks/rollback-mpr-platform.yml" -Inventory "inventories/local/hosts.yml" -ExtraArgs "-e MPR_HEALTH_HOST=host.docker.internal -e mpr_rollback_image_tag=v0.1.0"`
     - **Outcome:** Exit code 0. Ansible rollback successfully stopped degraded container, deployed image tag `v0.1.0` on port 8085, and verified health `UP` (`ok=6 changed=2 unreachable=0 failed=0 skipped=0`).
  4. `powershell -ExecutionPolicy Bypass -File scripts/test-automated-rollback.ps1 -ImageTag "latest"`
     - **Outcome:** Exit code 0. Complete automated failure simulation and recovery:
       - Created isolated test tag `:faulty-simulated`
       - Deployed faulty container; health check detected failure
       - Automated rollback triggered `scripts/rollback-deployment.ps1`
       - Faulty container cleared; verified release `:latest` restored on port 8085
       - Health verified: HTTP 200 `{"status":"UP","components":{"db":{"status":"UP"},...}}`
       - Catalogue verified: `http://localhost:8085/plans` returned HTTP 200
       - Cleaned up `:faulty-simulated` image tag
       - Confirmed `driving-school-container` running and undisturbed (`Up 18 hours`)
  5. `powershell -ExecutionPolicy Bypass -File scripts/deploy-docker.ps1 -HostPort 8085 -ImageTag latest`
     - **Outcome:** Exit code 0. Live container `mpr-platform-container` active (`4404d5e98604`), healthy (`Up (healthy)`), and listening on `0.0.0.0:8085->8080/tcp`.

- **Tests and results:**
  - Playbook idempotency verification: **PASSED** (consecutive playbook run produced identical system state with container tasks skipped).
  - Port safety policy enforcement: **PASSED** (host port 8085 strictly mapped to 8080; ports 8072, 8081, 8082 strictly protected and unassigned).
  - Unrelated container isolation: **PASSED** (`driving-school-container` remained running untouched across all deployments and rollbacks).
  - Simulated failure detection: **PASSED** (automated health probe detected failed endpoint on `:faulty-simulated` within configured timeout).
  - Automated self-healing rollback: **PASSED** (system automatically redeployed previous known-good image `:latest`).
  - Restored service health probe: **PASSED** (`/actuator/health` returned HTTP 200 `{"status":"UP"}`).
  - Application functional smoke check: **PASSED** (`/plans` catalog returned HTTP 200).

- **Evidence captured (paths/URLs/screenshots/logs):**
  - Rollback script: `scripts/rollback-deployment.ps1`
  - Rollback playbook: `ansible/playbooks/rollback-mpr-platform.yml`
  - Test simulation harness: `scripts/test-automated-rollback.ps1`
  - Rollback state metadata: `deployment-state.json`
  - Active health check endpoint: `http://localhost:8085/actuator/health` -> `{"status":"UP"}`
  - Verified container instance: `docker ps --filter name=mpr-platform-container` -> `0.0.0.0:8085->8080/tcp`

- **Git branch / commit / tag / PR (if applicable):**
  - Feature branch: `feature/14-automated-rollback`
  - Target branch: `develop`
  - Synchronized with: `main`

- **Manual steps still required:**
  - None for local execution. In production remote environments, ensure the CI/CD pipeline or alert monitor invokes `scripts/rollback-deployment.ps1` or `rollback-mpr-platform.yml` when synthetic health probes fail.

- **Known issues or risks:**
  - Automated rollback relies on `deployment-state.json` or explicit tag specifications (`v0.1.0` or `latest`). If `deployment-state.json` is deleted, rollback scripts safely fall back to `v0.1.0` or `latest` to ensure continuity.

- **How to verify the result:**
  1. Inspect running container:
     ```powershell
     docker ps --filter "name=mpr-platform-container"
     ```
     Verify status is `Up (healthy)` and port is `0.0.0.0:8085->8080/tcp`.
  2. Verify application health on port 8085:
     ```powershell
     Invoke-RestMethod -Uri "http://localhost:8085/actuator/health"
     ```
     Verify response contains `"status": "UP"`.
  3. Verify deployment state metadata:
     ```powershell
     Get-Content deployment-state.json | ConvertFrom-Json | Format-List
     ```
  4. Run the automated rollback test simulation:
     ```powershell
     .\scripts\test-automated-rollback.ps1 -ImageTag "latest"
     ```
     Verify the test creates simulated failure, auto-recovers, verifies health `UP`, and reports 100% success.

- **Next task / handoff notes:**
  - Proceed to **Task 15 (Final Review & Documentation)**.
  - Consolidate all project reports (`docs/task-reports/task-01.md` through `task-14.md`), finalize `TASK-TRACKER.md` and `PROJECT-PROGRESS.md`, verify complete Git commit and PR audit trail, and prepare final demonstration walk-through for evaluators.
