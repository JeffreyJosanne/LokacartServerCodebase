package app.business.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.data.repositories.UserRepository;
import app.entities.Organization;
import app.entities.OrganizationMembership;
import app.entities.User;
import app.util.Utils;

/*
 * This service will cater all controllers to fetch user related data.
 */

@Service
public class UserService {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	OrganizationMembershipService organizationMembershipService;
	
	/*
	 * Method to get organization memberships of user.
	 * Written currently just to help getAdminMembership method 
	 * to fetch data from current layer itself 
	 */
	public List<OrganizationMembership> getUserMembershipList(User user){
		
		List<OrganizationMembership> userMemberships = user.getOrganizationMemberships();
		return userMemberships;
	}
	
	/*
	 * Method to get list of user memberships where user is an admin.
	 */
	public List<OrganizationMembership> getAdminMembershipList(User user){
		
		List<OrganizationMembership> adminMemberships = new ArrayList<OrganizationMembership>();
		
		for (OrganizationMembership userMembership : this.getUserMembershipList(user)) {
			
			if (userMembership.getIsAdmin()){
				
				adminMemberships.add(userMembership);
			
			}
		}
		return adminMemberships;
	}
	
	
	/*
	 * Service layer function to get details of current user object.
	 */
	public User getCurrentUser(){
		return userRepository.findOne(Utils.getSecurityPrincipal().getUserId());
	}
	
	/*
	 * Decide on the role that user plays.
	 */
	public String getUserRole(User user, Organization organization){
		
		String role = null;
		
		try{
			OrganizationMembership organizationMembership = organizationMembershipService.getUserOrganizationMembership(user,organization);
			
			if(organizationMembership.getIsAdmin()==true && organizationMembership.getIsPublisher()==false){
				role="Admin";
			}
			else if(organizationMembership.getIsPublisher()==true && organizationMembership.getIsAdmin()==false){
				role="Publisher";
			}
			else if(organizationMembership.getIsAdmin()==false && organizationMembership.getIsPublisher()==false){
				role="Member";
			}
			else if(organizationMembership.getIsPublisher()==true && organizationMembership.getIsAdmin()==true){
				role="Admin Publisher";
			}
		}
		catch (NullPointerException e)
		{
			System.out.println("User whose Organization Membership is missing from database is: " + user.getName() + " userID: " + user.getUserId());
			System.out.println("Missing Organization ID is: " + organization.getOrganizationId());
		}
		
		return role;
	}

	/*
	 * add a user to database
	 */
	public User addUser(User user) {
			
		return userRepository.save(user);		
	}
		
	/*
	 * delete a user from database
	 */
	public void removeUser(User user) {
			
		userRepository.delete(user);
		
	}
	
	public User updateCallLocale(User user, String callLocale){
		user.setCallLocale(callLocale);
		return userRepository.save(user);
	}
	
	public User updateWebLocale(User user, String webLocale){
		user.setWebLocale(webLocale);
		return userRepository.save(user);
	}
	
	/*
	 * Get User object by userId
	 */
	public User getUser(int userId) {
		
		return userRepository.findOne(userId);
	}
	
	/*
	 * Get all users from database
	 */
	public List<User> getAllUserList() {
		
		return userRepository.findAll();
	}
	
	public User getUserFromEmail(String email) {
		List <User> users = userRepository.findByEmail(email);
		if(users.isEmpty()) {
		
			return null;
		}
		return users.get(0);
	}
}
