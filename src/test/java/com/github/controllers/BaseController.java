package com.github.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BaseController {
	@RequestMapping(path = "/", method = RequestMethod.GET)
	public String home() {
		return "home";
	}

	@RequestMapping(path = "/secret", method = RequestMethod.GET)
	public String secret() {
		return "secret";
	}
}
