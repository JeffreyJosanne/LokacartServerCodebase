package app.business.controllers;

import in.ac.iitb.ivrs.telephony.base.util.IVRUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import app.business.services.BroadcastRecipientService;
import app.business.services.GroupMembershipService;
import app.business.services.GroupService;
import app.business.services.LatestRecordedVoiceService;
import app.business.services.OrganizationService;
import app.business.services.UserService;
import app.business.services.VoiceService;
import app.business.services.broadcast.BroadcastService;
import app.entities.BroadcastRecipient;
import app.entities.Group;
import app.entities.GroupMembership;
import app.entities.LatestRecordedVoice;
import app.entities.Organization;
import app.entities.User;
import app.entities.UserPhoneNumber;
import app.entities.Voice;
import app.entities.broadcast.VoiceBroadcast;
import app.telephony.config.Configs;


@Controller
@RequestMapping("/web/{org}")
public class BroadcastVoiceController {

	@Autowired
	OrganizationService organizationService;
	@Autowired
	GroupService groupService;
	@Autowired
	GroupMembershipService groupMembershipService;
	@Autowired
	UserService userService;
	@Autowired
	VoiceService voiceService;
	@Autowired
	BroadcastService broadcastService;
	@Autowired
	BroadcastRecipientService broadcastRecipientService;
	@Autowired
	LatestRecordedVoiceService latestRecordedVoiceService;

	@RequestMapping(value="/broadcastVoiceMessages/{groupId}")
	@PreAuthorize("hasRole('ADMIN'+#org)")
	@Transactional
	public String broadcastVoiceMessages(@PathVariable String org, @PathVariable int groupId, Model model) {

		Group group = groupService.getGroup(groupId);
		Organization organization = organizationService.getOrganizationByAbbreviation(org);
		User publisher = userService.getCurrentUser();
		
		
		List<GroupMembership> groupMembershipList = new ArrayList<GroupMembership>(groupMembershipService.getGroupMembershipListByGroupSortedByUserName(group));
		
		//called latest recorded voice according to time
		LatestRecordedVoice broadcast = latestRecordedVoiceService.getLatestRecordedVoiceByOrganization(organization);
		model.addAttribute("broadcast", broadcast);
		
		List<User> users = new ArrayList<User>();
		for(GroupMembership groupMembership : groupMembershipList) {
			users.add(groupMembership.getUser());
			System.out.println(groupMembership.getUser().getName());
		}
		
		model.addAttribute("users",users);
		model.addAttribute("organization",organization);
		model.addAttribute("group",group);
		model.addAttribute("publisher",publisher);
		
		//TODO Ask what to do when user is not a publisher do we prevent it on UI side.
		String role = userService.getUserRole(publisher, organization);
		model.addAttribute("role", role);
		
		return "broadcastVoice";
	}
	
	@RequestMapping(value = "/broadcastVoiceMessages/{groupId}", method = RequestMethod.POST)
	@ResponseBody
	public void logs(@RequestBody Map<String,String> body) {
		Organization organization = organizationService.getOrganizationById(Integer.parseInt(body.get("organizationId")));
		Group group = groupService.getGroup(Integer.parseInt(body.get("groupId")));
		User publisher = userService.getUser(Integer.parseInt(body.get("publisherId")));
		String mode = body.get("mode");
		//Converting string to integer and converting to boolean
		boolean askOrder = (Integer.parseInt(body.get("askOrder")) !=0);
		boolean askFeedback = (Integer.parseInt(body.get("askFeedback")) !=0);;
		boolean askResponse = (Integer.parseInt(body.get("askResponse")) !=0);
		
		String broadcastedTime = body.get("broadcastedTime");
		Timestamp timestamp = Timestamp.valueOf(broadcastedTime);
		
		boolean appOnly = (Integer.parseInt(body.get("appOnly")) !=0);
		Voice voice = voiceService.getVoice(Integer.parseInt(body.get("voiceId")));
		String voiceUrl = voice.getUrl();
		boolean voiceBroadcastDraft = (Integer.parseInt(body.get("voiceBroadcastDraft")) !=0);
		 
		VoiceBroadcast broadcast = new VoiceBroadcast(organization, group, publisher, mode, askFeedback,  askOrder, askResponse, appOnly, voice, voiceBroadcastDraft);
	
		
		broadcastService.addBroadcast(broadcast);
		
		//TODO Remove the line just below. The time is updated right now but actually the top broadcast extracted in telephony service is to be done with the help of broadcast schedule and separate thread
		java.util.Date date= new java.util.Date();
		Timestamp currentTimestamp= new Timestamp(date.getTime());
		broadcast.setBroadcastedTime(currentTimestamp);
		
		String userIdString = body.get("userIds");
		String[] userIdList = userIdString.split(",");
		List<BroadcastRecipient> broadcastRecipients = new ArrayList<BroadcastRecipient>();
		for(int i=0 ; i<userIdList.length;i++)
		{	
			
			User user = userService.getUser(Integer.parseInt(userIdList[i]));
			BroadcastRecipient broadcastRecipient = new BroadcastRecipient(broadcast, user);
			broadcastRecipients.add(broadcastRecipient);
			broadcastRecipientService.addBroadcastRecipient(broadcastRecipient);
		}
		
		//TODO have to shift this function to thread. Also have to ask where is the Broadcast object mentioned here.
		for(BroadcastRecipient recipient: broadcastRecipients)
		{
			User user=recipient.getUser();
			System.out.println("User:"+user.getName());
			List<UserPhoneNumber> phoneNumbers=user.getUserPhoneNumbers();
			for(UserPhoneNumber no:phoneNumbers)
			{	
				//Outbound call has to be appended with a zero after removing 91 
				String phoneNumber = "0" + no.getPhoneNumber().substring(2);
				if(IVRUtils.makeOutboundCall(phoneNumber, Configs.Telephony.IVR_NUMBER, Configs.Telephony.OUTBOUND_APP_URL));
				{
					break;
				}
			}
		}
	    
	}
	
	@RequestMapping(value = "/latestBroadcastVoiceMessages/{groupId}", method = RequestMethod.POST)
	@ResponseBody
	public void latestRecordedLogs(@RequestBody Map<String,String> body) {
		System.out.println("We have received the latest body from uploader in Angular "+body);
		Organization organization = organizationService.getOrganizationById(Integer.parseInt(body.get("organizationId")));
		Voice voice = voiceService.getVoice(Integer.parseInt(body.get("voiceId")));
		String recordedTime = body.get("broadcastedTime");
		Timestamp timestamp = Timestamp.valueOf(recordedTime);
		
		latestRecordedVoiceService.updateLatestRecordedVoice(organization, timestamp, voice);
	}

}
