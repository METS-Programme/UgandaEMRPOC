package org.openmrs.module.ugandaemrpoc.api;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;

import java.util.List;

public abstract interface UgandaEMRPOCService extends OpenmrsService {

	/**
	 * @param patientQueueList
	 * @return
	 */
	public List<PatientQueueMapper> mapPatientQueueToMapper(List<PatientQueue> patientQueueList);


	/**
	 *
	 * @param patientQueue
	 * @return
	 */
	public PatientQueueMapper singlePatientQueueToMapper(PatientQueue patientQueue);

	/**
	 * Complete Previous Queue of Patient
	 *
	 * @param patient
	 * @return
	 */
	public PatientQueue completePreviousQueue(Patient patient, Location location, String status);

}
