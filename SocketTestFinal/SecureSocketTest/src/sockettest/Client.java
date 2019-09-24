/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockettest;

/**
 *
 * @author tobak11
 */
import java.net.*; 
import java.io.*; 
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Random;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
  
public class Client {
    private static enum ClientType {DEFAULT, SECURE};
    
    private DataOutputStream out;
    private DataInputStream in;
    
    private byte data[];
    
    private ClientType clientType;
    
    //For DEFAULT Client
    private Socket socket;
    
    //For SECURE Client
    private SSLContext sslContext;
    private SSLSocket sslSocket;

    public Client (String address, int port, ClientType clientType) throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, KeyManagementException {
        this.clientType = clientType;

        if(clientType.equals(ClientType.DEFAULT)){
            socket = new Socket(address, port); 
            System.out.println("Connected");
            
            out = new DataOutputStream(socket.getOutputStream()); 
            in = new DataInputStream(socket.getInputStream());
        }
        
        if(clientType.equals(ClientType.SECURE)){
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("mykeystore.keystore"),"123456".toCharArray());
             
            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();
             
            // Initialize SSLContext
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null,  tm, null);
        
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            sslSocket = (SSLSocket) sslSocketFactory.createSocket(address, port);
            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

            sslSocket.startHandshake();
            
            out = new DataOutputStream(sslSocket.getOutputStream()); 
            in = new DataInputStream(sslSocket.getInputStream());
        }
    }
    
    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException, CertificateException, KeyStoreException, KeyManagementException {
        PrintWriter pw = new PrintWriter(new File("benchmark.txt"));
        
//        Client defaultClient = new Client("localhost", 6666, ClientType.DEFAULT);  
//        System.out.println("Standard socket bandwidth test");
//        
//        //128 KB TEST
        int dataSize = (int)Math.pow(2, 17);
//        pw.println("128 kB DATA TEST");
//        pw.println("----------------");
//        defaultClient.generateData(bufferData);
//        
//        for(int i=10;i<18;i++){
//            defaultClient.test(bufferData, (int)(Math.pow(2, i)), pw);
//        }
//        
//        //16 MB TEST
//        bufferData = (int)(Math.pow(2, 24));
//        pw.println("16 Mb DATA TEST");
//        pw.println("---------------");
//        defaultClient.generateData(bufferData);
//        
//        for(int i=10;i<25;i++){
//            defaultClient.test(bufferData, (int)(Math.pow(2, i)), pw);   
//        }
//        
//        //512 MB TEST
//        bufferData = (int)(Math.pow(2, 29));
//        pw.println("512 Mb DATA TEST");
//        pw.println("----------------");
//        defaultClient.generateData(bufferData);
//        
//        for(int i=10;i<30;i++){
//            defaultClient.test((int)(bufferData), (int)(Math.pow(2, i)), pw);   
//        }
//        
        
        Client secureClient = new Client("localhost", 6669, ClientType.SECURE);  
        System.out.println("Secure socket bandwidth test");
        
        //128 KB TEST
        dataSize = (int)Math.pow(2, 17);
        pw.println("128 kB DATA TEST");
        pw.println("----------------");
        secureClient.generateData(dataSize);
        
        for(int i=10;i<18;i++){
            secureClient.test(dataSize, (int)(Math.pow(2, i)), pw);
        }
        
        //16 MB TEST
        dataSize = (int)(Math.pow(2, 24));
        pw.println("16 Mb DATA TEST");
        pw.println("---------------");
        secureClient.generateData(dataSize);
        
        for(int i=10;i<25;i++){
            secureClient.test(dataSize, (int)(Math.pow(2, i)), pw);   
        }
        
        //512 MB TEST
        dataSize = (int)(Math.pow(2, 29));
        pw.println("512 Mb DATA TEST");
        pw.println("----------------");
        secureClient.generateData(dataSize);
        
        for(int i=10;i<30;i++){
            secureClient.test(dataSize, (int)(Math.pow(2, i)), pw);   
        }
        
        secureClient.in.close();
        secureClient.out.close();
        
        pw.close();
    }
    
    public void generateData(int numOfData){
        data = new byte[numOfData];
        
        Random rnd = new Random();        
        rnd.nextBytes(data);
    }
    
    public void test(int dataSize, int bufferSize, PrintWriter pw) throws IOException, InterruptedException{
        byte[] serverResponse = new byte[bufferSize];
        
        float sum = 0;
        int reps = 1;

//        System.out.println("DataSize: " + dataSize + ", bufferSize: " + bufferSize);
        pw.println("DataSize: " + dataSize + ", bufferSize: " + bufferSize);
        pw.println("");
        for(int k=0;k<reps;k++){
            long startTime = System.nanoTime();
            for(int i=0;i<dataSize/bufferSize;i++){
                out.write(data, i*bufferSize, bufferSize);
                
                in.readFully(serverResponse);
            }
            
            long endTime = System.nanoTime(); 
            sum+=(endTime-startTime);
            
            pw.println(endTime-startTime);
        }
        
        pw.println("Sum: " + sum);
        pw.println("Avg: " + sum/10);
        pw.println("Bandwith (MB/s): " + (dataSize / (1024.0*1024.0)) * 1e9 / (sum / reps) );
        System.out.println("data size, bufferSize, Bandwith (MB/s): " + dataSize + ", " + bufferSize + ", " + (dataSize / (1024.0*1024.0)) * 1e9 / (sum / reps));
    }    
} 
