package nur.edu.nurtricenter_patient.infraestructure.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PathNormalizationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String rawUri = request.getRequestURI();
    String normalizedUri = normalize(rawUri);

    if (rawUri.equals(normalizedUri)) {
      filterChain.doFilter(request, response);
      return;
    }

    HttpServletRequestWrapper wrapped = new HttpServletRequestWrapper(request) {
      @Override
      public String getRequestURI() {
        return normalizedUri;
      }

      @Override
      public String getServletPath() {
        return normalizedUri;
      }

      @Override
      public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        url.append(getScheme())
          .append("://")
          .append(getServerName());

        int port = getServerPort();
        boolean skipPort = ("http".equalsIgnoreCase(getScheme()) && port == 80)
          || ("https".equalsIgnoreCase(getScheme()) && port == 443);
        if (!skipPort) {
          url.append(':').append(port);
        }

        url.append(normalizedUri);
        return url;
      }
    };

    filterChain.doFilter(wrapped, response);
  }

  private String normalize(String rawPath) {
    if (rawPath == null || rawPath.isBlank()) {
      return "/";
    }

    String normalized = rawPath.replaceAll("/+", "/");
    if (normalized.length() > 1 && normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }
}
