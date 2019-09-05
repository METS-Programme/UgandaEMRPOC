package org.openmrs.module.ugandaemrpoc.lab.mapper;

import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;

import java.io.Serializable;
import java.util.Set;

public class LabQueueMapper extends PatientQueueMapper implements Serializable {
	
	private Integer encounterId;
	
	Set<OrderMapper> orderMapper;
	
	public LabQueueMapper() {
	}
	
	public Integer getEncounterId() {
		return encounterId;
	}
	
	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}
	
	public Set<OrderMapper> getOrderMapper() {
		return orderMapper;
	}
	
	public void setOrderMapper(Set<OrderMapper> orderMapper) {
		this.orderMapper = orderMapper;
	}
}
