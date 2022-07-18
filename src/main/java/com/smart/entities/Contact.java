package com.smart.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name= "CONTACT")
public class Contact {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int cId;
	private String name;
	private String nickname;
	private String work;
	private String email;
	private String image;
	//for Long TEXT Datatype to store long contact descriptions...
	@Lob
//	@Column(length = 1000)
	private String description;
	private String phone;
	
	@ManyToOne
	@JsonIgnore
	private User user;

	public int getcId() {
		return cId;
	}

	public void setcId(int cId) {
		this.cId = cId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}


	/*
	 * @Override public String toString() { return "Contact [cId=" + cId + ", name="
	 * + name + ", nickname=" + nickname + ", work=" + work + ", email=" + email +
	 * ", image=" + image + ", description=" + description + ", phone=" + phone +
	 * ", user=" + user + "]"; }
	 */
	
	
	//This Overriding equals method is used so that list.remove() function can use 
	//contact ID as a matching parameter to remove that particulat Contact ID from the user's Contacts List
	@Override
	public boolean equals(Object obj) {
		
		return this.cId== ((Contact) obj).getcId();
	}
	
	

}
