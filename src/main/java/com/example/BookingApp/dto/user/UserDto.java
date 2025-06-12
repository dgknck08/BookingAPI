package com.example.BookingApp.dto.user;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import com.example.BookingApp.entityenums.Role;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class UserDto implements Serializable { /**
	 *
	 * 
	 * deleted createdAt field.
	 */
	
	private static final long serialVersionUID = 1L;
// Added Serializable for Redis	
    private Long id; // Added missing ID field
    private String username;
    private String email;
    private Role role;
    private boolean active;
}