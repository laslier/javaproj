
import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SpellCorrection {
    public static HashSet<String> keyword = new HashSet<>();

    static {
        keyword.add("AND");
        keyword.add("NOT");
        keyword.add("(");
        keyword.add(")");
        keyword.add("OR");
    }

    public static void main(String[] args) throws IOException {
        Correction corr = new Correction();
        String qry = "NOT I AND like AND NOT ( eating ) AND applo AND every AND day OR eating AND pineapples";
        String[] qrylist = qry.split(" ");
        ArrayList<String> thiskey = new ArrayList<>();
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < qrylist.length; i++) {
            if (keyword.contains(qrylist[i])) {
                if (s.length() != 0) {
                    s.append(" ");
                    s.append(qrylist[i]);
                } else
                    s.append(qrylist[i]);
            } else {
                if (s.length() != 0) {
                    thiskey.add(s.toString());
                    s.setLength(0);
                }
            }
        }

        String[] handle = qry.replaceAll("AND|NOT|\\(|\\)", "").replaceAll(" +", " ").split("OR");
        StringBuilder res = new StringBuilder();
        int cur = 0;
        if (keyword.contains(qrylist[0])){
            res.append(thiskey.get(cur));
            cur++;
        }

        for (String query : handle
        ) {
            String[] to_handle = query.strip().toLowerCase().split(" ");
            for (int i = 0; i < to_handle.length; i++
            ) {

                // error
                if (!(Correction.term_cnt.containsKey(to_handle[i]))) {
                    // edit distance 1
                    HashSet<String> candi = corr.editDis1(to_handle[i]);
                    HashSet<String> candi2 = new HashSet<>(candi);
                    // edit distance 2
                    for (String j : candi
                    ) {
                        candi2.addAll(corr.editDis1(j));

                    }
                    res.append(" ").append(corr.calculate(candi2, to_handle[i], i, to_handle));
                } else
                    res.append(" ").append(to_handle[i]);
                if (cur<thiskey.size()){
                    res.append(" ").append(thiskey.get(cur));
                    cur++;
                }
            }
        }


        System.out.println(res);
    }

}