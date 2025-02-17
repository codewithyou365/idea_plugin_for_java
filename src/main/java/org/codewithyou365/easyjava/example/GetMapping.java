package org.codewithyou365.easyjava.example;

public @interface GetMapping {
    String name() default "";
    String value() default "";
    String[] path() default {};
}
