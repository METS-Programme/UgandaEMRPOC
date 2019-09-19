package org.openmrs.module.ugandaemrpoc.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.patientqueueing.utils.QueueingUtil;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClinicianQueueListFragmentController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public ClinicianQueueListFragmentController() {
	}
	
	public void controller(@SpringBean FragmentModel pageModel,
	        @SpringBean("locationService") LocationService locationService, UiSessionContext uiSessionContext) {
		SimpleObject simpleObject = new SimpleObject();
		
		List<String> list = new ArrayList();
		list.add("86863db4-6101-4ecf-9a86-5e716d6504e4");
		list.add("11d5d2b8-0fdd-42e0-9f53-257c760bb9a3");
		list.add("e9bc61b5-69ff-414b-9cf0-0c22d6dfca2b");
		list.add("8f96e239-8586-4ec6-9375-04c6e19628ae");
		list.add("8ab22b55-9a17-4121-bf08-6134a9a2439f");
		pageModel.put("locationList", ((Location) locationService.getRootLocations(false).get(0)).getChildLocations());
		pageModel.put("clinicianLocation", list);
		pageModel.put("currentProvider", Context.getAuthenticatedUser());
	}
	
	public SimpleObject getPatientQueueList(@RequestParam(value = "searchfilter", required = false) String searchfilter,
	        UiSessionContext uiSessionContext) {
		UgandaEMRPOCService ugandaEMRPOCService = Context.getService(UgandaEMRPOCService.class);
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		ObjectMapper objectMapper = new ObjectMapper();
		
		SimpleObject simpleObject = new SimpleObject();
		
		List<PatientQueue> patientQueueList = new ArrayList();
		if (!searchfilter.equals("")) {
			try {
				patientQueueList = patientQueueingService.searchQueue(searchfilter,
				    QueueingUtil.dateFormtterString(new Date(), "00:00:00"),
				    QueueingUtil.dateFormtterString(new Date(), "23:59:59"), null, uiSessionContext.getSessionLocation());
			}
			catch (ParseException e) {
				log.error(e);
			}
		} else {
			try {
				patientQueueList = patientQueueingService.getPatientInQueueList(null,
				    QueueingUtil.dateFormtterDate(new Date(), "00:00:00"),
				    QueueingUtil.dateFormtterDate(new Date(), "23:59:59"), uiSessionContext.getSessionLocation());
			}
			catch (ParseException e) {
				log.error(e);
			}
			
		}
		List<PatientQueueMapper> patientQueueMappers = ugandaEMRPOCService.mapPatientQueueToMapper(patientQueueList);
		try {
			simpleObject.put("patientClinicianQueueList", objectMapper.writeValueAsString(patientQueueMappers));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return simpleObject;
	}
	
	/**
	 * Convert PatientQueue List to PatientQueueMapping
	 * 
	 * @param patientQueueList
	 * @return
	 */
}
