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

import java.net.URL;
import java.util.Objects;

/**
 * See documentation at https://dev.onedrive.com/facets/permission_facet.htm
 *
 * @since 1.0
 */
public class OneDrivePermission extends OneDriveResource {

    private static final URLTemplate PERMISSIONS_URL = new URLTemplate("/drive/items/%s/permissions/%s");

    private static final URLTemplate PERMISSIONS_ROOT_URL = new URLTemplate("/drive/root/permissions/%s");

    private final String itemId;

    private final String permissionId;

    public OneDrivePermission(OneDriveAPI api) {
        super(api);
        this.itemId = null;
        this.permissionId = null;

    }

    public OneDrivePermission(OneDriveAPI api, String permissionId) {
        super(api, "root$$" + permissionId, null);
        this.itemId = null;
        this.permissionId = Objects.requireNonNull(permissionId);
    }

    public OneDrivePermission(OneDriveAPI api, String itemId, String permissionId) {
        super(api, itemId + "$$" + permissionId, null);
        this.itemId = Objects.requireNonNull(itemId);
        this.permissionId = Objects.requireNonNull(permissionId);
    }

    public void delete() throws OneDriveAPIException {
        URL url;
        if (isRoot()) {
            url = PERMISSIONS_ROOT_URL.build(getApi().getBaseURL(), this.getPermissionId());
        } else {
            url = PERMISSIONS_URL.build(getApi().getBaseURL(), this.itemId, this.getPermissionId());
        }
        OneDriveJsonRequest request = new OneDriveJsonRequest(getApi(), url, "DELETE");
        request.send();

    }
    public boolean isRoot() {
        return itemId == null;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public class Metadata extends OneDriveResource.Metadata {

        private boolean writable;

        private OneDriveSharingLink link;
        private OneDriveInvitation invitation;

        private OneDriveIdentitySet grantedTo;

        private OneDriveFolder.Reference inheritedFrom;

        private String shareId;

        private String permissionId;

        public Metadata(JsonObject json) {
            super(json);
        }

        public boolean isWritable() {
            return writable;
        }

        public OneDriveSharingLink getLink() {
            return link;
        }

        public OneDriveIdentitySet getGrantedTo() {
            return grantedTo;
        }

        public OneDriveFolder.Reference getInheritedFrom() {
            return inheritedFrom;
        }

        public String getShareId() {
            return shareId;
        }

        @Override
        protected void parseMember(JsonObject.Member member) {
            super.parseMember(member);
            try {
                JsonValue value = member.getValue();
                String memberName = member.getName();
                if ("roles".equals(memberName)) {
                    this.writable = value.asArray()
                            .values()
                            .stream()
                            .filter(JsonValue::isString)
                            .map(JsonValue::asString)
                            .anyMatch("write"::equalsIgnoreCase);
                } else if ("link".equals(memberName)) {
                    link = new OneDriveSharingLink(value.asObject());
                } else if ("invitation".equals(memberName)) {
                    invitation = new OneDriveInvitation(value.asObject());
                } else if ("grantedTo".equals(memberName)) {
                    grantedTo = new OneDriveIdentitySet(value.asObject());
                } else if ("inheritedFrom".equals(memberName)) {
                    JsonObject valueObject = value.asObject();
                    String id = valueObject.get("id").asString();
                    OneDriveFolder inheritedFromFolder = new OneDriveFolder(getApi(), id);
                    inheritedFrom = inheritedFromFolder.new Reference(valueObject);
                } else if ("shareId".equals(memberName)) {
                    shareId = value.asString();
                }
            } catch (ParseException e) {
                throw new OneDriveRuntimeException("Parse failed, maybe a bug in client.", e);
            }
        }

        @Override
        public OneDrivePermission getResource() {
            return OneDrivePermission.this;
        }

        public OneDriveInvitation getInvitation() {
            return invitation;
        }

        public String getPermissionId() {
            return OneDrivePermission.this.getPermissionId();
        }

    }

}
