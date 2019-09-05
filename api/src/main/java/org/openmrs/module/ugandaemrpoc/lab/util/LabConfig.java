package org.openmrs.module.ugandaemrpoc.lab.util;

public class LabConfig {
	
	public static final String PROCESSED_OEDER_WITH_RESULT_QUERY = "select orders.order_id from orders  inner join test_order on (test_order.order_id=orders.order_id) inner join obs on (orders.order_id=obs.order_id) where accession_number!=\"\" and specimen_source!=\"\" AND orders.date_created BETWEEN \"%s\" AND \"%s\"";
	
	public static final String PROCESSED_OEDER_WITHOUT_RESULT_QUERY = "select orders.order_id from orders  inner join test_order on (test_order.order_id=orders.order_id) where accession_number!=\"\" and specimen_source!=\"\" AND orders.date_created BETWEEN \"%s\" AND \"%s\"";
}
