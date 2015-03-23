package webapp.model.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;


/**
 * The persistent class for the welcome_message database table.
 * 
 */
@Entity
@Table(name="welcome_message")
public class WelcomeMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="welcome_message_id")
	private int welcomeMessageId;

	private String locale;

	//bi-directional many-to-one association to Organization
	@ManyToOne
	@JoinColumn(name="organization_id")
	private Organization organization;

	//bi-directional many-to-one association to Voice
	@ManyToOne
	@JoinColumn(name="voice_id")
	private Voice voice;

	public WelcomeMessage() {
	}

	public WelcomeMessage(Organization organization, String locale, Voice voice) {
		this.organization = organization;
		this.locale = locale;
		this.voice = voice;
	}

	public int getWelcomeMessageId() {
		return this.welcomeMessageId;
	}

	public void setWelcomeMessageId(int welcomeMessageId) {
		this.welcomeMessageId = welcomeMessageId;
	}

	public String getLocale() {
		return this.locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	@JsonProperty(value="organizationId")
	@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="organizationId")
	@JsonIdentityReference(alwaysAsId=true)
	public Organization getOrganization() {
		return this.organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	@JsonProperty(value="voiceId")
	@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="voiceId")
	@JsonIdentityReference(alwaysAsId=true)
	public Voice getVoice() {
		return this.voice;
	}

	public void setVoice(Voice voice) {
		this.voice = voice;
	}

}