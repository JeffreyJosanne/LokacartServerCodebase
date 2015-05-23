package app.business.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import app.data.repositories.OrganizationRepository;
import app.entities.Group;
import app.entities.Organization;
import app.entities.OrganizationMembership;
import app.entities.User;

@Service
public class OrganizationService {
	@Autowired
	OrganizationRepository organizationRepository;
	public Organization getOrganizationByAbbreviation(String org)
	{
		Organization organization = organizationRepository.findByAbbreviation(org);
		return organization;	
	}
	
	public List<OrganizationMembership> getOrganizationMembershipList(Organization organization) {
		return organization.getOrganizationMemberships();
	}
	
	public int getOrganizationId(Organization organization) {
		return organization.getOrganizationId();
	}
	
	public List<Group> getOrganizationGroupList(Organization organization){
		return (new ArrayList<Group>(organization.getGroups()));
		
	}
	
	public void addOrganization(Organization organization){
		organizationRepository.save(organization);
	}
	
	public void removeOrganization(Organization organization){
		organizationRepository.delete(organization);
	}
	
	public Organization getOrganizationById(int organizationId){
		Organization organization = organizationRepository.findOne(organizationId);
		return organization;
	}
	
	public List<Organization> getAllOrganizationList(){
		return (new ArrayList<Organization> (organizationRepository.findAll()));
	}
	public List<Organization> getAllOrganizationListSortedByName(){
		/*
		 * The query can also be done by the statement public List<Organization> findAllByOrderByNameAsc(); in the repository as well. 
		 */
		List<Organization> organizationSorted = organizationRepository.findAll(sortByName());
		return organizationSorted;
	}
	private Sort sortByName(){
		return new Sort(Sort.Direction.ASC, "name");
	}
	

	

}
