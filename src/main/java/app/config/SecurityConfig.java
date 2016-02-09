package app.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import app.data.repositories.UserPhoneNumberRepository;
import app.data.repositories.UserRepository;
import app.entities.User;
import app.entities.UserPhoneNumber;
import app.security.AuthenticatedUser;

@Configuration
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class SecurityConfig extends GlobalAuthenticationConfigurerAdapter {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new StandardPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService() {
		return new UserDetailsService() {

			@Autowired
			UserRepository userRepository;

			@Autowired
			UserPhoneNumberRepository userPhoneNumberRepository;

			@Override
			@Transactional
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				User user = null;

				if (username.startsWith("@User")) {
					String userId = username.substring("@User".length());
					try {
						user = userRepository.findOne(Integer.parseInt(userId));
					} catch (NumberFormatException e) {
						e.printStackTrace(); // this should never happen, assuming we have done proper validation of emails
					}
				}
				if (user == null) {
					List<User> users = userRepository.findByEmail(username);
					if (users != null && !users.isEmpty()) {
						user = users.get(0);
					} else {
						UserPhoneNumber number = userPhoneNumberRepository.findByPhoneNumber(username);
						if (number != null) {
							user = number.getUser();
						}
					}
					if (user == null)
						throw new UsernameNotFoundException("could not find the user " + username);
				}

				return new AuthenticatedUser(user, userPhoneNumberRepository);
			}

		};
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.userDetailsService(userDetailsService())
				.passwordEncoder(passwordEncoder());
	}

	/**
	 * Security configuration for REST
	 */
	@Configuration
	@Order(1)                                                        
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
	
		protected void configure(HttpSecurity http) throws Exception {
			http
			.antMatcher("/api/**")
			.authorizeRequests()
				.anyRequest().authenticated()
				.and()
			.httpBasic()
				.and()
			.csrf()
				.disable();
					//.sessionManagement()
					//	.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
					//
					// ^ Ideally, you want a REST API to be stateless. But we're letting it be sessioned so that its
					//   easy for the web interface to call the REST service without going through hoops.
		}

	}

	/**
	 * Security configuration for Web
	 */
	@Configuration                                                  
	public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
				.csrf()
					.disable() // This disables CSRF protection for everything, including the web part!
					           // I couldn't figure out how to do it only for the REST part. If someone figures
					           // this out, please do it.  --Ankit
				.authorizeRequests() 
					.antMatchers("/static/**", "/CallHandler","/OutboundCallHandler","/BroadcastCallHandler","/IncomingSMSHandler","/app/**","/success","/expire","/invalid").permitAll()
					.anyRequest().authenticated()
					.and()
				.formLogin()
					.loginPage("/login")
					.permitAll()
					.and()
				.logout()
					.permitAll();
		}

	}

}