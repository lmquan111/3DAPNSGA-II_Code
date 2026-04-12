import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static List<Depot> depots = new ArrayList<>();
    static List<StaticCustomer> staticCustomers = new ArrayList<>();
    static List<DynamicCustomer> dynamicCustomers = new ArrayList<>();

    static void readData() {

        String csvFile = "16 Instance16.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            
            br.readLine(); // Bỏ qua dòng tiêu đề

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy);

                int id = Integer.parseInt(data[0]);
                double x = Double.parseDouble(data[1]);
                double y = Double.parseDouble(data[2]);
                double deliveryDemand = Double.parseDouble(data[3]);
                double pickupDemand = Double.parseDouble(data[4]);
                double twLeftD = Double.parseDouble(data[5]);
                double twRightD = Double.parseDouble(data[6]);
                double twLeftP = Double.parseDouble(data[7]);
                double twRightP = Double.parseDouble(data[8]);
                double knownTime = Double.parseDouble(data[9]);
                int dcs = Integer.parseInt(data[10]);
                int pcs = Integer.parseInt(data[11]);

                if (dcs >= 1000 || pcs >= 1000) {
                    if (dcs >= 1000) {
                        depots.add(new Depot(id, x, y, DepotType.DELIVERY, twLeftD, twRightD));
                    } 
                    else {
                        depots.add(new Depot(id, x, y, DepotType.PICKUP, twLeftP, twRightP));
                    }
                } 
                else {
                    CustomerType type;
                    double demand;
                    double startTimeWindow;
                    double endTimeWindow;

                    int tmpCluster = -1;
                    
                    if (deliveryDemand > 0) {
                        type = CustomerType.DELIVERY;
                        demand = deliveryDemand;
                        startTimeWindow = twLeftD;
                        endTimeWindow = twRightD;

                        if (dcs == 1) {
                            tmpCluster = 2;
                        } else if (dcs == 2) {
                            tmpCluster = 0;
                        }

                    } 
                    else {
                        type = CustomerType.PICKUP;
                        demand = pickupDemand;
                        startTimeWindow = twLeftP;
                        endTimeWindow = twRightP;

                        if (pcs == 1) {
                            tmpCluster = 1;
                        } else if (pcs == 2) {
                            tmpCluster = 3;
                        }
                    }

                    if (knownTime == 0) {
                        staticCustomers.add(new StaticCustomer(id, x, y, demand, type, startTimeWindow, endTimeWindow, tmpCluster));
                    } 
                    else {
                        dynamicCustomers.add(new DynamicCustomer(id, x, y, demand, type, startTimeWindow, endTimeWindow, knownTime));
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // System.out.println("=== DANH SÁCH KHO (" + depots.size() + ") ===");
        // for (Depot d : depots) {
        //     System.out.println(d.toString());
        // }

        // System.out.println("\n=== DANH SÁCH KHÁCH TĨNH (" + staticCustomers.size() + ") ===");
        // for (int i = 0; i < staticCustomers.size(); i++) {
        //     System.out.println(staticCustomers.get(i).toString());
        // }

        // System.out.println("\n=== DANH SÁCH KHÁCH ĐỘNG (" + dynamicCustomers.size() + ") ===");
        // for (DynamicCustomer dc : dynamicCustomers) {
        //     System.out.println(dc.toString());
        // }
    }

    public static void main(String[] args) {
        readData();

        // ANSGA-II
        int Run = 1000; // số lần lặp NSGA-II
        int numTimeSlot = 10;

        // tìm thời gian kết thúc của việc vận chuyển
        double timeHorizon = 0;
        for (Depot d : depots) {
            if (d.getEndTimeWindow() > timeHorizon) {
                timeHorizon = d.getEndTimeWindow();
            }
        }

        ANSGA algorithm = new ANSGA(Run, numTimeSlot, timeHorizon, staticCustomers, dynamicCustomers, depots);
        List<Individual> Perato = algorithm.runAlgorithm();

        for(Individual x : Perato){
            System.out.println(x.toString());
        }
    
    }
}