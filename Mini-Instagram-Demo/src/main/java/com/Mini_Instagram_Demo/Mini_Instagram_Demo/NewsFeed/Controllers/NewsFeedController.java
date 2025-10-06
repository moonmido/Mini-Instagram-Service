package com.Mini_Instagram_Demo.Mini_Instagram_Demo.NewsFeed.Controllers;

import com.Mini_Instagram_Demo.Mini_Instagram_Demo.NewsFeed.Services.NewsFeedService;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/newsfeed")
public class NewsFeedController {

    private final NewsFeedService newsFeedService;

    public NewsFeedController(NewsFeedService newsFeedService) {
        this.newsFeedService = newsFeedService;
    }

    @GetMapping("/followed/{followerId}")
    public ResponseEntity<?> getNewsFeedFollowedUsers(@PathVariable int followerId) {
        try {
            // Validate follower ID
            if (followerId < 0) {
                return buildErrorResponse("Follower ID must be a positive number",
                        HttpStatus.BAD_REQUEST);
            }

            List<Resource> newsFeed = newsFeedService.getNewsFeedFollowedUsers(followerId);

            // Check if news feed is empty
            if (newsFeed == null || newsFeed.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "No photos available in news feed");
                response.put("followerId", followerId);
                response.put("photoCount", 0);
                return ResponseEntity.ok(response);
            }

            // Prepare success response with metadata
            Map<String, Object> response = new HashMap<>();
            response.put("message", "News feed retrieved successfully");
            response.put("followerId", followerId);
            response.put("photoCount", newsFeed.size());
            response.put("photos", newsFeed);
            response.put("status", "success");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Invalid follower ID: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (NullPointerException e) {
            return buildErrorResponse("No followed users found for this user",
                    HttpStatus.NOT_FOUND);
        } catch (MalformedURLException e) {
            return buildErrorResponse("Invalid photo path encountered: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            // This catches the RuntimeException thrown in the stream when converting photos
            if (e.getMessage() != null && e.getMessage().contains("Invalid photo path")) {
                return buildErrorResponse("Error loading photo: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return buildErrorResponse("Error loading photos: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error fetching news feed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/followed/{followerId}/count")
    public ResponseEntity<?> getNewsFeedCount(@PathVariable int followerId) {
        try {
            if (followerId < 0) {
                return buildErrorResponse("Follower ID must be a positive number",
                        HttpStatus.BAD_REQUEST);
            }

            List<Resource> newsFeed = newsFeedService.getNewsFeedFollowedUsers(followerId);

            Map<String, Object> response = new HashMap<>();
            response.put("followerId", followerId);
            response.put("photoCount", newsFeed != null ? newsFeed.size() : 0);
            response.put("message", "Photo count retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Invalid follower ID: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (NullPointerException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("followerId", followerId);
            response.put("photoCount", 0);
            response.put("message", "No followed users found");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Error fetching photo count: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/followed/{followerId}/check")
    public ResponseEntity<?> checkNewsFeedAvailability(@PathVariable int followerId) {
        try {
            if (followerId < 0) {
                return buildErrorResponse("Follower ID must be a positive number",
                        HttpStatus.BAD_REQUEST);
            }

            List<Resource> newsFeed = newsFeedService.getNewsFeedFollowedUsers(followerId);

            Map<String, Object> response = new HashMap<>();
            response.put("followerId", followerId);
            response.put("hasPhotos", newsFeed != null && !newsFeed.isEmpty());
            response.put("photoCount", newsFeed != null ? newsFeed.size() : 0);
            response.put("message", "News feed availability checked");

            return ResponseEntity.ok(response);

        } catch (NullPointerException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("followerId", followerId);
            response.put("hasPhotos", false);
            response.put("photoCount", 0);
            response.put("message", "User is not following anyone");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Error checking news feed: " + e.getMessage(),
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

    // Global exception handlers for this controller
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return buildErrorResponse("Invalid argument: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, String>> handleNullPointer(NullPointerException e) {
        return buildErrorResponse("User has no followed users or data not found",
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MalformedURLException.class)
    public ResponseEntity<Map<String, String>> handleMalformedURL(MalformedURLException e) {
        return buildErrorResponse("Invalid photo path: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        if (e.getMessage() != null && e.getMessage().contains("Invalid photo path")) {
            return buildErrorResponse("Error processing photo: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return buildErrorResponse("Runtime error: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
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