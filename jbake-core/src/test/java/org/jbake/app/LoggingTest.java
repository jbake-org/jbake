package org.jbake.app;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public abstract class LoggingTest {

    @Mock
    protected Appender<ILoggingEvent> mockAppender;

    @Captor
    protected ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    protected Logger root;

    @Before
    public void setup() {
        root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        root.addAppender(mockAppender);
        root.setLevel(Level.INFO);
    }

    @After
    public void teardown() {
        root.detachAppender(mockAppender);
    }


}
