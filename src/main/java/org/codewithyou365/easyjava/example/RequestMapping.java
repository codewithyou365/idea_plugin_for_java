package org.codewithyou365.easyjava.example;

public @interface RequestMapping {
    String name() default "";
    String value() default "";
    String[] path() default {};
}
