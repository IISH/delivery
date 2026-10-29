package nl.knaw.huc.di.delivery.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
  private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (shouldLog(request)) {
      START_TIME.set(System.currentTimeMillis());
      LOGGER.info("Incoming: {} {}", request.getMethod(), request.getRequestURI());
    }
    return true;
  }

  private static boolean shouldLog(HttpServletRequest request) {
    return !request.getRequestURI().contains("/css") && !request.getRequestURI().contains("/js") && !request.getRequestURI().contains("/favicon");
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    if (shouldLog(request)) {
      long duration = System.currentTimeMillis() - START_TIME.get();
      LOGGER.info("Completed: {} {} Status={} Time={}ms",
          request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
    }
  }
}
