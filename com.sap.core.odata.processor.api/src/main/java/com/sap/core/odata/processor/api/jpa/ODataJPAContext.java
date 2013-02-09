package com.sap.core.odata.processor.api.jpa;

import javax.persistence.EntityManagerFactory;

import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.processor.ODataContext;
import com.sap.core.odata.api.processor.ODataProcessor;
import com.sap.core.odata.processor.api.jpa.factory.ODataJPAAccessFactory;
import com.sap.core.odata.processor.api.jpa.factory.ODataJPAFactory;

/**
 * This class does the compilation of context objects required for OData JPA
 * Runtime. The context object should be properly initialized with values else
 * the behavior of processor and EDM provider can result in exception.
 * 
 * Following are the mandatory parameter to be set into the context object
 * <ol>
 * <li>Persistence Unit Name</li>
 * <li>An instance of Java Persistence Entity Manager Factory</li>
 * </ol>
 * 
 * @author SAP AG <br>
 * @DoNotImplement
 * @see {@link ODataJPAFactory}, {@link ODataJPAAccessFactory}
 * 
 */
public interface ODataJPAContext {

	/**
	 * The method gets the Java Persistence Unit Name set into the context.
	 * 
	 * @return Java Persistence Unit Name
	 */
	public String getPersistenceUnitName();

	/**
	 * The method sets the Java Persistence Unit Name into the context.
	 * 
	 * @param pUnitName
	 *            is the Java Persistence Unit Name.
	 * 
	 */
	public void setPersistenceUnitName(String pUnitName);

	/**
	 * The method gets the OData Processor for JPA from the context.
	 * 
	 * @return OData JPA Processor
	 */
	public ODataProcessor getODataProcessor();

	/**
	 * The method sets the OData Processor for JPA into the context.
	 * 
	 * @param processor
	 *            is the specific implementation of {@link ODataJPAProcessor}
	 *            for processing OData service requests.
	 */
	public void setODataProcessor(ODataProcessor processor);

	/**
	 * The method gets the EDM provider for JPA from the context.
	 * 
	 * @return EDM provider
	 */
	public EdmProvider getEdmProvider();

	/**
	 * The method sets EDM provider into the context
	 * 
	 * @param edmProvider
	 *            is the specific implementation of {@link EdmProvider} for
	 *            transforming Java persistence models to Entity Data Model
	 * 
	 */
	public void setEdmProvider(EdmProvider edmProvider);

	/**
	 * The method gets the Java Persistence Entity Manager factory from the
	 * context.
	 * 
	 * @return an instance of Java Persistence Entity Manager Factory
	 */
	public EntityManagerFactory getEntityManagerFactory();

	/**
	 * The method sets the Java Persistence Entity Manager factory into the
	 * context.
	 * 
	 * @param emf
	 *            is of type {@link EntityManagerFactory}
	 * 
	 */
	public void setEntityManagerFactory(EntityManagerFactory emf);

	/**
	 * The method gets OData Context into the context.
	 * 
	 * @return OData Context
	 */
	public ODataContext getODataContext();

	/**
	 * The method sets OData context into the context.
	 * 
	 * @param ctx
	 *            is an OData context of type {@link ODataContext}
	 */
	public void setODataContext(ODataContext ctx);

}
