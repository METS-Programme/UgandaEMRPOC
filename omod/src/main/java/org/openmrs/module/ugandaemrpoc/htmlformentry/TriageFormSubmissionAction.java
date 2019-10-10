package org.openmrs.module.ugandaemrpoc.htmlformentry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;

import java.util.List;

import static org.openmrs.module.ugandaemrpoc.UgandaEMRPOCConfig.QUEUE_STATUS_COMPLETED;

/**
 * Enrolls patients into DSDM programs
 */
public class TriageFormSubmissionAction implements CustomFormSubmissionAction {
	
	private static final Log log = LogFactory.getLog(TriageFormSubmissionAction.class);
	
	@Override
	public void applyAction(FormEntrySession session) {
		Mode mode = session.getContext().getMode();
		if (!(mode.equals(Mode.ENTER) || mode.equals(Mode.EDIT))) {
			return;
		}
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		PatientQueue patientQueue = new PatientQueue();
		
		List<PatientQueue> patientQueueList = patientQueueingService.getPatientInQueueList(null, null, null, null,
		    session.getPatient(), QUEUE_STATUS_COMPLETED);
		if (patientQueueList.size() > 0) {
			patientQueue = patientQueueList.get(0);
			patientQueue.setEncounter(session.getEncounter());
			patientQueueingService.savePatientQue(patientQueue);
		}
		
	}
}
