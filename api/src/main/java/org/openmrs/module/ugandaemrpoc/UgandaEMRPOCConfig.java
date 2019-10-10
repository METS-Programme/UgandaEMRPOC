package org.openmrs.module.ugandaemrpoc;

import org.springframework.stereotype.Component;

@Component("ugandaemrpoc.UgandaEMRPOCConfig")
public class UgandaEMRPOCConfig {
	
	public static final String MODULE_ID = "ugandaemrpoc";
	
	public static final String MODULE_PRIVILEGE = "UgandaEMRPOC Privilege";
	
	public static final String TRIAGE_LOCATION_UUID = "ff01eaab-561e-40c6-bf24-539206b521ce";

	public static final String  DAY_START_TIME = "00:00:00";
	public static final String  DAY_END_TIME = "23:59:59";

	public static final String QUEUE_STATUS_PENDING = "pending";
	public static final String QUEUE_STATUS_COMPLETED = "completed";

	public static final String ENCOUNTER_ROLE = "240b26f9-dd88-4172-823d-4a8bfeb7841f";

	public UgandaEMRPOCConfig() {
	}
}
