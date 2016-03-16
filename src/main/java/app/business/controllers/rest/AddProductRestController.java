package app.business.controllers.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import app.business.services.OrganizationService;
import app.business.services.ProductService;
import app.business.services.ProductTypeService;
import app.entities.Organization;
import app.entities.Product;
import app.entities.ProductType;
import app.util.SpreadsheetParser;


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
	
	@Transactional
	@RequestMapping(value="/{org}/generatesheet", method=RequestMethod.GET)
	@ResponseBody
	public  FileSystemResource generateSheet(@PathVariable String org) {
		System.out.println("Controller hit");

		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		List<ProductType> productTypes = productService.getProductTypeList(organization);
		List<Product> products = productService.getProductList(productTypes);
		Iterator <Product> iterator = products.iterator();
		List <String> prodNames = new ArrayList<String>();
		List <Integer> prodId = new ArrayList<Integer>();
		List <String> prodType = new ArrayList<String>();
		List <Float> unitRate = new ArrayList<Float>();
		List <Float> quantity = new ArrayList<Float>();

		while(iterator.hasNext()) {
			Product product = iterator.next();
			prodNames.add(product.getName());
			prodId.add(product.getProductId());
			prodType.add(String.valueOf(product.getProductType().getName()));
			unitRate.add(product.getUnitRate());
			quantity.add(product.getQuantity());
		}
		File file = SpreadsheetParser.generateProductSheet(prodId, prodNames, prodType, unitRate, quantity, organization.getName());
		return new FileSystemResource(file); 
	}
	
	@Transactional
	@RequestMapping(value="/{org}/parsesheet", method=RequestMethod.GET)
	public void parseSheet(@PathVariable String org) {
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		HashMap<Integer, String []> data = SpreadsheetParser.parseProductSheet(organization.getAbbreviation());
		System.out.println("Data Size: "+data.size());
		for (int i =0;i<data.size();i++) {
			String []contents = data.get(i);
			int id = Integer.parseInt(contents[0]);
			String name = contents[1];
			String type = contents[2];
			float rate = Float.parseFloat(contents[3]);
			float quantity = Float.parseFloat(contents[4]);
			if (id ==0) {
				//new product
				System.out.println("New product");
				Product product = new Product();
				ProductType productType = productTypeService.getByOrganizationAndName(organization, type);
				product.setName(name);
				product.setUnitRate(rate);
				product.setProductType(productType);
				product.setQuantity(quantity);
				try {
					productService.addProduct(product);
				}
				catch(Exception e) {
					System.out.println("cannot add");
				}
				
			}
			else if ( id != 0) {
				//Edit product
				System.out.println("in edit prodcut");
				Product product = productService.getProductById(id);
				int flag = 0;
				if (!name.equals(product.getName())) {
					product.setName(name);
					flag =1;
				}
				if (rate != product.getUnitRate()){
					product.setUnitRate(rate);
					flag=1;
				}
				if (flag == 1 ) {
					try {
						productService.addProduct(product);
					}
					catch(Exception e) {
						System.out.println("cannot update");
					}
				}
			}
			
			
			
		}
	}

}
