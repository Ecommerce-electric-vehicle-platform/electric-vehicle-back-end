package Green_trade.green_trade_platform.filter;

import Green_trade.green_trade_platform.exception.AuthException;
import Green_trade.green_trade_platform.service.UserDetailsServiceCustomer;
import Green_trade.green_trade_platform.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceCustomer userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if (path.startsWith("/api/v1/auth") || path.startsWith("/test")
                || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
                || path.startsWith("/verify-otp") || path.startsWith("/api/test/redis")) {
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("Authentication in request : {}",  request.getRequestURI());
        try {
            String token = getTokenFromRequest(request);
            if(token != null && jwtUtils.verifyToken(token)) {
                String username = jwtUtils.getUsernameFromToken(token);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                log.debug("Role from JWT: {}", userDetails.getAuthorities());

                // Set user's information like : cookies, session,...
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception e) {
            throw new AuthException("Authentication failed at AuthTokenFilter: " + e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String jwt = jwtUtils.getTokenFromRequest(request);
        log.debug("AuthTokenFilter.java: {}", jwt);
        return jwt;
    }
}
