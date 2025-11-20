package com.versionvault.cli;

import com.versionvault.service.VersionVaultService;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        VersionVaultService vvs = new VersionVaultService(System.getProperty("user.dir"));

        System.out.println("VersionVault - Personal Version Control System");
        System.out.println("Commands: init, add <file>, commit <message> <author> [tag:key=value], log, exit");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            String[] parts = input.split(" ");
            String command = parts[0];

            switch (command) {
                case "init":
                    vvs.init();
                    break;
                case "add":
                    if (parts.length > 1) {
                        vvs.add(parts[1]);
                    } else {
                        System.out.println("Usage: add <file>");
                    }
                    break;
                case "commit":
                    if (parts.length >= 3) {
                        String message = parts[1];
                        String author = parts[2];
                        Map<String, String> tags = new HashMap<>();
                        if (parts.length > 3) {
                            for (int i = 3; i < parts.length; i++) {
                                String[] tagParts = parts[i].split(":");
                                if (tagParts.length == 2) {
                                    tags.put(tagParts[0], tagParts[1]);
                                }
                            }
                        }
                        vvs.commit(message, author, tags);
                    } else {
                        System.out.println("Usage: commit <message> <author> [tag:key=value]");
                    }
                    break;
                case "log":
                    vvs.log();
                    break;
                case "exit":
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Unknown command");
            }
        }
    }
}