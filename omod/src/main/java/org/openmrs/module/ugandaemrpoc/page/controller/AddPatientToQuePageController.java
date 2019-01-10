package org.openmrs.module.ugandaemrpoc.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.fragment.controller.visit.QuickVisitFragmentController;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
import org.openmrs.module.ugandaemrpoc.model.PatientQue;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class AddPatientToQuePageController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public AddPatientToQuePageController() {
	}
	
	public void get(@SpringBean PageModel pageModel,
	        @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride,
	        @SpringBean("patientService") PatientService patientService, @RequestParam("patientId") Patient patient,
	        UiSessionContext uiSessionContext) {
		pageModel.put("birthDate", patient.getBirthdate());
		pageModel.put("patient", patient);
		pageModel.put("patientId", patient.getPatientId());
		pageModel.put("locationList",
		    ((Location) Context.getLocationService().getRootLocations(false).get(0)).getChildLocations());
		pageModel.put("providerList", Context.getProviderService().getAllProviders(false));
		ClinicianDashboardPageController clinicianDashboardPageController = new ClinicianDashboardPageController();
		
		pageModel.put("breadcrumbOverride", breadcrumbOverride);
	}
	
	public String post(@SpringBean("patientService") PatientService patientService,
	        @RequestParam("patientId") Patient patient, @RequestParam("providerId") Provider provider, UiUtils ui,
	        @RequestParam("locationId") Location location,
	        @RequestParam(value = "returnUrl", required = false) String returnUrl, UiSessionContext uiSessionContext,
	        UiUtils uiUtils, HttpServletRequest request) {
		PatientQue patientQue = new PatientQue();
		
		patientQue.setLocationFrom(uiSessionContext.getSessionLocation());
		patientQue.setPatient(patient);
		patientQue.setLocationTo(location);
		patientQue.setProvider(provider);
		patientQue.setStatus("pending");
		patientQue.setCreator(uiSessionContext.getCurrentUser());
		patientQue.setDateCreated(new Date());
		((UgandaEMRPOCService) Context.getService(UgandaEMRPOCService.class)).savePatientQue(patientQue);
		
		if (Context.getVisitService().getActiveVisitsByPatient(patient).size() <= 0) {
			QuickVisitFragmentController quickVisitFragmentController = new QuickVisitFragmentController();
			quickVisitFragmentController.create((AdtService) Context.getService(AdtService.class),
			    Context.getVisitService(), patient, location, uiUtils, getFacilityVisitType(), uiSessionContext, request);
		}
		try {
			return "redirect:"
			        + ui.pageLink("coreapps", "clinicianfacing/patient",
			            SimpleObject.create(new Object[] { "patientId", patient.getId(), "returnUrl", returnUrl }));
		}
		catch (APIException e) {
			log.error(e.getMessage(), e);
		}
		return "redirect:" + ui.pageLink("ugandaemrpoc", "addPatientToQue", SimpleObject.create(patient.getPatientId()));
	}
	
	private VisitType getFacilityVisitType() {
		String visitTypeUUID = Context.getAdministrationService().getGlobalProperty(
		    "ugandaemrpoc.defaultFacilityVisitTypeUUID");
		return Context.getVisitService().getVisitTypeByUuid(visitTypeUUID);
	}
}