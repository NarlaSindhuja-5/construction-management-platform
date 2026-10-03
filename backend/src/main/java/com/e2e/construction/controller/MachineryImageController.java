package com.e2e.construction.controller;

import com.e2e.construction.dto.MachineryImageResponse;
import com.e2e.construction.entity.MachineryImageType;
import com.e2e.construction.service.MachineryImageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/machinery")
public class MachineryImageController {

    private final MachineryImageService machineryImageService;

    public MachineryImageController(MachineryImageService machineryImageService) {
        this.machineryImageService = machineryImageService;
    }

    /**
     * POST /api/machinery/{machineId}/images
     * Upload a real machine photograph. Only the owner can upload images for their machine.
     */
    @PostMapping(value = "/{machineId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MACHINERY_OWNER')")
    public ResponseEntity<MachineryImageResponse> uploadImage(
            @PathVariable Long machineId,
            Principal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "imageType", required = false) MachineryImageType imageType) {

        MachineryImageResponse response = machineryImageService.uploadImage(
                machineId,
                principal.getName(),
                file,
                imageType
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/machinery/{machineId}/images
     * Retrieve metadata list of all images uploaded for a machine.
     */
    @GetMapping("/{machineId}/images")
    public ResponseEntity<List<MachineryImageResponse>> getMachineImages(@PathVariable Long machineId) {
        List<MachineryImageResponse> responses = machineryImageService.getMachineImages(machineId);
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/machinery/images/{storedFileName}
     * Download or stream the raw image file for display in browsers and apps.
     */
    @GetMapping("/images/{storedFileName:.+}")
    public ResponseEntity<Resource> viewImage(@PathVariable String storedFileName) {
        Resource resource = machineryImageService.getImageResource(storedFileName);

        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * DELETE /api/machinery/images/{imageId}
     * Delete an image from disk and database. Only the owner can delete their machine's images.
     */
    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasRole('MACHINERY_OWNER')")
    public ResponseEntity<Map<String, String>> deleteImage(
            @PathVariable Long imageId,
            Principal principal) {

        machineryImageService.deleteImage(imageId, principal.getName());
        return ResponseEntity.ok(Map.of(
                "message", "Machinery image deleted successfully",
                "imageId", imageId.toString()
        ));
    }
}
