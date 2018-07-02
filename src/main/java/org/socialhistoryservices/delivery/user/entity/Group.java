package org.socialhistoryservices.delivery.user.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * A Group a user can be in.
 */
@Entity
@Table(name="groups")
public class Group {
    /** The Group's id. */
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;

    /**
     * Get the Group's id.
     * @return the Group's id.
     */
    public int getId() {
        return id;
    }

    /**
     * Set the Group's id.
     * @param id the Group's id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /** The Group's name. */
    @Column(name="name", nullable=false)
    private String name;

    /**
     * Get the Group's name.
     * @return the Group's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the Group's name.
     * @param name the Group's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /** The Group's description. */
    @Column(name="description", nullable=false)
    private String description;

    /**
     * Get the Group's description.
     * @return the Group's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the Group's description.
     * @param description the Group's description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** The Group's permissions. */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="group_permissions",
      joinColumns=@JoinColumn(name="group_id"),
      inverseJoinColumns=@JoinColumn(name="permission_id"))

    private Set<Authority> permissions;

    /**
     * Get the Group's permissions.
     * @return the Group's permissions.
     */
    public Set<Authority> getPermissions() {
        return permissions;
    }

    /**
     * Check whether this group has a specific permission.
     * @param permission The permission type to check for.
     * @return Whether this group has the permission.
     */
    public boolean hasPermission(String permission) {
        for (Authority perm : permissions) {
            if (perm.getName().equals(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set defaults.
     */
    public Group() {
        permissions = new HashSet<>();
        setName("");
        setDescription("");
    }
}
