// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.common.utils;

import org.junit.Test;

import java.net.URI;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UrlHelperTest {

    private final UrlHelper.ParseResultValidator validator = new UrlHelper.ParseResultValidator() {
        @Override
        public boolean validate(final UrlHelper.ParseResult result) {
            return result.getProjectName().equals("project");
        }
    };

    @Test
    public void testIsValidServerUrl() throws Exception {

    }

    @Test
    public void testGetBaseUri() throws Exception {

    }

    @Test
    public void testResolveEndpointURI() throws Exception {

        URI expected, resolved;

        expected = URI.create("http://foo/Bar");

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo"), "Bar");
        assertEquals(expected, resolved);

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo/"), "Bar");
        assertEquals(expected, resolved);

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo"), "/Bar");
        assertEquals(expected, resolved);

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo/"), "/Bar");
        assertEquals(expected, resolved);

        expected = URI.create("http://foo:8080/Bar");

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo:8080"), "Bar");
        assertEquals(expected, resolved);

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo:8080/"), "Bar");
        assertEquals(expected, resolved);

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo:8080"), "/Bar");
        assertEquals(expected, resolved);

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo:8080/"), "/Bar");
        assertEquals(expected, resolved);

        expected = URI.create("http://foo:8080/serverPath/Bar");

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo:8080/serverPath"), "Bar");
        assertEquals(expected, resolved);

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo:8080/serverPath/"), "Bar");
        assertEquals(expected, resolved);

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo:8080/serverPath"), "/Bar");
        assertEquals(expected, resolved);

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo:8080/serverPath/"), "/Bar");
        assertEquals(expected, resolved);

        expected = URI.create("http://foo:8080/serverPath/servicePath/Bar");

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo:8080/serverPath"), "servicePath/Bar");
        assertEquals(expected, resolved);

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo:8080/serverPath/"), "servicePath/Bar");
        assertEquals(expected, resolved);

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo:8080/serverPath"), "/servicePath/Bar");
        assertEquals(expected, resolved);

        resolved = UrlHelper.resolveEndpointUri(URI.create("http://foo:8080/serverPath/"), "/servicePath/Bar");
        assertEquals(expected, resolved);

    }

    @Test
    public void testBasicTryParseScenarios() {

        // only support http/https protocol
        UrlHelper.ParseResult result = UrlHelper.tryParse("protocol1://test.visualstudio.com/collection/project/_git/test", validator);
        assertFalse(result.isSuccess());

        // ssh is not supported yet
        result = UrlHelper.tryParse("git@test.com:test/test.git", validator);
        assertFalse(result.isSuccess());

        result = UrlHelper.tryParse("http://test.visualstudio.com/collection/project/_git/repo", validator);
        assertTrue(result.isSuccess());
        assertEquals("project", result.getProjectName());
        assertEquals("repo", result.getRepoName());
        assertEquals("http://test.visualstudio.com/collection/", result.getCollectionUrl());

        result = UrlHelper.tryParse("https://test.visualstudio.com/collection/_git/project", validator);
        assertTrue(result.isSuccess());
        assertEquals("project", result.getProjectName());
        assertEquals("project", result.getRepoName());
        assertEquals("https://test.visualstudio.com/collection/", result.getCollectionUrl());

        result = UrlHelper.tryParse("https://localhost:8080/iispath1/iispath2/iispath3/collection/project/_git/repo?queryParam=query", validator);
        assertTrue(result.isSuccess());
        assertEquals("project", result.getProjectName());
        assertEquals("repo", result.getRepoName());
        assertEquals("https://localhost:8080/iispath1/iispath2/iispath3/collection/", result.getCollectionUrl());

        result = UrlHelper.tryParse("http://localhost:8080/iispath1/iispath2/iispath3/collection%20with%20space/_git/project#withfragments", validator);
        assertTrue(result.isSuccess());
        assertEquals("project", result.getProjectName());
        assertEquals("project", result.getRepoName());
        assertEquals("collection with space", result.getCollectionName());
        assertEquals("http://localhost:8080/iispath1/iispath2/iispath3/collection with space/", result.getCollectionUrl());
    }

    @Test
    public void testTryParseWithLimitedRefs() {
        UrlHelper.ParseResult result = UrlHelper.tryParse("http://test.visualstudio.com/collection/project/_git/_optimized/repo", validator);
        assertTrue(result.isSuccess());
        assertEquals("project", result.getProjectName());
        assertEquals("repo", result.getRepoName());
        assertEquals("http://test.visualstudio.com/collection/", result.getCollectionUrl());

        result = UrlHelper.tryParse("https://test.visualstudio.com/collection/_git/_full/project", validator);
        assertTrue(result.isSuccess());
        assertEquals("project", result.getProjectName());
        assertEquals("project", result.getRepoName());
        assertEquals("https://test.visualstudio.com/collection/", result.getCollectionUrl());
    }

    @Test
    public void testTryParseWithMalformedUrl() {
        UrlHelper.ParseResult result = UrlHelper.tryParse("", validator);
        assertFalse(result.isSuccess());

        result = UrlHelper.tryParse(null, validator);
        assertFalse(result.isSuccess());

        result = UrlHelper.tryParse("abc/def", validator);
        assertFalse(result.isSuccess());

        result = UrlHelper.tryParse("abc\\def", validator);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testTryParseGitHubStyleUrl() {
        UrlHelper.ParseResult result = UrlHelper.tryParse("https://test.com/account/project.git", validator);
        assertFalse(result.isSuccess());

        result = UrlHelper.tryParse("git@test.com:account/project.git", validator);
        assertFalse(result.isSuccess());
    }
}
