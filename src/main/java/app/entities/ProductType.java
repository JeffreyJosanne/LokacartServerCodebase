package app.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;


/**
 * The persistent class for the product_type database table.
 * 
 */
@Entity
@Table(name="product_type")
public class ProductType implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="product_type_id")
	private int productTypeId;

	private String description;

	@Column(name="image_url")
	private String imageUrl;

	private String name;

	//bi-directional many-to-one association to PresetQuantity
	@OneToMany(mappedBy="productType")
	private List<PresetQuantity> presetQuantities;

	//bi-directional many-to-one association to Product
	@OneToMany(fetch = FetchType.EAGER, mappedBy="productType")
	private List<Product> products;

	//bi-directional many-to-one association to Organization
	@ManyToOne
	@JoinColumn(name="organization_id")
	private Organization organization;

	//bi-directional many-to-one association to ProductType
	@ManyToOne
	@JoinColumn(name="parent_product_type_id")
	private ProductType parentProductType;

	//bi-directional many-to-one association to ProductType
	@OneToMany(mappedBy="parentProductType")
	private List<ProductType> subProductTypes;

	public ProductType() {
	}

	public ProductType(String name, Organization organization, ProductType parentProductType, String description,
			String imageUrl) {

		this.name = name;
		this.organization = organization;
		this.parentProductType = parentProductType;
		this.description = description;
		this.imageUrl = imageUrl;
	}

	public int getProductTypeId() {
		return this.productTypeId;
	}

	public void setProductTypeId(int productTypeId) {
		this.productTypeId = productTypeId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImageUrl() {
		return this.imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<PresetQuantity> getPresetQuantities() {
		return this.presetQuantities;
	}

	public void setPresetQuantities(List<PresetQuantity> presetQuantities) {
		this.presetQuantities = presetQuantities;
	}

	public PresetQuantity addPresetQuantity(PresetQuantity presetQuantity) {
		getPresetQuantities().add(presetQuantity);
		presetQuantity.setProductType(this);

		return presetQuantity;
	}

	public PresetQuantity removePresetQuantity(PresetQuantity presetQuantity) {
		getPresetQuantities().remove(presetQuantity);
		presetQuantity.setProductType(null);

		return presetQuantity;
	}

	public List<Product> getProducts() {
		return this.products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public Product addProduct(Product product) {
		getProducts().add(product);
		product.setProductType(this);

		return product;
	}

	public Product removeProduct(Product product) {
		getProducts().remove(product);
		product.setProductType(null);

		return product;
	}

	public Organization getOrganization() {
		return this.organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public ProductType getParentProductType() {
		return this.parentProductType;
	}

	public void setParentProductType(ProductType parentProductType) {
		this.parentProductType = parentProductType;
	}

	public List<ProductType> getSubProductTypes() {
		return this.subProductTypes;
	}

	public void setSubProductTypes(List<ProductType> subProductTypes) {
		this.subProductTypes = subProductTypes;
	}

	public ProductType addSubProductType(ProductType subProductType) {
		getSubProductTypes().add(subProductType);
		subProductType.setParentProductType(this);

		return subProductType;
	}

	public ProductType removeSubProductType(ProductType subProductType) {
		getSubProductTypes().remove(subProductType);
		subProductType.setParentProductType(null);

		return subProductType;
	}

}
