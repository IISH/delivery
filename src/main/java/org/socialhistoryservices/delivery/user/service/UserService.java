package org.socialhistoryservices.delivery.user.service;

import org.socialhistoryservices.delivery.user.entity.Group;
import org.socialhistoryservices.delivery.user.entity.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface representing the service of the user package.
 */
public interface UserService {
    /**
     * Add a User to the database.
     * @param obj User to add.
     */
    void addUser(User obj);

    /**
     * Remove a User from the database.
     * @param obj User to remove.
     */
    void removeUser(User obj);

    /**
     * Save changes to a User in the database.
     * @param obj User to save.
     */
    void saveUser(User obj);

    /**
     * Retrieve the User matching the given Id.
     * @param id Id of the User to retrieve.
     * @return The User matching the Id.
     */
    User getUserById(int id);

    /**
     * Retrieve the User matching the given username.
     * @param name Username of the User to retrieve.
     * @return The User matching the username.
     */
    User getUserByName(String name);

    /**
     * Get a criteria builder for querying Users.
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getUserCriteriaBuilder();

    /**
     * List all Users matching a built query.
     * @param query The query to match by.
     * @return A list of matching Users.
     */
    List<User> listUsers(CriteriaQuery<User> query);

    /**
     * List all Users.
     * @return A list of Users.
     */
    List<User> listUsers();

    /**
     * Get a single User matching a built query.
     * @param query The query to match by.
     * @return The matching User.
     */
    User getUser(CriteriaQuery<User> query);

    /**
     * Add a Group to the database.
     * @param obj Group to add.
     */
    void addGroup(Group obj);

    /**
     * Remove a Group from the database.
     * @param obj Group to remove.
     */
    void removeGroup(Group obj);

    /**
     * Save changes to a Group in the database.
     * @param obj Group to save.
     */
    void saveGroup(Group obj);

    /**
     * Retrieve the Group matching the given Id.
     * @param id Id of the Group to retrieve.
     * @return The Group matching the Id.
     */
    Group getGroupById(int id);

    /**
     * Get a criteria builder for querying Groups.
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getGroupCriteriaBuilder();

    /**
     * List all Groups matching a built query.
     * @param query The query to match by.
     * @return A list of matching Groups.
     */
    List<Group> listGroups(CriteriaQuery<Group> query);

    /**
     * List all Groups.
     * @return A list of Groups.
     */
    List<Group> listGroups();

    /**
     * Get a single Group matching a built query.
     * @param query The query to match by.
     * @return The matching Group.
     */
    Group getGroup(CriteriaQuery<Group> query);

}
