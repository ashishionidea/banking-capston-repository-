package com.ashish.banking.customerservice.api;
import com.ashish.banking.customerservice.domain.Customer;
import com.ashish.banking.customerservice.repository.CustomerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/customers")
public class CustomerController {
 private final CustomerRepository customers;
 public CustomerController(CustomerRepository customers){this.customers=customers;}
 public record Registration(@NotBlank String fullName,@Email @NotBlank String email,@NotBlank String phone){}
 public record Profile(@NotBlank String fullName,@NotBlank String phone,String address){}
 public record CustomerView(Long customerId,String fullName,String email,String phone,String address,String status){}
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
 public CustomerView register(@Valid @RequestBody Registration r){
  if(customers.existsByEmailIgnoreCase(r.email())) throw new IllegalArgumentException("Email already registered");
  if(customers.existsByPhone(r.phone())) throw new IllegalArgumentException("Phone already registered");
  Customer c=new Customer();c.setFullName(r.fullName());c.setEmail(r.email().trim().toLowerCase());c.setPhone(r.phone());return view(customers.save(c));
 }
 @GetMapping("/me") public CustomerView me(@AuthenticationPrincipal Jwt jwt){return view(owned(jwt));}
 @PutMapping("/me") public CustomerView update(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Profile p){
  Customer c=owned(jwt);if(!c.getPhone().equals(p.phone())&&customers.existsByPhone(p.phone()))throw new IllegalArgumentException("Phone already registered");
  c.setFullName(p.fullName());c.setPhone(p.phone());c.setAddress(p.address());return view(customers.save(c));
 }
 private Customer owned(Jwt jwt){return customers.findById(Long.valueOf(jwt.getSubject())).orElseThrow(()->new IllegalArgumentException("Customer not found"));}
 private CustomerView view(Customer c){return new CustomerView(c.getId(),c.getFullName(),c.getEmail(),c.getPhone(),c.getAddress(),c.getStatus());}
}
