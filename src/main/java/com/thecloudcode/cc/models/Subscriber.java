package com.thecloudcode.cc.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


import java.time.LocalDateTime;

@Entity
@Data

@Table(name = "subscribers")
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @Column(name = "subscribed_date", nullable = false)
    private LocalDateTime subscribedDate;

    @Column(nullable = false)
    private boolean active;

        public Subscriber() {
        this.subscribedDate = LocalDateTime.now();
        this.active = true;
    }

       public Subscriber(String email) {
        this();
        this.email = email;
    }

  
}
