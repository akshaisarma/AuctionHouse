package hw3;


import java.io.Serializable;
import javax.persistence.*;

@Entity
public class AuctionBid implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue
	private int id;
	
	private int auctionItemId;
	private String itemName;
	private String auctionUserName;
	private int bidAmount;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getAuctionItemId() {
		return auctionItemId;
	}
	public void setAuctionItemId(int auctionItemId) {
		this.auctionItemId = auctionItemId;
	}
	public String getAuctionUserName() {
		return auctionUserName;
	}
	public void setAuctionUserName(String auctionUserName) {
		this.auctionUserName = auctionUserName;
	}
	public int getBidAmount() {
		return bidAmount;
	}
	public void setBidAmount(int bidAmount) {
		this.bidAmount = bidAmount;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}	
}