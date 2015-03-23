package webapp.model.entities;

import java.io.Serializable;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;


/**
 * The persistent class for the preset_quantity database table.
 * 
 */
@Entity
@Table(name="preset_quantity")
public class PresetQuantity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="preset_quantity_id")
	private int presetQuantityId;

	private float quantity;

	//bi-directional many-to-one association to Organization
	@ManyToOne
	@JoinColumn(name="organization_id")
	private Organization organization;

	//bi-directional many-to-one association to ProductType
	@ManyToOne
	@JoinColumn(name="product_type_id")
	private ProductType productType;

	public PresetQuantity() {
	}

	public PresetQuantity(Organization organization, ProductType productType, float quantity) {
		this.organization = organization;
		this.productType = productType;
		this.quantity = quantity;
	}

	public int getPresetQuantityId() {
		return this.presetQuantityId;
	}

	public void setPresetQuantityId(int presetQuantityId) {
		this.presetQuantityId = presetQuantityId;
	}

	public float getQuantity() {
		return this.quantity;
	}

	public void setQuantity(float quantity) {
		this.quantity = quantity;
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

	@JsonProperty(value="productTypeId")
	@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="productTypeId")
	@JsonIdentityReference(alwaysAsId=true)
	public ProductType getProductType() {
		return this.productType;
	}

	public void setProductType(ProductType productType) {
		this.productType = productType;
	}

}