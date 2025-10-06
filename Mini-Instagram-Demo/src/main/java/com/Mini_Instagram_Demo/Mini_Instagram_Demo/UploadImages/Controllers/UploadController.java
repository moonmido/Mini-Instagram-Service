package com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Controllers;

import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Inputs.LanLonInput;
import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Models.Photo;
import com.Mini_Instagram_Demo.Mini_Instagram_Demo.UploadImages.Services.UploadService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService uploadService;

    // Allowed image types
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(value = "/image/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @PathVariable int userId,
            @RequestParam("image") MultipartFile image,
            @RequestParam("photoLan") int photoLan,
            @RequestParam("photoLon") int photoLon,
            @RequestParam("userLan") int userLan,
            @RequestParam("userLon") int userLon) {

        try {
            // Validate user ID
            if (userId < 0) {
                return buildErrorResponse("User ID must be a positive number", HttpStatus.BAD_REQUEST);
            }

            // Validate image file
            ResponseEntity<?> validationError = validateImageFile(image);
            if (validationError != null) {
                return validationError;
            }

            // Validate coordinates
            ResponseEntity<?> coordinateError = validateCoordinates(photoLan, photoLon, userLan, userLon);
            if (coordinateError != null) {
                return coordinateError;
            }

            LanLonInput lanLonInput = new LanLonInput(photoLan, photoLon, userLan, userLon);
            Photo uploadedPhoto = uploadService.uploadImage(userId, image, lanLonInput);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Image uploaded successfully");
            response.put("photo", uploadedPhoto);
            response.put("status", "success");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Invalid input: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (FileAlreadyExistsException e) {
            return buildErrorResponse("A file with this name already exists", HttpStatus.CONFLICT);
        } catch (AccessDeniedException e) {
            return buildErrorResponse("Access denied: Cannot write to upload directory",
                    HttpStatus.FORBIDDEN);
        } catch (FileNotFoundException e) {
            return buildErrorResponse("Upload directory not found", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            return buildErrorResponse("Error saving image file: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (DataAccessException e) {
            return buildErrorResponse("Database error while saving photo metadata",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error during upload: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/image-with-body/{userId}",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> uploadImageWithBody(
            @PathVariable int userId,
            @RequestPart("image") MultipartFile image,
            @RequestPart("location") LanLonInput lanLonInput) {

        try {
            // Validate user ID
            if (userId < 0) {
                return buildErrorResponse("User ID must be a positive number", HttpStatus.BAD_REQUEST);
            }

            // Validate image file
            ResponseEntity<?> validationError = validateImageFile(image);
            if (validationError != null) {
                return validationError;
            }

            // Validate location data
            if (lanLonInput == null) {
                return buildErrorResponse("Location data is required", HttpStatus.BAD_REQUEST);
            }

            ResponseEntity<?> coordinateError = validateCoordinates(
                    lanLonInput.photoLan(), lanLonInput.photoLon(),
                    lanLonInput.userLan(), lanLonInput.userLon());
            if (coordinateError != null) {
                return coordinateError;
            }

            Photo uploadedPhoto = uploadService.uploadImage(userId, image, lanLonInput);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Image uploaded successfully");
            response.put("photo", uploadedPhoto);
            response.put("status", "success");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Invalid input: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (FileAlreadyExistsException e) {
            return buildErrorResponse("A file with this name already exists", HttpStatus.CONFLICT);
        } catch (AccessDeniedException e) {
            return buildErrorResponse("Access denied: Cannot write to upload directory",
                    HttpStatus.FORBIDDEN);
        } catch (IOException e) {
            return buildErrorResponse("Error saving image file: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (DataAccessException e) {
            return buildErrorResponse("Database error while saving photo metadata",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Unexpected error during upload: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Validates the uploaded image file
     */
    private ResponseEntity<?> validateImageFile(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return buildErrorResponse("Image file is required and cannot be empty", HttpStatus.BAD_REQUEST);
        }

        // Check file size
        if (image.getSize() > MAX_FILE_SIZE) {
            return buildErrorResponse("File size exceeds maximum allowed size of 10MB",
                    HttpStatus.PAYLOAD_TOO_LARGE);
        }

        // Check content type
        String contentType = image.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            return buildErrorResponse("Invalid file type. Allowed types: JPEG, PNG, GIF, WEBP",
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        // Check original filename
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return buildErrorResponse("Invalid file name", HttpStatus.BAD_REQUEST);
        }

        // Check for potentially dangerous file extensions
        if (containsDangerousExtension(originalFilename)) {
            return buildErrorResponse("File name contains invalid characters or extension",
                    HttpStatus.BAD_REQUEST);
        }

        return null; // Validation passed
    }

    /**
     * Validates coordinate values
     */
    private ResponseEntity<?> validateCoordinates(double photoLan, double photoLon,
                                                  double userLan, double userLon) {
        // Validate latitude range (-90 to 90)
        if (photoLan < -90 || photoLan > 90) {
            return buildErrorResponse("Photo latitude must be between -90 and 90",
                    HttpStatus.BAD_REQUEST);
        }
        if (userLan < -90 || userLan > 90) {
            return buildErrorResponse("User latitude must be between -90 and 90",
                    HttpStatus.BAD_REQUEST);
        }

        // Validate longitude range (-180 to 180)
        if (photoLon < -180 || photoLon > 180) {
            return buildErrorResponse("Photo longitude must be between -180 and 180",
                    HttpStatus.BAD_REQUEST);
        }
        if (userLon < -180 || userLon > 180) {
            return buildErrorResponse("User longitude must be between -180 and 180",
                    HttpStatus.BAD_REQUEST);
        }

        return null; // Validation passed
    }

    /**
     * Checks if the content type is a valid image type
     */
    private boolean isValidImageType(String contentType) {
        for (String allowedType : ALLOWED_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for potentially dangerous file extensions
     */
    private boolean containsDangerousExtension(String filename) {
        String lowerFilename = filename.toLowerCase();
        String[] dangerousExtensions = {".exe", ".bat", ".cmd", ".sh", ".php", ".jsp", ".asp"};

        for (String ext : dangerousExtensions) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("status", String.valueOf(status.value()));
        error.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.status(status).body(error);
    }

    // Global exception handlers
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSizeException(MaxUploadSizeExceededException e) {
        return buildErrorResponse("File size exceeds maximum upload limit",
                HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return buildErrorResponse("Invalid argument: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> handleIOException(IOException e) {
        return buildErrorResponse("File operation error: " + e.getMessage(),
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