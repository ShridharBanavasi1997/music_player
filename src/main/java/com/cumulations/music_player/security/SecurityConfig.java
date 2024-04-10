package com.cumulations.music_player.security;

import com.cumulations.music_player.components.AuthEntryPointComponent;
import com.cumulations.music_player.components.AuthTokenFilter;
import com.cumulations.music_player.components.VersionChecker;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthEntryPointComponent authEntryPointComponent;

    @Autowired
    private VersionChecker versionChecker;

//    public SecurityConfig(UserDetailsService userDetailsService) {
//        this.userDetailsService = userDetailsService;
//    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }


//    @Bean
//    public UserDetailsManager userDetailsManager(DataSource dataSource) {
//        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
//        userDetailsManager.setUsersByUsernameQuery("select email, password, active from users_info where email = ?");
//        userDetailsManager.setAuthoritiesByUsernameQuery("select u.email, r.name as authority "
//                + "from users_info u "
//                + "join user_roles ur on u.id = ur.user_id "
//                + "join roles r on r.id = ur.role_id where u.email = ?");
//        return userDetailsManager;
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authEntryPointComponent))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((config) ->
                        config
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST,"/api/music/**").hasRole("CREATOR")
                                .requestMatchers(HttpMethod.GET,"/api/music/**").hasAnyRole("ADMIN","USER","CREATOR")
                                .requestMatchers("/api/user/**").hasAnyRole("ADMIN","USER","CREATOR")
                                .requestMatchers("/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                                .anyRequest().authenticated())
                .httpBasic(withDefaults());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
//        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(versionChecker)
                .addPathPatterns("/api/**");;
    }

    @Bean
    public OperationCustomizer customGlobalHeaders() {

        return (Operation operation, HandlerMethod handlerMethod) -> {

            Parameter app_version = new Parameter()
                    .in(ParameterIn.HEADER.toString())
                    .schema(new StringSchema())
                    .name("App-Version")
                    .description("app version")
                    .example("1.0")
                    .required(true);

            operation.addParametersItem(app_version);

            return operation;
        };
    }
}
