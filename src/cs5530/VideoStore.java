package cs5530;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

class VideoStore {
	private final Connection connection;
	private String username = "";
    private boolean isManager;

    static SimpleDateFormat year = new SimpleDateFormat("y");
    private static final SimpleDateFormat sqlDate = new SimpleDateFormat("yyyy-MM-dd");

	public VideoStore() throws Exception {
		try{
			String username = "cs5530u33";
			String password = "mdtiepn5";
			String url = "jdbc:mysql://georgia.eng.utah.edu/cs5530db33";
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			connection = DriverManager.getConnection (url, username, password);

			/*//DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
			//stmt=con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			stmt = con.createStatement();
			//stmt=con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);*/
		} catch(Exception e) {
			System.err.println("Unable to open mysql JDBC connection. The error is as follows,");
			System.err.println(e.getMessage());
			throw e;
		}
	}

	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception ignored) {
			}
		}
	}

	public boolean isLoggedIn() {
		return !username.isEmpty();
	}

    public boolean isManager() {
        return isManager;
    }

	public void logIn(String username, String password) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("SELECT Password, IsManager FROM Users " +
                "WHERE Username = ?");
		statement.setString(1, username);
		ResultSet result = statement.executeQuery();
		if (!result.next() || !result.getString(1).equals(password)) {
            return;
        }
		this.username = username;
        isManager = result.getBoolean(2);
	}

	public String register(String username, String password, String name,
                           String creditCard, String address, String phone) throws SQLException {
		if (username.isEmpty()) {
			return "Please enter a username.";
		}
		if (password.isEmpty()) {
			return "Please enter a password.";
		}
		PreparedStatement statement = connection.prepareStatement("SELECT Username FROM Users WHERE Username = ?");
		statement.setString(1, username);
		ResultSet result = statement.executeQuery();

		if (result.next()) {
            return "Username is unavailable. Please choose a different username.";
        }

		LinkedList<String> columns = new LinkedList<String>();
		LinkedList<String> values = new LinkedList<String>();

		columns.add("Username");
		values.add(username);
		columns.add("Password");
		values.add(password);
		if (!name.isEmpty()) {
			columns.add("Name");
			values.add(name);
		}
		if (!creditCard.isEmpty()) {
			columns.add("CreditCard");
			values.add(creditCard);
		}
		if (!address.isEmpty()) {
			columns.add("Address");
			values.add(address);
		}
		if (!phone.isEmpty()) {
			columns.add("Phone");
			values.add(phone);
		}

		boolean success = insertSpecificValues("Users", columns, values);

		if (!success) {
            return "Registration was unsuccessful.";
        }

		logIn(username, password);
		return "Registration was successful.";
	}

    public LinkedList<Video> searchVideos(LinkedList<Pair<String, String>> whereClauses,
                                           LinkedList<String> whereConjunctions, char order) throws SQLException {
        LinkedList<String> select = new LinkedList<String>();
        select.add("*");
        LinkedList<String> from = new LinkedList<String>();
        from.add("Videos");
        Pair<String, LinkedList<String>> orderBy;
        if (order == 'a') {
            orderBy = new Pair<String, LinkedList<String>>("Year", new LinkedList<String>());
        } else if (order == 'b') {
            orderBy = new Pair<String, LinkedList<String>>(
                    "(SELECT AVG(Score) FROM Reviews WHERE Reviews.ISBN = Videos.ISBN)", new LinkedList<String>());
        } else {
            orderBy = new Pair<String, LinkedList<String>>(
                    "(SELECT AVG(Score) FROM Reviews, Trusts WHERE IsTrusted = 1 AND Trustee = Reviewer AND " +
                            "Reviews.ISBN = Videos.ISBN AND Truster = ?)", new LinkedList<String>());
            orderBy.getValue().add(username);
        }
        String orderDirection = "DESC";
        int limit = 0;
        ResultSet resultSet = executeDynamicQuery(select, from, whereClauses, whereConjunctions, orderBy,
                orderDirection, limit);

        return videoListFromResultSet(resultSet);
    }

    public LinkedList<Review> getUsefulReviews(String isbn, int amount) throws SQLException {
        return getReviews("", isbn, amount);
    }

    public LinkedList<Video> getVideoSuggestions() throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * " +
                "FROM Videos " +
                "WHERE 0 < (SELECT SUM(Different.Amount) " +
                    "FROM Orders as Same, Orders as Different " +
                    "WHERE Same.Username=Different.Username AND " +
                        "Same.Username<>? AND " +
                        "Same.ISBN<>Different.ISBN AND " +
                        "Same.ISBN=Videos.ISBN AND " +
                        "EXISTS (SELECT * " +
                            "FROM Orders " +
                            "WHERE ISBN=Different.ISBN AND " +
                                "Username=?)) " +
                "ORDER BY (SELECT SUM(Different.Amount) " +
                    "FROM Orders as Same, Orders as Different " +
                    "WHERE Same.Username=Different.Username AND " +
                        "Same.Username<>? AND " +
                        "Same.ISBN<>Different.ISBN AND " +
                        "Same.ISBN=Videos.ISBN AND " +
                        "EXISTS (SELECT * " +
                            "FROM Orders " +
                            "WHERE ISBN=Different.ISBN AND " +
                                "Username=?)) DESC");
        statement.setString(1, username);
        statement.setString(2, username);
        statement.setString(3, username);
        statement.setString(4, username);

        ResultSet resultSet = statement.executeQuery();

        return videoListFromResultSet(resultSet);
    }

    public boolean reviewVideo(String isbn, int score, String review) throws SQLException {
        LinkedList<String> columns = new LinkedList<String>();
        LinkedList<String> values = new LinkedList<String>();
        columns.add("Reviewer");
        values.add(username);
        columns.add("ISBN");
        values.add(isbn);
        columns.add("Score");
        values.add(String.valueOf(score));
        if (!review.isEmpty()) {
            columns.add("Review");
            values.add(review);
        }

        return insertSpecificValues("Reviews", columns, values);
    }

    public boolean rateReview(String isbn, String reviewer, int score) throws SQLException {
        if (reviewer.equals(username)) {
            return false;
        }
        LinkedList<String> columns = new LinkedList<String>();
        LinkedList<String> values = new LinkedList<String>();
        columns.add("ISBN");
        values.add(isbn);
        columns.add("Reviewer");
        values.add(reviewer);
        columns.add("Rater");
        values.add(username);
        columns.add("Score");
        values.add(String.valueOf(score));

        return insertSpecificValues("Usefulness", columns, values);
    }

    public boolean trustUser(String trustee, boolean isTrusted) throws SQLException {
        LinkedList<String> columns = new LinkedList<String>();
        LinkedList<String> values = new LinkedList<String>();
        columns.add("Truster");
        values.add(username);
        columns.add("Trustee");
        values.add(trustee);
        columns.add("IsTrusted");
        if (isTrusted) {
            values.add("1");
        } else {
            values.add("0");
        }

        return insertSpecificValues("Trusts", columns, values);
    }

    public boolean placeOrder(String isbn, int amount) throws SQLException {
        LinkedList<String> columns = new LinkedList<String>();
        LinkedList<String> values = new LinkedList<String>();
        columns.add("Amount");
        values.add(String.valueOf(amount));
        columns.add("Username");
        values.add(username);
        columns.add("ISBN");
        values.add(isbn);

        return insertSpecificValues("Orders", columns, values);
    }

    public User getUserProfile() throws SQLException {
        LinkedList<String> select = new LinkedList<String>();
        select.add("Username");
        select.add("Name");
        select.add("CreditCard");
        select.add("Address");
        select.add("Phone");
        select.add("IsManager");
        LinkedList<String> from = new LinkedList<String>();
        from.add("Users");
        LinkedList<Pair<String, String>> whereClauses = new LinkedList<Pair<String, String>>();
        whereClauses.add(new Pair<String, String>("Username=?", username));
        LinkedList<String> whereConjunctions = new LinkedList<String>();
        ResultSet resultSet = executeDynamicQuery(select, from, whereClauses, whereConjunctions, null, null, 0);

        resultSet.first();

        return new User(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                    resultSet.getString(4), resultSet.getString(5), resultSet.getBoolean(6));
    }

    public LinkedList<Order> getOwnOrders() throws SQLException {
        LinkedList<String> select = new LinkedList<String>();
        select.add("OrderID");
        select.add("Date");
        select.add("Amount");
        select.add("Username");
        select.add("ISBN");
        LinkedList<String> from = new LinkedList<String>();
        from.add("Orders");
        LinkedList<Pair<String, String>> whereClauses = new LinkedList<Pair<String, String>>();
        whereClauses.add(new Pair<String, String>("Username=?", username));
        LinkedList<String> whereConjunctions = new LinkedList<String>();
        ResultSet resultSet = executeDynamicQuery(select, from, whereClauses, whereConjunctions, null, null, 0);

        LinkedList<Order> orders = new LinkedList<Order>();
        while (resultSet.next()) {
            orders.add(new Order(resultSet.getInt(1), resultSet.getDate(2), resultSet.getInt(3), resultSet.getString(4),
                    resultSet.getString(5)));
        }

        return orders;
    }

    public boolean addVideo(String isbn, String title, int year, String rating, String format, Double price, int stock,
                            String directors, String performers, String subject, String keywords) throws SQLException {
        LinkedList<String> columns = new LinkedList<String>();
        LinkedList<String> values = new LinkedList<String>();
        columns.add("ISBN");
        values.add(String.valueOf(isbn));
        columns.add("Title");
        values.add(title);
        if (year > 0) {
            columns.add("Year");
            values.add(String.valueOf(year));
        }
        if (!rating.isEmpty()) {
            columns.add("Rating");
            values.add(rating);
        }
        if (!format.isEmpty()) {
            columns.add("Format");
            values.add(format);
        }
        columns.add("Price");
        values.add(String.valueOf(price));
        columns.add("Stock");
        values.add(String.valueOf(stock));
        if (!directors.isEmpty()) {
            columns.add("Directors");
            values.add(directors);
        }
        if (!performers.isEmpty()) {
            columns.add("Performers");
            values.add(performers);
        }
        if (!keywords.isEmpty()) {
            columns.add("Subject");
            values.add(subject);
        }
        if (!keywords.isEmpty()) {
            columns.add("Keywords");
            values.add(keywords);
        }

        return insertSpecificValues("Videos", columns, values);
    }

    private static LinkedList<Video> videoListFromResultSet(ResultSet resultSet) throws SQLException {
        LinkedList<Video> videos = new LinkedList<Video>();
        while (resultSet.next()) {
            videos.add(new Video(resultSet.getString(1), resultSet.getString(2), resultSet.getDate(3),
                    resultSet.getString(4), resultSet.getString(5), resultSet.getDouble(6), resultSet.getInt(7),
                    Arrays.asList(resultSet.getString(8).split(",")), Arrays.asList(resultSet.getString(9).split(",")),
                    Arrays.asList(resultSet.getString(10).split(",")),
                    Arrays.asList(resultSet.getString(11).split(","))));
        }
        return videos;
    }

    public LinkedList<Review> getOwnReviews(String isbn, int amount) throws SQLException {
        return getReviews(username, isbn, amount);
    }

    LinkedList<Review> getReviews(String reviewer, String isbn, int amount) throws SQLException {
        LinkedList<String> select = new LinkedList<String>();
        select.add("*");
        LinkedList<String> from = new LinkedList<String>();
        from.add("Reviews");
        LinkedList<Pair<String, String>> whereClauses = new LinkedList<Pair<String, String>>();
        if (!reviewer.isEmpty()) {
            whereClauses.add(new Pair<String, String>("Reviewer=?", reviewer));
        }
        if (!isbn.isEmpty()) {
            whereClauses.add(new Pair<String, String>("ISBN=?", isbn));
        }
        LinkedList<String> whereConjunctions = new LinkedList<String>();
        if (whereClauses.size() == 2) {
            whereConjunctions.add("AND");
        }
        Pair<String, LinkedList<String>> orderBy = new Pair<String, LinkedList<String>>("(SELECT AVG(SCORE) " +
                "FROM Usefulness WHERE Usefulness.Reviewer = Reviews.Reviewer AND Usefulness.ISBN = Reviews.ISBN)",
                new LinkedList<String>());
        String orderDirection = "DESC";
        ResultSet resultSet =
                executeDynamicQuery(select, from, whereClauses, whereConjunctions, orderBy, orderDirection, amount);
        LinkedList<Review> result = new LinkedList<Review>();
        while (resultSet.next()) {
            result.add(new Review(resultSet.getString(1), resultSet.getString(2), resultSet.getInt(3),
                    resultSet.getDate(4), resultSet.getString(5)));
        }

        return result;
    }

    public LinkedList<Review> getOtherReviews(String reviewer, String isbn) throws SQLException {
        LinkedList<String> select = new LinkedList<String>();
        select.add("*");
        LinkedList<String> from = new LinkedList<String>();
        from.add("Reviews");
        LinkedList<Pair<String, String>> whereClauses = new LinkedList<Pair<String, String>>();
        whereClauses.add(new Pair<String, String>("Reviewer<>?", username));
        if (!reviewer.isEmpty()) {
            whereClauses.add(new Pair<String, String>("Reviewer=?", reviewer));
        }
        if (!isbn.isEmpty()) {
            whereClauses.add(new Pair<String, String>("ISBN=?", isbn));
        }
        LinkedList<String> whereConjunctions = new LinkedList<String>();
        if (whereClauses.size() == 2) {
            whereConjunctions.add("AND");
        }
        Pair<String, LinkedList<String>> orderBy = new Pair<String, LinkedList<String>>("(SELECT AVG(SCORE) " +
                "FROM Usefulness WHERE Usefulness.Reviewer = Reviews.Reviewer AND Usefulness.ISBN = Reviews.ISBN)",
                new LinkedList<String>());
        String orderDirection = "DESC";
        ResultSet resultSet =
                executeDynamicQuery(select, from, whereClauses, whereConjunctions, orderBy, orderDirection, 0);
        LinkedList<Review> result = new LinkedList<Review>();
        while (resultSet.next()) {
            result.add(new Review(resultSet.getString(1), resultSet.getString(2), resultSet.getInt(3),
                    resultSet.getDate(4), resultSet.getString(5)));
        }

        return result;
    }

    public LinkedList<UsefulnessRating> getOwnUsefulnessRatings() throws SQLException {
        LinkedList<String> select = new LinkedList<String>();
        select.add("*");
        LinkedList<String> from = new LinkedList<String>();
        from.add("Usefulness");
        LinkedList<Pair<String, String>> whereClauses = new LinkedList<Pair<String, String>>();
        whereClauses.add(new Pair<String, String>("Rater=?", username));
        LinkedList<String> whereConjunctions = new LinkedList<String>();
        Pair<String, LinkedList<String>> orderBy =
                new Pair<String, LinkedList<String>>("Score", new LinkedList<String>());
        String orderDirection = "DESC";
        ResultSet resultSet = executeDynamicQuery(select, from, whereClauses, whereConjunctions, orderBy,
                orderDirection, 0);
        LinkedList<UsefulnessRating> result = new LinkedList<UsefulnessRating>();
        while (resultSet.next()) {
            result.add(new UsefulnessRating(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                    resultSet.getInt(4)));
        }

        return result;
    }

    public LinkedList<Trusts> getTrusts() throws SQLException {
        LinkedList<String> select = new LinkedList<String>();
        select.add("*");
        LinkedList<String> from = new LinkedList<String>();
        from.add("Trusts");
        LinkedList<Pair<String, String>> whereClauses = new LinkedList<Pair<String, String>>();
        whereClauses.add(new Pair<String, String>("Truster=?", username));
        LinkedList<String> whereConjunctions = new LinkedList<String>();
        Pair<String, LinkedList<String>> orderBy =
                new Pair<String, LinkedList<String>>("Date", new LinkedList<String>());
        String orderDirection = "ASC";
        ResultSet resultSet = executeDynamicQuery(select, from, whereClauses, whereConjunctions, orderBy,
                orderDirection, 0);
        LinkedList<Trusts> result = new LinkedList<Trusts>();
        while (resultSet.next()) {
            result.add(new Trusts(resultSet.getString(1), resultSet.getString(2), resultSet.getInt(3) == 1,
                    resultSet.getDate(4)));
        }

        return result;
    }

    public int getTwoDegrees(String performer1, String performer2) throws SQLException {
        LinkedList<String> select = new LinkedList<String>();
        select.add("Performers");
        LinkedList<String> from = new LinkedList<String>();
        from.add("Videos");
        LinkedList<Pair<String, String>> whereClauses = new LinkedList<Pair<String, String>>();
        whereClauses.add(new Pair<String, String>("Performers LIKE ?", '%' + performer1 + '%'));
        LinkedList<String> whereConjunctions = new LinkedList<String>();
        ResultSet resultSet = executeDynamicQuery(select, from, whereClauses, whereConjunctions, null, null, 0);

        HashSet<String> performersLeft = new HashSet<String>();
        HashSet<String> seen = new HashSet<String>();
        while (resultSet.next()) {
            for (String performer : resultSet.getString(1).split(",")) {
                if (performer.equals(performer2)) {
                    return 1;
                }
                if (performer.equals(performer1)) {
                    seen.add(performer);
                }
                performersLeft.add(performer);
            }
        }

        HashSet<String> performersRight = new HashSet<String>();
        whereClauses.set(0, new Pair<String, String>("Performers LIKE ?", '%' + performer2 + '%'));
        resultSet = executeDynamicQuery(select, from, whereClauses, whereConjunctions, null, null, 0);
        while (resultSet.next()) {
            for (String performer : resultSet.getString(1).split(",")) {
                if (performersLeft.contains(performer)) {
                    return 2;
                }
                if (performer.equals(performer2)) {
                    seen.add(performer);
                }
                performersRight.add(performer);
            }
        }

        return 0;
    }

    public boolean addInventory(String isbn, int amount) throws SQLException {
        Video video = getVideoFromISBN(isbn);
        PreparedStatement statement = connection.prepareStatement("UPDATE Videos SET Stock=? WHERE ISBN=?");
        statement.setInt(1, video.getStock() + amount);
        statement.setString(2, video.getIsbn());

        return statement.executeUpdate() == 1;
    }

    public LinkedList<Video> getPopularVideos(Date startDate, Date endDate, int amount) throws SQLException {
        LinkedList<String> select = new LinkedList<String>();
        select.add("*");
        LinkedList<String> from = new LinkedList<String>();
        from.add("Videos");
        LinkedList<String> orderBinds = new LinkedList<String>();
        orderBinds.add(sqlDate.format(startDate));
        orderBinds.add(sqlDate.format(endDate));
        Pair<String, LinkedList<String>> orderBy = new Pair<String, LinkedList<String>>(
                "(SELECT SUM(Amount) FROM Orders WHERE Date>=? AND Date<=? AND Orders.ISBN=Videos.ISBN)", orderBinds);
        String orderDirection = "DESC";
        return videoListFromResultSet(executeDynamicQuery(select, from, null, null, orderBy, orderDirection, amount));
    }

    public LinkedList<String> getPopularDirectors(Date startDate, Date endDate, int amount) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT ISBN, SUM(Amount) FROM Orders WHERE Date>=? AND Date<=? GROUP BY ISBN");
        statement.setString(1, sqlDate.format(startDate));
        statement.setString(2, sqlDate.format(endDate));
        ResultSet resultSet = statement.executeQuery();

        HashMap<String, Integer> directors = new HashMap<String, Integer>();
        while (resultSet.next()) {
            Video video = getVideoFromISBN(resultSet.getString(1));
            for (String director : video.getDirectors()) {
                int popularity = resultSet.getInt(2);
                if (directors.containsKey(director)) {
                    popularity += directors.get(director);
                }
                directors.put(director, popularity);
            }
        }
        int max = 0;
        for (int value : directors.values()) {
            max = Math.max(max, value);
        }

        LinkedList<String> popularDirectors = new LinkedList<String>();
        while (popularDirectors.size() < amount || popularDirectors.size() < directors.size()) {
            for (String key : directors.keySet()) {
                if (directors.get(key) == max) {
                    popularDirectors.add(key);
                }
            }
            max--;
        }

        return popularDirectors;
    }

    public LinkedList<String>getPopularPerformers(Date startDate, Date endDate, int amount) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT ISBN, SUM(Amount) FROM Orders WHERE Date>=? AND Date<=? GROUP BY ISBN");
        statement.setString(1, sqlDate.format(startDate));
        statement.setString(2, sqlDate.format(endDate));
        ResultSet resultSet = statement.executeQuery();

        HashMap<String, Integer> performers = new HashMap<String, Integer>();
        while (resultSet.next()) {
            Video video = getVideoFromISBN(resultSet.getString(1));
            for (String performer : video.getPerformers()) {
                int popularity = resultSet.getInt(2);
                if (performers.containsKey(performer)) {
                    popularity += performers.get(performer);
                }
                performers.put(performer, popularity);
            }
        }
        int max = 0;
        for (int value : performers.values()) {
            max = Math.max(max, value);
        }

        LinkedList<String> popularPerformers = new LinkedList<String>();
        while (popularPerformers.size() < amount || popularPerformers.size() < performers.size()) {
            for (String key : performers.keySet()) {
                if (performers.get(key) == max) {
                    popularPerformers.add(key);
                }
            }
            max--;
        }

        return popularPerformers;
    }

    public Video getVideoFromISBN(String isbn) throws SQLException {
        LinkedList<String> select = new LinkedList<String>();
        select.add("*");
        LinkedList<String> from = new LinkedList<String>();
        from.add("Videos");
        LinkedList<Pair<String, String>> whereClauses = new LinkedList<Pair<String, String>>();
        whereClauses.add(new Pair<String, String>("ISBN=?", isbn));
        LinkedList<String> whereConjunctions = new LinkedList<String>();
        Pair<String, LinkedList<String>> orderBy =
                new Pair<String, LinkedList<String>>("Year", new LinkedList<String>());
        String orderDirection = "DESC";
        ResultSet resultSet = executeDynamicQuery(select, from, whereClauses, whereConjunctions, orderBy,
                orderDirection, 0);

        resultSet.next();

        return new Video(resultSet.getString(1), resultSet.getString(2), resultSet.getDate(3), resultSet.getString(4),
                resultSet.getString(5), resultSet.getDouble(6), resultSet.getInt(7),
                Arrays.asList(resultSet.getString(8).split(",")), Arrays.asList(resultSet.getString(9).split(",")),
                Arrays.asList(resultSet.getString(10).split(",")), Arrays.asList(resultSet.getString(11).split(",")));
    }

    public LinkedList<User> getUsers(String username) throws SQLException {
        LinkedList<String> select = new LinkedList<String>();
        select.add("Username");
        select.add("Name");
        select.add("CreditCard");
        select.add("Address");
        select.add("Phone");
        select.add("IsManager");
        LinkedList<String> from = new LinkedList<String>();
        from.add("Users");
        LinkedList<Pair<String, String>> whereClauses = new LinkedList<Pair<String, String>>();
        if (!username.isEmpty()) {
            whereClauses.add(new Pair<String, String>("Username LIKE ?", '%' + username + '%'));
        }
        LinkedList<String> whereConjunctions = new LinkedList<String>();
        ResultSet resultSet = executeDynamicQuery(select, from, whereClauses, whereConjunctions, null, null, 0);

        LinkedList<User> result = new LinkedList<User>();
        while (resultSet.next()) {
            result.add(new User(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                    resultSet.getString(4), resultSet.getString(5), resultSet.getBoolean(6)));
        }

        return result;
    }

    public LinkedList<User> getMostTrustedUsers(int amount) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT Username, Name, CreditCard, Address, Phone, IsManager FROM Users " +
                        "ORDER BY (SELECT COUNT(*) FROM Trusts WHERE IsTrusted=1 AND Trustee=Username) -" +
                        "(SELECT COUNT(*) FROM Trusts WHERE IsTrusted=0 AND Trustee=Username) DESC " +
                        "LIMIT ?");
        statement.setInt(1, amount);
        ResultSet resultSet = statement.executeQuery();
        LinkedList<User> users = new LinkedList<User>();
        while (resultSet.next()) {
            users.add(new User(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                    resultSet.getString(4), resultSet.getString(5), resultSet.getInt(6) == 1));
        }
        return users;
    }

    public LinkedList<User> getMostUsefulUsers(int amount) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT Username, Name, CreditCard, Address, Phone, IsManager FROM Users " +
                        "ORDER BY (SELECT AVG(Score) FROM Usefulness WHERE Reviewer=Username) DESC " +
                        "LIMIT ?");
        statement.setInt(1, amount);
        ResultSet resultSet = statement.executeQuery();
        LinkedList<User> users = new LinkedList<User>();
        while (resultSet.next()) {
            users.add(new User(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                    resultSet.getString(4), resultSet.getString(5), resultSet.getInt(6) == 1));
        }
        return users;
    }

	private boolean insertSpecificValues(String table, List<String> columns, List<String> values) throws SQLException {
		StringBuilder query = new StringBuilder(128);
        query.append("INSERT INTO ").append(table).append(" (");
        StringBuilder columnsStringBuilder = new StringBuilder(64);
		StringBuilder parameters = new StringBuilder(16);
		for (String column : columns) {
			columnsStringBuilder.append(", ").append(column);
			parameters.append(", ?");
		}
        query.append(columnsStringBuilder.substring(2)).append(") VALUES (").append(parameters.substring(2))
                .append(')');

		PreparedStatement statement = connection.prepareStatement(query.toString());
		for (int parameter = 0; parameter < values.size(); parameter++) {
			statement.setString(parameter + 1, values.get(parameter));
		}
		return statement.executeUpdate() == 1;
	}

    private ResultSet executeDynamicQuery(LinkedList<String> select, LinkedList<String> from,
                                          LinkedList<Pair<String, String>> whereClauses,
                                          LinkedList<String> whereConjunctions,
                                          Pair<String, LinkedList<String>> orderBy,
                                          String orderDirection, int limit) throws SQLException {
        StringBuilder query = new StringBuilder(256);
        query.append("SELECT ");
        for (String column : select) {
            query.append(column);
            if (!select.getLast().equals(column)) {
                query.append(',');
            }
            query.append(' ');
        }
        query.append("FROM ");
        for (String table : from) {
            query.append(table);
            if (!from.getLast().equals(table)) {
                query.append(',');
            }
            query.append(' ');
        }
        if (whereClauses != null && !whereClauses.isEmpty()) {
            query.append("WHERE ");
            int index = 0;
            for (Pair<String, String> whereClause : whereClauses) {
                query.append(whereClause.getKey()).append(' ');
                if (index < whereConjunctions.size()) {
                    query.append(whereConjunctions.get(index++)).append(' ');
                }
            }
        }
        if (orderBy != null) {
            query.append("ORDER BY ");
            query.append(orderBy.getKey()).append(' ');
            query.append(orderDirection).append(' ');
        }
        if (limit > 0) {
            query.append("LIMIT ?");
        }
        PreparedStatement statement = connection.prepareStatement(query.toString());
        int index = 1;
        if (whereClauses != null) {
            for (Pair<String, String> whereClause : whereClauses) {
                statement.setString(index++, whereClause.getValue());
            }
        }
        if (orderBy != null && !orderBy.getValue().isEmpty()) {
            for (String orderByBind : orderBy.getValue()) {
                statement.setString(index++, orderByBind);
            }
        }
        if (limit > 0) {
            statement.setInt(index, limit);
        }

        return statement.executeQuery();
    }
}
