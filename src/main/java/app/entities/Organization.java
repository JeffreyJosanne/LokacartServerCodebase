package app.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import app.entities.broadcast.Broadcast;


/**
 * The persistent class for the organization database table.
 * 
 */
@Entity
@Table(name="organization")
public class Organization implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="organization_id")
	private int organizationId;

	private String abbreviation;

	private String address;

	private String contact;

	@Column(name="default_call_locale")
	private String defaultCallLocale;

	@Column(name="default_web_locale")
	private String defaultWebLocale;

	@Column(name="enable_billing")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean enableBilling;

	@Column(name="enable_broadcasts")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean enableBroadcasts;

	@Column(name="enable_feedbacks")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean enableFeedbacks;

	@Column(name="enable_order_cancellation")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean enableOrderCancellation;

	@Column(name="enable_responses")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean enableResponses;

	@Column(name="enable_sms")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean enableSms;

	@Column(name="inbound_call_ask_feedback")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean inboundCallAskFeedback;

	@Column(name="inbound_call_ask_order")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean inboundCallAskOrder;

	@Column(name="inbound_call_ask_response")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean inboundCallAskResponse;

	@Column(name="inbound_call_groupwise_latest_broadcast")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean inboundCallGroupwiseLatestBroadcast;

	@Column(name="inbound_call_play_latest_broadcast")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean inboundCallPlayLatestBroadcast;

	@Column(name="incoming_sms_code")
	private String incomingSmsCode;

	@Column(name="ivr_number")
	private String ivrNumber;

	@Column(name="auto_approve")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean autoApprove;
	
	
	private String name;
	
	/*
	 * These columns will be added to Beta version
	@Column(name="has_only_inbox")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean hasOnlyInbox;
	
	@Column(name="has_feedback")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean hasFeedback;
	
	@Column(name="has_response")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean hasResponse;
	
	@Column(name="has_text_message_response")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean hasTextMessageResponse;
	
	@Column(name="has_bill")
	@Type(type="org.hibernate.type.NumericBooleanType")
	private boolean hasBill;
	
	public boolean isHasOnlyInbox() {
		return hasOnlyInbox;
	}

	public void setHasOnlyInbox(boolean hasOnlyInbox) {
		this.hasOnlyInbox = hasOnlyInbox;
	}

	public boolean isHasFeedback() {
		return hasFeedback;
	}

	public void setHasFeedback(boolean hasFeedback) {
		this.hasFeedback = hasFeedback;
	}

	public boolean isHasResponse() {
		return hasResponse;
	}

	public void setHasResponse(boolean hasResponse) {
		this.hasResponse = hasResponse;
	}

	public boolean isHasTextMessageResponse() {
		return hasTextMessageResponse;
	}

	public void setHasTextMessageResponse(boolean hasTextMessageResponse) {
		this.hasTextMessageResponse = hasTextMessageResponse;
	}

	public boolean isHasBill() {
		return hasBill;
	}

	public void setHasBill(boolean hasBill) {
		this.hasBill = hasBill;
	}
	*/
	//bi-directional many-to-one association to Broadcast
	@OneToMany(mappedBy="organization")
	private List<Broadcast> broadcasts;

	//bi-directional many-to-one association to BroadcastDefaultSetting
	@OneToMany(mappedBy="organization")
	private List<BroadcastDefaultSettings> broadcastDefaultSettings;

	//bi-directional many-to-one association to Group
	@OneToMany(mappedBy="organization")
	private List<Group> groups;

	//bi-directional many-to-one association to InboundCall
	@OneToMany(mappedBy="organization")
	private List<InboundCall> inboundCalls;

	//bi-directional many-to-one association to Organization
	@ManyToOne
	@JoinColumn(name="parent_organization_id")
	private Organization parentOrganization;

	//bi-directional many-to-one association to Organization
	@OneToMany(mappedBy="parentOrganization")
	private List<Organization> subOrganizations;

	//bi-directional many-to-one association to OrganizationMembership
	@OneToMany(mappedBy="organization")
	private List<OrganizationMembership> organizationMemberships;

	//bi-directional many-to-one association to PresetQuantity
	@OneToMany(mappedBy="organization")
	private List<PresetQuantity> presetQuantities;

	//bi-directional many-to-one association to ProductType
	@OneToMany(mappedBy="organization")
	private List<ProductType> productTypes;

	//bi-directional many-to-one association to Order
	@OneToMany(mappedBy="organization")
	private List<Order> orders;

	//bi-directional many-to-one association to WelcomeMessage
	@OneToMany(mappedBy="organization")
	private List<WelcomeMessage> welcomeMessages;

	//bi-directional many-to-one association to LatestRecordedVoice
	@OneToMany(mappedBy="organization")
	private List<LatestRecordedVoice> latestRecordedVoices;
	
	public Organization() {
	}

	public Organization(String name, String abbreviation, String ivrNumber, String incomingSmsCode, String address,
			String contact, Organization parentOrganization, String defaultWebLocale, String defaultCallLocale) {

		this.name = name;
		this.abbreviation = abbreviation;
		this.ivrNumber = ivrNumber;
		this.incomingSmsCode = incomingSmsCode;
		this.address = address;
		this.contact = contact;
		this.parentOrganization = parentOrganization;
		this.defaultWebLocale = defaultWebLocale;
		this.defaultCallLocale = defaultCallLocale;
	}

	public int getOrganizationId() {
		return this.organizationId;
	}

	public void setOrganizationId(int organizationId) {
		this.organizationId = organizationId;
	}

	public String getAbbreviation() {
		return this.abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getContact() {
		return this.contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getDefaultCallLocale() {
		return this.defaultCallLocale;
	}

	public void setDefaultCallLocale(String defaultCallLocale) {
		this.defaultCallLocale = defaultCallLocale;
	}

	public String getDefaultWebLocale() {
		return this.defaultWebLocale;
	}

	public void setDefaultWebLocale(String defaultWebLocale) {
		this.defaultWebLocale = defaultWebLocale;
	}

	public boolean getEnableBilling() {
		return this.enableBilling;
	}

	public void setEnableBilling(boolean enableBilling) {
		this.enableBilling = enableBilling;
	}

	public boolean getEnableBroadcasts() {
		return this.enableBroadcasts;
	}

	public void setEnableBroadcasts(boolean enableBroadcasts) {
		this.enableBroadcasts = enableBroadcasts;
	}

	public boolean getEnableFeedbacks() {
		return this.enableFeedbacks;
	}

	public void setEnableFeedbacks(boolean enableFeedbacks) {
		this.enableFeedbacks = enableFeedbacks;
	}

	public boolean getEnableOrderCancellation() {
		return this.enableOrderCancellation;
	}

	public void setEnableOrderCancellation(boolean enableOrderCancellation) {
		this.enableOrderCancellation = enableOrderCancellation;
	}

	public boolean getEnableResponses() {
		return this.enableResponses;
	}

	public void setEnableResponses(boolean enableResponses) {
		this.enableResponses = enableResponses;
	}

	public boolean getEnableSms() {
		return this.enableSms;
	}

	public void setEnableSms(boolean enableSms) {
		this.enableSms = enableSms;
	}

	public boolean getInboundCallAskFeedback() {
		return this.inboundCallAskFeedback;
	}

	public void setInboundCallAskFeedback(boolean inboundCallAskFeedback) {
		this.inboundCallAskFeedback = inboundCallAskFeedback;
	}

	public boolean getInboundCallAskOrder() {
		return this.inboundCallAskOrder;
	}

	public void setInboundCallAskOrder(boolean inboundCallAskOrder) {
		this.inboundCallAskOrder = inboundCallAskOrder;
	}

	public boolean getInboundCallAskResponse() {
		return this.inboundCallAskResponse;
	}

	public void setInboundCallAskResponse(boolean inboundCallAskResponse) {
		this.inboundCallAskResponse = inboundCallAskResponse;
	}

	public boolean getInboundCallGroupwiseLatestBroadcast() {
		return this.inboundCallGroupwiseLatestBroadcast;
	}

	public void setInboundCallGroupwiseLatestBroadcast(boolean inboundCallGroupwiseLatestBroadcast) {
		this.inboundCallGroupwiseLatestBroadcast = inboundCallGroupwiseLatestBroadcast;
	}

	public boolean getInboundCallPlayLatestBroadcast() {
		return this.inboundCallPlayLatestBroadcast;
	}

	public void setInboundCallPlayLatestBroadcast(boolean inboundCallPlayLatestBroadcast) {
		this.inboundCallPlayLatestBroadcast = inboundCallPlayLatestBroadcast;
	}
	
	
	public boolean getAutoApprove() {
		return this.autoApprove;
	}
	
	
	public void setAutoApprove(boolean autoApprove) {
		this.autoApprove = autoApprove;
	}

	public String getIncomingSmsCode() {
		return this.incomingSmsCode;
	}

	public void setIncomingSmsCode(String incomingSmsCode) {
		this.incomingSmsCode = incomingSmsCode;
	}

	public String getIvrNumber() {
		return this.ivrNumber;
	}

	public void setIvrNumber(String ivrNumber) {
		this.ivrNumber = ivrNumber;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Broadcast> getBroadcasts() {
		return this.broadcasts;
	}

	public void setBroadcasts(List<Broadcast> broadcasts) {
		this.broadcasts = broadcasts;
	}

	public Broadcast addBroadcast(Broadcast broadcast) {
		getBroadcasts().add(broadcast);
		broadcast.setOrganization(this);

		return broadcast;
	}

	public Broadcast removeBroadcast(Broadcast broadcast) {
		getBroadcasts().remove(broadcast);
		broadcast.setOrganization(null);

		return broadcast;
	}

	public List<BroadcastDefaultSettings> getBroadcastDefaultSettings() {
		return this.broadcastDefaultSettings;
	}

	public void setBroadcastDefaultSettings(List<BroadcastDefaultSettings> broadcastDefaultSettings) {
		this.broadcastDefaultSettings = broadcastDefaultSettings;
	}

	public BroadcastDefaultSettings addBroadcastDefaultSetting(BroadcastDefaultSettings broadcastDefaultSetting) {
		getBroadcastDefaultSettings().add(broadcastDefaultSetting);
		broadcastDefaultSetting.setOrganization(this);

		return broadcastDefaultSetting;
	}

	public BroadcastDefaultSettings removeBroadcastDefaultSetting(BroadcastDefaultSettings broadcastDefaultSetting) {
		getBroadcastDefaultSettings().remove(broadcastDefaultSetting);
		broadcastDefaultSetting.setOrganization(null);

		return broadcastDefaultSetting;
	}

	public List<Group> getGroups() {
		return this.groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public Group addGroup(Group group) {
		getGroups().add(group);
		group.setOrganization(this);

		return group;
	}

	public Group removeGroup(Group group) {
		getGroups().remove(group);
		group.setOrganization(null);

		return group;
	}

	public List<InboundCall> getInboundCalls() {
		return this.inboundCalls;
	}

	public void setInboundCalls(List<InboundCall> inboundCalls) {
		this.inboundCalls = inboundCalls;
	}

	public InboundCall addInboundCall(InboundCall inboundCall) {
		getInboundCalls().add(inboundCall);
		inboundCall.setOrganization(this);

		return inboundCall;
	}

	public InboundCall removeInboundCall(InboundCall inboundCall) {
		getInboundCalls().remove(inboundCall);
		inboundCall.setOrganization(null);

		return inboundCall;
	}

	public Organization getParentOrganization() {
		return this.parentOrganization;
	}

	public void setParentOrganization(Organization parentOrganization) {
		this.parentOrganization = parentOrganization;
	}

	public List<Organization> getSubOrganizations() {
		return this.subOrganizations;
	}

	public void setSubOrganizations(List<Organization> subOrganizations) {
		this.subOrganizations = subOrganizations;
	}

	public Organization addSubOrganization(Organization subOrganization) {
		getSubOrganizations().add(subOrganization);
		subOrganization.setParentOrganization(this);

		return subOrganization;
	}

	public Organization removeSubOrganization(Organization subOrganization) {
		getSubOrganizations().remove(subOrganization);
		subOrganization.setParentOrganization(null);

		return subOrganization;
	}

	public List<OrganizationMembership> getOrganizationMemberships() {
		return this.organizationMemberships;
	}

	public void setOrganizationMemberships(List<OrganizationMembership> organizationMemberships) {
		this.organizationMemberships = organizationMemberships;
	}

	public OrganizationMembership addOrganizationMembership(OrganizationMembership organizationMembership) {
		getOrganizationMemberships().add(organizationMembership);
		organizationMembership.setOrganization(this);
		//organizationMembership.setStatus(1);
		return organizationMembership;
	}

	public OrganizationMembership removeOrganizationMembership(OrganizationMembership organizationMembership) {
		getOrganizationMemberships().remove(organizationMembership);
		organizationMembership.setOrganization(null);

		return organizationMembership;
	}

	public List<PresetQuantity> getPresetQuantities() {
		return this.presetQuantities;
	}

	public void setPresetQuantities(List<PresetQuantity> presetQuantities) {
		this.presetQuantities = presetQuantities;
	}

	public PresetQuantity addPresetQuantity(PresetQuantity presetQuantity) {
		getPresetQuantities().add(presetQuantity);
		presetQuantity.setOrganization(this);

		return presetQuantity;
	}

	public PresetQuantity removePresetQuantity(PresetQuantity presetQuantity) {
		getPresetQuantities().remove(presetQuantity);
		presetQuantity.setOrganization(null);

		return presetQuantity;
	}

	public List<ProductType> getProductTypes() {
		return this.productTypes;
	}

	public void setProductTypes(List<ProductType> productTypes) {
		this.productTypes = productTypes;
	}

	public ProductType addProductType(ProductType productType) {
		getProductTypes().add(productType);
		productType.setOrganization(this);

		return productType;
	}

	public ProductType removeProductType(ProductType productType) {
		getProductTypes().remove(productType);
		productType.setOrganization(null);

		return productType;
	}

	public List<Order> getOrders() {
		return this.orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public Order addOrder(Order order) {
		getOrders().add(order);
		order.setOrganization(this);

		return order;
	}

	public Order removeOrder(Order order) {
		getOrders().remove(order);
		order.setOrganization(null);

		return order;
	}

	public List<WelcomeMessage> getWelcomeMessages() {
		return this.welcomeMessages;
	}

	public void setWelcomeMessages(List<WelcomeMessage> welcomeMessages) {
		this.welcomeMessages = welcomeMessages;
	}

	public WelcomeMessage addWelcomeMessage(WelcomeMessage welcomeMessage) {
		getWelcomeMessages().add(welcomeMessage);
		welcomeMessage.setOrganization(this);

		return welcomeMessage;
	}

	public WelcomeMessage removeWelcomeMessage(WelcomeMessage welcomeMessage) {
		getWelcomeMessages().remove(welcomeMessage);
		welcomeMessage.setOrganization(null);

		return welcomeMessage;
	}

	public List<LatestRecordedVoice> getLatestRecordedVoices() {
		return this.latestRecordedVoices;
	}

	public void setLatestRecordedVoices(List<LatestRecordedVoice> latestRecordedVoices) {
		this.latestRecordedVoices = latestRecordedVoices;
	}

	public LatestRecordedVoice addLatestRecordedVoice(LatestRecordedVoice latestRecordedVoice) {
		getLatestRecordedVoices().add(latestRecordedVoice);
		latestRecordedVoice.setOrganization(this);

		return latestRecordedVoice;
	}

	public LatestRecordedVoice removeLatestRecordedVoice(LatestRecordedVoice latestRecordedVoice) {
		getLatestRecordedVoices().remove(latestRecordedVoice);
		latestRecordedVoice.setOrganization(null);

		return latestRecordedVoice;
	}

	public boolean containsGroup(Group group)
	{
		if(groups.contains(group))
		{
			return true;
		}
		return false;
	}
}
