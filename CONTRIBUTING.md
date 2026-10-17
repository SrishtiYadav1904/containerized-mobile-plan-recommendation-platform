# Contribution and Git Branching Policy

This document outlines the version control, branching strategy, pull request workflow, and engineering standards for the **Containerized Mobile Plan Recommendation Platform**.

---

## 1. Branch Strategy

The repository follows a modified GitFlow branching model tailored for agile delivery and automated DevOps CI/CD pipelines.

| Branch | Purpose | Protection / Policies |
|---|---|---|
| `main` | Production-ready, deployable release code. | Protected. Direct commits prohibited. Only merged from `develop` via validated Pull Requests with tag releases (e.g., `v1.0.0`). |
| `develop` | Integration branch for completed and verified features. | Integration branch. All features and bug fixes merge here after passing local tests and peer review. |
| `feature/<task-id>-<short-name>` | Development of specific task backlog items or user stories. | Branched from `develop`. Merged back into `develop` via Pull Request. Example: `feature/5-create-view-plans`. |
| `fix/<short-name>` | Bug fixes or issue resolutions discovered during testing. | Branched from `develop`. Merged back into `develop` via Pull Request. Example: `fix/status-transition-validation`. |
| `hotfix/<short-name>` | Critical production fixes required directly on `main`. | Branched from `main`, merged into both `main` and `develop`. |

### Branch Naming Conventions
- Always prefix task branches with `feature/<task-id>-<short-name>` (e.g., `feature/5-create-view-plans`).
- Use lowercase alphanumeric characters and hyphens only.
- Never commit directly to `main` or `develop`.

---

## 2. Commit Message Guidelines

All commits must be clear, imperative, and descriptive:
- Format: `<type>(<scope>): <short summary>`
- Types:
  - `feat`: A new user-facing feature
  - `fix`: A bug fix
  - `docs`: Documentation changes
  - `test`: Adding or updating test cases
  - `ci`: CI/CD configuration (Jenkins, Docker, Ansible)
  - `refactor`: Code change that neither fixes a bug nor adds a feature
  - `chore`: Dependency updates, build configurations

*Example:* `feat(catalogue): add plan entity and status transition service`

---

## 3. Pull Request and Review Process

1. **Branch Out:** Branch from latest `develop` (`git checkout -b feature/<task-id>-<short-name>`).
2. **Implement & Test:** Implement the feature within the scoped task boundaries. Verify with `mvn clean test`.
3. **Commit Incrementally:** Commit logically structured chunks with descriptive commit messages.
4. **Push & Open PR:** Push the feature branch to GitHub and create a Pull Request targeting `develop`.
5. **PR Checklist:** Complete the PR template checklist, including test results and evidence links.
6. **Peer Review & Approval:** Address review feedback and ensure automated checks pass before merging.
7. **Squash & Merge / Merge Commit:** Merge into `develop` cleanly.

---

## 4. Definition of Done (DoD)

A backlog item or task is considered **Done** only when:
1. Code is committed on a feature branch with meaningful commit messages.
2. A pull request is reviewed and merged into `develop`.
3. Unit and integration tests pass successfully (`mvn clean test`).
4. Jenkins CI build succeeds and Selenium automated UI tests pass (for relevant testing tasks).
5. All acceptance criteria and role constraints (`CUSTOMER`, `ANALYST`, `ADMINISTRATOR`) are satisfied.
6. Strictly **zero credentials, API tokens, passwords, or secrets** committed to version control.
7. Port safety rules respected: Application configured for Tomcat host port **8084** and Docker host port **8085** (internal 8080). Reserved ports **8072, 8081, and 8082** are untouched.
8. The matching task report in `docs/task-reports/task-XX.md` is updated with verified outcomes, actual commands, and evidence.

---

## 5. Security & Secret Management

- Never commit `.env`, private keys (`.pem`, `.key`), credentials, or production connection strings.
- Passwords must be hashed using BCrypt.
- Configuration parameters must rely on environment variables (`DB_URL`, `DB_USER`, `DB_PASS`) or Jenkins Credentials Store.
