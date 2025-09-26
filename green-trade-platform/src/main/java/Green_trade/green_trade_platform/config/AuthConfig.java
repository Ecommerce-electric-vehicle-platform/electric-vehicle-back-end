package Green_trade.green_trade_platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class AuthConfig {
    @Bean
    public DelegatingPasswordEncoder passwordEncoder() {
        return (DelegatingPasswordEncoder) (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())       // tắt CSRF
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()       // cho phép tất cả request không cần login
                )
                .formLogin(form -> form.disable())  // tắt form login mặc định
                .httpBasic(basic -> basic.disable()); // tắt Basic Auth

        return http.build();
    }
}
