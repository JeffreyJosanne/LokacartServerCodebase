package app.business.controllers.rest;

import javax.transaction.Transactional;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import app.business.services.BillLayoutSettingsService;
import app.business.services.OrganizationService;
import app.entities.BillLayoutSettings;
import app.entities.Organization;

@Controller
@RequestMapping("/api/{org}")
public class SettingsRestController {
	
	@Autowired
	OrganizationService organizationService;
	
	@Autowired
	BillLayoutSettingsService billLayoutSettingsService;
	
	
	@RequestMapping(value="/billsettingsupdate", method=RequestMethod.POST, produces = "application/json")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public @ResponseBody String changeBillSettings(@PathVariable String org, @RequestBody String requestBody) {
		JSONObject jsonResponseObject = new JSONObject();
		String abbr = null, newName = null, newAddress = null, newContact = null, newHeader = null, newFooter = null;
		try{
			JSONObject object = new JSONObject(requestBody);
			abbr = object.getString("orgabbr");
			newName = object.getString("newname");
			newAddress = object.getString("newaddress");
			newContact = object.getString("newcontact");
			newHeader = object.getString("newheader");
			newFooter = object.getString("newfooter");
			Organization organization = organizationService.getOrganizationByAbbreviation(abbr);
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
	@Transactional
	public @ResponseBody String viewBillSettings(@PathVariable String org) {
		JSONObject jsonResponseObject = new JSONObject();
		String abbr = null;
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
	
	
	
	
}

