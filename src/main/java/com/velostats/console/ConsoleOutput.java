package com.velostats.console;

import org.springframework.stereotype.Component;

/**
 * What a command prints. Kept behind a seam so tests can read a command's output instead of the
 * console.
 */
@Component
public class ConsoleOutput {

    public void success(String message) {
        System.out.println("[OK] " + message);
    }

    public void error(String message) {
        System.err.println("[ERROR] " + message);
    }

    public void line(String message) {
        System.out.println(message);
    }
}
