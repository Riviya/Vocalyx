package com.example.aisales_backend.controller;

import com.example.aisales_backend.dto.WorkspaceRequest;
import com.example.aisales_backend.dto.WorkspaceResponse;
import com.example.aisales_backend.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Slf4j
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            Authentication authentication,
            @Valid @RequestBody WorkspaceRequest request
    ) {
        String email = authentication.getName();
        WorkspaceResponse response = workspaceService.createWorkspaceForAdminEmail(email, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}


