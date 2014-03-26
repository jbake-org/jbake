package org.jbake.app.creator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class MarkdownFileCreatorTest {

    @Test
    public void useAllCustomValues() throws Exception {
        MarkdownFileCreator fileCreator = new MarkdownFileCreator(new Scanner("published\npage\nMy Title\n2014-02-01\ntag1 tagTwo"));

        List<String> metadata = fileCreator.obtainMetadata();

        Assertions.assertThat(metadata).containsExactly("title=My Title", "date=2014-02-01", "type=page", "tags=tag1 tagTwo", "status=published");
    }

    @Test
    public void useAllDefaultValues() throws Exception {
        MarkdownFileCreator fileCreator = new MarkdownFileCreator(new Scanner("\n\n\n\n\n"));

        List<String> metadata = fileCreator.obtainMetadata();

        Assertions.assertThat(metadata).containsExactly("type=post", "status=draft");
    }

    @Test
    public void useTodayShortcut() throws Exception {
        MarkdownFileCreator fileCreator = new MarkdownFileCreator(new Scanner("\n\n\ntoday\n\n"));

        List<String> metadata = fileCreator.obtainMetadata();

        Assertions.assertThat(metadata).contains("date=" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }
}
