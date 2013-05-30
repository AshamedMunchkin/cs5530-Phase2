package cs5530;

public class User {
    private final String username;
    private final String name;
    private final String creditCard;
    private final String address;
    private final String phone;
    private final Boolean isManager;

    public User(String username, String name, String creditCard, String address, String phone, Boolean manager) {
        this.username = username;
        this.name = name;
        this.creditCard = creditCard;
        this.address = address;
        this.phone = phone;
        isManager = manager;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public Boolean getIsManager() {
        return isManager;
    }
}
