package managers;

/**
 * RateUtil – tiny helpers for drop rates.
 *
 * Rates are stored as PERMILLE (parts per 1000) so 0.5% can be kept as the
 * whole number 5 (no floating-point rounding problems):
 *     100%  = 1000      45% = 450      0.5% = 5
 */
public final class RateUtil {

    private RateUtil() { }   // helper class: nobody should create an object of it

    /** 450 -> "45",  5 -> "0.5",  125 -> "12.5" */
    public static String fmt(int permille) {
        return (permille / 10) + (permille % 10 == 0 ? "" : "." + (permille % 10));
    }

    /**
     * Turns what the admin typed as a percent ("45", "0.5") into permille.
     *
     * @return the permille value, or -1 if the text is not a number from 0 to 100.
     */
    public static int parsePercent(String text) {
        try {
            double percent = Double.parseDouble(text.trim());
            if (percent < 0 || percent > 100) return -1;
            return (int) Math.round(percent * 10);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
