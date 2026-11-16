package com.pantrypal.web;

import com.pantrypal.security.PantryUserDetails;
import com.pantrypal.service.UserService;
import com.pantrypal.web.dto.ProfileDtos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService users;

    public ProfileController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public ProfileDtos.ProfileDto get(@AuthenticationPrincipal PantryUserDetails me) {
        return users.profile(me.getId());
    }

    @PutMapping
    public ProfileDtos.ProfileDto account(@AuthenticationPrincipal PantryUserDetails me,
                                          @Valid @RequestBody ProfileDtos.AccountRequest req) {
        return users.updateAccount(me.getId(), req);
    }

    @PutMapping("/dietary")
    public ProfileDtos.ProfileDto dietary(@AuthenticationPrincipal PantryUserDetails me,
                                          @Valid @RequestBody ProfileDtos.DietaryRequest req) {
        return users.updateDietary(me.getId(), req);
    }

    @PutMapping("/notifications")
    public ProfileDtos.ProfileDto notifications(@AuthenticationPrincipal PantryUserDetails me,
                                                @Valid @RequestBody ProfileDtos.NotificationRequest req) {
        return users.updateNotifications(me.getId(), req);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> password(@AuthenticationPrincipal PantryUserDetails me,
                                         @Valid @RequestBody ProfileDtos.PasswordRequest req) {
        users.changePassword(me.getId(), req);
        return ResponseEntity.noContent().build();
    }
}
