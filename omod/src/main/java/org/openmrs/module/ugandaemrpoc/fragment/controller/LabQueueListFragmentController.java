package org.openmrs.module.ugandaemrpoc.fragment.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.*;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.patientqueueing.utils.QueueingUtil;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrpoc.lab.util.LabConfig.PROCESSED_ORDER_WITHOUT_RESULT_QUERY;
import static org.openmrs.module.ugandaemrpoc.lab.util.LabConfig.PROCESSED_ORDER_WITH_RESULT_QUERY;

public class LabQueueListFragmentController {
	
	protected final Log log = LogFactory.getLog(LabQueueListFragmentController.class);
	
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
		simpleObject.put(
		    "patientLabQueueList",
		    objectMapper.writeValueAsString(Context.getService(UgandaEMRPOCService.class).mapPatientQueueToMapper(
		        patientQueueList)));
		return simpleObject;
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
		
		return Context.getService(UgandaEMRPOCService.class).getProcessedOrders(PROCESSED_ORDER_WITHOUT_RESULT_QUERY,
		    asOfDate, true);
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
		return Context.getService(UgandaEMRPOCService.class).getProcessedOrders(PROCESSED_ORDER_WITH_RESULT_QUERY, asOfDate,
		    true);
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
		UgandaEMRPOCService ugandaEMRPOCService = Context.getService(UgandaEMRPOCService.class);
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
		} while (ugandaEMRPOCService.isSampleIdExisting(defaultSampleId, orderNumber));
		
		return SimpleObject.create("defaultSampleId", objectMapper.writeValueAsString(defaultSampleId));
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
	 * Save Test Results
	 * 
	 * @param resultWrapper
	 * @param sessionContext
	 * @return
	 */
	public SimpleObject saveResult(@BindParams("wrap") ResultModelWrapper resultWrapper, UiSessionContext sessionContext) {
		Provider provider = sessionContext.getCurrentProvider();
		String result = null;
		String resultDisplay = "";
		OrderService orderService = Context.getOrderService();
		EncounterService encounterService = Context.getEncounterService();
		UgandaEMRPOCService ugandaEMRPOCService = Context.getService(UgandaEMRPOCService.class);
		
		Order test = orderService.getOrder(resultWrapper.getTestId());
		
		Encounter encounter = test.getEncounter();
		for (ResultModel resultModel : resultWrapper.getResults()) {
			result = resultModel.getSelectedOption() == null ? resultModel.getValue() : resultModel.getSelectedOption();
			if (StringUtils.isBlank(result)) {
				continue;
			}
			if (StringUtils.contains(resultModel.getConceptName(), ".")) {
				String[] parentChildConceptIds = StringUtils.split(resultModel.getConceptName(), ".");
				Concept testGroupConcept = Context.getConceptService().getConcept(parentChildConceptIds[0]);
				Concept testConcept = Context.getConceptService().getConcept(parentChildConceptIds[1]);
				ugandaEMRPOCService.addLaboratoryTestObservation(encounter, testConcept, testGroupConcept, result, test);
				if (StringUtils.isNumeric(result)) {
					resultDisplay += testConcept.getName().getName() + "\t"
					        + Context.getConceptService().getConcept(result).getName().getName() + "\n";
				} else {
					resultDisplay += testConcept.getName().getName() + "\t" + result + "\n";
				}
			} else {
				Concept concept = Context.getConceptService().getConcept(resultModel.getConceptName());
				ugandaEMRPOCService.addLaboratoryTestObservation(encounter, concept, null, result, test);
				resultDisplay += concept.getName().getName() + "\t" + result + "\n";
			}
		}
		
		encounter = encounterService.saveEncounter(encounter);
		test.setEncounter(encounter);
		try {
			orderService.discontinueOrder(test, "Completed", new Date(), provider, test.getEncounter());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return SimpleObject.create("status", "success", "message", "Saved!");
	}
}
