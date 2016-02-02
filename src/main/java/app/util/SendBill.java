package app.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;

import app.business.services.BillLayoutSettingsService;
import app.business.services.OrderService;
import app.business.services.OrganizationService;
import app.data.repositories.BillLayoutSettingsRepository;
import app.data.repositories.BinaryMessageRepository;
import app.data.repositories.OrderRepository;
import app.entities.BillLayoutSettings;
import app.entities.Order;
import app.entities.OrderItem;
import app.entities.Organization;
import app.entities.Product;

public class SendBill {
	
	
	
	public static int sendMail(Order order, Organization organization, BillLayoutSettings billLayoutSetting){
		
		
		
		float totalCost = 0; 
		for(OrderItem orderItem : order.getOrderItems()) {
			totalCost += orderItem.getUnitRate() * orderItem.getQuantity();
		}
	
		
		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		  // Get a Properties object
		     Properties props = System.getProperties();
		     props.setProperty("mail.smtp.host", "10.129.1.1");
		     props.put("mail.smtp.starttls.enable","false");
		     props.put("mail.smtp.ssl.trust", "10.129.1.1");
		     props.setProperty("mail.smtp.port", "25");
		     props.setProperty("mail.smtp.connectiontimeout", "30000");
		     props.setProperty("mail.smtp.timeout", "30000");
		     props.setProperty("mail.smtp.auth", "true");
		     props.setProperty("mail.debug", "true");
		     props.setProperty("mail.debug.auth", "true");
		     props.setProperty("mail.store.protocol", "pop3");
		     props.setProperty("mail.transport.protocol", "smtp");
		     final String username = "username";
		     final String password = "password";
		     try{
		    	 
		     Session session = Session.getDefaultInstance(props, 
		                          new Authenticator(){
		                             protected PasswordAuthentication getPasswordAuthentication() {
		                                return new PasswordAuthentication(username, password);
		                             }});

		   // -- Create a new message --
		     Message msg = new MimeMessage(session);

		  // -- Set the FROM and TO fields --
		     msg.setFrom(new InternetAddress("lokacart@cse.iitb.ac.in"));
		     msg.setRecipients(Message.RecipientType.TO, 
		                      InternetAddress.parse(order.getMessage().getUser().getEmail(),false));
		     msg.setSubject("Cottage Industry App Order Bill");
		     String rows= "";
		     double total= 0;
		     HashMap<String,OrderItem> map= new HashMap<String,OrderItem>();
		     for(OrderItem orderitem: order.getOrderItems())
	    	 {
	    		 if(!map.containsKey(orderitem.getProduct().getName()))
	    			 map.put(orderitem.getProduct().getName(), orderitem);
	    		 else
	    		 {
	    			 OrderItem orderItem=new OrderItem();
	    			 float qty=map.get(orderitem.getProduct().getName()).getQuantity()+orderitem.getQuantity();
	    			 orderItem.setOrder(order);
	    			 orderItem.setProduct(orderitem.getProduct());
	    			 orderItem.setQuantity(qty);
	    			 orderItem.setUnitRate(orderitem.getUnitRate());
	    			 map.put(orderitem.getProduct().getName(),orderItem);
	    		 }
	    	 }
		     Set<String> product= map.keySet();
		     Iterator<String> i= product.iterator();
		     while(i.hasNext())
		     {
		    	 OrderItem orderitem= map.get(i.next());
		    	 rows = rows+"<tr>";
		    	 rows = rows + "<td style=\"text-align:right\">"+orderitem.getProduct().getName()+"</td>";
		    	 rows = rows + "<td style=\"text-align:right\">"+orderitem.getQuantity()+"</td>";
		    	 rows = rows + "<td style=\"text-align:right\">"+orderitem.getUnitRate()+"</td>";
		    	 rows = rows + "<td style=\"text-align:right\">"+(orderitem.getQuantity()*orderitem.getUnitRate())+"</td></tr>";
		    	 total = total + orderitem.getQuantity()*orderitem.getUnitRate();
		     }
		    msg.setContent("<html>"+
			"<head>"+
					"<title>Bill</title>"+
					"<meta charset=\"utf-8\" />"+
					"<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\" />"+
					"<title>RuralIVRS - Welcome</title>"+
					"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"+
					"<meta name=\"description\" content=\"Rural IVRS Web application.\" />"+
					"<meta name=\"author\" content=\"IIT Bombay\" />"+
					"<style>"+
						"table, th, td {"+
			    			"border: 1px solid black;"+
			    			"border-collapse: collapse;"+
						"}"+
					"</style>"+
				"</head>"+
				"<body style=\"font-family:Times New Roman; font-size:8\">"+
					"<p>Dear User,<br>Your Order has been processed.<br>Please find the attached bill below.</p><br>"+
					"<div class=\"container-fluid\">"+
						"<div class=\"row-fluid\" style=\"text-align:center\">"+
							"<div class=\"span4\">"+
							"<center>"+
								"<table class=\"table\">"+
									"<thead>"+
										"<tr>"+
											"<td colspan=\"2\" style=\"text-align:center\">"+
												"<b>"+
													"<div>"+organization.getName()+"</div>"+
												"</b>"+
												"<div>"+organization.getAddress()+"</div>"+
												"<div>"+organization.getContact()+"</div>"+
												"<div>"+billLayoutSetting.getHeaderContent()+"</div>"+
											"</td>"+
										"</tr>"+
									"</thead>"+
									"<tbody>"+
										"<tr>"+
											"<td>"+
												"<div><b>Member: </b></div>"+
												"<div>"+order.getMessage().getUser().getName()+"</div>"+
											"</td>"+
											"<td>"+
												"<div><b>Group Name: </b></div>"+
												"<div>"+order.getMessage().getGroup().getName()+"</div>"+			
											"</td>"+
										"</tr>"+
										"<tr>"+
											"<td>"+
												"<div><b>Order Id: </b></div>"+
												"<div>#"+order.getOrderId()+"</div>"+
											"</td>"+
											"<td>"+
												"<div><b>Time: </b></div>"+
												"<div>"+order.getMessage().getTime()+"</div>"+			
											"</td>"+
										"</tr>"+
										"<tr>"+
											"<td colspan=\"2\">"+
												"<table>"+
													"<thead>"+
														"<th>Product</th>"+
														"<th>Quantity</th>"+
														"<th>Rate(Rs.)</th>"+
														"<th>Cost(Rs.)</th>"+
													"</thead>"+
													"<tbody>"+
														rows+
													"</tbody>"+
												"</table>"+			
											"</td>"+
										"</tr>"+
										"<tr>"+
											"<td colspan=\"2\">"+
												"<table width=\"100%\">"+
													"<tbody>"+
														"<tr>"+
															"<th>Total</th>"+
															"<td style=\"text-align:right\">"+total+"</td>"+
														"</tr>"+
													"</tbody>"+
												"</table>"+
											"</td>"+
										"</tr>"+
										"<tr>"+
											"<td colspan=\"2\">"+
												"<div style=\"text-align:center\">"+billLayoutSetting.getFooterContent()+"</div>"+
											"</td>"+
										"</tr>"+
									"</tbody>"+
								"</table>"+
							"</center>"+
							"</div>"+
						"</div>"+
					"</div>"+
				"</body>"+
			"</html>", "text/html");
		     msg.setSentDate(new Date());

		     Transport.send(msg);
		     System.out.println("Message sent.");
		     map.clear();
		     return 1;
		  }catch (MessagingException e){ System.out.println("Error, cause: " + e); return 0;}
		
	}
	
	public static String returnBill(Order order, Organization organization, BillLayoutSettings billLayoutSetting) {
		String rows= "";
	     double total= 0;
	     HashMap<String,OrderItem> map= new HashMap<String,OrderItem>();
	     for(OrderItem orderitem: order.getOrderItems())
   	 {
   		 if(!map.containsKey(orderitem.getProduct().getName()))
   			 map.put(orderitem.getProduct().getName(), orderitem);
   		 else
   		 {
   			 OrderItem orderItem=new OrderItem();
   			 float qty=map.get(orderitem.getProduct().getName()).getQuantity()+orderitem.getQuantity();
   			 orderItem.setOrder(order);
   			 orderItem.setProduct(orderitem.getProduct());
   			 orderItem.setQuantity(qty);
   			 orderItem.setUnitRate(orderitem.getUnitRate());
   			 map.put(orderitem.getProduct().getName(),orderItem);
   		 }
   	 }
	     Set<String> product= map.keySet();
	     Iterator<String> i= product.iterator();
	     while(i.hasNext())
	     {
	    	 OrderItem orderitem= map.get(i.next());
	    	 rows = rows+"<tr>";
	    	 rows = rows + "<td style=\"text-align:right\">"+orderitem.getProduct().getName()+"</td>";
	    	 rows = rows + "<td style=\"text-align:right\">"+orderitem.getQuantity()+"</td>";
	    	 rows = rows + "<td style=\"text-align:right\">"+orderitem.getUnitRate()+"</td>";
	    	 rows = rows + "<td style=\"text-align:right\">"+(orderitem.getQuantity()*orderitem.getUnitRate())+"</td></tr>";
	    	 total = total + orderitem.getQuantity()*orderitem.getUnitRate();
	     }
	     String msg = new String("<html>"+
	 			"<head>"+
				"<title>Bill</title>"+
				"<meta charset=\"utf-8\" />"+
				"<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\" />"+
				"<title>RuralIVRS - Welcome</title>"+
				"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"+
				"<meta name=\"description\" content=\"Rural IVRS Web application.\" />"+
				"<meta name=\"author\" content=\"IIT Bombay\" />"+
				"<style>"+
					"table, th, td {"+
		    			"border: 1px solid black;"+
		    			"border-collapse: collapse;"+
					"}"+
				"</style>"+
			"</head>"+
			"<body style=\"font-family:Times New Roman; font-size:8\">"+
				"<br>"+
				"<div class=\"container-fluid\">"+
					"<div class=\"row-fluid\" style=\"text-align:center\">"+
						"<div class=\"span4\">"+
						"<center>"+
							"<table class=\"table\">"+
								"<thead>"+
									"<tr>"+
										"<td colspan=\"2\" style=\"text-align:center\">"+
											"<b>"+
												"<div>"+organization.getName()+"</div>"+
											"</b>"+
											"<div>"+organization.getAddress()+"</div>"+
											"<div>"+organization.getContact()+"</div>"+
											"<div>"+billLayoutSetting.getHeaderContent()+"</div>"+
										"</td>"+
									"</tr>"+
								"</thead>"+
								"<tbody>"+
									"<tr>"+
										"<td>"+
											"<div><b>Member: </b></div>"+
											"<div>"+order.getMessage().getUser().getName()+"</div>"+
										"</td>"+
										"<td>"+
											"<div><b>Group Name: </b></div>"+
											"<div>"+order.getMessage().getGroup().getName()+"</div>"+			
										"</td>"+
									"</tr>"+
									"<tr>"+
										"<td>"+
											"<div><b>Order Id: </b></div>"+
											"<div>#"+order.getOrderId()+"</div>"+
										"</td>"+
										"<td>"+
											"<div><b>Time: </b></div>"+
											"<div>"+order.getMessage().getTime()+"</div>"+			
										"</td>"+
									"</tr>"+
									"<tr>"+
										"<td colspan=\"2\">"+
											"<table>"+
												"<thead>"+
													"<th>Product</th>"+
													"<th>Quantity</th>"+
													"<th>Rate(Rs.)</th>"+
													"<th>Cost(Rs.)</th>"+
												"</thead>"+
												"<tbody>"+
													rows+
												"</tbody>"+
											"</table>"+			
										"</td>"+
									"</tr>"+
									"<tr>"+
										"<td colspan=\"2\">"+
											"<table width=\"100%\">"+
												"<tbody>"+
													"<tr>"+
														"<th>Total</th>"+
														"<td style=\"text-align:right\">"+total+"</td>"+
													"</tr>"+
												"</tbody>"+
											"</table>"+
										"</td>"+
									"</tr>"+
									"<tr>"+
										"<td colspan=\"2\">"+
											"<div style=\"text-align:center\">"+billLayoutSetting.getFooterContent()+"</div>"+
										"</td>"+
									"</tr>"+
								"</tbody>"+
							"</table>"+
						"</center>"+
						"</div>"+
					"</div>"+
				"</div>"+
			"</body>"+
		"</html>");
		return msg;
	}
	
}
