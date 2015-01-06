package hw3;


import java.util.List;
import java.util.Scanner;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;

public class AuctionTester implements MessageListener {
	MessageConsumer consumer;
	String userName;
	String passWord;
	UserSessionRemote ses;
	Scanner s;
	String messages;
	public AuctionTester() {}
	
	public void startSession()
	{
		try
		{
			InitialContext ctx = new InitialContext();
			ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup("alarmConnectionFactory");
	        Connection connection = connectionFactory.createConnection();
	        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	        Topic notificationTopic = (Topic)ctx.lookup("jms/alarmNotifier");
	        consumer = session.createConsumer(notificationTopic);
	        consumer.setMessageListener(this);
	        connection.start();
			ses  = (UserSessionRemote) ctx.lookup("hw3.UserSessionRemote");			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		s = new Scanner (System.in);
		messages = null;
		while (!authenticated()) {
			System.out.print ("Authentication failed! Enter q to quit or anything else to retry: ");
			if (s.nextLine().equals("q"))
				return;
		}
		System.out.println("Congratulations! You are now authenticated. \n");
		for (String str : ses.getStatus(userName, passWord))
				System.out.println(str);
		
		do {
			System.out.println("L: Log off");
			System.out.println("M: Check Messages");
			for (String i : ses.getActions(userName, passWord)) 
				System.out.println(i);
			try {
				String choice = s.nextLine();
				if (choice.equals("L")) {
					ses.logOff(userName, passWord);
					return;
				}
				if (choice.equals("M")) {
					if (messages != null) {
						System.out.println();
						System.out.println(this.messages);
						messages = null;
					}
					else
						System.out.println("No new messages since you last checked!");
					continue;
				}
				List<String> res = ses.doAction(choice, userName, passWord);
				System.out.println();
				for (String str : res) 
					System.out.println(str);
				System.out.print("Enter your next choice: ");
			}
			catch (NumberFormatException e) {
				System.out.print("Enter a valid choice! : ");
				continue;
			}
		} while (s.hasNextLine());
	}
	
	public boolean authenticated() 
	{
			System.out.println("Welcome to the Auction House. You need to login to proceed. Do you want to: ");
			System.out.println("1. Login\n2. Register");
			int r = Integer.parseInt(s.nextLine());
			if (r == 1) {
				return loggedIn();
			}
			else if (r == 2) {
				return registered();
			}
			else {
				return false;
			}
	}
	public boolean loggedIn() 
	{
		System.out.print("Enter your username and password\nUsername: ");
		userName = s.nextLine();
		System.out.print("Enter your password: ");
		passWord = s.nextLine();
		return ses.logIn(userName, passWord);
	}
	
	public boolean registered()
	{
		System.out.print("Choose a username: ");
		userName = s.nextLine();
		System.out.print("Choose a password: ");
		passWord = s.nextLine();
		return ses.register(userName, passWord);
	}
	
	public static void main(String[] args) {
		AuctionTester a = new AuctionTester();
		a.startSession();
		System.out.println("Client exiting...");
	}
	
	@Override
	public void onMessage(Message message) {
		try {
			if (messages == null)
				messages = "";
			messages += ((TextMessage) message).getText() + "\n";
		} 
		catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
