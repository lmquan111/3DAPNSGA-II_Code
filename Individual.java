import java.util.ArrayList;
import java.util.List;

public class Individual implements Comparable<Individual> {
    
    private List<Integer> chromosome; 
    

    private int NV;           
    private double TOC;       
    
    private int rank;
    private double crowdingDistance;

    public Individual(List<Integer> initialChromosome) {
        this.chromosome = new ArrayList<>(initialChromosome);
    }
    
    public Individual() {
        this.chromosome = new ArrayList<>();
    }

    @Override
    public Individual clone() {
        Individual copy = new Individual();
        copy.setNV(this.NV);
        copy.setTOC(this.TOC);
        copy.setRank(this.rank);
        copy.setCrowdingDistance(this.crowdingDistance);
        
        copy.setChromosome(new ArrayList<>(this.chromosome));
        
        return copy;
    }

    public List<Integer> getChromosome() { return chromosome; }
    public void setChromosome(List<Integer> chromosome) { this.chromosome = chromosome; }

    public int getNV() { return NV; }
    public void setNV(int NV) { this.NV = NV; }

    public double getTOC() { return TOC; }
    public void setTOC(double TOC) { this.TOC = TOC; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public double getCrowdingDistance() { return crowdingDistance; }
    public void setCrowdingDistance(double crowdingDistance) { this.crowdingDistance = crowdingDistance; }

    @Override
    public int compareTo(Individual other) {
        return Double.compare(other.crowdingDistance, this.crowdingDistance); 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Individual that = (Individual) o;
        return chromosome != null ? chromosome.equals(that.chromosome) : that.chromosome == null;
    }

    @Override
    public int hashCode() {
        return chromosome != null ? chromosome.hashCode() : 0;
    }
    
    public String toString() {
        return "Individual{" +
                "TOC=" + TOC +
                ", NV=" + NV +
                '}';
    }
}