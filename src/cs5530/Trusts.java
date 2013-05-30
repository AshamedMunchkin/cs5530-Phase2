package cs5530;

import java.util.Date;

class Trusts {
    private final String truster;
    private final String trustee;
    private final Boolean isTrusted;
    private final Date date;

    public Trusts(String truster, String trustee, Boolean isTrusted, Date date) {
        this.truster = truster;
        this.trustee = trustee;
        this.isTrusted = isTrusted;
        this.date = date;
    }

    public String getTruster() {
        return truster;
    }

    public String getTrustee() {
        return trustee;
    }

    public Boolean getTrusted() {
        return isTrusted;
    }

    public Date getDate() {
        return date;
    }
}
