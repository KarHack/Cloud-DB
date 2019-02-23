/*
 * 
 * 36TH ELEMENT LICENSE 1.0
 *
 * This is a project of 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * This project is a closed source and proprietary software package.
 * None of the contents of this software is to be used for uses not intended,
 * And no one is to interface with the software in methods not defined or previously decided by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * No changes should be done to this project without prior authorization by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 2017 (C) 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 
 * 
 */
package helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author karan
 */
public class Helper {

    // This class will handle all the methods required for errors.
    public static class Error {

        // This method will help in converting it into a printable stack trace.
        public static String getPrintStack(Exception e) {
            try {
                // Here we will unwrap the stack trace so that we can log it to the response.
                StringBuilder errBuilder = new StringBuilder();
                for (StackTraceElement stackTrace : e.getStackTrace()) {
                    errBuilder.append(stackTrace.toString());
                    errBuilder.append("\n");
                }
                return errBuilder.toString();
            } catch (Exception er) {
                // There was an Error.
                return null;
            }
        }

        public static String getErrorMessage(Exception e) {
            try {
                return e.getMessage();
            } catch (Exception er) {
                // There was an Error.
                return null;
            }
        }

    }

    // Generate Random Sring of 'x' size.
    public static class StringManu {

        public static class Type {

            // Options to allow user to select.
            public static int ALPHA = 100;
            public static int ALPHA_NUMERIC = 101;
            public static int NUMERIC = 102;
            public static int ALPHA_NUMERIC_SPECIAL = 103;
            public static int ALPHA_SPECIAL = 104;
            public static int SPECIAL = 105;
            public static int DEFAULT = 106;
            public static int ALPHA_UPPER = 107;
            public static int ALPHA_LOWER = 108;
            //public static int ALPHA_NUMBERIC_SIMPLE_SPECIAL = 109;

            // Allow the Selection to Choose Automatically.
            static Map<Integer, String> typeMap;

            static void fillTypeHash() {
                try {
                    // Here we will try to fill the hash map of type.
                    typeMap = new HashMap<>();
                    // Fill the Map.
                    typeMap.put(ALPHA, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
                    typeMap.put(ALPHA_NUMERIC, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
                    typeMap.put(NUMERIC, "1234567890");
                    typeMap.put(ALPHA_NUMERIC_SPECIAL, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#~`$%^&*()-_=+:;{}[]/.,");
                    typeMap.put(ALPHA_SPECIAL, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#~`$%^&*()-_=+:;{}[]/.,");
                    typeMap.put(SPECIAL, "!@#~`$%^&*()-_=+:;{}[]/.,");
                    typeMap.put(ALPHA_UPPER, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                    typeMap.put(ALPHA_LOWER, "abcdefghijklmnopqrstuvwxyz");
                    typeMap.put(DEFAULT, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890_-");
                    //typeMap.put(ALPHA_NUMBERIC_SIMPLE_SPECIAL, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890-_");
                } catch (Exception e) {
                    // There was an Error.
                }
            }

        }

        public static String generate(int size) {
            try {
                // Here we will generate the random string.
                Type.fillTypeHash();
                String SALTCHARS = Type.typeMap.get(Type.DEFAULT);
                StringBuilder salt = new StringBuilder();
                Random rnd = new Random();
                while (salt.length() <= size) { // length of the random string.
                    int index = (int) (rnd.nextFloat() * SALTCHARS.length());
                    salt.append(SALTCHARS.charAt(index));
                }
                String saltStr = salt.toString();
                return saltStr;

            } catch (Exception e) {
                // There was an Error.
                return null;
            }
        }

        public static String generate(int size, int type) {
            try {
                // Here we will generate the random string.
                Type.fillTypeHash();
                String SALTCHARS = Type.typeMap.containsKey(type) ? Type.typeMap.get(type) : Type.typeMap.get(Type.DEFAULT);
                StringBuilder salt = new StringBuilder();
                Random rnd = new Random();
                while (salt.length() < size) { // length of the random string.
                    int index = (int) (rnd.nextFloat() * SALTCHARS.length());
                    salt.append(SALTCHARS.charAt(index));
                }
                String saltStr = salt.toString();
                return saltStr;

            } catch (Exception e) {
                // There was an Error.
                return null;
            }
        }

    }

    // Serialize & Deserialize the Object.
    public static class Serializer {

        // Lets Convert the Object to a String (Byte Array).
        public static String toString(Object objectToSerialize) {
            try {
                // Here we will Serialize the Object into a String.
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                ObjectOutputStream so = new ObjectOutputStream(bo);
                so.writeObject(objectToSerialize);
                so.flush();
                return bo.toString();
            } catch (Exception er) {
                // There was an Error.
                return "";
            }
        }

        // Lets Convert the String (Byte Array) to a Object. [Lets keep the ownest of casting to the right object with the Caller.]
        public static Object toObject(String stringToDeserialize) {
            try {
                // Here we will convert the String into the Object.
                byte b[] = stringToDeserialize.getBytes();
                ByteArrayInputStream bi = new ByteArrayInputStream(b);
                ObjectInputStream si = new ObjectInputStream(bi);
                return si.readObject();
            } catch (Exception er) {
                // There was an Error.
                return null;
            }
        }

    }

}
