package app.entities.projections;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.rest.core.config.Projection;
import org.springframework.data.rest.core.config.Projection;

import app.entities.Order;
import app.entities.OrderItem;

@Projection(name = "default", types = { Order.class,OrderItem.class })
public interface OrderProjectionApp {
	
	@Value("#{target.getOrderId()}")
	int getOrderId();
	
	Timestamp getAutolockTime();
	
	boolean getIsLocked();
	
	String getStatus();
	
	@Value("#{target.getMessage().getTime()}")
	Timestamp getRegisteredTime();
	
	@Value("#{target.getOrganization().getStockManagement()}")
	Boolean getStockManagement();
	
	@Value("#{target.getOrderItemsHashMap()}")
	List<HashMap<String,String>> getOrderItems();
	
}