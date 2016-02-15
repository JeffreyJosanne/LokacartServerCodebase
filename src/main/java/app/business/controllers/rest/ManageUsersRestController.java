package app.business.controllers.rest;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import app.business.services.GroupMembershipService;
import app.business.services.GroupService;
import app.business.services.OrganizationMembershipService;
import app.business.services.OrganizationService;
import app.business.services.UserPhoneNumberService;
import app.business.services.UserService;
import app.business.services.UserViewService;
import app.entities.Group;
import app.entities.Organization;
import app.entities.OrganizationMembership;
import app.entities.User;
import app.entities.UserPhoneNumber;
import app.util.SendMail;
import app.util.UserManage;

@RestController
@RequestMapping("/api/{org}/manageUsers")
public class ManageUsersRestController {

	@Autowired
	UserViewService userViewService;

	@Autowired
	UserService userService;
	
	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	OrganizationService organizationService;

	@Autowired
	OrganizationMembershipService organizationMembershipService;

	@Autowired
	GroupService groupService;

	@Autowired
	GroupMembershipService groupMembershipService;

	@Autowired
	UserPhoneNumberService userPhoneNumberService;

	
	

	@RequestMapping(value = "/userList", method = RequestMethod.GET, produces = "application/json")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	public String getUserListJson(@PathVariable String org) {
		//List<UserManage> userrows = new ArrayList<UserManage>();
		JSONObject jsonResponseObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		Organization organization = organizationService.getOrganizationByAbbreviation(org);

		List<OrganizationMembership> membershipList = organizationMembershipService.getOrganizationMembershipListByStatus(organization, 1);

		for(OrganizationMembership membership : membershipList)
		{

			User user = membership.getUser();

			try
			{
				// Get required attributes for each user
				int manageUserID = user.getUserId();
				String name = user.getName();
				String email = user.getEmail();
				String phone = userPhoneNumberService.getUserPrimaryPhoneNumber(user).getPhoneNumber();
				String role  = userService.getUserRole(user, organization);
				String address = user.getAddress();
				Timestamp time= user.getTime();
				JSONObject object = new JSONObject();
				try {
				object.put("userID", manageUserID);
				object.put("name", name);
				object.put("email", email);
				object.put("phone", phone);
				object.put("role", role);
				object.put("address", address);
				object.put("time", time);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				// Create the UserManage Object and add it to the list
			//	UserManage userrow = new UserManage(manageUserID, name, email, phone, role, address, time);
				jsonArray.put(object);
			}
			catch(NullPointerException e)
			{
				System.out.println("User name not having his phone number is: " + user.getName() + " having userID: " + user.getUserId());
			}
		}
		try {
			jsonResponseObject.put("users", jsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponseObject.toString();		
	}
	
	
	@RequestMapping(value="/userApprovalList", method=RequestMethod.GET, produces = "application/json")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	public String getUserApprovalListJson(@PathVariable String org) {
		JSONObject jsonResponseObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		List<OrganizationMembership> membershipList = organizationMembershipService.getOrganizationMembershipListByStatus(organization, 0);
		for(OrganizationMembership membership : membershipList)
		{

			User user = membership.getUser();
			
			try
			{
				// Get required attributes for each user
				int manageUserID = user.getUserId();
				String name = user.getName();
				String email = user.getEmail();
				String phone = userPhoneNumberService.getUserPrimaryPhoneNumber(user).getPhoneNumber();
				String role  = userService.getUserRole(user, organization);
				String address = user.getAddress();
				Timestamp time = user.getTime();
				// Create the UserManage Object and add it to the list
				JSONObject object = new JSONObject();
				try {
					object.put("userID", manageUserID);
					object.put("name", name);
					object.put("email", email);
					object.put("phone", phone);
					object.put("role", role);
					object.put("address", address);
					object.put("time", time);
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				jsonArray.put(object);
			}
			catch(NullPointerException e)
			{
				System.out.println("User name not having his phone number is: " + user.getName() + " having userID: " + user.getUserId());
			}
		}
		try {
			jsonResponseObject.put("users",jsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponseObject.toString();
	}
	
	
	
	
	
	// Method to the get all the User list in the 'Manage Users' tab
	@RequestMapping(value="/getUserList", method=RequestMethod.GET, produces = "application/json")

	public List<UserManage> getUserList(@PathVariable String org) {

		List<UserManage> userrows = new ArrayList<UserManage>();

		Organization organization = organizationService.getOrganizationByAbbreviation(org);

		List<OrganizationMembership> membershipList = organizationMembershipService.getOrganizationMembershipListByStatus(organization, 1);

		for(OrganizationMembership membership : membershipList)
		{

			User user = membership.getUser();

			try
			{
				// Get required attributes for each user
				int manageUserID = user.getUserId();
				String name = user.getName();
				String email = user.getEmail();
				String phone = userPhoneNumberService.getUserPrimaryPhoneNumber(user).getPhoneNumber();
				String role  = userService.getUserRole(user, organization);
				String address = user.getAddress();
				Timestamp time= user.getTime();

				// Create the UserManage Object and add it to the list
				UserManage userrow = new UserManage(manageUserID, name, email, phone, role, address, time);
				userrows.add(userrow);
			}
			catch(NullPointerException e)
			{
				System.out.println("User name not having his phone number is: " + user.getName() + " having userID: " + user.getUserId());
			}
		}
		return userrows;
	}
	@RequestMapping(value="/getUserApprovalList", method=RequestMethod.GET, produces = "application/json")

	public List<UserManage> getUserApprovalList(@PathVariable String org) {
		List<UserManage> userrows = new ArrayList<UserManage>();
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		List<OrganizationMembership> membershipList = organizationMembershipService.getOrganizationMembershipListByStatus(organization, 0);
		for(OrganizationMembership membership : membershipList)
		{

			User user = membership.getUser();
			
			try
			{
				// Get required attributes for each user
				int manageUserID = user.getUserId();
				String name = user.getName();
				String email = user.getEmail();
				String phone = userPhoneNumberService.getUserPrimaryPhoneNumber(user).getPhoneNumber();
				String role  = userService.getUserRole(user, organization);
				String address = user.getAddress();
				Timestamp time = user.getTime();
				// Create the UserManage Object and add it to the list
				UserManage userrow = new UserManage(manageUserID, name, email, phone, role, address, time);
				userrows.add(userrow);
			}
			catch(NullPointerException e)
			{
				System.out.println("User name not having his phone number is: " + user.getName() + " having userID: " + user.getUserId());
			}
		}
		return userrows;
	}
	
	
	@RequestMapping(value="/editUser", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public String editUser(@PathVariable String org, @RequestBody String requestBody) {

		
		JSONObject object = null;
		JSONObject responseJsonObject = new JSONObject();
		// Get the input parameters from AngularJS
		String name = null, email = null, address = null, role =null, pincode = null, phonenumber =null;
		boolean isAdmin = false, isPublisher = false;
		int userId=0, isPubInt =0, isAdminInt =0;
		try{
		object = new JSONObject(requestBody);
		userId = Integer.parseInt(object.getString("userid"));
		name = object.getString("name");
		email = object.getString("email");
		address = object.getString("address");
		pincode  = object.getString("pincode");
		phonenumber = object.getString("phone");
		//role = object.getString("role");
		isPubInt = Integer.parseInt(object.getString("isPublisher"));
		if (isPubInt == 1) {
			isPublisher = true;
		}
	
		isAdminInt = Integer.parseInt(object.getString("isAdmin"));
		if (isAdminInt == 1) {
			isAdmin = true;
		}
		
		
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		// Add the new User to database
		User user = null;
		OrganizationMembership membership = null;
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		UserPhoneNumber userPhoneNumber = null;
		try{
		user = userService.getUser(userId);
		if (user == null)
		{
			responseJsonObject.put("response", "User not found");
			return responseJsonObject.toString();
		}
		UserPhoneNumber tempUserPhoneNumber = userPhoneNumberService.getUserPhoneNumber(phonenumber);
		if (tempUserPhoneNumber != null) {
			if (tempUserPhoneNumber.getUser() != user){
				responseJsonObject.put("response", "Phone number exists");
				return responseJsonObject.toString();
			}
		}
		User tempUser = userService.getUserFromEmail(email);
		if (user != tempUser) {
			responseJsonObject.put("response", "Email ID exists");
			return responseJsonObject.toString();
		}
		
	/*	Iterator <UserPhoneNumber>iterator = list.iterator();
		while(iterator.hasNext()){
			if(iterator.next().getPhoneNumber().equals(phonenumber)) {
				
				
				
			}
		}*/
		membership = organizationMembershipService.getUserOrganizationMembership(user, organization);
		userPhoneNumber = userPhoneNumberService.getUserPrimaryPhoneNumber(user);
		}
		catch(Exception e) {
			e.printStackTrace();
			try {
				responseJsonObject.put("response", "User not found");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return responseJsonObject.toString();
		}
		
		// Update the attributes of the user
		user.setName(name);
		user.setEmail(email);
		user.setAddress(address);
		user.setPincode(pincode);
		membership.setIsAdmin(isAdmin);
		membership.setIsPublisher(isPublisher);
		organizationMembershipService.addOrganizationMembership(membership);
		userService.addUser(user);
		try {
			responseJsonObject.put("response","success");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return responseJsonObject.toString();
	}
	
	@RequestMapping(value="/addUser", method = RequestMethod.POST, produces = "application/json")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public String addUser(@PathVariable String org, @RequestBody String requestBody) {

		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		JSONObject object = null;
		JSONObject responseJsonObject = new JSONObject();
		// Get the input parameters from AngularJS
		int isPubInt =0, isAdminInt=0;
		boolean isPublisher=false, isAdmin= false;
		String name = null, email = null, phone = null, role = null, address = null, fname = null,pincode = null;
		try{
			
		object = new JSONObject(requestBody);
		name = object.getString("name");
		email = object.getString("email");
		phone = object.getString("phone");
		//role  = object.getString("role");
		address = object.getString("address");
		pincode = object.getString("pincode");
		
		isPubInt = Integer.parseInt(object.getString("isPublisher"));
		if (isPubInt == 1) {
			isPublisher = true;
		}
	
		isAdminInt = Integer.parseInt(object.getString("isAdmin"));
		if (isAdminInt == 1) {
			isAdmin = true;
		}
		fname=name;
		if(name.contains(" "))
		{
			int i=name.indexOf(" ");
			fname=name.substring(0, i);
		}
		
		}
		catch (Exception e){
			e.printStackTrace();
			try {
				responseJsonObject.put("response","Failed");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return responseJsonObject.toString();
		}
		Random randomint = new Random();
		String password= fname+randomint.nextInt(1000);

		// Variables to store the boolean values of the roles
/*		boolean isAdmin = false;
		boolean isPublisher = false;*/

		// Find if the number is already present in the database
		// If present report it to the frontend
		if(!userPhoneNumberService.findPreExistingPhoneNumber(phone))
		{
			try {
				responseJsonObject.put("response", "Phone Number already exists");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return responseJsonObject.toString();
		}

		// Add the new User to database
		User tempUser = userService.getUserFromEmail(email);
		if (tempUser != null) {
			try {
				responseJsonObject.put("response", "Email already exists");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return responseJsonObject.toString();
		}
 		User user = new User(name, address, "en", "en", email, pincode);
		java.util.Date date= new java.util.Date();
		Timestamp currentTimestamp= new Timestamp(date.getTime());
		user.setTime(currentTimestamp);
		user.setTextbroadcastlimit(0);
		user.setVoicebroadcastlimit(0);
		user.setSha256Password(passwordEncoder.encode(password));
		if(isAdmin)
		{
			user.setTextbroadcastlimit(-1);
			user.setVoicebroadcastlimit(-1);
		}
		else if(isPublisher)
		{
			user.setTextbroadcastlimit(-1);
			user.setVoicebroadcastlimit(-1);
			
		}
		else
		{
			isAdmin=false;
			isPublisher=false;;
		}
		try
		{
		userService.addUser(user);
		}
		catch(Exception e) {
			try {
				responseJsonObject.put("response", "Email ID already exists");
			} catch (JSONException e1) {
				e.printStackTrace();
			}
			return responseJsonObject.toString();
		}
		System.out.println("user timestamp is: "+user.getTime());

		UserPhoneNumber primaryPhoneNumber = new UserPhoneNumber(user, phone, true);
		userPhoneNumberService.addUserPhoneNumber(primaryPhoneNumber);

		// Add the Organization Membership for the user in the Database
		
		OrganizationMembership membership = new OrganizationMembership(organization, user, isAdmin, isPublisher, 1);
		organizationMembershipService.addOrganizationMembership(membership);
		// By Default Add the new user to parent group
		groupMembershipService.addParentGroupMembership(organization, user);

		// Create the UserManage Object
		int manageUserID = user.getUserId();

		//UserManage userrow = new UserManage(manageUserID, name, email, phone, role, address, currentTimestamp);
		System.out.println("password is: "+password);
		SendMail.sendMail(email, "Cottage Industry App: User credentials", "Dear User,\nThe admin of "+organization.getName()+" has added you as a trusted member in the organization.\nNow you can place your order by logging in to our lokacart app using the credentials given below-\nUsername : "+email+"\nPassword : "+password+"\n\nIf you wish to change your password, you can simply click on forget your password button on the app login screen and follow the instructions.\n\nThankyou,\nBest Regards,\nLokacart Team");
	
		// Finally return it as a JSON response body
		try {
			responseJsonObject.put("response","success");
			responseJsonObject.put("userId",user.getUserId());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return responseJsonObject.toString();
	}
	
	@RequestMapping(value="/addRole", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public String addRole(@PathVariable String org, @RequestBody String requestBody) {

		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		JSONObject object = null;
		JSONObject responseJsonObject = new JSONObject();
		OrganizationMembership membership = null;
		int userId=0;
		User user =null;
		String addRole = null;
		try {
		object = new JSONObject(requestBody);
		userId = Integer.parseInt(object.getString("userid"));
		addRole = object.getString("addRole");
		user = userService.getUser(userId);
		membership = organizationMembershipService.getUserOrganizationMembership(user, organization);
		}
		
		catch(Exception e){
			e.printStackTrace();
			try{
			responseJsonObject.put("response","Member not found");
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
			}
			return responseJsonObject.toString();
		}
		if(addRole.equals("Admin"))
		{
			membership.setIsAdmin(true);
			user.setTextbroadcastlimit(-1);
			user.setVoicebroadcastlimit(-1);
			userService.addUser(user);
		}
		else if(addRole.equals("Publisher"))
		{
			membership.setIsPublisher(true);
			user.setTextbroadcastlimit(-1);
			user.setVoicebroadcastlimit(-1);
			userService.addUser(user);
		}
		else if(addRole.equals("Member"))
		{
			membership.setIsAdmin(false);
			membership.setIsPublisher(false);
		}

		// Finally make changes in the database
		try{
		organizationMembershipService.addOrganizationMembership(membership);
		}
		catch(Exception e){
			e.printStackTrace();
			try {
				responseJsonObject.put("response", "failed");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return responseJsonObject.toString();
		}
		try {
			responseJsonObject.put("response", "Success");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return responseJsonObject.toString();
		}
	
	
	@RequestMapping(value="/removeRole", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public String removeUserRole(@PathVariable String org, @RequestBody String requestBody) {

		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		//int manageUserId = Integer.parseInt(userDetails.get("userid"));
		//String removeRole = userDetails.get("removeRole");
		JSONObject object = null;
		JSONObject responseJsonObject = new JSONObject();
		User user = null;
		int userId=0;
		String removeRole = null;
		OrganizationMembership membership=null;
		try{
		object = new JSONObject(requestBody);
		userId = Integer.parseInt(object.getString("userid"));
		removeRole = object.getString("removeRole");
		user = userService.getUser(userId);
		membership = organizationMembershipService.getUserOrganizationMembership(user, organization);
		}
		catch(Exception e) {
			e.printStackTrace();
			try {
				responseJsonObject.put("response", "failure");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			
		}
		
		if(removeRole.equals("Admin"))
		{
			membership.setIsAdmin(false);
			user.setTextbroadcastlimit(0);
			user.setVoicebroadcastlimit(0);
			userService.addUser(user);
		}
		else if(removeRole.equals("Publisher"))
		{
			membership.setIsPublisher(false);
			user.setTextbroadcastlimit(0);
			user.setVoicebroadcastlimit(0);
			userService.addUser(user);
		}

		// Finally make changes in the database
		organizationMembershipService.addOrganizationMembership(membership);
		try {
			responseJsonObject.put("response", "Success");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return responseJsonObject.toString();
	}
	

	
	@RequestMapping(value="/countUserList", method=RequestMethod.GET, produces = "application/json")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	public List<Integer> countUserList(@PathVariable String org) {

		List<Integer> count = new ArrayList<Integer>();
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		List<OrganizationMembership> membershipListpending = organizationMembershipService.getOrganizationMembershipListByStatus(organization, 0);
		List<OrganizationMembership> membershipListapproved = organizationMembershipService.getOrganizationMembershipListByStatus(organization, 1);
		int totUsers=membershipListpending.size()+membershipListapproved.size();
		int todayUsers=0;
		int pendingUsers=membershipListpending.size();
		for(OrganizationMembership membership : membershipListpending)
		{

			User user = membership.getUser();
		
			try
			{
				// Get required attributes for each user
				int manageUserID = user.getUserId();
				String name = user.getName();
				String email = user.getEmail();
				String phone = userPhoneNumberService.getUserPrimaryPhoneNumber(user).getPhoneNumber();
				String role  = userService.getUserRole(user, organization);
				String address = user.getAddress();
				Timestamp time = user.getTime();
				
				// Create the UserManage Object and add it to the list
				UserManage userrow = new UserManage(manageUserID, name, email, phone, role, address, time);
				Calendar cal= Calendar.getInstance();
				cal.clear(Calendar.HOUR_OF_DAY);
				cal.clear(Calendar.AM_PM);
				cal.clear(Calendar.MINUTE);
				cal.clear(Calendar.SECOND);
				cal.clear(Calendar.MILLISECOND);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");      
			    java.util.Date dateWithoutTime = sdf.parse(sdf.format(new java.util.Date()));
				if(time.after(dateWithoutTime))
				{
					todayUsers=todayUsers+1;
				}
			}
			catch(NullPointerException | ParseException e)
			{
				System.out.println("User name not having his phone number is: " + user.getName() + " having userID: " + user.getUserId());
			}
		}
		count.add(totUsers);
		count.add(pendingUsers);
		count.add(todayUsers);
		return count;
	}
	
	// Method to add a new user according to the details entered in the Modal Dialog Box
	@RequestMapping(value="/addNewUser", method = RequestMethod.POST, produces = "application/json")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public UserManage addNewUser(@PathVariable String org, @RequestBody Map<String,String> newUserDetails) {

		Organization organization = organizationService.getOrganizationByAbbreviation(org);

		// Get the input parameters from AngularJS
		String name = newUserDetails.get("name");
		String email = newUserDetails.get("email");
		String phone = newUserDetails.get("phone");
		String role  = newUserDetails.get("role");
		String address = newUserDetails.get("address");
		String fname=name;
		if(name.contains(" "))
		{
			int i=name.indexOf(" ");
			fname=name.substring(0, i);
		}
		Random randomint = new Random();
		String password= fname+randomint.nextInt(1000);

		// Variables to store the boolean values of the roles
		boolean isAdmin = false;
		boolean isPublisher = false;

		// Find if the number is already present in the database
		// If present report it to the frontend
		if(!userPhoneNumberService.findPreExistingPhoneNumber(phone))
		{
			return null;
		}

		// Add the new User to database
		User user = new User(name, address, "en", "en", email);
		java.util.Date date= new java.util.Date();
		Timestamp currentTimestamp= new Timestamp(date.getTime());
		user.setTime(currentTimestamp);
		user.setTextbroadcastlimit(0);
		user.setVoicebroadcastlimit(0);
		user.setSha256Password(passwordEncoder.encode(password));
		userService.addUser(user);
		System.out.println("user timestamp is: "+user.getTime());

		UserPhoneNumber primaryPhoneNumber = new UserPhoneNumber(user, phone, true);
		userPhoneNumberService.addUserPhoneNumber(primaryPhoneNumber);

		// Add the Organization Membership for the user in the Database
		OrganizationMembership membership = new OrganizationMembership(organization, user, isAdmin, isPublisher, 1);
		organizationMembershipService.addOrganizationMembership(membership);

		// By Default Add the new user to parent group
		groupMembershipService.addParentGroupMembership(organization, user);

		// Create the UserManage Object
		int manageUserID = user.getUserId();

		UserManage userrow = new UserManage(manageUserID, name, email, phone, role, address, currentTimestamp);
		System.out.println("password is: "+password);
		SendMail.sendMail(email, "Cottage Industry App: User credentials", "Dear User,\nThe admin of "+organization.getName()+" has added you as a trusted member in the organization.\nNow you can place your order by logging in to our lokacart app using the credentials given below-\nUsername : "+email+"\nPassword : "+password+"\n\nIf you wish to change your password, you can simply click on forget your password button on the app login screen and follow the instructions.\n\nThankyou,\nBest Regards,\nLokacart Team");
		
		// Finally return it as a JSON response body
		return userrow;
	}

	// It is assumed that the user does not have the role of admin or publisher
	@RequestMapping(value="/addUserRole", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public void addRole(@PathVariable String org, @RequestBody Map<String,String> userDetails) {

		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		int manageUserId = Integer.parseInt(userDetails.get("userid"));
		String addRole = userDetails.get("addRole");

		User user = userService.getUser(manageUserId);

		OrganizationMembership membership = organizationMembershipService.getUserOrganizationMembership(user, organization);

		if(addRole.equals("Admin"))
		{
			membership.setIsAdmin(true);
			user.setTextbroadcastlimit(-1);
			user.setVoicebroadcastlimit(-1);
			userService.addUser(user);
		}
		else if(addRole.equals("Publisher"))
		{
			membership.setIsPublisher(true);
			user.setTextbroadcastlimit(-1);
			user.setVoicebroadcastlimit(-1);
			userService.addUser(user);
		}
		else if(addRole.equals("Member"))
		{
			membership.setIsAdmin(false);
			membership.setIsPublisher(false);
		}

		// Finally make changes in the database
		organizationMembershipService.addOrganizationMembership(membership);
	}

	@RequestMapping(value="/removeUserRole", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public void removeRole(@PathVariable String org, @RequestBody Map<String,String> userDetails) {

		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		int manageUserId = Integer.parseInt(userDetails.get("userid"));
		String removeRole = userDetails.get("removeRole");

		User user = userService.getUser(manageUserId);
		OrganizationMembership membership = organizationMembershipService.getUserOrganizationMembership(user, organization);

		if(removeRole.equals("Admin"))
		{
			membership.setIsAdmin(false);
			user.setTextbroadcastlimit(0);
			user.setVoicebroadcastlimit(0);
			userService.addUser(user);
		}
		else if(removeRole.equals("Publisher"))
		{
			membership.setIsPublisher(false);
			user.setTextbroadcastlimit(0);
			user.setVoicebroadcastlimit(0);
			userService.addUser(user);
		}

		// Finally make changes in the database
		organizationMembershipService.addOrganizationMembership(membership);
	}

	// Method to add a new user according to the details entered in the Modal Dialog Box
	@RequestMapping(value="/editUserWithPhoneNumber", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public String editUserWithPhoneNumber(@PathVariable String org, @RequestBody Map<String,String> currentUserDetails) {

		// Get the input parameters from AngularJS
		int manageUserId = Integer.parseInt(currentUserDetails.get("userid"));
		String name = currentUserDetails.get("name");
		String email = currentUserDetails.get("email");
		String phone = currentUserDetails.get("phone");
		String address = currentUserDetails.get("address");
		
		// Find if the number is already present in the database 
		// If present report it to the Frontend
		if(!userPhoneNumberService.findPreExistingPhoneNumber(phone))
		{
			return "-1";
		}
		
		// Add the new User to database
		User user = userService.getUser(manageUserId);

		// Update the attributes of the user
		user.setName(name);
		user.setEmail(email);
		user.setAddress(address);
		userService.addUser(user);
		
		// Update the primary phone number of the user
		UserPhoneNumber userPrimaryPhoneNumber = userPhoneNumberService.getUserPrimaryPhoneNumber(user);
		userPrimaryPhoneNumber.setPhoneNumber(phone);
		userPhoneNumberService.addUserPhoneNumber(userPrimaryPhoneNumber);
		
		return phone;
	}
	
	// Method to edit a exisitng user only if his phone number is not altered
	@RequestMapping(value="/editUserOnly", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public void editUserOnly(@PathVariable String org, @RequestBody Map<String,String> currentUserDetails) {

		// Get the input parameters from AngularJS
		int manageUserId = Integer.parseInt(currentUserDetails.get("userid"));
		String name = currentUserDetails.get("name");
		String email = currentUserDetails.get("email");
		String address = currentUserDetails.get("address");
		
		// Add the new User to database
		User user = userService.getUser(manageUserId);

		// Update the attributes of the user
		user.setName(name);
		user.setEmail(email);
		user.setAddress(address);
		userService.addUser(user);
	}

	// Method to get user details in a Modal Dialog Box
	@RequestMapping(value="/getUserDetails", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public Map<String, Vector<String> > getUserDetails(@PathVariable String org, @RequestBody int manageUserId) {

		Organization organization = organizationService.getOrganizationByAbbreviation(org);

		Vector<String> userGroupNames = new Vector<String>(10,2);
		Vector<String> userPhoneNumbers = new Vector<String>(10,2);

		// Add the new User to database
		User user = userService.getUser(manageUserId);

		List<Group> userGroups = groupMembershipService.getGroupListByUserAndOrganization(user, organization);

		for(Group userGroup : userGroups)
		{
			userGroupNames.add(userGroup.getName());
		}

		// Add the Primary Phone number for the user in the database
		UserPhoneNumber primaryPhoneNumber = userPhoneNumberService.getUserPrimaryPhoneNumber(user);
		userPhoneNumbers.add(primaryPhoneNumber.getPhoneNumber() + " (Primary)" );

		List<UserPhoneNumber> userSecondaryPhoneNumbers = userPhoneNumberService.getUserSecondaryPhoneNumbers(user);

		if(userSecondaryPhoneNumbers != null)
		{
			for(UserPhoneNumber userSecondaryPhoneNumber : userSecondaryPhoneNumbers)
			{
				userPhoneNumbers.add(userSecondaryPhoneNumber.getPhoneNumber());
			}
		}

		Map<String, Vector<String> > jsonbody = new HashMap<String, Vector<String> >();
		jsonbody.put("groups", userGroupNames);
		jsonbody.put("phoneNumbers", userPhoneNumbers);

		return jsonbody;
	}

}

