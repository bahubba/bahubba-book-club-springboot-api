package com.bahubba.bahubbabookclub.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Authentication endpoints */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth Controller", description = "Auth endpoints")
@RequiredArgsConstructor
@Log4j2
public class AuthController {}
