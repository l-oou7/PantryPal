package com.pantrypal.web;

import com.pantrypal.security.PantryUserDetails;
import com.pantrypal.service.BookmarkService;
import com.pantrypal.web.dto.RecipeDtos;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarks;

    public BookmarkController(BookmarkService bookmarks) {
        this.bookmarks = bookmarks;
    }

    @GetMapping
    public List<RecipeDtos.RecipeCard> list(@AuthenticationPrincipal PantryUserDetails me) {
        return bookmarks.list(me.getId());
    }

    @PostMapping("/{recipeId}")
    public ResponseEntity<Void> add(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long recipeId) {
        bookmarks.add(me.getId(), recipeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{recipeId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long recipeId) {
        bookmarks.remove(me.getId(), recipeId);
        return ResponseEntity.noContent().build();
    }
}
