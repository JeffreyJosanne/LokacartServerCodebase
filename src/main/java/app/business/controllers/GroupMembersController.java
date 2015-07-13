package app.business.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import app.business.services.OrganizationService;
import app.business.services.UserViewService;
import app.business.services.UserViewService.UserView;
import app.entities.Group;
import app.entities.Organization;

@Controller
@RequestMapping("/web/{org}")
public class GroupMembersController {

	@Autowired
	UserViewService userViewService;
	
	@Autowired
	OrganizationService organizationService;
	
	@RequestMapping(value="/memberList/{groupId}")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	public String settingsPage(@PathVariable String org, @PathVariable int groupId, Model model) {
		
		List<UserView> userViewList = userViewService.getUserViewListByGroup(groupId);
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		Group parentGroup = organizationService.getParentGroup(organization);
		int parentGroupId = parentGroup.getGroupId();
		model.addAttribute("organization", organization);
		model.addAttribute("parentGroup", parentGroup);
		model.addAttribute("parentgroupid", parentGroupId);
		model.addAttribute("userViews", userViewList);
		model.addAttribute("groupId", groupId);
		return "groupWiseMember";
	}

}