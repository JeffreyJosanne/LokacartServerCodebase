package app.data.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import app.entities.Organization;
import app.entities.OrganizationMembership;
import app.entities.User;

public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembership, Integer> {
	/*
	 * Default functions
	 */

	@PostAuthorize("principal.userId == returnObject.user.userId or "
			+ "hasRole('ADMIN'+returnObject.organization.abbreviation)")
	@Override
	public OrganizationMembership findOne(Integer id);

	@PostFilter("principal.userId == filterObject.user.userId or "
			+ "hasRole('ADMIN'+filterObject.organization.abbreviation)")
	@Override
	public List<OrganizationMembership> findAll();

	@PostFilter("principal.userId == filterObject.user.userId or "
			+ "hasRole('ADMIN'+filterObject.organization.abbreviation)")
	@Override
	public Page<OrganizationMembership> findAll(Pageable pageable);

	@PostFilter("principal.userId == filterObject.user.userId or "
			+ "hasRole('ADMIN'+filterObject.organization.abbreviation)")
	@Override
	public List<OrganizationMembership> findAll(Sort sort);

	//@PreAuthorize("hasRole('ADMIN'+#membership.organization.abbreviation)")
	@Override
	public <S extends OrganizationMembership> S save(@Param("membership") S membership);

	@PreAuthorize("hasRole('ADMIN'+#membership.organization.abbreviation)")
	@Override
	public void delete(@Param("membership") OrganizationMembership membership);

	/*
	 * Search functions
	 */
	public OrganizationMembership findByUserAndOrganization(User user, Organization organization);

	public List<OrganizationMembership> findByOrganization(Organization organization);
	
	public List<OrganizationMembership> findByOrganizationAndStatus(Organization organization, int status);
	
	public OrganizationMembership findByUserAndIsAdmin(User user, boolean isAdmin);
	

	public List<OrganizationMembership> findByOrganizationAndIsAdmin(Organization organization, boolean isAdmin);
}
