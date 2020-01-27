package org.socialhistoryservices.delivery.user.entity;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

/**
 * A permission a group (and thus user) can have.
 */
@Entity
@Table(name = "authorities")
public class Authority implements GrantedAuthority {
    /**
     * The Authority's id.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    /**
     * Get the Authority's id.
     *
     * @return the Authority's id.
     */
    public int getId() {
        return id;
    }

    /**
     * Set the Authority's id.
     *
     * @param id the Authority's id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * The Authority's name.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Get the Authority's name.
     *
     * @return the Authority's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the Authority's name.
     *
     * @param name the Authority's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Authority's description.
     */
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * Get the Authority's description.
     *
     * @return the Authority's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the Authority's description.
     *
     * @param description the Authority's description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the name of the role.
     *
     * @return The name of the role.
     */
    public String getAuthority() {
        return name;
    }

    /**
     * Set defaults
     */
    public Authority() {
        setDescription("");
        setName("");
    }

    /**
     * Default constructor.
     *
     * @param name        The name of the permission.
     * @param description The description of the permission.
     */
    public Authority(String name, String description) {
        setDescription(description);
        setName(name);
    }

    public String toString() {
        return name;
    }
}
