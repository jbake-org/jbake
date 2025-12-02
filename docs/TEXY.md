# Texy Markup Engine Support

JBake now supports [Texy](https://texy.info/), a markup language from the Czech Republic that converts plain text to XHTML/HTML5.

## Overview

The Texy engine allows JBake to process `.texy` files by communicating with a Texy service via HTTP. This approach keeps JBake lightweight while allowing support for various markup formats through external services.

## Requirements

The Texy engine requires a running Texy service that accepts HTTP POST requests. You have several options:

1. **Docker** (Recommended):
   ```bash
   docker run -d -p 8080:8080 --name texy-service <texy-docker-image>
   ```

2. **Standalone Service**:
   You can run a custom Texy service implementation that exposes an HTTP endpoint.

3. **Custom Implementation**:
   Implement your own Texy service that accepts POST requests with Texy markup and returns HTML.

## Configuration

Add the following configuration to your `jbake.properties` file:

```properties
# Texy service URL (default: http://localhost:8080/texy)
texy.service.url=http://localhost:8080/texy

# Connection timeout in milliseconds (default: 5000)
texy.connection.timeout=5000

# Read timeout in milliseconds (default: 10000)
texy.read.timeout=10000
```

## Usage

### Creating a Texy Document

Create a file with the `.texy` extension in your content directory:

**example.texy**:
```
title=My Texy Document
status=published
type=post
date=2025-12-01
tags=texy,markup
~~~~~~

Heading
=======

This is a **bold** text and this is an //italic// text.

- First item
- Second item
- Third item

Links
-----

"Link text":http://example.com

Images
------

[* image.jpg *]

Tables
------

|----
| Name | Age
|----
| John | 25
| Jane | 30
|----
```

### Texy Markup Syntax

Texy supports various markup features:

- **Bold**: `**text**` or `*text*`
- **Italic**: `//text//` or `/text/`
- **Links**: `"link text":http://url` or `[link text|http://url]`
- **Images**: `[* image.jpg *]` or `[* image.jpg >*]` (with alignment)
- **Lists**: Lines starting with `-` or `*` (unordered) or numbers (ordered)
- **Tables**: Using `|----` delimiters
- **Headings**:
  - `Heading\n=======` (level 1)
  - `Heading\n-------` (level 2)
  - Or `#Heading` style

For complete Texy syntax, visit [Texy Documentation](https://texy.info/en/).

## Service API

The Texy service must implement the following API:

**Endpoint**: `POST /texy` (or your configured URL)

**Request**:
- Content-Type: `text/plain; charset=UTF-8`
- Body: Raw Texy markup text

**Response**:
- Content-Type: `text/html; charset=UTF-8`
- Body: Rendered HTML
- Status: `200 OK` on success

**Example Service Implementation** (pseudocode):
```
POST /texy
Accept: text/html
Content-Type: text/plain

Input: Texy markup text
Output: Rendered HTML
```

## Docker Service Module

A complete Maven module for the Texy service is provided in `jbake-texy-service/`:
- Full Maven build integration
- Automated Docker image building
- End-to-end tests with TestContainers
- JBake integration tests
- Docker Hub push capabilities

### Quick Start with Maven

```bash
# Build the Docker image
cd jbake-texy-service/
mvn package

# Run the service
docker run -d -p 8080:8080 --name texy jbake/texy-service:latest

# Test the service
curl -X POST http://localhost:8080/texy \
  -H "Content-Type: text/plain" \
  -d "This is **bold** and this is //italic//"

# Run tests (includes Docker image build and container tests)
mvn test

# Stop the service
docker stop texy && docker rm texy
```

### Quick Start with Gradle

```bash
# Build the Docker image
cd jbake-texy-service/
./gradlew assemble

# Run the service
docker run -d -p 8080:8080 --name texy jbake/texy-service:latest

# Run tests (includes Docker image build and container tests)
./gradlew test

# Stop the service
docker stop texy && docker rm texy
```

### Building and Running with Docker Directly

```bash
# Build from source
cd jbake-texy-service/
docker build -t texy-service .

# Or pull from Docker Hub (once published)
docker pull jbake/texy-service:latest

# Run the service
docker run -d -p 8080:8080 --name texy jbake/texy-service:latest
```

The service will be available at `http://localhost:8080/texy`

## Architecture

The implementation follows JBake's existing pattern for markup engines:

1. **Engine Registration**: The `TexyEngine` class is registered in `MarkupEngines.properties`
2. **File Processing**: JBake routes `.texy` files to `TexyEngine`
3. **Header Parsing**: Standard JBake header parsing (title, status, type, date, etc.)
4. **Body Rendering**: HTTP POST request to Texy service with markup content
5. **Error Handling**: Graceful degradation if service is unavailable

## Key Features

- ✅ HTTP-based service communication (Docker or standalone)
- ✅ Configurable timeouts and service URL
- ✅ Full Texy markup support via external service
- ✅ Standard JBake header metadata parsing
- ✅ Graceful error handling
- ✅ Written in Kotlin
- ✅ Follows existing JBake patterns
- ✅ Comprehensive tests
- ✅ Complete documentation

## Troubleshooting

### Service Not Available

If the Texy service is not available, the engine will wrap the error message and original content in a `<pre>` tag. Check:

1. Is the Texy service running?
   ```bash
   curl -X POST http://localhost:8080/texy -d "test" -H "Content-Type: text/plain"
   ```


### Timeout Issues

If you're processing large documents, increase the timeout values:

```properties
texy.connection.timeout=10000
texy.read.timeout=30000
```

### Character Encoding

Ensure your Texy files are saved in UTF-8 encoding to avoid character issues.

## Performance Considerations

Since the Texy engine makes HTTP requests for each file, consider:

1. **Service Performance**: Ensure your Texy service is optimized and can handle multiple requests.
2. **Network Latency**: Run the Texy service locally or on the same network as JBake.
3. **Caching**: JBake's built-in caching will help avoid re-processing unchanged files.
4. **Batch Processing**: For large sites, consider processing in batches or optimizing your service.

## Credits

- **Texy**: Created by David Grudl (https://texy.info/)

## Links

- Texy Official Site: https://texy.info/
- Texy Documentation: https://texy.info/en/
- Texy GitHub: https://github.com/dg/texy
- JBake Website: https://jbake.org/
