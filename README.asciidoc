= JBake
Jonathan Bullock
2021-04-13
:idprefix:

http://jbake.org[JBake] is a Java based open source static site/blog generator for developers.

image:https://img.shields.io/github/v/release/jbake-org/jbake?label=download&sort=semver["GitHub Release Download", link="https://github.com/jbake-org/jbake/releases/latest"]
image:https://img.shields.io/maven-central/v/org.jbake/jbake-core.svg["Maven Download", link="http://jbake.org/download.html#maven"]
image:https://img.shields.io/homebrew/v/jbake.svg["Homebrew Download", link="http://jbake.org/download.html#homebrew"]

image:https://img.shields.io/travis/com/jbake-org/jbake/master.svg["Build Status", link="https://travis-ci.com/github/jbake-org/jbake"]
image:https://ci.appveyor.com/api/projects/status/2q7hvg03wsjx953b?svg=true["Appveyor Status", link="https://ci.appveyor.com/project/jbake-org/jbake"]
image:https://img.shields.io/coveralls/jbake-org/jbake/master.svg["Coverage Status", link="https://coveralls.io/r/jbake-org/jbake"]

image:https://badges.gitter.im/jbake-org/jbake.svg["Gitter Chat", link="https://gitter.im/jbake-org/jbake"]

== Documentation

Full documentation is available on http://jbake.org/docs/[jbake.org].

== Contributing

We welcome all contributions to the project both big and small. From new features, bug reports to even spelling mistake corrections in
the documentation. Please don't hesitate to submit an issue.

The link:CONTRIBUTING.asciidoc[Contributing] guide provides information on how to submit an issue or create a pull request to fix a bug or
add a new feature to JBake.

== Versioning

The project has adopted the http://semver.org[Semantic Versioning] spec from v2.2.0 onwards to maintain an
understandable backwards compatibility strategy.

The version format is as follows:

----
<major>.<minor>.<patch>-<label>
----

* An increment of the major version represents incompatible API changes.
* An increment of the minor version represents additional functionality in a backwards-compatible manner.
* An increment of the patch version represents backwards-compatible bug fixes.
* Existence of a label represents a pre-release or build metadata.

== Community

Talk to the developers behind JBake:

* http://groups.google.com/group/jbake-dev[Mailing list]
* link:irc://irc.freenode.net/#jbake[IRC - #jbake on Freenode]

Talk to other users of JBake on the forum:

* http://groups.google.com/group/jbake-user[Forum]

== Docker Image

The image uses the official https://hub.docker.com/r/adoptopenjdk/openjdk11/[adoptopenjdk/openjdk11:alpine] image
for building a distribution of JBake and
https://hub.docker.com/r/adoptopenjdk/openjdk11/[adoptopenjdk/openjdk11:alpine-jre] for runtime.

=== Build

To build the Docker image:

----
$ docker build -t jbake/jbake:latest .
----

=== Usage

To execute JBake via Docker run this from project directory:

----
$ docker run --rm -u jbake -v "$PWD":/mnt/site jbake/jbake:latest
----

This command will execute using the jbake user to avoid running as root and will mount the current working directory as `/mnt/site`
in the Docker container where JBake is expecting your project files to be. By default the Docker image will execute a bake `-b` only.

If you want to bake and serve your project using the Docker image then you'll need to override the default command:

----
$ docker run --rm -u jbake -v "$PWD":/mnt/site -p 8820:8820 jbake/jbake:latest -b -s
----

This command will also expose port 8820 from the container, you'll also need to set the following option in your `jbake.properties` file:

----
server.hostname=0.0.0.0
----

NOTE: Docker image timezone is _UTC_. This may affect the date and time expected in output content. To set different timezone, add `TZ` environment variable and set value to required https://en.wikipedia.org/wiki/List_of_tz_database_time_zones[timezone^]. Example - `docker run --rm -u jbake  -e TZ=America/New_York -v "$PWD":/mnt/site jbake/jbake:latest`


== Build System

The project uses http://gradle.org[Gradle] 4.9+ as the build system.
To build the JBake distribution ZIP file execute the following command from the root of the repo:

----
$ ./gradlew distZip
----

This will build a ZIP file in the `/build/distributions` folder.

For more information see link:BUILD.adoc[Test, Build and Deploy]

== Coding conventions

The project uses a basic set of http://checkstyle.sourceforge.net/[checkstyle] rules to keep the Code in shape.

We configured the gradle checkstyle Plugin to run with the `check` task.
It does not break the build if convention violations are found. But prints a warning and generates a report.

For more information see link:BUILD.adoc[Test, Build and Deploy]

=== Setup Intellij

* Install https://github.com/jshiell/checkstyle-idea[checkstyle-idea plugin]
+
Settings -> Plugins -> CheckStyle-IDEA

* Configure
+
Settings -> Other Settings -> Checksytle
+
Add a new _Configuration File_.
Enter a Description like "jbake Checkstyle" and choose "Use a local Checkstyle file".
The checkstyle File is located at the project root path `config/checkstyle/checkstyle.xml`

* Add to Editor Code Style Scheme
+
Settings -> Editor -> Code Style
+
Click the gear Symbol besides the "Scheme:" drop-down.
+
Import Scheme -> Checkstyle Configuration
+
Pick the project checkstyle file `config/checkstyle/checkstyle.xml`

== Tools & Libraries Used

* http://commons.apache.org/[Apache Commons IO, Configuration, Lang & Logging]
* http://args4j.kohsuke.org/[Args4j]
* http://asciidoctor.org/[AsciidoctorJ]
* http://freemarker.org/[Freemarker]
* http://gradle.org[Gradle]
* http://groovy-lang.org/[Groovy]
* http://junit.org/[JUnit]
* https://github.com/vsch/flexmark-java[Flexmark]
* http://www.eclipse.org/jetty/[Jetty Server]
* http://www.orientdb.org/[OrientDB]

== Copyright & License

Licensed under the MIT License, see the link:LICENSE[LICENSE] file for details.
