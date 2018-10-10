package org.jbake;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BinaryRunner {

    private File folder;

    public BinaryRunner(File folder) {
        this.folder = folder;
    }

    public Process runWithArguments(String... arguments) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(folder);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        printOutput(process.getInputStream());
        process.waitFor();

        return process;
    }

    private void printOutput(InputStream inputStream) throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        while ((line = reader.readLine()) != null ) {
            System.out.println(line);
        }
        reader.close();
    }

}
