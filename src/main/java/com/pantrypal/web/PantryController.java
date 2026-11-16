package com.pantrypal.web;

import com.pantrypal.domain.Category;
import com.pantrypal.domain.Unit;
import com.pantrypal.security.PantryUserDetails;
import com.pantrypal.service.ExpiryMonitorService;
import com.pantrypal.service.PantryService;
import com.pantrypal.web.dto.PantryDtos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pantry")
public class PantryController {

    private final PantryService pantry;
    private final ExpiryMonitorService expiry;

    public PantryController(PantryService pantry, ExpiryMonitorService expiry) {
        this.pantry = pantry;
        this.expiry = expiry;
    }

    @GetMapping
    public List<PantryDtos.IngredientDto> list(@AuthenticationPrincipal PantryUserDetails me,
                                               @RequestParam(required = false) String q,
                                               @RequestParam(required = false) Category category) {
        return pantry.list(me.getId(), q, category);
    }

    /** Items ordered by waste-risk score, highest first. */
    @GetMapping("/risk")
    public List<PantryDtos.IngredientDto> risk(@AuthenticationPrincipal PantryUserDetails me) {
        return pantry.byWasteRisk(me.getId());
    }

    @PostMapping
    public ResponseEntity<PantryDtos.IngredientDto> create(@AuthenticationPrincipal PantryUserDetails me,
                                                           @Valid @RequestBody PantryDtos.IngredientRequest req) {
        PantryDtos.IngredientDto dto = pantry.create(me.getId(), req);
        expiry.scanUser(me.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    public PantryDtos.IngredientDto update(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long id,
                                           @Valid @RequestBody PantryDtos.IngredientRequest req) {
        PantryDtos.IngredientDto dto = pantry.update(me.getId(), id, req);
        expiry.scanUser(me.getId());
        return dto;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long id) {
        pantry.delete(me.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/use")
    public PantryDtos.ActionResult use(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long id,
                                       @Valid @RequestBody PantryDtos.UseRequest req) {
        return pantry.use(me.getId(), id, req.quantity());
    }

    @PostMapping("/{id}/discard")
    public PantryDtos.ActionResult discard(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long id) {
        return pantry.discard(me.getId(), id);
    }
}
