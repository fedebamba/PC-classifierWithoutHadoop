import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.LMClassifier;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Bamba on 21/02/2017.
 */
public class Sequential {
    private static String topic = "rome";
    private static int poss = 0;
    private static int negs = 0;
    //private static int positives = 0;

    private static PorterStemmerTokenizerFactory prt = new PorterStemmerTokenizerFactory(new IndoEuropeanTokenizerFactory());

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        for(int i = 0; i<10; i++){
            __main(args);
        }
    }

    public static void __main(String[] args) throws IOException, ClassNotFoundException {
        CsvReader csvr = new CsvReader("data\\train\\trainingdata.csv");
        LMClassifier clss= (LMClassifier) AbstractExternalizable.readObject(new File("C:\\Users\\Bamba\\Documents\\GitHub\\sentana\\data\\classifier\\classifierLingPipe"));

        String[] a;
        long start = System.nanoTime();

        while((a = csvr.nextTweet()) != null){
            if(filter(a[1])){
      //          positives++;
                String t = stem(a[1].toLowerCase());
                clss.classify(t).bestCategory().equals(Integer.parseInt(a[0].replace("\"", "")) == 4 ? "pos" : "neg");
                if(clss.classify((t)).bestCategory().equals("pos")){
                    poss++;
                }else{
                    negs++;
                }
            }
        }
        System.out.println((double)(System.nanoTime() - start)/1000000000);
        //System.out.println(positives);
        //positives=0;

    }

    private static String stem(String s){
        String[] tmp=s.split(" ");
        String out="";
        for(String t:tmp){
            out=out+" "+prt.stem(t);
        }
        return out;
    }

    private static boolean filter(String tweet){
        return new ArrayList<String>(Arrays.asList(tweet.toLowerCase().replace("\n", " ").split(" "))).contains(topic);
    }

}
