package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.sql.Timestamp;


import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	
	//FOR DECALRING UPLOAD LOCATION FOR HEROKU
	private String uploadLocation; 
	
	public HomeController(@Value("${upload.location}") String uploadLocation) throws IOException
	{
		this.uploadLocation = uploadLocation;
		
		var uploadPath = Paths.get(uploadLocation);
		
		if (!Files.exists(uploadPath))
		{
			Files.createDirectory(uploadPath);
		}
		
	}
	
	
	// Home Handler
	@RequestMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	// About Handler
	@RequestMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}

	// Signup Handler
	@RequestMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Register - Smart Contact Manager");
		m.addAttribute("user", new User());
		return "signup";
	}

	
	
	// This is a handler is for registering User
	@RequestMapping(path = "/do_register", method = RequestMethod.POST)
	public String registerUser(
			@Valid @ModelAttribute("user") User user, 
			BindingResult bindingResult,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, 
			@RequestParam("profileImage") MultipartFile imageFile,
			Model m, 
			HttpSession s) {
		try {
			if (!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}

			if (bindingResult.hasErrors()) {
				System.out.println("ERROR: " + bindingResult.toString());
				m.addAttribute("user", user);
				return "signup";
			}
			
			
			if (imageFile.isEmpty())
			{
				//if the file is empty then try your message
				System.out.println("File is Empty");
				user.setImageUrl("user_default .png");
			}
			else {
//				//upload the file to the folder and update the image file name to the user
//				user.setImageUrl(imageFile.getOriginalFilename());
//				
//				File saveFile = new ClassPathResource("/static/img/").getFile();
//				
//				//to make contact image different with same name for different user add extra pathfile name like date or something
//				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + imageFile.getOriginalFilename());
//				
//				Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				
				
				//HEROKU TEST UPLOADING PROFILE PIC
				
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				
				//upload the file to the folder and update the image file name to the user
				String imageFileName = user.getName() + timestamp.getTime() + imageFile.getOriginalFilename();
				
				user.setImageUrl(imageFileName);
				
				var filename = imageFileName;
				var dest = Paths.get(uploadLocation + "/" + filename);
					
				Files.copy(imageFile.getInputStream(), dest);
					
				
				System.out.println("IMAGE SAVE FORMAT ID : " + user.getName() + timestamp.getTime() + imageFile.getOriginalFilename() );
				System.out.println("DEST: " + dest);
				System.out.println("User Image is Uploaded");
			}
			
			

			user.setRole("ROLE_USER");
			user.setEnabled(true);
//			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			User result_user = this.userRepository.save(user);

			m.addAttribute("user", user);
			m.addAttribute("user", new User());
			s.setAttribute("message", new Message("Successfully Registered!!", "alert-success"));

			System.out.println("Agreement " + agreement);
			System.out.println("USER " + result_user);

			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user", user);
			s.setAttribute("message", new Message("Something Went Wrong !! " + e.getMessage(), "alert-danger"));
			return "signup";
		}
	}
	
	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model m)
	{
		m.addAttribute("title", "Login Page - Smart Contact Manager");
		return "login";
	}
	
	//delete User Handler
	@GetMapping("/delete/{id}")
	public String deleteUser(@PathVariable("id") Integer id, HttpSession s, Principal p)
	{
		User loggedUser = this.userRepository.getUserByUserName(p.getName());

		
		User user = this.userRepository.findById(id).get();
		

		if (user.getId() == loggedUser.getId())
		{
			this.userRepository.delete(user);
			
				//deleting contact profile photo
				String imageFileName = user.getImageUrl();
				File userFile;
				
				//if image is not default.png
				if (!imageFileName.equals("user_default .png")) {
					
					try {
						
//						userFile = new ClassPathResource("/static/img/").getFile();
//						Path path = Paths.get(userFile.getAbsolutePath() + File.separator + imageFileName);
						
						
						
						//HEROKU FILE UPLOADING PATH
						
						var dest = Paths.get(uploadLocation + "/" + imageFileName);
						Files.deleteIfExists(dest);
					} 
					catch (IOException e) {
						System.out.println("No User Image File");
						e.printStackTrace();
					}
				}
				
				s.setAttribute("message", new Message("Your Account Has Been Deleted Successfully", "alert-info"));
		}
		
		else
		{
			s.setAttribute("message", new Message("Unauthorised Account Deletion", "danger"));
			return "redirect:/user/profile";
		}
		
		return "redirect:/signup";
	}
	
	//updating User Handler
	@PostMapping("/update-user/{id}")
	public String updateUser(@PathVariable("id") int id, Model m)
	{
		m.addAttribute("title", "Update User");
		
		User user = this.userRepository.findById(id).get();
		m.addAttribute("user", user);
		
		return "normal/settings";
	}

	//Process update user handler 
	@RequestMapping(value = "/process-update-user/{id}", method = RequestMethod.POST)
	public String processUpdateHandler(
			@PathVariable("id") int id,
			@Valid @ModelAttribute User user, 
			BindingResult result,
			@RequestParam("profileImage") MultipartFile imgFile, 
			Model m,
			HttpSession session,
			Principal p)
	{
		
		try {
			
			//setting user id coming from URL
			user.setId(id);
			
			//old user details
			User oldUserDetail = this.userRepository.findById(user.getId()).get();
			
			
			if (!imgFile.isEmpty())
			{
				//rewrite file
				
				//if image is not default.png
				if (!oldUserDetail.getImageUrl().equals("user_default .png")) {
					
					//delete old photo
					
//					//old way (new way is done in delete handler by me)
//					File deleteFile = new ClassPathResource("/static/img/").getFile();
//					File f1 = new File(deleteFile, oldUserDetail.getImageUrl());
//					f1.delete();
					
					
					//HEROKU FILE DELETION
					
					//old way (new way is done in delete handler by me)
					File deleteFile = new File(uploadLocation + "/" + oldUserDetail.getImageUrl());
					deleteFile.delete();
				}
				//update new photo
				
//				File saveFile = new ClassPathResource("/static/img/").getFile();				
//				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + imgFile.getOriginalFilename());
//				
//				Files.copy(imgFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//				user.setImageUrl(imgFile.getOriginalFilename());
				
				//HEROKU FILE UPLOADING
				
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String imageFileName = oldUserDetail.getName() + timestamp.getTime() + imgFile.getOriginalFilename();
				
				
				var filename = imageFileName;
				var dest = Paths.get(uploadLocation + "/" + filename);
									
				Files.copy(imgFile.getInputStream(), dest);
					
			
				
				user.setImageUrl(imageFileName);
			
			
			
			}
			else
			{
				user.setImageUrl(oldUserDetail.getImageUrl());
			}
			
			
			if (result.hasErrors()) {
				System.out.println("ERROR: " + result.toString());
				m.addAttribute("user", user);
				return "update_user_form";
			}
			
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setContacts(oldUserDetail.getContacts());
			
			User savedUser = this.userRepository.save(user);
			
			System.out.println("Updated User: " + savedUser );
			
			session.setAttribute("message", new Message("Your Account Data is Updated!!", "info"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("User Name: " + user.getName()+ "\nUser ID: " + user.getId());
		
		return "normal/profile";
	}
}
