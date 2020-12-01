package com.xiliulou.electricity.token.config;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.security.component.CustomAccessDeniedHandler;
import com.xiliulou.security.component.CustomAuthenticationEntryPoint;
import com.xiliulou.security.component.CustomPasswordEncoder;
import com.xiliulou.security.component.CustomTokenAuthenticationFilter;
import com.xiliulou.security.component.CustomUsernamePasswordAuthenticationFilter;
import com.xiliulou.security.component.JwtTokenManager;
import com.xiliulou.security.component.TokenLogoutHandler;
import com.xiliulou.security.config.TokenConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author: eclair
 * @Date: 2020/11/25 10:48
 * @Description:
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class TokenWebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Qualifier("userDetailServiceImpl")
	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	RedisService redisService;

	@Autowired
	CustomPasswordEncoder customPasswordEncoder;

	@Bean
	public JwtTokenManager jwtTokenManager() {
		return new JwtTokenManager();
	}

	@Bean
	public TokenConfig tokenConfig() {
		return new TokenConfig();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
				.accessDeniedHandler(new CustomAccessDeniedHandler())
				.and().csrf().disable()
				.authorizeRequests()
				.antMatchers("/auth/token/**", "/actuator/**", "/error").permitAll()
				.anyRequest().authenticated()
				.and().logout().logoutUrl("/auth/token/logout")
				.addLogoutHandler(new TokenLogoutHandler(redisService, jwtTokenManager()))
				.and().addFilter(new CustomUsernamePasswordAuthenticationFilter(jwtTokenManager(), authenticationManager()))
				.addFilter(new CustomTokenAuthenticationFilter(authenticationManager(), jwtTokenManager())).httpBasic()
				//不缓存session
				.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/css/**");
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(customPasswordEncoder);
	}
}
