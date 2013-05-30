package cs5530;

public class UsefulnessRating {
    private final String reviewer;
    private final String isbn;
    private final String rater;
    private final int score;

    public UsefulnessRating(String reviewer, String isbn, String rater, int score) {
        this.reviewer = reviewer;
        this.isbn = isbn;
        this.rater = rater;
        this.score = score;
    }

    public String getReviewer() {
        return reviewer;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getRater() {
        return rater;
    }

    public int getScore() {
        return score;
    }
}
