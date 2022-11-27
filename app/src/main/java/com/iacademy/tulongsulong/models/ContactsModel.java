package com.iacademy.tulongsulong.models;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class ContactsModel {

    //declare variables
    private String name;
    private String email;
    private String number;
    private static final String FILE_NAME = "fileDatabase.txt";
    private static final String TEMP_FILE = "tempDatabase.txt";

    //constructor
    public ContactsModel(String name, String email, String number) {
        this.name = name;
        this.email = email;
        this.number = number;
    }

    //setters and getters
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }

    public void setNumber(String number) { this.number = number; }
    public String getNumber() { return number; }


    /**************************
     * A. check if file exist
     *------------------------*/
    public static void checkIfFileExist(Context context) {
        //check if file exists
        boolean fileExists = new File(context.getFilesDir() + FILE_NAME).exists();
        if (fileExists)
            Toast.makeText(context, "File exists OMG!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "File does NOT exist :(", Toast.LENGTH_SHORT).show();
    }

    /**************************
     * B. read from .txt file
     *------------------------*/
    public static void readFromFile(ArrayList<ContactsModel> listModels, Context context) {
        try {

            //open reader
            BufferedReader br = new BufferedReader( new FileReader(context.getFilesDir() + FILE_NAME) );
            String line;

            //read each line
            while( (line = br.readLine()) != null ) {
                //split into array then add to recycler view model
                String[] phoneBook = line.split("—");
                listModels.add(new ContactsModel(phoneBook[0], phoneBook[1], phoneBook[2])); //add
            }

            br.close();

        } catch (FileNotFoundException e) {
            Toast.makeText(context, "Database file is empty. Please add a number.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "Database file is empty. Please add a number.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Database file is empty. Please add a number.", Toast.LENGTH_SHORT).show();
        }
    }

    /**************************
     * C. write to .txt file
     *------------------------*/
    public static void writeToFile(String name, String email, String number, Context context) {

        //create new string
        String newContactString = (name + "—" + email + "—" + number);

        try {

            //open writer
            BufferedWriter bw = new BufferedWriter(new FileWriter(context.getFilesDir() + FILE_NAME,true) );

            //write new string
            bw.write(newContactString);
            bw.flush();
            bw.newLine();
            bw.close();

        } catch (FileNotFoundException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**************************
     * D. edit .txt file
     *------------------------*/
    public static void editFile(String name, String email, String number, Context context, int position) {

        //create new string
        String newContactString = (name + "—" + email + "—" + number);

        try {

            File originalFile = new File(context.getFilesDir() + FILE_NAME);
            File tempFile = new File(context.getFilesDir() + TEMP_FILE);
            //open reader (old file) and writer (temp file)
            BufferedReader br = new BufferedReader( new FileReader(originalFile) );
            BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile) );

            //declare variables for looping
            String line;
            int i = 0;

            //read each line
            while( (line = br.readLine()) != null ) {

                //if line is not the position, write existing record
                if (i != position) {
                    bw.write(line);
                    bw.flush();
                    bw.newLine();
                }
                //else, write the new edited information
                else {
                    bw.write(newContactString);
                    bw.flush();
                    bw.newLine();
                }

                //iterate again
                i++;
            }

            //close reader and writer
            bw.close();
            br.close();

            //create reference for files and rename temp to the file
            originalFile.delete();
            tempFile.renameTo(originalFile);


        } catch (FileNotFoundException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**************************
     * E. delete from .txt file
     *------------------------*/
    public static void deleteFromFile(Context context, int position) {

        try {

            //open reader (old file) and writer (temp file)
            BufferedReader br = new BufferedReader( new FileReader(context.getFilesDir() + FILE_NAME) );
            BufferedWriter bw = new BufferedWriter(new FileWriter(context.getFilesDir() + TEMP_FILE,true) );

            //declare variables for looping
            String line;
            int i = 0;

            //read each line
            while( (line = br.readLine()) != null ) {

                //if position is found, then ignore
                if (position != i) {
                    bw.write(line);
                    bw.flush();
                    bw.newLine();
                }

                //iterate again
                i++;
            }


            //create reference for files and rename temp to the file
            File originalFile = new File(context.getFilesDir() + FILE_NAME);
            File tempFile = new File(context.getFilesDir() + TEMP_FILE);
            originalFile.delete();
            tempFile.renameTo(originalFile);

            //close reader and writer
            bw.close();
            br.close();

        } catch (FileNotFoundException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**************************
     * Extra. delete from .txt file
     *------------------------*/
    public static void deleteFile(Context context) {

        try {

            //create reference for file
            File file = new File(context.getFilesDir() + FILE_NAME);

            //create reference for file
            file.delete();

            //check if file still exist
            checkIfFileExist(context);
            System.exit(0);

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
