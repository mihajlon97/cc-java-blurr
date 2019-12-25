package com.example.demo.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Message;

@RestController
@CrossOrigin
public class HelloController {

	@GetMapping("/hello/message")
	public Message helloWorldMessageEndpoint() {
		return new Message("hello from helloWorldMessageEndpoint", 1);
	}
	
	@GetMapping("/hello/string")
	public String helloWorldStringEndpoint() {
		return "hello";
	}
}
