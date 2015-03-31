package app.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import app.entities.ProductType;

public interface ProductTypeRepository extends JpaRepository<ProductType, Integer> {
	/*
	 * Default functions
	 */

	@PostAuthorize("hasRole('ADMIN'+returnObject.organization.organizationId)")
	@Override
	public ProductType findOne(Integer id);

	@PostFilter("hasRole('ADMIN'+filterObject.organization.organizationId)")
	@Override
	public List<ProductType> findAll();

	@PostFilter("hasRole('ADMIN'+filterObject.organization.organizationId)")
	@Override
	public Page<ProductType> findAll(Pageable pageable);

	@PostFilter("hasRole('ADMIN'+filterObject.organization.organizationId)")
	@Override
	public List<ProductType> findAll(Sort sort);

	@PreAuthorize("hasRole('ADMIN'+#type.organization.organizationId)")
	@Override
	public <S extends ProductType> S save(S type);

	@PreAuthorize("hasRole('ADMIN'+#type.organization.organizationId)")
	@Override
	public void delete(ProductType type);

	/*
	 * Search functions
	 */

}