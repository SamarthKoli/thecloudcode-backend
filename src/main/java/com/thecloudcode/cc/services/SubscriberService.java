package com.thecloudcode.cc.services;




import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thecloudcode.cc.models.Subscriber;
import com.thecloudcode.cc.repository.SubscriberRepository;

@Service
public class SubscriberService {

    @Autowired
    private SubscriberRepository subscriberRepository;

    public Subscriber subscribe(String email) throws Exception {
        if (subscriberRepository.existsByEmail(email)) {
            throw new Exception("Email already subscribed");
        }
        Subscriber subscriber = new Subscriber(email);
        return subscriberRepository.save(subscriber);
    }

      public List<Subscriber> getAllActiveSubscribers() {
        return subscriberRepository.findByActiveTrue();
    }
    
    public long getActiveSubscriberCount() {
        return subscriberRepository.countActiveSubscribers();
    }
}
