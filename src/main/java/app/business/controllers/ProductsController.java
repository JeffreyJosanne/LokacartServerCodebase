package app.business.controllers;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import app.business.services.OrganizationService;
import app.business.services.ProductService;
import app.entities.Organization;
import app.entities.Product;
import app.entities.ProductType;
import app.util.SpreadsheetParser;
import app.util.Utils;

@Controller
@RequestMapping("/web/{org}")
public class ProductsController {

	@Autowired
	OrganizationService organizationService;
	@Autowired
	ProductService productService;
	
	@Transactional
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@RequestMapping(value="/productsPage",method = RequestMethod.GET)
	public String productsPageInitial(@PathVariable String org, Model model) {
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		List<ProductType> productTypes = productService.getProductTypeList(organization);
		List<Product> products = productService.getProductList(productTypes);
		model.addAttribute("organization",organization);
		model.addAttribute("productTypes",productTypes);
		model.addAttribute("products",products);
		return "productList";
	}
	
	@Transactional
	@RequestMapping(value="/uploadpicture", method=RequestMethod.POST, produces = "text/plain")
	public @ResponseBody String handleFileUpload(HttpServletRequest request) {
		MultipartHttpServletRequest mRequest;
		mRequest = (MultipartHttpServletRequest) request;
		Iterator<String> itr = mRequest.getFileNames();
		
		//only one iteration i.e itr.next() as it has only one file
		MultipartFile mFile = mRequest.getFile(itr.next());
		String fileName = mFile.getOriginalFilename();
		File temp = Utils.saveFile("temp.jpg", Utils.getImageDir(), mFile);
		File serverFile = new File(Utils.getImageDir() +File.separator+ fileName);
		Random randomint = new Random();
		int flag=1;
		do{
			try 
			{
				Files.copy(temp.toPath(), serverFile.toPath());
				flag=1;
			}	
			catch (FileAlreadyExistsException e)
			{
				System.out.println("File already exist. Renaming file and trying again.");
				fileName = fileName.substring(0,fileName.length()-4);
				fileName = fileName + "_" + Integer.toString(randomint.nextInt()) + ".jpg";
				serverFile = new File(Utils.getImageDir() +File.separator+ fileName);
				flag=0;
			}
			catch (IOException e) {
				e.printStackTrace();
				flag=1;
			}
		}while(flag==0);
		
		
		String url = Utils.getImageDirURL() + fileName;
		System.out.println(url);
		return url;
	    
	}
	
	@RequestMapping(value="/statusToggle",method = RequestMethod.GET)
	public @ResponseBody String globalStatusChange(@PathVariable String org, @RequestParam(value="status") int status)
	{
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		List<Product> products = productService.getProductList(organization);
		Iterator <Product> iterator = products.iterator();
		while (iterator.hasNext()) {
			Product product = iterator.next();
			product.setStatus(status);
			productService.addProduct(product);
		}
		return null;
	}
	
	@Transactional
	@RequestMapping(value="/generatesheet", method=RequestMethod.GET,produces="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	@ResponseBody
	public File generateSheet(@PathVariable String org/*, HttpServletResponse response*/) {
		System.out.println("Controller hit");
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		//response.setHeader("Content-disposition","attachment; filename=" + organization.getAbbreviation()+"-product.xlsx");
        

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
		File file = SpreadsheetParser.generateProductSheet(prodId, prodNames, prodType, unitRate, quantity, organization.getAbbreviation());
		return file;
		/*String mimeType = new MimetypesFileTypeMap().getContentType(organization.getAbbreviation()+"-product.xlsx");
        System.out.println("MIME Type: "+mimeType);
        response.setContentType(mimeType);
        response.setContentLength((int) file.length());
        
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		OutputStream out = null;
		try {
			out = response.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] buffer= new byte[81920];
		int length = 0;

		try {
			while ((length = in.read(buffer)) > 0){
			     out.write(buffer, 0, length);
			     System.out.println("length: "+length);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/

	}

}
