package app.business.controllers.rest;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import app.business.services.OrganizationService;
import app.business.services.ProductService;
import app.business.services.ProductTypeService;
import app.entities.Organization;
import app.entities.Product;
import app.entities.ProductType;


@RestController
@RequestMapping("/api")

public class AddProductRestController {
	
	@Autowired 
	OrganizationService organizationService;
	
	@Autowired
	ProductService productService;
	
	@Autowired
	ProductTypeService productTypeService;
	
	@RequestMapping(value ="/product/add", method = RequestMethod.POST )
	public String addProduct(@RequestBody String requestBody){
		
		JSONObject jsonObject = null;
		JSONObject responseJsonObject = new JSONObject();
		String organizationabbr = null;
		Organization organization;
		Product product = new Product();
		try {
			jsonObject = new JSONObject(requestBody);
			organizationabbr=jsonObject.getString("orgabbr");
			product.setName(jsonObject.getString("name"));
			organization = organizationService.getOrganizationByAbbreviation(organizationabbr);
			ProductType productType = productTypeService.getProductTypeByNameAndOrg(jsonObject.getString("productType"), organization);
			product.setUnitRate(Float.parseFloat(jsonObject.getString("rate")));
			product.setProductType(productType);
			product.setQuantity(Integer.parseInt(jsonObject.getString("qty")));
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		try{
			productService.addProduct(product);
		}
		catch(Exception e){
			try {
				responseJsonObject.put("upload", "failure");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return responseJsonObject.toString();
		}
		try {
			responseJsonObject.put("upload", "success");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return responseJsonObject.toString();
	}
	
	@Transactional
	@RequestMapping(value ="/product/edit", method = RequestMethod.POST)
	
	public String editProduct(@RequestBody String requestBody) {
		
		JSONObject jsonObject = null;
		JSONObject responseJsonObject = new JSONObject();
		String organizationabbr = null;
		String productName = null, newName = null;
		Product product = null;
		Organization organization =null;
		int newQuantity = 0;
		float newRate = 0;
		try {
			jsonObject = new JSONObject(requestBody);
			organizationabbr = jsonObject.getString("orgabbr");
			organization = organizationService.getOrganizationByAbbreviation(organizationabbr);
			productName = jsonObject.getString("name");
			newName = jsonObject.getString("newname");
			newQuantity = Integer.parseInt(jsonObject.getString("qty"));
			newRate = Float.parseFloat(jsonObject.getString("rate"));
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
			product = productService.getProductByNameAndOrg(productName, organization);
			product.setName(newName);
			product.setQuantity(newQuantity);
			product.setUnitRate(newRate);
		try{
			productService.addProduct(product);
		}
		catch(Exception e) {
			try {
				responseJsonObject.put("edit", "failure");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			return responseJsonObject.toString();
		}
		try {
			responseJsonObject.put("edit","success");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return responseJsonObject.toString();
	}
	
	@RequestMapping(value ="/product/delete", method = RequestMethod.POST)
	public String deleteProduct(@RequestBody String requestBody) {
		JSONObject jsonResponseObject = new JSONObject();
		String abbr = null, name = null;
		try{
			JSONObject object = new JSONObject(requestBody);
			abbr = object.getString("orgabbr");
			name = object.getString("name");
			Organization organization = organizationService.getOrganizationByAbbreviation(abbr);
			Product product = productService.getProductByNameAndOrg(name, organization);
			productService.removeProduct(product);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			try {
				jsonResponseObject.put("result", "Failed to delete");
				return jsonResponseObject.toString();
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		try {
			jsonResponseObject.put("result", "Delete successful");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponseObject.toString();
	}
}
