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

import java.net.URL;
import java.util.Iterator;
import java.util.Objects;

/**
 * @since 2.x
 */
public class OneDrivePermissionIterator implements Iterator<OneDrivePermission.Metadata> {

    private final OneDriveAPI api;

    private final JsonObjectIterator jsonObjectIterator;

    public OneDrivePermissionIterator(OneDriveAPI api, URL url) {
        this.api = Objects.requireNonNull(api);
        this.jsonObjectIterator = new JsonObjectIterator(api, url) {

            @Override
            protected void onResponse(JsonObject response) {
                OneDrivePermissionIterator.this.onResponse(response);
            }

        };
    }

    @Override
    public boolean hasNext() throws OneDriveRuntimeException {
        return jsonObjectIterator.hasNext();
    }

    @Override
    public OneDrivePermission.Metadata next() throws OneDriveRuntimeException {
        JsonObject nextObject = jsonObjectIterator.next();
        String id = nextObject.get("id").asString();

        OneDrivePermission.Metadata nextMetadata;
        OneDrivePermission oneDrivePermission = new OneDrivePermission(api, id);
        nextMetadata = oneDrivePermission.new Metadata(nextObject);

        return nextMetadata;
    }

    /**
     * @since 1.1
     */
    protected void onResponse(JsonObject response) {
        // Hook method
    }

}
