package app.business.controllers.rest;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.transaction.Transactional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.business.services.GcmTokensService;
import app.business.services.OrderService;
import app.business.services.OrganizationMembershipService;
import app.business.services.OrganizationService;
import app.business.services.PresetQuantityService;
import app.business.services.ProductService;
import app.business.services.UserPhoneNumberService;
import app.business.services.UserService;
import app.business.services.message.MessageService;
import app.data.repositories.BillLayoutSettingsRepository;
import app.data.repositories.BinaryMessageRepository;
import app.data.repositories.GroupRepository;
import app.data.repositories.OrderItemRepository;
import app.data.repositories.OrderRepository;
import app.data.repositories.OrganizationRepository;
import app.entities.BillLayoutSettings;
import app.entities.GcmTokens;
import app.entities.Group;
import app.entities.Order;
import app.entities.OrderItem;
import app.entities.Organization;
import app.entities.OrganizationMembership;
import app.entities.PresetQuantity;
import app.entities.Product;
import app.entities.User;
import app.entities.message.BinaryMessage;
import app.entities.message.Message;
import app.util.GcmRequest;
import app.util.SendBill;
import app.util.SendMail;

@RestController
@RequestMapping("/api")
public class OrderRestController {
	
	@Autowired
	OrganizationService organizationService;
	
	@Autowired
	MessageService messageService;
	
	@Autowired
	PresetQuantityService presetQuantityService;
	
	@Autowired
	ProductService productService;
	
	@Autowired
	OrderRepository orderRepository;
	
	@Autowired
	OrganizationRepository organizationRepository;
	
	@Autowired
	OrderItemRepository orderItemRepository;
	
	@Autowired
	BinaryMessageRepository binaryMessageRepository;
	
	@Autowired
	GroupRepository groupRepository;

	@Autowired
	UserService userService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	BillLayoutSettingsRepository billLayoutSettingsRepository;
	
	@Autowired 
	UserPhoneNumberService userPhoneNumberService;
	
	@Autowired
	OrganizationMembershipService organizationMembershipService;
	
	@Autowired
	GcmTokensService gcmTokensService;
	
	
		public HashMap<String, Integer> dashBoardLocal(String orgabbr) throws ParseException {

		Organization organization = organizationService.getOrganizationByAbbreviation("Test2");
		Group g= organizationService.getParentGroup(organization);
		List<Message> messageapppro=messageService.getMessageListByOrderStatus(g, "binary", "processed");
		List<Message> messageappnew=messageService.getMessageListByOrderStatus(g, "binary", "saved");
		List<Message> messageappcan=messageService.getMessageListByOrderStatus(g, "binary", "cancelled");
		HashMap<String, Integer> dashmap = new HashMap<String, Integer>();
		dashmap.put("saved", messageappnew.size());
		dashmap.put("processed", messageapppro.size());
		dashmap.put("cancelled", messageappcan.size());
		
		List<OrganizationMembership> membershipListpending = organizationMembershipService.getOrganizationMembershipListByStatus(organization, 0);
		List<OrganizationMembership> membershipListapproved = organizationMembershipService.getOrganizationMembershipListByStatus(organization, 1);
		dashmap.put("totalUsers", membershipListpending.size()+membershipListapproved.size());
		dashmap.put("pendingUsers", membershipListpending.size());
		int todayUsers=0;
		for(OrganizationMembership membership : membershipListpending)
		{

			User user = membership.getUser();
		
			try
			{
				Timestamp time = user.getTime();
				
				Calendar cal= Calendar.getInstance();
				cal.clear(Calendar.HOUR_OF_DAY);
				cal.clear(Calendar.AM_PM);
				cal.clear(Calendar.MINUTE);
				cal.clear(Calendar.SECOND);
				cal.clear(Calendar.MILLISECOND);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");      
			    java.util.Date dateWithoutTime = sdf.parse(sdf.format(new java.util.Date()));
				if(time.after(dateWithoutTime))
				{
					todayUsers=todayUsers+1;
				}
			}
			catch(NullPointerException | ParseException e)
			{
				System.out.println("User name not having his timestamp recorded is: " + user.getName() + " having userID: " + user.getUserId());
			}
		}
		dashmap.put("newUsersToday",todayUsers);
		return dashmap;
	}
	
	public List <String> getTargetDevices (Organization organization)  {
			
		List<OrganizationMembership> organizationMembership = organizationMembershipService.getOrganizationMembershipListByIsAdmin(organization, true);
		List<String> phoneNumbers = new ArrayList<String>();
		Iterator <OrganizationMembership> membershipIterator = organizationMembership.iterator();
		while (membershipIterator.hasNext()) {
			OrganizationMembership membership = membershipIterator.next();
			User user = membership.getUser();
			phoneNumbers.add(userPhoneNumberService.getUserPrimaryPhoneNumber(user).getPhoneNumber());
		}
		Iterator <String> iterator = phoneNumbers.iterator();
		List <String>androidTargets = new ArrayList<String>();
		while(iterator.hasNext()) {
			String number = iterator.next();
			try{
			List<GcmTokens> gcmTokens = gcmTokensService.getListByPhoneNumber(number);
			Iterator <GcmTokens> iter = gcmTokens.iterator();
			while(iter.hasNext()) {
			
			androidTargets.add(iter.next().getToken());
			}
			}
			catch(Exception e){
				System.out.println("no token for number: "+number);
			}
		}
		return androidTargets;
	}
	
	@Transactional
	
	@RequestMapping(value = "/orders/add",method = RequestMethod.POST )
	public HashMap<String,String> addOrders(@RequestBody String requestBody){
		HashMap<String,String> response= new HashMap<String, String>();
		JSONObject jsonObject = null;
		String organizationabbr = null;
		String groupname=null;
		try {
			jsonObject = new JSONObject(requestBody);
			organizationabbr=jsonObject.getString("orgabbr");
			groupname=jsonObject.getString("groupname");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Order order = new Order();
		Organization organization= organizationRepository.findByAbbreviation(organizationabbr);
		order.setOrganization(organization);
		order.setStatus("saved");
		order=orderRepository.save(order);
		List<OrderItem> orderItems= new ArrayList<OrderItem>();
		try {
			JSONArray orderItemsJSON = jsonObject.getJSONArray("orderItems");
			for (int i = 0; i < orderItemsJSON.length(); i++) {
			    OrderItem orderItem= new OrderItem();
				JSONObject row = orderItemsJSON.getJSONObject(i);
			    String productname=row.getString("name");
			    float productQuantity =(float)row.getDouble("quantity");
			    Product product=productService.getProductByNameAndOrg(productname,organization);
			    orderItem.setProduct(product);
			    orderItem.setQuantity(productQuantity);	
			    orderItem.setUnitRate(product.getUnitRate());
			    orderItem.setOrder(order);
			    orderItem=orderItemRepository.save(orderItem);
			    orderItems.add(orderItem);
			}
			
			System.out.println(orderItemsJSON.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		order.setOrderItems(orderItems);
		BinaryMessage bmessage= new BinaryMessage();
		bmessage.setTime(new Timestamp((new Date()).getTime()));
		bmessage.setOrder(order);
		bmessage.setGroup(groupRepository.findByNameAndOrganization(groupname, organization));
		bmessage.setUser( userService.getCurrentUser());
		bmessage.setMode("app");
		bmessage.setType("order");
		binaryMessageRepository.save(bmessage);
		order.setMessage(bmessage);
		orderRepository.save(order);
		response.put("orderId",new Integer(order.getOrderId()).toString());
		response.put("Status", "Success");
		String email=order.getMessage().getUser().getEmail();
		List <String> androidTargets = getTargetDevices(organization);
		if(androidTargets.size()>0) {
		GcmRequest gcmRequest = new GcmRequest();
		HashMap<String,Integer>dashData = null;
		try {
			dashData = dashBoardLocal(organizationabbr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		gcmRequest.broadcast(userService.getCurrentUser().getName()+" has placed an order", "New order", androidTargets,0);
		System.out.println(organizationabbr);
		gcmRequest.broadcast(androidTargets,organizationabbr,dashData);
		}
		SendMail.sendMail(email, "Cottage Industry App: Order Placed Successfully" , "Your order has been placed successfully with order id is: " + new Integer(order.getOrderId()).toString());
		if(organization.getAbbreviation().equals("NatG"))
			SendMail.sendMail("vishalghodke@gmail.com", "Cottage Industry App: Order Placed", "Dear Admin,\nCustomer "+order.getMessage().getUser().getName()+" has placed an order of order id "+order.getOrderId()+".\n\nThankyou\nLokacart Team\n");
		return response;
	}

	

	@Transactional
	@RequestMapping(value = "/orders/update/{orderId}",method = RequestMethod.POST ,produces="application/json" )
	public String updateOrders(@PathVariable int orderId,@RequestBody String requestBody)
	{
		JSONObject response= new JSONObject();
		JSONObject jsonObject = null;
		String status=null;
		String comments=null;
		String orgabr=null;
		JSONArray orderItemsJSON = null;
		try {
			jsonObject = new JSONObject(requestBody);
			status=jsonObject.getString("status");
			comments=jsonObject.getString("comments");
			orderItemsJSON = jsonObject.getJSONArray("orderItems");
			//orgabr= jsonObject.getString("orgabbr");
		} catch (JSONException e) {
			//Uncaught but not untamed :-)			
			//return "Error";
		}
		if(orderRepository.findOne(orderId)==null)
		{
			try {
				response.put("Status", "Error");
				response.put("Error", "No Order of the following Id found");
				return response.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}	
		Order order = orderRepository.findOne(orderId);
		Organization organization= order.getOrganization();
		String organizationabbr = organization.getAbbreviation();
		if( order.getStatus().equals("processed")) {
			try {
				response.put("Status", "Order cannot be cancelled. Please sync app immediately");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return response.toString();
		}
		//Will be used later when comments are added while ordering.
		if(!comments.equals("null"))
		{
			BinaryMessage message=(BinaryMessage)order.getMessage();
			message.setComments(comments);
			binaryMessageRepository.save(message);
		}
		System.out.println(order.getStatus());
		if( order.getStatus().equals("processed")) {
			try {
				System.out.println("inside");
				response.put("Status", "Order cannot be cancelled. Please sync app immediately");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return response.toString();
		}
		if(orderItemsJSON!=null)
		{
			for( OrderItem orderitem : order.getOrderItems())
			{
				orderItemRepository.delete(orderitem);
			}
			List<OrderItem> orderItems= new ArrayList<OrderItem>();
			try {
				orderItemsJSON = jsonObject.getJSONArray("orderItems");
				for (int i = 0; i < orderItemsJSON.length(); i++) {
				    OrderItem orderItem= new OrderItem();
					JSONObject row = orderItemsJSON.getJSONObject(i);
				    String productName=row.getString("name");
				    float productQuantity =(float)row.getDouble("quantity");
				    Product product=productService.getProductByNameAndOrg(productName, organization);
				    orderItem.setProduct(product);
				    orderItem.setQuantity(productQuantity);	
				    orderItem.setUnitRate(product.getUnitRate());
				    orderItem.setOrder(order);
				    orderItem=orderItemRepository.save(orderItem);
				    orderItems.add(orderItem);
				}
				System.out.println(orderItemsJSON.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			order.setOrderItems(orderItems);
		}
		try {
			response.put("Status", "Success");
			order.setStatus(status);
			if(status.equals("cancelled"))
			{
				SendMail.sendMail(order.getMessage().getUser().getEmail(), "Cottage Industry App: Order Cancellation Acknowledgement", "Dear User,\nYour order with order id "+order.getOrderId()+" has been successfully cancelled.\nWe hope to serve you again.");
				if(organization.getAbbreviation().equals("NatG"))
					SendMail.sendMail("vishalghodke@gmail.com", "Cottage Industry App: Order Cancellation", "Dear Admin,\nCustomer "+order.getMessage().getUser().getName()+" has cancelled the order of order id "+order.getOrderId()+".\n\nThankyou\nLokacart Team\n");
			}
			else {
				SendMail.sendMail(order.getMessage().getUser().getEmail(), "Cottage Industry App: Order Modification Acknowledgement", "Dear User,\nYour order with order id "+order.getOrderId()+" has been successfully modified on "+order.getMessage().getTime());
				order.getMessage().setTime(new Timestamp((new Date()).getTime()));
				binaryMessageRepository.save((BinaryMessage)order.getMessage());
				if(organization.getAbbreviation().equals("NatG"))
					SendMail.sendMail("vishalghodke@gmail.com", "Cottage Industry App: Order Modification", "Dear Admin,\nCustomer "+order.getMessage().getUser().getName()+" has modified the order of order id "+order.getOrderId()+".\n\nThankyou\nLokacart Team\n");
			}
			orderRepository.save(order);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		List <String> androidTargets = getTargetDevices(organization);
		if(androidTargets.size() > 0) {
			GcmRequest gcmRequest = new GcmRequest();
	
			HashMap<String, Integer> dashData = null;
			try {
				dashData = dashBoardLocal(organizationabbr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			gcmRequest.broadcast(androidTargets,organizationabbr,dashData);			
			}
		return response.toString();
	}
	
	@Transactional
	@RequestMapping(value = "/orders/updatestatus/{orderId}",method = RequestMethod.GET, produces="text/plain" )
	public String updateOrderStatus(@PathVariable int orderId)
	{
		System.out.println("In update status with order id = "+orderId);
		Order order = orderRepository.findOne(orderId);
		Organization organization= order.getOrganization();
		String organizationabbr = organization.getAbbreviation();
		if(order.getStatus().equals("processed"))
		{
			System.out.println("In processed");
			BillLayoutSettings billLayoutSetting = billLayoutSettingsRepository.findByOrganization(organization);
			SendBill.sendMail(order, organization, billLayoutSetting);
		}
		else if (order.getStatus().equals("saved"))
		{
			System.out.println("In saved");
			SendMail.sendMail(order.getMessage().getUser().getEmail(), "Cottage Industry App: Order recieved", "Organization "+organization.getName()+" has recieved your order. Order processing might take couple of days. Once your order is processed, you will receive an email confirming the same.\n\n Thank you for shopping with us.");
		}
		System.out.println("Mail sent");
		List <String> androidTargets = getTargetDevices(organization);
		if(androidTargets.size() > 0) {
			GcmRequest gcmRequest = new GcmRequest();

			HashMap<String, Integer> dashData = null;
			try {
				dashData = dashBoardLocal(organizationabbr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			gcmRequest.broadcast(androidTargets,organizationabbr,dashData);			}
		return "Success";
	}
	
	@Transactional
	@RequestMapping(value="/orders/get", method = RequestMethod.GET)
	public String displayAllOrders(@RequestParam(value="orgabbr") String orgabbr)
	{
		JSONObject jsonResponseObject = new JSONObject();
		JSONArray savedArray = new JSONArray();
		JSONArray processedArray = new JSONArray();
		JSONArray cancelledArray = new JSONArray();
		Organization organization =organizationService.getOrganizationByAbbreviation(orgabbr);
		List<Order> orderList = orderService.getOrderByOrganization(organization);
		Iterator <Order> iterator = orderList.iterator();
		while(iterator.hasNext()) {
			Order order = iterator.next();
			Message message = messageService.getMessageFromOrder(order);
			JSONObject orderObject = new JSONObject();
			if(message != null){
				try {
					orderObject.put("orderid",order.getOrderId());
					orderObject.put("timestamp", message.getTime().toString());
					orderObject.put("username", userService.getUser(message.getUser().getUserId()).getName());
					orderObject.put("comment", message.getComments());
					if(order.getStatus().equals("saved")){
						savedArray.put(orderObject);
					}
					else if (order.getStatus().equals("processed")){
						processedArray.put(orderObject);
					}
					else if (order.getStatus().equals("cancelled")) {
						cancelledArray.put(orderObject);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				}
			
		}
		try{
		jsonResponseObject.put("saved", savedArray);
		jsonResponseObject.put("processed", processedArray);
		jsonResponseObject.put("cancelled", cancelledArray);
		jsonResponseObject.put("response", "success");
		}
		catch(JSONException e){
			e.printStackTrace();
			try {
				jsonResponseObject.put("response", "error");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return jsonResponseObject.toString();
		}
		return jsonResponseObject.toString();
	}
	
	@Transactional
	@RequestMapping(value = "/orders/saved",method = RequestMethod.GET )
	public String displaySavedOrders(@RequestParam(value="orgabbr") String orgabbr)
	{
		JSONObject jsonResponseObject = new JSONObject();
		Organization organization =organizationService.getOrganizationByAbbreviation(orgabbr);
		List<Order> orderList = orderService.getOrderByOrganizationSaved(organization);
		JSONArray orderArray = new JSONArray();
		Iterator<Order> iterator = orderList.iterator();
		while(iterator.hasNext())
		{
			JSONObject orderObject = new JSONObject();
			Order order = iterator.next();
			Message message = messageService.getMessageFromOrder(order);
			if(message != null){
			try {
				orderObject.put("orderid",order.getOrderId());
				orderObject.put("timestamp", message.getTime().toString());
				orderObject.put("username", userService.getUser(message.getUser().getUserId()).getName());
				orderObject.put("comment", message.getComments());
				JSONArray items = new JSONArray();
				List<OrderItem> orderItems = order.getOrderItems();
				Iterator<OrderItem> iter = orderItems.iterator();
				while(iter.hasNext()){
					OrderItem orderItem = iter.next();
					JSONObject item = new JSONObject();
					try {
				
						item.put("productname", orderItem.getProduct().getName());
						item.put("quantity", Float.toString(orderItem.getQuantity()));
						item.put("rate", Float.toString(orderItem.getUnitRate()));
						items.put(item);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					items.put(item);
				}
				orderObject.put("items", items);
				orderArray.put(orderObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			}
		}
		try {
			jsonResponseObject.put("orders",orderArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponseObject.toString();
	}
	
	@Transactional
	@RequestMapping(value = "/orders/processed",method = RequestMethod.GET )
	public String displayProcessedOrders(@RequestParam(value="orgabbr") String orgabbr)
	{
		
		Organization organization =organizationService.getOrganizationByAbbreviation(orgabbr);
		List<Order> orderList = orderService.getOrderByOrganizationProcessed(organization);
		JSONObject jsonResponseObject = new JSONObject();
		JSONArray orderArray = new JSONArray();
		Iterator<Order> iterator = orderList.iterator();
		while(iterator.hasNext())
		{
			JSONObject orderObject = new JSONObject();
			Order order = iterator.next();
			Message message = messageService.getMessageFromOrder(order);
			if(message != null){
			try {
				orderObject.put("orderid",order.getOrderId());
				orderObject.put("timestamp", message.getTime().toString());
				orderObject.put("username", userService.getUser(message.getUser().getUserId()).getName());
				orderObject.put("comment", message.getComments());
				JSONArray items = new JSONArray();
				List<OrderItem> orderItems = order.getOrderItems();
				Iterator<OrderItem> iter = orderItems.iterator();
				while(iter.hasNext()){
					OrderItem orderItem = iter.next();
					JSONObject item = new JSONObject();
					try {
						item.put("productname", orderItem.getProduct().getName());
						item.put("quantity", Float.toString(orderItem.getQuantity()));
						item.put("rate", Float.toString(orderItem.getUnitRate()));
						items.put(item);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					items.put(item);
				}
				orderObject.put("items", items);
				orderArray.put(orderObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			}
		}
		try {
			jsonResponseObject.put("orders",orderArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponseObject.toString();
	}
	
	@Transactional
	@RequestMapping(value = "/orders/cancelled",method = RequestMethod.GET )
	public String displayCancelledOrders(@RequestParam(value="orgabbr") String orgabbr)
	{
		
		Organization organization =organizationService.getOrganizationByAbbreviation(orgabbr);
		List<Order> orderList = orderService.getOrderByOrganizationCancelled(organization);
		JSONObject jsonResponseObject = new JSONObject();
		JSONArray orderArray = new JSONArray();
		Iterator<Order> iterator = orderList.iterator();
		while(iterator.hasNext())
		{
			JSONObject orderObject = new JSONObject();
			Order order = iterator.next();
			Message message = messageService.getMessageFromOrder(order);
			if(message != null){
			try {
				orderObject.put("orderid",order.getOrderId());
				orderObject.put("timestamp", message.getTime().toString());
				orderObject.put("username", userService.getUser(message.getUser().getUserId()).getName());
				orderObject.put("comment", message.getComments());
				JSONArray items = new JSONArray();
				List<OrderItem> orderItems = order.getOrderItems();
				Iterator<OrderItem> iter = orderItems.iterator();
				while(iter.hasNext()){
					OrderItem orderItem = iter.next();
					JSONObject item = new JSONObject();
					try {
						item.put("productname", orderItem.getProduct().getName());
						item.put("quantity", Float.toString(orderItem.getQuantity()));
						item.put("rate", Float.toString(orderItem.getUnitRate()));
						items.put(item);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					items.put(item);
				}
				orderObject.put("items", items);
				orderArray.put(orderObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			}
		}
		try {
			jsonResponseObject.put("orders",orderArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponseObject.toString();
	}
	
	@Transactional
	@RequestMapping(value = "/orders/rejected",method = RequestMethod.GET )
	public String displayRejectedOrders(@RequestParam(value="orgabbr") String orgabbr)
	{
		
		Organization organization =organizationService.getOrganizationByAbbreviation(orgabbr);
		List<Order> orderList = orderService.getOrderByOrganizationRejected(organization);
		JSONObject jsonResponseObject = new JSONObject();
		JSONArray orderArray = new JSONArray();
		Iterator<Order> iterator = orderList.iterator();
		while(iterator.hasNext())
		{
			JSONObject orderObject = new JSONObject();
			Order order = iterator.next();
			Message message = messageService.getMessageFromOrder(order);
			if(message != null){
			try {
				orderObject.put("orderid",order.getOrderId());
				orderObject.put("timestamp", message.getTime().toString());
				orderObject.put("username", userService.getUser(message.getUser().getUserId()).getName());
				orderObject.put("comment", message.getComments());
				orderArray.put(orderObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			}
		}
		try {
			jsonResponseObject.put("orders",orderArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jsonResponseObject.toString();
	}
	
	
	@RequestMapping(value = "/orders/changestate/processed/{orderId}",method = RequestMethod.GET )
	public String changeToProcessedState(@PathVariable int orderId) {
		JSONObject responseJsonObject = new JSONObject();
		Order order = orderService.getOrder(orderId);
		order.setStatus("processed");
		Organization organization = order.getOrganization();
		String organizationabbr = organization.getAbbreviation();
		try {
		orderService.processOrder(order);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try {
				return responseJsonObject.put("result", "failure").toString();
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	try {
		responseJsonObject.put("result", "success");
	} catch (JSONException e) {
		e.printStackTrace();
	}	
	List <String> androidTargets = getTargetDevices(organization);
	if(androidTargets.size() > 0) {
		GcmRequest gcmRequest = new GcmRequest();
		HashMap<String, Integer> dashData = null;
		try {
			dashData = dashBoardLocal(organizationabbr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		gcmRequest.broadcast(androidTargets,organizationabbr,dashData);		}
	return responseJsonObject.toString();
	}
	
	@Transactional
	@RequestMapping(value = "/orders/changestate/cancelled/{orderId}",method = RequestMethod.GET )
	public String changeToCancelledState(@PathVariable int orderId) {
		JSONObject responseJsonObject = new JSONObject();
		Order order = orderService.getOrder(orderId);
		order.setStatus("cancelled");
		Organization organization = order.getOrganization();
		String organizationabbr = organization.getAbbreviation();
		try {
		orderService.cancelOrder(order);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try {
				return responseJsonObject.put("result", "failure").toString();
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	try {
		responseJsonObject.put("result", "success");
	} catch (JSONException e) {
		e.printStackTrace();
	}	
	List <String> androidTargets = getTargetDevices(organization);
	if(androidTargets.size() > 0) {
		GcmRequest gcmRequest = new GcmRequest();
	
		HashMap<String, Integer> dashData = null;
		try {
			dashData = dashBoardLocal(organizationabbr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		gcmRequest.broadcast(androidTargets,organizationabbr,dashData);
		}
	return responseJsonObject.toString();
	}
	
	@Transactional
	@RequestMapping(value = "/orders/changestate/rejected/{orderId}",method = RequestMethod.GET )
	public String changeToRejectedState(@PathVariable int orderId) {
		JSONObject responseJsonObject = new JSONObject();
		Order order = orderService.getOrder(orderId);
		order.setStatus("rejected");
		Organization organization = order.getOrganization();
		String organizationabbr = organization.getAbbreviation();
		try {
		orderService.rejectOrder(order);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try {
				return responseJsonObject.put("result", "failure").toString();
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	try {
		responseJsonObject.put("result", "success");
	} catch (JSONException e) {
		e.printStackTrace();
	}	
	
	List <String> androidTargets = getTargetDevices(organization);
	if(androidTargets.size() > 0) {
	GcmRequest gcmRequest = new GcmRequest();
	HashMap<String, Integer> dashData = null;
	try {
		dashData = dashBoardLocal(organizationabbr);
	} catch (ParseException e) {
		e.printStackTrace();
	}
	gcmRequest.broadcast(androidTargets,organizationabbr,dashData);
	}
	return responseJsonObject.toString();
	}
	
	
	
	
	@RequestMapping(value = "/orders/details/{orderId}",method = RequestMethod.GET )
	public String getOrderDetails(@PathVariable int orderId) {
		JSONObject responseJsonObject = new JSONObject();
		JSONArray items = new JSONArray();
		Order order = orderService.getOrder(orderId);
		List<OrderItem> orderItems = order.getOrderItems();
		Iterator<OrderItem> iterator = orderItems.iterator();
		while(iterator.hasNext()) {
			OrderItem orderItem = iterator.next(); 
			JSONObject item = new JSONObject();
			try {
				item.put("productname", orderItem.getProduct().getName());
				item.put("quantity", Float.toString(orderItem.getQuantity()));
				item.put("rate", Float.toString(orderItem.getUnitRate()));
				items.put(item);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		try {
			responseJsonObject.put("items", items);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return responseJsonObject.toString();
	}
	
	@RequestMapping(value = "/presetquantities",method = RequestMethod.GET )
	public String getPresetQuantity(@RequestParam (value="orgabbr") String orgabbr) {
		Organization organization = organizationService.getOrganizationByAbbreviation(orgabbr);
		JSONObject jsonResponseObject = new JSONObject();
		JSONArray array = new JSONArray();
		List <PresetQuantity> presetQty = presetQuantityService.getPresetQuantityList(organization);
		Iterator <PresetQuantity> iterator = presetQty.iterator();
		while(iterator.hasNext()) {
			JSONObject qty = new JSONObject();
			PresetQuantity presetQuantity = iterator.next();
			try {
				qty.put("producttype",presetQuantity.getProductType().getName());
				qty.put("qty", presetQuantity.getQuantity());
				array.put(qty);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		try {
			jsonResponseObject.put("presetquantity", array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonResponseObject.toString();
		
	}
	 
	
}