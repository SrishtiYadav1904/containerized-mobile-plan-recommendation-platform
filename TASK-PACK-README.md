# Containerized Mobile Plan Recommendation Platform — Codex Task Pack

This pack contains one Codex prompt per remaining task, Tasks 4–14. Tasks 1–3 are treated as completed and are referenced from the uploaded project documentation; they are not redone.

## How to use

1. Open the project folder/repository in Codex.
2. Give Codex the matching `task-XX.md` prompt for the task you are currently implementing.
3. Review the changes before accepting them. Run commands only in the intended project directory.
4. Check `docs/task-reports/task-XX.md` after each task. It must report actual work and results, not intended work.
5. Keep evidence such as Git logs, PR links, Jenkins build logs, test reports, screenshots, Docker output, Ansible logs and health-check responses.

## Project decisions carried forward from Tasks 1–3

- Java 17, Spring Boot 3, Maven, Thymeleaf, Spring Security, Spring Data JPA.
- MySQL 8 for local/runtime use and H2 for automated tests.
- Tomcat 10.1, Jenkins, Docker and Ansible.
- Roles: Customer, Analyst, Administrator.
- UI: light theme, soft pink/lilac accents, responsive, polished and user-friendly.
- GitHub owner: `SrishtiYadav1904`.
- Ports 8072, 8081 and 8082 are reserved by other projects. Task prompts use Tomcat host port 8084 and Docker host port 8085 (container app port 8080). Check availability before binding.
- No credentials in Git. Do not claim external actions succeeded unless verified.

## Important scope note

The uploaded Task 1–3 document lists a **Task 15 — Documentation and Final Review** in its frozen delivery plan, while the new task list supplied for this pack ends at Task 14. This pack intentionally creates prompts for Tasks 4–14 only. If your course requires all 15 tasks, add a separate final documentation/review task after Task 14.

## Reports

Each prompt asks Codex to create/update `docs/task-reports/task-XX.md`. Use these reports as a running record of what happened, which commands actually ran, the test results, evidence and what remains manual.
