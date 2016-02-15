package app.data.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import app.entities.Group;
import app.entities.GroupMembership;
import app.entities.Organization;
import app.entities.User;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Integer> {
	/*
	 * Default functions
	 */

	@PostAuthorize("principal.userId == returnObject.user.userId or "
			+ "hasRole('ADMIN_OR_PUBLISHER'+returnObject.group.organization.abbreviation)")
	@Override
	public GroupMembership findOne(Integer id);

	
	@Override
	public List<GroupMembership> findAll();

	@PostFilter("principal.userId == filterObject.user.userId or "
			+ "hasRole('ADMIN_OR_PUBLISHER'+filterObject.group.organization.abbreviation)")
	@Override
	public Page<GroupMembership> findAll(Pageable pageable);

	@PostFilter("principal.userId == filterObject.user.userId or "
			+ "hasRole('ADMIN_OR_PUBLISHER'+filterObject.group.organization.abbreviation)")
	@Override
	public List<GroupMembership> findAll(Sort sort);

	//@PreAuthorize("hasRole('ADMIN'+#membership.group.organization.abbreviation)")
	@Override
	public <S extends GroupMembership> S save(@Param("membership") S membership);

	//@PreAuthorize("hasRole('ADMIN'+#membership.group.organization.abbreviation)")
	@Override
	public void delete(@Param("membership") GroupMembership membership);
	
	/*
	 * Search functions
	 */
	
	public GroupMembership findByUserAndGroup(@Param("user") User user, @Param("group") Group group);
	
	public List<GroupMembership> findByUser(@Param("user") User user);
	
	public List<GroupMembership> findByGroup(Group group);
	
	public List<GroupMembership> findByGroupOrderByUser_NameAsc(Group group);
	
	public List<GroupMembership> findByUserOrderByGroup_NameAsc(User user);
	
	public List<GroupMembership> findAllByOrderByGroup_NameAsc();
	
	public List<GroupMembership> findAllByOrderByUser_NameAsc();
	
	@RestResource(rel="findByUserAndGroup_Organization", path="findByUserAndGroup_Organization")
	public List<GroupMembership> findByUserAndGroup_Organization(@Param("user") User user,@Param("organization") Organization organization);
	
	public List<GroupMembership> findByUserAndGroup_OrganizationOrderByGroup_NameAsc(User user,Organization organization);	

}
