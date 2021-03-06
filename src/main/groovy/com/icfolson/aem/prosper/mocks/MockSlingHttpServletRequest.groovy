package com.icfolson.aem.prosper.mocks

import com.icfolson.aem.prosper.mocks.request.MockRequestPathInfo
import com.icfolson.aem.prosper.mocks.request.MockRequestParameterMap
import groovy.transform.ToString
import org.apache.sling.api.SlingHttpServletRequest
import org.apache.sling.api.adapter.SlingAdaptable
import org.apache.sling.api.request.RequestDispatcherOptions
import org.apache.sling.api.request.RequestParameter
import org.apache.sling.api.request.RequestParameterMap
import org.apache.sling.api.request.RequestPathInfo
import org.apache.sling.api.request.RequestProgressTracker
import org.apache.sling.api.resource.Resource
import org.apache.sling.api.resource.ResourceResolver
import org.springframework.mock.web.MockHttpServletRequest

import javax.servlet.RequestDispatcher
import javax.servlet.http.Cookie

/**
 * Mock Sling request that delegates to a Spring <code>MockHttpServletRequest</code>.  This class should not be used
 * directly; rather, use a <code>RequestBuilder</code> instance from test specs to instantiate mock requests.
 */
@ToString(includes = ["resource", "requestPathInfo", "requestParameterMap"])
class MockSlingHttpServletRequest extends SlingAdaptable implements SlingHttpServletRequest {

    @Delegate
    private final MockHttpServletRequest mockRequest

    private final ResourceResolver resourceResolver

    private final Resource resource

    private final RequestParameterMap requestParameterMap

    private final RequestPathInfo requestPathInfo

    MockSlingHttpServletRequest(MockHttpServletRequest mockRequest, ResourceResolver resourceResolver, String path,
        List<String> selectors, String extension, String suffix) {
        this.mockRequest = mockRequest
        this.resourceResolver = resourceResolver

        resource = resourceResolver.resolve(path)
        requestParameterMap = MockRequestParameterMap.create(mockRequest)
        requestPathInfo = new MockRequestPathInfo(resourceResolver, path, selectors, extension, suffix)
    }

    @Override
    Resource getResource() {
        resource
    }

    @Override
    ResourceResolver getResourceResolver() {
        resourceResolver
    }

    @Override
    RequestPathInfo getRequestPathInfo() {
        requestPathInfo
    }

    @Override
    RequestParameter getRequestParameter(String name) {
        requestParameterMap.getValue(name)
    }

    @Override
    RequestParameter[] getRequestParameters(String name) {
        requestParameterMap.getValues(name)
    }

    @Override
    RequestParameterMap getRequestParameterMap() {
        requestParameterMap
    }

    @Override
    List<RequestParameter> getRequestParameterList() {
        def result = []

        requestParameterMap.values().each { requestParameterArray ->
            result.addAll(requestParameterArray as List)
        }

        result
    }

    @Override
    RequestDispatcher getRequestDispatcher(String path, RequestDispatcherOptions options) {
        throw new UnsupportedOperationException()
    }

    @Override
    RequestDispatcher getRequestDispatcher(Resource resource, RequestDispatcherOptions options) {
        throw new UnsupportedOperationException()
    }

    @Override
    RequestDispatcher getRequestDispatcher(Resource resource) {
        throw new UnsupportedOperationException()
    }

    @Override
    Cookie getCookie(String name) {
        cookies.find { it.name == name }
    }

    @Override
    String getResponseContentType() {
        throw new UnsupportedOperationException()
    }

    @Override
    Enumeration<String> getResponseContentTypes() {
        throw new UnsupportedOperationException()
    }

    @Override
    ResourceBundle getResourceBundle(Locale locale) {
        throw new UnsupportedOperationException()
    }

    @Override
    ResourceBundle getResourceBundle(String baseName, Locale locale) {
        throw new UnsupportedOperationException()
    }

    @Override
    RequestProgressTracker getRequestProgressTracker() {
        throw new UnsupportedOperationException()
    }

    @Override
    String getQueryString() {
        // check for overridden query string
        def queryString = mockRequest.queryString

        if (!queryString) {
            def builder = new StringBuilder()
            def map = mockRequest.getParameterMap()

            if (map) {
                map.each { name, values ->
                    values.each { value ->
                        builder.append(name)
                        builder.append('=')
                        builder.append(value)
                        builder.append('&')
                    }
                }

                builder.deleteCharAt(builder.length() - 1)
            }

            queryString = builder.toString()
        }

        queryString
    }
}
