package com.example.lize_app.injector;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 *
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface PerViewpagerAdapter {
}