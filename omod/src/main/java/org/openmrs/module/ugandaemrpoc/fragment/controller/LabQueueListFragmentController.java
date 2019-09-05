package org.openmrs.module.ugandaemrpoc.fragment.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.*;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.patientqueueing.utils.QueueingUtil;
import org.openmrs.module.ugandaemrpoc.lab.mapper.LabQueueMapper;
import org.openmrs.module.ugandaemrpoc.lab.mapper.OrderMapper;
import org.openmrs.module.ugandaemrpoc.lab.util.*;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.openmrs.module.ugandaemrpoc.lab.util.LabConfig.PROCESSED_OEDER_WITHOUT_RESULT_QUERY;

public class LabQueueListFragmentController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public LabQueueListFragmentController() {
	}
	
	public void controller(@SpringBean FragmentModel pageModel, UiSessionContext uiSessionContext) {
		
		pageModel.put("specimenSource", Context.getOrderService().getTestSpecimenSources());
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String dateStr = sdf.format(new Date());
		pageModel.addAttribute("currentDate", dateStr);
	}
	
	/**
	 * Get Patients in Lab Queue
	 * 
	 * @param searchfilter
	 * @param uiSessionContext
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public SimpleObject getPatientQueueList(@RequestParam(value = "labSearchFilter", required = false) String searchfilter,
	        UiSessionContext uiSessionContext) throws IOException, ParseException {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleObject simpleObject = new SimpleObject();
		List<PatientQueue> patientQueueList = new ArrayList();
		if (!searchfilter.equals("")) {
			patientQueueList = patientQueueingService.searchQueue(searchfilter,
			    QueueingUtil.dateFormtterString(new Date(), "00:00:00"),
			    QueueingUtil.dateFormtterString(new Date(), "23:59:59"), null, uiSessionContext.getSessionLocation());
		} else {
			patientQueueList = patientQueueingService.getPatientInQueueList(
			    QueueingUtil.dateFormtterDate(new Date(), "00:00:00"),
			    QueueingUtil.dateFormtterDate(new Date(), "23:59:59"), uiSessionContext.getSessionLocation());
		}
		simpleObject.put("patientLabQueueList", objectMapper.writeValueAsString(mapPatientQueueToMapper(patientQueueList)));
		return simpleObject;
	}
	
	/**
	 * Convert PatientQueue List to PatientQueueMapping
	 * 
	 * @param patientQueueList
	 * @return
	 */
	private List<PatientQueueMapper> mapPatientQueueToMapper(List<PatientQueue> patientQueueList) {
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
					labQueueMapper.setOrderMapper(processOrders(patientQueue.getEncounter().getOrders(), true));
				}
				patientQueueMappers.add(labQueueMapper);
			}
		}
		return patientQueueMappers;
	}
	
	/**
	 * Processes Orders from Encounter to Order Mapper
	 * 
	 * @param orders
	 * @return
	 */
	private Set<OrderMapper> processOrders(Set<Order> orders, boolean fiterOutProccessed) {
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
			orderMapper.setInstructions(order.getInstructions());
			orderMapper.setUrgency(order.getUrgency().name());
			orderMapper.setPatient(names.replace("null", ""));
			orderMapper.setOrderId(order.getOrderId());
			orderMapper.setEncounterId(order.getEncounter().getEncounterId());
			if (order.isActive()) {
				orderMapper.setStatus("active");
			}
			
			orderMappers.add(orderMapper);
		}
		return orderMappers;
	}
	
	/**
	 * This Method Schedules an Order basing on the Instructions eg (Test Order, Send to Reference
	 * Lab .....)
	 * 
	 * @param orderId
	 * @param sampleId
	 * @param referenceLab
	 * @return
	 */
	public void scheduleTest(@RequestParam(value = "orderId") String orderId,
	        @RequestParam(value = "sampleId") String sampleId,
	        @RequestParam(value = "specimenSourceId", required = false) String specimenSourceId,
	        @RequestParam(value = "referenceLab", required = false) String referenceLab) {
		OrderService orderService = Context.getOrderService();
		Order order = orderService.getOrderByOrderNumber(orderId);
		
		TestOrder testOrder = new TestOrder();
		testOrder.setAccessionNumber(sampleId);
		if (referenceLab != "") {
			testOrder.setInstructions("REFER TO " + referenceLab);
		}
		testOrder.setConcept(order.getConcept());
		testOrder.setEncounter(order.getEncounter());
		testOrder.setOrderer(order.getOrderer());
		testOrder.setPatient(order.getPatient());
		testOrder.setUrgency(Order.Urgency.STAT);
		testOrder.setCareSetting(order.getCareSetting());
		testOrder.setOrderType(order.getOrderType());
		testOrder.setPreviousOrder(order);
		testOrder.setAction(Order.Action.REVISE);
		testOrder.setSpecimenSource(Context.getConceptService().getConcept(specimenSourceId));
		orderService.saveOrder(testOrder, null);
	}
	
	/**
	 * Get Lab Orders without Results
	 * 
	 * @param asOfDate
	 * @return
	 * @throws IOException
	 */
	public SimpleObject getOrders(@RequestParam(value = "date", required = false) String asOfDate) throws IOException,
	        ParseException {
		return getProcessedOrders(PROCESSED_OEDER_WITHOUT_RESULT_QUERY, asOfDate);
	}
	
	/**
	 * Get Lab Orders without Results
	 * 
	 * @param asOfDate
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public SimpleObject getOrderWithResult(@RequestParam(value = "date", required = false) String asOfDate)
	        throws IOException, ParseException {
		return getProcessedOrders(PROCESSED_OEDER_WITHOUT_RESULT_QUERY, asOfDate);
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
	private SimpleObject getProcessedOrders(String query, String asOfDate) throws ParseException, IOException {
        Date date;
        SimpleObject simpleObject = new SimpleObject();
        ObjectMapper objectMapper = new ObjectMapper();
        OrderService orderService = Context.getOrderService();

        if (!asOfDate.equals("")) {
            date = new Date(asOfDate);
        } else {
            date = new Date();
        }

        query=String.format(query,QueueingUtil.dateFormtterString(date,"00:00:00"),QueueingUtil.dateFormtterString(date,"23:59:59"));

        List list = Context.getAdministrationService().executeSQL(query, true);
        Set<Order> orderList = new HashSet<>();

        if (list.size() > 0) {
            for (Object o : list) {
                orderList.add(orderService.getOrder(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString())));
            }
        }
        if (!orderList.isEmpty()) {
            simpleObject.put("ordersList", objectMapper.writeValueAsString(orderList));
        }
        return simpleObject;
    }
	
	/**
	 * Generates Sample ID on Call from interface
	 * 
	 * @param orderNumber
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public SimpleObject generateSampleID(@RequestParam(value = "orderId", required = false) String orderNumber)
	        throws ParseException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		Order order = Context.getOrderService().getOrderByOrderNumber(orderNumber);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String date = sdf.format(new Date());
		String letter = order.getConcept().getConceptId().toString();
		String defaultSampleId = "";
		int id = 0;
		do {
			++id;
			defaultSampleId = date + "-" + letter + "-" + id;
		} while (this.isSampleIdExisting(defaultSampleId, orderNumber));
		
		return SimpleObject.create("defaultSampleId", objectMapper.writeValueAsString(defaultSampleId));
	}
	
	/**
	 * Checks if Sample ID genereated is already issued out
	 * 
	 * @param sampleId
	 * @param orderNumber
	 * @return
	 * @throws ParseException
	 */
	private boolean isSampleIdExisting(String sampleId, String orderNumber) throws ParseException {
		Order tests = Context.getOrderService().getOrderByOrderNumber(orderNumber);
		if (Context.getAdministrationService()
		        .executeSQL(String.format("select * from orders where accession_number=\"%s\"", sampleId), true).size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Search for results of Test that have been done
	 * 
	 * @param dateStr
	 * @param phrase
	 * @param investigationId
	 * @param ui
	 * @return
	 */
	public List<SimpleObject> searchForResults(@RequestParam(value = "date", required = false) String dateStr,
	        @RequestParam(value = "phrase", required = false) String phrase,
	        @RequestParam(value = "investigation", required = false) Integer investigationId, UiUtils ui) {
		
		Order investigation = Context.getOrderService().getOrder(investigationId);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date date = null;
		List<SimpleObject> simpleObjects = new ArrayList<SimpleObject>();
		
		List<TestModel> tests = LaboratoryUtil.generateModelsFromTests(investigation);
		
		simpleObjects = SimpleObject.fromCollection(tests, ui, "startDate", "patientId", "patientIdentifier", "patientName",
		    "gender", "age", "test.name", "investigation", "testId", "orderId", "sampleId", "status", "value");
		return simpleObjects;
	}
	
	public List<SimpleObject> getResultTemplate(@RequestParam("testId") Integer testId, UiUtils ui) {
		Order test = Context.getOrderService().getOrder(testId);
		List<ParameterModel> parameters = new ArrayList<ParameterModel>();
		LaboratoryUtil.generateParameterModels(parameters, test.getConcept(), null, test);
		//Collections.sort(parameters);
		List<SimpleObject> resultsTemplate = new ArrayList<SimpleObject>();
		for (ParameterModel parameter : parameters) {
			SimpleObject resultTemplate = new SimpleObject();
			resultTemplate.put("type", parameter.getType());
			resultTemplate.put("id", parameter.getId());
			resultTemplate.put("container", parameter.getContainer());
			resultTemplate.put("containerId", parameter.getContainerId());
			resultTemplate.put("title", parameter.getTitle());
			resultTemplate.put("unit", parameter.getUnit());
			resultTemplate.put("validator", parameter.getValidator());
			resultTemplate.put("defaultValue", parameter.getDefaultValue());
			List<SimpleObject> options = new ArrayList<SimpleObject>();
			for (ParameterOption option : parameter.getOptions()) {
				SimpleObject parameterOption = new SimpleObject();
				parameterOption.put("label", option.getLabel());
				parameterOption.put("value", option.getValue());
				options.add(parameterOption);
			}
			resultTemplate.put("options", options);
			resultsTemplate.add(resultTemplate);
		}
		
		return resultsTemplate;
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
	private void addLaboratoryTestObservation(Encounter encounter, Concept testConcept, Concept testGroupConcept,
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
	
	/**
	 * Save Test Results
	 * 
	 * @param resultWrapper
	 * @param sessionContext
	 * @return
	 */
	public SimpleObject saveResult(@BindParams("wrap") ResultModelWrapper resultWrapper, UiSessionContext sessionContext) {
		OrderService orderService = Context.getOrderService();
		Order test = orderService.getOrder(resultWrapper.getTestId());
		Encounter encounter = test.getEncounter();
		encounter.setLocation(sessionContext.getSessionLocation());
		
		//TODO get date from user
		Provider provider = new Provider(); //TODO get Provider from Current User
		
		String result = null;
		
		String resultDisplay = "\n";
		
		for (ResultModel resultModel : resultWrapper.getResults()) {
			result = resultModel.getSelectedOption() == null ? resultModel.getValue() : resultModel.getSelectedOption();
			
			if (StringUtils.isBlank(result)) {
				continue;
			}
			
			if (StringUtils.contains(resultModel.getConceptName(), ".")) {
				String[] parentChildConceptIds = StringUtils.split(resultModel.getConceptName(), ".");
				Concept testGroupConcept = Context.getConceptService().getConcept(parentChildConceptIds[0]);
				Concept testConcept = Context.getConceptService().getConcept(parentChildConceptIds[1]);
				addLaboratoryTestObservation(encounter, testConcept, testGroupConcept, result, test);
				if (StringUtils.isNumeric(result)) {
					resultDisplay += testConcept.getName().getName() + "\t"
					        + Context.getConceptService().getConcept(result).getName().getName() + "\n";
				} else {
					resultDisplay += testConcept.getName().getName() + "\t" + result + "\n";
				}
			} else {
				Concept concept = Context.getConceptService().getConcept(resultModel.getConceptName());
				addLaboratoryTestObservation(encounter, concept, null, result, test);
				resultDisplay += concept.getName().getName() + "\t" + result + "\n";
			}
		}
		
		encounter = Context.getEncounterService().saveEncounter(encounter);
		
		test.setEncounter(encounter);
		
		try {
			// TODO Get Provider From Current User and Assign them to the null the statement below
			orderService.discontinueOrder(test, "Completed", new Date(), null, test.getEncounter());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		//TODO  QUEUE Patient from Previous Location this.sendPatientToOpdQueue(encounter);
		//        Send results via e-mail if the patient was referred to us.
		return SimpleObject.create("status", "success", "message", "Saved!");
	}
	
}
