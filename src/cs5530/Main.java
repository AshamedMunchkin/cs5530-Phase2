package cs5530;

import java.io.*;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

public class Main {
    private static final SimpleDateFormat year = new SimpleDateFormat("y");

    private static int readInt(BufferedReader reader, int min, int max) {
        int result = min - 1;
        while (true) {
            String choice = null;
            try {
                choice = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                result = Integer.parseInt(choice);
            } catch (NumberFormatException ignored) {
            }

            if ((min == -1 || min <= result) && (max == -1 || max >= result)) {
                break;
            }
            System.out.print("Please enter a number");
            if (min != -1) {
                System.out.print(" greater than " + min);
                if (max != -1) {
                    System.out.print(" and");
                }
            }
            if (max != -1) {
                System.out.print(" less than " + max);
            }
            System.out.println(".");
        }
        return result;
    }

    private static int readMenuChoice(BufferedReader reader, int max) {
        int result = 0;
        while (true) {
            String choice = null;
            try {
                choice = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                result = Integer.parseInt(choice);
            } catch (NumberFormatException ignored) {
            }

            if (result >= 1 && result <= max) {
                break;
            }
            System.out.println ("Please enter a number corresponding to one of the items in the menu:");
        }
        return result;
    }

    private static void displayVideos(LinkedList<Video> videos) {
        System.out.println((videos.isEmpty() ? "0" : videos.size()) +
                (videos.size() == 1 ? " video" : " videos") + " found");

        for(Video video : videos) {
            System.out.println();
            System.out.println("ISBN: " + video.getIsbn());
            System.out.println("Title: " + video.getTitle());
            if (video.getYear() != null) {
                System.out.println("Year: " + year.format(video.getYear()));
            }
            if (video.getRating() != null) {
                System.out.println("Rating: " + video.getRating());
            }
            if (video.getFormat() != null) {
                System.out.println("Format: " + video.getFormat());
            }
            System.out.println("Price: $" + video.getPrice());
            System.out.println("Stock: " + video.getStock());
            if (video.getDirectors() != null) {
                System.out.println("Directors: " + video.getDirectors());
            }
            if (video.getPerformers() != null) {
                System.out.println("Performers: " + video.getPerformers());
            }
            if (video.getSubject() != null) {
                System.out.println("Subject: " + video.getSubject());
            }
            if (video.getKeywords() != null) {
                System.out.println("Keywords: " + video.getKeywords());
            }
        }
    }

    private static void displayReviews(LinkedList<Review> reviews, boolean isMenu) {
        if (reviews.isEmpty()) {
            System.out.println("0 reviews");
        } else {
            System.out.println(reviews.size() + " reviews");
        }

        int menuItem = 1;
        for(Review review : reviews) {
            System.out.println();
            if (isMenu) {
                System.out.println(menuItem++ + ".");
            }
            System.out.println(review.getReviewer() + ": " + review.getScore());
            System.out.print(year.format(review.getDate()));
            if (!review.getReview().isEmpty()) {
                System.out.print(" - " + review.getReview());
            }
            System.out.println();
        }
        System.out.println();
    }

    private static String getISBNFromTitle(BufferedReader reader, VideoStore videoStore, String title) throws
            SQLException {
        int choice;
        LinkedList<Pair<String, String>> whereClauses = new LinkedList<Pair<String, String>>();
        whereClauses.add(new Pair<String, String>("Title LIKE ?", '%' + title + '%'));
        LinkedList<String> whereConjunctions = new LinkedList<String>();
        LinkedList<Video> videos = videoStore.searchVideos(whereClauses, whereConjunctions, 'a');

        String isbn;
        if (videos.size() > 1) {
            System.out.println(videos.size() + " videos matched the given title.");
            int index = 1;
            for (Video video : videos) {
                System.out.println(index++ + ". " + video.getIsbn() + " - " + video.getTitle() +
                        " (" + video.getFormat() + ')');
            }

            System.out.println(index + ". Cancel");
            System.out.println("Please choose the video you want to review:");

            choice = readMenuChoice(reader, videos.size() + 1);

            if (choice == videos.size() + 1) {
                return null;
            }
            isbn = videos.get(choice - 1).getIsbn();
        } else if (videos.size() == 1) {
            isbn = videos.get(0).getIsbn();
        } else {
            System.out.println("No videos matched the given title.");
            return null;
        }

        return isbn;
    }

    private static void displayLogin(BufferedReader reader, VideoStore videoStore) {
        try {
            String username = "";
            String password = "";
            while (username.isEmpty()) {
                System.out.println("Please enter your username:");
                username = reader.readLine();
            }
            while (password.isEmpty()) {
                System.out.println("Please enter your password:");
                password = reader.readLine();
            }
            videoStore.logIn(username, password);
            if (videoStore.isLoggedIn()) {
                System.out.println("Login was successful.");
            } else {
                System.out.println("Login was unsuccessful.");
            }
        } catch (Exception e) {
            System.out.println("There was an error while logging in.");
        }
    }

    private static void displayRegistration(BufferedReader reader, VideoStore videoStore) {
        try {
            String username = "";
            String password = "";
            String name;
            String creditCard;
            String address;
            String phone;
            while (username.isEmpty()) {
                System.out.println("Please enter a username:");
                username = reader.readLine();
            }
            while (password.isEmpty()) {
                System.out.println("Please enter a password:");
                password = reader.readLine();
                System.out.println("Please enter the password again:");
                if (!password.equals(reader.readLine())) {
                    System.out.println("Passwords did not match.");
                    password = "";
                }
            }
            System.out.println("Please enter your name (optional):");
            name = reader.readLine();
            System.out.println("Please enter your credit card (optional):");
            creditCard = reader.readLine();
            System.out.println("Please enter your address (optional):");
            address = reader.readLine();
            System.out.println("Please enter your phone number (optional):");
            phone = reader.readLine();
            String result = videoStore.register(username, password, name, creditCard, address, phone);
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("There was an error while registering.");
        }
    }

    private static void displayVideoSearch(BufferedReader reader, VideoStore videoStore) {
        LinkedList<Pair<String, String>> whereClauses = new LinkedList<Pair<String, String>>();
        LinkedList<String> whereConjunctions = new LinkedList<String>();
        Boolean cancel = false;
        int choice;

        try {
            searchBuilderLoop: while(true) {

                System.out.println("Build your search with the following options.");
                System.out.println(" 1. Add title keyword/phrase");
                System.out.println(" 2. Add director");
                System.out.println(" 3. Add performer");
                System.out.println(" 4. Add MPAA rating");
                System.out.println(" 5. Return All");
                System.out.println(" 6. Cancel");
                System.out.println("Please enter your choice:");

                choice = readMenuChoice(reader, 6);

                switch (choice) {
                    case 1:
                        System.out.println("Please enter a title keyword/phrase:");
                        whereClauses.add(new Pair<String, String>(
                                "Title LIKE ?", '%' + reader.readLine() + '%'));
                        break;
                    case 2:
                        System.out.println("Please enter a director's name:");
                        whereClauses.add(new Pair<String, String>(
                                "Directors LIKE ?", '%' + reader.readLine() + '%'));
                        break;
                    case 3:
                        System.out.println("Please enter an actor's name:");
                        whereClauses.add(new Pair<String, String>(
                                "Performers LIKE ?", '%' + reader.readLine() + '%'));
                        break;
                    case 4:
                        System.out.println("Please enter an MPAA rating (G, PG, PG-13, R, NC-17):");
                        whereClauses.add(new Pair<String, String>("Rating=?", reader.readLine()));
                        break;
                    case 5:
                        whereClauses.add(new Pair<String, String>("Title LIKE ?", "%"));
                        break searchBuilderLoop;
                    case 6:
                        cancel = true;
                        break searchBuilderLoop;
                }

                System.out.println("Would you like to add more to your search?");
                System.out.println(" 1. And");
                System.out.println(" 2. Or");
                System.out.println(" 3. Search");
                System.out.println(" 4. Cancel");

                choice = readMenuChoice(reader, 4);

                switch (choice) {
                    case 1:
                        whereConjunctions.add("AND");
                        break;
                    case 2:
                        whereConjunctions.add("OR");
                        break;
                    case 3:
                        break searchBuilderLoop;
                    case 4:
                        cancel = true;
                        break searchBuilderLoop;
                }
            }
            if (cancel) {
                return;
            }

            System.out.println("How would you like to sort the results?");
            System.out.println(" 1. Year (descending)");
            System.out.println(" 2. All Ratings (descending)");
            System.out.println(" 3. Ratings only from users you trust (descending)");
            System.out.println(" 4. Cancel search");
            System.out.println("Please enter your choice:");

            choice = readMenuChoice(reader, 4);

            char order = '\0';
            switch (choice) {
                case 1:
                    order = 'a';
                    break;
                case 2:
                    order = 'b';
                    break;
                case 3:
                    order = 'c';
                    break;
                case 4:
                    cancel = true;
                    break;
            }

            if (cancel) {
                return;
            }

            displayVideos(videoStore.searchVideos(whereClauses, whereConjunctions, order));
        } catch (Exception e) {
            System.out.println("There was an error while searching.");
        }
    }

    private static void displayMostUsefulReviews(BufferedReader reader, VideoStore videoStore) {
        try {
            System.out.println("Please enter the title of the video you want to get reviews for:");
            String isbn = getISBNFromTitle(reader, videoStore, reader.readLine());

            System.out.println("Up to how many reviews?");
            int amount = Integer.parseInt(reader.readLine());

            displayReviews(videoStore.getUsefulReviews(isbn, amount), false);
        } catch (Exception e) {
            System.out.println("There was an error while getting the most useful reviews.");
        }
    }

    private static void displayVideoSuggestions(VideoStore videoStore) {
        try {
            displayVideos(videoStore.getVideoSuggestions());
        } catch (Exception e) {
            System.out.println("There was an error while getting video suggestions.");
        }
    }

    private static void displayReviewVideo(BufferedReader reader, VideoStore videoStore) {
        try {
            System.out.println("Please enter the title of the video you want review:");
            String isbn = getISBNFromTitle(reader, videoStore, reader.readLine());

            if (videoStore.getOwnReviews(isbn, 1).size() == 1) {
                System.out.println("You have already reviewed this video.");
                return;
            }

            System.out.println("Please enter your review score (0 = Terrible, 10 = Masterpiece):");
            int score = readInt(reader, 0, 10);

            String review = null;
            System.out.println("Please enter your review (optional):");
            try {
                review = reader.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (videoStore.reviewVideo(isbn, score, review)) {
                System.out.println("Successfully added the review for the video.");
                return;
            }
            System.out.println("Could not successfully add the review for the video.");

        } catch (Exception e) {
            System.out.println("There was an error while creating the review.");
        }
    }

    private static void displayRateReview(BufferedReader reader, VideoStore videoStore) {
        try {
            System.out.println("Please enter the title of the video the review was for:");
            String isbn = getISBNFromTitle(reader, videoStore, reader.readLine());

            System.out.println("Please enter the name of the reviewer (leave blank to show all reviews):");
            String reviewer = reader.readLine();

            LinkedList<Review> reviews = videoStore.getOtherReviews(reviewer, isbn);

            displayReviews(reviews, true);
            System.out.println(reviews.size() + 1 + ". Cancel");
            System.out.println("Please choose a review:");

            int choice = readMenuChoice(reader, reviews.size() + 1);

            if (choice == reviews.size() + 1) {
                return;
            }
            reviewer = reviews.get(choice - 1).getReviewer();

            System.out.println("Please enter the usefulness of the review (0 = Useless, 1 = Useful, 2 = Very Useful):");
            int score = readInt(reader, 0, 2);

            if (videoStore.rateReview(isbn, reviewer, score)) {
                System.out.println("Successfully added the usefulness rating for the review.");
                return;
            }
            System.out.println("Could not successfully add the usefulness rating for the review.");

        } catch (Exception e) {
            System.out.println("There was an error while creating the usefulness rating.");
        }
    }

    private static void displayTrustUser(BufferedReader reader, VideoStore videoStore) {
        try {
            System.out.println("Please enter the name of the user you want to trust/distrust:");
            String trustee = reader.readLine();
            int choice;

            LinkedList<User> users = videoStore.getUsers(trustee);
            if (users.size() > 1) {
                System.out.println(users.size() + " users matched the given title.");
                int index = 1;
                for (User user : users) {
                    System.out.println(index++ + ". " + user.getUsername());
                }

                System.out.println(index + ". Cancel");
                System.out.println("Please choose the user you want to trust/distrust:");

                choice = readMenuChoice(reader, users.size() + 1);

                if (choice == users.size() + 1) {
                    return;
                }
                trustee = users.get(choice - 1).getUsername();
            } else if (users.size() == 1) {
                trustee = users.get(0).getUsername();
            } else {
                System.out.println("No users matched the given username.");
                return;
            }

            System.out.println("Do you want to trust (1) or distrust (0) this user:");

            int trust = readInt(reader, 0, 1);

            if (videoStore.trustUser(trustee, trust == 1)) {
                if (trust == 1) {
                    System.out.println("You now trust " + trustee + '.');
                } else {
                    System.out.println("You now distrust " + trustee + '.');
                }
            } else {
                System.out.println("Could not trust/distrust the given user.");
            }

        } catch (Exception e) {
            System.out.println("There was an error while creating a user trust.");
        }
    }

    private static void displayOrderVideo(BufferedReader reader, VideoStore videoStore) {
        try {
            System.out.println("Please enter the title of the video you would like to order " +
                    "(leave blank to display all videos):");
            String isbn = getISBNFromTitle(reader, videoStore, reader.readLine());
            System.out.println("Please enter how many copies of the video you would like to order:");

            if (videoStore.placeOrder(isbn, readInt(reader, 1, -1))) {
                System.out.println("Sucessfully placed the order.");
            }
            System.out.println("Could not successfully place the order.");

        } catch (Exception e) {
            System.out.println("There was an error while ordering a video.");
        }
    }

    private static void displayRecord(VideoStore videoStore) {
        try {
            System.out.println();
            User user = videoStore.getUserProfile();
            System.out.println("*** User Profile ***");
            System.out.println("Username: " + user.getUsername());
            System.out.println("Name: " + user.getName());
            System.out.println("Credit Card: " + user.getCreditCard());
            System.out.println("Address: " + user.getAddress());
            System.out.println("Phone: " + user.getPhone());
            if (user.getIsManager()) {
                System.out.println("Manager: True");
            } else {
                System.out.println("Manager: False");
            }
            System.out.println();
            LinkedList<Order> orders = videoStore.getOwnOrders();
            System.out.println("*** Order History ***");
            for (Order order : orders) {
                Video video = videoStore.getVideoFromISBN(order.getIsbn());
                System.out.println(year.format(order.getDate()) + " - " + video.getTitle() + ": " + order.getAmount());
                System.out.println();
            }
            System.out.println();
            LinkedList<Review> reviews = videoStore.getOwnReviews("", 0);
            System.out.println("*** Reviews ***");
            for (Review review : reviews) {
                Video video = videoStore.getVideoFromISBN(review.getIsbn());
                System.out.println(video.getIsbn() + " " + video.getTitle() + " (" + video.getFormat() + ")");
                System.out.println(review.getReviewer() + ": " + review.getScore());
                System.out.print(year.format(review.getDate()));
                if (!review.getReview().isEmpty()) {
                    System.out.print(" - " + review.getReview());
                }
                System.out.println();
                System.out.println();
            }
            System.out.println();
            LinkedList<UsefulnessRating> usefulnessRatings = videoStore.getOwnUsefulnessRatings();
            System.out.println("*** Usefulness Ratings ***");
            for (UsefulnessRating usefulnessRating : usefulnessRatings) {
                Video video = videoStore.getVideoFromISBN(usefulnessRating.getIsbn());
                System.out.print(usefulnessRating.getReviewer() + "'s review of " + video.getTitle());
                if (usefulnessRating.getScore() == 0) {
                    System.out.println(": Useless");
                }
                if (usefulnessRating.getScore() == 1) {
                    System.out.println(": Useful");
                }
                if (usefulnessRating.getScore() == 2) {
                    System.out.println(": Very Useful");
                }
                System.out.println();
            }
            System.out.println();
            LinkedList<Trusts> trusts = videoStore.getTrusts();
            System.out.println("*** Trusted and Distrusted Users ***");
            for (Trusts trust : trusts) {
                System.out.print("You ");
                if (trust.getTrusted()) {
                    System.out.print("trusted ");
                } else {
                    System.out.print("distrusted ");
                }
                System.out.println(trust.getTrustee() + " on " + trust.getDate());
                System.out.println();
            }

        } catch (Exception e) {
            System.out.println("There was an error while printing your record.");
        }
    }

    private static void displayTwoDegrees(BufferedReader reader, VideoStore videoStore) throws IOException, SQLException {
        System.out.println("Please enter the first performer:");
        String performer1 = reader.readLine();
        System.out.println("Please enter the second performer:");
        String performer2 = reader.readLine();

        int degrees = videoStore.getTwoDegrees(performer1, performer2);

        if (degrees > 1) {
            System.out.println("These actors are " + degrees + " degrees apart.");
        }
        if (degrees == 1) {
            System.out.println("These actors are one degree apart.");
        }
        if (degrees == 0) {
            System.out.println("These actors are either more than 2 degrees apart or not connected.");
        }
    }

    private static void displayAddVideo(BufferedReader reader, VideoStore videoStore) {
        try {
            String isbn;
            System.out.println("Please enter the ISBN of the video:");
            while (true) {
                isbn = reader.readLine();
                if (!isbn.isEmpty()) {
                    break;
                }

                System.out.println("Please enter an ISBN number:");
            }
            String title;
            System.out.println("Please enter the title of the video:");
            while (true) {
                title = reader.readLine();
                if (!title.isEmpty()) {
                    break;
                }
                System.out.println("Please enter a title:");
            }
            System.out.println("Please enter the year of the video (optional):");
            int year = readInt(reader, 0, 9999);
            System.out.println("Please enter the MPAA rating of the video (optional):");
            String rating = reader.readLine();
            System.out.println("Please enter the format of the video (optional):");
            String format = reader.readLine();
            Double price = 0.0;
            System.out.println("Please enter the price of the video:");
            while (true) {
                try {
                    price = Double.parseDouble(reader.readLine());
                } catch (Exception ignored) {
                }

                if (price > 0) {
                    break;
                }

                System.out.println("Please enter a number:");
            }
            int stock = -1;
            System.out.println("Please enter the current stock of the video:");
            while (true) {
                try {
                    stock = Integer.parseInt(reader.readLine());
                } catch (Exception ignored) {
                }

                if (stock > -1) {
                    break;
                }

                System.out.println("Please enter a number.");
            }
            StringBuilder directors = new StringBuilder();
            System.out.println("Please enter the directors of the video (one on each line; finish with a blank line).");
            while (true) {
                String director = reader.readLine();
                if (director.isEmpty()) {
                    break;
                }
                if (directors.length() != 0) {
                    directors.append(',');
                }
                directors.append(director);
            }
            StringBuilder performers = new StringBuilder();
            System.out.println("Please enter the performers in the video (one on each line; finish with a blank line):");
            while (true) {
                String performer = reader.readLine();
                if (performer.isEmpty()) {
                    break;
                }
                if (performers.length() != 0) {
                    performers.append(',');
                }
                performers.append(performer);
            }
            StringBuilder subjects = new StringBuilder();
            System.out.println("Please enter the subjects of the video (one on each line; finish with a blank line):");
            while (true) {
                String subject = reader.readLine();
                if (subject.isEmpty()) {
                    break;
                }
                if (subjects.length() != 0) {
                    subjects.append(',');
                }
                subjects.append(subject);
            }
            StringBuilder keywords = new StringBuilder();
            System.out.println("Please enter the directors of the video (one on each line; finish with a blank line):");
            while (true) {
                String keyword = reader.readLine();
                if (keyword.isEmpty()) {
                    break;
                }
                if (keywords.length() != 0) {
                    keywords.append(',');
                }
                keywords.append(keyword);
            }

            if (videoStore.addVideo(isbn, title, year, rating, format, price, stock, directors.toString(),
                    performers.toString(), subjects.toString(), keywords.toString())) {
                System.out.println("Successfully added the video.");
                return;
            }
            System.out.println("Unable to successfully add the video.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void displayAddInventory(BufferedReader reader, VideoStore videoStore) {
        try {
            System.out.println("Please enter the title of the video you would like to add inventory to:");
            String isbn = getISBNFromTitle(reader, videoStore, reader.readLine());
            System.out.println("How much inventory is being added?");
            int amount = 0;
            while(true) {
                try {
                    amount = Integer.parseInt(reader.readLine());
                } catch (Exception ignored) {
                }
                if (amount > 0) {
                    break;
                }
            }
            if (videoStore.addInventory(isbn, amount)) {
                System.out.println("Successfully added inventory.");
                return;
            }
            System.out.println("Unable to successfully add inventory.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void displayStatistics(BufferedReader reader, VideoStore videoStore) {
        try {
            System.out.println("Please enter the start of a date range (YYYY-MM-DD):");
            Date beginDate = Date.valueOf(reader.readLine());
            System.out.println("Please enter the end of a date range (YYYY-MM-DD)");
            Date endDate = Date.valueOf(reader.readLine());
            System.out.println("Please enter the max number of items you would like to see in each category:");
            int amount = readInt(reader, 1, -1);
            System.out.println();
            System.out.println("*** Most Popular Videos ***");
            LinkedList<Video> popularVideos = videoStore.getPopularVideos(beginDate, endDate, amount);
            int i = 1;
            for (Video video : popularVideos) {
                System.out.println(
                        i++ + ". " + video.getIsbn() + " - " + video.getTitle() + " (" + video.getFormat() +")");
            }
            System.out.println();
            System.out.println("*** Most Popular Directors ***");
            LinkedList<String> popularDirectors = videoStore.getPopularDirectors(beginDate, endDate, amount);
            i = 1;
            for (String director : popularDirectors) {
                System.out.println(i++ + ". " + director);
            }
            System.out.println();
            System.out.println("*** Most Popular Performers ***");
            LinkedList<String> popularPerformers = videoStore.getPopularPerformers(beginDate, endDate, amount);
            i = 1;
            for (String performer : popularPerformers) {
                System.out.println(i++ + ". " + performer);
            }
            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void displayUserAwards(BufferedReader reader, VideoStore videoStore) {
        try {
            System.out.println("Please enter the max number of user you would like to see in each category");
            int amount = readInt(reader, 1, -1);
            System.out.println();
            System.out.println("*** Most Trusted Users ***");
            LinkedList<User> trustedUsers = videoStore.getMostTrustedUsers(amount);
            int i = 1;
            for (User user : trustedUsers) {
                System.out.println(i++ + ". " + user.getUsername());
            }
            System.out.println();
            System.out.println("*** Most Useful Users ***");
            LinkedList<User> usefulUsers = videoStore.getMostUsefulUsers(amount);
            i = 1;
            for (User user : usefulUsers) {
                System.out.println(i++ + ". " + user.getUsername());
            }
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public static void main(String[] args) {
		VideoStore videoStore;
		try {
			videoStore = new VideoStore();
			System.out.println ("Database connection established");

			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


			mainLoop: while(true) {
                System.out.println("*** Video Store Management System ***");

                if (!videoStore.isLoggedIn()) {
                    System.out.println(" 1. Login");
                    System.out.println(" 2. Register");
                    System.out.println(" 3. Exit");
                    System.out.println("Please enter your choice:");

                    int choice = readMenuChoice(reader, 3);

                    switch (choice) {
                        case 1:
                            displayLogin(reader, videoStore);
                            break;
                        case 2:
                            displayRegistration(reader, videoStore);
                            break;
                        case 3:
                            break mainLoop;
                    }
                } else {
                    System.out.println(" 1. Search videos");
                    System.out.println(" 2. Get the most useful reviews for a video");
                    System.out.println(" 3. Suggested videos");
                    System.out.println(" 4. Review a video");
                    System.out.println(" 5. Rate the usefulness of a review");
                    System.out.println(" 6. Trust/Distrust a user");
                    System.out.println(" 7. Order videos");
                    System.out.println(" 8. Print user record");
                    System.out.println(" 9. Find the degrees of separation between two performers");

                    int choice;
                    if (!videoStore.isManager()) {
                        System.out.println("10. Exit");
                        System.out.println("Please enter your choice:");

                        choice = readMenuChoice(reader, 10);
                        if (choice == 10) {
                            choice = 14;
                        }
                    } else {
                        System.out.println("10. Add video");
                        System.out.println("11. Add inventory");
                        System.out.println("12. Get the most popular videos, directors, and actors for this semester");
                        System.out.println("13. Get the most trusted users, and most useful users");
                        System.out.println("14. Exit");
                        System.out.println("Please enter your choice:");

                        choice = readMenuChoice(reader, 14);
                    }

                    switch (choice) {
                        case 1:
                            displayVideoSearch(reader, videoStore);
                            break;
                        case 2:
                            displayMostUsefulReviews(reader, videoStore);
                            break;
                        case 3:
                            displayVideoSuggestions(videoStore);
                            break;
                        case 4:
                            displayReviewVideo(reader, videoStore);
                            break;
                        case 5:
                            displayRateReview(reader, videoStore);
                            break;
                        case 6:
                            displayTrustUser(reader, videoStore);
                            break;
                        case 7:
                            displayOrderVideo(reader, videoStore);
                            break;
                        case 8:
                            displayRecord(videoStore);
                            break;
                        case 9:
                            displayTwoDegrees(reader, videoStore);
                            break;
                        case 10:
                            displayAddVideo(reader, videoStore);
                            break;
                        case 11:
                            displayAddInventory(reader, videoStore);
                            break;
                        case 12:
                            displayStatistics(reader, videoStore);
                            break;
                        case 13:
                            displayUserAwards(reader, videoStore);
                            break;
                        case 14:
                            break mainLoop;
                    }
                }
                System.out.println("Press <Enter> to continue.");
                reader.readLine();
			}
			videoStore.close();
			System.out.println("Database connection terminated.");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println ("Cannot connect to database server.");
		}
	}
}
