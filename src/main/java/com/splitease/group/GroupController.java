package com.splitease.group;

import com.splitease.group.dto.*;
import com.splitease.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupDetailResponse> create(
            @Valid @RequestBody GroupCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GroupDetailResponse response = groupService.createGroup(req, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(groupService.listMyGroups(userDetails.getUser()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDetailResponse> get(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(groupService.getGroup(id, userDetails.getUser()));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Map<String, String>> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupService.addMember(id, req, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Member added successfully"));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Map<String, String>> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupService.removeMember(id, userId, userDetails.getUser());
        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Map<String, String>> leave(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupService.leaveGroup(id, userDetails.getUser());
        return ResponseEntity.ok(Map.of("message", "Left group successfully"));
    }
}