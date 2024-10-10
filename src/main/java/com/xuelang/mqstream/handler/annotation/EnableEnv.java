package com.xuelang.mqstream.handler.annotation;

import com.xuelang.mqstream.config.EnvUtil;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({EnvUtil.class})
public @interface EnableEnv {
}
