package com.talkwire.messenger.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/secured")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MainController {

}

