package org.jbake.app;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public abstract class LoggingTest {

    @Mock
    protected Appender<ILoggingEvent> mockAppender;

    @Captor
    protected ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    protected Logger root;

    @BeforeEach
    public void setupBase() {
        root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        root.addAppender(mockAppender);
        root.setLevel(Level.INFO);
    }

    @AfterEach
    public void teardownBase() {
        root.detachAppender(mockAppender);
    }


}
