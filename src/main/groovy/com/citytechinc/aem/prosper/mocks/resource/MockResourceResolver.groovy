package com.citytechinc.aem.prosper.mocks.resource

import com.citytechinc.aem.prosper.mocks.adapter.TestAdaptable
import org.apache.sling.api.resource.NonExistingResource
import org.apache.sling.api.resource.Resource
import org.apache.sling.api.resource.ResourceResolver
import org.apache.sling.jcr.resource.JcrResourceUtil

import javax.jcr.Node
import javax.jcr.RepositoryException
import javax.jcr.Session
import javax.servlet.http.HttpServletRequest

@SuppressWarnings("deprecation")
class MockResourceResolver implements ResourceResolver, GroovyInterceptable, TestAdaptable {

    private final Session session

    private final def resourceResolverAdapters

    private final def resourceAdapters

    private final def adapterFactories

    private def searchPath

    private boolean closed

    MockResourceResolver(session, resourceResolverAdapters, resourceAdapters, adapterFactories) {
        this.session = session
        this.resourceResolverAdapters = resourceResolverAdapters
        this.resourceAdapters = resourceAdapters
        this.adapterFactories = adapterFactories
    }

    @Override
    void addResourceAdapter(Class adapterType, Closure closure) {
        resourceAdapters.put(adapterType, closure)
    }

    @Override
    void addResourceResolverAdapter(Class adapterType, Closure closure) {
        resourceResolverAdapters.put(adapterType, closure)
    }

    void setSearchPath(String... searchPath) {
        this.searchPath = searchPath
    }

    @Override
    def invokeMethod(String name, args) {
        if (["isLive", "close"].contains(name) || !closed) {
            return this.&"$name"(args)
        }

        throw new IllegalStateException("The resource resolver is closed.")
    }

    @Override
    Resource getResource(String path) {
        def resource = null

        try {
            if (session.nodeExists(path)) {
                resource = new MockResource(this, session.getNode(path), resourceAdapters, adapterFactories)
            }
        } catch (RepositoryException e) {
            // ignore
        }

        resource
    }

    @Override
    Resource getResource(Resource base, String path) {
        path?.startsWith("/") ? getResource(path) : base ? getResource("${base.path}/$path") : null
    }

    @Override
    ResourceResolver clone(Map<String, Object> authenticationInfo) {
        throw new UnsupportedOperationException()
    }

    @Override
    Iterator<Resource> findResources(String query, String language) {
        def resourceResults = JcrResourceUtil.query(session, query, language).nodes.collect() {
            getResource(it.path)
        }

        resourceResults.iterator()
    }

    @Override
    String[] getSearchPath() {
        searchPath
    }

    @Override
    Iterator<Resource> listChildren(Resource parent) {
        getChildren(parent).iterator()
    }

    @Override
    Iterable<Resource> getChildren(Resource parent) {
        parent.adaptTo(Node).nodes.collect {
            new MockResource(this, it, resourceAdapters, adapterFactories)
        } as Iterable<Resource>
    }

    @Override
    String map(String resourcePath) {
        resourcePath
    }

    @Override
    String map(HttpServletRequest request, String resourcePath) {
        throw new UnsupportedOperationException()
    }

    @Override
    Iterator<Map<String, Object>> queryResources(String query, String language) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean hasChildren(Resource resource) {
        resource.hasChildren()
    }

    @Override
    Resource resolve(HttpServletRequest request, String absPath) {
        resolve(absPath)
    }

    @Override
    Resource resolve(HttpServletRequest request) {
        throw new UnsupportedOperationException()
    }

    @Override
    Resource resolve(String absPath) {
        getResource(absPath) ?: new NonExistingResource(this, absPath)
    }

    @Override
    <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        def result = (AdapterType) adapterFactories.findResult {
            adapterFactory -> adapterFactory.getAdapter(this, type)
        }

        if (!result) {
            def adapter = resourceResolverAdapters.find { it.key == type }

            if (adapter) {
                result = (AdapterType) adapter.value.call(this)
            }
        }

        result
    }

    @Override
    boolean isLive() {
        !closed
    }

    @Override
    void close() {
        closed = true
    }

    @Override
    String getUserID() {
        throw new UnsupportedOperationException()
    }

    @Override
    Object getAttribute(String name) {
        throw new UnsupportedOperationException()
    }

    @Override
    Iterator<String> getAttributeNames() {
        throw new UnsupportedOperationException()
    }

    @Override
    void delete(Resource resource) {
        throw new UnsupportedOperationException()
    }

    @Override
    Resource create(Resource parent, String name, Map<String, Object> properties) {
        throw new UnsupportedOperationException()
    }

    @Override
    void revert() {
        throw new UnsupportedOperationException()
    }

    @Override
    void commit() {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean hasChanges() {
        throw new UnsupportedOperationException()
    }

    @Override
    String getParentResourceType(Resource resource) {
        throw new UnsupportedOperationException()
    }

    @Override
    String getParentResourceType(String resourceType) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean isResourceType(Resource resource, String resourceType) {
        resource.resourceType == resourceType
    }

    @Override
    void refresh() {
        throw new UnsupportedOperationException()
    }
}