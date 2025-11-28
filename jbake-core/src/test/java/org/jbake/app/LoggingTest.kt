package org.jbake.app

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.Appender
import io.mockk.mockk
import io.mockk.slot
import org.slf4j.LoggerFactory

abstract class LoggingTest {

    protected lateinit var mockAppender: Appender<ILoggingEvent>
    protected val captorLoggingEvent = slot<LoggingEvent>()
    protected lateinit var root: Logger

    protected fun setupBase() {
        mockAppender = mockk(relaxed = true)
        root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger

        root.addAppender(mockAppender)
        root.level = Level.INFO
    }

    protected fun teardownBase() {
        root.detachAppender(mockAppender)
    }
}
