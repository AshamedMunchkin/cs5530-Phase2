package cs5530;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

public class Video {
    private final String isbn;
    private final String title;
    private final Date year;
    private final String rating;
    private final String format;
    private final double price;
    private final int stock;
    private final LinkedList<String> directors;
    private final LinkedList<String> performers;
    private final LinkedList<String> subject;
    private final LinkedList<String> keywords;

    public Video(String isbn, String title, Date year, String rating, String format, Double price, int stock,
                 Collection<String> directors, Collection<String> performers, Collection<String> subject,
                 Collection<String> keywords) {
        this.isbn = isbn;
        this.title = title;
        this.year = year;
        this.rating = rating;
        this.format = format;
        this.price = price;
        this.stock = stock;
        this.directors = new LinkedList<String>(directors);
        this.performers = new LinkedList<String>(performers);
        this.subject = new LinkedList<String>(subject);
        this.keywords = new LinkedList<String>(keywords);
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public Date getYear() {
        return year;
    }

    public String getRating() {
        return rating;
    }

    public String getFormat() {
        return format;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public Collection<String> getDirectors() {
        return directors;
    }

    public Collection<String> getPerformers() {
        return performers;
    }

    public Collection<String> getKeywords() {
        return keywords;
    }

    public Collection<String> getSubject() {
        return subject;
    }
}
