#!/usr/bin/env php
<?php
/**
 * Simple Texy Service for JBake
 *
 * This is a simple HTTP service that processes Texy markup and returns HTML.
 * It can be used with JBake's TexyEngine.
 *
 * Requirements:
 * - PHP 7.4 or higher
 * - Texy library (install via composer: composer require texy/texy)
 *
 * Usage:
 *   php texy-service.php [port]
 *
 * Default port is 8080
 *
 * Example:
 *   php texy-service.php 8080
 *
 * The service will listen on http://localhost:8080/texy
 */

// Check if Texy is available
if (!file_exists(__DIR__ . '/vendor/autoload.php')) {
    die("Error: Texy library not found. Please run: composer require texy/texy\n");
}

require __DIR__ . '/vendor/autoload.php';

use Texy\Texy;

$port = isset($argv[1]) ? (int)$argv[1] : 8080;
$host = '0.0.0.0';

echo "Starting Texy service on http://$host:$port/texy\n";
echo "Press Ctrl+C to stop\n\n";

// Create socket
$socket = stream_socket_server("tcp://$host:$port", $errno, $errstr);
if (!$socket) {
    die("Error creating socket: $errstr ($errno)\n");
}

// Initialize Texy
$texy = new Texy();

// Main server loop
while ($conn = stream_socket_accept($socket, -1)) {
    $request = '';

    // Read the request
    while (!feof($conn)) {
        $chunk = fread($conn, 8192);
        $request .= $chunk;

        // Check if we've received the full headers
        if (strpos($request, "\r\n\r\n") !== false) {
            break;
        }
    }

    // Parse HTTP request
    $lines = explode("\r\n", $request);
    $firstLine = $lines[0];
    list($method, $path, $protocol) = explode(' ', $firstLine);

    // Find Content-Length header
    $contentLength = 0;
    foreach ($lines as $line) {
        if (preg_match('/^Content-Length:\s*(\d+)$/i', $line, $matches)) {
            $contentLength = (int)$matches[1];
            break;
        }
    }

    // Read body if present
    $body = '';
    if ($contentLength > 0) {
        $headerEnd = strpos($request, "\r\n\r\n") + 4;
        $body = substr($request, $headerEnd);

        // Read remaining body if needed
        while (strlen($body) < $contentLength && !feof($conn)) {
            $body .= fread($conn, $contentLength - strlen($body));
        }
    }

    // Log request
    $timestamp = date('Y-m-d H:i:s');
    echo "[$timestamp] $method $path - Content-Length: $contentLength bytes\n";

    // Handle request
    if ($method === 'POST' && $path === '/texy') {
        try {
            // Process Texy markup
            $html = $texy->process($body);

            // Send response
            $response = "HTTP/1.1 200 OK\r\n";
            $response .= "Content-Type: text/html; charset=UTF-8\r\n";
            $response .= "Content-Length: " . strlen($html) . "\r\n";
            $response .= "Connection: close\r\n";
            $response .= "\r\n";
            $response .= $html;

            fwrite($conn, $response);
            echo "[$timestamp] Response sent: " . strlen($html) . " bytes\n";
        } catch (Exception $e) {
            // Send error response
            $errorMsg = "Error processing Texy markup: " . $e->getMessage();
            $response = "HTTP/1.1 500 Internal Server Error\r\n";
            $response .= "Content-Type: text/plain; charset=UTF-8\r\n";
            $response .= "Content-Length: " . strlen($errorMsg) . "\r\n";
            $response .= "Connection: close\r\n";
            $response .= "\r\n";
            $response .= $errorMsg;

            fwrite($conn, $response);
            echo "[$timestamp] Error: {$e->getMessage()}\n";
        }
    } elseif ($method === 'GET' && $path === '/') {
        // Health check endpoint
        $msg = "Texy Service is running\n";
        $response = "HTTP/1.1 200 OK\r\n";
        $response .= "Content-Type: text/plain; charset=UTF-8\r\n";
        $response .= "Content-Length: " . strlen($msg) . "\r\n";
        $response .= "Connection: close\r\n";
        $response .= "\r\n";
        $response .= $msg;

        fwrite($conn, $response);
    } else {
        // 404 Not Found
        $msg = "Not Found\n";
        $response = "HTTP/1.1 404 Not Found\r\n";
        $response .= "Content-Type: text/plain; charset=UTF-8\r\n";
        $response .= "Content-Length: " . strlen($msg) . "\r\n";
        $response .= "Connection: close\r\n";
        $response .= "\r\n";
        $response .= $msg;

        fwrite($conn, $response);
    }

    fclose($conn);
}

fclose($socket);

