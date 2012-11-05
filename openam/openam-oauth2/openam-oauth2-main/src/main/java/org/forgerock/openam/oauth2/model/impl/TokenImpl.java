/*
 * DO NOT REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 ForgeRock Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [2012] [ForgeRock Inc]"
 */

package org.forgerock.openam.oauth2.model.impl;

import java.util.*;

import org.forgerock.json.fluent.JsonValue;
import org.forgerock.openam.oauth2.OAuth2Constants;
import org.forgerock.openam.oauth2.exceptions.OAuthProblemException;
import org.forgerock.openam.oauth2.model.SessionClient;
import org.forgerock.openam.oauth2.model.Token;
import org.restlet.Request;

/**
 * Implements a Token
 */
public abstract class TokenImpl extends JsonValue implements Token {

    private String id;

    /**
     * Constructor that sets common values in the token object.
     * 
     * @param id
     *            the ID of the token, kept out of the JsonValue
     * @param userID
     *            the userID
     * @param client
     *            the client object (id and redirect URI)
     * @param realm
     *            the realm that governs this token
     * @param scope
     *            the set of scopes
     * @param expiresIn
     *            the number of seconds from message generation time that this
     *            token is valid
     */
    protected TokenImpl(String id, String userID, SessionClient client, String realm,
            Set<String> scope, long expiresIn) {
        super(new HashMap<String, Object>());

        this.id = id;

        setUserID(userID);
        setClient(client);
        setRealm(realm);
        setScope(scope);
        setAbsoluteExpiryTime(calculateAbsoluteExpiry(expiresIn));
    }

    /**
     * Converts a countdown-style lifetime in seconds to a more absolute expiry
     * time suitable for storage.
     * 
     * @param expiresIn
     *            lifetime of token in seconds from time of generation
     * @return expiry time in milliseconds relative to the last epoch
     */
    private long calculateAbsoluteExpiry(long expiresIn) {
        return System.currentTimeMillis() + expiresIn * 1000; // Seconds to
                                                              // milliseconds
    }

    /**
     * Constructs a TokenImpl object using the values in a JsonValue object and
     * an associated ID.
     * 
     * @param id
     *            the ID of the token as used when storing/modifying/retrieving
     *            the object
     * @param value
     *            the JSON object containing the values for this object
     */
    protected TokenImpl(String id, JsonValue value) {
        //super(new HashMap<String, Object>());
        super(value);
        this.id = id;
    }

    private Set<String> convertScope(List<Object> scopeList) {
        Set<String> scopeSet = new HashSet<String>();
        for (Object o : scopeList) {
            scopeSet.add(o.toString());
        }
        return scopeSet;
    }

    /**
     * Sets the UserID of the token
     * 
     * @param userID
     *            The UserID of the token
     */
    public void setUserID(String userID) {
        Set<String> s = new HashSet<String>();
        s.add(userID);
        this.put(OAuth2Constants.Params.USERNAME, s);
    }

    /**
     * Sets the Realm of the token
     * 
     * @param realm
     *            The realm of the token
     */
    public void setRealm(String realm) {
        Set<String> s = new HashSet<String>();
        s.add(realm == null ? "/" : new String(realm));
        this.put(OAuth2Constants.Params.REALM, s);
    }

    /**
     * Sets the Client of the token
     * 
     * @param client
     *            The client of the token
     */
    public void setClient(SessionClient client) {
        if (client != null) {
            Set<String> s = new HashSet<String>();
            s.add(client.getClientId());
            this.put(OAuth2Constants.Params.CLIENT_ID, s);
            s = new HashSet<String>();
            s.add(client.getRedirectUri());
            this.put(OAuth2Constants.Params.REDIRECT_URI, s);
        }
    }

    /**
     * Sets the scope of the token
     * 
     * @param scope
     *            The scope of the token
     */
    public void setScope(Set<String> scope) {
        if (scope == null) {
            scope = Collections.emptySet();
        }
        this.put(OAuth2Constants.Params.SCOPE, scope);
    }

    /**
     * Sets the ExpiryTime of the token
     * 
     * @param expiryTime
     *            The epoch in milliseconds when this token will expire
     */
    public void setAbsoluteExpiryTime(long expiryTime) {
        Set<String> s = new HashSet<String>();
        s.add(String.valueOf(expiryTime));
        this.put(OAuth2Constants.StoredToken.EXPIRY_TIME, s);
    }

    @Override
    public String getToken() {
        return id;
    }

    @Override
    public String getUserID() {
        String username = null;
        Set username_set = (Set) get(OAuth2Constants.Params.USERNAME).getObject();
        if (username_set != null){
            username = username_set.iterator().next().toString();
        }
        return username;
    }

    @Override
    public String getRealm() {
        String realm = null;
        Set realm_set = (Set) get(OAuth2Constants.Params.REALM).getObject();
        if (realm_set != null){
            realm = realm_set.iterator().next().toString();
        }
        return realm;
    }

    @Override
    public SessionClient getClient() {
        Set client_id_set = (Set) get(OAuth2Constants.Params.CLIENT_ID).getObject();
        Set redirect_uri_set = (Set) get(OAuth2Constants.Params.REDIRECT_URI).getObject();
        String client_id = null;
        String redirect_url = null;
        if (client_id_set != null){
            client_id = (client_id_set).iterator().next().toString();
        }
        if (redirect_uri_set != null){
            Object redirect = redirect_uri_set.iterator().next();
            if (redirect != null) {
                redirect_url = redirect.toString();
            } else {
                redirect_url = null;
            }
        }
        return new SessionClientImpl(client_id, redirect_url);
    }

    @Override
    public Set<String> getScope() {
        // return convertScope(this.get(OAuth2.Params.SCOPE).asList());
        return (Set<String>) this.get(OAuth2Constants.Params.SCOPE).getObject();
    }

    @Override
    public long getExpireTime() {
        return (getAbsoluteExpiryTime() - System.currentTimeMillis());
    }

    /**
     * Returns the expiry time as stored.
     * 
     * @return time of expiry expressed as milliseconds since the epoch.
     */
    public long getAbsoluteExpiryTime() {
        Set expiry_set = (Set) get(OAuth2Constants.StoredToken.EXPIRY_TIME).getObject();
        if (expiry_set != null){
            return Long.parseLong(expiry_set.iterator().next().toString());
        }
        throw OAuthProblemException.OAuthError.INVALID_TOKEN.handle(Request.getCurrent(),
                "Token has no expire time. Invalid Token");
    }

    @Override
    public boolean isExpired() {
        return (System.currentTimeMillis() > getAbsoluteExpiryTime());
    }

    /**
     * Presents the "type" parameter of the token.
     * 
     * @return the OAuth2 token type.
     */
    public String getType() {
        String token_type = null;
        Set token_type_set = (Set) get(OAuth2Constants.Params.TOKEN_TYPE).getObject();
        if (token_type_set != null){
            token_type = token_type_set.iterator().next().toString();
        }
        return token_type;
    }

}
