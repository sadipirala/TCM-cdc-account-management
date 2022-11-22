package com.thermofisher.cdcam.config;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RequestFilter extends OncePerRequestFilter {
    private final String REQUEST_ID_RESPONSE_HEADER = "X-Request-ID";
    
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        UUID uniqueId = UUID.randomUUID();
        MDC.put(REQUEST_ID_RESPONSE_HEADER, uniqueId.toString());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        log.info("Request IP address is {}", request.getRemoteAddr());
        log.info("Request initiated for path: '{}'", request.getRequestURI());
        log.info("Request content type is {}", request.getContentType());
        
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        chain.doFilter(request, responseWrapper);
        responseWrapper.setHeader(REQUEST_ID_RESPONSE_HEADER, uniqueId.toString());
        responseWrapper.copyBodyToResponse();

        log.info("{} response header value: {}", REQUEST_ID_RESPONSE_HEADER, responseWrapper.getHeader(REQUEST_ID_RESPONSE_HEADER));
        stopWatch.stop();
        log.info("Request completed for path: '{}'. Response Status: {}. Took: {} ms", request.getRequestURI(), response.getStatus(), stopWatch.getLastTaskTimeMillis());
        MDC.clear();
    }
}