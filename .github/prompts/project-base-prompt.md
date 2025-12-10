
This document provides guidelines for contributing to **JBake**, an open-source static site generator written in Kotlin.

**Project Context:**
- Primary language: Kotlin (with Java interoperability)
- Build systems: Maven and Gradle (both must be maintained)
- Key technologies: Asciidoc, Freemarker, Thymeleaf, Markdown parsers
- Users: Site generators who rely on stable APIs

**Core Principles:**
- Maintain backwards compatibility for existing JBake users
- Write concise, idiomatic Kotlin code
- Minimize unnecessary code - question whether new code is needed
- Run unit tests frequently; run full test suite (including IT and E2E) before finalizing changes
- Keep dependency versions current while ensuring compatibility

## Environment Setup

- JBake test suite requires JDK 17. Use Eclipse Temurin JDK 17 to run the tests.
  ~~`export JAVA_HOME=$HOME/.jdks/temurin-17.0.17 && export PATH=$JAVA_HOME/bin:$PATH && mvn ...`~~
    - Update: Not needed anymore, Maven is now set up with `<toolchain>` so it works with just `mvn ...`.

## Build System

- When modifying build files, ensure compatibility with both Maven and Gradle builds. Update both `pom.xml` and `build.gradle` as needed.
- When adding new dependencies, prefer using BOMs to manage versions consistently across Maven and Gradle.
- Maven: Put the <dependency> elements on a single line each.
- Maven and Gradle: When adding plugins, ensure they are added to both `pom.xml` and `build.gradle` files.

## Code Style & Idioms

- Check the docs/CONTRIBUTING.md file for coding style.
- Prefer Kotlin idioms over Java ones where possible.
    * runCatching instead of try-catch when both `try` and `catch` blocks are short.
    * Use Kotlin's rich collection APIs.
    * Kotlin 2.2.x has a syntax of $$"..." where you do not need to escape dollar symbols using ${'$'}. So keep those where they exist, and use them where sensible.
    * DO NOT REPLACE THE $$"..." syntax with escaped dollars!!
- Prefer brief code constructs over longer ones. For instance:
  - Use `mapNotNull` instead of `map` followed by `filterNotNull`.
  - If an `if` contains only `return`, `break`, or `continue`, then put it on a single line. I.e.:
    - `if (inBlockComment) continue`
    - `if (isMeaningfulLine(line))\n    count++`
  - Exit the function early instead of wrapping the main logic in an `if` block.
  - Use lambda logging using Slf4j's lazy logging: `logger.debug { "Expensive log message: ${compute()}" }`.
  - Put the logger `log` instance as a private member of the class (at the end of it) instead of using companion object.
  - Use single-expression functions when they are short.
      - For longer ones, put the `=` indented on the next line.
      - Prefer returning simpler values in `if` rather than later (in `else` or after the `if`).
        - ```kotlin
          //  Not so great:
          if (!templateFileName.isNullOrEmpty()) {
              return templateDir.resolve(templateFileName)
          }
          return null

          // Better:
          if (templateFileName.isNullOrEmpty()) return null
          return templateDir.resolve(templateFileName)
          ```
- Don't leave empty lines before closing braces.
- Try not to duplicate code. If you find yourself copying and pasting code, consider refactoring it into a shared function, class, or module.
- Keep Java interop in mind. JBake is also used as a library. New public APIs should be easily usable from Java.
- When you add code intended for debugging or investigation, mark it somehow, e.g. with `... // DEBUG` comment or `// +++ DEBUG block start` and `// +++ DEBUG block end`, so it can be easily found and removed later.
- Add dots at the end of comment sentences, and capitalize them.

## Testing

- Write unit tests for new features and bug fixes. Use Kotest and Mockk for testing. Kotest allows parametrized tests with `forAll` and `row`. Use StringSpec rather than FunSpec. Do NOT use JUnit, especially not constructs like `assertTrue(foo.contains("bar"))`!
- When you assume a task is done, run the full test suite with both Maven and Gradle. `mvn clean verify` and `./gradlew clean test`. Include E2E tests.
  - The run may take around a minute.

## Development Workflow

- Feel free to suggest large-scale refactoring if you see opportunities to improve code structure or readability.
- Avoid running exec commands in CLI, as the developer has to confirm them.
  - For instance, `find ... -exec` or `sed -i` are discouraged. Use IDEA's Find and Replace tool instead.
  - Instead of `find ... | grep ...` or `find ... -exec ...`, prefer using `rg`. If not installed, prompt the user to install `ripgrep`.
- When running CLI, add the motivation for the command as a very brief comment after it, e.g. `rg 'somePattern'  # Expecting it to appear in ...`.
- Write scripts as Kotlin Scripts (.kts) when practical. Avoid Python. Avoid Bash except for short scripts where Kotlin alternative would be more than twice as long.
- When invoking the command line, remember to add batch mode options to avoid interactive prompts during automated processes, e.g., `--batch-mode` for Maven commands, `--no-pager` for Git commands, etc.
- To get the usages of ModelAttributes, run this: `rg 'import .+ModelAttributes' --type kotlin | grep '.kt' | cut -f1 -d':' | xargs grep -B6 -A4 'ModelAttributes' {} | grep -v import`

## Agent Behavior

- Do not generate lengthy explanations or justifications for your actions. Focus on providing concise, actionable code changes or suggestions. Assume the user knows Kotlin, Java, Docker, and the frameworks used. Do not explain basic concepts. Do not apologize. Do not exaggerate by saying "you are absolutely right" or similar; challenge the prompts with your expertise after analyzing them the user's prompt.
- Specifically for Grok agent: You tend not to provide any comments on your actions. Provide concise reasoning for your code changes when non-trivial. Comment on results of test runs and findings from web.
- Do not generate lengthy reports. The user is supposed to read and understand the code. If some construct is complex, add concise comments in the code itself. But ideally, rely on perfect naming and structure.
- Specifically for Gemini agent: After stating "I will do X now", do it, rather than stopping.
- If you detect "corrupted" file, it is probably the developer doing changes. Pause for 10 seconds and retry reading it. If still corrupted, mention it concisely and ask the user to fix it.
- IDE console tool has some weird bug that the first command in a given console gets split to two lines after the first minus and fails. If that happens, run the same command again, that works.
