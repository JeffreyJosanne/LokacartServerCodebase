package app.entities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import app.entities.message.Message;


/**
 * The persistent class for the order database table.
 * 
 */
@Entity
@Table(name="order")
public class Order implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="order_id")
	private int orderId;

	@Column(name="autolock_time")
	private Timestamp autolockTime;

	@Column(name="is_locked")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean isLocked;

	private String status;

	//bi-directional many-to-one association to OrderItem
	@OneToMany(mappedBy="order")
	private List<OrderItem> orderItems;

	//bi-directional many-to-one association to Organization
	@ManyToOne
	@JoinColumn(name="organization_id")
	private Organization organization;

	//bi-directional many-to-one association to Message
	@OneToOne(mappedBy="order")
	private Message message;

	public Order() {
	}

	public int getOrderId() {
		return this.orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public Timestamp getAutolockTime() {
		return this.autolockTime;
	}

	public void setAutolockTime(Timestamp autolockTime) {
		this.autolockTime = autolockTime;
	}

	public boolean getIsLocked() {
		return this.isLocked;
	}

	public void setIsLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<OrderItem> getOrderItems() {
		return this.orderItems;
	}

	public void setOrderItems(List<OrderItem> orderItems) {
		this.orderItems = orderItems;
	}

	public Organization getOrganization() {
		return this.organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Message getMessage() {
		return this.message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public OrderItem addOrderItem(OrderItem orderItem) {
		getOrderItems().add(orderItem);
		orderItem.setOrder(this);

		return orderItem;
	}

	public OrderItem removeOrderItem(OrderItem orderItem) {
		getOrderItems().remove(orderItem);
		orderItem.setOrder(null);

		return orderItem;
	}
	
	public List<HashMap<String,String>> getOrderItemsHashMap()
	{
		/*Marshalling the order items in a string hashmap since included version of the spring data rest doesnt support 
		 * nested projection in collection containers. Upgrading to newer version requires rework in code.
		 * */
		List<HashMap<String,String>> response= new ArrayList<HashMap<String,String>>();
		for(OrderItem orderItem:orderItems)
		{
			HashMap<String, String> temp = new HashMap<String, String>();
			temp.put("quantity",Float.toString(orderItem.getQuantity()));
			temp.put("unitrate",Float.toString(orderItem.getUnitRate()));
			temp.put("productname",orderItem.getProduct().getName());
			temp.put("productId", Integer.toString(orderItem.getProduct().getProductId()));
			temp.put("stockquantity",Float.toString(orderItem.getProduct().getQuantity()));
			response.add(temp);
		}
		return response;
	}

}
