package model;

public class lostlist
{
    private String Serialnumber;
    private String racknumber;

    public lostlist(String serialnumber, String racknumber) {
        Serialnumber = serialnumber;
        this.racknumber = racknumber;
    }

    public String getSerialnumber() {
        return Serialnumber;
    }

    public void setSerialnumber(String serialnumber) {
        Serialnumber = serialnumber;
    }

    public String getRacknumber() {
        return racknumber;
    }

    public void setRacknumber(String racknumber) {
        this.racknumber = racknumber;
    }
}