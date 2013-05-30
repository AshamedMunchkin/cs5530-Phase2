package cs5530;

import java.sql.*;
//import javax.servlet.http.*;

public class Order{
    private final int orderId;
    private final Date date;
    private final int amount;
    private final String username;
    private final String isbn;

    public Order(int orderId, Date date, int amount, String username, String isbn) {
        this.orderId = orderId;
        this.date = date;
        this.amount = amount;
        this.username = username;
        this.isbn = isbn;
    }

    public int getOrderId() {
        return orderId;
    }

    public Date getDate() {
        return date;
    }

    public int getAmount() {
        return amount;
    }

    public String getUsername() {
        return username;
    }

    public String getIsbn() {
        return isbn;
    }
}
