/*******************************************************************************
 * Copyright 2013 SAP AG
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
 ******************************************************************************/
package com.sap.core.odata.core.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

import com.sap.core.odata.api.ODataService;
import com.sap.core.odata.api.ODataServiceFactory;
import com.sap.core.odata.api.commons.ODataHttpMethod;
import com.sap.core.odata.api.exception.MessageReference;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.exception.ODataNotImplementedException;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.core.ODataContextImpl;
import com.sap.core.odata.core.ODataExceptionWrapper;
import com.sap.core.odata.core.ODataRequestHandler;
import com.sap.core.odata.core.ODataRequestImpl;

/**
 * @author SAP AG
 */
public final class ODataSubLocator {

  private ODataServiceFactory serviceFactory;
  private ODataRequestImpl request;

  @GET
  public Response handleGet() throws ODataException {
    return handle(ODataHttpMethod.GET);
  }

  @PUT
  public Response handlePut() throws ODataException {
    return handle(ODataHttpMethod.PUT);
  }

  @PATCH
  public Response handlePatch() throws ODataException {
    return handle(ODataHttpMethod.PATCH);
  }

  @MERGE
  public Response handleMerge() throws ODataException {
    return handle(ODataHttpMethod.MERGE);
  }

  @DELETE
  public Response handleDelete() throws ODataException {
    return handle(ODataHttpMethod.DELETE);
  }

  @POST
  public Response handlePost(@HeaderParam("X-HTTP-Method") final String xHttpMethod) throws ODataException {
    Response response;

    if (xHttpMethod == null) {
      response = handle(ODataHttpMethod.POST);
    } else {
      /* tunneling */
      if ("MERGE".equals(xHttpMethod)) {
        response = handle(ODataHttpMethod.MERGE);
      } else if ("PATCH".equals(xHttpMethod)) {
        response = handle(ODataHttpMethod.PATCH);
      } else if (HttpMethod.DELETE.equals(xHttpMethod)) {
        response = handle(ODataHttpMethod.DELETE);
      } else if (HttpMethod.PUT.equals(xHttpMethod)) {
        response = handle(ODataHttpMethod.PUT);
      } else if (HttpMethod.GET.equals(xHttpMethod)) {
        response = handle(ODataHttpMethod.GET);
      } else if (HttpMethod.POST.equals(xHttpMethod)) {
        response = handle(ODataHttpMethod.POST);
      } else if (HttpMethod.HEAD.equals(xHttpMethod)) {
        response = handleHead();
      } else if (HttpMethod.OPTIONS.equals(xHttpMethod)) {
        response = handleOptions();
      } else {
        response = returnNotImplementedResponse(ODataNotImplementedException.TUNNELING);
      }
    }
    return response;
  }

  private Response returnNotImplementedResponse(final MessageReference messageReference) {
    // RFC 2616, 5.1.1: "An origin server SHOULD return the status code [...]
    // 501 (Not Implemented) if the method is unrecognized [...] by the origin server."
    ODataContextImpl context = new ODataContextImpl(request, serviceFactory);
    context.setRequest(request);
    context.setAcceptableLanguages(request.getAcceptableLanguages());
    context.setPathInfo(request.getPathInfo());
    context.setServiceFactory(serviceFactory);
    ODataExceptionWrapper exceptionWrapper = new ODataExceptionWrapper(context, request.getQueryParameters(), request.getAcceptHeaders());
    ODataResponse response = exceptionWrapper.wrapInExceptionResponse(new ODataNotImplementedException(messageReference));
    return RestUtil.convertResponse(response);
  }

  @OPTIONS
  public Response handleOptions() throws ODataException {
    // RFC 2616, 5.1.1: "An origin server SHOULD return the status code [...]
    // 501 (Not Implemented) if the method is unrecognized or not implemented
    // by the origin server."
    return returnNotImplementedResponse(ODataNotImplementedException.COMMON);
  }

  @HEAD
  public Response handleHead() throws ODataException {
    // RFC 2616, 5.1.1: "An origin server SHOULD return the status code [...]
    // 501 (Not Implemented) if the method is unrecognized or not implemented
    // by the origin server."
    return returnNotImplementedResponse(ODataNotImplementedException.COMMON);
  }

  private Response handle(final ODataHttpMethod method) throws ODataException {
    request.setMethod(method);

    ODataContextImpl context = new ODataContextImpl(request, serviceFactory);
    ODataService service = serviceFactory.createService(context);
    context.setService(service);
    service.getProcessor().setContext(context);

    ODataRequestHandler requestHandler = new ODataRequestHandler(serviceFactory, service, context);

    final ODataResponse odataResponse = requestHandler.handle(request);
    final Response response = RestUtil.convertResponse(odataResponse);

    return response;
  }

  public static ODataSubLocator create(final SubLocatorParameter param) throws ODataException {
    ODataSubLocator subLocator = new ODataSubLocator();

    subLocator.serviceFactory = param.getServiceFactory();

    subLocator.request = new ODataRequestImpl();
    subLocator.request.setRequestHeaders(param.getHttpHeaders().getRequestHeaders());
    subLocator.request.setPathInfo(RestUtil.buildODataPathInfo(param));
    subLocator.request.setBody(RestUtil.contentAsStream(RestUtil.extractRequestContent(param)));
    subLocator.request.setQueryParameters(RestUtil.convertToSinglevaluedMap(param.getUriInfo().getQueryParameters()));
    subLocator.request.setAcceptHeaders(RestUtil.extractAcceptHeaders(param));
    subLocator.request.setContentType(RestUtil.extractRequestContentType(param));
    subLocator.request.setAcceptableLanguages(param.getHttpHeaders().getAcceptableLanguages());

    return subLocator;
  }

  private ODataSubLocator() {
    super();
  }
}
