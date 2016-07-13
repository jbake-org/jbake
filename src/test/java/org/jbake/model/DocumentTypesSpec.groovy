package org.jbake.model

class DocumentTypesSpec extends spock.lang.Specification {

    def "should return default document types"() {
        expect:
            DocumentTypes.getDocumentTypes().sort() == ["page", "post", "masterindex", "archive", "feed"].sort()
    }

    def "should add new document type"() {
        given:
            String newDocumentType = "newDocumentType"

        when:
            DocumentTypes.addDocumentType(newDocumentType)

        then:
            DocumentTypes.getDocumentTypes().contains(newDocumentType)
    }

    def "should add document type only once"() {
        given: "A a document type is already known"
            String knownDocumentType = "known"
            DocumentTypes.addDocumentType(knownDocumentType)

        when: "adding the known document type again"
            DocumentTypes.addDocumentType(knownDocumentType)

        then: "only one document type could be found in the list"
            String[] foundTypes = DocumentTypes.getDocumentTypes().findAll { docType ->  docType == knownDocumentType }
            foundTypes.size() == 1
    }

    def "should tell if document type is known"() {
        given:
            String knownDocumentType = "known"
            DocumentTypes.addDocumentType(knownDocumentType)

        expect:
            DocumentTypes.contains(knownDocumentType)
    }

    def "should tell if document type is unknown"() {
        given:
            String unknownType = "unknown"

        expect:
            ! DocumentTypes.contains(unknownType)
    }

    def "should notify listeners when new document type is added"() {
        given: "A DocumentTypeListener is added"
            String newDocumentType = "newDocumentType"
            DocumentTypeListener listener = Mock()
            DocumentTypes.addListener(listener)

        when: "a new document type added"
            DocumentTypes.addDocumentType(newDocumentType)

        then: "the listener was called with new document type"
            1 * listener.added(newDocumentType)
    }
}
