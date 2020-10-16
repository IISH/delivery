package org.socialhistoryservices.delivery.user.service;

import org.socialhistoryservices.delivery.user.dao.GroupDAO;
import org.socialhistoryservices.delivery.user.dao.UserDAO;
import org.socialhistoryservices.delivery.user.entity.Group;
import org.socialhistoryservices.delivery.user.entity.User;
import org.socialhistoryservices.delivery.user.entity.User_;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Represents the service of the user package.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {
    /**
     * user DAO, do not autowire unless userServiceDetails bean removed
     */
    private UserDAO userDAO;

    /**
     * group DAO, do not autowire unless userServiceDetails bean removed
     */
    private GroupDAO groupDAO;

    /**
     * Add a User to the database.
     *
     * @param obj User to add.
     */
    public void addUser(User obj) {
        userDAO.add(obj);
    }

    /**
     * Remove a User from the database.
     *
     * @param obj User to remove.
     */
    public void removeUser(User obj) {
        userDAO.remove(obj);
    }

    /**
     * Save changes to a User in the database.
     *
     * @param obj User to save.
     */
    public void saveUser(User obj) {
        userDAO.save(obj);
    }

    /**
     * Retrieve the User matching the given Id.
     *
     * @param id Id of the User to retrieve.
     * @return The User matching the Id.
     */
    public User getUserById(int id) {
        return userDAO.getById(id);
    }

    /**
     * Retrieve the User matching the given username.
     *
     * @param name Username of the User to retrieve.
     * @return The User matching the username.
     */
    public User getUserByName(String name) {
        CriteriaBuilder builder = getUserCriteriaBuilder();

        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        query.select(root);

        query.where(builder.equal(builder.lower(root.get(User_.username)), name.toLowerCase()));

        return getUser(query);
    }

    /**
     * Load a user by its user name.
     *
     * @param name The name/e-mail of the user.
     * @return The UserDetails of this user (User object) if successful.
     * @throws UsernameNotFoundException Thrown when the user was not found.
     * @throws DataAccessException       Thrown when there was a problem accessing the database.
     */
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException, DataAccessException {
        User u = getUserByName(name);
        if (u == null) {
            throw new UsernameNotFoundException(name + " is no valid user");
        }
        return u;
    }

    /**
     * Get a criteria builder for querying Users.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getUserCriteriaBuilder() {
        return userDAO.getCriteriaBuilder();
    }

    /**
     * List all Users matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching Users.
     */
    public List<User> listUsers(CriteriaQuery<User> query) {
        return userDAO.list(query);
    }

    /**
     * List all Users.
     *
     * @return A list of Users.
     */
    public List<User> listUsers() {
        CriteriaBuilder builder = getUserCriteriaBuilder();

        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        query.select(root);

        return userDAO.list(query);
    }

    /**
     * Get a single User matching a built query.
     *
     * @param query The query to match by.
     * @return The matching User.
     */
    public User getUser(CriteriaQuery<User> query) {
        return userDAO.get(query);
    }

    /**
     * Add a Group to the database.
     *
     * @param obj Group to add.
     */
    public void addGroup(Group obj) {
        groupDAO.add(obj);
    }

    /**
     * Remove a Group from the database.
     *
     * @param obj Group to remove.
     */
    public void removeGroup(Group obj) {
        groupDAO.remove(obj);
    }

    /**
     * Save changes to a Group in the database.
     *
     * @param obj Group to save.
     */
    public void saveGroup(Group obj) {
        groupDAO.save(obj);
    }

    /**
     * Retrieve the Group matching the given Id.
     *
     * @param id Id of the Group to retrieve.
     * @return The Group matching the Id.
     */
    public Group getGroupById(int id) {
        return groupDAO.getById(id);
    }

    /**
     * Get a criteria builder for querying Groups.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getGroupCriteriaBuilder() {
        return groupDAO.getCriteriaBuilder();
    }

    /**
     * List all Groups matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching Groups.
     */
    public List<Group> listGroups(CriteriaQuery<Group> query) {
        return groupDAO.list(query);
    }

    /**
     * List all Groups matching a built query.
     *
     * @return A list of matching Groups.
     */
    public List<Group> listGroups() {
        CriteriaBuilder builder = getGroupCriteriaBuilder();

        CriteriaQuery<Group> query = builder.createQuery(Group.class);
        Root<Group> root = query.from(Group.class);
        query.select(root);

        return groupDAO.list(query);
    }

    /**
     * Get a single Group matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Group.
     */
    public Group getGroup(CriteriaQuery<Group> query) {
        return groupDAO.get(query);
    }

    /**
     * Set user dao (autowiring does not work because UserService bean is
     * defined in xml).
     *
     * @param ud The user DAO to set.
     */
    public void setUserDAO(UserDAO ud) {
        userDAO = ud;
    }

    /**
     * Set group dao (autowiring does not work because UserService bean is
     * defined in xml).
     *
     * @param groupDAO The group DAO to set.
     */
    public void setGroupDAO(GroupDAO groupDAO) {
        this.groupDAO = groupDAO;
    }
}
