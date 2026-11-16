package com.pantrypal.web;

import com.pantrypal.security.PantryUserDetails;
import com.pantrypal.service.DashboardService;
import com.pantrypal.web.dto.DashboardDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/api/dashboard")
    public DashboardDto get(@AuthenticationPrincipal PantryUserDetails me) {
        return dashboard.build(me.getId());
    }
}
