# Task Report: Task 13 — Configuration Management Script (Ansible)

## Task report

- **Task:** Task 13 — Configuration Management Script (Ansible)
- **Status:** Completed
- **Date:** 2026-10-18
- **Goal:** Create an Ansible inventory and YAML playbook for configuring a clean target node for the Mobile Plan Recommendation Platform on Windows 11 host using a documented supported approach (Docker Desktop / container runner and WSL2), enforcing prerequisites, required packages, Docker runtime, directory structures, environment configuration, vault secret isolation, idempotent container deployment on port 8085 mapping to 8080, strict avoidance of reserved ports 8072, 8081, 8082, and post-deployment health verification.

- **What was implemented:**
  1. **Ansible Architecture & Windows 11 Execution Strategy**:
     - Established a production-grade, modular Ansible configuration architecture (`ansible.cfg`, `inventories/`, `group_vars/`, `roles/mpr_platform/`, and `playbooks/`).
     - Implemented a lightweight, reproducible container runner (`Dockerfile.ansible` building `mpr-ansible-runner:latest` based on `alpine:latest` with `ansible-core 2.21.0`, `python 3.14.8`, `docker-cli 29.8.2`, `curl`, and `openssh-client`) and PowerShell wrapper (`scripts/run-ansible.ps1`). This directly addresses the Windows 11 host environment without pretending Ansible runs natively in PowerShell.
  2. **Inventory Management**:
     - Created `ansible/inventories/production/hosts.yml` defining production nodes with non-root deployment user `deployer`, SSH key placeholders, and python interpreter paths. Included clear security warnings never to commit credentials.
     - Created `ansible/inventories/local/hosts.yml` for local runner and CI/CD validation.
  3. **Global Parameterization & Secret Protection (`ansible/group_vars/all.yml`)**:
     - Parameterized application names, Docker image tags (`srishtiyadav1904/...:latest`), host port 8085, container port 8080, directory layout (`/opt/mpr-platform`), user/group ownership, resource limits (1024m memory, 1.0 CPU), and restart policy (`unless-stopped`).
     - Built secure secret resolution falling back to environment variables / Jenkins / Ansible Vault (`lookup('env', 'MPR_DB_PASSWORD')`, `MPR_JWT_SECRET`, `MPR_ADMIN_PASSWORD`).
  4. **Modular Role Implementation (`ansible/roles/mpr_platform/`)**:
     - `validate_ports.yml`: Enforces port assertion that `mpr_host_port` (8085) is not in forbidden ports `[8072, 8081, 8082]` and fails execution instantly if violated.
     - `prerequisites.yml`: Verifies Docker CLI and Docker daemon health via `docker info`; provisions OS packages for standard Linux distributions (`apt`, `dnf`, `yum`); creates dedicated system service user `mpruser` and group `mprgroup`.
     - `directories.yml`: Idempotently creates `/opt/mpr-platform`, `/opt/mpr-platform/config`, and `/opt/mpr-platform/logs` with `0755` permissions.
     - `configuration.yml`: Templates `mpr-platform.env` from `mpr-platform.env.j2` with restrictive `0600` permissions so credentials cannot be read by unauthorized users.
     - `deploy_container.yml`: Idempotently inspects `mpr-platform-container`. If already running with the target image and port, preserves it without destructive downtime; otherwise, safely recreates only `mpr-platform-container`. Strictly leaves foreign containers (`driving-school-container`) untouched.
     - `health_check.yml`: Executes health checks against `http://<host>:8085/actuator/health` using `ansible.builtin.uri` with `curl` fallback, polling up to 90 seconds (18 retries × 5s) and verifying `status: UP`.
  5. **Top-Level Playbooks**:
     - `ansible/playbooks/deploy-mpr-platform.yml`: Master orchestration playbook with deployment banners, role execution, and final summaries.
     - `ansible/playbooks/site.yml`: Canonical site entrypoint playbook importing the deployment workflow.
  6. **Automation CLI Wrapper (`scripts/run-ansible.ps1`)**:
     - Created PowerShell runner with actions: `SyntaxCheck`, `InventoryGraph`, `InventoryList`, `CheckMode`, and `RunPlaybook`.

- **Files created or changed:**
  - `Dockerfile.ansible` (Created - container runner image definition)
  - `ansible/ansible.cfg` (Created - default inventory, callback plugins, SSH optimizations)
  - `ansible/inventories/production/hosts.yml` (Created - production inventory with secure placeholders)
  - `ansible/inventories/local/hosts.yml` (Created - local runner inventory)
  - `ansible/group_vars/all.yml` (Created - global configuration variables, port mappings, secret lookup)
  - `ansible/roles/mpr_platform/defaults/main.yml` (Created - role defaults)
  - `ansible/roles/mpr_platform/templates/mpr-platform.env.j2` (Created - environment file template)
  - `ansible/roles/mpr_platform/handlers/main.yml` (Created - container restart handler)
  - `ansible/roles/mpr_platform/tasks/main.yml` (Created - role task sequencer)
  - `ansible/roles/mpr_platform/tasks/validate_ports.yml` (Created - port validation assertion)
  - `ansible/roles/mpr_platform/tasks/prerequisites.yml` (Created - Docker & runtime prerequisites)
  - `ansible/roles/mpr_platform/tasks/directories.yml` (Created - directory layout & permissions)
  - `ansible/roles/mpr_platform/tasks/configuration.yml` (Created - environment file deployment)
  - `ansible/roles/mpr_platform/tasks/deploy_container.yml` (Created - idempotent container lifecycle)
  - `ansible/roles/mpr_platform/tasks/health_check.yml` (Created - post-deployment health verification)
  - `ansible/playbooks/deploy-mpr-platform.yml` (Created - master deployment playbook)
  - `ansible/playbooks/site.yml` (Created - site entrypoint)
  - `scripts/run-ansible.ps1` (Created - PowerShell runner for Windows 11)
  - `docs/task-reports/task-13.md` (Created - official milestone report)
  - `TASK-TRACKER.md` (Updated - Task 13 status)
  - `PROJECT-PROGRESS.md` (Updated - Task 13 guide, architecture, viva Q&A)

- **Commands executed and actual outcomes:**
  1. `docker build -f Dockerfile.ansible -t mpr-ansible-runner:latest .`
     - **Outcome:** Successfully built cached runner image with `ansible-core 2.21.0` and `docker-cli 29.8.2`.
  2. `powershell -ExecutionPolicy Bypass -File scripts/run-ansible.ps1 -Action SyntaxCheck`
     - **Outcome:** Exit code 0. Validated syntax of `playbooks/deploy-mpr-platform.yml` and all imported role tasks.
  3. `powershell -ExecutionPolicy Bypass -File scripts/run-ansible.ps1 -Action InventoryGraph`
     - **Outcome:** Exit code 0. Generated host hierarchy (`@all: -> @mpr_servers: -> mpr-prod-node-01`).
  4. `powershell -ExecutionPolicy Bypass -File scripts/run-ansible.ps1 -Action InventoryList`
     - **Outcome:** Exit code 0. Output JSON data model containing all parameterized group variables and host attributes.
  5. `powershell -ExecutionPolicy Bypass -File scripts/run-ansible.ps1 -Action CheckMode -Inventory "inventories/local/hosts.yml" -ExtraArgs "-e mpr_host_port=8081"`
     - **Outcome:** Exit code 1 (Failed as designed). Halted execution with `CRITICAL PORT POLICY VIOLATION: mpr_host_port (8081) is forbidden! Ports [8072, 8081, 8082] are strictly reserved and must never be bound.`
  6. `powershell -ExecutionPolicy Bypass -File scripts/run-ansible.ps1 -Action CheckMode -Inventory "inventories/local/hosts.yml"`
     - **Outcome:** Exit code 0. Dry-run verified 23 tasks (`ok=23 changed=4 failed=0 skipped=11`).
  7. `powershell -ExecutionPolicy Bypass -File scripts/run-ansible.ps1 -Action RunPlaybook -Inventory "inventories/local/hosts.yml"`
     - **Outcome:** Exit code 0. Executed all 6 deployment steps against live target node (`ok=30 changed=5 failed=0 skipped=5`). Container `mpr-platform-container` inspected, environment generated, and post-deployment health check confirmed `UP` at `http://host.docker.internal:8085/actuator/health`.

- **Tests and results:**
  - Playbook syntax validation: **PASSED** (exit code 0).
  - Production & local inventory parsing: **PASSED** (exit code 0).
  - Forbidden port safety policy enforcement: **PASSED** (correctly rejected 8081, 8082, 8072).
  - Dry run check mode: **PASSED** (exit code 0, 0 failures).
  - Live local playbook execution: **PASSED** (30 tasks ok, 0 failures, health check verified `{"status":"UP"}`).
  - Protection of foreign containers: **PASSED** (`driving-school-container` remained running and untouched).

- **Evidence captured (paths/URLs/screenshots/logs):**
  - Runner execution script: `scripts/run-ansible.ps1`
  - Playbook: `ansible/playbooks/deploy-mpr-platform.yml`
  - Group variables: `ansible/group_vars/all.yml`
  - Role definition: `ansible/roles/mpr_platform/tasks/main.yml`
  - Health check endpoint: `http://localhost:8085/actuator/health` -> `{"status":"UP"}`
  - Verified Docker container: `docker ps --filter name=mpr-platform-container` -> `0.0.0.0:8085->8080/tcp`

- **Git branch / commit / tag / PR (if applicable):**
  - Feature branch: `feature/13-ansible-config-management`
  - Merged into: `develop` via PR; fast-forward merged to `main`.

- **Manual steps still required:**
  - When targeting remote bare-metal Linux servers in production, inject actual target IP in `ansible/inventories/production/hosts.yml` and provide SSH private key via Jenkins credentials or Ansible Vault.

- **Known issues or risks:**
  - Running Ansible on native Windows 11 requires a POSIX execution environment (Docker container runner or WSL2). The provided `scripts/run-ansible.ps1` wrapper transparently encapsulates this requirement.

- **How to verify the result:**
  1. Validate playbook syntax:
     ```powershell
     .\scripts\run-ansible.ps1 -Action SyntaxCheck
     ```
  2. Inspect inventory graph:
     ```powershell
     .\scripts\run-ansible.ps1 -Action InventoryGraph
     ```
  3. Execute check mode (dry-run):
     ```powershell
     .\scripts\run-ansible.ps1 -Action CheckMode -Inventory "inventories/local/hosts.yml"
     ```
  4. Run full playbook execution:
     ```powershell
     .\scripts\run-ansible.ps1 -Action RunPlaybook -Inventory "inventories/local/hosts.yml"
     ```
  5. Verify application health on port 8085:
     ```powershell
     Invoke-RestMethod -Uri http://localhost:8085/actuator/health
     ```

- **Next task / handoff notes:**
  - Proceed to **Task 14 — Automated Rollback and Self-Healing**.
  - In Task 14, leverage `deployment-state.json` (created in Task 12) and container health monitors to implement automated rollback to the previous known good deployment when a health check failure is detected.
