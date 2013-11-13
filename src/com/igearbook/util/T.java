package com.igearbook.util;

public class T {

    public static void main(String[] args) {
        String description = "1234567890123456789012345678901234567890123456789";
        if (description.length() > 50) {
            description = description.substring(0, 50);
        }
        System.out.println(description);
    }

}
