package com.anonymouschat.anonymouschatserver.common.util;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseUtil {
	public static void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");
		String body = String.format("{\"code\":\"UNAUTHORIZED\", \"message\":\"%s\"}", message);
		response.getWriter().write(body);
		response.getWriter().flush();
	}
}
