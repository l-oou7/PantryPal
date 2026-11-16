package com.pantrypal.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** A user's shopping list. Each user has one active list. */
@Entity
@Table(name = "shopping_lists", uniqueConstraints = @UniqueConstraint(name = "uk_shopping_list_user", columnNames = "user_id"))
public class ShoppingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_shopping_lists_user"))
    private User user;

    @Column(nullable = false, length = 80)
    private String name = "My Shopping List";

    /** Date range of the meal plan the automatic items were generated from (null if never generated). */
    @Column(name = "generated_from")
    private LocalDate generatedFrom;

    @Column(name = "generated_to")
    private LocalDate generatedTo;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("purchased ASC, category ASC, name ASC")
    private List<ShoppingListItem> items = new ArrayList<>();

    protected ShoppingList() {}

    public ShoppingList(User user) { this.user = user; }

    public void addItem(ShoppingListItem item) {
        item.setShoppingList(this);
        items.add(item);
        touch();
    }

    public void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getName() { return name; }
    public LocalDate getGeneratedFrom() { return generatedFrom; }
    public LocalDate getGeneratedTo() { return generatedTo; }

    public void setGeneratedRange(LocalDate from, LocalDate to) {
        this.generatedFrom = from;
        this.generatedTo = to;
        touch();
    }

    public Instant getUpdatedAt() { return updatedAt; }
    public List<ShoppingListItem> getItems() { return items; }
}
