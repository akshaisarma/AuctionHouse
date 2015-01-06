package hw3;


import javax.persistence.*;

@Entity
public class AuctionExpired {
	@Id
	@GeneratedValue
	private int id;
	
	private String userName;
	private int itemId;

	private int finalValue;
	private String itemName;
	private boolean notified;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public int getFinalValue() {
		return finalValue;
	}
	public void setFinalValue(int finalValue) {
		this.finalValue = finalValue;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public boolean isNotified() {
		return notified;
	}
	public void setNotified(boolean notified) {
		this.notified = notified;
	}
	
}
