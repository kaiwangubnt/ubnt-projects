/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package awssqssample;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import java.util.List;

/**
 *
 * @author kaiwangubiquiti
 */
public class AwsSQSSample {
    private void receiveMessage(){
        MessageReceiver messageReceiver = new MessageReceiver();
        Thread messageReceiverThread = new Thread(messageReceiver, "receive message from sqs");
        messageReceiverThread.start();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        AwsSQSSample instance = new AwsSQSSample();
        instance.receiveMessage();
    }
    
    public class MessageReceiver implements Runnable {
        private AmazonSQS sqs;
        private String queueUrl;
        private boolean running;
        private int messageNum;
        
        public MessageReceiver(){
            // init
            running = true;
            sqs = new AmazonSQSClient(new ClasspathPropertiesFileCredentialsProvider("/awssqssample/AwsCredentials.properties"));
            queueUrl = "https://sqs.us-east-1.amazonaws.com/608456421645/mFi-cometmessage";
        }
        
        public void stop(){
            running = false;
        }
        
        public void run(){
            while(running){
                try {
                    // receive message
                    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
                    receiveMessageRequest.setMaxNumberOfMessages(5);
                    List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
                    System.out.println("message number: "+messages.size());
                    if(messages.size()==0){
                        stop();
                    }
                    for(Message message : messages){
                        System.out.println("Message Body: "+ message.getBody());
                        // delete this message
                        sqs.deleteMessage(new DeleteMessageRequest(queueUrl, message.getReceiptHandle()));
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
