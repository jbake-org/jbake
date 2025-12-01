# Releasing JBake to Maven Central

This document describes how to release JBake artifacts to Maven Central under the `ch.zizka.jbake` groupId.

## Quick Start

```bash
export JAVA_HOME=.../temurin-17.0.17
export PATH=$JAVA_HOME/bin:$PATH
export GPG_TTY=$(tty)
cd /home/o/uw/jbake
mvn clean deploy -Prelease
```

Or use the deployment script:
```bash
cd /home/o/uw/jbake
./deploy-to-central.sh
```

## Prerequisites

### 1. Java 17

JBake requires Java 17 for building:

```bash
export JAVA_HOME=.../temurin-17.0.17
export PATH=$JAVA_HOME/bin:$PATH
java -version  # Verify it's Java 17
```

### 2. Maven Central Portal Account

- Portal: https://central.sonatype.com/
- Login with your OSSRH JIRA credentials
- Ensure `ch.zizka` namespace is verified

**Note**: The old OSSRH service (s01.oss.sonatype.org) was shut down on June 30, 2025. We now use the Maven Central Portal.

### 3. Maven Settings (~/.m2/settings.xml)

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>YOUR_TOKEN_USERNAME</username>
      <password>YOUR_TOKEN_PASSWORD</password>
    </server>
  </servers>

  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase></gpg.passphrase>  <!-- Optional: add if key has passphrase -->
      </properties>
    </profile>
  </profiles>
</settings>
```

To get fresh credentials:
1. Go to https://central.sonatype.com/account
2. Click "Generate User Token"
3. Copy the credentials to settings.xml

### 4. GPG Key

Your GPG key must be uploaded to public keyservers.

#### If you already have a key at /home/o/.ssh/OSSRH_gpg.key:

```bash
cd /home/o/uw/jbake
./import-gpg-key.sh
```

#### To generate a new key:

```bash
gpg --full-generate-key
# Key type: RSA and RSA (default)
# Key size: 4096
# Expiration: 0 (never expires)
# Name: Ondrej Zizka
# Email: zizka@seznam.cz
# Passphrase: (optional, can be empty)
```

#### Upload public key to keyservers:

```bash
# Get your key ID
gpg --list-secret-keys --keyid-format=long

# Upload (replace KEY_ID with actual ID)
gpg --keyserver keys.openpgp.org --send-keys KEY_ID
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
```

## Deployment Methods

### Method 1: Direct Deployment (Recommended)

```bash
export JAVA_HOME=/home/o/.jdks/temurin-17.0.17
export PATH=$JAVA_HOME/bin:$PATH
export GPG_TTY=$(tty)
cd /home/o/uw/jbake
mvn clean deploy -Prelease
```

This will:
1. Build all modules
2. Generate sources and javadoc JARs (using Dokka for Kotlin code)
3. Sign all artifacts with GPG
4. Upload to Maven Central Portal
5. Automatically validate and publish (autoPublish=true)

### Method 2: Using Deployment Script

```bash
cd /home/o/uw/jbake
./deploy-to-central.sh
```

The script performs the same steps with interactive prompts and validation.

### Method 3: Using maven-release-plugin

For version management and Git tagging:

```bash
export JAVA_HOME=/home/o/.jdks/temurin-17.0.17
export PATH=$JAVA_HOME/bin:$PATH
export GPG_TTY=$(tty)
cd /home/o/uw/jbake

# Prepare release (updates versions, creates Git tag)
mvn release:prepare -Prelease

# Perform release (builds and deploys)
mvn release:perform -Prelease
```

## After Deployment

### Timeline

- **Upload**: Immediate
- **Validation**: 1-5 minutes
- **Maven Central**: 15-30 minutes
- **Search Index**: 2-24 hours

### Verification

1. **Central Portal** (immediate):
   https://central.sonatype.com/publishing

2. **Maven Central Repository** (15-30 min):
   https://repo1.maven.org/maven2/ch/zizka/jbake/

3. **Maven Search** (few hours):
   https://search.maven.org/search?q=g:ch.zizka.jbake

## Modules Published

- `ch.zizka.jbake:jbake-base` - Parent POM
- `ch.zizka.jbake:jbake-core` - Core library (with Kotlin KDoc documentation)
- `ch.zizka.jbake:jbake-dist` - Distribution JAR
- `ch.zizka.jbake:jbake-maven-plugin` - Maven plugin

Note: `jbake-e2e-tests` is not deployed (has `maven.deploy.skip=true`)

## Project Configuration

### Maven Central Portal

The project uses the new Maven Central Portal (OSSRH was shut down June 30, 2025):

- **Portal URL**: https://central.sonatype.com/
- **Plugin**: `central-publishing-maven-plugin` 0.9.0
- **Server ID**: `central`
- **Auto-publish**: Enabled (artifacts automatically released after validation)

### Kotlin Documentation

The project uses Dokka to generate Javadoc-compatible documentation from Kotlin code:

- **Plugin**: `dokka-maven-plugin` 2.1.0
- **Format**: Javadoc (compatible with Maven Central)
- **Generated**: Real HTML documentation from KDoc comments

### GPG Signing

All artifacts are signed with GPG in the release profile:

- Configured with `--pinentry-mode loopback`
- Requires GPG_TTY environment variable
- Public keys must be on keyservers

## Troubleshooting

### Error: 401 Unauthorized

Your Maven Central Portal credentials are incorrect or expired.

**Solution**: Generate fresh token from https://central.sonatype.com/account

### Error: GPG signing failed: No secret key

Your GPG key is not imported.

**Solution**:
```bash
# Import existing key
cd /home/o/uw/jbake && ./import-gpg-key.sh

# Or generate new key
gpg --full-generate-key
```

### Error: GPG signing failed: Inappropriate ioctl for device

GPG_TTY is not set.

**Solution**:
```bash
export GPG_TTY=$(tty)
```

### Error: Invalid signature - Could not find public key

Your GPG public key is not on keyservers.

**Solution**:
```bash
# Get key ID
gpg --list-secret-keys --keyid-format=long

# Upload to keyservers
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### Error: Javadocs/Sources must be provided

Missing javadoc or sources JARs.

**Solution**: This should be automatic with the current configuration. Dokka generates Kotlin documentation. If issues persist, rebuild:
```bash
mvn clean package -Prelease -DskipTests
```

### Error: Namespace not verified

Your account doesn't have access to `ch.zizka.*` namespace.

**Solution**: Contact central-support@sonatype.com

## Manual Review Before Publishing

If you want to review artifacts before they're released to Maven Central:

1. Edit `pom.xml` and change:
   ```xml
   <autoPublish>false</autoPublish>
   ```

2. Deploy:
   ```bash
   mvn clean deploy -Prelease
   ```

3. Review at: https://central.sonatype.com/publishing

4. Click "Publish" to release to Maven Central


## Maven Central Requirements

All requirements are met by the current configuration:

- ✅ **POM Metadata**: name, description, url, licenses, developers, scm
- ✅ **Sources JAR**: maven-source-plugin configured
- ✅ **Javadoc JAR**: Dokka generates from Kotlin code
- ✅ **GPG Signatures**: maven-gpg-plugin in release profile
- ✅ **Valid Coordinates**: ch.zizka.jbake namespace verified

## Helper Scripts

- `deploy-to-central.sh` - Interactive deployment script
- `import-gpg-key.sh` - Import existing GPG key
- `setup-gpg-key.sh` - Generate new GPG key

## Reference

- Maven Central Portal: https://central.sonatype.com/
- Portal Documentation: https://central.sonatype.org/publish/publish-portal-maven/
- OSSRH EOL Notice: https://central.sonatype.org/pages/ossrh-eol/
- Dokka Documentation: https://kotlinlang.org/docs/dokka-introduction.html

---

**Last Updated**: December 1, 2025

