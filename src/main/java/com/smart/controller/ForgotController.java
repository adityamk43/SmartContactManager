package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {

	//for generating otp
	
	//1000 is called seed. Setting seed causes same sequence of numbers after every restart of the controller!!
	//So not good to set seed for OTP stuff
//	Random random = new Random(1000);
	Random random = new Random();
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	
	//emailid form open handler
	
	@RequestMapping("/forgot")
	public String openEmailform(Model m)
	{
		m.addAttribute("title", "Forgot Password");
		
		return "forgot_email_form";
	}
	
	//send otp handler
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession s, Model m)
	{
		System.out.println("EMAIL OTP: " + email);
		
		//Generating OTP of 4 digits
		
		int otp = random.nextInt(999999);
		
		System.out.println("OTP: " + otp);
		
		//write code for send otp to email
		
		String subject ="OTP from SCM";
//		String message = "OTP = " + otp;
		String message =  ""
				+ "<div style='text-align: center; border:1px solid black; border-radius: 50px; box-shadow: 4px 6px 1px grey; background-color: #e2e2e2; padding:20px; color: black;' >"
				+ "<h2>"
				+ "Your OTP is "
				+ "<b style='color: #009688;'>" + otp
				+ "</b>"
				+ "</h1>"
				+ "</div>";
		String to = email;
		
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if (flag)
		{
			s.setAttribute("sessionotp", otp);
			s.setAttribute("email", email);
			m.addAttribute("title", "Verify OTP");
			return "verify_otp";
		}
		else {
			
			s.setAttribute("message", "Check Your Email ID!!");
			
			return "forgot_email_form";
		}
		
	}
	
	
	
	//verify otp
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp") int otp, HttpSession session, Model m)
	{
		int sessionOtp = (int) session.getAttribute("sessionotp"); 
		String email = (String) session.getAttribute("email");
		
		if (sessionOtp == otp)
		{
			//password change form
			
			User user = this.userRepository.getUserByUserName(email);
			
			if (user == null)
			{
				//send error message
				session.setAttribute("message", "User doesn't exist with this Email ID !!");
				
				return "forgot_email_form";
			}
			else {
				//send change password form
				
			}
			
			m.addAttribute("title", "Reset Password");
			
			return "password_change_form";
		}
		else {
			session.setAttribute("message", "You have entered Wrong OTP!!");
			return "verify_otp";
		}
	}
	
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session)
	{
		String email = (String) session.getAttribute("email");
		
		User user = this.userRepository.getUserByUserName(email);
		
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		
		this.userRepository.save(user);
		
		
		
//		session.setAttribute("message", "You have entered Wrong OTP!!");
		
		return "redirect:/signin?change=Password Changed Successfully !!";
		
	}
	
}
