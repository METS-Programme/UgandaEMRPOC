package org.openmrs.module.ugandaemrpoc.api.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.*;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.patientqueueing.utils.QueueingUtil;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
import org.openmrs.module.ugandaemrpoc.lab.mapper.LabQueueMapper;
import org.openmrs.module.ugandaemrpoc.lab.mapper.OrderMapper;
import org.openmrs.module.ugandaemrpoc.lab.util.LaboratoryUtil;
import org.openmrs.module.ugandaemrpoc.lab.util.TestResultModel;
import org.openmrs.ui.framework.SimpleObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static org.openmrs.module.ugandaemrpoc.lab.util.LabConfig.*;

public class UgandaEMRPOCServiceImpl extends BaseOpenmrsService implements UgandaEMRPOCService {
	
	protected final Log log = LogFactory.getLog(UgandaEMRPOCServiceImpl.class);
	
	/**
	 * Checks if Sample ID genereated is already issued out
	 * 
	 * @param sampleId
	 * @param orderNumber
	 * @return
	 * @throws ParseException
	 */
	public boolean isSampleIdExisting(String sampleId, String orderNumber) throws ParseException {
		Order tests = Context.getOrderService().getOrderByOrderNumber(orderNumber);
		if (Context.getAdministrationService()
		        .executeSQL(String.format("select * from orders where accession_number=\"%s\"", sampleId), true).size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @param test
	 * @return
	 */
	public Set<TestResultModel> renderTests(Order test) {
		Set<TestResultModel> trms = new HashSet<TestResultModel>();
		if (test.getEncounter() != null) {
			Encounter encounter = test.getEncounter();
			for (Obs obs : encounter.getAllObs()) {
				if (obs.getOrder() != null) {
					if (obs.hasGroupMembers()) {
						for (Obs groupMemberObs : obs.getGroupMembers()) {
							TestResultModel trm = new TestResultModel();
							trm.setInvestigation(test.getConcept().getDisplayString());
							trm.setSet(obs.getConcept().getDisplayString());
							trm.setConcept(obs.getConcept());
							setTestResultModelValue(groupMemberObs, trm);
							trms.add(trm);
						}
					} else if (obs.getObsGroup() == null) {
						TestResultModel trm = new TestResultModel();
						trm.setInvestigation(test.getConcept().getName().getName());
						trm.setSet(test.getConcept().getDatatype().getName());
						trm.setConcept(obs.getConcept());
						setTestResultModelValue(obs, trm);
						trms.add(trm);
					}
				}
			}
		}
		return trms;
	}
	
	/**
	 * Process Lab Orders
	 * 
	 * @param query
	 * @param asOfDate
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public SimpleObject getProcessedOrders(String query, String asOfDate, boolean includeProccesed) throws ParseException,
	        IOException {
		Date date;
		SimpleObject simpleObject = new SimpleObject();
		ObjectMapper objectMapper = new ObjectMapper();
		OrderService orderService = Context.getOrderService();
		
		if (!asOfDate.equals("")) {
			date = new Date(asOfDate);
		} else {
			date = new Date();
		}
		query = String.format(query, QueueingUtil.dateFormtterString(date, "00:00:00"),
		    QueueingUtil.dateFormtterString(date, "23:59:59"));
		
		List list = Context.getAdministrationService().executeSQL(query, true);
		Set<Order> unProcesedOrderList = new HashSet<Order>();
		
		Set<Order> proccesedOrderList = new HashSet<Order>();
		
		if (list.size() > 0) {
			for (Object o : list) {
				Order order = orderService.getOrder(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString()));
				if (order.getAccessionNumber() == null) {
					unProcesedOrderList.add(order);
				}
				proccesedOrderList.add(order);
			}
		}
		
		if (includeProccesed && !proccesedOrderList.isEmpty()) {
			simpleObject.put("ordersList", objectMapper.writeValueAsString(processOrders(proccesedOrderList, true)));
		} else if (!unProcesedOrderList.isEmpty() && !includeProccesed) {
			simpleObject.put("ordersList", objectMapper.writeValueAsString(processOrders(unProcesedOrderList, true)));
		}
		return simpleObject;
	}
	
	/**
	 * Processes Orders from Encounter to Order Mapper
	 * 
	 * @param orders
	 * @return
	 */
	
	public Set<OrderMapper> processOrders(Set<Order> orders, boolean fiterOutProccessed) {
		Set<OrderMapper> orderMappers = new HashSet<OrderMapper>();
		for (Order order : orders) {
			String names = order.getPatient().getFamilyName() + " " + order.getPatient().getGivenName() + " "
			        + order.getPatient().getMiddleName();
			OrderMapper orderMapper = new OrderMapper();
			orderMapper.setAccessionNumber(order.getAccessionNumber());
			orderMapper.setCareSetting(order.getCareSetting().getName());
			orderMapper.setConcept(order.getConcept().getConceptId().toString());
			orderMapper.setConceptName(order.getConcept().getDisplayString());
			orderMapper.setDateActivated(order.getDateActivated().toString());
			orderMapper.setOrderer(order.getOrderer().getName());
			orderMapper.setOrderNumber(order.getOrderNumber());
			orderMapper.setPatientId(order.getPatient().getPatientId());
			orderMapper.setInstructions(order.getInstructions());
			orderMapper.setUrgency(order.getUrgency().name());
			orderMapper.setPatient(names.replace("null", ""));
			orderMapper.setOrderId(order.getOrderId());
			orderMapper.setEncounterId(order.getEncounter().getEncounterId());
			if (order.isActive()) {
				orderMapper.setStatus(QUEUE_STATUS_ACTIVE);
			}
			if (orderHasResults(order)) {
				orderMapper.setStatus(QUEUE_STATUS_HAS_RESULTS);
			}
			orderMappers.add(orderMapper);
		}
		return orderMappers;
	}
	
	/**
	 * Set Results Model
	 * 
	 * @param obs
	 * @param trm
	 */
	private void setTestResultModelValue(Obs obs, TestResultModel trm) {
		Concept concept = obs.getConcept();
		trm.setTest(obs.getConcept().getDisplayString());
		if (concept != null) {
			String datatype = concept.getDatatype().getName();
			if (datatype.equalsIgnoreCase("Text")) {
				trm.setValue(obs.getValueText());
			} else if (datatype.equalsIgnoreCase("Numeric")) {
				if (obs.getValueText() != null) {
					trm.setValue(obs.getValueText().toString());
				} else if (obs.getValueNumeric() != null) {
					trm.setValue(obs.getValueNumeric().toString());
				}
				ConceptNumeric cn = Context.getConceptService().getConceptNumeric(concept.getConceptId());
				trm.setUnit(cn.getUnits());
				if (cn.getLowNormal() != null)
					trm.setLowNormal(cn.getLowNormal().toString());
				if (cn.getHiNormal() != null)
					trm.setHiNormal(cn.getHiNormal().toString());
				if (cn.getHiAbsolute() != null) {
					trm.setHiAbsolute(cn.getHiAbsolute().toString());
				}
				if (cn.getHiCritical() != null) {
					trm.setHiCritical(cn.getHiCritical().toString());
				}
				if (cn.getLowAbsolute() != null) {
					trm.setLowAbsolute(cn.getLowAbsolute().toString());
				}
				if (cn.getLowCritical() != null) {
					trm.setLowCritical(cn.getLowCritical().toString());
				}
				
			} else if (datatype.equalsIgnoreCase("Coded")) {
				trm.setValue(obs.getValueCoded().getName().getName());
			}
		}
	}
	
	private boolean orderHasResults(Order order) {
		if (Context.getAdministrationService()
		        .executeSQL("select obs_id from obs where order_id=" + order.getOrderId() + "", true).size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Add Lab Results Observation to Encounter
	 * 
	 * @param encounter
	 * @param testConcept
	 * @param testGroupConcept
	 * @param result
	 * @param test
	 */
	public void addLaboratoryTestObservation(Encounter encounter, Concept testConcept, Concept testGroupConcept,
	        String result, Order test) {
		log.warn("testConceptId=" + testConcept);
		log.warn("testGroupConceptId=" + testGroupConcept);
		Obs obs = new Obs();
		obs = getObs(encounter, testConcept, testGroupConcept);
		setObsAttributes(obs, encounter);
		obs.setConcept(testConcept);
		obs.setOrder(test);
		
		if (testConcept.getDatatype().getName().equalsIgnoreCase("Text")) {
			obs.setValueText(result);
		} else if (testConcept.getDatatype().getName().equalsIgnoreCase("Numeric")) {
			if (StringUtils.isNotBlank(result)) {
				obs.setValueNumeric(Double.parseDouble(result));
			}
		} else if (testConcept.getDatatype().getName().equalsIgnoreCase("Coded")) {
			Concept answerConcept = LaboratoryUtil.searchConcept(result);
			obs.setValueCoded(answerConcept);
		}
		if (testGroupConcept != null) {
			Obs testGroupObs = getObs(encounter, testGroupConcept, null);
			if (testGroupObs.getConcept() == null) {
				//TODO find out what valueGroupId is and set to testGroupObs if necessary
				testGroupObs.setConcept(testGroupConcept);
				testGroupObs.setOrder(test);
				setObsAttributes(testGroupObs, encounter);
				encounter.addObs(testGroupObs);
			}
			log.warn("Adding obs[concept=" + obs.getConcept() + ",uuid=" + obs.getUuid() + "] to obsgroup[concept="
			        + testGroupObs.getConcept() + ", uuid=" + testGroupObs.getUuid() + "]");
			testGroupObs.addGroupMember(obs);
		} else {
			encounter.addObs(obs);
		}
		
		log.warn("Obs size is: " + encounter.getObs().size());
	}
	
	/**
	 * Convert PatientQueue List to PatientQueueMapping
	 * 
	 * @param patientQueueList
	 * @return
	 */
	public List<PatientQueueMapper> mapPatientQueueToMapper(List<PatientQueue> patientQueueList) {
		List<PatientQueueMapper> patientQueueMappers = new ArrayList<PatientQueueMapper>();
		
		for (PatientQueue patientQueue : patientQueueList) {
			if (patientQueue.getEncounter() != null && patientQueue.getEncounter().getOrders().size() > 0) {
				String names = patientQueue.getPatient().getFamilyName() + " " + patientQueue.getPatient().getGivenName()
				        + " " + patientQueue.getPatient().getMiddleName();
				LabQueueMapper labQueueMapper = new LabQueueMapper();
				labQueueMapper.setId(patientQueue.getId());
				labQueueMapper.setPatientNames(names.replace("null", ""));
				labQueueMapper.setPatientId(patientQueue.getPatient().getPatientId());
				labQueueMapper.setLocationFrom(patientQueue.getLocationFrom().getName());
				labQueueMapper.setLocationTo(patientQueue.getLocationTo().getName());
				labQueueMapper.setProviderNames(patientQueue.getProvider().getName());
				labQueueMapper.setStatus(patientQueue.getStatus());
				labQueueMapper.setAge(patientQueue.getPatient().getAge().toString());
				labQueueMapper.setDateCreated(patientQueue.getDateCreated().toString());
				labQueueMapper.setEncounterId(patientQueue.getEncounter().getEncounterId());
				if (patientQueue.getEncounter() != null) {
					labQueueMapper.setOrderMapper(Context.getService(UgandaEMRPOCService.class).processOrders(
					    patientQueue.getEncounter().getOrders(), true));
				}
				patientQueueMappers.add(labQueueMapper);
			}
		}
		return patientQueueMappers;
	}
	
	/**
	 * Set Attributes for Observation
	 * 
	 * @param obs
	 * @param encounter
	 */
	private void setObsAttributes(Obs obs, Encounter encounter) {
		obs.setObsDatetime(encounter.getEncounterDatetime());
		obs.setPerson(encounter.getPatient());
		obs.setLocation(encounter.getLocation());
		obs.setEncounter(encounter);
	}
	
	/**
	 * Get Existing Observation of The Encounter Which the results are going to be returned
	 * 
	 * @param encounter
	 * @param concept
	 * @param groupingConcept
	 * @return
	 */
	private Obs getObs(Encounter encounter, Concept concept, Concept groupingConcept) {
		for (Obs obs : encounter.getAllObs()) {
			if (groupingConcept != null) {
				Obs obsGroup = getObs(encounter, groupingConcept, null);
				if (obsGroup.getGroupMembers() != null) {
					for (Obs member : obsGroup.getGroupMembers()) {
						if (member.getConcept().equals(concept)) {
							return member;
						}
					}
				}
			} else if (obs.getConcept().equals(concept)) {
				return obs;
			}
		}
		return new Obs();
	}
	
	public void sendPatientToLab(FormEntrySession session) {
		PatientQueue patientQueue = new PatientQueue();
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		Location location = Context.getLocationService().getLocationByUuid(LAB_LOCATION_UUID);
		Provider provider = getProvider(session.getEncounter());
		patientQueue.setLocationFrom(session.getEncounter().getLocation());
		patientQueue.setPatient(session.getEncounter().getPatient());
		patientQueue.setLocationTo(location);
		patientQueue.setProvider(provider);
		patientQueue.setEncounter(session.getEncounter());
		patientQueue.setStatus(QUEUE_STATUS_PENDING);
		patientQueue.setCreator(Context.getUserService().getUsersByPerson(provider.getPerson(), false).get(0));
		patientQueue.setDateCreated(new Date());
		patientQueueingService.savePatientQue(patientQueue);
	}
	
	public Set<Order> proccessLabTestOrdersFromObs(Set<Obs> obsList) {
		Set<Order> orders = new HashSet<Order>();
		CareSetting careSetting = Context.getOrderService().getCareSettingByName(CARE_SETTING_OPD);
		
		for (Obs obs : obsList) {
			if (obs.getValueCoded() != null
			        && (obs.getValueCoded().getConceptClass().getName().equals(LAB_SET_CLASS) || obs.getValueCoded()
			                .getConceptClass().getName().equals(TEST_SET_CLASS))) {
				TestOrder testOrder = new TestOrder();
				testOrder.setConcept(obs.getValueCoded());
				testOrder.setEncounter(obs.getEncounter());
				testOrder.setOrderer(getProvider(obs.getEncounter()));
				testOrder.setPatient(obs.getEncounter().getPatient());
				testOrder.setUrgency(Order.Urgency.STAT);
				testOrder.setCareSetting(careSetting);
				orders.add(testOrder);
			}
		}
		return orders;
	}
	
	public Provider getProvider(Encounter encounter) {
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRoleByUuid(ENCOUNTER_ROLE);
		
		Set<Provider> providers = encounter.getProvidersByRole(encounterRole);
		for (Provider provider : providers) {
			return provider;
		}
		return null;
	}
}
