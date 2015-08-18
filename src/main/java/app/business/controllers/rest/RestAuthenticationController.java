package app.business.controllers.rest;

import in.ac.iitb.ivrs.telephony.base.util.IVRUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import app.data.repositories.GroupMembershipRepository;
import app.data.repositories.GroupRepository;
import app.data.repositories.OrganizationMembershipRepository;
import app.data.repositories.OrganizationRepository;
import app.data.repositories.UserPhoneNumberRepository;
import app.data.repositories.UserRepository;
import app.entities.Group;
import app.entities.GroupMembership;
import app.entities.Organization;
import app.entities.OrganizationMembership;
import app.entities.User;
import app.entities.UserPhoneNumber;


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
	UserRepository userRepository;
	
	@RequestMapping(value = "/otp",method = RequestMethod.POST )
	public String otp(@RequestBody String requestBody)
	{
		JSONObject responseJsonObject = new JSONObject();
		JSONObject orgListJsonObject = new JSONObject();
		HashMap<String,String> response= new HashMap<String, String>();
		JSONObject jsonObject=null;
		String phonenumber = null;
		try {
			jsonObject = new JSONObject(requestBody);
			phonenumber=jsonObject.getString("phonenumber");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		UserPhoneNumber userPhoneNumber=userPhoneNumberRepository.findByPhoneNumber(phonenumber);
		List<Organization> orglist = organizationRepository.findAll();
		Integer i =new Integer(0);
		for(Organization organization: orglist)
		{
			JSONObject orgObj = new JSONObject();
			try {
				
				orgObj.put("name", organization.getName());
				orgObj.put("org_id", organization.getOrganizationId());
				orgObj.put("abbr", organization.getAbbreviation());		
				orgListJsonObject.put(i.toString(), orgObj);
			} catch (JSONException e) {
				e.printStackTrace();
			}	
			i++;
		}
		
		if(userPhoneNumber==null)
		{
			String otp=randomString(4);
			IVRUtils.sendSMS(phonenumber, otp, null , null);
			response.put("otp",otp);
			try {
				responseJsonObject.put("otp", otp);
				responseJsonObject.put("organizations",orgListJsonObject);
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
		try {
			jsonObject = new JSONObject(requestBody);
			phonenumber=jsonObject.getString("phonenumber");
			address=jsonObject.getString("address");
			password=jsonObject.getString("password");
			name=jsonObject.getString("name");
			email=jsonObject.getString("email");
			orgListJsonArray=jsonObject.getJSONArray("orglist");
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
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		user.setAddress(address);
		user.setCallLocale("en");
		user.setEmail(email);
		user.setSha256Password(passwordEncoder.encode(password));
		user.setName(name);
		user=userRepository.save(user);
		List<OrganizationMembership>  organizationMemberships= new ArrayList<OrganizationMembership>();
		List<GroupMembership>  groupMemberships= new ArrayList<GroupMembership>();
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
				OrganizationMembership organizationMembership = new OrganizationMembership();
				organizationMembership.setOrganization(organization);
				organizationMembership.setUser(user);
				organizationMembership.setIsAdmin(false);
				organizationMembership.setIsPublisher(false);
				organizationMembership=organizationMemberRepository.save(organizationMembership);
				organizationMemberships.add(organizationMembership);
				//Get GroupMembership
				Group group = groupRepository.findByNameAndOrganization("Parent Group", organization);
				GroupMembership groupMembership= new GroupMembership();
				groupMembership.setGroup(group);
				groupMembership.setUser(user);
				groupMembership=groupMembershipRepository.save(groupMembership);
				groupMemberships.add(groupMembership);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		user.setGroupMemberships(groupMemberships);
		user.setOrganizationMemberships(organizationMemberships);
		user.setTextbroadcastlimit(0);
		user.setVoicebroadcastlimit(0);
		user=userRepository.save(user);
		userPhoneNumber.setPhoneNumber(phonenumber);
		userPhoneNumber.setPrimary(true);
		userPhoneNumber.setUser(user);
		userPhoneNumber=userPhoneNumberRepository.save(userPhoneNumber);
		userPhoneNumbers.add(userPhoneNumber);
		user.setUserPhoneNumbers(userPhoneNumbers);
		userRepository.save(user);
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
			IVRUtils.sendSMS(phonenumber, otp, null , null);
			response.put("otp",otp);
			try {
				responseJsonObject.put("otp", otp);
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
		if(password!=null)
		{
			User user = userRepository.findByuserPhoneNumbers_phoneNumber(phonenumber);
			if(passwordEncoder.matches(password, user.getSha256Password()))
			{
				try 
				{
					responseJsonObject.put("Authentication", "success");	
					responseJsonObject.put("email", user.getEmail());
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return responseJsonObject.toString();
			}
			else
			{
				try {
					responseJsonObject.put("Authentication", "failure");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return responseJsonObject.toString();
			}
		}
		else{
			
			try {
				responseJsonObject.put("Authentication", "failure");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return responseJsonObject.toString();
		}
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
