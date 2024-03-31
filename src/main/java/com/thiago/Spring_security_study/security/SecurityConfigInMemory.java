package com.thiago.Spring_security_study.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfigInMemory {

	// aqui criamos os roles que podem acessar nossa aplicação, na memoria, posteriormente será trocada para memoria no banco de dados.
		@Bean
		public UserDetailsService userDetailsService(BCryptPasswordEncoder bCryptPasswordEncoder) {
			InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
			manager.createUser(User.withUsername("user").password(bCryptPasswordEncoder.encode("1234")).roles("USER").build());
			manager.createUser(User.withUsername("admin").password(bCryptPasswordEncoder.encode("123"))
					.roles("USER", "ADMIN").build());
			return manager;
		}
	
		/* criando o maneger de autenticação, que é a api que diz para o spring security como performar a authenticação
		   o userDetailsService é o que foi contruido antes para criação dos roles com senhas e usuarios e aqui é onde dizemos ao spring
		   que esses detalhes que devem ser utilizados. A só a nivel pra não ter duvidas, o ProviderManeger implementa o AuthenticationManeger.
		   
		   Obs: no caso de usar o banco de dados, essa função é desnecessaria.
		*/
		@Bean
		public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
				BCryptPasswordEncoder bCryptPasswordEncoder) {
			DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
			authenticationProvider.setUserDetailsService(userDetailsService);
			authenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);
			
			ProviderManager provider =  new ProviderManager(authenticationProvider);
			provider.setEraseCredentialsAfterAuthentication(false);
			return provider;
		}
	
	/*
	 * configura a ordem de verificação dos filtros, caso determinado filtro tenha
	 * sido configurado no projeto. 1- CSRF FILTER 2- USER E PASSWORD AUTHENTICATION
	 * FILTER (like login) 3- BASIC AUTHENTICATION FILTER 4- AUTORIZATION FILTER (if
	 * the user can do the task or not)
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(Customizer.withDefaults())
		.authorizeHttpRequests(
				authorize -> authorize
				.requestMatchers(HttpMethod.POST, "/login").permitAll()
				.requestMatchers("/admin/*").hasAnyRole("ADMIN")
				.requestMatchers("/**").hasAnyRole("USER", "ADMIN")
				.anyRequest().authenticated())
		.httpBasic(Customizer.withDefaults())
		.formLogin(formLogin -> formLogin
                .loginPage("/login")
                .permitAll());
		return http.build();
	}
	
	@Bean
	private BCryptPasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
}
