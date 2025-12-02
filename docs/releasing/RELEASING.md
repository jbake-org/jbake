# Releasing JBake to Maven Central

This document describes how to release JBake artifacts to Maven Central under the `ch.zizka.jbake` groupId.

It's under this groupId for this branch. If/when merged, it may be changed to `org.jbake`.


## Quick Start

```bash
export JAVA_HOME=$HOME/.jdks/temurin-17.0.17
export PATH=$JAVA_HOME/bin:$PATH
export GPG_TTY=$(tty)
mvn clean deploy -Prelease
```

Or use the deployment script:
```bash
./deploy-to-central.sh
```

## Prerequisites

### 1. Java 17

JBake requires Java 17 for building:

```bash
export JAVA_HOME=$HOME/.jdks/temurin-17.0.17
export PATH=$JAVA_HOME/bin:$PATH
java -version  # Verify it's Java 17
```

### 2. Maven Central Portal Account

- Portal: https://central.sonatype.com/
- Login with your OSSRH JIRA credentials
- Ensure you may publish to `ch.zizka`

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

#### If you already have a key at $HOME/.ssh/OSSRH_gpg.key:

```bash
./import-gpg-key.sh
```

#### To generate a new key:

```bash
gpg --full-generate-key
# Key type: RSA and RSA (default)
# Key size: 4096
# Expiration: 0 (never expires)
# Name: Ondrej Zizka
# Email: ...
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
export JAVA_HOME=$HOME/.jdks/temurin-17.0.17
export PATH=$JAVA_HOME/bin:$PATH
export GPG_TTY=$(tty)
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
./deploy-to-central.sh
```

The script performs the same steps with interactive prompts and validation.

### Method 3: Using maven-release-plugin

For version management and Git tagging:

```bash
export JAVA_HOME=$HOME/.jdks/temurin-17.0.17
export PATH=$JAVA_HOME/bin:$PATH
export GPG_TTY=$(tty)
mvn release:prepare -Prelease  ## Updates versions, creates Git tag.
mvn release:perform -Prelease  ## Builds and uploads.
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

### GPG Signing

All artifacts are signed with GPG in the release profile:

- Configured with `--pinentry-mode loopback`
- Requires GPG_TTY environment variable
- Public keys must be on keyservers

## Troubleshooting

### Error: GPG signing failed: No secret key

Your GPG key is not imported.

**Solution**:
```bash
./import-gpg-key.sh      ## Import existing key
gpg --full-generate-key  ## Or generate new key
```

### Error: GPG signing failed: Inappropriate ioctl for device

GPG_TTY is not set. Use `export GPG_TTY=$(tty)`.

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


## Manual Review Before Publishing

If you want to review artifacts before they're released to Maven Central:

1. Change in `pom.xml`: `<autoPublish>false</autoPublish>`
2. Deploy: `mvn clean deploy -Prelease`
3. Review at: https://central.sonatype.com/publishing
4. Click "Publish" to release to Maven Central

## Helper Scripts

- `deploy-to-central.sh` - Interactive deployment script
- `import-gpg-key.sh` - Import existing GPG key
- `setup-gpg-key.sh` - Generate new GPG key

## Reference

- Maven Central Portal: https://central.sonatype.com/
- Portal Documentation: https://central.sonatype.org/publish/publish-portal-maven/
- GPG Documentation: https://www.gnupg.org/documentation/S
