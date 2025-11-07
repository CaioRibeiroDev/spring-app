package com.caioribeirodev.events_backend.service;

import com.caioribeirodev.events_backend.domain.address.Address;
import com.caioribeirodev.events_backend.domain.event.Event;
import com.caioribeirodev.events_backend.domain.event.EventRequestDTO;
import com.caioribeirodev.events_backend.repositories.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public Address  createAddress(EventRequestDTO data, Event event) {
        Address address = new Address();
        address.setCity(data.city());
        address.setUf(data.state());
        address.setEvent(event);

        return addressRepository.save(address);
    }

    public Optional<Address> findByEventId(UUID eventId) {
        return addressRepository.findByEventId(eventId);
    }
}
