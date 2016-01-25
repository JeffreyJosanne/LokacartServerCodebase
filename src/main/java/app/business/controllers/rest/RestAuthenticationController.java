package app.business.controllers.rest;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.business.services.GcmTokensService;
import app.business.services.GroupMembershipService;
import app.business.services.OrganizationMembershipService;
import app.business.services.OrganizationService;
import app.business.services.UserPhoneNumberService;
import app.business.services.UserService;
import app.business.services.message.MessageService;
import app.data.repositories.GroupMembershipRepository;
import app.data.repositories.GroupRepository;
import app.data.repositories.OrganizationMembershipRepository;
import app.data.repositories.OrganizationRepository;
import app.data.repositories.UserPhoneNumberRepository;
import app.data.repositories.UserRepository;
import app.data.repositories.VersionCheckRepository;
import app.entities.GcmTokens;
import app.entities.Group;
import app.entities.GroupMembership;
import app.entities.Organization;
import app.entities.OrganizationMembership;
import app.entities.User;
import app.entities.UserPhoneNumber;
import app.entities.VersionCheck;
import app.entities.message.Message;
import app.util.GcmRequest;
import app.util.SendMail;


@RestController
@RequestMapping("/app")
public class RestAuthenticationController {

	@Autowired
	UserPhoneNumberRepository userPhoneNumberRepository;
	
	@Autowired
	OrganizationRepository organizationRepository;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	OrganizationMembershipRepository organizationMemberRepository;
	
	@Autowired
	GroupMembershipRepository groupMembershipRepository;
	
	@Autowired
	GroupRepository groupRepository;
	
	@Autowired
	GroupMembershipService groupMembershipService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	OrganizationService organizationService;
	
	@Autowired
	OrganizationMembershipService organizationMembershipService;
	
	@Autowired
	UserPhoneNumberService userPhoneNumberService;
	
	@Autowired
	VersionCheckRepository versionCheckRepository;
	

	@Autowired
	GcmTokensService gcmTokensService;
	
	@Autowired
	UserService userService;

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
	@RequestMapping(value = "/versioncheck",method = RequestMethod.GET)
	public String checkVersion (@RequestParam(value="version")String version) {
		int id=1;
		float appVersion = Float.parseFloat(version);
		JSONObject responseJsonObject = new JSONObject();
		VersionCheck currentVersion = versionCheckRepository.findOne(id);
		float curVersion = currentVersion.getVersion();
		int curMandatory = currentVersion.getMandatory();
		if (curVersion <= appVersion) {
			try {
				responseJsonObject.put("response", "0");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else if ((curVersion > appVersion) && (curMandatory == 0) ) {
			try {
				responseJsonObject.put("response", "1");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else if ((curVersion > appVersion) && (curMandatory == 1) ) {
			try {
				responseJsonObject.put("response", "2");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return responseJsonObject.toString();
	}
	
	@RequestMapping(value = "/otp",method = RequestMethod.POST )
	public String otp(@RequestBody String requestBody) throws Exception
	{
		JSONObject responseJsonObject = new JSONObject();
		HashMap<String,String> response= new HashMap<String, String>();
		JSONObject jsonObject=null;
		String phonenumber = null;
		String email = null;
		try {
			jsonObject = new JSONObject(requestBody);
			phonenumber=jsonObject.getString("phonenumber");
			email=jsonObject.getString("email");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		UserPhoneNumber userPhoneNumber=userPhoneNumberRepository.findByPhoneNumber(phonenumber);
		if( userRepository.findByEmail(email).size()!=0)
		{
			responseJsonObject.put("text", "Email entered already exists.");
			responseJsonObject.put("otp", "null");
			return responseJsonObject.toString();
		}
		if( userRepository.findByuserPhoneNumbers_phoneNumber(phonenumber)!=null)
		{
			responseJsonObject.put("text", "Phone number entered already exists.");
			responseJsonObject.put("otp", "null");
			return responseJsonObject.toString();
		}
		List<Organization> orglist = organizationRepository.findAll();
		JSONArray orgArray=new JSONArray();
		for(Organization organization: orglist)
		{
			try {
				if(!organization.getName().equals("Testing") && !organization.getName().equals("TestOrg1") && !organization.getName().equals("Testorg3"))
				{
					JSONObject org= new JSONObject();
					org.put("name", organization.getName());
					org.put("org_id", organization.getOrganizationId());
					org.put("abbr", organization.getAbbreviation());	
					org.put("ph_no", organization.getIvrNumber());
					orgArray.put(org);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}	
		}
				
		if(userPhoneNumber==null)
		{
			String otp=randomString(4);
			int status=0;
			if((status=SendMail.sendMail(email, "Cottage Industry App OTP" , "Your OTP is: " + otp ))==1)
				response.put("text", "Otp has been sent to your email");
			//IVRUtils.sendSMS(phonenumber, otp, null , null);
			response.put("otp",otp);
			try {
				responseJsonObject.put("otp", otp);
				responseJsonObject.put("organizations",orgArray);
				if(status==1)
					responseJsonObject.put("text", "Otp has been sent to your email");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return responseJsonObject.toString();
		}
		else
		{
			response.put("otp",null);
			try {
				responseJsonObject.put("otp", "null");
				responseJsonObject.put("text", "null");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return responseJsonObject.toString();
		}
		
		
	}
	
	@RequestMapping(value = "/register",method = RequestMethod.POST ,produces="application/json")
	@Transactional
	public HashMap<String,String> registration(@RequestBody String requestBody)
	{
		/*
		 * Add organization membership, user, organization
		 */
		HashMap<String,String> response= new HashMap<String, String>();
		JSONObject jsonObject=null;
		String phonenumber = null;
		String address =null;
		String password=null;
		String name=null;
		String email=null;
		JSONArray orgListJsonArray = null;
		User user = new User();
		UserPhoneNumber userPhoneNumber= new UserPhoneNumber();
		List<UserPhoneNumber> userPhoneNumbers= new ArrayList<UserPhoneNumber>();
		//List<Organization> orgList= new ArrayList<Organization>();
		try {
			jsonObject = new JSONObject(requestBody);
			phonenumber=jsonObject.getString("phonenumber");
			address=jsonObject.getString("address");
			password=jsonObject.getString("password");
			name=jsonObject.getString("name");
			email=jsonObject.getString("email");
			//orgListJsonArray=jsonObject.getJSONArray("orglist");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		/*
		 * Check if exists 
		*/
		User usercheck = userRepository.findByuserPhoneNumbers_phoneNumber(phonenumber);
		if(usercheck!=null)
		{
			response.put("Status", "Failure");
			response.put("Error", "Number Exists");
			return response;
		}
		List<User> userCheckList = userRepository.findByEmail(email);
		if(userCheckList.size()>0)
		{
			response.put("Status", "Failure");
			response.put("Error", "Email Exists");
			return response;
		}
		/*for (int i=0;i<orgListJsonArray.length();i++)
		{
			 try {
				JSONObject org = orgListJsonArray.getJSONObject(i);
				int org_id=org.getInt("org_id");
				//Adding organization
				Organization organization= organizationRepository.findOne(org_id);
				if(organization==null)
				{
					response.put("Status", "Failure");
					response.put("Error", "Organization with Id "+org_id+" does not exists");
					return response;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}*/
		
		//Organization organization=new Organization();
		user.setAddress(address);
		user.setCallLocale("en");
		user.setEmail(email);
		user.setSha256Password(passwordEncoder.encode(password));
		user.setName(name);
//		java.util.Date date= new java.util.Date();
//		Timestamp currentTimestamp= new Timestamp(date.getTime());
//		user.setTime(currentTimestamp);
		user=userRepository.save(user);
		/*List<OrganizationMembership>  organizationMemberships= new ArrayList<OrganizationMembership>();
		List<GroupMembership>  groupMemberships= new ArrayList<GroupMembership>();
		for (int i=0;i<orgListJsonArray.length();i++)
		{
			 try {
				JSONObject org = orgListJsonArray.getJSONObject(i);
				int org_id=org.getInt("org_id");
				//Adding organization
				organization= organizationRepository.findOne(org_id);
				if(organization==null)
				{
					response.put("Status", "Failure");
					response.put("Error", "Organization with Id "+org_id+" does not exists");
					return response;
				}
				orgList.add(organization);
				OrganizationMembership organizationMembership = new OrganizationMembership();
				organizationMembership.setOrganization(organization);
				organizationMembership.setUser(user);
				organizationMembership.setIsAdmin(false);
				organizationMembership.setIsPublisher(false);
				organizationMembership.setStatus(0);
				organizationMembership=organizationMemberRepository.save(organizationMembership);
				organizationMemberships.add(organizationMembership);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}*/
		//user.setGroupMemberships(groupMemberships);
		//user.setOrganizationMemberships(organizationMemberships);
		user.setTextbroadcastlimit(0);
		user.setVoicebroadcastlimit(0);
		user=userRepository.save(user);
		System.out.println("User phone number is being saved");
		phonenumber="91"+phonenumber;
		userPhoneNumber.setPhoneNumber(phonenumber);
		userPhoneNumber.setPrimary(true);
		userPhoneNumber.setUser(user);
		userPhoneNumber=userPhoneNumberRepository.save(userPhoneNumber);
		userPhoneNumbers.add(userPhoneNumber);
		user.setUserPhoneNumbers(userPhoneNumbers);
		System.out.println("User phone number is  saved");
		userRepository.save(user);
//		for(Organization org: orgList)
//		{
//			groupMembershipService.addParentGroupMembership(org, user);
//		}
		response.put("Status","Success");
		return response;
	}
	
	@RequestMapping(value = "/orgsave",method = RequestMethod.POST ,produces="application/json")
	@Transactional
	public HashMap<String,String> orgselection(@RequestBody String requestBody)
	{
		/*
		 * Add organization membership, user, organization
		 */
		HashMap<String,String> response= new HashMap<String, String>();
		JSONObject jsonObject=null;
		String phonenumber = null;
		String email=null;
		JSONArray orgListJsonArray = null;
	
		List<Organization> orgList= new ArrayList<Organization>();
		try {
			jsonObject = new JSONObject(requestBody);
			phonenumber="91"+jsonObject.getString("phonenumber");
//			address=jsonObject.getString("address");
//			password=jsonObject.getString("password");
//			name=jsonObject.getString("name");
//			email=jsonObject.getString("email");
			orgListJsonArray=jsonObject.getJSONArray("orglist");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		/*
		 * Check if exists 
		*/
		System.out.println("User phone no is: "+phonenumber);
		User user = userRepository.findByuserPhoneNumbers_phoneNumber(phonenumber);
		if(user==null)
		{
			response.put("Status", "Failure");
			response.put("Error", "Number doesn't Exists");
			return response;
		}
		List<User> userCheckList = userRepository.findByEmail(email);
		if(userCheckList.size()==0)
		{
			response.put("Status", "Failure");
			response.put("Error", "Email doesn't exist Exists");
			return response;
		}
		for (int i=0;i<orgListJsonArray.length();i++)
		{
			 try {
				JSONObject org = orgListJsonArray.getJSONObject(i);
				int org_id=org.getInt("org_id");
				//Adding organization
				Organization organization= organizationRepository.findOne(org_id);
				if(organization==null)
				{
					response.put("Status", "Failure");
					response.put("Error", "Organization with Id "+org_id+" does not exists");
					return response;
				}
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
					gcmRequest.broadcast(androidTargets,orgabbr,dashData);		
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		Organization organization=new Organization();
//		user.setAddress(address);
//		user.setCallLocale("en");
//		user.setEmail(email);
//		user.setSha256Password(passwordEncoder.encode(password));
//		user.setName(name);
		java.util.Date date= new java.util.Date();
		Timestamp currentTimestamp= new Timestamp(date.getTime());
		user.setTime(currentTimestamp);
		user=userRepository.save(user);
		List<OrganizationMembership>  organizationMemberships= new ArrayList<OrganizationMembership>();
		List<GroupMembership>  groupMemberships= new ArrayList<GroupMembership>();
		for (int i=0;i<orgListJsonArray.length();i++)
		{
			 try {
				JSONObject org = orgListJsonArray.getJSONObject(i);
				int org_id=org.getInt("org_id");
				//Adding organization
				organization= organizationRepository.findOne(org_id);
				if(organization==null)
				{
					response.put("Status", "Failure");
					response.put("Error", "Organization with Id "+org_id+" does not exists");
					return response;
				}
				orgList.add(organization);
				OrganizationMembership organizationMembership = new OrganizationMembership();
				organizationMembership.setOrganization(organization);
				organizationMembership.setUser(user);
				organizationMembership.setIsAdmin(false);
				organizationMembership.setIsPublisher(false);
				organizationMembership.setStatus(0);
				organizationMembership=organizationMemberRepository.save(organizationMembership);
				organizationMemberships.add(organizationMembership);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		//user.setGroupMemberships(groupMemberships);
		user.setOrganizationMemberships(organizationMemberships);
//		user.setTextbroadcastlimit(0);
//		user.setVoicebroadcastlimit(0);
//		user=userRepository.save(user);
//		phonenumber="91"+phonenumber;
//		userPhoneNumber.setPhoneNumber(phonenumber);
//		userPhoneNumber.setPrimary(true);
//		userPhoneNumber.setUser(user);
//		userPhoneNumber=userPhoneNumberRepository.save(userPhoneNumber);
//		userPhoneNumbers.add(userPhoneNumber);
//		user.setUserPhoneNumbers(userPhoneNumbers);
		userRepository.save(user);
		for(Organization org: orgList)
		{
			groupMembershipService.addParentGroupMembership(org, user);
		}
		response.put("Status","Success");
		return response;
	}
	
	@RequestMapping(value = "/forgotpassword",method = RequestMethod.POST )
	public String forgotpassword(@RequestBody String requestBody)
	{	
		JSONObject responseJsonObject = new JSONObject();
		HashMap<String,String> response= new HashMap<String, String>();
		JSONObject jsonObject=null;
		String phonenumber = null;
		try {
			jsonObject = new JSONObject(requestBody);
			phonenumber=jsonObject.getString("phonenumber");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		UserPhoneNumber userPhoneNumber = userPhoneNumberRepository.findByPhoneNumber(phonenumber);
		if(userPhoneNumber!=null)
		{
			String otp=randomString(4);
			int status=0;
			String email=userPhoneNumber.getUser().getEmail();
			if((status=SendMail.sendMail(email, "Cottage Industry App OTP" , "Your OTP is: " + otp ))==1)
				response.put("text", "Otp has been sent to your email");
			//IVRUtils.sendSMS(phonenumber, otp, null , null);
			response.put("otp",otp);
			try {
				responseJsonObject.put("otp", otp);
				if(status==1)
				responseJsonObject.put("text", "Otp has been sent to your email");	
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return responseJsonObject.toString();
		}
		else
		{
			response.put("otp",null);
			try {
				responseJsonObject.put("otp", "null");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return responseJsonObject.toString();
		}
		
	}
	
	@RequestMapping(value = "/changenumber",method = RequestMethod.POST )
	public String changenumber(@RequestBody String requestBody)
	{	
		JSONObject responseJsonObject = new JSONObject();
		JSONObject jsonObject=null;
		String phonenumber_old = null;
		String phonenumber_new = null;
		try {
			jsonObject = new JSONObject(requestBody);
			phonenumber_old="91"+jsonObject.getString("phonenumber");
			phonenumber_new="91"+jsonObject.getString("phonenumber_new");
			System.out.println("phonenumber_old is :"+phonenumber_old);
			System.out.println("phonenumber_new is :"+phonenumber_new);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
		UserPhoneNumber userPhoneNumber = userPhoneNumberRepository.findByPhoneNumber(phonenumber_old);
		User user = userPhoneNumber.getUser();
		try{
			if (userPhoneNumberRepository.findByPhoneNumber(phonenumber_new)!=null)
			{
				responseJsonObject.put("status", "new number already present.");
				return responseJsonObject.toString();
			}
			userPhoneNumber.setPhoneNumber(phonenumber_new);
			userPhoneNumberService.addUserPhoneNumber(userPhoneNumber);
			userPhoneNumberService.setPrimaryPhoneNumberByUser(user, userPhoneNumber);
			responseJsonObject.put("status", "success");
		}
		catch (JSONException e) {
			e.printStackTrace();
			} 
		} catch (NullPointerException e)
		{
			try {
				responseJsonObject.put("status","user phone number not present");
			} catch (JSONException e1) {
				e.printStackTrace();
			}
		}
		return responseJsonObject.toString();
	}
	
	@RequestMapping(value = "/login",method = RequestMethod.POST )
	public String login(@RequestBody String requestBody)
	{
		//Check if exists
		JSONObject responseJsonObject = new JSONObject();
		JSONObject jsonObject=null;
		String phonenumber = null;
		String password=null;
		try {
			jsonObject = new JSONObject(requestBody);
			phonenumber=jsonObject.getString("phonenumber");
			password=jsonObject.getString("password");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		User user = userRepository.findByuserPhoneNumbers_phoneNumber(phonenumber);
		
		
		if(user!=null)
		{
			/*
			 * Check if the user is approved
			 * 
			 */
			if(password==null)
			{
				try {
					responseJsonObject.put("Status", "Error");
					responseJsonObject.put("Error", "Password is null.");
					responseJsonObject.put("Authentication", "failure");
					responseJsonObject.put("registered", "true");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return responseJsonObject.toString();
			}
			if(user.getSha256Password()==null)
			{
				try {
					responseJsonObject.put("Status", "Error");
					responseJsonObject.put("Error", "User Password is null. Set the password");
					responseJsonObject.put("Authentication", "failure");
					responseJsonObject.put("registered", "true");
				} catch (JSONException e) {
					
					e.printStackTrace();
				}
				return responseJsonObject.toString();
			}
			if(passwordEncoder.matches(password, user.getSha256Password()))
			{
				List<OrganizationMembership> organizationMemberships = user.getOrganizationMemberships();
				List<Organization> organizationList=organizationService.getAllOrganizationList();
				if(organizationList.size()==0)
				{
					try{
						responseJsonObject.put("Authentication", "success");	
						responseJsonObject.put("email", user.getEmail());
						responseJsonObject.put("Error", "No organization has accepted the user");
						responseJsonObject.put("organizations", "null");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					return responseJsonObject.toString();
				}
				
				
				JSONArray orgArray= new JSONArray();
				for(Organization organization: organizationList)
				{
					try {
						if(!organization.getName().equals("Testing") && !organization.getName().equals("TestOrg1") && !organization.getName().equals("Testorg3"))
						{
							
							JSONObject org= new JSONObject();
							org.put("name", organization.getName());
							org.put("org_id", organization.getOrganizationId());
							org.put("abbr", organization.getAbbreviation());	
							org.put("ph_no", organization.getIvrNumber());
							if(organizationMembershipService.getUserOrganizationMembership(user, organization)==null)
							{
								org.put("status", "Rejected");
								orgArray.put(org);
								continue;
							}
							if(organizationMembershipService.getOrganizationMembershipStatus(user, organization)==1)
								org.put("status", "Accepted");
							//organizationList.add(organizationMembership.getOrganization());
							else if(organizationMembershipService.getOrganizationMembershipStatus(user, organization)==0)
								org.put("status", "Pending");
							else
								org.put("status", "Rejected");
							orgArray.put(org);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}	
				}
				try 
				{
					responseJsonObject.put("Authentication", "success");	
					responseJsonObject.put("email", user.getEmail());
					responseJsonObject.put("organizations", orgArray);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return responseJsonObject.toString();
			}
			else
			{
				try {
					responseJsonObject.put("Authentication", "failure");
					responseJsonObject.put("registered", "true");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return responseJsonObject.toString();
			}
		}
		else
		{
			try {
				responseJsonObject.put("Authentication", "failure");
				responseJsonObject.put("registered", "false");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return responseJsonObject.toString();
		}
	
	}
	
	//authentication for admin app.
		@RequestMapping(value = "/loginAdmin",method = RequestMethod.POST )
		public String loginAdmin(@RequestBody String requestBody)
		{
			//Check if exists
			JSONObject responseJsonObject = new JSONObject();
			JSONObject jsonObject=null;
			String phonenumber = null;
			String password=null;
			try {
				jsonObject = new JSONObject(requestBody);
				phonenumber=jsonObject.getString("phonenumber");
				password=jsonObject.getString("password");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			User user = userRepository.findByuserPhoneNumbers_phoneNumber(phonenumber);
			
			
			if(user!=null)
			{
				/*
				 * Check if the user is approved
				 * 
				 */
				if(password==null)
				{
					try {
						responseJsonObject.put("Status", "Error");
						responseJsonObject.put("Error", "Password is null.");
						responseJsonObject.put("Authentication", "failure");
						responseJsonObject.put("registered", "true");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					return responseJsonObject.toString();
				}
				if(user.getSha256Password()==null)
				{
					try {
						responseJsonObject.put("Status", "Error");
						responseJsonObject.put("Error", "User Password is null. Set the password");
						responseJsonObject.put("Authentication", "failure");
						responseJsonObject.put("registered", "true");
					} catch (JSONException e) {
						
						e.printStackTrace();
					}
					return responseJsonObject.toString();
				}
				if(passwordEncoder.matches(password, user.getSha256Password()))
				{
					List<OrganizationMembership> organizationMemberships = user.getOrganizationMemberships();
					for (OrganizationMembership orgm: organizationMemberships)
					{
						if(orgm.getIsAdmin())
						{
							try{
								responseJsonObject.put("Authentication", "success");	
								responseJsonObject.put("email", user.getEmail());
								JSONObject org= new JSONObject();
								org.put("name", orgm.getOrganization().getName());
								org.put("org_id", orgm.getOrganization().getOrganizationId());
								org.put("abbr", orgm.getOrganization().getAbbreviation());	
								org.put("ph_no", orgm.getOrganization().getIvrNumber());
								responseJsonObject.put("organization", org);
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
								return responseJsonObject.toString();
						}
							
					}
					try {
						responseJsonObject.put("Authentication", "failure");
						responseJsonObject.put("registered", "true");
						responseJsonObject.put("Error", "Registered user is not an admin.");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					return responseJsonObject.toString();
				}
				else
				{
					try {
						responseJsonObject.put("Authentication", "failure");
						responseJsonObject.put("registered", "true");
						responseJsonObject.put("Error", "null");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					return responseJsonObject.toString();
				}
			}
			else
			{
				try {
					responseJsonObject.put("Authentication", "failure");
					responseJsonObject.put("registered", "false");
					responseJsonObject.put("Error", "null");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return responseJsonObject.toString();
			}
		
		}
	
	@RequestMapping(value = "/changepassword",method = RequestMethod.POST )
	public HashMap<String,String> changePassword(@RequestBody String requestBody)
	{
		HashMap<String,String> response = new HashMap<String, String>();
		JSONObject jsonObject=null;
		String password = null;
		String phonenumber=null;
		try {
			jsonObject = new JSONObject(requestBody);
			phonenumber=jsonObject.getString("phonenumber");
			password=jsonObject.getString("password");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		User user = userRepository.findByuserPhoneNumbers_phoneNumber(phonenumber);
		if(user==null)
		{
			response.put("Status","Error");
			response.put("Error","No user with the phone number:"+phonenumber+" exists.");
			return response;
		}
		//AuthenticatedUser authuser=Utils.getSecurityPrincipal();
		
		password=passwordEncoder.encode(password);
		user.setSha256Password(password);
		userRepository.save(user);
		response.put("Status","Success");
		return response;
	}
	
	
	//Util functions
	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	static Random rnd = new Random();

	String randomString( int len ) 
	{
	   StringBuilder sb = new StringBuilder( len );
	   for( int i = 0; i < len; i++ ) 
	      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
	   return sb.toString();
	}
	
}
