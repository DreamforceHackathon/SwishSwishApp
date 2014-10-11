package com.partlycloudy.swishswish;

import com.google.api.client.auth.oauth2.TokenResponseException;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReauthFilter implements Filter {
  private static final Logger LOG = Logger.getLogger(ReauthFilter.class.getSimpleName());

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {

    // Skip this filter if somehow we have a request that's not HTTP
    if (response instanceof HttpServletResponse && request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      HttpServletResponse httpResponse = (HttpServletResponse) response;

      // Attempt to re-auth if we have an invalid grant exception.
      // This will only work if the request has not yet been committed.
      try {
        filterChain.doFilter(request, response);
      } catch (TokenResponseException e) {
        if (e.getDetails().getError().contains("invalid_grant")) {
          LOG.warning("User disabled Glassware. Attempting to re-authenticate");
          httpResponse.sendRedirect(WebUtil.buildUrl(httpRequest, "/oauth2callback"));
        }
      }
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void destroy() {
  }
}
