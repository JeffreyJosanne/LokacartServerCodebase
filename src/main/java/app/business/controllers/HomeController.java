package app.business.controllers;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import app.business.controllers.rest.DashboardRestController;
import app.business.services.GcmTokensService;
import app.business.services.GroupMembershipService;
import app.business.services.OrganizationMembershipService;
import app.business.services.OrganizationService;
import app.business.services.UserPhoneNumberService;
import app.business.services.UserService;
import app.business.services.message.MessageService;
import app.entities.GcmTokens;
import app.entities.Group;
import app.entities.GroupMembership;
import app.entities.Organization;
import app.entities.OrganizationMembership;
import app.entities.User;
import app.entities.message.Message;
import app.util.GcmRequest;
import app.util.SendMail;

@Controller
@RequestMapping("/web/{org}")
public class HomeController {

	@Autowired
	OrganizationService organizationService;
	
	@Autowired
	OrganizationMembershipService organizationMembershipService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	GroupMembershipService groupMembershipService;
	
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
		dashmap.put("totalUsers", membershipListapproved.size());
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

	@RequestMapping(value="/homePage")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	public String homePage(@PathVariable String org, Model model) {
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		model.addAttribute("organization",organization);
		return "home";
	}
	@RequestMapping(value="/homePage/approve", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	@ResponseBody
	public void approveUser(@PathVariable String org, @RequestBody Map<String,String> userDetails) {
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		int userId = Integer.parseInt(userDetails.get("userid"));
		String phoneno= userDetails.get("phno");
		User user = userService.getUser(userId);
		OrganizationMembership membership= organizationMembershipService.getUserOrganizationMembership(user, organization);
		membership.setStatus(1);
		organizationMembershipService.addOrganizationMembership(membership);
		if(organization.getName().equalsIgnoreCase("Nature's Gram"))
			SendMail.sendMail(user.getEmail(), "Cottage Industry app Organization Approval" , "Congratualtions!!! You have been approved by "+organization.getName()+" organization.\n\nThank You for being part of our community. You are now ready to place an order.\n\nWishing you Health and Happiness always.\n\nFor support call : 9930332255\nOr mail us at : vishal@naturesgram.com\n\nThanks & Regards\nVishal V Ghodke\nFounder - Natures Gram\nwww.naturesgram.com\nfacebook.com/naturesgram" );
		else
			SendMail.sendMail(user.getEmail(), "Cottage Industry app Organization Approval" , "Congratualtions!!! You have been approved by "+organization.getName()+" organization.\n\nThank You for being part of our community. You are now ready to place an order.\n\nWishing you Health and Happiness always.");
			
		String orgabbr = organization.getAbbreviation();
		List <String> androidTargets = getTargetDevices(organization);
		if(androidTargets.size()>0) {
			GcmRequest gcmRequest = new GcmRequest();
			gcmRequest.broadcast(user.getName()+" would like to be a member", "New Member Request", androidTargets,1,user.getUserId());
			HashMap<String, Integer> dashData = null;
			try {
				dashData = dashBoardLocal(orgabbr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			gcmRequest.broadcast(androidTargets,orgabbr,dashData);				}
		//IVRUtils.sendSMS(phoneno, "Congratualtions!!! You have been approved by "+organization.getName()+" organization.",null,null);
	}
	
	@RequestMapping(value="/homePage/reject", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	@ResponseBody
	public void rejectUser(@PathVariable String org, @RequestBody Map<String,String> userDetails) {
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		int userId = Integer.parseInt(userDetails.get("userid"));
		String phoneno= userDetails.get("phno");
		User user = userService.getUser(userId);
		for(GroupMembership groupMembership: user.getGroupMemberships()) {
			if(groupMembership.getGroup().getOrganization().getName().equals(organization.getName()))
				groupMembershipService.removeGroupMembership(groupMembership);
		}
		
		OrganizationMembership organizationMembership= organizationMembershipService.getUserOrganizationMembership(user, organization);
		organizationMembershipService.removeOrganizationMembership(organizationMembership);
		String orgabbr = organization.getAbbreviation();
		List <String> androidTargets = getTargetDevices(organization);
		if(androidTargets.size()>0) {
			GcmRequest gcmRequest = new GcmRequest();
			gcmRequest.broadcast(user.getName()+" would like to be a member", "New Member Request", androidTargets,1,user.getUserId());
			HashMap<String, Integer> dashData = null;
			try {
				dashData = dashBoardLocal(orgabbr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			gcmRequest.broadcast(androidTargets,orgabbr,dashData);				}
	}
}
