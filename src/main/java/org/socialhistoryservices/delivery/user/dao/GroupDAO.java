/**
 * Copyright (C) 2013 International Institute of Social History
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2011 International Institute of Social History
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.socialhistoryservices.delivery.user.dao;

import org.socialhistoryservices.delivery.user.entity.Group;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface representing the Data Access Object for user groups.
 */
public interface GroupDAO {
    /**
     * Add a Group to the database.
     * @param obj Group to add.
     */
    public void add(Group obj);

    /**
     * Remove a Group from the database.
     * @param obj Group to remove.
     */
    public void remove(Group obj);

    /**
     * Save changes to a Group in the database.
     * @param obj Group to save.
     */
    public void save(Group obj);

    /**
     * Retrieve the Group matching the given Id.
     * @param id Id of the Group to retrieve.
     * @return The Group matching the Id.
     */
    public Group getById(int id);

    /**
     * Get a criteria builder for querying Groups.
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder();

    /**
     * List all Groups matching a built query.
     * @param query The query to match by.
     * @return A list of matching Groups.
     */
    public List<Group> list(CriteriaQuery<Group> query);

    /**
     * Get a single Group matching a built query.
     * @param query The query to match by.
     * @return The matching Group.
     */
    public Group get(CriteriaQuery<Group> query);
}
