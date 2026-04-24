package nur.edu.nurtricenter_patient.infraestructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PathNormalizationFilterTest {

  private PathNormalizationFilter filter;

  @BeforeEach
  void setUp() {
    filter = new PathNormalizationFilter();
  }

  @Test
  void doFilterInternal_alreadyNormal_passesDirectly() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(req.getRequestURI()).thenReturn("/api/patients");

    filter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(req, res);
  }

  @Test
  void doFilterInternal_trailingSlash_wrapsRequest() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(req.getRequestURI()).thenReturn("/api/patients/");
    when(req.getScheme()).thenReturn("http");
    when(req.getServerName()).thenReturn("localhost");
    when(req.getServerPort()).thenReturn(8080);

    ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
    filter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(captor.capture(), any());
    HttpServletRequest wrapped = captor.getValue();
    assertEquals("/api/patients", wrapped.getRequestURI());
    assertEquals("/api/patients", wrapped.getServletPath());
    assertTrue(wrapped.getRequestURL().toString().contains("/api/patients"));
    assertFalse(wrapped.getRequestURL().toString().endsWith("/"));
  }

  @Test
  void doFilterInternal_doubleSlash_wrapsAndNormalizes() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(req.getRequestURI()).thenReturn("/api//patients");
    when(req.getScheme()).thenReturn("https");
    when(req.getServerName()).thenReturn("example.com");
    when(req.getServerPort()).thenReturn(443);

    ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
    filter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(captor.capture(), any());
    assertEquals("/api/patients", captor.getValue().getRequestURI());
  }

  @Test
  void doFilterInternal_httpsPort443_skipsPortInUrl() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(req.getRequestURI()).thenReturn("/api/patients/");
    when(req.getScheme()).thenReturn("https");
    when(req.getServerName()).thenReturn("example.com");
    when(req.getServerPort()).thenReturn(443);

    ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
    filter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(captor.capture(), any());
    String url = captor.getValue().getRequestURL().toString();
    assertFalse(url.contains(":443"), "Port 443 should be omitted for https");
  }

  @Test
  void doFilterInternal_httpPort80_skipsPortInUrl() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(req.getRequestURI()).thenReturn("/api/patients/");
    when(req.getScheme()).thenReturn("http");
    when(req.getServerName()).thenReturn("localhost");
    when(req.getServerPort()).thenReturn(80);

    ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
    filter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(captor.capture(), any());
    String url = captor.getValue().getRequestURL().toString();
    assertFalse(url.contains(":80"), "Port 80 should be omitted for http");
  }

  @Test
  void doFilterInternal_customPort_includesPortInUrl() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(req.getRequestURI()).thenReturn("/api/patients/");
    when(req.getScheme()).thenReturn("http");
    when(req.getServerName()).thenReturn("localhost");
    when(req.getServerPort()).thenReturn(8080);

    ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
    filter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(captor.capture(), any());
    String url = captor.getValue().getRequestURL().toString();
    assertTrue(url.contains(":8080"), "Custom port should be included in URL");
  }

  @Test
  void doFilterInternal_rootSlashOnly_passesUnchanged() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(req.getRequestURI()).thenReturn("/");

    filter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(req, res);
  }

  @Test
  void doFilterInternal_wrappedRequest_isHttpServletRequestWrapper() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(req.getRequestURI()).thenReturn("/api/patients/");
    when(req.getScheme()).thenReturn("http");
    when(req.getServerName()).thenReturn("localhost");
    when(req.getServerPort()).thenReturn(8080);

    ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
    filter.doFilterInternal(req, res, chain);

    verify(chain).doFilter(captor.capture(), any());
    assertTrue(captor.getValue() instanceof HttpServletRequestWrapper);
  }
}
