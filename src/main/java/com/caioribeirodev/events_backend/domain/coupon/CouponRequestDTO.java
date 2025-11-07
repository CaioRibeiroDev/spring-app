package com.caioribeirodev.events_backend.domain.coupon;

import com.caioribeirodev.events_backend.domain.event.Event;

import java.util.Date;

public record CouponRequestDTO(String code, Integer discount, Long valid) {
}
