package com.smart.Controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

//	method for adding common data 
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("UserName :" + userName);

		User user = userRepository.getUserByUserName(userName);
		System.out.println("User" + user);
		model.addAttribute("user", user);

	}

//	DashBoard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal, HttpSession session) {
//		get the user using userName(Email)
		model.addAttribute("title", "User DashBoard");
		session.removeAttribute("message");
		return "normal/user_dashboard";
	}

//	add form handler

	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

//		processing and uploading file.
			if (file.isEmpty()) {
//			if file is empty
				System.out.println("File is Empty");
				contact.setImage("contact.jpg");
			} else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/Img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}

			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("Added to data base");
//			success message
			session.setAttribute("message", new Message("Your contact is added !! Add more..", "success"));

		} catch (Exception e) {
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
//			error message
			session.setAttribute("message", new Message("Something went Wrong !! try agian", "danger"));

		}
		return "normal/add_contact_form";
	}

//	show contact handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "View Contacts");
//		show all contacts list
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);

		Pageable pageable = PageRequest.of(page, 3);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

//	showing particular contact details.
	@GetMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("CID " + cId);

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_detail";
	}

	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Principal principal, HttpSession session) {

		Contact contact = this.contactRepository.findById(cId).get();
		System.out.println("contact" + contact.getcId());

		String name = principal.getName();

		User user = this.userRepository.getUserByUserName(name);
		user.getContacts().remove(contact);
		this.userRepository.save(user);

		session.setAttribute("message", new Message("Contact deleted successfully", "success"));

		return "redirect:/user/show-contacts/0";
	}

//	open update form handler
	@PostMapping("/update-contact/{cId}")
	public String updateContact(@PathVariable("cId") Integer cId, Model model) {
		model.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cId).get();
		model.addAttribute("contact", contact);

		return "normal/update_form";
	}

//	update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model model, HttpSession session, Principal principal) {

		try {

//			fetch old contact details.
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();

//			image
			if (!file.isEmpty()) {
//				file re-write
//				delete old photo

				File deleteFile = new ClassPathResource("static/Img").getFile();
				File file1 = new File(deleteFile, oldContactDetail.getImage());
				file1.delete();

//				update new photo
				File saveFile = new ClassPathResource("static/Img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());

			} else {
				contact.setImage(oldContactDetail.getImage());
			}

			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Your Contact is updated..", "success"));

		} catch (Exception e) {
			// TODO: handle exception
		}

		System.out.println("Contact Name " + contact.getName());
		System.out.println("Contact Id " + contact.getcId());

		return "redirect:/user/" + contact.getcId() + "/contact";

	}

	@GetMapping("/profile")
	public String yourProfile(Model model) {
		return "normal/profile";
	}

	@PostMapping("/update-profile/{id}")
	public String updateProfile(@PathVariable("id") Integer id, Model model) {
		model.addAttribute("title", "Update Profile");
		User user = this.userRepository.findById(id).get();
		model.addAttribute("user", user	);
		return "normal/update_profile_form";
	}
	
	@PostMapping("/profile-update")
	public String profileUpdate(@ModelAttribute("user") User user, Principal principal) {
		String name = principal.getName();
		User oldUser = this.userRepository.getUserByUserName(name);
		oldUser.setName(user.getName());
		oldUser.setEmail(user.getEmail());
		oldUser.setAbout(user.getAbout());
		
		this.userRepository.save(oldUser);
		
		return "redirect:/user/profile";
	}
	
//	open settings handler
	@GetMapping("/settings")
	public String openSettings(Model model) {
		model.addAttribute("title", "Change Password");
		return "normal/settings";
	}

//	change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		System.out.println("Old Password "+ oldPassword);
		System.out.println("New Password " + newPassword);
		
		String name = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(name);
		System.out.println(currentUser.getPassword());
		
		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is successfully chnaged" , "success"));
		}else {
			session.setAttribute("message", new Message("Please Enter correct old password " , "danger"));
			return "redirect:/user/settings";
			
		}
		
		return "redirect:/user/index";
	}
}
