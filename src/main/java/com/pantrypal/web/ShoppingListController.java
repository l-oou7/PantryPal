package com.pantrypal.web;

import com.pantrypal.security.PantryUserDetails;
import com.pantrypal.service.ShoppingListService;
import com.pantrypal.web.dto.ShoppingDtos;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shopping-list")
public class ShoppingListController {

    private final ShoppingListService shopping;

    public ShoppingListController(ShoppingListService shopping) {
        this.shopping = shopping;
    }

    @GetMapping
    public ShoppingDtos.ListDto get(@AuthenticationPrincipal PantryUserDetails me) {
        return shopping.get(me.getId());
    }

    @PostMapping("/generate")
    public ShoppingDtos.GenerateResult generate(@AuthenticationPrincipal PantryUserDetails me,
                                                @RequestBody(required = false) ShoppingDtos.GenerateRequest req) {
        return shopping.generate(me.getId(), req == null ? null : req.from(), req == null ? null : req.to());
    }

    @PostMapping("/items")
    public ShoppingDtos.ListDto add(@AuthenticationPrincipal PantryUserDetails me,
                                   @Valid @RequestBody ShoppingDtos.ItemRequest req) {
        return shopping.addItem(me.getId(), req);
    }

    @PutMapping("/items/{id}")
    public ShoppingDtos.ListDto update(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long id,
                                      @Valid @RequestBody ShoppingDtos.ItemRequest req) {
        return shopping.updateItem(me.getId(), id, req);
    }

    @PatchMapping("/items/{id}/purchased")
    public ShoppingDtos.ListDto purchased(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long id,
                                         @RequestBody Map<String, Boolean> body) {
        return shopping.setPurchased(me.getId(), id, Boolean.TRUE.equals(body.get("purchased")));
    }

    @DeleteMapping("/items/{id}")
    public ShoppingDtos.ListDto delete(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long id) {
        return shopping.deleteItem(me.getId(), id);
    }

    @PostMapping("/clear-purchased")
    public ShoppingDtos.ListDto clearPurchased(@AuthenticationPrincipal PantryUserDetails me) {
        return shopping.clearPurchased(me.getId());
    }

    @PostMapping("/move-to-pantry")
    public ShoppingDtos.ListDto moveToPantry(@AuthenticationPrincipal PantryUserDetails me) {
        return shopping.movePurchasedToPantry(me.getId());
    }
}
