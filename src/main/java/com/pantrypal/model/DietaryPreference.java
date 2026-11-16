package com.pantrypal.model;

import com.pantrypal.domain.Allergen;
import com.pantrypal.domain.DietType;
import jakarta.persistence.*;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/** A user's diet and allergies. Recipes that conflict with these are never recommended. */
@Entity
@Table(name = "dietary_preferences",
        uniqueConstraints = @UniqueConstraint(name = "uk_dietpref_user", columnNames = "user_id"))
public class DietaryPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dietpref_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "diet_type", nullable = false, length = 20)
    private DietType dietType = DietType.GENERAL;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dietary_preference_allergies",
            joinColumns = @JoinColumn(name = "preference_id", foreignKey = @ForeignKey(name = "fk_allergy_pref")))
    @Enumerated(EnumType.STRING)
    @Column(name = "allergen", nullable = false, length = 20)
    private Set<Allergen> allergies = new HashSet<>();

    public DietaryPreference() {}

    public DietaryPreference(DietType dietType) { this.dietType = dietType; }

    public Long getId() { return id; }
    public User getUser() { return user; }
    void setUser(User user) { this.user = user; }
    public DietType getDietType() { return dietType; }
    public void setDietType(DietType dietType) { this.dietType = dietType; }

    public Set<Allergen> getAllergies() {
        return allergies.isEmpty() ? EnumSet.noneOf(Allergen.class) : EnumSet.copyOf(allergies);
    }

    public void setAllergies(Set<Allergen> allergies) {
        this.allergies.clear();
        if (allergies != null) this.allergies.addAll(allergies);
    }
}
