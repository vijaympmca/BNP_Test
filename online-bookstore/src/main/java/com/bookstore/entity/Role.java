package com.bookstore.entity;

/**
 * The two roles of the application.
 * USER          – browses books, manages own cart, places and views own orders.
 * ADMIN         – "Administrator": everything a USER can do, plus manage books,
 *                 manage users (role / enable-disable), view all orders and reports.
 */
public enum Role {
    USER("User"),
    ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Spring Security authority name, e.g. ROLE_ADMIN. */
    public String authority() {
        return "ROLE_" + name();
    }
}
