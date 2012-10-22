package com.sap.core.odata.ref.producer;

import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.core.edm.Edm;
import com.sap.core.odata.core.edm.EdmAssociation;
import com.sap.core.odata.core.edm.EdmComplexType;
import com.sap.core.odata.core.edm.EdmEntityContainer;
import com.sap.core.odata.core.edm.EdmEntityType;
import com.sap.core.odata.core.edm.EdmServiceMetadata;
import com.sap.core.odata.core.producer.EntitySet;
import com.sap.core.odata.core.producer.Metadata;
import com.sap.core.odata.core.producer.ODataProducer;
import com.sap.core.odata.core.producer.ODataResponseImpl;

public class ScenarioProducer extends ODataProducer implements EntitySet, Metadata {

  private static final Logger log = LoggerFactory.getLogger(ScenarioProducer.class);

  private String segment1;
  private String segment2;

  public String getSegment1() {
    return segment1;
  }

  public String getSegment2() {
    return segment2;
  }

  public void injectServiceResolutionPath(String segment1, String segment2) {
    this.segment1 = segment1;
    this.segment2 = segment2;
    ScenarioProducer.log.debug("service resolution segment1: " + this.segment1);
    ScenarioProducer.log.debug("service resolution segment2: " + this.segment2);
  }

  @Override
  public ODataResponseImpl count() {
    return null;
  }

  @Override
  public ODataResponseImpl createEntity() {
    return null;
  }

  @Override
  public Edm getEdm() {
    return new EdmImpl();
  }

  @Override
  public ODataResponseImpl read() {
    return ODataResponseImpl.status(Status.OK.getStatusCode()).entity("$metadata").build();
  }

  private class EdmImpl implements Edm {

    @Override
    public EdmEntityContainer getEntityContainer(String name) {
      return null;
    }

    @Override
    public EdmEntityType getEntityType(String namespace, String name) {
      return null;
    }

    @Override
    public EdmComplexType getComplexType(String namespace, String name) {
      return null;
    }

    @Override
    public EdmAssociation getAssociation(String namespace, String name) {
      return null;
    }

    @Override
    public EdmServiceMetadata getServiceMetadata() {
      return new EdmServiceMetadataImpl();
    }

    @Override
    public EdmEntityContainer getDefaultEntityContainer() {
      return null;
    }

  }

  private class EdmServiceMetadataImpl implements EdmServiceMetadata {

    @Override
    public byte[] getMetadata() {
      return null;
    }

    @Override
    public String getDataServiceVersion() {
      return "2.0";
    }

  }

}