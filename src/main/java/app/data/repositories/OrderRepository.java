package app.data.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
//import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import app.entities.Group;
import app.entities.Order;
import app.entities.Organization;
import app.entities.Product;

public interface OrderRepository extends JpaRepository<Order, Integer> {
	/*
	 * Default functions
	 */

	@Override
	public Order findOne(Integer id);

	@PostFilter("hasRole('ADMIN'+filterObject.organization.abbreviation)")
	@Override
	public List<Order> findAll();

	@PostFilter("hasRole('ADMIN'+filterObject.organization.abbreviation)")
	@Override
	public Page<Order> findAll(Pageable pageable);

	@PostFilter("hasRole('ADMIN'+filterObject.organization.abbreviation)")
	@Override
	public List<Order> findAll(Sort sort);

	@PreAuthorize("hasRole('MEMBER'+#order.organization.abbreviation)")
	@Override
	public void delete(@Param("order") Order order);

	/*
	 * Search functions
	 */
     
	public List<Order> findByOrganization(Organization organization);
	
	public List<Order> findByOrganizationOrderByOrderIdDesc(Organization organization);
	
	public List<Order> findByOrganizationAndStatus(Organization organization,String status);
	
	public List<Order> findByOrganizationAndStatusOrderByOrderIdDesc(Organization organization,String status);
	
	public List<Order> findByMessage_group(Group group);
	
	
	public List<Order> findByMessage_groupAndStatus(Group group,String status);
	
	//public List<Order> findByMessage_formatAndOrganization_abbreviation(@Param("format") String format,@Param("abbr") String abbr);
	
	@PostFilter("hasRole('MEMBER'+filterObject.organization.abbreviation)")
	@RestResource(rel="getOrdersForMember", path="getOrdersForMember")
	public List<Order> findByMessage_formatAndStatusAndOrganization_abbreviationAndMessage_user_userPhoneNumbers_phoneNumber(@Param("format") String format,@Param("status") String status,@Param("abbr") String abbr,@Param("phonenumber")String phonenumber);
	
}