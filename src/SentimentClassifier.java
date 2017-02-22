import com.aliasi.classify.*;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.TrieCharSeqCounter;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

import java.io.*;
import java.util.Properties;

/**
 * Created by cioni on 22/01/17.
 */

public class SentimentClassifier {

    LMClassifier clss;
    PorterStemmerTokenizerFactory prt;
    String[] cats={"pos","neg"};

    private LMClassifier load(String path){
        try {
            clss= (LMClassifier) AbstractExternalizable.readObject(new File(path));
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return clss;
    }


    public SentimentClassifier(LMClassifier lmc){
        prt = new PorterStemmerTokenizerFactory(new IndoEuropeanTokenizerFactory());
        clss = lmc;
    }

    public SentimentClassifier() throws IOException{
        Properties prop = new Properties();
        PropertiesLoader prs=new PropertiesLoader();
        prt = new PorterStemmerTokenizerFactory(new IndoEuropeanTokenizerFactory());
        Classification cl;
        int nGram=3;


        try {
            prop.load(prs.getPropAsStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(prop.getProperty("train").equals("0")){
            clss=load(prop.get("pathToClassifier").toString());

        }else{
            DynamicLMClassifier cls=DynamicLMClassifier.createNGramProcess(cats,nGram);
            String[] paths= {prop.getProperty("trainPositiveFile"),prop.getProperty("trainNegativeFile")};
            train(cls,paths);
            clss=load(prop.getProperty("pathToClassifier"));
        }
    }

    public void testRun(String testPath)throws IOException{
        BufferedReader bf=new BufferedReader(new FileReader(testPath));
        String ln;
        while ((ln=bf.readLine())!=null){
            String fixed=stemProcess(ln.toLowerCase());
            String cat=clss.classify(fixed).bestCategory();
            System.out.println(fixed+"----"+cat);
        }
    }

    private String stemProcess(String s){
        String[] tmp=s.split(" ");
        String out="";
        for(String t:tmp){
            out=out+" "+prt.stem(t);
        }
        return out;
    }


    private void train(DynamicLMClassifier mClassifier,String[] paths)throws IOException{

            for (int i = 0; i < cats.length; ++i) {
                NGramProcessLM lm=(NGramProcessLM) mClassifier.languageModel(cats[i]);

                TrieCharSeqCounter con= lm.substringCounter();


                CsvReader reader= new CsvReader(paths[i]);//input files
                String category = cats[i];
                Classification classification = new Classification(category);
                String[] twt;
                while ((twt=reader.nextTweet())!=null){
                    String fixed=stemProcess(twt[1].toLowerCase());
                    Classified<CharSequence> classified = new Classified<CharSequence>(fixed,classification);
                        mClassifier.handle(classified);
                    }
                con.prune(2);
            }



        AbstractExternalizable.compileTo(mClassifier,new File("classifierLingPipe"));
    }


    public String classify(String text) {
        ConditionalClassification classification = clss.classify(stemProcess(text));

        return classification.bestCategory();
        // /return java.util.Arrays.asList(categories).indexOf(classification.bestCategory());
    }

    public static void main(String[] args)throws IOException{
        SentimentClassifier cl=new SentimentClassifier();
        cl.testRun("/home/cioni/git/sentimentw/testSet.txt");
    }
}
