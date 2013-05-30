package cs5530;

import java.util.Date;

class Review {
    private final String reviewer;
    private final String isbn;
    private final int score;
    private final Date date;
    private final String review;

    public Review(String reviewer, String isbn, int score, Date date, String review) {
        this.reviewer = reviewer;
        this.isbn = isbn;
        this.score = score;
        this.date = date;
        this.review = review;
    }

    public String getReviewer() {
        return reviewer;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getScore() {
        return score;
    }

    public Date getDate() {
        return date;
    }

    public String getReview() {
        return review;
    }
}
