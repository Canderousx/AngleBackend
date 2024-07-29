package com.example.Angle.Config;


import com.example.Angle.Config.Filters.JwtAuthFilter;
import com.example.Angle.Config.Filters.RequestsLogger;
import com.example.Angle.Config.SecServices.EnvironmentVariables;
import com.example.Angle.Config.SecServices.JwtService;
import com.example.Angle.Config.SecServices.MyUserDetailsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {

    private final Logger logger = LogManager.getLogger(SecurityConfig.class);

    @Bean
    EnvironmentVariables environmentVariables(){
        logger.info("Getting required System Environment Variables");
        EnvironmentVariables env = EnvironmentVariables.builder()
                .secretKey(System.getenv("JTOKEN_KEY"))
                .ffmpegPath(System.getenv("FFMPEG_PATH"))
                .hlsOutputPath(System.getenv("HLS_OUTPUT_PATH"))
                .thumbnailsPath(System.getenv("Thumbnails_PATH"))
                .ffmpegTempFolder(System.getenv("FFMPEG_TEMP_FOLDER"))
                .build();
        if(!env.checkIfNotNull()){
            logger.error("ERROR: SOME OF YOUR SYSTEM VARIABLES DOESN'T EXIST. PLEASE CHECK IT OUT IMMEDIATELY");
            return new EnvironmentVariables();
        }
        logger.info("System variables loaded successfully");
        return env;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200","http://192.168.100.36:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","DELETE","PATCH"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("","*"));
        configuration.setExposedHeaders(Arrays.asList("totalComments","totalVideos","totalReports",""));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:E:/IT/Angle/BACKEND/Angle/src/main/resources/media/");
    }




    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers("/unAuth/**","/media/**").permitAll()
                                .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(requestsLogger(), UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    @Bean
    JwtAuthFilter jwtAuthFilter(){
        return new JwtAuthFilter(jwtService(),userDetailsService());
    }

    @Bean
    JwtService jwtService(){
        return new JwtService();
    }

    @Bean
    RequestsLogger requestsLogger(){
        return new RequestsLogger();
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    MyUserDetailsService userDetailsService(){
        return new MyUserDetailsService();
    }

    @Bean
    AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService());
        return authProvider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
