import java.util.Comparator;

public class FactorComparator implements Comparator<Factor> {
    @Override
    public int compare(Factor o1, Factor o2) {
        // comparing the title of the factors by their Size sum value
        return o1.getFactorTable().size() - o2.getFactorTable().size();
    }
}