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

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;

/**
 * @since 1.0
 */
public class OneDriveInvitation extends OneDriveJsonObject {
    private boolean signInRequired;

    private String email;

    private OneDriveIdentitySet invitedBy;

    public OneDriveInvitation(JsonObject json) {
        super(json);
    }

    @Override
    protected void parseMember(JsonObject.Member member) {
        super.parseMember(member);
        try {
            JsonValue value = member.getValue();
            String memberName = member.getName();
            if ("signInRequired".equals(memberName)) {
                signInRequired = value.asBoolean();
            } else if ("email".equals(memberName)) {
                email = value.asString();
            } else if ("invitedBy".equals(memberName)) {
                invitedBy = new OneDriveIdentitySet(value.asObject());
            }
        } catch (ParseException e) {
            throw new OneDriveRuntimeException("Parse failed, maybe a bug in client.", e);
        }
    }

    public boolean isSignInRequired() {
        return signInRequired;
    }

    public void setSignInRequired(boolean signInRequired) {
        this.signInRequired = signInRequired;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public OneDriveIdentitySet getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(OneDriveIdentitySet invitedBy) {
        this.invitedBy = invitedBy;
    }

}
