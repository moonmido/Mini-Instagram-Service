package com.Mini_Instagram_Demo.Mini_Instagram_Demo.DownloadImages.Controllers;

import com.Mini_Instagram_Demo.Mini_Instagram_Demo.DownloadImages.Services.DownloadingService;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/download")
public class DownloadController {

    private final DownloadingService downloadingService;

    public DownloadController(DownloadingService downloadingService) {
        this.downloadingService = downloadingService;
    }

    @GetMapping("/image/{photoId}")
    public ResponseEntity<?> downloadImage(@PathVariable int photoId) {
        try {
            // Validate photo ID
            if (photoId < 0) {
                return buildErrorResponse("Photo ID must be a positive number", HttpStatus.BAD_REQUEST);
            }

            Resource resource = downloadingService.downloadImage(photoId);

            // Check if resource exists and is readable
            if (!resource.exists()) {
                return buildErrorResponse("Photo file not found on server", HttpStatus.NOT_FOUND);
            }

            if (!resource.isReadable()) {
                return buildErrorResponse("Photo file is not readable", HttpStatus.FORBIDDEN);
            }

            // Determine content type based on file extension
            String contentType = determineContentType(resource.getFilename());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()))
                    .body(resource);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Invalid photo ID: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("no Data found")) {
                return buildErrorResponse("Photo with ID " + photoId + " not found", HttpStatus.NOT_FOUND);
            }
            return buildErrorResponse("Error downloading image: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (MalformedURLException e) {
            return buildErrorResponse("Invalid file path in database: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            return buildErrorResponse("Error reading image file: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error downloading image: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/image/{photoId}/info")
    public ResponseEntity<?> getImageInfo(@PathVariable int photoId) {
        try {
            if (photoId < 0) {
                return buildErrorResponse("Photo ID must be a positive number", HttpStatus.BAD_REQUEST);
            }

            Resource resource = downloadingService.downloadImage(photoId);

            Map<String, Object> info = new HashMap<>();
            info.put("photoId", photoId);
            info.put("filename", resource.getFilename());
            info.put("exists", resource.exists());
            info.put("readable", resource.isReadable());

            try {
                info.put("size", resource.contentLength());
                info.put("sizeKB", String.format("%.2f", resource.contentLength() / 1024.0));
                info.put("sizeMB", String.format("%.2f", resource.contentLength() / (1024.0 * 1024.0)));
            } catch (IOException e) {
                info.put("size", "Unable to determine");
            }

            info.put("contentType", determineContentType(resource.getFilename()));
            info.put("message", "Image info retrieved successfully");
            info.put("status", "success");

            return ResponseEntity.ok(info);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Invalid photo ID", HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("no Data found")) {
                return buildErrorResponse("Photo with ID " + photoId + " not found", HttpStatus.NOT_FOUND);
            }
            return buildErrorResponse("Error fetching image info: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/image/{photoId}/preview")
    public ResponseEntity<?> previewImage(@PathVariable int photoId) {
        try {
            if (photoId < 0) {
                return buildErrorResponse("Photo ID must be a positive number", HttpStatus.BAD_REQUEST);
            }

            Resource resource = downloadingService.downloadImage(photoId);

            if (!resource.exists() || !resource.isReadable()) {
                return buildErrorResponse("Photo file not available", HttpStatus.NOT_FOUND);
            }

            String contentType = determineContentType(resource.getFilename());

            // For preview, use inline instead of attachment
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Invalid photo ID", HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("no Data found")) {
                return buildErrorResponse("Photo not found", HttpStatus.NOT_FOUND);
            }
            return buildErrorResponse("Error loading preview: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/image/{photoId}/exists")
    public ResponseEntity<?> checkImageExists(@PathVariable int photoId) {
        try {
            if (photoId < 0) {
                return buildErrorResponse("Photo ID must be a positive number", HttpStatus.BAD_REQUEST);
            }

            Resource resource = downloadingService.downloadImage(photoId);

            Map<String, Object> response = new HashMap<>();
            response.put("photoId", photoId);
            response.put("exists", resource.exists());
            response.put("readable", resource.isReadable());
            response.put("status", "success");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("no Data found")) {
                Map<String, Object> response = new HashMap<>();
                response.put("photoId", photoId);
                response.put("exists", false);
                response.put("message", "Photo not found in database");
                return ResponseEntity.ok(response);
            }
            return buildErrorResponse("Error checking image: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Determines content type based on file extension
     */
    private String determineContentType(String filename) {
        if (filename == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFilename.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowerFilename.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (lowerFilename.endsWith(".ico")) {
            return "image/x-icon";
        } else {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("status", String.valueOf(status.value()));
        error.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.status(status).body(error);
    }

    // Global exception handlers
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return buildErrorResponse("Invalid argument: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        if (e.getMessage() != null && e.getMessage().contains("no Data found")) {
            return buildErrorResponse("Photo not found", HttpStatus.NOT_FOUND);
        }
        return buildErrorResponse("Runtime error: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MalformedURLException.class)
    public ResponseEntity<Map<String, String>> handleMalformedURL(MalformedURLException e) {
        return buildErrorResponse("Invalid file path: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleFileNotFound(FileNotFoundException e) {
        return buildErrorResponse("Image file not found on server", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException e) {
        return buildErrorResponse("Access denied to image file", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> handleIOException(IOException e) {
        return buildErrorResponse("Error reading image file: " + e.getMessage(),
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
