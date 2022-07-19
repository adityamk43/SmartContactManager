package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	
	//FOR DECALRING UPLOAD LOCATION FOR HEROKU
		private String uploadLocation; 
		
		public UserController(@Value("${upload.location}") String uploadLocation) throws IOException
		{
			this.uploadLocation = uploadLocation;
			
			var uploadPath = Paths.get(uploadLocation);
			
			if (!Files.exists(uploadPath))
			{
				Files.createDirectory(uploadPath);
			}
			
		}
	
	
	
	//method for adding common data to response
	@ModelAttribute
	public void commonData(Model m, Principal principal)
	{
	
		String userName = principal.getName();
		System.out.println("Common Data Model Attribute Username: " + userName);
		
		//get the user using username (email here)
		User user = userRepository.getUserByUserName(userName);
		System.out.println(user);
		m.addAttribute("user", user);
		
	}
	
	//user home dashboard handler
	@RequestMapping("/index")
	public String dashboard(Model m, Principal p)
	{
		String email = p.getName();
		User u = userRepository.getUserByUserName(email);
		
		m.addAttribute("title", "User Dashboard - " + u.getName() );
		return "normal/user_dashboard";
	}
	
	//open add contact form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model m)
	{
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile imageFile,
			Principal p, 
			HttpSession session)
	{
		try {
			String email = p.getName();
			User u = userRepository.getUserByUserName(email);
			
			contact.setUser(u);	
			u.getContacts().add(contact);
			
			if (imageFile.isEmpty())
			{
				//if the file is empty then try your message
				System.out.println("File is Empty");
				contact.setImage("default.jpg");
			}
			else {
				//upload the file to the folder and update the image file name to the contact
//				contact.setImage(imageFile.getOriginalFilename());
//				
//				File saveFile = new ClassPathResource("/static/img/").getFile();
//				
//				//to make contact image different with same name for different user add extra pathfile name like date or something
//				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + imageFile.getOriginalFilename());
//				
//				Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				
				
				//HEROKU UPLOAD CONTACT PROFILE PIC
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String imageFileName = contact.getName() + timestamp.getTime() + imageFile.getOriginalFilename();
				
				contact.setImage(imageFileName);
				
				var filename = imageFileName;
				var dest = Paths.get(uploadLocation + "/" + filename);
				
				Files.copy(imageFile.getInputStream(), dest);
				
				
				System.out.println("Contact Image is Uploaded");
			}
			
			this.userRepository.save(u);
			
			System.out.println(contact);
			System.out.println("Added to database");
			
			//message success..
			session.setAttribute("message", new Message("Contact Saved Successfully!! You can add more....", "success"));
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("CONTACT PROCESSING ERROR: " + e.getMessage());
			
			//message error...
			session.setAttribute("message", new Message("Something Went Wrong!!", "danger"));
			
		}
		
		return "normal/add_contact_form";
	}
	
	//show contacts handler
	//per page 5[n]  n-number of contacts per page
	//current page 0 [page] page=current page number
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") int page, Model m, Principal principal)
	{
		
		m.addAttribute("title", "Show User Contacts");
		
		//sending contact lists from here from database
		/*
		 * String email = principal.getName();
		 * 
		 * User user = userRepository.getUserByUserName(email); List<Contact> contacts =
		 * user.getContacts();
		 * 
		 */
		
		//Will be using another way so that we can also implement pagination
		String email = principal.getName();
		User user = this.userRepository.getUserByUserName(email);
		
		//Current Page-page
		//Contact per page - 5
		//Pageable is a parent interface of PageRequest so we can story PageRequest object in Pageable class object
		Pageable paegable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), paegable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}

	//showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model m, Principal principal)
	{
		System.out.println(cId);
		Optional<Contact> optionalcontacts = this.contactRepository.findById(cId);
		Contact contact = optionalcontacts.get();	
		
		//getting logged in user id to remove security bug of accessing other user full contact derail page
		String email = principal.getName();
		User user = this.userRepository.getUserByUserName(email);
		m.addAttribute("title", "Contact Details");
		
		
		if (user.getId() == contact.getUser().getId())
		{
			m.addAttribute("contact", contact);
			m.addAttribute("title", contact.getName());
			
		}
		
		
		//One way for else or we can use th:if in card to give some message their for unauthroization access
//		else
//			return "404_error_page";
		
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Principal principal, HttpSession s)
	{
		String email = principal.getName();
		User user = this.userRepository.getUserByUserName(email);
		

		Optional<Contact> optionalContacts = this.contactRepository.findById(cId);
		
		Contact contact = optionalContacts.get();
		
		
		if (user.getId() == contact.getUser().getId())
		{
			/*
			 * The contact value was only setting user id null but was not getting deleted
			 * from db due to syncing problem of cascade working!!
			 */
			
//			contact.setUser(null);
//			this.contactRepository.delete(contact);
//			this.contactRepository.flush();
			
			
			/* Solving bug of contact not getting deleted */
			
			/*
			 * This will match and remove the contact object from contacts list using
			 * override equals function we did in Contact.java
			 * 
			 * Remember to put OrphanRemoval=true in @OneToMany annotation of User class
			 * this will help detach that contact from contacts list as cascading all is
			 * implemented in this project
			 */
			
			user.getContacts().remove(contact);
			
			this.userRepository.save(user);
			
			
			//deleting contact profile photo
			
//			String imageFileName = contact.getImage();
//			File contactFile;
//			
//			//if image is not default.png
//			if (!imageFileName.equals("default.jpg")) {
//				
//				try {
//					
//					contactFile = new ClassPathResource("/static/img/").getFile();
//					Path path = Paths.get(contactFile.getAbsolutePath() + File.separator + imageFileName);
//					Files.deleteIfExists(path);
//				} 
//				catch (IOException e) {
//					System.out.println("No Contact Image File");
//					e.printStackTrace();
//				}
//			}
			
			
			//HEROKU DELETE CONTACT PROFILE PIC
			
			String imageFileName = contact.getImage();
			
			//if image is not default.png
			if (!imageFileName.equals("default.jpg")) {
				
				try {
					
					var dest = Paths.get(uploadLocation + "/" + imageFileName);
					Files.deleteIfExists(dest);

				} 
				catch (IOException e) {
					System.out.println("No Contact Image File");
					e.printStackTrace();
				}
			}
			
			
			s.setAttribute("message", new Message("Contact Deleted Successfully", "info"));
		}
		else
		{
			s.setAttribute("message", new Message("Unauthorised or Unavailable Contact Deletion", "danger"));
		}

		return "redirect:/user/show-contacts/0";
	}

	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") int cId, Model m)
	{
		m.addAttribute("title", "Update Contact");
		
		Contact contact = this.contactRepository.findById(cId).get();
		m.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	//Process update contact handler 
	@RequestMapping(value = "/process-update/{cid}", method = RequestMethod.POST)
	public String processUpdateHandler(
			@PathVariable("cid") int cId,
			@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile imgFile, 
			Model m,
			HttpSession session,
			Principal p)
	{
		
		try {
			
			//setting contact id coming from URL
			contact.setcId(cId);
			
			//old contact details
			Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			if (!imgFile.isEmpty())
			{
				//rewrite file
				
				//if image is not default.png
				if (!oldcontactDetail.getImage().equals("default.jpg")) {
					
					//delete old photo
					
//					//old way (new way is done in delete handler by me)
//					File deleteFile = new ClassPathResource("/static/img/").getFile();
//					File f1 = new File(deleteFile, oldcontactDetail.getImage());
//					f1.delete();
					
					//HEROKU DELETE CONTACT PROFILE PIC
					
					var dest = Paths.get(uploadLocation + "/" + oldcontactDetail.getImage());
					Files.deleteIfExists(dest);
					
				}
				//update new photo
				
//				File saveFile = new ClassPathResource("/static/img/").getFile();				
//				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + imgFile.getOriginalFilename());
//				
//				Files.copy(imgFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//				
//				contact.setImage(imgFile.getOriginalFilename());
				
				//HEROKU CONTACT FILE UPLOADING
				
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String imageFileName = oldcontactDetail.getName() + timestamp.getTime() + imgFile.getOriginalFilename();
				
				
				var filename = imageFileName;
				var dest = Paths.get(uploadLocation + "/" + filename);
									
				Files.copy(imgFile.getInputStream(), dest);
					
				contact.setImage(imageFileName);
				
				
			
			
			}
			else
			{
				contact.setImage(oldcontactDetail.getImage());
			}
			
			//linking User
			User user = this.userRepository.getUserByUserName(p.getName());
			contact.setUser(user);
			
			Contact savedContact = this.contactRepository.save(contact);
			System.out.println("Updated Contact: " + savedContact );
			
			session.setAttribute("message", new Message("Your Contact is Updated!!", "info"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Contact Name: " + contact.getName()+ "\nContact ID: " + contact.getcId());
		
		return "redirect:/user/" + contact.getcId() + "/contact";
	}

	//Your Profile Handler
	@GetMapping("/profile")
	public String yourProfile(Model m)
	{
		m.addAttribute("title", "Profile page");
		return "normal/profile";
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings(Model m)
	{
		m.addAttribute("title", "Settings");
		return "normal/settings";
	}

	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(
			@RequestParam("oldPassword") String oldPassword, 
			@RequestParam("newPassword") String newPassword,
			Principal principal,
			HttpSession s
			)
	{
		String email = principal.getName();
		
		User currentUser = this.userRepository.getUserByUserName(email);
		
		
		System.out.println("oldPassword: " + oldPassword);
		System.out.println("newPassword: " + newPassword);
		System.out.println(currentUser.getPassword());
		
		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			
			s.setAttribute("message", new Message("Your Password is changed successfully!!", "success"));
		}
		else {
			//error.. or doesn't change password
			s.setAttribute("message", new Message("Invalid Password!!\nPlease Enter Correct Old Password", "danger"));
			
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
}
