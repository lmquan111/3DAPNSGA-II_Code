enum DepotType {
    DELIVERY, 
    PICKUP    
}

public class Depot {
    private int id;                 
    private double x;               
    private double y;               
    private DepotType type;         
    
    
    private double startTimeWindow; 
    private double endTimeWindow;   

    
    public Depot(int id, double x, double y, DepotType type, double startTimeWindow, double endTimeWindow) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
        this.startTimeWindow = startTimeWindow;
        this.endTimeWindow = endTimeWindow;
    }

    
    public int getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public DepotType getType() { return type; }
    public double getStartTimeWindow() { return startTimeWindow; }
    public double getEndTimeWindow() { return endTimeWindow; }

    
    public void setId(int id) { this.id = id; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setType(DepotType type) { this.type = type; }
    public void setStartTimeWindow(double startTimeWindow) { this.startTimeWindow = startTimeWindow; }
    public void setEndTimeWindow(double endTimeWindow) { this.endTimeWindow = endTimeWindow; }

    @Override
    public String toString() {
        return "Depot{" +
                "id=" + id +
                ", type=" + type +
                ", x=" + x +
                ", y=" + y +
                ", TW=[" + startTimeWindow + ", " + endTimeWindow + "]" +
                '}';
    }
}