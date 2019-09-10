package org.openmrs.module.ugandaemrpoc.htmlformentry;

import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;

import java.util.Set;

/**
 * Enrolls patients into DSDM programs
 */
public class LabOrderSubmissionAction implements CustomFormSubmissionAction {
	
	@Override
	public void applyAction(FormEntrySession session) {
		UgandaEMRPOCService ugandaEMRPOCService = Context.getService(UgandaEMRPOCService.class);
		Mode mode = session.getContext().getMode();
		if (!(mode.equals(Mode.ENTER) || mode.equals(Mode.EDIT))) {
			return;
		}
		
		if (mode.equals(Mode.EDIT)) {
			
		}
		
		Set<Order> orders = ugandaEMRPOCService.proccessLabTestOrdersFromObs(session.getEncounter().getObs());
		Encounter encounter = session.getEncounter();
		encounter.setOrders(orders);
		Context.getEncounterService().saveEncounter(encounter);
		if (session.getEncounter().getOrders().size() > 0) {
			ugandaEMRPOCService.sendPatientToLab(session);
		}
	}
}
