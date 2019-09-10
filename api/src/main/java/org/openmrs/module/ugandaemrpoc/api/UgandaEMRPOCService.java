package org.openmrs.module.ugandaemrpoc.api;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.ugandaemrpoc.lab.mapper.OrderMapper;
import org.openmrs.module.ugandaemrpoc.lab.util.TestResultModel;
import org.openmrs.ui.framework.SimpleObject;

import java.io.IOException;
import java.text.ParseException;
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
	public SimpleObject getProcessedOrders(String query, String asOfDate, boolean includeProccesed) throws ParseException,
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
	 * Process Orders
	 * 
	 * @param formSession
	 * @return
	 */
	public Encounter processLabTestOrdersFromEncounterObs(FormEntrySession formSession);
	
	/**
	 * Send Patient To Lab
	 * 
	 * @param session
	 */
	public void sendPatientToLab(FormEntrySession session);
}
