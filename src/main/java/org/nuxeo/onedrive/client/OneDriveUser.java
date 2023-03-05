/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *
 * Contributors:
 *     Kevin Leturc
 */
package org.nuxeo.onedrive.client;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;

import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @since 1.0
 */
public class OneDriveUser extends OneDriveItem {
    private static final URLTemplate USER_URL = new URLTemplate("");

    public OneDriveUser(OneDriveAPI api) {
        super(api);
    }

    public OneDriveUser(OneDriveAPI api, String id) {
        super(api, id);
    }

    @Override
    public Metadata getMetadata(OneDriveExpand... expand) throws OneDriveAPIException {
        return null;
    }

    public User getUser() throws OneDriveAPIException {
        URL url = USER_URL.build(getApi().getBaseURL());
        OneDriveJsonRequest request = new OneDriveJsonRequest(getApi(), url, "GET");
        OneDriveJsonResponse response = request.send();
        return new User(response.getContent());
    }

    /**
     * See documentation at https://dev.onedrive.com/resources/item.htm.
     */
    public class User extends OneDriveJsonObject {

        public User(JsonObject json){
            super(json);
            parseMember(json, this::parseMember);
        }

        private String displayName;

        private String surname;

        private String givenName;

        private String id;

        private String userPrincipalName;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }

        public String getGivenName() {
            return givenName;
        }

        public void setGivenName(String givenName) {
            this.givenName = givenName;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUserPrincipalName() {
            return userPrincipalName;
        }

        public void setUserPrincipalName(String userPrincipalName) {
            this.userPrincipalName = userPrincipalName;
        }
        protected void parseMember(JsonObject.Member member) {
            JsonValue value = member.getValue();
            String memberName = member.getName();
            if ("displayName".equals(memberName)) {
                displayName = value.asString();
            } else if ("surname".equals(memberName)) {
                surname = value.asString();
            } else if ("givenName".equals(memberName)) {
                givenName = value.asString();
            } else if ("id".equals(memberName)) {
                id = value.asString();
            } else if ("userPrincipalName".equals(memberName)) {
                userPrincipalName = value.asString();
            }
        }
    }

}
