import java.util.Date;

public class HistoryModel {
    private Date date;
    private Long timeSpend;
    private String url;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getTimeSpend() {
        return timeSpend;
    }

    public void setTimeSpend(long timeSpend) {
        this.timeSpend = timeSpend;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
