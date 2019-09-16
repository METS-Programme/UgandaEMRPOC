package org.openmrs.module.ugandaemrpoc.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.patientqueueing.utils.QueueingUtil;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrpoc.UgandaEMRPOCConfig.TRIAGE_LOCATION_UUID;

public class TriageQueueListFragmentController {
	
	protected final Log log = LogFactory.getLog(TriageQueueListFragmentController.class);
	
	public TriageQueueListFragmentController() {
	}
	
	public void controller(@SpringBean FragmentModel pageModel, UiSessionContext uiSessionContext) {
		
		pageModel.put("specimenSource", Context.getOrderService().getTestSpecimenSources());
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String dateStr = sdf.format(new Date());
		pageModel.addAttribute("currentDate", dateStr);
		pageModel.addAttribute("locationSession", uiSessionContext.getSessionLocation().getUuid());
		pageModel.addAttribute("triageLocation", TRIAGE_LOCATION_UUID);
		
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
	public SimpleObject getPatientQueueList(
	        @RequestParam(value = "triageSearchFilter", required = false) String searchfilter,
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
		simpleObject.put("patientTriageQueueList",
		    objectMapper.writeValueAsString(mapPatientQueueToMapper(patientQueueList)));
		return simpleObject;
	}
	
	public List<PatientQueueMapper> mapPatientQueueToMapper(List<PatientQueue> patientQueueList) {
		List<PatientQueueMapper> patientQueueMappers = new ArrayList<PatientQueueMapper>();
		
		for (PatientQueue patientQueue : patientQueueList) {
			String names = patientQueue.getPatient().getFamilyName() + " " + patientQueue.getPatient().getGivenName() + " "
			        + patientQueue.getPatient().getMiddleName();
			PatientQueueMapper patientQueueMapper = new PatientQueueMapper();
			patientQueueMapper.setId(patientQueue.getId());
			patientQueueMapper.setPatientNames(names.replace("null", ""));
			patientQueueMapper.setPatientId(patientQueue.getPatient().getPatientId());
			patientQueueMapper.setLocationFrom(patientQueue.getLocationFrom().getName());
			patientQueueMapper.setLocationTo(patientQueue.getLocationTo().getName());
			patientQueueMapper.setQueueNumber(patientQueue.getQueueNumber());
			
			if (patientQueue.getProvider() != null) {
				patientQueueMapper.setProviderNames(patientQueue.getProvider().getName());
			}
			
			if (patientQueue.getCreator() != null) {
				patientQueueMapper.setCreatorNames(patientQueue.getCreator().getDisplayString());
			}
			
			patientQueueMapper.setStatus(patientQueue.getStatus());
			patientQueueMapper.setAge(patientQueue.getPatient().getAge().toString());
			patientQueueMapper.setGender(patientQueue.getPatient().getGender());
			patientQueueMapper.setDateCreated(patientQueue.getDateCreated().toString());
			patientQueueMappers.add(patientQueueMapper);
		}
		return patientQueueMappers;
	}
}
