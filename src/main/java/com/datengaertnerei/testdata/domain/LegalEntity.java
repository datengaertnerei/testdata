/*MIT License

Copyright (c) 2018 Jens Dibbern

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package com.datengaertnerei.testdata.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class LegalEntity {

	private static final int MAX_NAME_LEN = 250;
	private int id;
	String legalEntityIdentifier;
	String name;
	String legalForm;

	PostalAddress legalAddress;
	PostalAddress headquarterAddress;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLegalEntityIdentifier() {
		return legalEntityIdentifier;
	}

	public void setLegalEntityIdentifier(String legalEntityIdentifier) {
		this.legalEntityIdentifier = legalEntityIdentifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null && name.length() > MAX_NAME_LEN) {
			this.name = name.substring(0, MAX_NAME_LEN);
		} else {
			this.name = name;
		}
	}

	public String getLegalForm() {
		return legalForm;
	}

	public void setLegalForm(String legalForm) {
		this.legalForm = legalForm;
	}

	@OneToOne(optional = true, cascade = CascadeType.DETACH)
	@JoinColumn(name = "fk_lg_address")
	public PostalAddress getLegalAddress() {
		return legalAddress;
	}

	public void setLegalAddress(PostalAddress legalAddress) {
		this.legalAddress = legalAddress;
	}

	@OneToOne(optional = true, cascade = CascadeType.DETACH)
	@JoinColumn(name = "fk_hq_address")
	public PostalAddress getHeadquarterAddress() {
		return headquarterAddress;
	}

	public void setHeadquarterAddress(PostalAddress headquarterAddress) {
		this.headquarterAddress = headquarterAddress;
	}
}
