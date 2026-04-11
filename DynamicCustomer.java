public class DynamicCustomer {
    private int id;                 
    private double x;               
    private double y;               
    
    private double demand;          // weight
    private CustomerType type;      // CustomerType.PICKUP
    
    
    private double startTimeWindow;  
    private double endTimeWindow;   
    
           
    private double knownTime;       

    
    public DynamicCustomer(int id, double x, double y, double demand, CustomerType type, 
                           double startTimeWindow, double endTimeWindow, double knownTime) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.demand = demand;
        this.type = type;
        this.startTimeWindow = startTimeWindow;
        this.endTimeWindow = endTimeWindow;
        this.knownTime = knownTime;
    }

    
    public int getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getDemand() { return demand; }
    public CustomerType getType() { return type; }
    public double getStartTimeWindow() { return startTimeWindow; }
    public double getEndTimeWindow() { return endTimeWindow; }
    public double getKnownTime() { return knownTime; }

    
    public void setId(int id) { this.id = id; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setDemand(double demand) { this.demand = demand; }
    public void setType(CustomerType type) { this.type = type; }
    public void setStartTimeWindow(double startTimeWindow) { this.startTimeWindow = startTimeWindow; }
    public void setEndTimeWindow(double endTimeWindow) { this.endTimeWindow = endTimeWindow; }
    public void setKnownTime(double knownTime) { this.knownTime = knownTime; }

    @Override
    public String toString() {
        return "DynamicCustomer{" +
                "id=" + id +
                ", demand=" + demand +
                ", knownTime=" + knownTime + // In ra để dễ debug thuật toán chèn
                ", TW=[" + startTimeWindow + ", " + endTimeWindow + "]" +
                '}';
    }
}