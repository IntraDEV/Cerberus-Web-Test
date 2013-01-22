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

package com.intradev.cerberus.web.server;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.c2dm.server.C2DMessaging;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;
import com.intradev.cerberus.allshared.AllConfig;
import com.intradev.cerberus.allshared.JsonRpcException;
import com.intradev.cerberus.allshared.JsonRpcMethod;
import com.intradev.cerberus.allshared.CerberusProtocol;
import com.intradev.cerberus.javashared.Util;
import com.intradev.cerberus.web.jsonrpc.server.JsonRpcServlet;
import com.intradev.cerberus.web.server.ModelImpl.DeviceRegistration;
import com.intradev.cerberus.web.server.ModelImpl.Password;
import com.intradev.cerberus.web.server.ModelImpl.UserInfo;

/**
 * The server side implementation of the Cerberus JSON-RPC service.
 */
@SuppressWarnings("serial")
public class CerberusServlet extends JsonRpcServlet {

    public static final int DEBUG = -1; // 1 == yes, 0 == no, -1 == decide based on server info

    private static final Logger log = Logger.getLogger(CerberusServlet.class.getName());

    private static final String PROTOCOL_VERSION = "1";
    public static final String DEVICE_TYPE_ANDROID = "android";

    @Override
    @SuppressWarnings("all")
    protected boolean isDebug(HttpServletRequest req) {
        if (DEBUG == 1)
            return true;
        else if (DEBUG == 0)
            return false;
        else
            return this.getServletContext().getServerInfo().contains("Development");
    }

    @JsonRpcMethod(method = CerberusProtocol.UserInfo.METHOD)
    public JSONObject userInfo(final CallContext context) throws JSONException, JsonRpcException {
        String continueUrl = context.getParams().optString(
                CerberusProtocol.UserInfo.ARG_LOGIN_CONTINUE, "/");

        JSONObject data = new JSONObject();
        if (context.getUserService().isUserLoggedIn()) {
            UserInfo userInfo = new UserInfo(context.getUserService().getCurrentUser());
            data.put(CerberusProtocol.UserInfo.RET_USER, userInfo.toJSON());
            data.put(CerberusProtocol.UserInfo.RET_LOGOUT_URL, context.getUserService().createLogoutURL(continueUrl));
        } else {
            data.put(CerberusProtocol.UserInfo.RET_LOGIN_URL, context.getUserService().createLoginURL(continueUrl));
        }

        return data;
    }

    @JsonRpcMethod(method = CerberusProtocol.ServerInfo.METHOD)
    public JSONObject serverInfo(final CallContext context) throws JSONException, JsonRpcException {
        JSONObject responseJson = new JSONObject();
        responseJson.put(CerberusProtocol.ServerInfo.RET_PROTOCOL_VERSION, PROTOCOL_VERSION);
        return responseJson;
    }

    @JsonRpcMethod(method = CerberusProtocol.PasswordsList.METHOD, requires_login = true)
    public JSONObject passwordList(final CallContext context) throws JSONException, JsonRpcException {
        UserInfo userInfo = getCurrentUserInfo(context);

        // Password: this would be inefficient for large password collections
        Query query = context.getPersistenceManager().newQuery(Password.class);
        query.setFilter("ownerKey == ownerKeyParam && pendingDelete == false");
        query.declareParameters(Key.class.getName() + " ownerKeyParam");
        @SuppressWarnings("unchecked")
        List<Password> passwords = (List<Password>) query.execute(userInfo.getKey());

        JSONObject responseJson = new JSONObject();
        try {
            JSONArray passwordsJson = new JSONArray();
            for (Password password : passwords) {
                passwordsJson.put(password.toJSON());
            }

            responseJson.put(CerberusProtocol.PasswordsList.RET_PASSWORDS, passwordsJson);
        } catch (JSONException e) {
            throw new JsonRpcException(500, "Error serializing response.", e);
        }

        return responseJson;
    }

    @JsonRpcMethod(method = CerberusProtocol.PasswordsGet.METHOD, requires_login = true)
    public JSONObject passwordsGet(final CallContext context) throws JSONException, JsonRpcException {
        UserInfo userInfo = getCurrentUserInfo(context);

        String passwordId = context.getParams().getString(CerberusProtocol.PasswordsGet.ARG_ID);
        Key passwordKey = Password.makeKey(userInfo.getId(), passwordId);
        try {
            Password password = context.getPersistenceManager().getObjectById(Password.class, passwordKey);
            if (password.isPendingDelete()) {
                throw new JDOObjectNotFoundException();
            }
            if (!password.getOwnerId().equals(userInfo.getId())) {
                throw new JsonRpcException(403, "You do not have permission to access this password.");
            }
            return (JSONObject) password.toJSON();
        } catch (JDOObjectNotFoundException e) {
            throw new JsonRpcException(404, "Password with ID " + passwordId + " does not exist.");
        }
    }
    
    @JsonRpcMethod(method = CerberusProtocol.PasswordsCount.METHOD, requires_login = true)
    public JSONObject passwordsCount(final CallContext context) throws JSONException, JsonRpcException {
        UserInfo userInfo = getCurrentUserInfo(context);

        // Password: this would be inefficient for large password collections
        Query query = context.getPersistenceManager().newQuery(Password.class);
        query.setFilter("ownerKey == ownerKeyParam && pendingDelete == false");
        query.declareParameters(Key.class.getName() + " ownerKeyParam");
        @SuppressWarnings("unchecked")
        List<Password> passwords = (List<Password>) query.execute(userInfo.getKey());

        JSONObject responseJson = new JSONObject();
        try {
            responseJson.put(CerberusProtocol.PasswordsCount.RET_COUNT, Integer.toString(passwords.size()));
        } catch (JSONException e) {
            throw new JsonRpcException(500, "Error serializing response.", e);
        }

        return responseJson;
    }

    @JsonRpcMethod(method = CerberusProtocol.PasswordsCreate.METHOD, requires_login = true)
    public JSONObject passwordsCreate(final CallContext context) throws JSONException, JsonRpcException {
        UserInfo userInfo = getCurrentUserInfo(context);

        String clientDeviceId = null;
        JSONObject passwordJson;
        Password password;
        try {
            clientDeviceId = context.getParams().optString(CerberusProtocol.ARG_CLIENT_DEVICE_ID);
            passwordJson = context.getParams().getJSONObject(CerberusProtocol.PasswordsCreate.ARG_PASSWORD);
            passwordJson.put("owner_id", userInfo.getId());
            password = new Password(passwordJson);
        } catch (JSONException e) {
            throw new JsonRpcException(400, "Invalid password parameter.", e);
        }

        context.getPersistenceManager().makePersistent(password);
        passwordJson = (JSONObject) password.toJSON(); // get new parameters like ID, creation date, etc.

        enqueueDeviceMessage(context.getPersistenceManager(), userInfo, clientDeviceId);
        
        JSONObject responseJson = new JSONObject();
        responseJson.put(CerberusProtocol.PasswordsCreate.RET_PASSWORD, passwordJson);
        return responseJson;
    }
    

    public void enqueueDeviceMessage(PersistenceManager pm,
            UserInfo userInfo, String clientDeviceId) {

        Query query = pm.newQuery(DeviceRegistration.class);
        query.setFilter("ownerKey == ownerKeyParam");
        query.declareParameters(Key.class.getName() + " ownerKeyParam");
        @SuppressWarnings("unchecked")
        List<DeviceRegistration> registrations = (List<DeviceRegistration>)
                query.execute(userInfo.getKey());

        int numDeviceMessages = 0;
        for (DeviceRegistration registration : registrations) {
            if (registration.getDeviceId().equals(clientDeviceId) ||
                registration.getRegistrationToken() == null)
                continue;
            if (DEVICE_TYPE_ANDROID.equals(registration.getDeviceType())) {
                ++numDeviceMessages;
                String email = userInfo.getEmail();
                
                String collapseKey = Long.toHexString(email.hashCode());
                
                try {
                    C2DMessaging.get(getServletContext()).sendWithRetry(
                        registration.getRegistrationToken(),
                        collapseKey,
                        AllConfig.C2DM_MESSAGE_EXTRA,
                        AllConfig.C2DM_MESSAGE_SYNC,
                        AllConfig.C2DM_ACCOUNT_EXTRA,
                        email);
                } catch (IOException ex) {
                    log.severe("Can't send C2DM message, next manual sync " +
                    		"will get the changes.");
                }
            }
        }

        log.info("Scheduled " + numDeviceMessages + " C2DM device messages for user " +
                userInfo.getEmail() + ".");
    }

    @JsonRpcMethod(method = CerberusProtocol.PasswordssEdit.METHOD, requires_login = true)
    public JSONObject passwordsEdit(final CallContext context) throws JSONException, JsonRpcException {
        UserInfo userInfo = getCurrentUserInfo(context);

        String clientDeviceId;
        JSONObject passwordJson;
        Password password;
        String passwordId = "n/a";
        Transaction tx = context.getPersistenceManager().currentTransaction();
        try {
            clientDeviceId = context.getParams().optString(CerberusProtocol.ARG_CLIENT_DEVICE_ID);
            passwordJson = context.getParams().getJSONObject(CerberusProtocol.PasswordssEdit.ARG_PASSWORD);
            passwordId = passwordJson.getString("id");
            Key passwordKey = Password.makeKey(userInfo.getId(), passwordId);

            tx.begin();
            password = context.getPersistenceManager().getObjectById(Password.class, passwordKey);
            if (!password.getOwnerId().equals(userInfo.getId())) {
                throw new JsonRpcException(403, "You do not have permission to modify this password.");
            }
            passwordJson.put("owner_id", userInfo.getId());
            password.fromJSON(passwordJson);
            tx.commit();
        } catch (JSONException e) {
            throw new JsonRpcException(400, "Invalid password parameter.", e);
        } catch (JDOObjectNotFoundException e) {
            throw new JsonRpcException(404, "Password with ID " + passwordId + " does not exist.");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        enqueueDeviceMessage(context.getPersistenceManager(), userInfo, clientDeviceId);

        passwordJson = (JSONObject) password.toJSON(); // get more parameters like ID, creation date, etc.
        JSONObject responseJson = new JSONObject();
        responseJson.put(CerberusProtocol.PasswordssEdit.RET_PASSWORD, passwordJson);
        return responseJson;
    }

    @JsonRpcMethod(method = CerberusProtocol.PasswordsDelete.METHOD, requires_login = true)
    public JSONObject passwordsDelete(final CallContext context) throws JSONException, JsonRpcException {
        UserInfo userInfo = getCurrentUserInfo(context);

        String clientDeviceId = null;
        Password password;
        String passwordId;
        try {
            clientDeviceId = context.getParams().optString(CerberusProtocol.ARG_CLIENT_DEVICE_ID);
            passwordId = context.getParams().getString(CerberusProtocol.PasswordsDelete.ARG_ID);
        } catch (JSONException e) {
            throw new JsonRpcException(400, "Invalid password ID.", e);
        }

        Transaction tx = context.getPersistenceManager().currentTransaction();
        try {
            tx.begin();
            password = context.getPersistenceManager().getObjectById(Password.class,
                    Password.makeKey(userInfo.getId(), passwordId));
            if (password.isPendingDelete()) {
                throw new JDOObjectNotFoundException();
            }
            if (!password.getOwnerId().equals(userInfo.getId())) {
                throw new JsonRpcException(403, "You do not have permission to modify this password.");
            }

            password.markForDeletion();
            tx.commit();
        } catch (JDOObjectNotFoundException e) {
            throw new JsonRpcException(404, "Password with ID " + passwordId + " does not exist.");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        enqueueDeviceMessage(context.getPersistenceManager(), userInfo, clientDeviceId);
        return null;
    }

    @JsonRpcMethod(method = CerberusProtocol.PasswordsSync.METHOD, requires_login = true)
    public JSONObject passwordsSync(final CallContext context) throws JSONException, JsonRpcException {
        // This method should return a list of updated passwords since a current
        // date, optionally reconciling/merging a set of a local passwords.
        String clientDeviceId = null;
        UserInfo userInfo = getCurrentUserInfo(context);
        Date sinceDate;

        try {
            clientDeviceId = context.getParams().optString(CerberusProtocol.ARG_CLIENT_DEVICE_ID);
            sinceDate = Util.parseDateISO8601(context.getParams().getString(CerberusProtocol.PasswordsSync.ARG_SINCE_DATE));
        } catch (ParseException e) {
            throw new JsonRpcException(400, "Invalid since_date.", e);
        } catch (JSONException e) {
            throw new JsonRpcException(400, "Invalid since_date.", e);
        }

        JSONObject responseJson = new JSONObject();
        JSONArray passwordsJson = new JSONArray();
        Transaction tx = context.getPersistenceManager().currentTransaction();
        Date newSinceDate = new Date();
        try {
            tx.begin();
            List<Password> localPasswords = new ArrayList<Password>();
            if (context.getParams().has(CerberusProtocol.PasswordsSync.ARG_LOCAL_PASSWORDS)) {
                JSONArray localChangesJson = context.getParams().getJSONArray(CerberusProtocol.PasswordsSync.ARG_LOCAL_PASSWORDS);
                for (int i = 0; i < localChangesJson.length(); i++) {
                    try {
                        JSONObject passwordJson = localChangesJson.getJSONObject(i);

                        if (passwordJson.has("id")) {
                            Key existingPasswordKey = Password.makeKey(userInfo.getId(),
                            		passwordJson.get("id").toString());
                            try {
                                Password existingPassword = (Password) context.getPersistenceManager().getObjectById(
                                        Password.class, existingPasswordKey);
                                if (!existingPassword.getOwnerId().equals(userInfo.getId())) {
                                    // User doesn't have permission to edit this password. Instead of
                                    // throwing an error, just re-create it on the server side.
                                    //throw new JsonRpcException(403,
                                    //        "You do not have permission to modify this password.");
                                	passwordJson.remove("id");
                                }
                            } catch (JDOObjectNotFoundException e) {
                                // Password doesn't exist, instead of throwing an error,
                                // just re-create the password on the server side (unassign its ID).
                                //throw new JsonRpcException(404, "Password with ID "
                                //        + passwordJson.get("id").toString() + " does not exist.");
                            	passwordJson.remove("id");
                            }
                        }

                        passwordJson.put("owner_id", userInfo.getId());
                        Password localPassword = new Password(passwordJson);
                        localPasswords.add(localPassword);
                    } catch (JSONException e) {
                        throw new JsonRpcException(400, "Invalid local password content.", e);
                    }
                }
            }

            // Query server-side password changes.
            Query query = context.getPersistenceManager().newQuery(Password.class);
            query.setFilter("ownerKey == ownerKeyParam && modifiedDate > sinceDate");
            query.setOrdering("modifiedDate desc");
            query.declareParameters(Key.class.getName() + " ownerKeyParam, java.util.Date sinceDate");
            @SuppressWarnings("unchecked")
            List<Password> passwords = (List<Password>) query.execute(userInfo.getKey(), sinceDate);

            // Now merge the lists and conflicting objects.
            Reconciler<Password> reconciler = new Reconciler<Password>() {
                @Override
                public Password reconcile(Password o1, Password o2) {
                    boolean pick1 = o1.getModifiedDate().after(o2.getModifiedDate());

                    // Make sure only the chosen version of the password is persisted
                    context.getPersistenceManager().makeTransient(pick1 ? o2 : o1);

                    return pick1 ? o1 : o2;
                }
            };

            Collection<Password> reconciledPasswords = reconciler.reconcileLists(passwords, localPasswords);

            for (Password password : reconciledPasswords) {
                // Save the password.
                context.getPersistenceManager().makePersistent(password);

                // Put it in the response output.
                passwordsJson.put(password.toJSON());
            }
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        enqueueDeviceMessage(context.getPersistenceManager(), userInfo, clientDeviceId);

        responseJson.put(CerberusProtocol.PasswordsSync.RET_PASSWORDS, passwordsJson);
        responseJson.put(CerberusProtocol.PasswordsSync.RET_NEW_SINCE_DATE,
                Util.formatDateISO8601(newSinceDate));
        return responseJson;
    }

    @JsonRpcMethod(method = CerberusProtocol.DevicesRegister.METHOD, requires_login = true)
    public JSONObject devicesRegister(final CallContext context) throws JSONException, JsonRpcException {
        UserInfo userInfo = getCurrentUserInfo(context);

        JSONObject registrationJson;
        DeviceRegistration registrationParam;
        try {
            registrationJson = context.getParams().getJSONObject(CerberusProtocol.DevicesRegister.ARG_DEVICE);
            registrationJson.put("owner_id", userInfo.getId());
            registrationParam = new DeviceRegistration(registrationJson);
        } catch (JSONException e) {
            throw new JsonRpcException(400, "Invalid device parameter.", e);
        }

        Transaction tx = context.getPersistenceManager().currentTransaction();
        try {
            tx.begin();
            Query query = context.getPersistenceManager().newQuery(DeviceRegistration.class);
            query.setFilter("ownerKey == ownerKeyParam && deviceId == deviceIdParam");
            query.declareParameters(Key.class.getName() + " ownerKeyParam, String deviceIdParam");
            @SuppressWarnings("unchecked")
            List<DeviceRegistration> registrations = (List<DeviceRegistration>)
                    query.execute(userInfo.getKey(), registrationParam.getDeviceId());

            // Update all existing registration tokens.
            boolean registeredForUser = false;
            for (DeviceRegistration registration : registrations) {
                if (registration.getOwnerId().equals(userInfo.getId()))
                    registeredForUser = true;
                registration.setRegistrationToken(registrationParam.getRegistrationToken());
            }

            // Register the device for the logged in user if not already registered.
            if (!registeredForUser) {
                context.getPersistenceManager().makePersistent(registrationParam);
            }
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        registrationJson = (JSONObject) registrationParam.toJSON();

        JSONObject responseJson = new JSONObject();
        responseJson.put(CerberusProtocol.DevicesRegister.RET_DEVICE, registrationJson);
        return responseJson;
    }

    @JsonRpcMethod(method = CerberusProtocol.DevicesUnregister.METHOD, requires_login = true)
    public JSONObject devicesUnregister(final CallContext context) throws JSONException, JsonRpcException {
        UserInfo userInfo = getCurrentUserInfo(context);

        String deviceId;
        try {
            deviceId = context.getParams().getString(CerberusProtocol.DevicesUnregister.ARG_DEVICE_ID);
        } catch (JSONException e) {
            throw new JsonRpcException(400, "Invalid device ID parameter.", e);
        }

        Transaction tx = context.getPersistenceManager().currentTransaction();
        try {
            tx.begin();
            Query query = context.getPersistenceManager().newQuery(DeviceRegistration.class);
            query.setFilter("ownerKey == ownerKeyParam && deviceId == deviceIdParam");
            query.declareParameters(Key.class.getName() + " ownerKeyParam, String deviceIdParam");
            @SuppressWarnings("unchecked")
            List<DeviceRegistration> registrations = (List<DeviceRegistration>)
                    query.execute(userInfo.getKey(), deviceId);

            if (registrations.size() == 0) {
                throw new JsonRpcException(404, "Device with provided ID is not registered.");
            }

            for (DeviceRegistration registration : registrations) {
                context.getPersistenceManager().deletePersistent(registration);
            }
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        return null;
    }

    @JsonRpcMethod(method = CerberusProtocol.DevicesClear.METHOD, requires_login = true)
    public JSONObject devicesClear(final CallContext context) throws JSONException, JsonRpcException {
        UserInfo userInfo = getCurrentUserInfo(context);

        Transaction tx = context.getPersistenceManager().currentTransaction();
        try {
            tx.begin();
            Query query = context.getPersistenceManager().newQuery(DeviceRegistration.class);
            query.setFilter("ownerKey == ownerKeyParam");
            query.declareParameters(Key.class.getName() + " ownerKeyParam");
            @SuppressWarnings("unchecked")
            List<DeviceRegistration> registrations = (List<DeviceRegistration>)
                    query.execute(userInfo.getKey());

            for (DeviceRegistration registration : registrations) {
                context.getPersistenceManager().deletePersistent(registration);
            }
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        return null;
    }

    public UserInfo getCurrentUserInfo(final CallContext context) {
        if (!context.getUserService().isUserLoggedIn())
            return null;

        User user = context.getUserService().getCurrentUser();

        try {
            UserInfo userInfo = context.getPersistenceManager().getObjectById(UserInfo.class,
                    user.getUserId());
            return userInfo;
        } catch (JDOObjectNotFoundException e) {
            UserInfo userInfo = new UserInfo(user);
            context.getPersistenceManager().makePersistent(userInfo);
            return userInfo;
        }
    }
}
