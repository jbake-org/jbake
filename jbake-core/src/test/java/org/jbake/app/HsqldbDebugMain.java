package org.jbake.app;

import org.jbake.model.DocumentModel;
import java.util.Date;

public class HsqldbDebugMain {
    public static void main(String[] args) {
        System.out.println("=== HSQLDB Debug Test ===");

        HsqldbContentRepository repo = new HsqldbContentRepository("memory", "test-debug");
        System.out.println("1. Created repository");

        repo.startup();
        System.out.println("2. Called startup()");

        DocumentModel doc = new DocumentModel();
        doc.setSourceUri("test.html");
        doc.setType("post");
        doc.setStatus("published");
        doc.setTitle("Test");
        doc.setDate(new Date());
        doc.setRendered(false);

        System.out.println("3. Created document");

        repo.addDocument(doc);
        System.out.println("4. Added document");

        long count = repo.getDocumentCount("post");
        System.out.println("5. Got count: " + count);

        if (count == 1) {
            System.out.println("SUCCESS!");
        } else {
            System.out.println("FAILURE! Expected 1 but got " + count);
        }

        repo.close();
    }
}

