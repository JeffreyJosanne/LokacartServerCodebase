package webapp.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import webapp.entities.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {
	/*
	 * Default functions
	 */

	@PostAuthorize("hasRole('ADMIN'+returnObject.organization.organizationId)")
	@Override
	public Order findOne(Integer id);

	@PostFilter("hasRole('ADMIN'+filterObject.organization.organizationId)")
	@Override
	public List<Order> findAll();

	@PostFilter("hasRole('ADMIN'+filterObject.organization.organizationId)")
	@Override
	public Page<Order> findAll(Pageable pageable);

	@PostFilter("hasRole('ADMIN'+filterObject.organization.organizationId)")
	@Override
	public List<Order> findAll(Sort sort);

	@PreAuthorize("hasRole('MEMBER'+#order.organization.organizationId)")
	@Override
	public <S extends Order> S save(S order);

	@PreAuthorize("hasRole('MEMBER'+#order.organization.organizationId)")
	@Override
	public void delete(Order order);

	/*
	 * Search functions
	 */

}