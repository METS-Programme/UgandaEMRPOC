package org.openmrs.module.ugandaemrpoc.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;

import java.util.ArrayList;
import java.util.List;

public class UgandaEMRPOCServiceImpl extends BaseOpenmrsService implements UgandaEMRPOCService {
	
	protected final Log log = LogFactory.getLog(UgandaEMRPOCServiceImpl.class);
	
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
			
			if (patientQueue.getEncounter() != null) {
				patientQueueMapper.setEncounterId(patientQueue.getEncounter().getEncounterId().toString());
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
