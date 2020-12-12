package com.ersenpamuk;
/**
 * Author: Erşen Pamuk
 */

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException {

        Scanner scanner = new Scanner(System.in);
        Analyze analyze = new Analyze();

        System.out.println("How many text files would you like to edit? (1-4)");
        int editNumber = scanner.nextInt();
        scanner.nextLine();
        if ((editNumber > 4) || (editNumber < 1)) {
            System.out.println("You must enter between 1 and 4...");
            return;
        }

        for (int i = 0; i < editNumber; i++) {
            generate();
            Thread client = new Thread(new Client().new Server(analyze));
            client.start();
            try {
                client.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeToFile(String path, byte[] key) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();

        FileOutputStream fos = new FileOutputStream(f);
        fos.write(key);
        fos.flush();
        fos.close();
    }

    public static void generate() {

        try {

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);

            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey privateKey = pair.getPrivate();
            PublicKey publicKey = pair.getPublic();

            writeToFile("Client/publicKey", publicKey.getEncoded());
            writeToFile("Client/privateKey", privateKey.getEncoded());
            writeToFile("Server/publicKey", publicKey.getEncoded());
            writeToFile("Server/privateKey", privateKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}

class threadCase implements Runnable {
    private Analyzer analyze;

    public threadCase(Analyzer analyze) {
        this.analyze = analyze;
    }

    @Override
    public void run() {
        analyze.caseAllChar();
    }
}

class threadColor implements Runnable {
    private Analyzer analyze;

    public threadColor(Analyzer analyze) {
        this.analyze = analyze;
    }

    @Override
    public void run() {
        analyze.colorAllChar();
    }
}

class threadShift implements Runnable {
    private Analyzer analyze;

    public threadShift(Analyzer analyze) {
        this.analyze = analyze;
    }

    @Override
    public void run() {
        analyze.shiftAllChar();
    }
}

class Client {
    private String pathOfFile = "http://homes.ieu.edu.tr/eokur/";
    private String caseChoice;
    private String colorChoices;
    private String fileText;
    private int shift;
    private Cipher cipher;
    private int id;
    private byte[] encrypted_text;
    private byte[] plain_text;
    private byte[] decrypted_text;
    private KeyFactory kf = KeyFactory.getInstance("RSA");


    public Client() throws NoSuchAlgorithmException {
        start();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the ID of your text file:");
        id = scanner.nextInt();
        scanner.nextLine();
        String fileName = "sample" + id + ".txt";
        try {
            readFile(pathOfFile, fileName);
        } catch (IOException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        System.out.println("Please state your choice...");
        System.out.print("UPPER case or lower case? (U or L) ");
        caseChoice = scanner.nextLine();
        System.out.println("Please state your choice...");
        System.out.print("How many characters to shift? (number between 1-3)");
        shift = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Color of characters? (R or Y)");
        colorChoices = scanner.nextLine();
    }

    public void readFile(String path, String fullN) throws IOException, InvalidKeySpecException {

        String fullPath = path + fullN;
        InputStream in = new URL(fullPath).openStream();
        Files.copy(in, Paths.get(fullN), StandardCopyOption.REPLACE_EXISTING);

        FileReader fileReader = new FileReader(fullN);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = bufferedReader.readLine();
        StringBuilder stringBuilder = new StringBuilder();

        while (line != null) {
            stringBuilder.append(line).append("\n");
            line = bufferedReader.readLine();
        }
        fileText = stringBuilder.toString();
        plain_text = fileText.getBytes("UTF-8");

        byte[] keyBytes = Files.readAllBytes(new File("Server/publicKey").toPath());
        X509EncodedKeySpec spec2 = new X509EncodedKeySpec(keyBytes);
        PublicKey publicKey = kf.generatePublic(spec2);

        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            encrypted_text = cipher.doFinal(plain_text);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }
/* COULDN'T FINISH
 /*private byte[] encrypted_text_Original;
    private byte[] encrypted_text_afterCase;
    private byte[] encrypted_text_afterShift;
    private byte[] encrypted_text_afterColor;
    private Analyze analyze;

    public void printEncryptedResult() throws IOException, InvalidKeySpecException, InvalidKeyException {
        try {
            byte[] keyBytes = Files.readAllBytes(new File("Client/privateKey").toPath());
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            PrivateKey privateKey = kf.generatePrivate(spec);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decryptedOriginal = cipher.doFinal(encrypted_text_Original);
            byte[] decryptedAfterCase = cipher.doFinal(encrypted_text_afterCase);
            byte[] decryptedAfterShift = cipher.doFinal(encrypted_text_afterShift);
            byte[] decryptedAfterColor = cipher.doFinal(encrypted_text_afterColor);

            String parsedOriginal = new String(decryptedOriginal);
            String parsedAfterCase = new String(decryptedAfterCase);
            String parsedAfterShift = new String(decryptedAfterShift);
            String parsedAfterColor = new String(decryptedAfterColor);

            System.out.println(parsedOriginal);
            System.out.println(parsedAfterCase);
            System.out.println(parsedAfterShift);
            System.out.println(parsedAfterColor);

        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }*/

    class Server extends Analyze implements Runnable {

        private Analyze analyzeObject;

        public Server(Analyze analyze) {
            analyzeObject = analyze;
        }

        public void textRequest() throws IOException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
            byte[] keyBytes = Files.readAllBytes(new File("Server/privateKey").toPath());
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            PrivateKey privateKey = kf.generatePrivate(spec);

            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            decrypted_text = cipher.doFinal(encrypted_text); // COULDN'T FINISH

            int pool_count = 1;
            for (int i = 0; i < pool_count; i++) {

                String analyzedFile = fileText;
                System.out.println("File text: " + fileText);
                String caseSensitive = caseChoice;
                String colorChoice = colorChoices;
                int shiftAmount = shift;

                analyzeObject.lowerUpper(caseSensitive);
                analyzeObject.setShift(shiftAmount);
                analyzeObject.colorChange(colorChoice);
                analyzeObject.AnalyzedChars(analyzedFile);

                Thread threadCase = new Thread(new threadCase(analyzeObject));
                Thread threadShift = new Thread(new threadShift(analyzeObject));
                Thread threadColor = new Thread(new threadColor(analyzeObject));

                threadCase.start();
                try {
                    threadCase.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                threadShift.start();
                try {
                    threadShift.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                threadColor.start();
                try {
                    threadColor.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                analyzeObject.createThreadedString();
            }
        }

        @Override
        public void run() {
            try {
                textRequest();
                analyzeObject.display();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
    }
}

interface Analyzer {

    void colorChange(String colorChoice);

    void lowerUpper(String caseSensitive);

    void display();

    void setShift(int shiftAmount);

    void createThreadedString();

    void AnalyzedChars(String shiftMe);

    void shiftAllChar();

    void caseAllChar();

    void colorAllChar();

}

class Analyze implements Analyzer {

    private String colorChoice;
    private int shiftAmount;
    private boolean caseSensitive;
    private String[] myString = new String[4];
    String colorYellow = "\u001B[33m";
    String colorRed = "\u001B[31m";
    String defaultColor = "\u001B[0m";
    String defaultMessage = "You must enter 'y' or 'r'...";
    String defaultMessage2 = "You must enter 'l' or 'u'...";
    char[] buffer;
    String[] bufferCase;
    String[] bufferShift;
    String[] bufferColor;


    @Override
    public void colorChange(String colorChoice) {
        if (colorChoice.equalsIgnoreCase("r")) {
            this.colorChoice = colorRed;
        } else if (colorChoice.equalsIgnoreCase("y")) {
            this.colorChoice = colorYellow;
        } else {
            System.out.println(defaultMessage);
            return;
        }
    }

    @Override
    public void lowerUpper(String caseSensitive) {
        if (caseSensitive.equalsIgnoreCase("l")) {
            this.caseSensitive = true;
        } else if (caseSensitive.equalsIgnoreCase("u")) {
            this.caseSensitive = false;
        } else {
            System.out.println(defaultMessage2);
            return;
        }
    }

    @Override
    public void display() {
        String original = "";
        String afterCase = "";
        String afterShift = "";

        original = original + myString[0];
        afterCase = afterCase + myString[1];
        afterShift = afterShift + myString[2];

        String afterColor = getColor() + afterCase + defaultColor;
        System.out.println(original);
        System.out.println(afterCase);
        System.out.println(afterShift);
        System.out.println(afterColor);
    }

    private String getColor() {
        return colorChoice;
    }

    private int getShift() {
        return shiftAmount;
    }

    @Override
    public void setShift(int shiftAmount) {
        this.shiftAmount = shiftAmount;
    }

    private boolean getCase() {
        return caseSensitive;
    }

    @Override
    public void createThreadedString() {
        for (int i = 0; i < buffer.length; i++) {
            if (i == 0) {
                myString[0] = String.valueOf(buffer[i]);
                myString[1] = bufferCase[i];

                myString[2] = bufferShift[i];
                myString[3] = bufferColor[i];
            } else {
                myString[0] += String.valueOf(buffer[i]);
                myString[1] += bufferCase[i];

                myString[2] += bufferShift[i];
                myString[3] += bufferColor[i];
            }
        }
    }

    @Override
    public void AnalyzedChars(String shifting) {
        shifting.replace(" ", "");
        shifting.replace(".", "");
        buffer = shifting.toCharArray();
        bufferCase = new String[buffer.length];
        bufferShift = new String[buffer.length];
        bufferColor = new String[buffer.length];
    }

    @Override
    public void shiftAllChar() {
        for (int i = 0; i < buffer.length; i++) {
            bufferShift[i] = String.valueOf(shiftChar(buffer[i]));
        }
    }

    @Override
    public void caseAllChar() {
        for (int i = 0; i < buffer.length; i++) {
            bufferCase[i] = upperLower(buffer[i]);
        }
    }

    @Override
    public void colorAllChar() {
        for (int i = 0; i < buffer.length; i++) {
            bufferColor[i] = colorChanger(buffer[i]);
        }
    }

    public char shiftChar(char myChar) {
        myChar = (char) (myChar + getShift());
        if (myChar > 'z' || myChar > 'Z') {
            myChar = (char) (myChar - 26);
        } else if (myChar < 'a' || myChar < 'A') {
            myChar = (char) (myChar + 26);
        }
        return myChar;
    }

    public String colorChanger(char afterCases) {
        String afterCase = String.valueOf(afterCases);
        return getColor() + afterCase + defaultColor;
    }

    public String upperLower(char myChar) {
        String value = String.valueOf(myChar);
        if (getCase()) {
            value = value.toLowerCase();
        } else {
            value = value.toUpperCase();
        }
        return value;
    }
}


