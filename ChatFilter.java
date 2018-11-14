package chatapplication;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;


public class ChatFilter {

    private final String badWordsFileName;
    private ArrayList<String> badWords = new ArrayList<>();

    public ChatFilter(String badWordsFileName) {

        this.badWordsFileName = badWordsFileName;
        try {
            File f = new File(badWordsFileName);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            while (true) {
                String word = br.readLine();
                if (word == null) {
                    break;
                }
                badWords.add(word);

                fr.close();
                br.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String filter(String msg) {

        for (int i = 0; i < badWords.size(); i++) {

            if (msg.contains(badWords.get(i))) {
                String censoredBadWord = "";
                for (int j = 0; j < badWords.get(i).length(); j++) {
                    censoredBadWord += "*";

                }

                msg = msg.replaceAll(("(?i)" + badWords.get(i)), censoredBadWord);
            }

        }

        return msg;

    }


}
