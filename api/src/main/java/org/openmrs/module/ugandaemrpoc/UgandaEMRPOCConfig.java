package org.openmrs.module.ugandaemrpoc;

import org.springframework.stereotype.Component;

@Component("ugandaemrpoc.UgandaEMRPOCConfig")
public class UgandaEMRPOCConfig {
	
	public static final String MODULE_ID = "ugandaemrpoc";
	
	public static final String MODULE_PRIVILEGE = "UgandaEMRPOC Privilege";
	public static final String TRIAGE_LOCATION_UUID = "ff01eaab-561e-40c6-bf24-539206b521ce";
	public static final String PHARMACY_LOCATION_UUID = "3ec8ff90-3ec1-408e-bf8c-22e4553d6e17";
	public static final String LAB_LOCATION_UUID = "ba158c33-dc43-4306-9a4a-b4075751d36c";

	public static final String LAB_SET_CLASS = "LabSet";
	public static final String TEST_SET_CLASS = "LabSet";

	public static final String ORDER_TYPE_LAB_UUID =  "52a447d3-a64a-11e3-9aeb-50e549534c5e";

	public static final String  DAY_START_TIME = "00:00:00";
	public static final String  DAY_END_TIME = "23:59:59";


	public static final String QUEUE_STATUS_PENDING = "pending";
	public static final String QUEUE_STATUS_COMPLETED = "completed";
	public static final String QUEUE_STATUS_ACTIVE = "active";
	public static final String QUEUE_STATUS_SENT_TO_LAB = "sent to lab";
	public static final String QUEUE_STATUS_FROM_LAB = "from lab";
	public static final String QUEUE_STATUS_SENT_TO_PHARMACY = "sent to pharmacy";
	public static final String QUEUE_STATUS_HAS_RESULTS = "has results";

	public static final String PROCESSED_ORDER_WITH_RESULT_QUERY = "select orders.order_id from orders  inner join test_order on (test_order.order_id=orders.order_id) inner join obs on (orders.order_id=obs.order_id) where orders.accession_number!=\"\" and specimen_source!=\"\" AND orders.date_created BETWEEN \"%s\" AND \"%s\"";

	public static final String PROCESSED_ORDER_WITH_RESULT_OF_ENCOUNTER_QUERY = "select orders.order_id from orders  inner join test_order on (test_order.order_id=orders.order_id) inner join obs on (orders.order_id=obs.order_id) where orders.accession_number!=\"\" and specimen_source!=\"\" AND orders.encounter_id=%s";

	public static final String PROCESSED_ORDER_WITHOUT_RESULT_QUERY = "select orders.order_id from orders  inner join test_order on (test_order.order_id=orders.order_id) where accession_number!=\"\" and specimen_source!=\"\" AND orders.date_created BETWEEN \"%s\" AND \"%s\"";

	public static final String ENCOUNTER_ROLE = "240b26f9-dd88-4172-823d-4a8bfeb7841f";

	public static final String CARE_SETTING_OPD = "Outpatient";
	public UgandaEMRPOCConfig() {
	}
}
