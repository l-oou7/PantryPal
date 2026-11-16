package com.pantrypal.repository;

import com.pantrypal.model.DietaryPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DietaryPreferenceRepository extends JpaRepository<DietaryPreference, Long> {
    Optional<DietaryPreference> findByUserId(Long userId);
}
