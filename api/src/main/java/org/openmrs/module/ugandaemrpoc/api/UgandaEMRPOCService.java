package org.openmrs.module.ugandaemrpoc.api;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;

import java.util.List;

public abstract interface UgandaEMRPOCService extends OpenmrsService {
	
	public List<PatientQueueMapper> mapPatientQueueToMapper(List<PatientQueue> patientQueueList);
}
