package com.example.BookingApp.dto.user;

import com.example.BookingApp.entityenums.Role;

public record UserResponse(
	    Long id,
	    String username,
	    String email,
	    Role role,
	    boolean active
	) {}
