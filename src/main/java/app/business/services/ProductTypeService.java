package app.business.services;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.List;

import app.data.repositories.ProductTypeRepository;
import app.entities.Organization;
import app.entities.ProductType;

public class ProductTypeService {
	
	@Autowired
	ProductTypeRepository productTypeRepository;
	
	void addProductType(ProductType productType){
		productTypeRepository.save(productType);
		
	}
	
	void removeProductType(ProductType productType){
		productTypeRepository.delete(productType);
	}
	
	void getProductTypeById(int productTypeId){
		productTypeRepository.findOne(productTypeId);
		
	}
	
	public List<ProductType> getAllProductTypeList(){
		return (new ArrayList<ProductType>(productTypeRepository.findAll()));
		
	}
	public List<ProductType> getAllProductTypeListSortedByName(){
		/*
		 * The query can also be done by the statement new Sort(Sort.Direction.ASC, "name") in the repository as well. 
		 */
		return productTypeRepository.findAllByOrderByNameAsc();
	}

}
