package com.example.tasks;

/**
 * @author		Albert Levy albert.school2015@gmail.com
 * @version     2.1
 * @since		9/3/2024
 * <p>
 * Utility class for date string conversions.
 * <p>
 * This class provides static methods to convert date strings between
 * two formats:
 * <ul>
 *     <li>Database format (YYYYMMDD)</li>
 *     <li>Display format (DD-MM-YYYY)</li>
 * </ul>
 */
public class Utilities {
    /**
     * Converts a date string from database format (YYYYMMDD) to display format (DD-MM-YYYY).
     * <p>
     * Example: "20231225" will be converted to "25-12-2023".
     *
     * @param str The date string in YYYYMMDD format.
     *            It is assumed that the input string is exactly 8 characters long
     *            and represents a valid date in this format.
     * @return The date string in DD-MM-YYYY format.
     *         Returns an empty string or throws an exception if the input is malformed,
     *         depending on the robustness desired (currently, it might throw
     *         StringIndexOutOfBoundsException for incorrect input length).
     */
    public static String db2Dsiplay(String str){
        return str.substring(6,8)+"-"+str.substring(4,6)+"-"+str.substring(0,4);
    }

    /**
     * Converts a date string from display format (DD-MM-YYYY) to database format (YYYYMMDD).
     * <p>
     * Example: "25-12-2023" will be converted to "20231225".
     *
     * @param str The date string in DD-MM-YYYY format.
     *            It is assumed that the input string is exactly 10 characters long
     *            and represents a valid date in this format with hyphens as separators.
     * @return The date string in YYYYMMDD format.
     *         Returns an empty string or throws an exception if the input is malformed,
     *         depending on the robustness desired (currently, it might throw
     *         StringIndexOutOfBoundsException for incorrect input length or format).
     */
    public static String display2DB(String str){
        return str.substring(7,11)+str.substring(4,6)+str.substring(0,2);
    }
}
