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

import app.entities.Group;
import app.entities.message.Message;

public interface MessageRepository extends JpaRepository<Message, Integer> {
	/*
	 * Default functions
	 */

	@PostAuthorize("hasRole('ADMIN'+returnObject.broadcast.organization.abbreviation)")
	@Override
	public Message findOne(Integer id);

	@PostFilter("hasRole('ADMIN'+filterObject.broadcast.organization.abbreviation)")
	@Override
	public List<Message> findAll();

	@PostFilter("hasRole('ADMIN'+filterObject.broadcast.organization.abbreviation)")
	@Override
	public Page<Message> findAll(Pageable pageable);

	@PostFilter("hasRole('ADMIN'+filterObject.broadcast.organization.abbreviation)")
	@Override
	public List<Message> findAll(Sort sort);

	@PreAuthorize("hasRole('ADMIN'+#message.broadcast.organization.abbreviation)")
	@Override
	public <S extends Message> S save(@Param("message") S message);

	@PreAuthorize("hasRole('ADMIN'+#message.broadcast.organization.abbreviation)")
	@Override
	public void delete(@Param("message") Message message);

	/*
	 * Search functions
	 */
	
	//public List<Message> findByOrganizationAndType(Organization organization,String type);
	public List<Message> findByGroupAndType(Group group,String type);
	public List<Message> findByGroupAndFormat(Group group,String format);
	public List<Message> findByGroupAndResponseAndType(Group group, boolean response,String type);
	public List<Message> findByGroup(Group group);
	//public Message findBy
	

}
