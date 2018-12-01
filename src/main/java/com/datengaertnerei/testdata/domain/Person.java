package com.datengaertnerei.testdata.domain;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Person {

	private int id;
	private String givenName;
	private String familyName;
	private String birthName;
	private String gender;
	private LocalDate birthDate;
	private int height;
	private String eyecolor;
	private String email;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getBirthName() {
		return birthName;
	}

	public void setBirthName(String birthName) {
		this.birthName = birthName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getEyecolor() {
		return eyecolor;
	}

	public void setEyecolor(String eyecolor) {
		this.eyecolor = eyecolor;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public Person() {
	}

	public Person(String firstname, String surname, String gender, LocalDate birthDate, int height, String eyecolor,
			String email) {
		this.givenName = firstname;
		this.familyName = surname;
		this.gender = gender;
		this.birthDate = birthDate;
		this.height = height;
		this.eyecolor = eyecolor;
		this.email = email;
	}
}
