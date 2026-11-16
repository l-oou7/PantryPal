package com.pantrypal.model;

import com.pantrypal.domain.Category;
import com.pantrypal.domain.Unit;
import jakarta.persistence.*;

/** One line of a shopping list, generated from the meal plan or added by hand. */
@Entity
@Table(name = "shopping_list_items", indexes = @Index(name = "idx_shopping_items_list", columnList = "shopping_list_id"))
public class ShoppingListItem {

    public enum Source { AUTO, MANUAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shopping_list_id", nullable = false, foreignKey = @ForeignKey(name = "fk_shopping_items_list"))
    private ShoppingList shoppingList;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false)
    private double quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Unit unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(nullable = false)
    private boolean purchased;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Source source;

    /** For generated items: which planned recipes need it. */
    @Column(length = 400)
    private String note;

    protected ShoppingListItem() {}

    public ShoppingListItem(String name, double quantity, Unit unit, Category category, Source source, String note) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.source = source;
        this.note = note;
    }

    public Long getId() { return id; }
    public ShoppingList getShoppingList() { return shoppingList; }
    void setShoppingList(ShoppingList list) { this.shoppingList = list; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public boolean isPurchased() { return purchased; }
    public void setPurchased(boolean purchased) { this.purchased = purchased; }
    public Source getSource() { return source; }
    public String getNote() { return note; }
}
