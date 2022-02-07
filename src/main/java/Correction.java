import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Correction {
    public HashMap<String, Integer> term_cnt;
    public HashMap<String, Integer> bi_cnt;
    public ArrayList<Character> alphabet;
    public HashMap<String,HashMap<String,Double>> spell_error;
    public Integer total;
    public Correction() throws IOException {
        // File file = new File("term_count.json");

        File file1 = new File("D:\\Documents\\javaproj\\src\\main\\java\\term_count.json");
        String term_count = FileUtils.readFileToString(file1, "UTF-8");
        term_cnt = JSON.parseObject(term_count, HashMap.class);

        File file2 = new File("D:\\Documents\\javaproj\\src\\main\\java\\bigram_count.json");
        String bigram_count = FileUtils.readFileToString(file2, "UTF-8");
        bi_cnt = JSON.parseObject(term_count, HashMap.class);

        //http://norvig.com/ngrams/spell-errors.txt
        File file3 = new File("D:\\Documents\\javaproj\\src\\main\\java\\spell-errors.txt");
        List<String> spelling = FileUtils.readLines(file3);
        for (String i: spelling
             ) {
            String correct = i.split(":")[0];
            String[] error = i.split(":")[1].split(",");
            Double prob = 1.0 / error.length;
            HashMap<String,Double> tmp = new HashMap<>();
            for (String j: error
                 ) {
                tmp.put(j,prob);
            }
            spell_error.put(correct,tmp);
        }


        char a = 'a';
        for (int i = 0; i < 26; i++) {
            alphabet.add(a);
            a++;
        }

        total = term_count.length();

    }

    static class Pair<T> {
        T first;
        T second;

        public Pair(T first, T second) {
            this.first = first;
            this.second = second;
        }
    }

    // edit distance: insert, delete, replace
    public HashSet<String> editDis1(String word) {
        HashSet<String> res = new HashSet<String>();
        ArrayList<Pair<String>> split = new ArrayList<Pair<String>>();

        // "abc"=> ('',abc),(a,bc),(ab,c),(abc,'')
        for (int i = 0; i < word.length(); i++) {
            split.add(new Pair<String>(word.substring(0, i), word.substring(i)));
        }

        // Transpose
        for (Pair<String> i : split
        ) {
            if (i.second.length() > 1)
                // ignore words not exist
                if (term_cnt.containsKey(i.first + i.second.charAt(1) + i.second.charAt(0) + i.second.substring(1)))
                    res.add(i.first + i.second.charAt(1) + i.second.charAt(0) + i.second.substring(1));
        }

        // insert,replace
        for (Pair<String> i : split
        ) {
            // add 26 alphabets
            for (char j: alphabet){
                //insert
                if (term_cnt.containsKey(i.first+j+i.second))
                    res.add(i.first+j+i.second);
                // replace
                if (i.second.length()>0)
                    if (term_cnt.containsKey(i.first+j+i.second.substring(1)))
                        res.add(i.first+j+i.second.substring(1));
            }
        }

        //delete
        for (Pair<String> i : split
        ) {
            if (i.second.length() > 0)
                if (term_cnt.containsKey(i.first + i.second.substring(1)))
                    res.add(i.first + i.second.substring(1));
        }

        return res;
    }

/*
p(correct|mistake) = p(correct)*p(mistake|correct)
                   = log p(correct) + log p(mistake|correct)

p(correct) : probability that type error, can get by the spell-error corpus
p(correct) = spell_error.get(correct_word).get(wrong_word)
if not exist, p = log(0.0001)

p(mistake|correct): can be calculated by unigram/bigram/..., use bigram here
p = log(p(current|former))+log(p(latter|current))
p(current|former) = count(former current)/count(former)
smoothing:        = count(...+1)/count(...+N)
N is number of words in corpus
*/
    public String calculate(HashSet<String> candidates,String mistake,Integer index,ArrayList<String> query){
        String res = mistake;
        double max_prob = 0.0;
        for (String candidate: candidates
             ) {
            double cur_prob=0.0;
            if (spell_error.get(candidate)!=null && spell_error.get(candidate).get(mistake)!=null){
                cur_prob+=Math.log(spell_error.get(candidate).get(mistake));
            }
            else
                // not in common spelling error, get a small weight
                cur_prob+=Math.log(0.001);

            if (index>0) {
                String previous = query.get(index - 1)+ " " + candidate;
                if (bi_cnt.get(previous)!=null && term_cnt.get(query.get(index-1))!=null)
                    cur_prob+=Math.log((bi_cnt.get(previous)+1.0)/(term_cnt.get(query.get(index-1)+total)));
                else
                    cur_prob+=Math.log(1.0/total);
            }
            if (index<query.size()-1){
                String latter = candidate+" "+ query.get(index+1);
                if (bi_cnt.get(latter)!=null && term_cnt.get(mistake)!=null)
                    cur_prob=Math.log((bi_cnt.get(latter)+1.0)/(term_cnt.get(mistake)+total));
                else
                    cur_prob+=Math.log(1.0/total);
            }
            if (cur_prob>max_prob)
                max_prob = cur_prob;
                res = candidate;
        }
        return res;
    }


}
