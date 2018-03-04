import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mashiru on 2/26/18.
 */
public class Experiment {
    protected boolean baseline;
    protected String TrialType;
    protected String TrailDetail;
    protected int standardGSize;
    protected int standardTopK;
    protected int standardDims;
    protected int standardNOPt;



    public Experiment(String type, boolean baseline) {
        this.baseline = baseline;
        TrialType = type;
        assert TrialType == "GP" || TrialType == "GG";
        if (TrialType == "GP") TrailDetail = "Group-Point";
        else TrailDetail = "Group-Group";
    }

    public List<Double> argumentsTrial(int gSize, int topK, int dimensions, int numOfPoints, String dir, String spliter) {
        System.out.println("\n********Experiment for group size "+gSize+", topK "+topK+", dimensions "+dimensions+", num of points "+numOfPoints+"********\n");
        List<Double> results = new ArrayList<>();
        String fileName = dir+"largeTestData_d"+dimensions+"_1e"+numOfPoints; // e.g. largeTestData_d2_1e5
        /* test for testData
        String fileName = "../data/testData";
        gSize = 2; topK = 4; dimensions = 2; numOfPoints = 13;
        */

        File file = new File(dir, fileName);
        if (!file.exists()) Data.generate(fileName, dimensions, numOfPoints, true);

        TopKGPSkyline test;
        if (TrialType == "GP") test = new TopKGPSkyline(gSize, topK);
        else test = new TopKGGSkyline(gSize, topK);
        System.out.println("Loading data!");
        List<Integer[]> data = Data.readData(fileName, spliter);

        // create layers
        // twoD or higherD for computing layers
        System.out.println("Creating graph...");
        long cStartT = System.nanoTime();
        SkGraph graph = test.createLayerGraph(data);// build the graph
        long cEndT = System.nanoTime();
        long creatGraphTime = cEndT - cStartT;
        results.add(creatGraphTime / Math.pow(10, 9));
        System.out.println("Creating Graph                  Time: " + creatGraphTime / Math.pow(10, 9) + "s\n"); // nano second convert to second


        boolean silent = true;
        SkGraph graphBaseline = graph;
        SkGraph graphTopk = graph;

        if (!baseline) {
            // TopK
            System.out.println(TrailDetail + " Skyline is working...");
            long start2 = System.nanoTime();
            List<SkGroup> topKGroups;
            if (TrialType == "GP") topKGroups = test.getTopKGroups(graphTopk, false, silent);
            else topKGroups = test.getTopKGroups(graphTopk, true, silent);

            long end2 = System.nanoTime();
            long calculation2 = end2 - start2;
            long timeSumTopK = creatGraphTime + calculation2;
            results.add(calculation2 / Math.pow(10, 9));
            results.add(timeSumTopK / Math.pow(10, 9));
            System.out.println("TopK " + TrailDetail + " Skyline calculation Time: " + calculation2 / Math.pow(10, 9) + "s");
            System.out.println("TopK " + TrailDetail + " Skyline total       Time: " + timeSumTopK / Math.pow(10, 9) + "s\n");
        } else {

            // Baseline
            System.out.println("Baseline is working...");
            long start1 = System.nanoTime();
            List<SkGroup> baselineGroups;
            if (TrialType == "GP") baselineGroups = test.getTopKGroups(graphBaseline, true, silent);
            else baselineGroups = test.getTopKGroups(graphBaseline, false, silent);

            long end1 = System.nanoTime();
            long calculation1 = end1 - start1;
            long timeSumBaseline = creatGraphTime + calculation1;
            results.add(calculation1 / Math.pow(10, 9));
            results.add(timeSumBaseline / Math.pow(10, 9));
            System.out.println("Baseline " + TrailDetail + " calculation Time: " + calculation1 / Math.pow(10, 9) + "s"); // nano second convert to second
            System.out.println("Baseline " + TrailDetail + " total       Time: " + timeSumBaseline / Math.pow(10, 9) + "s\n"); // nano second convert to second
        }

        return results;
    }

    public void setStandardParams(int stdGSize, int stdTopK, int stdDims, int stdNOPt) {
        standardGSize = stdGSize;
        standardTopK = stdTopK;
        standardDims = stdDims;
        standardNOPt = stdNOPt;
    }

    public void saveTrialResults(String type, int[] variables, String dir, String spliter, String resultsFileName) {
        try {
            File file = new File(resultsFileName);
            file.delete();
            if (!file.exists()) file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            List<Double> results;
            for (int var: variables) {
                String line = "" + var;
                switch (type) {
                    case "GS" : // fix topK = 3, dims = 3, numOfPts = 1e4
                        results = argumentsTrial(var, standardTopK, standardDims, standardNOPt, dir, spliter); break;
                    case "K" :  // fix gSize = 5, dims = 3, numOfPts = 1e4
                        results = argumentsTrial(standardGSize, var, standardDims, standardNOPt, dir, spliter); break;
                    case "D": // fix gSize = 5, topK = 3, numOfPts = 1e4
                        results = argumentsTrial(standardGSize, standardTopK, var, standardNOPt, dir, spliter); break;
                    case "PT": // fix gSize = 5, topK = 3, dims = 3
                        results = argumentsTrial(standardGSize, standardTopK, standardDims, var, dir, spliter); break;
                    default:
                        results = new ArrayList<>();
                }
                for (double result: results)
                    line += (spliter+result);
                line += "\n";
                writer.flush();
                writer.write(line);
            }
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
