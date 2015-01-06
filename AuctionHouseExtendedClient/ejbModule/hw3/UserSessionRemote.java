package hw3;

import java.util.List;

import javax.ejb.Remote;

@Remote
public interface UserSessionRemote {
	public boolean logIn(String user, String pass);
	public void logOff(String user, String pass);
	public boolean register(String user, String pass);
	public List<String> getStatus(String user, String pass);
	public List<String> getActions(String user, String pass);
	public boolean placeBid (String user, String pass, int item, int bidAmount);
	public List<String> doAction(String choice, String userName, String passWord);
}
