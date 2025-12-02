

- JTexy test suite only runs with JDK 17. Use Eclipse Temurin JDK 17 to run the tests (don't run them now, just remember).

  `export JAVA_HOME=/home/o/.jdks/temurin-17.0.17 && export PATH=$JAVA_HOME/bin:$PATH && cd /home/o/uw/jbake && mvn ...`


- Check the docs/CONTRIBUTING.md file for coding style.

- Prefer Kotlin idioms over Java ones where possible.

    * runCatching instead of try-catch when both `try` and `catch` blocks are short.
    * Use Kotlin's rich collection APIs.

- Feel free to suggest large-scale refactoring if you see opportunities to improve code structure or readability.

- Write unit tests for new features and bug fixes. Use Kotest and Mockk for testing. Kotest allows parametrized tests with `forAll` and `row`.

- Keep Java interop in mind. JBake is also used as a library. New public APIs should be easily usable from Java.

- When you assume a task is done, run the full test suite with both Maven and Gradle. `mvn clean verify` and `./gradlew clean test`. Include E2E tests.

- Avoid running custom commands in CLI, as the developer has to confirm them.
  - For instance, `find ... -exec` or `sed -i` are discouraged.
  - Use IDEA's Find and Replace tool instead.

- When modifying build files, ensure compatibility with both Maven and Gradle builds. Update both `pom.xml` and `build.gradle` as needed.
