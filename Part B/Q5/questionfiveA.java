import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import opennlp.tools.stemmer.PorterStemmer;
import java.util.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;

class Pair implements Comparable<Pair>{
    public String word;
    public int docfreq;
    public Pair(String w,int df){
        this.word = w;
        this.docfreq = df;
    } 
    @Override
    public int compareTo(Pair other){
        return this.docfreq - other.docfreq;
    }
    @Override
    public String toString(){
        return this.word + " " + this.docfreq;
    }
}
public class questionfiveA {
    public static class DFMapper extends Mapper<Object,Text,Text,IntWritable>{
        private Set <String> set_s = new HashSet<>();
        private PorterStemmer stemmer = new PorterStemmer();
        private Set <String> stopWords = new HashSet<>();
        @Override
        public void setup(Context context) throws IOException,InterruptedException{
            String line = null,trim_word = null;
            URI[] cachefiles = context.getCacheFiles();
            for(URI i : cachefiles){
                File file = new File(i);
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while((line = reader.readLine()) != null){
                    trim_word = line.trim().toLowerCase();
                    if(!trim_word.equals("")){
                        stopWords.add(trim_word);
                    }
                }
                reader.close();
            }
        }
        @Override
        public void map(Object key,Text value,Context context) throws IOException,InterruptedException {
            String line = value.toString();
            String[] tokens = line.split("[^\\w']+");
            final IntWritable one = new IntWritable(1);
            Text word = new Text();
            for(String token : tokens){
                String i = stemmer.stem(token).toLowerCase();
                if(!(set_s.contains(i)) && !(stopWords.contains(i)) && !(stopWords.contains(token)) && !i.equals("")){
                    set_s.add(i);
                    word.set(i);
                    context.write(word,one);
                }
            }
        }
    }
    public static class DFReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
        private PriorityQueue<Pair> pq = new PriorityQueue<>(100);
        @Override
        public void reduce(Text key,Iterable<IntWritable> values,Context context) throws IOException,InterruptedException{
            int sum = 0;
            for(IntWritable i : values){
                sum += i.get();
            }
            pq.add(new Pair(key.toString(), sum));
            if(pq.size() > 100){
                pq.poll();
            }
        }
        @Override
        public void cleanup(Context context) throws IOException,InterruptedException{
            for(Pair p : this.pq){
                context.write(new Text(p.word),new IntWritable(p.docfreq));
            }
        }
    }
    public static void main(String[] args) throws Exception{
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "docFreq");
        job.setMapperClass(DFMapper.class);
        job.setReducerClass(DFReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        File file = new File(args[2]);
        String absolutePath = file.getAbsoluteFile().toURI().toString();
        job.addCacheFile(new Path(absolutePath).toUri());
        FileInputFormat.addInputPath(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
