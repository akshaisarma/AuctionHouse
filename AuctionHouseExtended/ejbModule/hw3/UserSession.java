package hw3;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Session Bean implementation class UserSession
 */
@Stateless
@LocalBean
public class UserSession implements UserSessionRemote, TimedObject{
	@PersistenceContext(name="AuctionHouseExtended")
	private EntityManager em;
	
	@Resource
    private TimerService timerService;
    @Resource(mappedName="jms/alarmNotifier")
    private Topic topic;
    @Resource(mappedName="alarmConnectionFactory")
    private ConnectionFactory topicFactory;
	
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean logIn(String user, String pass) {
		AuctionUser cur = em.find(AuctionUser.class, user);
		if (cur != null && cur.getPassWord().equals(pass)) {
			cur.setLoggedIn(true);
			return true;
		}
		return false;
	}
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean register(String user, String pass) {
		AuctionUser exists = em.find(AuctionUser.class, user);
		if (exists == null && !user.equals("admin")) {
			AuctionUser nuser = new AuctionUser();
			nuser.setLoggedIn(true);
			nuser.setUserName(user);
			nuser.setPassWord(pass);
			em.persist(nuser);
			return true;
		}
		return false;
	}
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void logOff(String user, String pass) {
		AuctionUser cur = em.find(AuctionUser.class, user);
		if (cur != null && cur.getPassWord().equals(pass)) {
			cur.setLoggedIn(false);
		}
	}
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<String> getStatus(String user, String pass) {
		List<String> res = new ArrayList<String>();
		Query q = em.createQuery("SELECT a FROM AuctionExpired a WHERE a.userName = :un").setParameter("un", user);
		@SuppressWarnings("unchecked")
		List<AuctionExpired>qres = (List<AuctionExpired>) q.getResultList();
		for (AuctionExpired ex : qres) {
			if (!ex.isNotified()) {
				res.add(("You won Item: " + ex.getItemId() + " " + ex.getItemName() + " . The winning amount was " + ex.getFinalValue()));
				ex.setNotified(true);
			}				
		}
		return res;
	}

	public List<String> getActions(String user, String pass) {
		List<String> ret = new ArrayList<String>();	
		if (user.equals("admin")) {
			AuctionUser exists = em.find(AuctionUser.class, "admin");
			if (exists != null && exists.getPassWord().equals(pass)) 
				ret.add("A: Add an item to the Auction. (Example A itemId initialvalue auctionduration itemName(no spaces)) ");
		}
		ret.add("1: See the items in Auction. (Enter 1)");
		ret.add("2: Place a bid. (Example: 2 itemID bidAmount) Get itemId from Auctionlist, bidAmount > currentvalue");
		ret.add("3. See History of past and pending bids.");
		return ret;
	}
	
	public List<String> doAction(String inputline, String user, String pass) {
		List<String> ret = new ArrayList<String>();	
		String[] actions = inputline.split(" ");
		String input = actions[0];
		if (user.equals("admin")) {
			AuctionUser exists = em.find(AuctionUser.class, "admin");
			if (exists != null && exists.getPassWord().equals(pass) && input.equals("A")) {
				try {
					if (adminAdd(Integer.parseInt(actions[1]), Integer.parseInt(actions[2]), Integer.parseInt(actions[3]), actions[4] )) 
						ret.add("Item successfully added");
					else
						ret.add("Item addition failed. Item probably already exists.");
					return ret;	
				}
				catch (Exception e) {}
			}
		}
		if (input.equals("1")) 
			return printAuctionList();
		else if (input.equals("2")) {
			try {
				if (placeBid(user, pass, Integer.parseInt(actions[1]), Integer.parseInt(actions[2]))) 
					ret.add("Bid successfully placed");
				else
					ret.add("Bid failed. Check if item number or bid amount is valid.");
				return ret;
			}
			catch (Exception e) {}
		}
		else if (input.equals("3")) {
				return getHistory(user);
		}
		ret.add("Not a valid input! Reenter.");
		return ret;
	}
	
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private List<String> getHistory(String user) {
    	List<String> res = new ArrayList<String>();
    	Query q = em.createQuery("SELECT a FROM AuctionBid a WHERE a.auctionUserName = :un").setParameter("un", user);
		@SuppressWarnings("unchecked")
		List<AuctionBid>qres = (List<AuctionBid>) q.getResultList();
		for (AuctionBid bi : qres) 
			res.add ("You bid "+bi.getBidAmount() + " on "+bi.getItemName()+" : "+bi.getAuctionItemId());
		return res;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private boolean adminAdd(int itemId, int init, int seconds, String itemName) {
		AuctionItem exists = em.find(AuctionItem.class, itemId);
		if (exists == null) {
			AuctionItem ai = new AuctionItem(itemId, itemName, init, "admin");
			em.persist(ai);
			List<String> res = new ArrayList<String>();
			res.add("Created item " + ai.getItemName() + " : "+ ai.getId() + " with initial value "+ ai.getCurrentValue());
			createAuctionTimer(seconds, itemId);
			return true;
		}
		return false;
	}
	
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
	private List<String> printAuctionList() {
		List<String> res = new ArrayList<String>();;
		Query q = em.createQuery("SELECT a FROM AuctionItem a");
		@SuppressWarnings("unchecked")
		List<AuctionItem>qres = (List<AuctionItem>) q.getResultList();
		for (AuctionItem it : qres) 
			res.add("Item: "+it.getId() + ". Name: "+it.getItemName() +" -\n" +
					"     Current Value: " + it.getCurrentValue() + "\n" +
					"     Current Leader: " + it.getUserName() + "\n\n");
					
		return res;	
	}
	
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean placeBid(String user, String pass, int itemId, int bidAmount) {
		AuctionItem exists = em.find(AuctionItem.class, itemId);
		if (exists != null && bidAmount > exists.getCurrentValue()) {
			AuctionBid ab = new AuctionBid();
			ab.setAuctionItemId(itemId);
			ab.setAuctionUserName(user);
			ab.setBidAmount(bidAmount);
			ab.setItemName(exists.getItemName());
			em.persist(ab);
			exists.setCurrentValue(bidAmount);
			exists.setUserName(user);
			return true;
		}
		return false;	
	}
	    
    private void createAuctionTimer(int seconds, int itemId)
    {	
    	timerService.createTimer(seconds * 1000, itemId); //duration is in milliseconds
    }
    
    @Override
	public void ejbTimeout(Timer timer) {
    	System.out.println("Timeout occurred!");
		Connection topicConnection = null;
		Session session = null;
		TextMessage message = null;
		MessageProducer producer = null;
		try {
			topicConnection = topicFactory.createConnection();
			session = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(topic);
			int itemId = (Integer) timer.getInfo();
			AuctionItem ai = em.find(AuctionItem.class, itemId);
			String itemName = ai.getItemName();
			String victor = ai.getUserName();
			int finalAmount = ai.getCurrentValue();
			em.remove(ai);
			
			AuctionUser au = em.find(AuctionUser.class, victor);
			AuctionExpired ae = new AuctionExpired();
			ae.setFinalValue(finalAmount);
			ae.setItemId(itemId);
			ae.setItemName(itemName);
			ae.setUserName(victor);
			if (au.isLoggedIn()) 
				ae.setNotified(true);
			else
				ae.setNotified(false);
			em.persist(ae);
			
			message = session.createTextMessage("Auction for Item "+itemName + " : " + itemId + " expired! The victor was "+victor+ ". The final price was " +finalAmount);
			producer.send(message);
			timer.cancel(); //Not necessary since container should do it but doing it anyway.
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}

