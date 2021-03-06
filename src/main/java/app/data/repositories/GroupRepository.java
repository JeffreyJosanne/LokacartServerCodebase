package app.data.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import app.entities.Group;
import app.entities.Organization;

public interface GroupRepository extends JpaRepository<Group, Integer> {
	/*
	 * Default functions
	 */

	@PostFilter("hasRole('MEMBER'+filterObject.organization.abbreviation)")
	@Override
	public List<Group> findAll();

	@PostFilter("hasRole('MEMBER'+filterObject.organization.abbreviation)")
	@Override
	public Page<Group> findAll(Pageable pageable);

	@PostFilter("hasRole('MEMBER'+filterObject.organization.abbreviation)")
	@Override
	public List<Group> findAll(Sort sort);
	
	@PreAuthorize("hasRole('ADMIN'+#group.organization.abbreviation)")
	@Override
	public <S extends Group> S save(@Param("group") S group);

	@PreAuthorize("hasRole('ADMIN'+#group.organization.abbreviation)")
	@Override
	public void delete(@Param("group") Group group);

	/*
	 * Search functions
	 */
	public List<Group> findAllByOrderByNameAsc();
	
	public Group findByOrganizationAndParentGroup(Organization organization, Group parentGroup);
	
	public List<Group> findByOrganization(Organization organization);
	
	public Group findByNameAndOrganization(String name,Organization organization);
}
