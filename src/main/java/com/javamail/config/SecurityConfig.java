package com.javamail.config;

import com.javamail.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authenticationProvider(authProvider)
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD,
                                jakarta.servlet.DispatcherType.ERROR)
                        .permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/login", "/register", "/api/auth/**", "/user/login", "/user/register")
                        .permitAll()
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/mailbox", true)
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());

        // Allow GET /user/logout as an alias for Spring Security logout
        // (sidebar and profile page use this anchor href)
        http.addFilterBefore(
                new org.springframework.web.filter.OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest req,
                            jakarta.servlet.http.HttpServletResponse res,
                            jakarta.servlet.FilterChain chain)
                            throws jakarta.servlet.ServletException, java.io.IOException {
                        if (("GET".equalsIgnoreCase(req.getMethod()) || "POST".equalsIgnoreCase(req.getMethod()))
                                && req.getRequestURI().endsWith("/user/logout")) {
                            // Invalidate session and redirect to login
                            jakarta.servlet.http.HttpSession session = req.getSession(false);
                            if (session != null)
                                session.invalidate();
                            org.springframework.security.core.context.SecurityContextHolder.clearContext();
                            res.sendRedirect(req.getContextPath() + "/login?logout=true");
                            return;
                        }
                        chain.doFilter(req, res);
                    }
                },
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
