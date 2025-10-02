package Green_trade.green_trade_platform.config;

import Green_trade.green_trade_platform.filter.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
public class SecurityConfig {
    private final List<String> WHITE_LIST = List.of(
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/verify-otp",
            "/api/test/redis"
    );

    @Autowired
    private AuthTokenFilter filter;

    @Autowired
    private AuthEntryPoint authEntryPoint;

    @Bean
    public DelegatingPasswordEncoder passwordEncoder() {
        return (DelegatingPasswordEncoder) (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)       // tắt CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST.toArray(new String[0])).permitAll() // Permit url in WHITE_LIST that do not need to authenticated
                        .anyRequest().authenticated())
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))//  Add AuthTokenFilter to SecurityFilterChain
                .formLogin(AbstractHttpConfigurer::disable) // Turn off basic authentication form from Spring Security
                .httpBasic(AbstractHttpConfigurer::disable); // Turn off Basic Auth

        return http.build();
    }
}
