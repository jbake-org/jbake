package org.jbake.template

import org.jbake.model.DocumentTypes
import spock.lang.Specification

class ModelExtractorsSpec extends Specification {

    def "should load extractors on instantiation"() {
        when:
            ModelExtractors.getInstance()

        then:
            ModelExtractors.getInstance().containsKey( aKey )

        where:
            aKey << ['pages',
                    'posts',
                    'indexs',
                    'archives',
                    'feeds',
                    'published_posts',
                    'published_pages',
                    'published_content',
                    'published_date',
                    'all_content',
                    'alltags',
                    'db',
                    'tag_posts']

    }

    def "should register extractors only for custom types"() {
        given:
            String knownDocumentType = "alltag"
            DocumentTypes.addDocumentType(knownDocumentType)

        when:
            ModelExtractors.getInstance().registerExtractorsForCustomTypes(knownDocumentType)

        then:
            ! ModelExtractors.getInstance().containsKey("published_alltags")
    }

    def "should register extractors for custom type"() {
        given: "A document type is known"
            String newDocumentType = "project"
            DocumentTypes.addDocumentType(newDocumentType)

        when: "we register extractors for the new type"
            ModelExtractors.getInstance().registerExtractorsForCustomTypes(newDocumentType)

        then: "an extractor is registered by pluralized type as key"
            ModelExtractors.getInstance().containsKey("projects")

        and: "an extractor for published types is registered"
            ModelExtractors.getInstance().containsKey("published_projects")
    }

    def "should throw an exception if document type is unknown"() {
        given:
            String unknownDocumentType = "unknown"

        when:
            ModelExtractors.getInstance().registerExtractorsForCustomTypes(unknownDocumentType)

        then:
            thrown(UnsupportedOperationException)
    }
}
