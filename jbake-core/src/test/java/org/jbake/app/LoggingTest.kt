package org.jbake.app

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.Appender
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.LoggerFactory

@ExtendWith(MockitoExtension::class)
abstract class LoggingTest {
    @Mock
    protected var mockAppender: Appender<ILoggingEvent?>? = null

    @Captor
    protected var captorLoggingEvent: ArgumentCaptor<LoggingEvent?>? = null

    protected var root: Logger? = null

    @BeforeEach
    fun setupBase() {
        root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger

        root!!.addAppender(mockAppender)
        root!!.setLevel(Level.INFO)
    }

    @AfterEach
    fun teardownBase() {
        root!!.detachAppender(mockAppender)
    }
}
