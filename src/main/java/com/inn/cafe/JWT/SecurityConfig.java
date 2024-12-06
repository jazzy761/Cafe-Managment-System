package com.inn.cafe.JWT;

import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    CustomerUserDetailService customerUserDetailService;

    @Autowired
    JwtFilter jwtFilter;

    // Password encoder bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // Use NoOpPasswordEncoder for simplicity
    }

    // AuthenticationManager Bean (Spring Security 6+ way)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Security Filter Chain replacing WebSecurityConfigurerAdapter
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues())) // Configuring CORS
                .csrf(csrf -> csrf.disable()) // Disabling CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/login", "/user/signup", "/user/forgotPassword").permitAll() // Public endpoints
                        .anyRequest().authenticated()) // Securing all other endpoints
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Making session stateless
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT Filter

        return http.build(); // Return the configured SecurityFilterChain
    }


//"/user/signup"
}
