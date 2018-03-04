import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by mashiru on 2/25/18.
 */
public class Data {
    public static List<Integer[]> readData(String fileName, String spliter) {
        List<Integer[]> data = new ArrayList<Integer[]>();
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String sline;
            while ((sline = reader.readLine()) != null)
            {
                String[] s = sline.split(spliter); // spliter = "  "

                Integer[] line = new Integer[s.length];
                for (int i = 0; i < s.length; i++)
                    line[i] = Integer.parseInt(s[i].trim());
                data.add(line);
            }
            reader.close();
            return data;
        }
        catch (Exception e)
        {
            System.err.format("Exception occurred trying to read '%s'.", fileName);
            e.printStackTrace();
            return null;
        }
    }

    public static void generate(String fileName, int dimensions, int numOfPoints, boolean forceCreate) {// This will reference one line at a time
        File file;
        numOfPoints = (int)Math.pow(10, numOfPoints);
        try {
            file = new File(fileName);
            if (forceCreate) file.delete();
            if (!file.exists()) file.createNewFile();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            Random random = new Random();
            int bound = numOfPoints*5*dimensions;
            for (int pIdx=0; pIdx<numOfPoints; pIdx++) {
                String line = "" + random.nextInt(bound);
                for (int dIdx=1; dIdx<dimensions; dIdx++)
                    line += ("  " + random.nextInt(bound));
                line += "\n";
                writer.write(line);
            }
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        String dir = "../data/";
        String fileName = dir+args[0]; // filename
        int dimensions = Integer.parseInt(args[1]); // dimension
        int numOfPoints = Integer.parseInt(args[2]); // num of points in exponent
        boolean forceCreate = Boolean.parseBoolean(args[3]); // forceCreate
        // e.g.
        // largeTestData 2 10000 true

        Data.generate(fileName, dimensions, numOfPoints, forceCreate);
        System.out.println("Data generated!");
    }

}
