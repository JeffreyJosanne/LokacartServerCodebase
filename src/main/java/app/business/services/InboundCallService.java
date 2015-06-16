package app.business.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.data.repositories.InboundCallRepository;
import app.entities.Group;
import app.entities.InboundCall;
import app.entities.Organization;

@Service
public class InboundCallService {
	@Autowired
	InboundCallRepository inboundCallRepository;
	
	
	public List<InboundCall> getInboundCallList(Organization organization){
		return inboundCallRepository.findByOrganization(organization);
	}
	
	public List<InboundCall> getInboundCallList(Group group){
		//************************************************
		return null;
	}
	
	public void addInboundCall(InboundCall inboundCall){
		inboundCallRepository.save(inboundCall);
	}
	
	public void removeInboundCall(InboundCall inboundCall){
		inboundCallRepository.delete(inboundCall);
	}
	
}