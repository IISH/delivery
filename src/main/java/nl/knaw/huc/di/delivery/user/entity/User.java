package nl.knaw.huc.di.delivery.user.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A user of the system.
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

    /**
     * The User's id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_sequence_generator")
    @SequenceGenerator(
            name = "global_sequence_generator",
            sequenceName = "users_id_seq",
            allocationSize = 50
    )
    @Column(name = "id")
    private Integer id;

    /**
     * Get the User's id.
     *
     * @return the User's id.
     */
    public int getId() {
        return id;
    }

    /**
     * Set the User's id.
     *
     * @param id the User's id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * The User's username.
     */
    @Column(name = "username", nullable = false)
    private String username;

    /**
     * The User's email address.
     */
    @Column(name = "email", nullable = false)
    private String email;

    /**
     * The sub is the unique identifier associated with the openId account.
     */
    @Column(name = "sub", nullable = false)
    private String sub;

    /**
     * Get the User's username.
     *
     * @return the User's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the User's username.
     *
     * @param username the User's username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * The group this user is in.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_groups",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<Group> groups;

    /**
     * Get the User's groups.
     *
     * @return The groups.
     */
    public Set<Group> getGroups() {
        return groups;
    }

    /**
     * Get the authorities this user has.
     *
     * @return A collection of authorities (can be empty).
     */
    public Collection<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> roles = new HashSet<>();
        for (Group gr : getGroups()) {
            roles.addAll(gr.getPermissions());
        }
        return roles;
    }

    /**
     * Returns 'noPassWithCas' since CAS implementation does not use this.
     *
     * @return The password.
     */
    public String getPassword() {
        return "noPassWithCas";
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    /**
     * Set defaults.
     */
    public User() {
        setUsername("");
        groups = new HashSet<>();
    }

    /**
     * Get the user e-mail
     * @return The mail address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the user e-mail
     * @param email The mail address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    @Override
    public String toString() {
        String[] groups = this.getGroups().stream().map(Group::getName).toArray(String[]::new);
        return "Demo user " + this.getUsername() + ":" + this.getPassword() + String.join(":", groups);
    }
}