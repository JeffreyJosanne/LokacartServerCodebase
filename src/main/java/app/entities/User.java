package app.entities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Columns;

import com.fasterxml.jackson.annotation.JsonIgnore;

import app.entities.broadcast.Broadcast;
import app.entities.message.Message;


/**
 * The persistent class for the user database table.
 * 
 */
@Entity
@Table(name="user")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="user_id")
	private int userId;

	private String address;
	
	@Column (name = "pincode")
	private String pincode;

	@Column(name="call_locale")
	private String callLocale;

	private String email;

	private String name;
	
	@Column(name="time")
	private Timestamp time;

	@Column(name="sha256_password")
	private String sha256Password;

	@Column(name="web_locale")
	private String webLocale;
	
	@Column(name="voicebroadcastlimit")
	private Integer voicebroadcastlimit;
	
	@Column(name="textbroadcastlimit")
	private Integer textbroadcastlimit;
	
	@Column(name = "pass_otp")
	private String passOtp;
	
	@Column( name = "pass_time")
	private Timestamp passTime;
	
	@Column ( name = "pass_temp" )
	private String passTemp;

	public Integer getVoicebroadcastlimit() {
		return voicebroadcastlimit;
	}

	public void setVoicebroadcastlimit(Integer voicebroadcastlimit) {
		this.voicebroadcastlimit = voicebroadcastlimit;
	}

	public Integer getTextbroadcastlimit() {
		return textbroadcastlimit;
	}

	public void setTextbroadcastlimit(Integer textbroadcastlimit) {
		this.textbroadcastlimit = textbroadcastlimit;
	}

	//bi-directional many-to-one association to Broadcast
	@OneToMany(mappedBy="publisher")
	private List<Broadcast> broadcasts;

	//bi-directional many-to-one association to BroadcastRecipient
	@OneToMany(mappedBy="user")
	private List<BroadcastRecipient> broadcastRecipients;

	//bi-directional many-to-one association to GroupMembership
	@OneToMany(mappedBy="user")
	private List<GroupMembership> groupMemberships;

	//bi-directional many-to-one association to Message
	@OneToMany(mappedBy="user")
	private List<Message> messages;

	//bi-directional many-to-one association to OrganizationMembership
	@OneToMany(mappedBy="user")
	private List<OrganizationMembership> organizationMemberships;

	//bi-directional many-to-one association to UserPhoneNumber
	@OneToMany(mappedBy="user")
	private List<UserPhoneNumber> userPhoneNumbers;

	public User() {
	}

	public User(String name, String address, String webLocale, String callLocale, String email) {
		this.name = name;
		this.address = address;
		this.webLocale = webLocale;
		this.callLocale = callLocale;
		this.email = email;
	}
	
	public User(String name, String address, String webLocale, String callLocale, String email, String pincode) {
		this.name = name;
		this.address = address;
		this.webLocale = webLocale;
		this.callLocale = callLocale;
		this.email = email;
		this.pincode = pincode;
	}

	public int getUserId() {
		return this.userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getPincode() {
		return this.pincode;
	}
	
	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getCallLocale() {
		return this.callLocale;
	}

	public void setCallLocale(String callLocale) {
		this.callLocale = callLocale;
	}

	public String getEmail() {
		return this.email;
	}

	public Timestamp getTime() {
		return this.time;
	}
	
	public void setTime(Timestamp Time) {
		this.time = Time;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
	public String getSha256Password() {
		return this.sha256Password;
	}

	public void setSha256Password(String sha256Password) {
		this.sha256Password = sha256Password;
	}

	public String getWebLocale() {
		return this.webLocale;
	}

	public void setWebLocale(String webLocale) {
		this.webLocale = webLocale;
	}
	
	public String getPassOtp() {
		return this.passOtp;
	}
	
	public void setPassOtp(String passOtp) {
		this.passOtp = passOtp;
	}
	
	public Timestamp getPassTime() {
		return this.passTime;
	}
	
	public void setPassTime(Timestamp passTime) {
		this.passTime = passTime;
	}
	
	public String getPassTemp () {
		return this.passTemp;
	}
	
	public void setPassTemp( String passTemp) {
		this.passTemp = passTemp;
	}
	

	public List<Broadcast> getBroadcasts() {
		return this.broadcasts;
	}

	public void setBroadcasts(List<Broadcast> broadcasts) {
		this.broadcasts = broadcasts;
	}

	public Broadcast addBroadcast(Broadcast broadcast) {
		getBroadcasts().add(broadcast);
		broadcast.setPublisher(this);

		return broadcast;
	}

	public Broadcast removeBroadcast(Broadcast broadcast) {
		getBroadcasts().remove(broadcast);
		broadcast.setPublisher(null);

		return broadcast;
	}

	public List<BroadcastRecipient> getBroadcastRecipients() {
		return this.broadcastRecipients;
	}

	public void setBroadcastRecipients(List<BroadcastRecipient> broadcastRecipients) {
		this.broadcastRecipients = broadcastRecipients;
	}

	public BroadcastRecipient addBroadcastRecipient(BroadcastRecipient broadcastRecipient) {
		getBroadcastRecipients().add(broadcastRecipient);
		broadcastRecipient.setUser(this);

		return broadcastRecipient;
	}

	public BroadcastRecipient removeBroadcastRecipient(BroadcastRecipient broadcastRecipient) {
		getBroadcastRecipients().remove(broadcastRecipient);
		broadcastRecipient.setUser(null);

		return broadcastRecipient;
	}

	public List<GroupMembership> getGroupMemberships() {
		return this.groupMemberships;
	}

	public void setGroupMemberships(List<GroupMembership> groupMemberships) {
		this.groupMemberships = groupMemberships;
	}

	public GroupMembership addGroupMembership(GroupMembership groupMembership) {
		getGroupMemberships().add(groupMembership);
		groupMembership.setUser(this);

		return groupMembership;
	}

	public GroupMembership removeGroupMembership(GroupMembership groupMembership) {
		getGroupMemberships().remove(groupMembership);
		groupMembership.setUser(null);

		return groupMembership;
	}

	public List<Message> getMessages() {
		return this.messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public Message addMessage(Message message) {
		getMessages().add(message);
		message.setUser(this);

		return message;
	}

	public Message removeMessage(Message message) {
		getMessages().remove(message);
		message.setUser(null);

		return message;
	}

	public List<OrganizationMembership> getOrganizationMemberships() {
		return this.organizationMemberships;
	}

	public void setOrganizationMemberships(List<OrganizationMembership> organizationMemberships) {
		this.organizationMemberships = organizationMemberships;
	}

	public OrganizationMembership addOrganizationMembership(OrganizationMembership organizationMembership) {
		getOrganizationMemberships().add(organizationMembership);
		organizationMembership.setUser(this);
		return organizationMembership;
	}

	public OrganizationMembership removeOrganizationMembership(OrganizationMembership organizationMembership) {
		getOrganizationMemberships().remove(organizationMembership);
		organizationMembership.setUser(null);

		return organizationMembership;
	}

	public List<UserPhoneNumber> getUserPhoneNumbers() {
		return this.userPhoneNumbers;
	}

	public void setUserPhoneNumbers(List<UserPhoneNumber> userPhoneNumbers) {
		this.userPhoneNumbers = userPhoneNumbers;
	}

	public UserPhoneNumber addUserPhoneNumber(UserPhoneNumber userPhoneNumber) {
		getUserPhoneNumbers().add(userPhoneNumber);
		userPhoneNumber.setUser(this);

		return userPhoneNumber;
	}

	public UserPhoneNumber removeUserPhoneNumber(UserPhoneNumber userPhoneNumber) {
		getUserPhoneNumbers().remove(userPhoneNumber);
		userPhoneNumber.setUser(null);

		return userPhoneNumber;
	}
	
	public HashMap<String,HashMap<String,String>> getMembership()
	{
		System.out.println("User Name:"+getName());
		/*
		 * Response is of the format: organizationName:{ group1Id : group1Name ....} 
		 */
		HashMap<String,HashMap<String,String>> response=  new HashMap<String, HashMap<String,String>>();
		for (OrganizationMembership membership:organizationMemberships)
		{
			Organization organization=membership.getOrganization();
			System.out.println("Organization name:"+organization.getName());
			HashMap<String,String> organizationMap = new HashMap<String, String>();
			for (GroupMembership groupMembership: groupMemberships)
			{
				Group group=groupMembership.getGroup();
				System.out.println("Group name:"+group.getName());
				if( organization.containsGroup(group))
				{
					organizationMap.put(new Integer(group.getGroupId()).toString(), group.getName());
				}
			}
			response.put(organization.getName(), organizationMap);//Adding groups of the organization to response
		}
		return response;
	}

}
