/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockettest;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 *
 * @author tobak11
 */
public class Server { 
    private static enum ServerType  {DEFAULT, SECURE};
    private DataOutputStream out;
    private DataInputStream in;
    
    private ServerType serverType;
    
    //For DEFAULT Server
    private Socket socket; 
    private ServerSocket serverSocket;
    
    //For SECURE Server
    private SSLContext sslContext;
    private SSLSocket sslSocket;
  
    public Server(int port, ServerType serverType) throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException { 
        this.serverType=serverType; 
        
        if(serverType.equals(ServerType.DEFAULT)){
            serverSocket = new ServerSocket(port); 
            System.out.println("Default Server started");
            System.out.println("Waiting for a client ..."); 

            socket = serverSocket.accept(); 
            System.out.println("Client accepted"); 
            
            out = new DataOutputStream(socket.getOutputStream()); 
            in = new DataInputStream(socket.getInputStream());
        }
        
        if(serverType.equals(ServerType.SECURE)){
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("mykeystore.keystore"),"123456".toCharArray());
             
            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, "123456".toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();
             
            
            // Initialize SSLContext
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(km,  null, null);
            
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port); 
            System.out.println("Secure Server Started");
            System.out.println("Waiting for a client ..."); 
            sslSocket = (SSLSocket) sslServerSocket.accept();
            System.out.println("Client accepted");
            
//            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
            String[] enabledProtocols = sslSocket.getEnabledProtocols();
            for (String protocol : enabledProtocols) {
                System.out.println("enabledProtocols: " + protocol);
            }  
            
            System.out.println("");

            String[] enabledCipherSuites = sslSocket.getEnabledCipherSuites();
            for (String cipher : enabledCipherSuites) {
                System.out.println("enabledCipherSuite: " + cipher);
            }            
            

            
            sslSocket.startHandshake();
            
            out = new DataOutputStream(sslSocket.getOutputStream()); 
            in = new DataInputStream(sslSocket.getInputStream());
        }
    }
    
    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException {
//        Server defaultServer = new Server(6666, ServerType.DEFAULT);
//        defaultServer.start();
        
        Server secureServer = new Server(6669, ServerType.SECURE);
        secureServer.start();
    }
        
    public void start() throws IOException, InterruptedException {
        int kb128 = (int)Math.pow(2, 17);
        for(int i=10;i<18;i++){
            echoBack(kb128, (int)(Math.pow(2, i)));
        }

        int mb16 = (int)(Math.pow(2, 24));
        for(int i=10;i<25;i++){
            echoBack(mb16, (int)(Math.pow(2, i)));
        }

        int mb512 = (int)(Math.pow(2, 29));
        for(int i=10;i<30;i++){
            echoBack(mb512, (int)(Math.pow(2, i)));
        }
        
        in.close();
        out.close();
    }
    
    public void echoBack(int numOfData, int bufferSize) throws IOException, InterruptedException{
        byte[] clientData = new byte[bufferSize];
        int reps = 1;
        for (int k = 0; k < reps; k++) {
            for (int i = 0; i < numOfData / bufferSize; i++) {
                in.readFully(clientData);
                out.write(clientData);
            }
        }
    }       
}
