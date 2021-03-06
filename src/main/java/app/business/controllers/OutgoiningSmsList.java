package app.business.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import app.data.repositories.GroupRepository;
import app.entities.Group;
import app.entities.broadcast.Broadcast;



@Controller
@RequestMapping("/web/{org}")
public class OutgoiningSmsList {

	@Autowired
	GroupRepository groupRepository;


	@RequestMapping(value="/outGoingSmsList/{groupId}")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public String outgoingSmsList(@PathVariable String org, @PathVariable int groupId, Model model ) {

		Group group = groupRepository.findOne(groupId);
		List<Broadcast> broadcastList = group.getBroadcasts();
		List<Broadcast> broadcastedMessage =  new ArrayList<Broadcast>();
		for(Broadcast message : broadcastList) {
			
			if(message.getFormat().equalsIgnoreCase("text")){
				broadcastedMessage.add(message);
				model.addAttribute("outGoingSms", broadcastedMessage);

			}

		}


		return "outgoiningSmsList";
	}

}
