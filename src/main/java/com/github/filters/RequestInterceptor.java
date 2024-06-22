package com.github.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
public class RequestInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String requestId = UUID.randomUUID().toString();
		request.setAttribute("requestId", requestId);
		request.setAttribute("startTime", System.currentTimeMillis());
		response.setHeader("Request-Id", requestId);
		log.info("Request START, request Id : {}", requestId);
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		long executeTime = System.currentTimeMillis() - (long) request.getAttribute("startTime");
		log.info("Request END, request Id : {}, status : {}, execution time : {}ms", request.getAttribute("requestId"), response.getStatus(), executeTime);
	}
}
