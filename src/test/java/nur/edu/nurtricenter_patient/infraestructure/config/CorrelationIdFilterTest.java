package nur.edu.nurtricenter_patient.infraestructure.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

class CorrelationIdFilterTest {

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  @Test
  void existingHeader_usesProvidedValue() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(req.getHeader(CorrelationIdFilter.CORRELATION_HEADER)).thenReturn("existing-id");

    filter.doFilterInternal(req, res, chain);

    verify(res).setHeader(CorrelationIdFilter.CORRELATION_HEADER, "existing-id");
    verify(chain).doFilter(req, res);
  }

  @Test
  void missingHeader_generatesUUID() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(req.getHeader(CorrelationIdFilter.CORRELATION_HEADER)).thenReturn(null);

    final String[] capturedId = new String[1];
    org.mockito.Mockito.doAnswer(inv -> {
      capturedId[0] = inv.getArgument(1);
      return null;
    }).when(res).setHeader(eq(CorrelationIdFilter.CORRELATION_HEADER), org.mockito.ArgumentMatchers.anyString());

    filter.doFilterInternal(req, res, chain);

    assertNotNull(capturedId[0]);
  }

  @Test
  void blankHeader_generatesUUID() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(req.getHeader(CorrelationIdFilter.CORRELATION_HEADER)).thenReturn("   ");

    final String[] capturedId = new String[1];
    org.mockito.Mockito.doAnswer(inv -> {
      capturedId[0] = inv.getArgument(1);
      return null;
    }).when(res).setHeader(eq(CorrelationIdFilter.CORRELATION_HEADER), org.mockito.ArgumentMatchers.anyString());

    filter.doFilterInternal(req, res, chain);

    assertNotNull(capturedId[0]);
  }
}
