package com.Mini_Instagram_Demo.Mini_Instagram_Demo.ManageUsers.Controllers;

import com.Mini_Instagram_Demo.Mini_Instagram_Demo.ManageUsers.Models.User;
import com.Mini_Instagram_Demo.Mini_Instagram_Demo.ManageUsers.Services.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> addUser( @RequestBody User user, BindingResult bindingResult) {
        try {
            // Validate request body
            if (bindingResult.hasErrors()) {
                return buildValidationErrorResponse(bindingResult);
            }

            if (user == null) {
                return buildErrorResponse("User data is required", HttpStatus.BAD_REQUEST);
            }

            // Additional validation
            ResponseEntity<?> validationError = validateUser(user);
            if (validationError != null) {
                return validationError;
            }

            User savedUser = userService.addUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User created successfully");
            response.put("user", savedUser);
            response.put("status", "success");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (DataIntegrityViolationException e) {
            return buildErrorResponse("User with this email already exists or data constraint violation",
                    HttpStatus.CONFLICT);
        } catch (DataAccessException e) {
            return buildErrorResponse("Database error while creating user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error creating user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable int userId,
             @RequestBody User user,
            BindingResult bindingResult) {
        try {
            // Validate path variable
            if (userId < 0) {
                return buildErrorResponse("User ID must be a positive number", HttpStatus.BAD_REQUEST);
            }

            // Validate request body
            if (bindingResult.hasErrors()) {
                return buildValidationErrorResponse(bindingResult);
            }

            if (user == null) {
                return buildErrorResponse("User data is required", HttpStatus.BAD_REQUEST);
            }

            // Additional validation
            ResponseEntity<?> validationError = validateUser(user);
            if (validationError != null) {
                return validationError;
            }

            User updatedUser = userService.updateUser(userId, user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User updated successfully");
            response.put("user", updatedUser);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return buildErrorResponse("User with ID " + userId + " not found",
                        HttpStatus.NOT_FOUND);
            }
            return buildErrorResponse("Error updating user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error updating user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId) {
        try {
            if (userId < 0) {
                return buildErrorResponse("User ID must be a positive number", HttpStatus.BAD_REQUEST);
            }

            userService.deleteUser(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            response.put("userId", userId);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return buildErrorResponse("User with ID " + userId + " not found",
                        HttpStatus.NOT_FOUND);
            }
            return buildErrorResponse("Error deleting user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error deleting user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable int userId) {
        try {
            if (userId < 0) {
                return buildErrorResponse("User ID must be a positive number", HttpStatus.BAD_REQUEST);
            }

            User user = userService.getUserById(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User retrieved successfully");
            response.put("user", user);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return buildErrorResponse("User with ID " + userId + " not found",
                        HttpStatus.NOT_FOUND);
            }
            return buildErrorResponse("Error fetching user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error fetching user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int size) {
        try {
            // Validate pagination parameters
            if (page < 0) {
                return buildErrorResponse("Page number must be non-negative", HttpStatus.BAD_REQUEST);
            }
            if (size <= 0 || size > 100) {
                return buildErrorResponse("Page size must be between 1 and 100", HttpStatus.BAD_REQUEST);
            }

            List<User> users = userService.getAllUsers();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Users retrieved successfully");
            response.put("users", users);
            response.put("totalCount", users.size());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (DataAccessException e) {
            return buildErrorResponse("Database error while fetching users",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error fetching users: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam(required = false) String name,
                                         @RequestParam(required = false) String email) {
        try {
            if ((name == null || name.trim().isEmpty()) && (email == null || email.trim().isEmpty())) {
                return buildErrorResponse("At least one search parameter (name or email) is required",
                        HttpStatus.BAD_REQUEST);
            }

            // This would require adding a search method in the service
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Search functionality - implement in service layer");
            response.put("searchCriteria", Map.of(
                    "name", name != null ? name : "N/A",
                    "email", email != null ? email : "N/A"
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return buildErrorResponse("Error searching users: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Validates user data
     */
    private ResponseEntity<?> validateUser(User user) {
        // Validate name
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return buildErrorResponse("User name is required", HttpStatus.BAD_REQUEST);
        }
        if (user.getName().length() < 2 || user.getName().length() > 100) {
            return buildErrorResponse("User name must be between 2 and 100 characters",
                    HttpStatus.BAD_REQUEST);
        }

        // Validate email
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return buildErrorResponse("Email is required", HttpStatus.BAD_REQUEST);
        }
        if (!isValidEmail(user.getEmail())) {
            return buildErrorResponse("Invalid email format", HttpStatus.BAD_REQUEST);
        }

        // Validate date of birth (optional but if provided, should be valid)
        if (user.getDateOfBirth() != null) {
            if (user.getDateOfBirth().after(new java.util.Date())) {
                return buildErrorResponse("Date of birth cannot be in the future",
                        HttpStatus.BAD_REQUEST);
            }
        }

        return null; // Validation passed
    }

    /**
     * Validates email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

    /**
     * Builds validation error response from BindingResult
     */
    private ResponseEntity<?> buildValidationErrorResponse(BindingResult bindingResult) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Validation failed");
        errors.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
        errors.put("timestamp", String.valueOf(System.currentTimeMillis()));

        Map<String, String> fieldErrors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        errors.put("validationErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("status", String.valueOf(status.value()));
        error.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.status(status).body(error);
    }

    // Global exception handlers
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        if (e.getMessage() != null && e.getMessage().contains("User not found")) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        return buildErrorResponse("Runtime error: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return buildErrorResponse("Data constraint violation: Duplicate email or invalid reference",
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Map<String, String>> handleEmptyResult(EmptyResultDataAccessException e) {
        return buildErrorResponse("User not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDataAccess(DataAccessException e) {
        return buildErrorResponse("Database error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return buildErrorResponse("An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}