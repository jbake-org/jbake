package org.jbake.app.creator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MarkdownFileCreator implements FileCreator {

    private final Scanner scanner;

    public MarkdownFileCreator(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public List<String> obtainMetadata() {
        System.out.println("This will create a new file in the current directory.\n");
        System.out.println("Each metadata item may have a list of pre-defined options.\n"
                + "The starred option is the default.\n"
                + "If there is no starred option, this metadata item is optional.\n");

        System.out.print("Status: (draft* / published / published-date) ");
        String status = scanner.nextLine();

        System.out.print("Type: (post* / page) ");
        String type = scanner.nextLine();

        System.out.print("Title: ");
        String title = scanner.nextLine();

        System.out.print("Date: (today / yyyy-MM-dd) ");
        String date = scanner.nextLine();

        System.out.print("Tags: ");
        String tags = scanner.nextLine();

        FileMetadata fileMetadata = new FileMetadata(type, status);
        fileMetadata.setDate(date);
        fileMetadata.setTitle(title);
        fileMetadata.setTags(tags);

        ArrayList<String> metadata = new ArrayList<String>();

        if (fileMetadata.hasTitle()) {
            metadata.add("title=" + fileMetadata.getTitle());
        }
        if (fileMetadata.hasDate()) {
            metadata.add("date=" + fileMetadata.getDate());
        }
        metadata.add("type=" + fileMetadata.getType());
        if (fileMetadata.hasTags()) {
            metadata.add("tags=" + fileMetadata.getTags());
        }
        metadata.add("status=" + fileMetadata.getStatus());

        return metadata;
    }

    @Override
    public boolean accepts(String fileName) {
        return fileName.endsWith(".md");
    }

    @Override
    public String getSeparator() {
        return "~~~~~~";
    }
}
