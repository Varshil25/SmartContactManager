package com.smart.Controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.mail.Session;
import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

	private Random random = new Random();

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

//	email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {

		int otp = 1000 + random.nextInt(9000); // Generate a 4-digit OTP

//		logic for send otp on mail.
		String subject = "OTP From SCM";
		String message = "<div style='border: 1px solid #e2e2e2; padding:20px;'>" + "<h1>OTP for Verification</h1>"
				+ "<p>Dear Customer,</p>" + "<p>Your One Time Password (OTP) for verification is:</p>"
				+ "<h2 style='background-color:#f2f2f2; padding:10px;'>" + otp + "</h2>"
				+ "<p>Do not share your OTP with anyone, including admin officials. "
				+ "Our staff representatives will never ask for your OTP.</p>"
				+ "<p>If you did not request this OTP, please ignore this message.</p>" + "<p>Thank you,</p>"
				+ "<p>Smart Contact Manager</p>" + "</div>";
		String to = email;

		boolean flag = this.emailService.sendEmail(subject, message, to);

		if (flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		} else {
			session.setAttribute("message", new Message("check your email id!!", "alert-danger"));
			return "forgot_email_form";
		}
	}

//	verify-otp handler
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp") int otp, HttpSession session) {
		int myotp = (int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");

		if (myotp == otp) {
//			display password change form
			User user = this.userRepository.getUserByUserName(email);

			if (user == null) {
//				show error
				session.setAttribute("message", "User doesn't exits with this email !!");
				return "forgot_email_form";

			} else {
//				send change password form.
			}

			return "password_change_form";
		} else {
			session.setAttribute("message", new Message("You have entered Wrong OTP !!", "alert-danger"));
			return "verify_otp";
		}
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		this.userRepository.save(user);
		session.setAttribute(email, user);
		return "redirect:/signin?change=password chnaged successfully..";
	}

}
