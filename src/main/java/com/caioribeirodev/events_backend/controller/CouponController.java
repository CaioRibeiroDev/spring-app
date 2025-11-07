package com.caioribeirodev.events_backend.controller;

import com.caioribeirodev.events_backend.domain.coupon.Coupon;
import com.caioribeirodev.events_backend.domain.coupon.CouponRequestDTO;
import com.caioribeirodev.events_backend.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @PostMapping("/event/{eventId}")
    public ResponseEntity<Coupon> create(@PathVariable UUID eventId, @RequestBody CouponRequestDTO data) {
        Coupon coupons =  couponService.createCoupon(eventId, data);
        return ResponseEntity.ok(coupons);
    }

}
