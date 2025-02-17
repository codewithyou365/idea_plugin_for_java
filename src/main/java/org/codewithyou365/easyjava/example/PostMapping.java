package org.codewithyou365.easyjava.example;

public @interface PostMapping {
    String name() default "";
    String value() default "";
    String[] path() default {};
}
