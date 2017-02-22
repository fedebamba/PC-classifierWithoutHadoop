import com.aliasi.classify.LMClassifier;

import java.util.*;

/**
 * Created by Bamba on 21/02/2017.
 */
public class ClassificationTask implements Runnable{
    static ArrayList<String> tweets;
    static ArrayList<Integer> gtruth;
    static int numThreads;

    private SentimentClassifier sentimentClassifier;
    private int id;

    public ClassificationTask(LMClassifier lmc, int id){
        sentimentClassifier = new SentimentClassifier(lmc);
        this.id = id;
    }


    @Override public void run(){
        //filter
        //stem
        //lower
        //class
        for(int i = id; i < tweets.size(); i+=numThreads) {
            sentimentClassifier.classify(tweets.get(i));


        }


    }


}
