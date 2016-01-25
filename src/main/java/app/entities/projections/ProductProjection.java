package app.entities.projections;

import org.springframework.data.rest.core.config.Projection;

import app.entities.Product;
import app.entities.ProductType;

@Projection(name = "default", types = { Product.class })
public interface ProductProjection {
	  String getName();
	  int getProductId();
	  String getDescription();
	  String getImageUrl();
	  float getQuantity();
	  float getUnitRate();
	  ProductType getProductType();
	 
}