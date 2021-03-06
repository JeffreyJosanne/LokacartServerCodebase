package app.data.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import app.entities.Group;
import app.entities.Order;
import app.entities.message.Message;

public interface MessageRepository extends JpaRepository<Message, Integer> {
	/*
	 * Default functions
	 */

	@PostAuthorize("hasRole('ADMIN'+returnObject.group.organization.abbreviation)")
	@Override
	public Message findOne(Integer id);

	@PostFilter("hasRole('ADMIN'+filterObject.group.organization.abbreviation)")
	@Override
	public List<Message> findAll();

	@PostFilter("hasRole('ADMIN'+filterObject.group.organization.abbreviation)")
	@Override
	public Page<Message> findAll(Pageable pageable);

	@PostFilter("hasRole('ADMIN'+filterObject.group.organization.abbreviation)")
	@Override
	public List<Message> findAll(Sort sort);

	@PreAuthorize("hasRole('ADMIN'+#message.group.organization.abbreviation)")
	@Override
	public void delete(@Param("message") Message message);

	/*
	 * Search functions
	 */
	public Message findByOrder(Order order);
	public List<Message> findByGroupAndTypeAndFormat(Group group,String type,String format, Sort sort);
	public List<Message> findByGroupAndFormat(Group group,String format);
	public List<Message> findByGroupAndModeAndFormat(Group group,String mode,String format);
	//These are commented because these may be required if our function with sort would not work....
	//public List<Message> findByGroupAndFormatAndOrder_Status(Group group,String format,String status);
	//public List<Message> findByGroupAndFormatAndOrder_StatusOrderByTime(Group group,String format,String status);
	public List<Message> findByGroupAndFormatAndOrder_Status(Group group,String format,String status,Sort sort);
	public List<Message> findByGroupAndResponseAndTypeAndFormat(Group group, boolean response,String type, String format, Sort sort);
	public List<Message> findByGroup(Group group);
	
	@Query(value = "SELECT count(response),url FROM message,voice where group_id=?1 and response=?2 and type=?3 and format=?4 and voice.voice_id=(select voice_id from broadcast where broadcast_id=message.source_broadcast_id) group by (select url from voice where voice.voice_id=(select voice_id from broadcast where broadcast_id=message.source_broadcast_id))", nativeQuery = true)
	public List<Object[]> countByGroupAndResponseAndTypeAndFormat(int groupid,boolean b,String type,String format);
}
