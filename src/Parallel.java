import com.aliasi.classify.LMClassifier;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Bamba on 21/02/2017.
 */
public class Parallel {
    private static ConcurrentHashMap<String, AtomicInteger> result = new ConcurrentHashMap<String, AtomicInteger>();
    private static String[] topic = {
            "london",
            "paris",
            "moscow",
            "madrid",
            "rome"
    };

    private static class CT implements Runnable {
        private static String[] topic;
        private static ConcurrentHashMap<String, AtomicInteger> result;

        private String tweet;
        private LMClassifier clss;
        private CountDownLatch cdl;
        private PorterStemmerTokenizerFactory prt = new PorterStemmerTokenizerFactory(new IndoEuropeanTokenizerFactory());

        CT(String t, LMClassifier lmc, CountDownLatch cdl){
            tweet = t;
            clss = lmc;
            this.cdl = cdl;
        }


        @Override
        public void run() {
            if(filter(tweet)){
                tweet = stem(tweet.toLowerCase());
                result.get(clss.classify(tweet).bestCategory()).addAndGet(1);
            }
            cdl.countDown();
        }


        private String stem(String s){
            String[] tmp=s.split(" ");
            String out="";
            for(String t:tmp){
                out=out+" "+prt.stem(t);
            }
            return out;
        }

        private boolean filter(String tweet){
            ArrayList<String> arr = new ArrayList<String>(Arrays.asList(tweet.toLowerCase().replace("\n", " ").split(" ")));
            for (String t : topic){
                if(arr.contains(t)){
                    return true;
                }
            }
            return false;
            //return new ArrayList<String>(Arrays.asList(tweet.toLowerCase().replace("\n", " ").split(" "))).contains(topic);
        }

    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        for(int i = 0; i < 10; i++){
            __main(args);
        }
    }

    public static void __main(String[] args) throws IOException, ClassNotFoundException {
        ExecutorService exe = Executors.newFixedThreadPool(8);
        CountDownLatch cdl = new CountDownLatch(1600000);

        CsvReader csvr = new CsvReader("data\\train\\trainingdata.csv");
        LMClassifier clss= (LMClassifier) AbstractExternalizable.readObject(new File("C:\\Users\\Bamba\\Documents\\GitHub\\sentana\\data\\classifier\\classifierLingPipe"));

        Parallel.result.put("pos", new AtomicInteger(0));
        Parallel.result.put("neg", new AtomicInteger(0));

        CT.topic = Parallel.topic;
        CT.result = Parallel.result;

        String[] a;
        long start = System.nanoTime();
        while((a = csvr.nextTweet()) != null){
            exe.execute(new CT(a[1], clss, cdl));
        }
        try{
            cdl.await();
        } catch (InterruptedException e) {

        }
        System.out.println((double)(System.nanoTime() - start)/1000000000);
        //System.out.println(result.toString());
        result = new ConcurrentHashMap<String, AtomicInteger>();
        exe.shutdown();
    }

}
