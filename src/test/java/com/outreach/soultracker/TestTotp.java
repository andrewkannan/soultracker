package com.outreach.soultracker;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.exceptions.CodeGenerationException;

public class TestTotp {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Please provide the secret");
            return;
        }
        String secret = args[0];
        DefaultCodeGenerator generator = new DefaultCodeGenerator();
        SystemTimeProvider timeProvider = new SystemTimeProvider();
        long timeBucket = Math.floorDiv(timeProvider.getTime(), 30);
        String code = generator.generate(secret, timeBucket);
        System.out.println("Code for secret " + secret + " is: " + code);
    }
}
