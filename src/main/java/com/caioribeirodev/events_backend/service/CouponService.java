package com.caioribeirodev.events_backend.service;

import com.caioribeirodev.events_backend.domain.coupon.Coupon;
import com.caioribeirodev.events_backend.domain.coupon.CouponRequestDTO;
import com.caioribeirodev.events_backend.domain.event.Event;
import com.caioribeirodev.events_backend.repositories.CouponRepository;
import com.caioribeirodev.events_backend.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private EventRepository eventRepository;

    public Coupon createCoupon(UUID eventId, CouponRequestDTO data) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));

        Coupon newCoupon = new Coupon();
        newCoupon.setCode(data.code());
        newCoupon.setDiscount(data.discount());
        newCoupon.setEvent(event);
        newCoupon.setValid(new Date(data.valid()));

        couponRepository.save(newCoupon);

        return newCoupon;
    }

    public List<Coupon> consultCoupons(UUID eventId, Date currentDate) {
        return couponRepository.findByEventIdAndValidAfter(eventId, currentDate);
    }
}
