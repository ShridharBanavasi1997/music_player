package com.cumulations.music_player.components;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class VersionChecker implements HandlerInterceptor {
    private static final String API_VERSION_HEADER = "App-Version";
    private static final String MIN_SUPPORTED_VERSION = "1.0";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestedApiVersion = request.getHeader(API_VERSION_HEADER);
        if (requestedApiVersion == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "API version is missing");
            return false;
        }
        if(compareVersions(requestedApiVersion, MIN_SUPPORTED_VERSION) < 0){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported API version. Please upgrade.");
            return false;
        }

        return true;
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int minLength = Math.min(parts1.length, parts2.length);
        for (int i = 0; i < minLength; i++) {
            int num1 = Integer.parseInt(parts1[i]);
            int num2 = Integer.parseInt(parts2[i]);
            if (num1 < num2) {
                return -1;
            } else if (num1 > num2) {
                return 1;
            }
        }

        return Integer.compare(parts1.length, parts2.length);
    }
}
