package com.xiliulou.electricity.config.token;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.service.token.AliPayThirdAuthenticationServiceImpl;
import com.xiliulou.electricity.service.token.AliPayThirdForThirdPartyAuthenticationServiceImpl;
import com.xiliulou.electricity.service.token.LoginSuccessPostProcessor;
import com.xiliulou.electricity.service.token.WxProThirdAuthenticationServiceImpl;
import com.xiliulou.security.authentication.CustomAccessDeniedHandler;
import com.xiliulou.security.authentication.CustomAuthenticationEntryPoint;
import com.xiliulou.security.authentication.authorization.AuthorizationService;
import com.xiliulou.security.authentication.authorization.UrlFilterInvocationSecurityMetadataSource;
import com.xiliulou.security.authentication.authorization.UrlFilterSecurityInterceptor;
import com.xiliulou.security.authentication.authorization.UrlMatchVoter;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import com.xiliulou.security.authentication.CustomTokenAuthenticationFilter;
import com.xiliulou.security.authentication.console.CustomUsernamePasswordAuthenticationFilter;
import com.xiliulou.security.authentication.JwtTokenManager;
import com.xiliulou.security.authentication.TokenLogoutHandler;
import com.xiliulou.security.authentication.thirdauth.CustomThirdAuthAuthenticationFilter;
import com.xiliulou.security.authentication.thirdauth.ThirdAuthenticationServiceFactory;
import com.xiliulou.security.authentication.thirdauth.alipay.ThirdAliPayAuthenticationProvider;
import com.xiliulou.security.authentication.thirdauth.wxpro.ThirdWxProAuthenticationProvider;
import com.xiliulou.security.config.TokenConfig;
import com.xiliulou.security.constant.TokenConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.ExceptionTranslationFilter;

import javax.annotation.Resource;
import java.util.Collections;

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
	private WxProThirdAuthenticationServiceImpl wxProThirdAuthenticationService;
	
	@Autowired
	private AliPayThirdAuthenticationServiceImpl aliPayThirdAuthenticationService;
	
	@Resource
	private AliPayThirdForThirdPartyAuthenticationServiceImpl aliPayThirdForThirdPartyAuthenticationService;
	
	@Autowired
	RedisService redisService;
	@Autowired
	CustomPasswordEncoder customPasswordEncoder;
	@Autowired
	AuthorizationService authorizationService;
	@Autowired
	LoginSuccessPostProcessor loginSuccessPostProcessor;

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

		http.exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())//匿名用户访问无权限资源时的异常
				.accessDeniedHandler(new CustomAccessDeniedHandler())//认证过的用户访问无权限资源时的异常
				.and().csrf().disable()
				.logout().logoutUrl("/auth/token/logout")
				.addLogoutHandler(new TokenLogoutHandler(redisService, jwtTokenManager()))
//				.authorizeRequests()
//				.antMatchers("/auth/token/**", "/actuator/**", "/error").permitAll()
				.and().addFilter(new CustomUsernamePasswordAuthenticationFilter(jwtTokenManager(), authenticationManager(),loginSuccessPostProcessor))
				.addFilter(new CustomTokenAuthenticationFilter(authenticationManager(), jwtTokenManager(), authorizationService))
				.addFilterAfter(new CustomThirdAuthAuthenticationFilter(jwtTokenManager(), authenticationManager(),loginSuccessPostProcessor), CustomUsernamePasswordAuthenticationFilter.class)
				.addFilterAfter(new UrlFilterSecurityInterceptor(new UrlFilterInvocationSecurityMetadataSource(),authenticationManager(),new AffirmativeBased(Collections.singletonList(new UrlMatchVoter()))), ExceptionTranslationFilter.class)
				.httpBasic()
				//不缓存session
				.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);


		//增加第三方授权的service
		ThirdAuthenticationServiceFactory.putService(TokenConstant.THIRD_AUTH_WX_PRO, wxProThirdAuthenticationService);
		ThirdAuthenticationServiceFactory.putService(TokenConstant.THIRD_AUTH_ALI_PAY, aliPayThirdAuthenticationService);
		ThirdAuthenticationServiceFactory.putService(TokenConstant.THIRD_AUTH_ALI_PAY_THIRD_PARTY, aliPayThirdForThirdPartyAuthenticationService);
		
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/css/**");
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(customPasswordEncoder).and().authenticationProvider(new ThirdWxProAuthenticationProvider()).authenticationProvider(new ThirdAliPayAuthenticationProvider());
	}
}
