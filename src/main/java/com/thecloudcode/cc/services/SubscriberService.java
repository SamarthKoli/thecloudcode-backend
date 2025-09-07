package com.thecloudcode.cc.services;




import java.util.List;
import java.util.Optional;

import javax.management.RuntimeErrorException;

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


    public boolean isEmailSubscribed(String email){
        Optional<Subscriber>subscriber=subscriberRepository.findByEmail(email);
        if (subscriber.isPresent()) {
            return true;
        }
        else{
            return false;
        }
    }

    public  Subscriber findByEmail(String email){
        return subscriberRepository.findByEmail(email).get();
    }
    public void unSubscribe(String email)throws Exception{
      Optional<Subscriber> subscriber=subscriberRepository.findByEmail(email);

      if (subscriber.isEmpty()) {
          throw new Exception("User not found");
      }
      else{
             Subscriber existing=subscriber.get();
             existing.setActive(false);
             subscriberRepository.delete(existing);;
      }

    }

      public List<Subscriber> getAllActiveSubscribers() {
        return subscriberRepository.findByActiveTrue();
    }
    
    public long getActiveSubscriberCount() {
        return subscriberRepository.countActiveSubscribers();
    }
}
