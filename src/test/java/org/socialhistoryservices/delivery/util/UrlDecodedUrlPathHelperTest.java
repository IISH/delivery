package org.socialhistoryservices.delivery.util;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class UrlDecodedUrlPathHelperTest {
    private UrlDecodedUrlPathHelper helper;

    @Before
    public void setUp() {
        helper = spy(new UrlDecodedUrlPathHelper());
    }

    @Test
    public void testGetPathWithinServletMapping() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);

        final String path = "/record/12345%2F12345";
        final String decodedPath = "/record/12345/12345";

        doReturn(decodedPath).when(helper).getServletPath(request);
        doReturn(path).when(helper).getPathWithinApplication(request);

        assertEquals(
                "Should be decoded correctly so the decoded path and servlet path are equal.",
                "",
                helper.getPathWithinServletMapping(request)
        );

        String appPath = "/";
        String servletPath = "/index";

        doReturn(servletPath).when(helper).getServletPath(request);
        doReturn(appPath).when(helper).getPathWithinApplication(request);

        assertEquals("Should equal servlet path.", servletPath, helper.getPathWithinServletMapping(request));
    }
}
