package org.openmrs.module.ugandaemrpoc.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
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

	public PatientQueueMapper singlePatientQueueToMapper(PatientQueue patientQueue) {
		PatientQueueMapper patientQueueMapper = new PatientQueueMapper();
		if (patientQueue != null) {
			String names = patientQueue.getPatient().getFamilyName() + " " + patientQueue.getPatient().getGivenName() + " "
					+ patientQueue.getPatient().getMiddleName();

			patientQueueMapper.setId(patientQueue.getId());
			patientQueueMapper.setPatientNames(names.replace("null", ""));
			patientQueueMapper.setPatientId(patientQueue.getPatient().getPatientId());
			patientQueueMapper.setLocationFrom(patientQueue.getLocationFrom().getName());
			patientQueueMapper.setLocationTo(patientQueue.getLocationTo().getName());
			if (patientQueue.getProvider() != null) {
				patientQueueMapper.setProviderNames(patientQueue.getProvider().getName());
			}

			if (patientQueue.getCreator() != null) {
				patientQueueMapper.setCreatorNames(patientQueue.getCreator().getPersonName().getFullName());
			}
			patientQueueMapper.setStatus(patientQueue.getStatus());
			patientQueueMapper.setQueueNumber(patientQueue.getQueueNumber());
			patientQueueMapper.setGender(patientQueue.getPatient().getGender());
			patientQueueMapper.setAge(patientQueue.getPatient().getAge().toString());
			patientQueueMapper.setDateCreated(patientQueue.getDateCreated().toString());
		}
		return patientQueueMapper;
	}

	public PatientQueue completePreviousQueue(Patient patient, Location location, String status) {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		PatientQueue patientQueue = getPreviousQueue(patient, location);
		patientQueueingService.completeQueue(patientQueue, status);
		return patientQueue;
	}

	public PatientQueue getPreviousQueue(Patient patient, Location location) {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		PatientQueue previousQueue = new PatientQueue();
		List<PatientQueue> patientQueueList = patientQueueingService.getPatientInQueueList(null, null, null, location,
		    patient, "pending");
		if (patientQueueList.size() > 0) {
			previousQueue = patientQueueList.get(0);
		}
		return previousQueue;
	}
}
