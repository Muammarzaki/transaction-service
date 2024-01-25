package com.github.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("auth")
public class AuthenticationController {
	private final UserDetailServiceImp userDetailService;

	public AuthenticationController(UserDetailServiceImp userDetailService) {
		this.userDetailService = userDetailService;
	}

	@PostMapping("register")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void register(@RequestBody RegisterDomain dataRegister) {
		userDetailService.registerUser(dataRegister.username, dataRegister.password);
	}

	@PostMapping("check")
	@ResponseStatus(HttpStatus.OK)
	public void checkUserAuth() {
	}

	public record RegisterDomain(String username, String password) {
		public RegisterDomain(String username, String password) {
			this.username = username;
			this.password = new String(Base64.getDecoder().decode(password));
		}
	}
}
