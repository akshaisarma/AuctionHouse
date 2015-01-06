package hw3;


import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AuctionUser implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	private String userName;
	private String passWord;
	private boolean loggedIn;
	
	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
	
	public boolean authenticate(String user, String pass) {
		return userName.equals(user) && passWord.equals(pass);
	}
	
}