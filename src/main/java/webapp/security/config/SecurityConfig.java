package webapp.security.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import webapp.model.entities.User;
import webapp.model.entities.UserPhoneNumber;
import webapp.model.repositories.UserPhoneNumberRepository;
import webapp.model.repositories.UserRepository;
import webapp.security.AuthenticatedUser;

@Configuration
@EnableWebMvcSecurity
public class SecurityConfig extends GlobalAuthenticationConfigurerAdapter {

	@Bean
	UserDetailsService userDetailsService() {
		return new UserDetailsService() {

			@Autowired
			UserRepository userRepository;

			@Autowired
			UserPhoneNumberRepository userPhoneNumberRepository;

			@Override
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
						UserPhoneNumber number = userPhoneNumberRepository.findOne(username);
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
				.passwordEncoder(new StandardPasswordEncoder());
	}

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
					.sessionManagement()
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
						.and()
					.csrf()
						.disable();
		}

	}

	@Configuration                                                  
	public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
				.authorizeRequests()
					.anyRequest().authenticated()
					.and()
				.formLogin();
		}

	}

}