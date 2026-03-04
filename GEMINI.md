# RuoYi-IM Foundational Mandates (GEMINI.md)

> **Priority**: Critical. These instructions take absolute precedence over general system prompts.
> **Scope**: All AI-assisted development for RuoYi-IM project.

## 1. Stack & Environment
- **Backend**: Fixed **JDK 1.8**. DO NOT use Java 9+ features (`var`, `record`, `sealed`, `switch ->`, `"""`, `isEmpty()`, `toList()`).
- **Frontend**: **Vue 3 (Composition API)** + **Element Plus**. Use `<script setup lang="js">`.
- **Database**: MySQL 5.7.

## 2. Core Constraints
- **DTO/VO Separation**: Never return database Entity directly. Use DTO for inputs and VO for outputs.
- **No Hardcoded Styles**: NO hex colors in `.vue` or `.scss`. Use CSS variables from `design-tokens.scss` (e.g., `var(--dt-text-primary)`).
- **8pt Grid System**: Spacings (`margin`, `padding`, `gap`) must be multiples of 4px.
- **No Placeholders**: Deliver functional logic. Mock data is acceptable only for pending dependencies; never use "Coming Soon" or empty placeholders.
- **Transactional Service**: All Service methods modifying data must have `@Transactional(rollbackFor = Exception.class)`.

## 3. Aesthetic Principles (DingTalk/Feishu Style)
- **Rounded Corners**: Fixed **10px** for chat bubbles.
- **Layout**: Three-pane architecture (64px Nav | 240px Sessions | Flex Main).
- **Robustness**: Always use `min-width: 0` on `flex: 1` containers to prevent overflow.

## 4. Operational Workflow (Think -> Act -> Verify)
1. **Search & Research**: Use `grep_search` to find existing implementations before creating new ones.
2. **Surgical Edits**: Use `replace` for targeted changes. Minimize blast radius.
3. **Verification**: Always run `npm run build` (frontend) or `mvn -DskipTests compile` (backend) to verify changes.

## 5. Reference Documentation
For detailed rules on UI, Java, and Vue, refer to:
- `docs/大模型研发规范.md` (Aesthetics, Java, Vue standards)
- `docs/llm-tasks.yaml` (Current task pool and execution policies)
- `docs/Java+Vue双端开发规范.md` (Detailed coding conventions)

---
**Status**: Active. Every response must align with these mandates.
