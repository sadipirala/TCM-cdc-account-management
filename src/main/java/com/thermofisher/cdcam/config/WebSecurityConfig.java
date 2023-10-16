package com.thermofisher.cdcam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {//extends WebSecurityConfigurerAdapter {

   /* @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
            .antMatchers("/**").permitAll();
        
        httpSecurity.cors();
    }*/
   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       // Configure AuthenticationManagerBuilder

       return http.cors(withDefaults())
       .csrf(AbstractHttpConfigurer::disable)

               .authorizeHttpRequests((authz) -> authz
                       .requestMatchers("/**") .permitAll())
               .sessionManagement((session) -> session
                       .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .httpBasic(withDefaults()).build();

   }
}
