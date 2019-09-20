package org.openmrs.module.ugandaemrpoc.lab.util;

public class LabConfig {

	public static final String PROCESSED_ORDER_WITH_RESULT_QUERY = "select orders.order_id from orders  inner join test_order on (test_order.order_id=orders.order_id) inner join obs on (orders.order_id=obs.order_id) where orders.accession_number!=\"\" and specimen_source!=\"\" AND orders.date_created BETWEEN \"%s\" AND \"%s\"";

	public static final String PROCESSED_ORDER_WITHOUT_RESULT_QUERY = "select orders.order_id from orders  inner join test_order on (test_order.order_id=orders.order_id) where accession_number!=\"\" and specimen_source!=\"\" AND orders.date_created BETWEEN \"%s\" AND \"%s\"";

	public static final String LAB_LOCATION_UUID = "ba158c33-dc43-4306-9a4a-b4075751d36c";

	public static final String QUEUE_STATUS_PENDING = "pending";

	public static final String QUEUE_STATUS_ACTIVE = "active";

	public static final String QUEUE_STATUS_HAS_RESULTS = "has results";

	public static final String ENCOUNTER_ROLE = "240b26f9-dd88-4172-823d-4a8bfeb7841f";

	public static final String LAB_SET_CLASS = "LabSet";

	public static final String TEST_SET_CLASS = "LabSet";

	public static final String CARE_SETTING_OPD = "Outpatient";
}
