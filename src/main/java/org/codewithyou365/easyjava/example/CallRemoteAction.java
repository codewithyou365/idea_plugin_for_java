package org.codewithyou365.easyjava.example;

@RequestMapping("/abc/xyz")
public class CallRemoteAction {
    @PostMapping(value = "/postTest")
    public String postTest() {
        return "postTest";
    }

    @GetMapping(value = "/getTest")
    public String getTest() {
        return "getTest";
    }

    public static void main(String[] args) {
    }
}
