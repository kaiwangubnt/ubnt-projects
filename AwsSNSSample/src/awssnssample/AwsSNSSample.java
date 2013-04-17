/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package awssnssample;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;

/**
 *
 * @author kaiwangubiquiti
 */
public class AwsSNSSample {
    
    private void sendMessage(){
        MessageSender messageSender = new MessageSender();
        Thread messageSenderThread = new Thread(messageSender, "publish message to sns topic");
        messageSenderThread.start();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        AwsSNSSample instance = new AwsSNSSample();
        instance.sendMessage();
    }
    
    public class MessageSender implements Runnable {
        private AmazonSNS sns;
        private String topicArn;
        private boolean running;
        private int messageNum;
        
        public MessageSender(){
            // init
            running = true;
            messageNum = 0;
            sns = new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider("/awssnssample/AwsCredentials.properties"));
            topicArn = "arn:aws:sns:us-east-1:608456421645:mFi-cometmessage";
        }
        
        public void stop(){
            running = false;
        }
        
        @Override
        public void run() {
            while(running){
                try {
                   // publish message to topic 
                    sns.publish(new PublishRequest(topicArn, "Hello "+messageNum));
                    messageNum++;
                    if(messageNum>=5){
                        stop();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    
                } catch(AmazonServiceException ase){
                
                } catch(AmazonClientException ace){
                
                }
            }
        }
    }
}