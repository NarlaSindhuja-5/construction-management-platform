package com.e2e.construction.controller;

import com.e2e.construction.dto.ProjectRequest;
import com.e2e.construction.dto.ProjectResponse;
import com.e2e.construction.entity.ProjectStatus;
import com.e2e.construction.entity.ProjectType;
import com.e2e.construction.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * POST /api/projects
     * Create project. Only authenticated CONTRACTOR users can create projects.
     */
    @PostMapping
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<ProjectResponse> createProject(
            Principal principal,
            @Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/projects/{id}
     * View project by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        ProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/projects/{id}
     * Update project. A contractor must only be able to modify their own projects.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.updateProject(id, principal.getName(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/projects/my-projects
     * List projects created by the authenticated contractor with optional search and filters.
     */
    @GetMapping("/my-projects")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(
            Principal principal,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) ProjectType projectType,
            @RequestParam(required = false) String search) {
        List<ProjectResponse> responses = projectService.getContractorProjects(principal.getName(), status, projectType, search);
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/projects
     * Search and filter projects by status, project type, or keyword search.
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> searchProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) ProjectType projectType,
            @RequestParam(required = false) String search) {
        List<ProjectResponse> responses = projectService.searchAndFilterProjects(status, projectType, search);
        return ResponseEntity.ok(responses);
    }
}
