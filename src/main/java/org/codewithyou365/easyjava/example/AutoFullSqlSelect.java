package org.codewithyou365.easyjava.example;

import java.util.ArrayList;
import java.util.List;

public class AutoFullSqlSelect {
    public void print() {
        List<Foo> foos = new ArrayList<>();
        for (int i = 0; i < foos.size(); i++) {
            Foo foo = foos.get(i);
            System.out.println(foo.getJackName());
        }
    }

    // The name must be `QuerySelect` to activate the `full` prompt
    String sFoo = QuerySelect.from("`name`,`age`,`jack_age`");

    public static void main(String[] args) {

        // The name must be `QuerySelect` to activate the `full` prompt
        String sFoo = QuerySelect.from("`name`,`age`,`jack_age`");
        List<Foo> foos = new ArrayList<>();
        for (int i = 0; i < foos.size(); i++) {
            Foo foo = foos.get(i);
            System.out.println(foo.getName() + foo.getAge().toLowerCase() + foo.getJackAge().toLowerCase());
        }
    }
}