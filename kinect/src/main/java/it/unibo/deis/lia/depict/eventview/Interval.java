package it.unibo.deis.lia.depict.eventview;


public class Interval {

    public static final Interval TRUE = new Interval(1.0, 1.0);
    public static final Interval FALSE = new Interval(0.0, 0.0);
    public static final Interval UNKNOWN = new Interval(0.0, 1.0);

    private double lowerBound;
    private double upperBound;

    /**
     * @param lowerBound
     * @param upperBound
     */
    public Interval(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * @return
     */
    public double getLowerBound() {
        return lowerBound;
    }

    /**
     * @return
     */
    public double getUncertainty() {
        return upperBound - lowerBound;
    }

    /**
     * @return
     */
    public double getUpperBound() {
        return upperBound;
    }


    /*
       * (non-Javadoc)
       *
       * @see java.lang.Object#toString()
       */

    public String toString() {
        String result = getClass().getSimpleName() + "( ";
        if (this.equals(FALSE))
            result += "FALSE";
        else if (this.equals(TRUE))
            result += "TRUE";
        else if (this.equals(UNKNOWN))
            result += "UNKNOWN";
        else {
            result += "lowerBound='" + lowerBound + "', upperBound='"
                    + upperBound + "'";
        }
        result += " )";
        return result;
    }

}