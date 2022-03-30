package com.glasswork.dettbox;

/**
 * An enumeration with some String errors to show in console.
 */
public enum Messages {
    NUMBER_USERS("countUsers"),
    GROUP_NAME("groupName"),
    FIREBASE_LINK("https://dettbox-default-rtdb.europe-west1.firebasedatabase.app/"),
    SELECTED_PROFILE_IMAGE("selected_profile_image"),
    DAY("DAY"),
    WEEK("WEEK"),
    MONTH("MONTH"),
    CARA1_CONTENT("https://firebasestorage.googleapis.com/v0/b/dettbox.appspot.com/o/images%2Fcara1_content.gif?alt=media&token=fcd48127-1b26-49d7-9a26-cd67aff61359"),
    CARA1_MIG("https://firebasestorage.googleapis.com/v0/b/dettbox.appspot.com/o/images%2Fcara1_mig.gif?alt=media&token=967f39a4-5afa-4d55-af18-25c315b73054"),
    CARA1_TRIST("https://firebasestorage.googleapis.com/v0/b/dettbox.appspot.com/o/images%2Fcara1_trist.gif?alt=media&token=1bf8fe4f-194c-4d6d-9feb-ac391052391e"),
    CARA2_CONTENT("https://firebasestorage.googleapis.com/v0/b/dettbox.appspot.com/o/images%2Fcara2_content.gif?alt=media&token=55aa921a-26ca-4d5e-8a71-fd70a988da0f"),
    CARA2_MIG("https://firebasestorage.googleapis.com/v0/b/dettbox.appspot.com/o/images%2Fcara2_mig.gif?alt=media&token=c28f7948-b9f4-463a-9f9c-edc958b981dc"),
    CARA2_TRIST("https://firebasestorage.googleapis.com/v0/b/dettbox.appspot.com/o/images%2Fcara2_trist.gif?alt=media&token=6a4b23cb-627d-4af4-b3b3-265c34322a7e");



    private final String message;
    Messages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}