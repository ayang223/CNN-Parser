package com.company;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.opencsv.CSVReader;

public class Main {
    private static  String readAll(Reader rd) throws IOException{
        StringBuilder sb = new StringBuilder();
        int cp;
        while((cp=rd.read())!=-1){
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public static String[] add(String[] arr, String item ) {
        String [] temp = Arrays.copyOf(arr,arr.length+1);
        temp[temp.length-1] = item;
        return temp;
    }

    public static String getName(){
        LocalDate currentDate = LocalDate.now();
        int day = currentDate.getDayOfMonth();
        Month month = currentDate.getMonth();
        int year = currentDate.getYear();
        String date = "top_headlines_" + month + "_" + day + "_" + year;
        return "./" + date + ".csv";
    }


    public static void main(String[] args) throws IOException, JSONException {
        // gets the json file
        JSONObject json = readJsonFromUrl("https://newsapi.org/v1/articles?source=cnn&sortBy=top&apiKey=2a4639109b424bd3970e2fdf00fa54de");
        // get the json array of articles
        JSONArray arr = json.getJSONArray("articles");
        // check if csv exists
        Path p = Paths.get(getName());
        boolean exists = Files.exists(p);
        boolean notExists = Files.notExists(p);
        if (exists) {
            System.out.println("File exists! Checking if there are new articles");
            // check for new articles by checking urls
            CSVReader reader = new CSVReader(new FileReader(getName()));
            String [] nextLine;
            String [] strArr = new String[0];
            while ((nextLine = reader.readNext()) != null) {
                strArr = add(strArr, nextLine[3]);
            }
            List<String> list = Arrays.asList(strArr);
            // compare urls of csv to new get request of api to see if new article
            for(int i =0; i<arr.length(); i++) {
                if(list.contains(arr.getJSONObject(i).get("url").toString())){
                    continue;
                } else {
                    System.out.println("new article found, adding to file");
                    FileWriter mFileWriter = new FileWriter(getName(), true);
                    mFileWriter.append("\n");
                    mFileWriter.append(json.get("source").toString());
                    mFileWriter.append(",");
                    mFileWriter.append(arr.getJSONObject(i).get("author").toString().replaceAll(",",""));
                    mFileWriter.append(",");
                    mFileWriter.append(arr.getJSONObject(i).get("title").toString());
                    mFileWriter.append(",");
                    mFileWriter.append(arr.getJSONObject(i).get("url").toString());
                    mFileWriter.append(",");
                    mFileWriter.append(arr.getJSONObject(i).get("publishedAt").toString());
                    mFileWriter.close();
                }
            } System.out.println("No new articles to add");
        } else if (notExists) {
            // if doesn't exist create file with data
            System.out.println("File doesn't exist, creating it");
            File file = new File(getName());
            FileWriter outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile);
            String[] header = { "Source", "Author", "Title", "URL", "publishedAt" };
            writer.writeNext(header);
            for(int i =0; i<arr.length(); i++){
                String[] data = {
                        json.get("source").toString(),
                        arr.getJSONObject(i).get("author").toString(),
                        arr.getJSONObject(i).get("title").toString(),
                        arr.getJSONObject(i).get("url").toString(),
                        arr.getJSONObject(i).get("publishedAt").toString(),
                };
                writer.writeNext(data);
            }
            writer.close();
            System.out.println("CSV is created");
        } else {
            System.out.println("File's status is unknown!");
        }
    }
}
