package app.business.controllers.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.business.services.OrganizationService;
import app.business.services.ProductService;
import app.business.services.ProductTypeService;
import app.business.services.UserService;
import app.data.repositories.OrganizationRepository;
import app.entities.Organization;
import app.entities.PresetQuantity;
import app.entities.Product;
import app.entities.ProductType;

@RestController
@RequestMapping("/api/products/search/byType")
public class ProductTypeRestController {
	
	@Autowired
	OrganizationService organizationService;
	@Autowired
	ProductTypeService productTypeService;
	@Autowired
	UserService userService;
	@Autowired
	OrganizationRepository organizationRepository;
	@Autowired
	ProductService productService;
	
	@RequestMapping(value = "/map",method = RequestMethod.GET )
	public HashMap<String, List<HashMap<String, String>>> productListByType(@RequestParam(value="orgabbr") String orgabbr){
		List<ProductType> productTypeList=productTypeService.getProductTypeListByOrgAbbr(orgabbr);
		List<Product> productList = productService.getProductList(organizationService.getOrganizationByAbbreviation(orgabbr));
		HashMap<String, List<HashMap<String, String>>> productTypeMap = new HashMap<String, List<HashMap<String, String>>>();
		Boolean stockManagement = organizationService.getOrganizationByAbbreviation(orgabbr).getStockManagement();
		for (ProductType ptype: productTypeList)
		{
			//List<Object[]> iter= new ArrayList<Object[]>();
			List<HashMap<String, String>> Listmap=new ArrayList<HashMap<String, String>>();
			HashMap<String, HashMap<String, String>> productmap=new HashMap<String, HashMap<String, String>>();
			for(Product product: productList)
			{
				HashMap<String, String> map=new HashMap<String, String>();
				if(ptype.getName().equals(product.getProductType().getName()) && (product.getStatus() == 1))
				{
					map.put("id", Integer.toString(product.getProductId()));
					map.put("name", product.getName());
					map.put("quantity", Float.toString(product.getQuantity()));
					map.put("unitRate", Float.toString(product.getUnitRate()));
					map.put("imageUrl", product.getImageUrl());
					map.put("stockManagement", stockManagement.toString());
					Listmap.add(map);
				}
			}
			productTypeMap.put(ptype.getName(), Listmap);
		}
		return productTypeMap;
	}
	@RequestMapping(value = "/byname",method = RequestMethod.POST ,produces="application/json" )
	public String updatedPrice(@RequestBody String requestBody)
	{
		JSONObject responseJsonObject = new JSONObject();
		JSONObject jsonObject=null;
		JSONArray productListJsonArray = null;
		JSONArray proArray= new JSONArray();
		int org_id = 0;
		int flag=0;
		try {
			jsonObject = new JSONObject(requestBody);
			org_id = jsonObject.getInt("org_id");
			productListJsonArray = jsonObject.getJSONArray("product_list");
			responseJsonObject.put("status", "updated");
			Organization organization= organizationRepository.findOne(org_id); //Finding organization of the product name
			Boolean stockManagement = organization.getStockManagement();
			responseJsonObject.put("stockManagement",stockManagement.toString());
			for (int i=0;i<productListJsonArray.length();i++)
			{
				 try {
					JSONObject productObj = productListJsonArray.getJSONObject(i);
					String product_name = productObj.getString("product_name");
					float price = Float.parseFloat((productObj.getString("price")));
					if(organization==null)
					{
						responseJsonObject.put("Status", "Failure");
						responseJsonObject.put("Error", "Organization with Id "+org_id+" does not exists");
						return responseJsonObject.toString();
					}
					else
					{
						Product product = productService.getProductByNameAndOrg(product_name, organization); 
						JSONObject up_product= new JSONObject();
						
							flag=1;
							up_product.put("product_name", product.getName());
							up_product.put("price", product.getUnitRate());
							up_product.put("quantity", product.getQuantity());
							proArray.put(up_product);
							responseJsonObject.put("status", "updated");
							responseJsonObject.put("products", proArray);
						
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if(flag==0)
				responseJsonObject.put("status", "no change");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return responseJsonObject.toString();
	}		
}
