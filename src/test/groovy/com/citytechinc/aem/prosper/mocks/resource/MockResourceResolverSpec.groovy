package com.citytechinc.aem.prosper.mocks.resource
import com.citytechinc.aem.prosper.specs.ProsperSpec
import org.apache.sling.api.resource.SyntheticResource

import javax.jcr.query.Query

class MockResourceResolverSpec extends ProsperSpec {

    def setupSpec() {
        nodeBuilder.content {
            one()
            two()
        }
    }

    def "get resource"() {
        expect:
        resourceResolver.getResource("/content").path == "/content"
    }

    def "get non-existent resource returns null"() {
        expect:
        !resourceResolver.getResource("/etc")
    }

    def "get resource for malformed path returns null"() {
        expect:
        !resourceResolver.getResource("//")
    }

    def "get resource with base resource"() {
        setup:
        def baseResource = resourceResolver.getResource("/content")

        expect:
        resourceResolver.getResource(baseResource, "one").path == "/content/one"
    }

    def "get resource with base resource and absolute path returns absolute path"() {
        setup:
        def baseResource = resourceResolver.getResource("/content")

        expect:
        resourceResolver.getResource(baseResource, "/content/one").path == "/content/one"
    }

    def "get resource with null base resource returns null"() {
        expect:
        !resourceResolver.getResource(null, "one")
    }

    def "get resource with null base resource and absolute path returns absolute path"() {
        expect:
        resourceResolver.getResource(null, "/content/one").path == "/content/one"
    }

    def "get resource with base resource and malformed path returns null"() {
        setup:
        def baseResource = resourceResolver.getResource("/content")

        expect:
        !resourceResolver.getResource(baseResource, "//")
    }

    def "resolve resource"() {
        expect:
        resourceResolver.resolve("/content").path == "/content"
    }

    def "resolve non-existing resource"() {
        expect:
        resourceResolver.resolve("/content/three") instanceof SyntheticResource
    }

    def "list children"() {
        setup:
        def resource = resourceResolver.getResource("/content")

        expect:
        resourceResolver.listChildren(resource).size() == 2
    }

    def "get children"() {
        setup:
        def resource = resourceResolver.getResource("/content")

        expect:
        resourceResolver.getChildren(resource).size() == 2
    }

    def "get search path"() {
        setup:
        resourceResolver.setSearchPath("/content/one")

        expect:
        resourceResolver.searchPath == ["/content/one"]
    }

    def "find resources using XPath"() {
        expect:
        resourceResolver.findResources("/jcr:root/content//*[jcr:primaryType='nt:unstructured']",
            Query.XPATH).size() == 2
    }

    def "is live after close"() {
        setup:
        resourceResolver.close()

        expect:
        !resourceResolver.live
    }
}
