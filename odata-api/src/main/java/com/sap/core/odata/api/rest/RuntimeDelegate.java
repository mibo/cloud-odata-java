package com.sap.core.odata.api.rest;

import com.sap.core.odata.api.rest.ODataResponse.ODataResponseBuilder;

public abstract class RuntimeDelegate {

  private static final String IMPLEMENTATION = "com.sap.core.odata.core.rest.RuntimeDelegateImpl";
  
  public static RuntimeDelegate getInstance() {
    RuntimeDelegate delegate;
    
    try {
      Class<?> clazz = Class.forName(RuntimeDelegate.IMPLEMENTATION);

      Object object = clazz.newInstance();
      delegate = (RuntimeDelegate) object;
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return delegate;
  }

  public abstract ODataResponseBuilder createODataResponseBuilder();

}