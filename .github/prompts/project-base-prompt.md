

- JTexy test suite only runs with JDK 17. Use Eclipse Temurin JDK 17 to run the tests (don't run them now, just remember).
  ~~`export JAVA_HOME=$HOME/.jdks/temurin-17.0.17 && export PATH=$JAVA_HOME/bin:$PATH && mvn ...`~~
    - Update: Not needed anymore, Maven is now set up with `<toolchain>` so it work just with `mvn ...`.

- Check the docs/CONTRIBUTING.md file for coding style.

- Prefer Kotlin idioms over Java ones where possible.
    * runCatching instead of try-catch when both `try` and `catch` blocks are short.
    * Use Kotlin's rich collection APIs.

- Feel free to suggest large-scale refactoring if you see opportunities to improve code structure or readability.

- Write unit tests for new features and bug fixes. Use Kotest and Mockk for testing. Kotest allows parametrized tests with `forAll` and `row`. Use StringSpec rather than FunSpec.

- Keep Java interop in mind. JBake is also used as a library. New public APIs should be easily usable from Java.

- When you assume a task is done, run the full test suite with both Maven and Gradle. `mvn clean verify` and `./gradlew clean test`. Include E2E tests.
  - The run may take around a minute.

- Avoid running exec commands in CLI, as the developer has to confirm them.
  - For instance, `find ... -exec` or `sed -i` are discouraged. Use IDEA's Find and Replace tool instead.
  - Instead of a find + exec construct, prefer using `rg`. If not installed, prompt the user to install `ripgrep`. It's faster and needs no `-exec`.

- When modifying build files, ensure compatibility with both Maven and Gradle builds. Update both `pom.xml` and `build.gradle` as needed.

- Try not to duplicate code. If you find yourself copying and pasting code, consider refactoring it into a shared function, class, or module.

- Specifically for Gemini agent: After stating "I will do X now", do it, rather than stopping.

- Do not generate lengthy explanations or justifications for your actions. Focus on providing concise, actionable code changes or suggestions. Assume the user knows Kotlin, Java, Docker, and the frameworks used. Do not explain basic concepts. Do not apologize. Do not exaggerate by saying "you are absolutely right" or similar; challenge the prompts with your expertise after analyzing them the user's prompt.

- Do not generate lengthy reports. The user is supposed to read and understand the code. If some construct is complex, add concise comments in the code itself. But ideally, rely on perfect naming and structure.

- Prefer brief code constructs over longer ones. For instance:
  - Use `mapNotNull` instead of `map` followed by `filterNotNull`.
  - If an `if` contains only return, then put it on a single line. I.e.:
    - `if (inBlockComment) continue`
    - `if (isMeaningfulLine(line))\n    count++`
  - Exit the function early instead of wrapping the main logic in an `if` block.
  - Use lambda logging using Slf4j's lazy logging: `logger.debug { "Expensive log message: ${compute()}" }`.
  - Put the logger `log` instance as a private member of the class (at the end of it) instead of using companion object.
  - Use single-expression functions when they are short.
      - For longer ones, put the `=` indented on the next line.

- Prefer brief code constructs over longer ones. For instance, use `mapNotNull` instead of `map` followed by `filterNotNull`.

- When invoking the command line, remember to add batch mode options to avoid interactive prompts during automated processes, e.g., `--batch-mode` for Maven commands, `--no-pager` for Git commands, etc.

- Don't leave empty lines before closing braces.

- When adding new dependencies, prefer using BOMs to manage versions consistently across Maven and Gradle.
- Maven: Put the <dependency> elements on a single line each.
- Maven and Gradle: When adding plugins, ensure they are added to both `pom.xml` and `build.gradle` files.

- Write scripts as Kotlin Scripts (.kts) when practical. Avoid Python. Avoid Bash except for short scripts where Kotlin alternative would be more than twice as long.
