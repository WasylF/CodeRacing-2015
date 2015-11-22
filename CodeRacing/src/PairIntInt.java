
/**
 *
 * @author Wsl_F
 */
public class PairIntInt {

    public int first;
    public int second;

    public PairIntInt() {
        this.first = 0;
        this.second = 0;
    }

    public PairIntInt(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public PairIntInt(double first, double second) {
        this.first = (int) first;
        this.second = (int) second;
    }

    @Override
    public String toString() {
        return "( " + first + " , " + second + " )";
    }
}
