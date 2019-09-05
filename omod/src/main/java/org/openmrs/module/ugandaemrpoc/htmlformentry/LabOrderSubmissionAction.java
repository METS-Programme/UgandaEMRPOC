package org.openmrs.module.ugandaemrpoc.htmlformentry;

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Enrolls patients into DSDM programs
 */
public class LabOrderSubmissionAction implements CustomFormSubmissionAction {
	
	@Override
	public void applyAction(FormEntrySession session) {
		Mode mode = session.getContext().getMode();
		if (!(mode.equals(Mode.ENTER) || mode.equals(Mode.EDIT))) {
			return;
		}
		
		if (mode.equals(Mode.EDIT)) {
			
		}
		
		Set<Order> orders = proccessLabTestOrdersFromObs(session.getEncounter().getObs());
		Encounter encounter = session.getEncounter();
		encounter.setOrders(orders);
		Context.getEncounterService().saveEncounter(encounter);
		if (session.getEncounter().getOrders().size() > 0) {
			sendPatientToLab(session);
		}
	}
	
	public void sendPatientToLab(FormEntrySession session) {
		PatientQueue patientQueue = new PatientQueue();
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		Location location = Context.getLocationService().getLocationByUuid("ba158c33-dc43-4306-9a4a-b4075751d36c");
		Provider provider = getProvider(session.getEncounter());
		patientQueue.setLocationFrom(session.getEncounter().getLocation());
		patientQueue.setPatient(session.getEncounter().getPatient());
		patientQueue.setLocationTo(location);
		patientQueue.setProvider(provider);
		patientQueue.setEncounter(session.getEncounter());
		patientQueue.setStatus("pending");
		patientQueue.setCreator(Context.getUserService().getUsersByPerson(provider.getPerson(), false).get(0));
		patientQueue.setDateCreated(new Date());
		patientQueueingService.savePatientQue(patientQueue);
	}
	
	public Set<Order> proccessLabTestOrdersFromObs(Set<Obs> obsList) {
		Set<Order> orders = new HashSet<Order>();
		CareSetting careSetting = Context.getOrderService().getCareSettingByName("Outpatient");
		for (Obs obs : obsList) {
			if (obs.getValueCoded() != null
			        && (obs.getValueCoded().getConceptClass().getName().equals("LabSet") || obs.getValueCoded()
			                .getConceptClass().getName().equals("Test"))) {
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
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRoleByUuid(
		    "240b26f9-dd88-4172-823d-4a8bfeb7841f");
		
		Set<Provider> providers = encounter.getProvidersByRole(encounterRole);
		for (Provider provider : providers) {
			return provider;
		}
		return null;
	}
}
