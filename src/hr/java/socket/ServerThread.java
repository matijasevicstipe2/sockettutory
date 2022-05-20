package hr.java.socket;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;


public class ServerThread extends Thread   {
    private Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);



            String text;
            text = reader.readLine();
            System.out.println(text);
            writer.println("Server: " + text);

            //separationOfData
            String[] arrayString = text.split("&");
            String encryptedUserCredentials = arrayString[0];
            String encryptedKey = arrayString[1];

            //ivParameter
            byte[] allBytes = Files.readAllBytes(Paths.get("C:\\Users\\stipe\\ntp_projekt\\iv.dat"));
            IvParameterSpec ivParameterSpec = new IvParameterSpec(allBytes);


            //decryptKey
            File privateKeyFile = new File("C:\\Users\\stipe\\ntp_projekt\\private.key");
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKeyRSA = keyFactory.generatePrivate(privateKeySpec);

            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKeyRSA);
            byte[] decryptedMessageBytes = decryptCipher.doFinal(Base64.getDecoder().decode(encryptedKey));
            String decryptedKeyString = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
            SecretKey decryptedKey = convertStringToSecretKey(decryptedKeyString);

            //decryptUserCredentials
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, decryptedKey,ivParameterSpec);
            byte[] decryptedUserCredentialsBin = cipher.doFinal(Base64.getDecoder()
                    .decode(encryptedUserCredentials));
            String decryptedUserCredentials = new String(decryptedUserCredentialsBin);
            System.out.println(decryptedUserCredentials);

            //txt file - citanje
            List<String> userList = readingTxtFile();
            //check if user is valid
            Boolean valid = false;
            for(String user : userList) {
                if(Objects.equals(user,decryptedUserCredentials)){
                    valid = true;
                }

            }
            writer.println(valid);
            socket.close();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

    }

    public static SecretKey convertStringToSecretKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        return originalKey;
    }

    public static List<String> readingTxtFile(){

        List<String> userList = new ArrayList<>();

        try {
            FileReader reader = new FileReader("C:\\Users\\stipe\\ntp_projekt\\userCheck.txt");
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                userList.add(line);
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return userList;
    }
}
