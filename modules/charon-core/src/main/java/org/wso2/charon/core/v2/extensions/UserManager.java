/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.charon.core.v2.extensions;

import org.wso2.charon.core.v2.exceptions.*;
import org.wso2.charon.core.v2.utils.codeutils.Node;
import org.wso2.charon.core.v2.objects.Group;
import org.wso2.charon.core.v2.objects.User;

import java.util.List;

/**
 * This is the interface for UserManager extension.
 * An implementation can plugin their own user manager-(either LDAP based, DB based etc)
 * by implementing this interface and mentioning it in configuration.
 */
public interface UserManager {

        /***************User Manipulation operations*******************/

    public User createUser(User user) throws CharonException, ConflictException, BadRequestException;

    public User getUser(String id) throws CharonException,BadRequestException;

    public void deleteUser(String userId) throws NotFoundException, CharonException, NotImplementedException, BadRequestException;

    public List<User> listUsers() throws CharonException, NotImplementedException, BadRequestException;

    public List<User> listUsersWithPagination(int startIndex, int count) throws NotImplementedException, CharonException, BadRequestException;

    public int getUserCount() throws NotImplementedException, CharonException, BadRequestException;

    public User updateUser(User updatedUser) throws NotImplementedException, CharonException, BadRequestException;

    public List<User> filterUsers(Node rootNode) throws NotImplementedException, CharonException, BadRequestException;

    public List<User> sortUsers(String sortBy, String sortOrder) throws NotImplementedException, CharonException, BadRequestException;

    public User getMe(String userName) throws CharonException, BadRequestException, NotFoundException;

    public User createMe(User user) throws CharonException, ConflictException, BadRequestException;

    public void deleteMe(String userName) throws NotFoundException, CharonException, NotImplementedException, BadRequestException;

    public User updateMe(User updatedUser) throws NotImplementedException, CharonException, BadRequestException;


   /* ****************Group manipulation operations********************/

    public Group createGroup(Group group) throws CharonException, ConflictException, NotImplementedException, BadRequestException;

    public Group getGroup(String id) throws NotImplementedException, BadRequestException, CharonException;

    public void deleteGroup(String id) throws NotFoundException, CharonException, NotImplementedException, BadRequestException;

    public List<Group> listGroups() throws CharonException, NotImplementedException, BadRequestException;

    public int getGroupCount() throws NotImplementedException, BadRequestException, CharonException;

    public List<Group> listGroupsWithPagination(int startIndex, int count) throws NotImplementedException, BadRequestException, CharonException;

    public List<Group> filterGroups(Node rootNode) throws NotImplementedException, BadRequestException, CharonException;

    public List<Group> sortGroups(String sortByAttributeURI, String sortOrder) throws NotImplementedException, BadRequestException, CharonException;

    public Group updateGroup(Group oldGroup, Group newGroup) throws NotImplementedException, BadRequestException, CharonException;
}