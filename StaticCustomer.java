enum CustomerType {
    DELIVERY, 
    PICKUP    
}

public class StaticCustomer {
    private int id;                 
    private double x;               
    private double y;               
    
    private double demand;          // Weight
    private CustomerType type;      
    
    
    private double startTimeWindow; 
    private double endTimeWindow;   
    
    
    private int clusterId;          

    // Constructor
    public StaticCustomer(int id, double x, double y, double demand, CustomerType type, 
                          double startTimeWindow, double endTimeWindow, int clusterId) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.demand = demand;
        this.type = type;
        this.startTimeWindow = startTimeWindow;
        this.endTimeWindow = endTimeWindow;
        this.clusterId = clusterId;
    }

    
    public int getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getDemand() { return demand; }
    public CustomerType getType() { return type; }
    public double getStartTimeWindow() { return startTimeWindow; }
    public double getEndTimeWindow() { return endTimeWindow; }
    public int getClusterId() { return clusterId; }

    
    public void setId(int id) { this.id = id; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setDemand(double demand) { this.demand = demand; }
    public void setType(CustomerType type) { this.type = type; }
    public void setStartTimeWindow(double startTimeWindow) { this.startTimeWindow = startTimeWindow; }
    public void setEndTimeWindow(double endTimeWindow) { this.endTimeWindow = endTimeWindow; }
    public void setClusterId(int clusterId) { this.clusterId = clusterId; }

    @Override
    public String toString() {
        return "StaticCustomer{" +
                "id=" + id +
                ", type=" + type +
                ", demand=" + demand +
                ", TW=[" + startTimeWindow + ", " + endTimeWindow + "]" +
                ", clusterId=" + clusterId +
                '}';
    }
}