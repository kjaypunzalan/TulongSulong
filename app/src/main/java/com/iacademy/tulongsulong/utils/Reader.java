package com.iacademy.tulongsulong.utils;

//1st step - import the java I/O related classes
import java.io.*;

public class Reader {
    public static BufferedReader getReader() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        return reader;
    }

    //string
    public static String readString(String message) {
        String input = "";
        System.out.print(message + ": ");

        try {
            input = getReader().readLine();
        } catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        return input;
    }

    //char
    public static char readChar(String message) {
        char input = ' ';
        System.out.print(message + ": ");

        try {
            input = getReader().readLine().charAt(0);
        } catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        return input;
    }

    //int
    public static int readInt(String message) {
        int input = 0;
        System.out.print(message + ": ");

        try {
            //converts String data type to int primitive data type
            input = Integer.parseInt(getReader().readLine());
        } catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        } catch(NumberFormatException nfe) {
            System.err.println("Invalid Input: " + nfe.getMessage());
        }
        return input;
    }

    //long
    public static long readLong(String message) {
        long input = 0;
        System.out.print(message + ": ");

        try {
            //converts String data type to int primitive data type
            input = Long.parseLong(getReader().readLine());
        } catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        } catch(NumberFormatException nfe) {
            System.err.println("Invalid Input: " + nfe.getMessage());
        }
        return input;
    }

    //double
    public static double readDouble(String message) {
        double input = 0;
        System.out.print(message + ": ");

        try {
            //converts String data type to double primitive data type
            input = Double.parseDouble(getReader().readLine());
        } catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        } catch(NumberFormatException nfe) {
            System.err.println("Invalid Input: " + nfe.getMessage());
        }
        return input;
    }
}
