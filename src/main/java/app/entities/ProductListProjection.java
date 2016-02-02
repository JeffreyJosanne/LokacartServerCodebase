package app.entities;

import org.springframework.data.rest.core.config.Projection;

@Projection(name = "productproj", types = { Product.class }) 
public interface ProductListProjection {

	int getProductId();
	String getName();
	float getQuantity();
	float getUnitRate();
	public String getImageUrl();
	int getStatus();

}