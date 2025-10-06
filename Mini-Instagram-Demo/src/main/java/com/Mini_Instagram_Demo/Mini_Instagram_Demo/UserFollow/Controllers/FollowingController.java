package com.Mini_Instagram_Demo.Mini_Instagram_Demo.UserFollow.Controllers;

import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UserFollow.Services.FollowingService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
public class FollowingController {

    private final FollowingService followingService;

    public FollowingController(FollowingService followingService) {
        this.followingService = followingService;
    }

    @GetMapping("/following/count/{userId}")
    public ResponseEntity<?> getNumberOfFollowing(@PathVariable int userId) {
        try {
            if (userId < 0) {
                return buildErrorResponse("Invalid user ID", HttpStatus.BAD_REQUEST);
            }

            int count = followingService.getNumberOfFollowing(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("followingCount", count);
            response.put("message", "Following count retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Invalid user ID: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DataAccessException e) {
            return buildErrorResponse("Database error while fetching following count",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/followers/count/{userId}")
    public ResponseEntity<?> getNumberOfFollowers(@PathVariable int userId) {
        try {
            if (userId < 0) {
                return buildErrorResponse("Invalid user ID", HttpStatus.BAD_REQUEST);
            }

            int count = followingService.getNumberOfFollowers(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("followersCount", count);
            response.put("message", "Followers count retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Invalid user ID: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DataAccessException e) {
            return buildErrorResponse("Database error while fetching followers count",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{followerId}/follow/{followingId}")
    public ResponseEntity<?> followUser(
            @PathVariable int followerId,
            @PathVariable int followingId) {
        try {
            // Validation checks
            if (followerId < 0 || followingId < 0) {
                return buildErrorResponse("User IDs must be positive numbers", HttpStatus.BAD_REQUEST);
            }

            if (followerId == followingId) {
                return buildErrorResponse("Users cannot follow themselves", HttpStatus.BAD_REQUEST);
            }

            followingService.followUser(followerId, followingId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User followed successfully");
            response.put("followerId", followerId);
            response.put("followingId", followingId);
            response.put("status", "success");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Invalid input: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DataIntegrityViolationException e) {
            return buildErrorResponse("Follow relationship already exists or user not found",
                    HttpStatus.CONFLICT);
        } catch (DataAccessException e) {
            return buildErrorResponse("Database error while creating follow relationship",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{userId}/unfollow/{unfollowingId}")
    public ResponseEntity<?> unfollowUser(
            @PathVariable int userId,
            @PathVariable int unfollowingId) {
        try {
            // Validation checks
            if (userId < 0 || unfollowingId < 0) {
                return buildErrorResponse("User IDs must be positive numbers", HttpStatus.BAD_REQUEST);
            }

            if (userId == unfollowingId) {
                return buildErrorResponse("Invalid unfollow operation", HttpStatus.BAD_REQUEST);
            }

            followingService.unfollowUser(unfollowingId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User unfollowed successfully");
            response.put("userId", userId);
            response.put("unfollowedUserId", unfollowingId);
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Invalid input: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (EmptyResultDataAccessException e) {
            return buildErrorResponse("Follow relationship not found", HttpStatus.NOT_FOUND);
        } catch (DataAccessException e) {
            return buildErrorResponse("Database error while removing follow relationship",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{followerId}/{followingId}")
    public ResponseEntity<?> checkFollowStatus(
            @PathVariable int followerId,
            @PathVariable int followingId) {
        try {
            if (followerId < 0 || followingId < 0) {
                return buildErrorResponse("User IDs must be positive numbers", HttpStatus.BAD_REQUEST);
            }

            // This would require adding a method in the service to check if a follow exists
            Map<String, Object> response = new HashMap<>();
            response.put("followerId", followerId);
            response.put("followingId", followingId);
            response.put("message", "Use this endpoint to check follow status");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return buildErrorResponse("Error checking follow status: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("status", String.valueOf(status.value()));
        error.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return buildErrorResponse("Invalid argument: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return buildErrorResponse("Data integrity violation: Duplicate follow or invalid user reference",
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDataAccess(DataAccessException e) {
        return buildErrorResponse("Database access error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return buildErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}