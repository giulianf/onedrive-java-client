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

import java.io.InputStream;
import java.net.URL;

/**
 * @since 1.0
 */
public class OneDriveFile extends OneDriveItem {

    private static final URLTemplate GET_FILE_URL = new URLTemplate("/drive/items/%s");

    private static final URLTemplate GET_FILE_CONTENT_URL = new URLTemplate("/drive/items/%s/content");
    private static final URLTemplate UPLOAD_ROOT_CONTENT_URL = new URLTemplate("/drive/root:%s:/content");
    private static final URLTemplate UPLOAD_CONTENT_URL = new URLTemplate("/drive/items/%s/content");
    private static final URLTemplate GET_BY_PATH_URL = new URLTemplate("/drive/root:/%s");

    public OneDriveFile(OneDriveAPI api) {
        super(api);
    }
    public OneDriveFile(OneDriveAPI api, String id) {
        super(api, id);
    }
    public OneDriveFile(OneDriveAPI api, String id, String path) {
        super(api, id, path);
    }

    @Override
    public Metadata getMetadata(OneDriveExpand... expands) throws OneDriveAPIException {
        QueryStringBuilder query = new QueryStringBuilder().set("expand", expands);
        URL url = GET_FILE_URL.build(getApi().getBaseURL(), query, getId());
        OneDriveJsonRequest request = new OneDriveJsonRequest(getApi(), url, "GET");
        OneDriveJsonResponse response = request.send();
        return new Metadata(response.getContent());
    }

    public InputStream download() throws OneDriveAPIException {
        URL url = GET_FILE_CONTENT_URL.build(getApi().getBaseURL(), getId());
        OneDriveRequest request = new OneDriveRequest(getApi(), url, "GET");
        OneDriveResponse response = request.send();
        return response.getContent();
    }
    public Metadata upload(boolean isRoot , long size, InputStream contentFromBytes) throws OneDriveAPIException {
        URL url = isRoot ? UPLOAD_ROOT_CONTENT_URL.build(getApi().getBaseURL(), getId()) : UPLOAD_CONTENT_URL.build(getApi().getBaseURL(), getId());
        OneDriveJsonRequest request = new OneDriveJsonRequest(getApi(), url, "PUT", "application/octet-stream");
        request.addHeader("Content-Length", String.valueOf(size));

        request.setBody(contentFromBytes);
        OneDriveJsonResponse response = request.send();
        return new Metadata(response.getContent());
    }

    public Metadata renameItem(String newName, String newParentFolderId) throws OneDriveAPIException {
        URL url = ITEMS_URL.build(getApi().getBaseURL(), getId());

        OneDriveJsonRequest request = new OneDriveJsonRequest(getApi(), url, "PATCH");
        JsonObject jsonObject = new JsonObject();
        JsonObject jsonParentObject = new JsonObject();
        // move if not empty
        if (newParentFolderId != null && !newParentFolderId.isEmpty()) {
            jsonParentObject.add("id", newParentFolderId);
            jsonObject.add("newParentFolderId", jsonParentObject);
        }
        jsonObject.add("name", newName);
        request.setBody(jsonObject);
        OneDriveJsonResponse response = request.send();

        return new Metadata(response.getContent());
    }
    public Metadata getByPath() throws OneDriveAPIException {
        URL url;

        url = GET_BY_PATH_URL.build(getApi().getBaseURL(), getPath());
        OneDriveJsonRequest request = new OneDriveJsonRequest(getApi(), url, "GET");
        OneDriveJsonResponse response = request.send();

        return new Metadata(response.getContent());
    }

    public URL getUrlRename(){
        return ITEMS_URL.build(getApi().getBaseURL(), getId());
    }

    /** See documentation at https://dev.onedrive.com/resources/item.htm. */
    public class Metadata extends OneDriveItem.Metadata {

        private String cTag;

        private String mimeType;

        private String downloadUrl;

        /** Not available for business. */
        private String crc32Hash;

        /** Not available for business. */
        private String sha1Hash;

        /** custom value */
        private String custom;

        public Metadata(JsonObject json) {
            super(json);
        }

        public String getCTag() {
            return cTag;
        }

        /**
         * Returns the current version of OneDrive file.
         * CAUTION: this value is known from cTag field, it doesn't rely on public field from OneDrive API.
         *
         * @return the current version of OneDrive file
         */
        public String getVersion() {
            return cTag == null ? null : cTag.substring(cTag.lastIndexOf(',') + 1);
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public String getCrc32Hash() {
            return crc32Hash;
        }

        public String getSha1Hash() {
            return sha1Hash;
        }

        @Override
        protected void parseMember(JsonObject.Member member) {
            super.parseMember(member);
            try {
                JsonValue value = member.getValue();
                String memberName = member.getName();
                if ("cTag".equals(memberName)) {
                    cTag = value.asString();
                } else if ("@content.downloadUrl".equals(memberName)) {
                    downloadUrl = value.asString();
                } else if ("file".equals(memberName)) {
                    parseMember(value.asObject(), this::parseFileMember);
                } else if ("id".equals(memberName) && getPath() != null) {
                    custom = value.asString();
                }
            } catch (ParseException e) {
                throw new OneDriveRuntimeException("Parse failed, maybe a bug in client.", e);
            }
        }

        private void parseFileMember(JsonObject.Member member) {
            JsonValue value = member.getValue();
            String memberName = member.getName();
            if ("mimeType".equals(memberName)) {
                mimeType = value.asString();
            } else if ("hashes".equals(memberName)) {
                parseMember(value.asObject(), this::parseHashesMember);
            }
        }

        private void parseHashesMember(JsonObject.Member member) {
            JsonValue value = member.getValue();
            String memberName = member.getName();
            if ("crc32Hash".equals(memberName)) {
                crc32Hash = value.asString();
            } else if ("sha1Hash".equals(memberName)) {
                sha1Hash = value.asString();
            }
        }

        @Override
        public OneDriveFile getResource() {
            return OneDriveFile.this;
        }

        @Override
        public boolean isFile() {
            return true;
        }

        @Override
        public Metadata asFile() {
            return this;
        }

        public String getCustom() {
            return custom;
        }
    }

}
