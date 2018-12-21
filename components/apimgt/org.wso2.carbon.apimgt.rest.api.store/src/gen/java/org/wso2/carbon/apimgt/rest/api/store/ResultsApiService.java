package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.dto.ResultListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ResultsApiService {
    public abstract Response resultsGet(Integer limit,Integer offset,String xWSO2Tenant,String query,String accept,String ifNoneMatch);

    public abstract String resultsGetGetLastUpdatedTime(Integer limit,Integer offset,String xWSO2Tenant,String query,String accept,String ifNoneMatch);
}

