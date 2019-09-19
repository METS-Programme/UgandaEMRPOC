package org.openmrs.module.ugandaemrpoc.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.patientqueueing.utils.QueueingUtil;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
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
		pageModel.put("currentProvider", Context.getAuthenticatedUser());
		
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
		UgandaEMRPOCService ugandaEMRPOCService = Context.getService(UgandaEMRPOCService.class);
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
		    objectMapper.writeValueAsString(ugandaEMRPOCService.mapPatientQueueToMapper(patientQueueList)));
		return simpleObject;
	}
}
