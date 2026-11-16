package com.pantrypal.repository;

import com.pantrypal.model.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {
    Optional<ShoppingListItem> findByIdAndShoppingListUserId(Long id, Long userId);
}
