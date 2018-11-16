package org.jbake.parser.texy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.IOUtils;

/**
 * Texy converter that data and calls the remote Texy converting REST service.
 * TODO: Could have a connection pool and could be parallelized.
 */
public class TexyRestService
{
    private final URL url;

    public TexyRestService(URL url)
    {
        this.url = url;
    }

    /**
     * Sends the given inputstream as a POST request to the configured Texy converted web service
     * and returns an InputStream of the response body, which is the resulting XHTML.
     */
    public InputStream convertTexyToXhtml(InputStream texyMarkupStream)
    {
        try {
            HttpURLConnection conn = (HttpURLConnection) this.url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/texy");
            //conn.setRequestProperty("Content-Length", ""+);

            conn.setDoInput(true);
            conn.setDoOutput(true);
            BufferedInputStream bis = new BufferedInputStream(texyMarkupStream);

            try (OutputStream requestBodyStream = conn.getOutputStream()) {
                IOUtils.copy(texyMarkupStream, requestBodyStream);
            }

            return conn.getInputStream();
        }
        catch (IOException ex) {
            throw new RuntimeException("Call to Texy conversion service failed: " + ex.getMessage(), ex);
        }
    }
}
