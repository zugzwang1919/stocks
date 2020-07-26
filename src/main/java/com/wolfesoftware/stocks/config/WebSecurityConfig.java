/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Autowired
    private UserDetailsService jwtUserDetailsService;
    @Autowired
    private JwtRequestFilter jwtRequestFilter;


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // configure AuthenticationManager so that it knows from where to load
        // user for matching credentials
        // Use BCryptPasswordEncoder
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // FIXME: Since I store passwords today in the clear, this is required.
        // FIXME: Should be fixed in the future.
        // FIXME: NoOpPasswordEncoder is deprecated for this reason.
        // noinspection deprecation
        return NoOpPasswordEncoder.getInstance();
        // return new BCryptPasswordEncoder();
    }


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // NOTE: I had to add the Order(-1) in order to get the OPTIONS permitAll to work
    // NOTE: Prior to this addition, any OPTIONS request returned 401
    // NOTE: I BELIEVE, the Order(-1) guarantees that this will get the first shot at handling the OPTIONS request
    @Order(-1)
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // We don't need CSRF (for now anyway)
        httpSecurity.csrf().disable().
                authorizeRequests().
                // Endpoints that do not require any authentication
                antMatchers(HttpMethod.OPTIONS, "/**").permitAll().
                antMatchers("/authenticate").permitAll().  // Login as an existing user
                antMatchers( "/user").permitAll().  // Register as a new user
                //antMatchers("/stock").permitAll().
                // Endpoints available only to Admins
                antMatchers("/authority/**").access("hasRole('ROLE_ADMIN')").
                antMatchers(HttpMethod.DELETE, "/user/*").access("hasRole('ROLE_ADMIN')").  // Delete a User
                // all other requests are available to Users and Admins
                anyRequest().access("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')").
                and().
                // make sure we use stateless session; session won't be used to
                // store user's state.
                exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // Add a filter to validate the tokens with every request
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }
}    
