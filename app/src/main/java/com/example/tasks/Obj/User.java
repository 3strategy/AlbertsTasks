package com.example.tasks.Obj;

/**
 * @author		Albert Levy albert.school2015@gmail.com
 * @version     2.1
 * @since		9/3/2024
 * <p>
 * Represents a user in the application.
 * This class stores information about a user, including their unique ID and username.
 * It provides constructors for creating User objects and methods to access and modify user details.
 */
public class User {
    private String uid;
    private String username;

    /**
     * Default constructor for the User class.
     * Initializes a new User object with null values for UID and username.
     * This constructor is often used by data mapping frameworks like Firebase.
     */
    public User() {}

    /**
     * Constructs a new User object with the specified UID and username.
     *
     * @param uid The unique identifier for the user.
     * @param username The username of the user.
     */
    public User(String uid, String username) {
        this.uid = uid;
        this.username = username;
    }

    /**
     * Gets the unique identifier (UID) of the user.
     *
     * @return The UID string of the user.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the unique identifier (UID) of the user.
     *
     * @param uid The new UID string for the user.
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets the username of the user.
     *
     * @return The username string of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     *
     * @param username The new username string for the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
