package chatapplication;
import java.io.*;
import java.util.ArrayList;
/**
 * Project 04 -- Simple Chat Server
 *
 * This program allows for connection of a server and clients, and messaging.
 *
 * @authors Brian Norton, Briana Crowe lab sec 015
 *
 * @version November 26, 2018
 *
 */

public class ChatFilter {

    private final String badWordsFileName;

    public ChatFilter(String badWordsFileName) {
        this.badWordsFileName = badWordsFileName;

    }

    public String filter(String msg) {
        String casemsg = "";
        for(int i = 0; i < msg.length(); i++)
        {

            if (Character.isLetter(msg.charAt(i)))
            {
               String x = String.valueOf(msg.charAt(i)).toLowerCase();
                casemsg += x;
            }
            else
            {
               String x = String.valueOf(msg.charAt(i));
                casemsg += x;
            }

        }

        ArrayList<String> badWords = new ArrayList<>();

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

            }
            fr.close();
            br.close();

        } catch (Exception e) {
            System.out.println("File not found.");
        }

        for (int i = 0; i < badWords.size(); i++) {

            if (casemsg.contains(badWords.get(i))) {
                String censoredBadWord = "";
                for (int j = 0; j < badWords.get(i).length(); j++) {
                    censoredBadWord += "*";

                }

                String finallowermsg = casemsg.replaceAll(badWords.get(i), censoredBadWord);
                String finalcorrectedmsg = "";
                for (int j = 0; j < finallowermsg.length(); j++)
                {
                    if(finallowermsg.charAt(j) != '*')
                    {
                        finalcorrectedmsg += msg.charAt(j);
                    }
                    else
                    {
                        finalcorrectedmsg += "*";
                    }
                }
                msg = finalcorrectedmsg;


            }

        }

        return msg;

    }


}