package com.datengaertnerei.testdata.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PostalAddress implements Comparable<PostalAddress> {

	private int id;
	private String addressCountry;
	private String addressLocality;
	private String postalCode;
	private String streetAddress;
	private String houseNumber;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAddressCountry() {
		return addressCountry;
	}

	public void setAddressCountry(String addressCountry) {
		this.addressCountry = addressCountry;
	}

	public String getAddressLocality() {
		return addressLocality;
	}

	public void setAddressLocality(String addressLocality) {
		this.addressLocality = addressLocality;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public String getHouseNumber() {
		return houseNumber;
	}

	public void setHouseNumber(String houseNumber) {
		this.houseNumber = houseNumber;
	}

	public PostalAddress() {
	}

	public PostalAddress(String addressCountry, String addressLocality, String postalCode, String streetAddress,
			String houseNumber) {
		this.addressCountry = addressCountry;
		this.addressLocality = addressLocality;
		this.postalCode = postalCode;
		this.streetAddress = streetAddress;
		this.houseNumber = houseNumber;
	}

	@Override
	public int compareTo(PostalAddress o) {
		StringBuilder match = new StringBuilder().append(this.getAddressCountry()).append(this.getAddressLocality())
				.append(this.getPostalCode()).append(this.getStreetAddress()).append(this.getHouseNumber());
		StringBuilder other = new StringBuilder().append(o.getAddressCountry()).append(o.getAddressLocality())
				.append(o.getPostalCode()).append(o.getStreetAddress()).append(o.getHouseNumber());

		return match.compareTo(other);
	}

}
