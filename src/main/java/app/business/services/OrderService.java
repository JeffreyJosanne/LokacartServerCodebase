package app.business.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.data.repositories.OrderRepository;
import app.entities.Group;
import app.entities.Order;
import app.entities.Organization;
import app.entities.User;
import app.entities.message.Message;

@Service
public class OrderService {
	
	@Autowired
	OrderRepository orderRepository;
	
	/*
	 * Returns list of Orders by an Organization  
	 */
	public List<Order> getOrderByOrganizationProcessed(Organization organization) {
		return orderRepository.findByOrganizationAndStatus(organization, "processed");
	}
	
	public List<Order> getOrderByOrganizationSaved(Organization organization) {
		return orderRepository.findByOrganizationAndStatus(organization, "saved");
	}
	
	public List<Order> getOrderByOrganizationCancelled(Organization organization) {
		return orderRepository.findByOrganizationAndStatus(organization, "cancelled");
	}

	public List<Order> getOrderByOrganizationRejected(Organization organization) {
		return orderRepository.findByOrganizationAndStatus(organization, "rejected");
	}

	public List<Order> getOrderByGroupProcessed(Group group)
	{
		return orderRepository.findByMessage_groupAndStatus(group,"processed");
	}
	
	/*
	 * changes the status of an order to 'saved'
	 */
	public void saveOrder(Order order)
	{
		order.setStatus("saved");
		orderRepository.save(order);
	}
	
	/*
	 * changes the status of an order to 'accepted'
	 */
	public void acceptOrder(Order order)
	{
		order.setStatus("accepted");
		orderRepository.save(order);
	}
	
	/*
	 * changes the status of an order to 'rejected'
	 */
	public void rejectOrder(Order order)
	{
		order.setStatus("rejected");
		orderRepository.save(order);
	}
	
	/*
	 * changes the status of an order to 'processed'
	 */
	public void processOrder(Order order)
	{
		order.setStatus("processed");
		orderRepository.save(order);
	}
	
	/*
	 * Return message for a particular order
	 */
	public Message getMessageByOrder(Order order)
	{
		return order.getMessage();
	}
	
	public User getMessageUserByOrder(Order order)
	{
		return order.getMessage().getUser();
	}
	
	public Order getOrder(int orderId) {
		
		return orderRepository.findOne(orderId);
	}
	
	public Order addOrder(Order order) {
		
		return orderRepository.save(order);
	}

	public void removeOrder(Order order) {
	
		orderRepository.delete(order);
}

	public void cancelOrder(Order order) {
		order.setStatus("cancelled");
		orderRepository.save(order);
		
	}
	public List<Order> getOrderByOrganization(Organization organization) {
		return orderRepository.findByOrganization(organization); 
	}
}