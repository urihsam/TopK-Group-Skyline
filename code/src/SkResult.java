import java.util.List;

/**
 * Created by mashiru on 3/5/18.
 */
public class SkResult { // TODO: the class structure design can be improved. e.g. add toString into the SkXXX classes
    protected List<Double> timeCosts;
    protected List<List<List<Double>>> topKResults;

    public void setTimeCosts(List<Double> values) { timeCosts = values; }

    public void setTopKResults(List<List<List<Double>>> values) {topKResults = values; }

    public List<Double> getTimeCosts() { return timeCosts; }

    public List<List<List<Double>>> getTopKResults() { return topKResults; }
}
