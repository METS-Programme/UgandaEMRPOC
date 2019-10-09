package org.openmrs.module.ugandaemrpoc.api;

import org.openmrs.*;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.ugandaemrpoc.lab.mapper.OrderMapper;
import org.openmrs.module.ugandaemrpoc.lab.util.TestResultModel;
import org.openmrs.ui.framework.SimpleObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;

public abstract interface UgandaEMRPOCService extends OpenmrsService {
	
	/**
	 * Render Tests
	 * 
	 * @param test
	 * @return
	 */
	public Set<TestResultModel> renderTests(Order test);
	
	/**
	 * Check if Sample ID exists
	 * 
	 * @param sampleId
	 * @param orderNumber
	 * @return
	 * @throws ParseException
	 */
	public boolean isSampleIdExisting(String sampleId, String orderNumber) throws ParseException;
	
	/**
	 * Process Orders
	 * 
	 * @param query
	 * @param asOfDate
	 * @param includeProccesed
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public SimpleObject getProcessedOrders(String query, Date asOfDate, boolean includeProccesed) throws ParseException,
	        IOException;
	
	/**
	 * Convert Orders to OrderMappers
	 * 
	 * @param orders
	 * @param fiterOutProccessed
	 * @return
	 */
	public Set<OrderMapper> processOrders(Set<Order> orders, boolean fiterOutProccessed);
	
	/**
	 * @param encounter
	 * @param testConcept
	 * @param testGroupConcept
	 * @param result
	 * @param test
	 */
	public void addLaboratoryTestObservation(Encounter encounter, Concept testConcept, Concept testGroupConcept,
	        String result, Order test);
	
	/**
	 * @param patientQueueList
	 * @return
	 */
	public List<PatientQueueMapper> mapPatientQueueToMapper(List<PatientQueue> patientQueueList);
	
	/**
	 * With Orders
	 *
	 * @param patientQueueList
	 * @return
	 */
	public List<PatientQueueMapper> mapPatientQueueToMapperWithOrders(List<PatientQueue> patientQueueList);

	/**
	 * Process Orders
	 * 
	 * @param formSession
	 * @return
	 */
	public Encounter processLabTestOrdersFromEncounterObs(FormEntrySession formSession, boolean completePreviousQueue);
	
	/**
	 * Send Patient To Lab
	 * 
	 * @param session
	 */
	public void sendPatientToNextLocation(FormEntrySession session, String locationUUID,String locationFromUUID,String completePreviousQueueStatus,String nextQueueStatus,boolean completePreviousQueue);

	/**
	 * Complete Previous Queue of Patient
	 *
	 * @param patient
	 * @return
	 */
	public PatientQueue completePreviousQueue(Patient patient, Location location,String completionStatus, String searchStatus);

	/**
	 * @param encounter
	 * @return
	 */
	Provider getProviderFromEncounter(Encounter encounter);

	/**
	 * @param patient
	 * @param location
	 * @return
	 */
	public PatientQueue getPreviousQueue(Patient patient, Location location,String status);

	/**
	 * @param query
	 * @param encounterId
	 * @param includeProccesed
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public SimpleObject getOrderResultsOnEncounter(String query, int encounterId, boolean includeProccesed)
	        throws ParseException, IOException;

	/**
	 *
	 * @param encounter
	 * @param locationTo
	 * @return
	 * @throws ParseException
	 */
	public boolean patientQueueExists(Encounter encounter, Location locationTo,Location locationFrom,String status) throws ParseException;
}
