package com.github.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class RequestLogging implements Filter {
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		log.debug("Client Address:  {}, Client Host:  {}", request.getRemoteAddr(), request.getRemoteHost());
		log.info("Agent : {}, Request Method : {}, Request Locale : {}", request.getHeader("User-Agent"), request.getMethod(), request.getLocale());
		filterChain.doFilter(servletRequest, servletResponse);
	}
}
