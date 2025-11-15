package org.jbake.launcher;

import org.jbake.TestUtils;
import org.jbake.app.configuration.JBakeConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JettyServerTest {

    @Mock
    JBakeConfiguration jBakeConfiguration;

    @Test
    void shouldRunWithCustomPortAndContext(@TempDir Path output) throws Exception {
        File out = output.resolve("build/jbake").toFile();
        out.mkdirs();

        File source = TestUtils.getTestResourcesAsSourceFolder();
        int port = getRandoport();
        when(jBakeConfiguration.getServerPort()).thenReturn(port);
        when(jBakeConfiguration.getServerHostname()).thenReturn("localhost");
        when(jBakeConfiguration.getServerContextPath()).thenReturn("/foo");

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try(JettyServer server = new JettyServer()) {

            executorService.execute(()->server.run(source.getAbsolutePath(), jBakeConfiguration));

            while (!server.isStarted()) {
                System.out.println("waiting until jetty is running");
                Thread.sleep(100);
            }

            URL url = new URL("http://localhost:"+port+"/foo/content/about.html");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            assertThat(in.readLine()).isEqualTo("title=About");
        }
    }

    private int getRandoport() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)){
            return socket.getLocalPort();
        }
    }
}
