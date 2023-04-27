package model;

public class MyListData
{
    private String description;
    private String rack;
    private String qty;

    public MyListData(String description, String rack, String qty) {
        this.description = description;
        this.rack = rack;
        this.qty = qty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }


}