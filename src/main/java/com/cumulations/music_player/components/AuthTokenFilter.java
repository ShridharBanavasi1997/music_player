package com.cumulations.music_player.components;

import com.cumulations.music_player.dto.entity.RefreshToken;
import com.cumulations.music_player.service.CustomUserDetailService;
import com.cumulations.music_player.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtComponent.class);

    @Autowired
    private JwtComponent jwtComponent;

    @Autowired
    private CustomUserDetailService userDetailService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private CommonFunctions commonFunctions;

    private final List<String> inCludePattern = new ArrayList<>();

    public AuthTokenFilter() {
        inCludePattern.add("/api/user/**");
        inCludePattern.add("/api/admin/**");
        inCludePattern.add("/api/music/**");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!isIncluded(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String jwt = commonFunctions.parseJwt(request);
            System.out.println(jwt);
            if (jwt == null && !jwtComponent.validateJwtToken(jwt)) {
                throw new RuntimeException("Invalid or expired token");
            }
            String user = jwtComponent.getSubjectFromJwtToken(jwt);
            if (!refreshTokenService.refreshTokenExistsForUser(user)) {
                throw new RuntimeException("Invalid or expired token");
            }
            UserDetails userDetails = userDetailService.loadUserByUsername(user);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (Exception e) {
            System.out.println("System UNAUTHORIZED");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,e.getMessage());
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        filterChain.doFilter(request, response);
    }

    private boolean isIncluded(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();

        for (String pattern : inCludePattern) {
            if (pathMatcher.match(pattern, requestURI)) {
                return true;
            }
        }

        return false;
    }
}
