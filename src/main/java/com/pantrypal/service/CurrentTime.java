package com.pantrypal.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Single source of "today", so tests and services agree on the date. */
@Component
public class CurrentTime {
    public LocalDate today() { return LocalDate.now(); }
}
