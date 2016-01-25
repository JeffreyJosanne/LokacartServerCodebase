package app.business.controllers.rest;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import app.business.services.GcmTokensService;
import app.business.services.OrganizationMembershipService;
import app.business.services.OrganizationService;
import app.business.services.UserPhoneNumberService;
import app.business.services.UserService;
import app.business.services.message.MessageService;
import app.entities.GcmTokens;
import app.entities.Group;
import app.entities.Organization;
import app.entities.OrganizationMembership;
import app.entities.User;
import app.entities.message.Message;
import app.util.GcmRequest;

@RestController
@RequestMapping("/app")
public class MemberManagementRestController {
	
	@Autowired
	OrganizationMembershipService organizationMembershipService;
	
	@Autowired
	OrganizationService organizationService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserPhoneNumberService userPhoneNumberService;
	
	@Autowired
	GcmTokensService gcmTokensService;
	
	@Autowired
	MessageService messageService;
	
	public HashMap<String, Integer> dashBoardLocal(String orgabbr) throws ParseException {

		Organization organization = organizationService.getOrganizationByAbbreviation("Test2");
		Group g= organizationService.getParentGroup(organization);
		List<Message> messageapppro=messageService.getMessageListByOrderStatus(g, "binary", "processed");
		List<Message> messageappnew=messageService.getMessageListByOrderStatus(g, "binary", "saved");
		List<Message> messageappcan=messageService.getMessageListByOrderStatus(g, "binary", "cancelled");
		HashMap<String, Integer> dashmap = new HashMap<String, Integer>();
		dashmap.put("saved", messageappnew.size());
		dashmap.put("processed", messageapppro.size());
		dashmap.put("cancelled", messageappcan.size());
		
		List<OrganizationMembership> membershipListpending = organizationMembershipService.getOrganizationMembershipListByStatus(organization, 0);
		List<OrganizationMembership> membershipListapproved = organizationMembershipService.getOrganizationMembershipListByStatus(organization, 1);
		dashmap.put("totalUsers", membershipListpending.size()+membershipListapproved.size());
		dashmap.put("pendingUsers", membershipListpending.size());
		int todayUsers=0;
		for(OrganizationMembership membership : membershipListpending)
		{

			User user = membership.getUser();
		
			try
			{
				Timestamp time = user.getTime();
				
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
				System.out.println("User name not having his timestamp recorded is: " + user.getName() + " having userID: " + user.getUserId());
			}
		}
		dashmap.put("newUsersToday",todayUsers);
		return dashmap;
	}
	
	public List <String> getTargetDevices (Organization organization)  {
		List<OrganizationMembership> organizationMembership = organizationMembershipService.getOrganizationMembershipListByIsAdmin(organization, true);
		List<String> phoneNumbers = new ArrayList<String>();
		Iterator <OrganizationMembership> membershipIterator = organizationMembership.iterator();
		while (membershipIterator.hasNext()) {
			OrganizationMembership membership = membershipIterator.next();
			User user = membership.getUser();
			phoneNumbers.add(userPhoneNumberService.getUserPrimaryPhoneNumber(user).getPhoneNumber());
		}
		Iterator <String> iterator = phoneNumbers.iterator();
		List <String>androidTargets = new ArrayList<String>();
		while(iterator.hasNext()) {
			String number = iterator.next();
			try{
			List<GcmTokens> gcmTokens = gcmTokensService.getListByPhoneNumber(number);
			Iterator <GcmTokens> iter = gcmTokens.iterator();
			while(iter.hasNext()) {
			
			androidTargets.add(iter.next().getToken());
			}
			}
			catch(Exception e){
				System.out.println("no token for number: "+number);
			}
		}
		return androidTargets;
	}
	
	@RequestMapping(value = "/approve",method = RequestMethod.POST )
	public String approveMember(@RequestBody String requestBody) {
		JSONObject responseJsonObject = new JSONObject();
		String organizationabbr = null;
		Organization organization = null;
		int userId = 0;
		try{
			JSONObject object = new JSONObject(requestBody);
			organizationabbr = object.getString("orgabbr");
			userId = object.getInt("userId");
		}
		catch(JSONException e) {
			e.printStackTrace();
		}
		try {
		organization = organizationService.getOrganizationByAbbreviation(organizationabbr);
		User user = userService.getUser(userId);
		OrganizationMembership organizationMembership = organizationMembershipService.getUserOrganizationMembership(user, organization);
		organizationMembership.setStatus(1);
		organizationMembershipService.addOrganizationMembership(organizationMembership);
		}
		catch (Exception e) {
			try {
				responseJsonObject.put("response", "Failed");
				return responseJsonObject.toString();
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		try {
			responseJsonObject.put("response", "Member Approved");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		List<String > androidTargets = getTargetDevices(organization);
		if(androidTargets.size() > 0) {
			GcmRequest gcmRequest = new GcmRequest();
			HashMap<String, Integer> dashData = null;
			try {
				dashData = dashBoardLocal(organizationabbr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			gcmRequest.broadcast(androidTargets,organizationabbr,dashData);					}
		return responseJsonObject.toString();	
	}
	
	
	@RequestMapping(value = "/approveAll",method = RequestMethod.POST )
	public String approveAllMembers(@RequestBody String requestBody) {
		
		JSONObject responseJsonObject = new JSONObject();
		String organizationabbr = null;
		Organization organization = null;
		try {
			JSONObject object = new JSONObject(requestBody);
			organizationabbr = object.getString("orgabbr");
			organization = organizationService.getOrganizationByAbbreviation(organizationabbr);
			JSONArray jsonArray = object.getJSONArray("userIds");
			for(int i=0; i < jsonArray.length();i++) {
				int userId = jsonArray.getInt(i);
				User user = userService.getUser(userId);
				OrganizationMembership organizationMembership = organizationMembershipService.getUserOrganizationMembership(user, organization);
				organizationMembership.setStatus(1);
				organizationMembershipService.addOrganizationMembership(organizationMembership);
				
			}
		}
		catch(Exception e) {
			try {
				e.printStackTrace();
				responseJsonObject.put("response", "Failed");
				return responseJsonObject.toString();
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		try {
			responseJsonObject.put("response", "Members Approved");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		List<String > androidTargets = getTargetDevices(organization);
		if(androidTargets.size() > 0) {
			GcmRequest gcmRequest = new GcmRequest();
			HashMap<String, Integer> dashData = null;
			try {
				dashData =dashBoardLocal(organizationabbr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			gcmRequest.broadcast(androidTargets,organizationabbr,dashData);					}
		return responseJsonObject.toString();	
	}
	
}
