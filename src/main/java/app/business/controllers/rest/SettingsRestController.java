package app.business.controllers.rest;

import javax.transaction.Transactional;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import app.business.services.BillLayoutSettingsService;
import app.business.services.OrganizationService;
import app.business.services.UserPhoneNumberService;
import app.business.services.UserService;
import app.entities.BillLayoutSettings;
import app.entities.Organization;
import app.entities.User;
import app.entities.UserPhoneNumber;

@Controller
@RequestMapping("/api/{org}")
public class SettingsRestController {
	
	@Autowired
	OrganizationService organizationService;
	
	@Autowired
	BillLayoutSettingsService billLayoutSettingsService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserPhoneNumberService userPhoneNumberService;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	
	@RequestMapping(value="/billsettingsupdate", method=RequestMethod.POST, produces = "application/json")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public @ResponseBody String changeBillSettings(@PathVariable String org, @RequestBody String requestBody) {
		JSONObject jsonResponseObject = new JSONObject();
		String newName = null, newAddress = null, newContact = null, newHeader = null, newFooter = null;
		try{
			JSONObject object = new JSONObject(requestBody);
			newName = object.getString("newname");
			newAddress = object.getString("newaddress");
			newContact = object.getString("newcontact");
			newHeader = object.getString("newheader");
			newFooter = object.getString("newfooter");
			Organization organization = organizationService.getOrganizationByAbbreviation(org);
			BillLayoutSettings billLayoutSettings = billLayoutSettingsService.getBillLayoutSettingsByOrganization(organization);
			organization.setName(newName);
			organization.setAddress(newAddress);
			organization.setContact(newContact);
			billLayoutSettings.setHeaderContent(newHeader);
			billLayoutSettings.setFooterContent(newFooter);
			organizationService.addOrganization(organization);
			billLayoutSettingsService.addBillLayoutSettings(billLayoutSettings);
		}
		catch(Exception e) {
			try {
				e.printStackTrace();
				jsonResponseObject.put("response", "Failed to update");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return jsonResponseObject.toString();
		}
		
		try {
			jsonResponseObject.put("response", "Successfully updated");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponseObject.toString();
	}
	
	
	@RequestMapping(value="/billsettingsview", method=RequestMethod.GET, produces = "application/json")
	@PreAuthorize("hasRole('ADMIN'+#org)")

	public @ResponseBody String viewBillSettings(@PathVariable String org) {
		JSONObject jsonResponseObject = new JSONObject();
		try{
			
			Organization organization = organizationService.getOrganizationByAbbreviation(org);
			BillLayoutSettings billLayoutSettings = billLayoutSettingsService.getBillLayoutSettingsByOrganization(organization);
			jsonResponseObject.put("name", organization.getName());
			jsonResponseObject.put("address", organization.getAddress());
			jsonResponseObject.put("contact", organization.getContact());
			jsonResponseObject.put("header", billLayoutSettings.getHeaderContent());
			jsonResponseObject.put("footer", billLayoutSettings.getFooterContent());
		}
		catch(Exception e){
			e.printStackTrace();
			try {
				jsonResponseObject.put("response", "failure");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return jsonResponseObject.toString();
		}
		try {
			jsonResponseObject.put("response", "success");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponseObject.toString();
		
	}
	
	@RequestMapping(value="/profilesettingsview", method=RequestMethod.GET, produces = "application/json")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	
	
	public @ResponseBody String viewProfileSettings(@PathVariable String org, @RequestParam String number) {
		JSONObject jsonResponseObject = new JSONObject();
		try{
			UserPhoneNumber userPhoneNumber = userPhoneNumberService.getUserPhoneNumber(number);
			User user = userPhoneNumber.getUser();
			jsonResponseObject.put("name", user.getName());
			jsonResponseObject.put("email",user.getEmail());
			jsonResponseObject.put("phonenumber",userPhoneNumber.getPhoneNumber());
			jsonResponseObject.put("address", user.getAddress());
		}
		catch (Exception e){
			e.printStackTrace();
			try {
				jsonResponseObject.put("response", "failure");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return jsonResponseObject.toString();
		}
		
		try {
			jsonResponseObject.put("response", "success");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jsonResponseObject.toString();
	}
	
	
	
	@RequestMapping(value="/profilesettingsupdate", method=RequestMethod.POST, produces = "application/json")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	
	public @ResponseBody String changeProfileSettings(@PathVariable String org, @RequestBody String requestBody) {
		JSONObject jsonResponseObject = new JSONObject();
		String phonenumber = null, email =null, name = null, password = null, address = null, newNumber = null;
		try{
			JSONObject object = new JSONObject(requestBody);
			phonenumber = object.getString("phonenumber");
			newNumber = object.getString("newnumber");
			email = object.getString("email");
			name = object.getString("name");
			address = object.getString("address");
			UserPhoneNumber userPhoneNumber = userPhoneNumberService.getUserPhoneNumber(phonenumber);
			User user = userPhoneNumber.getUser();
			try{
				password = object.getString("password");
				user.setSha256Password(passwordEncoder.encode(password));
			}
			catch(Exception e){
				//do nothing, as no password update requested
			}
			user.setName(name);
			user.setEmail(email);
			user.setAddress(address);
			userPhoneNumber.setPhoneNumber(newNumber);
			userPhoneNumberService.addUserPhoneNumber(userPhoneNumber);
			userService.addUser(user);
		}
		catch(Exception e){
			e.printStackTrace();
			try {
				jsonResponseObject.put("response", "failure");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return jsonResponseObject.toString();
		}
	
		try {
			jsonResponseObject.put("response", "Successfully updated");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponseObject.toString();
	}
	
	@RequestMapping(value="/appsettingsview", method=RequestMethod.GET, produces = "application/json")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public @ResponseBody String viewAppSettings(@PathVariable String org, @RequestParam String number) {
		JSONObject jsonResponseObject = new JSONObject();
		Boolean stockManagement = null;
		Boolean autoApprove = null;
		try{
			Organization organization = organizationService.getOrganizationByAbbreviation(org);
			stockManagement = organization.getStockManagement();
			autoApprove = organization.getAutoApprove();
			jsonResponseObject.put("stockManagement", stockManagement);
			jsonResponseObject.put("autoApprove", autoApprove);
			jsonResponseObject.put("response", "success");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	return jsonResponseObject.toString();
	}
	
	@RequestMapping(value="/appsettingsupdate", method=RequestMethod.POST, produces = "application/json")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public @ResponseBody String changeAppSettings(@PathVariable String org, @RequestBody String requestBody) {
		JSONObject jsonResponseObject = new JSONObject();
		Boolean stockManagement, autoApprove;
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		try{
			JSONObject object = new JSONObject(requestBody);
			stockManagement = object.getBoolean("stockManagement");
			autoApprove = object.getBoolean("autoApprove");
			organization.setStockManagement(stockManagement);
			organization.setAutoApprove(autoApprove);
		}
		catch(Exception e) {
			e.printStackTrace();
			try {
				jsonResponseObject.put("response", "failure");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return jsonResponseObject.toString();
		}
		
		try {
			jsonResponseObject.put("response", "Successfully updated");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	return jsonResponseObject.toString();
	}
	
}




