import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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
    protected double standardScal;
    protected int standardNOLayer;


    public Experiment(String type, boolean baseline) {
        this.baseline = baseline;
        TrialType = type;
        assert TrialType == "GP" || TrialType == "GG";
        if (TrialType == "GP") TrailDetail = "Group-Point";
        else TrailDetail = "Group-Group";
    }

    protected List<List<List<Double>>> prepareResults(List<SkGroup> skGroups) {
        // print timeResults
        List<List<List<Double>>> results = new ArrayList<>();
        for (SkGroup group: skGroups) { // for certain group
            List<List<Double>> gResults = new ArrayList<>();
            for (SkNode node: group.getGroupNodes()) // for certain node
                gResults.add(new ArrayList<>(Arrays.asList(node.getVal()))); // add node's data to group
            double dominates = TrialType=="GG"? group.getSizeOfDominatedGroups():group.getSizeOfDominatedNodes();
            List<Double> dominatesResult = new ArrayList<>();
            dominatesResult.add(dominates);
            gResults.add(dominatesResult);
            results.add(gResults); // add group result to the topK
        }
        return results;
    }

    public SkResult argumentsTrial(int gSize, int topK, int dimensions, int numOfPoints, String dir, String spliter) {
        return argumentsTrial(gSize, topK, dimensions, numOfPoints, 1, dir, spliter); // use all layers
    }

    public SkResult argumentsTrial(int gSize, int topK, int dimensions, int numOfPoints, double scale,
                                   String dir, String spliter) {
        System.out.println("\n********Experiment for group size "+gSize+", topK "+topK+", dimensions "+dimensions+", num of points "+numOfPoints+"********\n");
        System.out.println("\n********scale "+scale+"********\n");
        SkResult skResult = new SkResult();
        List<Double> timeResults = new ArrayList<>();
        int postCount = -1;
        boolean silent = true;
        boolean smallerPref = true;
        boolean skyband;
        String fileName = dir+"largeTestData_d"+dimensions+"_"+scale+"e"+numOfPoints; // e.g. largeTestData_d2_1e5

        // nba data
        fileName = dir+"nba.csv";
        postCount = 5;
        spliter = ",";
        smallerPref = false;
        skyband = true;

        /*//test for testData
        silent = false;
        fileName = "../data/testData";
        gSize = 2; topK = 5;
        //smallerPref = false;*/

        File file = new File(dir, fileName);
        if (!file.exists()) Data.generate(fileName, dimensions, numOfPoints, scale, true);

        TopKGPSkyline test;
        if (TrialType == "GP") test = new TopKGPSkyline(gSize, topK, smallerPref);
        else test = new TopKGGSkyline(gSize, topK, smallerPref);
        System.out.println("Loading data!");
        List<Double[]> data = Data.readData(fileName, spliter, postCount);

        // create layers
        // twoD or higherD for computing layers
        System.out.println("Creating graph...");
        long cStartT = System.nanoTime();
        SkGraph graph;
        if (TrialType == "GP") graph= test.createLayerGraph(data);// build the graph
        // if (TrialType == "GP") graph= test.createLayerGraph(data, gSize, false);// build the graph
        else graph= test.createLayerGraph(data, gSize, skyband);// build the graph, only keep first group size layers and using skyband
        long cEndT = System.nanoTime();
        long creatGraphTime = cEndT - cStartT;
        timeResults.add(creatGraphTime / Math.pow(10, 9));
        System.out.println("Creating Graph                  Time: " + creatGraphTime / Math.pow(10, 9) + "s\n"); // nano second convert to second

        SkGraph graphBaseline = graph;
        SkGraph graphTopk = graph;

        if (!baseline) {
            // TopK
            System.out.println(TrailDetail + " Skyline is working...");
            long start2 = System.nanoTime();
            List<SkGroup> topKGroups;
            if (TrialType == "GP") topKGroups = test.getTopKGroups(graphTopk, false, silent); // Group-point
            else topKGroups = test.getTopKGroups(graphTopk, true, silent); // Group-group

            long end2 = System.nanoTime();
            long calculation2 = end2 - start2;
            long timeSumTopK = creatGraphTime + calculation2;
            timeResults.add(calculation2 / Math.pow(10, 9));
            timeResults.add(timeSumTopK / Math.pow(10, 9));
            System.out.println("TopK " + TrailDetail + " Skyline calculation Time: " + calculation2 / Math.pow(10, 9) + "s");
            System.out.println("TopK " + TrailDetail + " Skyline total       Time: " + timeSumTopK / Math.pow(10, 9) + "s\n");
            if (TrialType == "GG")
                System.out.println("TopK " + TrailDetail + " number of universe groups: "+ ((TopKGGSkyline)test).getNumOfUniverseGroups());

            skResult.setTopKResults(prepareResults(topKGroups));
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
            timeResults.add(calculation1 / Math.pow(10, 9));
            timeResults.add(timeSumBaseline / Math.pow(10, 9));
            System.out.println("Baseline " + TrailDetail + " calculation Time: " + calculation1 / Math.pow(10, 9) + "s"); // nano second convert to second
            System.out.println("Baseline " + TrailDetail + " total       Time: " + timeSumBaseline / Math.pow(10, 9) + "s\n"); // nano second convert to second

            skResult.setTopKResults(prepareResults(baselineGroups));
        }
        skResult.setTimeCosts(timeResults);

        return skResult;
    }

    public void setStandardParams(int stdGSize, int stdTopK, int stdDims, int stdNOPt, double stdScal) {
        standardGSize = stdGSize;
        standardTopK = stdTopK;
        standardDims = stdDims;
        standardNOPt = stdNOPt;
        standardScal = stdScal;
    }

    public void saveTrialResults(String type, int[] variables, String dir, String spliter, String resultsFileName) {
        try {
            File file = new File(resultsFileName);
            file.delete();
            if (!file.exists()) file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            SkResult skResult;
            for (int var: variables) {
                String line = "" + var;
                switch (type) {
                    case "GS" : // fix topK = 3, dims = 3, numOfPts = 1e4
                        skResult = argumentsTrial(var, standardTopK, standardDims, standardNOPt, standardScal, dir, spliter); break;
                    case "K" :  // fix gSize = 5, dims = 3, numOfPts = 1e4
                        skResult = argumentsTrial(standardGSize, var, standardDims, standardNOPt, standardScal, dir, spliter); break;
                    case "D": // fix gSize = 5, topK = 3, numOfPts = 1e4
                        skResult = argumentsTrial(standardGSize, standardTopK, var, standardNOPt, standardScal, dir, spliter); break;
                    case "PT": // fix gSize = 5, topK = 3, dims = 3
                        skResult = argumentsTrial(standardGSize, standardTopK, standardDims, var, standardScal, dir, spliter); break;
                    default:
                        skResult = new SkResult();
                }
                // add time
                for (double result: skResult.getTimeCosts())
                    line += (spliter+result);
                line += "\nTop k groups\n";
                int count = 1;
                for (List<List<Double>> group: skResult.getTopKResults()) {
                    line += ("Group " + (count++) + "\n");
                    for (List<Double> node : group) {
                        for (double data : node)
                            line +=  (data + spliter);
                        line += "\n";
                    }
                    line += "\n";
                }
                writer.flush();
                writer.write(line);
            }
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
