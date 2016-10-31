/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.core.exception.DuplicateAPIException;
import org.wso2.carbon.apimgt.core.util.dto.ErrorDTO;
import org.wso2.carbon.apimgt.core.util.exception.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.Tier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.wso2.carbon.apimgt.core.api.APIConsumer;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.util.dto.ErrorDTO;
import org.wso2.carbon.apimgt.core.util.exception.BadRequestException;
import org.wso2.carbon.apimgt.core.util.exception.ConflictException;
import org.wso2.carbon.apimgt.core.util.exception.ForbiddenException;
import org.wso2.carbon.apimgt.core.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.core.util.exception.NotFoundException;

public class RestApiUtil {

    public static String getLoggedInUsername() {
        return "DUMMY_LOGGEDUSER";
    }

    public static String getLoggedInUserTenantDomain() {
        return "DUMMY_TENANTdOMAIN";//CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }


    /**
     * Returns the current logged in consumer's group id
     * @return group id of the current logged in user.
     */
    @SuppressWarnings("unchecked")
    public static String getLoggedInUserGroupId() {
//        String username = RestApiUtil.getLoggedInUsername();
//        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
//        JSONObject loginInfoJsonObj = new JSONObject();
//        try {
//            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
//            loginInfoJsonObj.put("user", username);
//            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
//                loginInfoJsonObj.put("isSuperTenant", true);
//            } else {
//                loginInfoJsonObj.put("isSuperTenant", false);
//            }
//            String loginInfoString = loginInfoJsonObj.toJSONString();
//            return apiConsumer.getGroupIds(loginInfoString);
//        } catch (APIManagementException e) {
//            String errorMsg = "Unable to get groupIds of user " + username;
//            handleInternalServerError(errorMsg, e, log);
            return "";
//        }
    }

    /**
     * Logs the error, builds a ForbiddenException with specified details and throws it
     *
     * @param resource requested resource
     * @param id id of resource
     * @param log Log instance
     * @throws ForbiddenException
     */
    public static void handleAuthorizationFailure(String resource, String id, Log log) throws ForbiddenException {
        ForbiddenException forbiddenException = buildForbiddenException(resource, id);
        log.error(forbiddenException.getMessage());
        throw forbiddenException;
    }

    /**
     * Returns a new ForbiddenException
     *
     * @param resource Resource type
     * @param id identifier of the resource
     * @return a new ForbiddenException with the specified details as a response DTO
     */
    public static ForbiddenException buildForbiddenException(String resource, String id) {
        String description;
        if (!StringUtils.isEmpty(id)) {
            description = "You don't have permission to access the " + resource + " with Id " + id;
        } else {
            description = "You don't have permission to access the " + resource;
        }
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT, 403l, description);
        return new ForbiddenException(errorDTO);
    }

    /**
     * Logs the error, builds a NotFoundException with specified details and throws it
     *
     * @param resource requested resource
     * @param id id of resource
     * @param log Log instance
     * @throws NotFoundException
     */
    public static void handleResourceNotFoundError(String resource, String id, Log log) throws NotFoundException {
        NotFoundException notFoundException = buildNotFoundException(resource, id);
        log.error(notFoundException.getMessage());
        throw notFoundException;
    }
    /**
     * Returns a new NotFoundException
     *
     * @param resource Resource type
     * @param id identifier of the resource
     * @return a new NotFoundException with the specified details as a response DTO
     */
    public static NotFoundException buildNotFoundException(String resource, String id) {
        String description;
        if (!StringUtils.isEmpty(id)) {
            description = "Requested " + resource + " with Id '" + id + "' not found";
        } else {
            description = "Requested " + resource + " not found";
        }
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_NOT_FOUND_MESSAGE_DEFAULT, 404l, description);
        return new NotFoundException(errorDTO);
    }

    /**
     * Logs the error, builds a internalServerErrorException with specified details and throws it
     *
     * @param msg error message
     * @param t Throwable instance
     * @param log Log instance
     * @throws InternalServerErrorException
     */
    public static void handleInternalServerError(String msg, Throwable t, Log log) throws InternalServerErrorException {
        InternalServerErrorException internalServerErrorException = buildInternalServerErrorException();
        log.error(msg, t);
        throw internalServerErrorException;
    }

    /**
     * Returns a new InternalServerErrorException
     *
     * @return a new InternalServerErrorException with default details as a response DTO
     */
    public static InternalServerErrorException buildInternalServerErrorException() {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT, 500l,
                RestApiConstants.STATUS_INTERNAL_SERVER_ERROR_DESCRIPTION_DEFAULT);
        return new InternalServerErrorException(errorDTO);
    }


    /**
     * Logs the error, builds a BadRequestException with specified details and throws it
     *
     * @param msg error message
     * @param log Log instance
     * @throws BadRequestException
     */
    public static void handleBadRequest(String msg, Log log) throws BadRequestException {
        BadRequestException badRequestException = buildBadRequestException(msg);
        log.error(msg);
        throw badRequestException;
    }

    /**
     * Returns a new BadRequestException
     *
     * @param description description of the exception
     * @return a new BadRequestException with the specified details as a response DTO
     */
    public static BadRequestException buildBadRequestException(String description) {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400l, description);
        return new BadRequestException(errorDTO);
    }

    /**
     * Logs the error, builds a ConflictException with specified details and throws it
     *
     * @param description description of the error
     * @param t Throwable instance
     * @param log Log instance
     * @throws ConflictException
     */
    public static void handleResourceAlreadyExistsError(String description, Throwable t, Log log)
            throws ConflictException {
        ConflictException conflictException = buildConflictException(
                RestApiConstants.STATUS_CONFLICT_MESSAGE_RESOURCE_ALREADY_EXISTS, description);
        log.error(description, t);
        throw conflictException;
    }

    /**
     * Returns a new ConflictException
     *
     * @param message summary of the error
     * @param description description of the exception
     * @return a new ConflictException with the specified details as a response DTO
     */
    public static ConflictException buildConflictException(String message, String description) {
        ErrorDTO errorDTO = getErrorDTO(message, 409l, description);
        return new ConflictException(errorDTO);
    }

    /**
     * Check if the specified throwable e is happened as the updated/new resource conflicting with an already existing
     * resource
     *
     * @param e throwable to check
     * @return true if the specified throwable e is happened as the updated/new resource conflicting with an already
     *   existing resource, false otherwise
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public static boolean isDueToResourceAlreadyExists(Throwable e) {
        Throwable rootCause = getPossibleErrorCause(e);
        return rootCause instanceof APIMgtResourceAlreadyExistsException || rootCause instanceof DuplicateAPIException;
    }

    /**
     * Attempts to find the actual cause of the throwable 'e'
     *
     * @param e throwable
     * @return the root cause of 'e' if the root cause exists, otherwise returns 'e' itself
     */
    private static Throwable getPossibleErrorCause (Throwable e) {
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        rootCause = rootCause == null ? e : rootCause;
        return rootCause;
    }

    /**
     * Search the tier in the given collection of Tiers. Returns it if it is included there. Otherwise return null
     *
     * @param tiers    Tier Collection
     * @param tierName Tier to find
     * @return Matched tier with its name
     */
    public static Tier findTier(Collection<Tier> tiers, String tierName) {
        for (Tier tier : tiers) {
            if (tier.getName() != null && tierName != null && tier.getName().equals(tierName)) {
                return tier;
            }
        }
        return null;
    }

    /**
     * Returns a generic errorDTO
     *
     * @param message specifies the error message
     * @return A generic errorDTO with the specified details
     */
    public static ErrorDTO getErrorDTO(String message, Long code, String description){
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(code);
        errorDTO.setMoreInfo("");
        errorDTO.setMessage(message);
        errorDTO.setDescription(description);
        return errorDTO;
    }

    /**
     * Returns an APIConsumer.
     * 
     * @param subscriberName
     * @return
     * @throws APIManagementException
     */
    public static APIConsumer getConsumer(String subscriberName) throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIConsumer(subscriberName);
    }

    /**
     * Returns the next/previous offset/limit parameters properly when current offset, limit and size parameters are specified
     *
     * @param offset current starting index
     * @param limit current max records
     * @param size maximum index possible
     * @return the next/previous offset/limit parameters as a hash-map
     */
    public static Map<String, Integer> getPaginationParams(Integer offset, Integer limit, Integer size) {
        Map<String, Integer> result = new HashMap<>();
        if (offset >= size || offset < 0)
            return result;

        int start = offset;
        int end = offset + limit - 1;

        int nextStart = end + 1;
        if (nextStart < size) {
            result.put(RestApiConstants.PAGINATION_NEXT_OFFSET, nextStart);
            result.put(RestApiConstants.PAGINATION_NEXT_LIMIT, limit);
        }

        int previousEnd = start - 1;
        int previousStart = previousEnd - limit + 1;

        if (previousEnd >= 0) {
            if (previousStart < 0) {
                result.put(RestApiConstants.PAGINATION_PREVIOUS_OFFSET, 0);
                result.put(RestApiConstants.PAGINATION_PREVIOUS_LIMIT, limit);
            } else {
                result.put(RestApiConstants.PAGINATION_PREVIOUS_OFFSET, previousStart);
                result.put(RestApiConstants.PAGINATION_PREVIOUS_LIMIT, limit);
            }
        }
        return result;
    }

    /** Returns the paginated url for Applications API
     *
     * @param offset starting index
     * @param limit max number of objects returned
     * @param groupId groupId of the Application
     * @return constructed paginated url
     */
    public static String getApplicationPaginatedURL(Integer offset, Integer limit, String groupId) {
        groupId = groupId == null ? "" : groupId;
        String paginatedURL = RestApiConstants.APPLICATIONS_GET_PAGINATION_URL;
        paginatedURL = paginatedURL.replace(RestApiConstants.LIMIT_PARAM, String.valueOf(limit));
        paginatedURL = paginatedURL.replace(RestApiConstants.OFFSET_PARAM, String.valueOf(offset));
        paginatedURL = paginatedURL.replace(RestApiConstants.GROUPID_PARAM, groupId);
        return paginatedURL;
    }
}
