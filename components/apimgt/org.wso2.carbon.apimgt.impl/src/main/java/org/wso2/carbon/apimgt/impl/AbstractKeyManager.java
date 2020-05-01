/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.apimgt.impl;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mostly common features of  keyManager implementations will be handle here.
 * This class should be extended by Key manager implementation class.
 */
public abstract class AbstractKeyManager implements KeyManager {
    private static Log log = LogFactory.getLog(AbstractKeyManager.class);
    protected KeyManagerConfiguration configuration;

    public AccessTokenRequest buildAccessTokenRequestFromJSON(String jsonInput, AccessTokenRequest tokenRequest)
            throws APIManagementException {

        if (jsonInput == null || jsonInput.isEmpty()) {
            log.debug("JsonInput is null or Empty.");
            return tokenRequest;
        }

        JSONParser parser = new JSONParser();
        JSONObject jsonObject;

        if (tokenRequest == null) {
            log.debug("Input request is null. Creating a new Request Object.");
            tokenRequest = new AccessTokenRequest();
        }

        try {
            jsonObject = (JSONObject) parser.parse(jsonInput);
            // Getting parameters from input string and setting in TokenRequest.
            if (jsonObject != null && !jsonObject.isEmpty()) {
                Map<String, Object> params = (Map<String, Object>) jsonObject;

                if (null != params.get(ApplicationConstants.OAUTH_CLIENT_ID)) {
                    tokenRequest.setClientId((String) params.get(ApplicationConstants.OAUTH_CLIENT_ID));
                }

                if (null != params.get(ApplicationConstants.OAUTH_CLIENT_SECRET)) {
                    tokenRequest.setClientSecret((String) params.get(ApplicationConstants.OAUTH_CLIENT_SECRET));
                }

                if (null != params.get(ApplicationConstants.VALIDITY_PERIOD)) {
                    tokenRequest.setValidityPeriod(Long.parseLong((String) params.get(ApplicationConstants.VALIDITY_PERIOD)));
                }

                return tokenRequest;
            }
        } catch (ParseException e) {
            handleException("Error occurred while parsing JSON String", e);
        }
        return null;
    }


    /**
     * This method will accept json String and will do the json parse will set oAuth application properties to OAuthApplicationInfo object.
     *
     * @param jsonInput this jsonInput will contain set of oAuth application properties.
     * @return OAuthApplicationInfo object will be return.
     * @throws APIManagementException
     */
    public OAuthApplicationInfo buildFromJSON(OAuthApplicationInfo oAuthApplicationInfo, String jsonInput) throws
            APIManagementException {
        //initiate json parser.
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;

        try {
            //parse json String
            jsonObject = (JSONObject) parser.parse(jsonInput);
            if (jsonObject != null) {
                //create a map to hold json parsed objects.
                Map<String, Object> params = (Map) jsonObject;

                //set client Id
                if (params.get(APIConstants.JSON_CLIENT_ID) != null) {
                    oAuthApplicationInfo.setClientId((String) params.get(APIConstants.JSON_CLIENT_ID));
                }
                //set client secret
                if (params.get(APIConstants.JSON_CLIENT_SECRET) != null) {
                    oAuthApplicationInfo.setClientSecret((String) params.get(APIConstants.JSON_CLIENT_SECRET));
                }
                //copy all params map in to OAuthApplicationInfo's Map object.
                oAuthApplicationInfo.putAll(params);
                return oAuthApplicationInfo;
            }
        } catch (ParseException e) {
            handleException("Error occurred while parsing JSON String", e);
        }
        return null;
    }

    public AccessTokenRequest buildAccessTokenRequestFromOAuthApp(OAuthApplicationInfo oAuthApplication,
                                                                  AccessTokenRequest tokenRequest) throws
            APIManagementException {
        if (oAuthApplication == null) {
            return tokenRequest;
        }
        if (tokenRequest == null) {
            tokenRequest = new AccessTokenRequest();
        }

        if (oAuthApplication.getClientId() == null || oAuthApplication.getClientSecret() == null) {
            throw new APIManagementException("Consumer key or Consumer Secret missing.");
        }
        tokenRequest.setClientId(oAuthApplication.getClientId());
        tokenRequest.setClientSecret(oAuthApplication.getClientSecret());


        if (oAuthApplication.getParameter("tokenScope") != null) {
            String[] tokenScopes = (String[]) oAuthApplication.getParameter("tokenScope");
            tokenRequest.setScope(tokenScopes);
            oAuthApplication.addParameter("tokenScope", Arrays.toString(tokenScopes));
        }

        if (oAuthApplication.getParameter(ApplicationConstants.VALIDITY_PERIOD) != null) {
            tokenRequest.setValidityPeriod(Long.parseLong((String) oAuthApplication.getParameter(ApplicationConstants
                    .VALIDITY_PERIOD)));
        }

        return tokenRequest;
    }

    @Override
    public boolean canHandleToken(String accessToken) throws APIManagementException {

        Object enabledTokenValidation = configuration.getParameter(APIConstants.KeyManager.ENABLE_TOKEN_VALIDATION);
        if (enabledTokenValidation != null && (Boolean) enabledTokenValidation) {
            Object tokenHandlingScript = configuration.getParameter(APIConstants.KeyManager.VALIDATION_VALUE);
            Object tokenHandlingType = configuration.getParameter(APIConstants.KeyManager.VALIDATION_TYPE);
            if (APIConstants.KeyManager.VALIDATION_REGEX.equals(tokenHandlingType)) {
                if (tokenHandlingScript != null && StringUtils.isNotEmpty((CharSequence) tokenHandlingScript)) {
                    Pattern pattern = Pattern.compile((String) tokenHandlingScript);
                    Matcher matcher = pattern.matcher(accessToken);
                    return matcher.find();
                }
            } else if (APIConstants.KeyManager.VALIDATION_JWT.equals(tokenHandlingType) &&
                    accessToken.contains(APIConstants.DOT)) {
                Map<String, Map<String, String>> validationJson = (Map) tokenHandlingScript;
                try {
                    SignedJWT signedJWT = SignedJWT.parse(accessToken);
                    JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
                    for (Map.Entry<String, Map<String, String>> entry : validationJson.entrySet()) {
                        if (APIConstants.KeyManager.VALIDATION_ENTRY_JWT_BODY.equals(entry.getKey())) {
                            for (Map.Entry<String, String> e : entry.getValue().entrySet()) {
                                String key = e.getKey();
                                String value = e.getValue();
                                Object claimValue = jwtClaimsSet.getClaim(key);
                                if (claimValue == null) {
                                    return false;
                                }
                                Pattern pattern = Pattern.compile(value);
                                Matcher matcher = pattern.matcher((String) claimValue);
                                if (matcher.find()) {
                                    return true;
                                }
                            }
                        }
                    }
                } catch (java.text.ParseException e) {
                    throw new APIManagementException("Error while parsing jwt", e);
                }
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * common method to throw exceptions.
     *
     * @param msg this parameter contain error message that we need to throw.
     * @param e   Exception object.
     * @throws APIManagementException
     */
    protected void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }
}
