package com.glasswork.dettbox;

/**
 * An enumeration with some String errors to show in console.
 */
public enum Messages {
    NUMBER_USERS("countUsers"),
    GROUP_NAME("groupName");

    private final String message;
    Messages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}