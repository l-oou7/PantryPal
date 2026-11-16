package com.pantrypal.service;

import com.pantrypal.config.PantryPalProperties;
import com.pantrypal.domain.DietType;
import com.pantrypal.model.DietaryPreference;
import com.pantrypal.model.User;
import com.pantrypal.repository.IngredientRepository;
import com.pantrypal.repository.RecipeBookmarkRepository;
import com.pantrypal.repository.UserRepository;
import com.pantrypal.web.ApiException;
import com.pantrypal.web.dto.AuthDtos;
import com.pantrypal.web.dto.ProfileDtos;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registration, profile, preferences and password management. */
@Service
public class UserService {

    private final UserRepository users;
    private final IngredientRepository ingredients;
    private final RecipeBookmarkRepository bookmarks;
    private final PasswordEncoder encoder;
    private final PantryPalProperties props;

    public UserService(UserRepository users, IngredientRepository ingredients, RecipeBookmarkRepository bookmarks,
                       PasswordEncoder encoder, PantryPalProperties props) {
        this.users = users;
        this.ingredients = ingredients;
        this.bookmarks = bookmarks;
        this.encoder = encoder;
        this.props = props;
    }

    @Transactional
    public User register(AuthDtos.RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (req.confirmPassword() != null && !req.confirmPassword().equals(req.password())) {
            throw ApiException.badRequest("The two passwords don't match.");
        }
        PasswordPolicy.check(req.password());
        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("An account with that email already exists. Try logging in!");
        }
        User u = new User(req.displayName().trim(), email, encoder.encode(req.password()));
        u.setExpiryThresholdDays(props.getDefaultThresholdDays());
        u.setDietaryPreference(new DietaryPreference(DietType.GENERAL));
        return users.save(u);
    }

    @Transactional(readOnly = true)
    public User get(Long userId) {
        return users.findById(userId).orElseThrow(() -> ApiException.notFound("Account"));
    }

    @Transactional(readOnly = true)
    public AuthDtos.CurrentUser current(Long userId) {
        User u = get(userId);
        return new AuthDtos.CurrentUser(u.getId(), u.getDisplayName(), u.getEmail(),
                preference(u).getDietType().name(), u.isCelebrationsEnabled());
    }

    @Transactional(readOnly = true)
    public ProfileDtos.ProfileDto profile(Long userId) {
        User u = get(userId);
        DietaryPreference p = preference(u);
        return new ProfileDtos.ProfileDto(u.getId(), u.getDisplayName(), u.getEmail(), u.getCreatedAt(), p.getDietType(),
                p.getAllergies(), u.isExpiryAlertsEnabled(), u.getExpiryThresholdDays(), u.isCelebrationsEnabled(),
                ingredients.countByUserId(userId), bookmarks.countByUserId(userId));
    }

    @Transactional
    public ProfileDtos.ProfileDto updateAccount(Long userId, ProfileDtos.AccountRequest req) {
        get(userId).setDisplayName(req.displayName().trim());
        return profile(userId);
    }

    @Transactional
    public ProfileDtos.ProfileDto updateDietary(Long userId, ProfileDtos.DietaryRequest req) {
        DietaryPreference p = preference(get(userId));
        p.setDietType(req.dietType());
        p.setAllergies(req.allergies());
        return profile(userId);
    }

    @Transactional
    public ProfileDtos.ProfileDto updateNotifications(Long userId, ProfileDtos.NotificationRequest req) {
        User u = get(userId);
        u.setExpiryAlertsEnabled(req.expiryAlertsEnabled());
        u.setExpiryThresholdDays(req.expiryThresholdDays());
        u.setCelebrationsEnabled(req.celebrationsEnabled());
        return profile(userId);
    }

    @Transactional
    public void changePassword(Long userId, ProfileDtos.PasswordRequest req) {
        User u = get(userId);
        if (!encoder.matches(req.currentPassword(), u.getPasswordHash())) {
            throw ApiException.badRequest("Your current password isn't right.");
        }
        PasswordPolicy.check(req.newPassword());
        u.setPasswordHash(encoder.encode(req.newPassword()));
    }

    /** Returns the user's dietary preference, creating the default one for older accounts. */
    @Transactional
    public DietaryPreference preference(User u) {
        if (u.getDietaryPreference() == null) {
            u.setDietaryPreference(new DietaryPreference(DietType.GENERAL));
        }
        return u.getDietaryPreference();
    }
}
