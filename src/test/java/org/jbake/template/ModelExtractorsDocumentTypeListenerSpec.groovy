package org.jbake.template

import org.jbake.model.DocumentTypes
import spock.lang.Specification

class ModelExtractorsDocumentTypeListenerSpec extends Specification {

    def "should register extractors for custom type"() {
        given: "A document type is known"
            String newDocumentType = "project"
            DocumentTypes.addDocumentType(newDocumentType)
            ModelExtractorsDocumentTypeListener listener = new ModelExtractorsDocumentTypeListener()

        when: "the listener is called with that type"
            listener.added(newDocumentType)

        then: "an extractor is registered by pluralized type as key"
            ModelExtractors.getInstance().containsKey("projects")

        and: "an extractor for published types is registered"
            ModelExtractors.getInstance().containsKey("published_projects")
    }
}
