package app.business.controllers.rest;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import app.business.services.OrderItemService;
import app.business.services.OrderService;
import app.business.services.OrganizationMembershipService;
import app.business.services.OrganizationService;
import app.business.services.UserService;
import app.business.services.UserViewService;
import app.business.services.message.MessageService;
import app.entities.Group;
import app.entities.Organization;
import app.entities.OrganizationMembership;
import app.entities.User;
import app.entities.message.Message;

@RestController
@RequestMapping("/api")
public class DashboardRestController {

	@Autowired
	OrderItemService orderItemService;
	@Autowired
	OrganizationService organizationService;
	@Autowired
	MessageService messageService;
	@Autowired
	OrderService orderService;
	@Autowired
	UserViewService userViewService;
	@Autowired
	UserService userService;
	@Autowired
	OrganizationMembershipService organizationMembershipService;
	
	@RequestMapping(value = "/dashboard",method = RequestMethod.GET )
	public @ResponseBody HashMap<String, Integer> dashBoard(@RequestParam(value="orgabbr") String orgabbr) throws ParseException {
		System.out.println(orgabbr+"in dahsboard function");
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
		System.out.println(dashmap.toString());
		return dashmap;
	}
}