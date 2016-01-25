package app.business.controllers.rest;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.business.services.GcmTokensService;
import app.entities.GcmTokens;

@RestController
@RequestMapping("/app")
public class GcmRestController {
	
	
	@Autowired
	GcmTokensService gcmTokensService;
	
	@RequestMapping(value="/registertoken", method=RequestMethod.POST)
	public String registerToken(@RequestBody String requestBody) {
		JSONObject jsonResponseObject = new JSONObject();
		JSONObject tokenDetails = null;
		String token = null, number = null;
		try{
			tokenDetails = new JSONObject(requestBody);
			token = tokenDetails.getString("token");
			number = tokenDetails.getString("number");
		}
		catch(JSONException e){
			e.printStackTrace();
		}
		
		try {
			GcmTokens tokenCheck = gcmTokensService.getByToken(token);
			gcmTokensService.removeToken(tokenCheck);
		}
		catch(Exception e) {
			//no need to handle exception
			System.out.println("new token");
		}
		GcmTokens gcmToken = new GcmTokens(token, number);
		try{
			gcmTokensService.addToken(gcmToken);
		}
		catch (Exception e){
			try {
				jsonResponseObject.put("response", "fail");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		try {
			jsonResponseObject.put("response", "success");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jsonResponseObject.toString();
	}
	
	@RequestMapping(value="/deregistertoken", method=RequestMethod.POST) 
	public String deregisterToken(@RequestBody String requestBody) {
		JSONObject jsonResponseObject = new JSONObject();
		JSONObject tokenDetails = null;
		String token;
		System.out.println("deregister called");
		try{
		tokenDetails = new JSONObject(requestBody);
		token = tokenDetails.getString("token");
		GcmTokens gcmToken = gcmTokensService.getByToken(token);
		gcmTokensService.removeToken(gcmToken);
		}
		catch(Exception e) {
			System.out.println("Token remove failed");
			try {
				jsonResponseObject.put("response", "fail");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		try {
			jsonResponseObject.put("response", "success");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponseObject.toString();
	}
	
	
	
}
