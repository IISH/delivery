package org.socialhistoryservices.delivery.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

public class RequestContextToViewInterceptorTest {
    private RequestContextToViewInterceptor interceptor;

    @Before
    public void setUp() {
        interceptor = Mockito.spy(new RequestContextToViewInterceptor());
    }

    @Test
    public void testPostHandle() throws Exception {
        ModelAndView model = mock(ModelAndView.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Object handler = mock(Object.class);

        mock(RequestContextUtils.class);
        when(RequestContextUtils.findWebApplicationContext(request)).thenReturn(mock(WebApplicationContext.class));

        interceptor.postHandle(request, response, handler, model);

        verify(model).addObject(eq("rc"), any(RequestContext.class));
    }
}
