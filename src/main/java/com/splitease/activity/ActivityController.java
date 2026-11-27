package com.splitease.activity;

import com.splitease.activity.dto.ActivityResponse;
import com.splitease.common.ForbiddenException;
import com.splitease.group.GroupMemberRepository;
import com.splitease.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/activity")
public class ActivityController {

    private final ActivityLogRepository activityLogRepository;
    private final GroupMemberRepository groupMemberRepository;

    public ActivityController(
            ActivityLogRepository activityLogRepository,
            GroupMemberRepository groupMemberRepository
    ) {
        this.activityLogRepository = activityLogRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> list(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userDetails.getId())) {
            throw new ForbiddenException("Not a member of this group");
        }
        List<ActivityResponse> logs = activityLogRepository
                .findByGroupIdOrderByCreatedAtDesc(groupId)
                .stream()
                .map(ActivityResponse::from)
                .toList();
        return ResponseEntity.ok(logs);
    }
}