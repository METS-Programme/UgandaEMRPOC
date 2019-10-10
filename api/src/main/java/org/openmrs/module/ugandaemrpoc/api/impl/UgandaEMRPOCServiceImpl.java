package org.openmrs.module.ugandaemrpoc.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.ugandaemrpoc.api.UgandaEMRPOCService;
import org.openmrs.module.ugandaemrpoc.utils.DateFormatUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.openmrs.module.ugandaemrpoc.UgandaEMRPOCConfig.*;

public class UgandaEMRPOCServiceImpl extends BaseOpenmrsService implements UgandaEMRPOCService {

    protected final Log log = LogFactory.getLog(UgandaEMRPOCServiceImpl.class);

	@Override
    public List<PatientQueueMapper> mapPatientQueueToMapper(List<PatientQueue> patientQueueList) {
        List<PatientQueueMapper> patientQueueMappers = new ArrayList<>();

        for (PatientQueue patientQueue : patientQueueList) {
            String names = patientQueue.getPatient().getFamilyName() + " " + patientQueue.getPatient().getGivenName() + " " + patientQueue.getPatient().getMiddleName();
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


    public boolean patientQueueExists(Encounter encounter, Location locationTo, Location locationFrom,String status) throws ParseException {
        List list = Context.getAdministrationService().executeSQL("select patient_queue_id from patient_queue where encounter_id=" + encounter.getEncounterId() + " AND status='" + status + "' AND location_to=" + locationTo.getLocationId() + " AND location_from=" + locationFrom.getLocationId() + " AND date_created BETWEEN \"" + DateFormatUtil.dateFormtterString(encounter.getEncounterDatetime(), DAY_START_TIME) + "\" AND \"" + DateFormatUtil.dateFormtterString(encounter.getEncounterDatetime(), DAY_END_TIME) + "\"", true);
        boolean orderExists = false;
        if (!list.isEmpty()) {
            orderExists = true;
        }
        return orderExists;
    }



    public Provider getProviderFromEncounter(Encounter encounter) {
        EncounterRole encounterRole = Context.getEncounterService().getEncounterRoleByUuid(ENCOUNTER_ROLE);

        Set<Provider> providers = encounter.getProvidersByRole(encounterRole);
        List<Provider> providerList = new ArrayList<>();
        for (Provider provider : providers) {
            providerList.add(provider);
        }

        if (!providerList.isEmpty()) {
            return providerList.get(0);
        } else {
            return null;
        }
    }

    public PatientQueue completePreviousQueue(Patient patient, Location location, String completionStatus, String searchStatus) {
        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
        PatientQueue patientQueue = getPreviousQueue(patient, location, searchStatus);
        patientQueueingService.completeQueue(patientQueue, completionStatus);
        return patientQueue;
    }

    public PatientQueue getPreviousQueue(Patient patient, Location location, String status) {
        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
        PatientQueue previousQueue = new PatientQueue();
        List<PatientQueue> patientQueueList = patientQueueingService.getPatientInQueueList(null, null, null, location, patient, status);
        if (!patientQueueList.isEmpty()) {
            previousQueue = patientQueueList.get(0);
        }
        return previousQueue;
    }
}
