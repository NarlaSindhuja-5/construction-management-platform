package com.e2e.construction.service;

import com.e2e.construction.dto.ProjectRequest;
import com.e2e.construction.dto.ProjectResponse;
import com.e2e.construction.entity.Contractor;
import com.e2e.construction.entity.Project;
import com.e2e.construction.entity.ProjectStatus;
import com.e2e.construction.entity.ProjectType;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.ContractorRepository;
import com.e2e.construction.repository.ProjectRepository;
import com.e2e.construction.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ContractorRepository contractorRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ContractorRepository contractorRepository,
            UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.contractorRepository = contractorRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new construction project. Only authenticated CONTRACTOR users can create projects.
     */
    @Transactional
    public ProjectResponse createProject(String userEmail, ProjectRequest request) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        // Enforce CONTRACTOR role
        if (!user.getRole().getName().equalsIgnoreCase("CONTRACTOR")) {
            throw new BadRequestException("Only authenticated CONTRACTOR users can create projects. Current role: " + user.getRole().getName());
        }

        // Resolve contractor profile
        Contractor contractor = contractorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Please create your Contractor Profile before creating projects. Use POST /api/contractors/profile."));

        Project project = new Project();
        project.setContractor(contractor);
        project.setName(request.getName().trim());
        project.setProjectType(request.getProjectType());
        project.setClientName(request.getClientName() != null ? request.getClientName().trim() : null);
        project.setLocation(request.getLocation().trim());
        project.setCity(request.getCity().trim());
        project.setState(request.getState().trim());
        project.setStartDate(request.getStartDate());
        project.setExpectedCompletionDate(request.getExpectedCompletionDate());
        project.setBudget(request.getBudget());
        project.setDescription(request.getDescription().trim());
        project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.PLANNED);
        project.setProgressPercentage(request.getProgressPercentage() != null ? request.getProgressPercentage() : 0);

        project = projectRepository.save(project);
        return ProjectResponse.fromEntity(project);
    }

    /**
     * View project by ID.
     */
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        return ProjectResponse.fromEntity(project);
    }

    /**
     * Update project. A contractor must only be able to modify their own projects.
     */
    @Transactional
    public ProjectResponse updateProject(Long id, String userEmail, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        // Strict ownership check: Only the project's contractor can modify it
        if (!project.getContractor().getUser().getEmail().equalsIgnoreCase(userEmail.trim())) {
            throw new AccessDeniedException("Access denied: You can only modify your own projects.");
        }

        project.setName(request.getName().trim());
        project.setProjectType(request.getProjectType());
        if (request.getClientName() != null) project.setClientName(request.getClientName().trim());
        project.setLocation(request.getLocation().trim());
        project.setCity(request.getCity().trim());
        project.setState(request.getState().trim());
        if (request.getStartDate() != null) project.setStartDate(request.getStartDate());
        if (request.getExpectedCompletionDate() != null) project.setExpectedCompletionDate(request.getExpectedCompletionDate());
        project.setBudget(request.getBudget());
        project.setDescription(request.getDescription().trim());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        if (request.getProgressPercentage() != null) project.setProgressPercentage(request.getProgressPercentage());

        project = projectRepository.save(project);
        return ProjectResponse.fromEntity(project);
    }

    /**
     * List projects created by the authenticated contractor, with optional filtering.
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getContractorProjects(String userEmail, ProjectStatus status, ProjectType projectType, String search) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        return contractorRepository.findByUserId(user.getId())
                .map(contractor -> {
                    String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
                    return projectRepository.searchAndFilterContractorProjects(contractor.getId(), status, projectType, cleanSearch)
                            .stream()
                            .map(ProjectResponse::fromEntity)
                            .collect(Collectors.toList());
                })
                .orElse(Collections.emptyList());
    }

    /**
     * Search and filter all projects by status, project type, or keyword search.
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> searchAndFilterProjects(ProjectStatus status, ProjectType projectType, String search) {
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        return projectRepository.searchAndFilterProjects(status, projectType, cleanSearch)
                .stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
