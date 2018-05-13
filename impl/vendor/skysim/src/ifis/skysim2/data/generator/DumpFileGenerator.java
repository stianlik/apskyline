/*
 * Imports the NBA dataset from is60-db2.
 */
package ifis.skysim2.data.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads tuples from a dumpfile created by DBDUmpFileCreator.
 * @author Christoph
 */
public class DumpFileGenerator extends AbstractDataGenerator {

    private String fileName;
    private int numOfRows;
    private int numOfColumns;
    private BufferedReader reader;

    public DumpFileGenerator(String fileName) {
        this.fileName = fileName;
        openFile();
        try {
            reader.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void openFile() {
        // open file and read header
        File file = new File(fileName);
        try {
            reader = new BufferedReader(new FileReader(file));
            reader.readLine();
            // columns
            String s = reader.readLine();
            if (s.contains("numOfColumns: ")) {
                System.out.println(s);
                numOfColumns = Integer.parseInt(s.substring(14, s.length()));

            } else {
                throw new RuntimeException("DumpFile " + file.getAbsolutePath() + " has missing/malformatted num of columns section");
            }
            // rows
            s = reader.readLine();
            if (s.contains("numOfRows: ")) {
                numOfRows = Integer.parseInt(s.substring(11, s.length()));
            } else {
                throw new RuntimeException("DumpFile " + file.getAbsolutePath() + " has missing/malformatted num of rows section");
            }
            reader.readLine();
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("DumpFile " + file.getAbsolutePath() + " is could not be found", ex);
        } catch (IOException ex) {
            throw new RuntimeException("DumpFile " + file.getAbsolutePath() + " is malformated", ex);
        }

    }

    public String getFileName() {
        return fileName;
    }

    public int getNumOfColumns() {
        return numOfColumns;
    }

    public int getNumOfRows() {
        return numOfRows;
    }

    /**
     * Returns a value array of the given dump files. 
     * @param d needs to be less or smaller than maximum columns in the dumpfile, use -1 for all columns
     * @param n needs to be less or small than maximum rows in the dumpfile, use -1 for all rows
     * @return values
     */
    @Override
    public float[] generate(int d, int n) {
        openFile();
        try {
            // check if parameters valid
            if (d > numOfColumns) {
                throw new IllegalArgumentException(d + " exceeds maximum of allowed dimensions : " + numOfColumns);
            }
            if (n > numOfRows) {
                throw new IllegalArgumentException(d + " exceeds maximum of allowed rows : " + numOfRows);
            }
            if (n < 0) {
                n = numOfRows;
            }
            if (d < 0) {
                d = numOfColumns;
            }
            // load
            float[] result = new float[d * n];
            //
            for (int i = 0; i < n * d; i++) //
            {
                result[i] = Float.parseFloat(reader.readLine());
            }
            reader.close();
            return result;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getShortName() {
        return "d_Dumb(" + fileName + ")";
    }
}
