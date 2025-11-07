package com.caioribeirodev.events_backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.caioribeirodev.events_backend.domain.address.Address;
import com.caioribeirodev.events_backend.domain.coupon.Coupon;
import com.caioribeirodev.events_backend.domain.event.Event;
import com.caioribeirodev.events_backend.domain.event.EventDetailsDTO;
import com.caioribeirodev.events_backend.domain.event.EventRequestDTO;
import com.caioribeirodev.events_backend.domain.event.EventResponseDTO;
import com.caioribeirodev.events_backend.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private AddressService addressService;
    @Autowired
    private CouponService couponService;

    public Event createEvent(EventRequestDTO data) {
        String imgUrl = null;

        if(data.image() != null) {
            imgUrl = this.uploadImg(data.image());
        }

        Event newEvent = new Event();
        newEvent.setTitle(data.title());
        newEvent.setDescription(data.description());
        newEvent.setEventUrl(data.eventUrl());
        newEvent.setDate(new Date(data.date()));
        newEvent.setImgUrl(imgUrl);
        newEvent.setRemote(data.remote());

        eventRepository.save(newEvent);

        if(!data.remote()) {
            this.addressService.createAddress(data, newEvent);
        }

        return newEvent;
    }

    public List<EventResponseDTO> getEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventPage = this.eventRepository.findAll(pageable);

        return eventPage.map(event -> new EventResponseDTO(event.getId(), event.getTitle(), event.getDescription(), event.getDate(), "", "", event.getRemote(), event.getImgUrl(), event.getEventUrl())).stream().toList();
    }

    public List<EventResponseDTO> getUpComingEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventPage = this.eventRepository.findUpComingEvents(new Date(), pageable);

        return eventPage.map(event -> new EventResponseDTO(event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getAddress() != null ? event.getAddress().getCity() : "",
                event.getAddress() != null ? event.getAddress().getUf() : "",
                event.getRemote(),
                event.getImgUrl(),
                event.getEventUrl())).stream().toList();
    }

    public List<EventResponseDTO> getFilteredEvent(
            int page,
            int size,
            String title,
            String city,
            String uf,
            Date startDate,
            Date endDate
    ) {
        title = (title != null) ? title : "";
        city = (city != null) ? city : "";
        uf = (uf != null) ? uf : "";
        startDate = (startDate != null) ? startDate : new Date();
//        endDate = (endDate != null) ? endDate : "";


        if (endDate == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.YEAR, 10);
            endDate = calendar.getTime();
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Event> eventPage = this.eventRepository.findFilteredEvents(
                title, city, uf, startDate, endDate, pageable
        );


        return eventPage.map(event -> new EventResponseDTO(event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getAddress() != null ? event.getAddress().getCity() : "",
                event.getAddress() != null ? event.getAddress().getUf() : "",
                event.getRemote(),
                event.getImgUrl(),
                event.getEventUrl())).stream().toList();
    }

    public EventDetailsDTO getEventDetails(UUID eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event Not Found"));

        Optional<Address> address = addressService.findByEventId(eventId);

        List<Coupon> coupons = couponService.consultCoupons(eventId, new Date());

        List<EventDetailsDTO.CouponDTO> couponDTOs = coupons.stream()
                .map(coupon -> new EventDetailsDTO.CouponDTO(
                        coupon.getCode(),
                        coupon.getDiscount(),
                        coupon.getValid()))
                .collect(Collectors.toList());

        return new EventDetailsDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                address.isPresent() ? address.get().getCity() : "",
                address.isPresent() ? address.get().getUf() : "",
                event.getImgUrl(),
                event.getEventUrl(),
                couponDTOs);
    }


    private String uploadImg(MultipartFile multipartFile) {
        String filename = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();

        try {
            File file = this.convertMultipartToFile(multipartFile);
            s3Client.putObject(bucketName, filename, file);
            file.delete();
            return s3Client.getUrl(bucketName, filename).toString();
        } catch (Exception e) {
            System.out.println("ERRO AO SUBIR ARQUIVO");
            throw new RuntimeException(e);
//            return null;
        }
    }

    private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
        //Representa o caminho
        File convFile = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        //java vai escrever fisicamente um arquivo no disco com o nome que veio no upload
        FileOutputStream fos = new FileOutputStream(convFile);
        //Aqui ele pega o conteúdo do arquivo (multipartFile.getBytes()) e grava os bytes no arquivo físico (convFile).
        fos.write(multipartFile.getBytes());
        fos.close();

        return convFile;
    }

}
