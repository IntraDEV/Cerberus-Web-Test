/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intradev.cerberus.allshared;

import java.util.Date;

/**
 * Data model interface definitions for Cerberus; these should be implemented in
 * relevant server and client code.
 */
public interface Model {
    public static interface Timestamped {
        public Date getCreatedDate();
        public Date getModifiedDate();
    }

    public static interface Syncable extends Timestamped {
        public boolean isPendingDelete();
    }

    public static interface Password extends Syncable {
        public String getId();
        public String getOwnerId();
        public String getTitle();
        public void setTitle(String title);
        public String getBody();
        public void setBody(String body);
    }

    public static interface UserInfo {
        public String getId();
        public String getEmail();
        public String getNick();
    }

    public static interface DeviceRegistration {
        public String getDeviceId();
        public String getOwnerId();
        public String getDeviceType();
        public String getRegistrationToken();
        public void setRegistrationToken(String registrationToken);
    }
}
