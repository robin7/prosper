# Prosper

[CITYTECH, Inc.](http://www.citytechinc.com)

## Overview

Prosper is an integration testing library for AEM (Adobe CQ) projects using [Spock](http://spockframework.org/), a [Groovy](http://groovy.codehaus.org)-based testing framework notable for it's expressive specification language.  The library contains a base Spock specification using an in-memory repository for JCR session-based testing and also includes basic Sling request and resource implementations for testing interactions between CQ objects.

## Features

* Test AEM projects outside of an OSGi container in the standard Maven build lifecycle.
* Write test specifications in [Groovy](http://groovy.codehaus.org) using [Spock](http://spockframework.org/), a JUnit-based testing framework with an elegant syntax for writing tests more quickly and efficiently.
* Extends and augments the transient JCR implementation provided by the [Apache Sling Testing Tools](http://sling.apache.org/documentation/development/sling-testing-tools.html) to eliminate the need to deploy tests in OSGi bundles for most testing scenarios.
* While accepting the limitations of testing outside the container, provides minimal/mock implementations of Sling interfaces (e.g. `ResourceResolver`, `SlingHttpServletRequest`) to test common API usages.
* Utilizes Groovy builders from our [AEM Groovy Extension](https://github.com/Citytechinc/aem-groovy-extension) to provide a simple DSL for creating test content.
* Provides additional builders for Sling requests and responses to simplify setup of test cases.

## Requirements

* Maven 3.x
* Familiarity with Spock specification syntax (or see included tests for examples).

## Getting Started

1. Add Maven dependency to project `pom.xml`.

        <dependency>
            <groupId>com.citytechinc.aem.prosper</groupId>
            <artifactId>prosper</artifactId>
            <version>0.9.0</version>
            <scope>test</scope>
        </dependency>

2. Create a `src/test/groovy` directory in your project structure and add a Spock specification extending the base `AemSpec`.

        import com.citytechinc.aem.prosper.specs.AemSpec

        class ExampleSpec extends AemSpec {

            def setupSpec() {
                // use PageBuilder from base spec to create test content
                pageBuilder.content {
                    home("Home") {
                        "jcr:content"("sling:resourceType": "foundation/components/page")
                    }
                }
            }

            // basic content assertions provided
            def "home page exists"() {
                expect:
                assertNodeExists("/content/home")
            }

            // Node metaclass provided by AEM Groovy Extension simplifies JCR operations
            def "home page has expected resource type"() {
                setup:
                def contentNode = session.getNode("/content/home/jcr:content")

                expect:
                contentNode.get("sling:resourceType") == "foundation/components/page"
            }
        }

3. Configure Groovy compiler and Surefire plugin in Maven `pom.xml`.  Additional configurations details can be found [here](http://groovy.codehaus.org/Groovy-Eclipse+compiler+plugin+for+Maven).

        <build>
            <sourceDirectory>src/main/groovy</sourceDirectory>
            <testSourceDirectory>src/test/groovy</testSourceDirectory>
            <plugins>
                <plugin>
                    <groupId>org.codehaus.groovy</groupId>
                    <artifactId>groovy-eclipse-compiler</artifactId>
                    <version>2.8.0-01</version>
                    <extensions>true</extensions>
                    <dependencies>
                        <dependency>
                            <groupId>org.codehaus.groovy</groupId>
                            <artifactId>groovy-eclipse-batch</artifactId>
                            <version>2.1.8-01</version>
                        </dependency>
                    </dependencies>
                </plugin>
                <plugin>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.1</version>
                    <configuration>
                        <compilerId>groovy-eclipse-compiler</compilerId>
                        <source>1.6</source>
                        <target>1.6</target>
                        <encoding>utf-8</encoding>
                    </configuration>
                    <dependencies>
                        <dependency>
                            <groupId>org.codehaus.groovy</groupId>
                            <artifactId>groovy-eclipse-compiler</artifactId>
                            <version>2.8.0-01</version>
                        </dependency>
                        <dependency>
                            <groupId>org.codehaus.groovy</groupId>
                            <artifactId>groovy-eclipse-batch</artifactId>
                            <version>2.1.8-01</version>
                        </dependency>
                    </dependencies>
                </plugin>
                <plugin>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>2.16</version>
                    <configuration>
                        <includes>
                            <include>**/*Spec*</include>
                        </includes>
                    </configuration>
                </plugin>
            </plugins>
        </build>

4. Run `mvn test` from the command line to verify that specifications are found and execute successfully.

## User Guide

### Specification Anatomy

The [Spock documentation](https://code.google.com/p/spock/wiki/SpockBasics) outlines the features and methods that define a Spock specification (and their JUnit analogues, for those more familiar with Java-based testing), but the `setupSpec()` fixture method is of critical importance when testing AEM classes.  This method, executed prior to the first feature method of the specification, is the conventional location for creating test content in the JCR.  Likewise, the `cleanup` and `cleanupSpec()` fixture methods are the appropriate place to remove test content following the execution of a test (or set of tests).  However, the `cleanupSpec()` method will be implemented less frequently, as the base `ProsperSpec` removes all test content from the JCR after every specification is executed to prevent cross-contamination of content between specifications.

    class ExampleSpec extends ProsperSpec {

        def setupSpec() {
            // create test content
        }

        def setup() {
            // less commonly used
        }

        def "a feature method"() {
            setup:
            // create test-specific content, instances and/or mocks

            expect:
            // stimulus/response
        }

        def "another feature method"() {
            setup:
            // create test content, etc.

            when:
            // stimulus

            then:
            // response
        }

        def cleanup() {
            // optionally call the method below to remove all test content after each feature method
            removeAllNodes()
        }

        def cleanupSpec() {
            // less commonly used
        }
    }

### Content Builders

A test specification will often require content such as pages, components, or supporting node structures to facilitate the interactions of the class under test.  Creating a large and/or complex content hierarchy using the JCR and Sling APIs can be tedious and time consuming.  The base `ProsperSpec` simplifies the content creation process by defining two Groovy [builder](http://groovy.codehaus.org/Builders) instances, `pageBuilder` and `nodeBuilder`, that greatly reduce the amount of code needed to produce a working content structure in the JCR.

#### Page Builder

`PageBuilder` creates nodes of type `cq:Page` by default.

    def setupSpec() {
        pageBuilder.content {
            beer { // page with no title
                styles("Styles") { // create page with title "Styles"
                    "jcr:content"("jcr:lastModifiedBy": "mdaugherty", "jcr:lastModified": Calendar.instance) {
                        data("sling:Folder")  // descendant of "jcr:content", argument sets node type instead of title
                        navigation("sling:resourceType: "components/navigation", "rootPath": "/content/beer")
                    }
                    dubbel("Dubbel")
                    tripel("Tripel")
                    saison("Saison")
                }
                // create a page with title "Breweries" and set properties on it's "jcr:content" node
                breweries("Breweries", "jcr:lastModifiedBy": "mdaugherty", "jcr:lastModified": Calendar.instance)
            }
        }
    }

Nodes named `jcr:content`, however, are treated as unstructured nodes to allow the creation of descendant component/data nodes that are not of type `cq:Page`.  Descendants of `jcr:content` nodes can specify a node type using a `String` value as the first argument in the tree syntax (see `/content/beer/styles/jcr:content/data` in the above example).  As with page nodes, additional properties can be passed with a map argument.

#### Node Builder

This builder should be used when creating non-page content hierarchies, such as descendants of `/etc` in the JCR.  The syntax is similar to `PageBuilder`, but the first argument is used to specify the node type rather than the `jcr:title` property.

    def setupSpec() {
        nodeBuilder.etc { // unstructured node
            designs("sling:Folder") { // node with type
                site("sling:Folder", "jcr:title": "Site", "inceptionYear": 2014) // node with type and properties
            }
        }
    }

The above example will create an `nt:unstructured` (the default type) node at `/etc` and `sling:Folder` nodes at `/etc/designs` and `/etc/designs/site`.  An additional map argument will set properties on the target node in the same manner as `PageBuilder`.

Both builders automatically save the underlying JCR session after executing the provided closure.

In addition to the provided builders, the [session](http://www.day.com/maven/jsr170/javadocs/jcr-2.0/javax/jcr/Session.html) and [pageManager](http://dev.day.com/content/docs/en/cq/current/javadoc/com/day/cq/wcm/api/PageManager.html) instances provided by the base specification can be used directly to create test content in the JCR.

### Mocking Requests and Responses

Testing servlets and request-scoped POJOs require mocking the `SlingHttpServletRequest` and `SlingHttpServletResponse` objects.  The `RequestBuilder` and `ResponseBuilder` instances acquired through the `ProsperSpec` leverage Groovy closures to set the necessary properties and state on these mock objects in a lightweight manner.

### Adding Sling Adapters

Specs can add adapters by adding `AdapterFactory` instances or by providing mappings from adapter instances to closures that instantiate these instances from either a `Resource` or a `ResourceResolver`.  The methods for adding adapters are illustrated in the examples below.

    class ExampleAdapterFactory implements AdapterFactory {

        @Override
        public <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> type) {
            AdapterType result = null

            if (type == String) {
                if (adaptable instanceof ResourceResolver) {
                    result = "Hello."
                } else if (adaptable instanceof Resource) {
                    result = "Goodbye."
                }
            }

            result
        }
    }

    class ExampleSpec extends ProsperSpec {

        @Override
        Collection<AdapterFactory> addAdapterFactories() {
            [new ExampleAdapterFactory(), new OtherAdapterFactory()]
        }

        @Override
        Map<Class, Closure> addResourceAdapters() {
            def adapters = [:]

            // key is adapter type, value is closure that returns adapter instance from resource argument
            adapters[Integer] = { Resource resource -> resource.name.length() }
            adapters[Map] = { Resource resource -> resource.resourceMetadata }

            adapters
        }

        @Override
        Map<Class, Closure> addResourceResolverAdapters() {
            def adapters = [:]

            // key is adapter type, value is closure that returns adapter instance from resource resolver argument
            adapters[Integer] = { ResourceResolver resourceResolver -> resourceResolver.searchPath.length }
            adapters[Node] = { ResourceResolver resourceResolver -> resourceResolver.getResource("/").adaptTo(Node) }

            adapters
        }

        def "resource is adaptable to multiple types"() {
            setup:
            def resource = resourceResolver.getResource("/")

            expect:
            resource.adaptTo(Integer) == 0
            resource.adaptTo(Map).size() == 0
        }

        def "resource resolver is adaptable to multiple types"() {
            expect:
            resourceResolver.adaptTo(String) == "Hello."
            resourceResolver.adaptTo(Integer) == 0
            resourceResolver.adaptTo(Node).path == "/"
        }
    }

### Mocking Services

OSGi services can be mocked (fully or partially) using Spock's mocking API.  Classes that inject services using Felix SCR annotations (as in the example servlet below) should use `protected` visibility to allow setting of service fields to mocked instances during testing.

    import com.day.cq.replication.ReplicationActionType
    import com.day.cq.replication.ReplicationException
    import com.day.cq.replication.Replicator
    import groovy.util.logging.Slf4j
    import org.apache.felix.scr.annotations.Reference
    import org.apache.felix.scr.annotations.sling.SlingServlet
    import org.apache.sling.api.SlingHttpServletRequest
    import org.apache.sling.api.SlingHttpServletResponse

    import javax.jcr.Session
    import javax.servlet.ServletException

    @SlingServlet(paths = "/bin/replicate/custom")
    @Slf4j("LOG")
    class CustomReplicationServlet extends SlingAllMethodsServlet {

        @Reference
        protected Replicator replicator

        @Override
        protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws
            ServletException, IOException {
            def path = request.getParameter("path")
    		def session = request.resourceResolver.adaptTo(Session)

    		try {
                replicator.replicate(session, ReplicationActionType.ACTIVATE, path)
            } catch (ReplicationException e) {
                LOG.error "replication error", e
            }
        }
    }

The Prosper specification for this servlet can then set a mocked `Replicator` and verify the expected [interactions](http://docs.spockframework.org/en/latest/interaction_based_testing.html) using the Spock  syntax.

    def "servlet with mock service"() {
        setup:
        def servlet = new CustomReplicationServlet()
        def replicator = Mock(Replicator)

        servlet.replicator = replicator

        def request = requestBuilder.build {
            parameters = [path: "/content"]
        }
        def response = responseBuilder.build()

        when:
        servlet.doPost(request, response)

        then:
        1 * replicator.replicate(_, _, "/content")
    }

### Assertions

### GroovyDocs

### Testing Scenarios

#### Servlets

#### OSGi Services

#### Tag Libraries

## Versioning

Follows [Semantic Versioning](http://semver.org/) guidelines.