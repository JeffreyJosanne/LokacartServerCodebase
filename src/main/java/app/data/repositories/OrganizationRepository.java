package app.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import app.entities.Organization;
import app.entities.Product;

public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
	/*
	 * Default functions
	 */

	@PreAuthorize("hasRole('ADMIN'+#org.abbreviation) or "
			+ "(#org.parentOrganization != null and hasRole('ADMIN'+#org.parentOrganization.abbreviation))")
	@Override
	public <S extends Organization> S save(@Param("org") S org);

	@PreAuthorize("hasRole('ADMIN'+#org.abbreviation) or "
			+ "(#org.parentOrganization != null and hasRole('ADMIN'+#org.parentOrganization.abbreviation))")
	@Override
	public void delete(@Param("org") Organization org);
	
	
	/*
	 * Search functions
	 */
	@PostAuthorize("hasRole('ADMIN'+returnObject.productType.organization.abbreviation)")
	@Override
	public Organization findOne(Integer id);


	Organization findByAbbreviation(@Param("abbreviation") String abbreviation);
	
	
	

}
