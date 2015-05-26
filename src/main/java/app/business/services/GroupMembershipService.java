package app.business.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import app.data.repositories.GroupMembershipRepository;
import app.entities.Group;
import app.entities.GroupMembership;
import app.entities.Organization;
import app.entities.User;


public class GroupMembershipService {
	@Autowired
	GroupMembershipRepository groupMembershipRepository;
		
	public GroupMembership isUserGroupMembership(User user, Group group){
		return groupMembershipRepository.findByUserAndGroup(user,group);
	}
	
	public List<GroupMembership> getGroupMembershipListByUser(User user){
		return groupMembershipRepository.findByUser(user);
	}
	
	public List<GroupMembership> getGroupMembershipListByUserSortedByGroupName(User user){
		return groupMembershipRepository.findByUserOrderByGroup_NameAsc(user);
	}
	
	public List<GroupMembership> getGroupMembershipListByGroup(Group group){
		return groupMembershipRepository.findByGroup(group);
	}
	
	public List<GroupMembership> getGroupMembershipListByGroupSortedByUserName(Group group){
		return groupMembershipRepository.findByGroupOrderByUser_NameAsc(group);
	}
	
	public void addGroupMembership(GroupMembership groupMembership){
		groupMembershipRepository.save(groupMembership);
	}
	
	public void removeGroupMembership(GroupMembership groupMembership){
		groupMembershipRepository.delete(groupMembership);
	}
	
	public GroupMembership getGroupMembershipById(int groupMembershipId){
		return groupMembershipRepository.findOne(groupMembershipId);
	}
	
	public List<GroupMembership> getAllGroupMembershipList(){
		return groupMembershipRepository.findAll();
	}
	
	public List<GroupMembership> getAllGroupMembershipListSortedByUserName(){
		return groupMembershipRepository.findAllByOrderByUser_NameAsc();
	}
	
	public List<GroupMembership> getAllGroupMembershipListSortedByGroupName(){
		return groupMembershipRepository.findAllByOrderByGroup_NameAsc();
	}
	
	public List<GroupMembership> getGroupsByUserAndOrganization(User user,Organization organization){
		return groupMembershipRepository.findByUserAndGroup_Organization(user,organization);
	}
	
	public List<GroupMembership> getGroupsByUserAndOrganizationSorted(User user,Organization organization){
		return groupMembershipRepository.findByUserAndGroup_OrganizationOrderByGroup_NameAsc(user,organization);
	}

}
