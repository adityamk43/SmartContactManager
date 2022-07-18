package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer>{
	
	//pagination
	
	@Query("from Contact as c where c.user.id=:userId order by c.cId desc ")
//	public List<Contact> findContactsByUser(@Param("userId") int userId);
	// for Pagination
	//information to be given to abstract class Pageable =>
	//Current Page-page
	//Contact per page - 5
	public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);
	
	
	//search contact implementation
	public List<Contact> findByNameContainingAndUser(String name, User user);

}
