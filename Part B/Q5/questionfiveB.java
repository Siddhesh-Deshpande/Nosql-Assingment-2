import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import opennlp.tools.stemmer.PorterStemmer;
import java.util.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.net.URI;

class TFPair implements Writable {
    private double tf;
    private String word;

    public TFPair() {}

    public TFPair(double tf, String word) {
        this.tf = tf;
        this.word = word;
    }
    public double gettf() { 
        return tf; 
    }
    public String getWord() { 
        return word; 
    }
    public void set(double tf, String word) {
        this.tf = tf;
        this.word = word;
    }
    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(word);
        out.writeDouble(tf);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        word = in.readUTF();
        tf = in.readDouble();
    }

    @Override
    public String toString() {
        return word + "\t" + tf;
    }
}
public class questionfiveB {
    public static class TFIDFMapper extends Mapper<Object,Text,IntWritable,MapWritable>{
        private PorterStemmer stemmer = new PorterStemmer();
        private IntWritable id = new IntWritable();
        private Set<String> top100 = new HashSet<>();
        private Map<String,Integer> freqmap = new HashMap<>();
        @Override
        public void setup(Context context) throws IOException,InterruptedException {
            FileSplit filesplit = (FileSplit) context.getInputSplit();
            String filename = filesplit.getPath().getName();
            filename = filename.substring(0,filename.length()-4);
            this.id.set(Integer.parseInt(filename));
            URI[] cachefiles = context.getCacheFiles();
            String line;
            for(URI i : cachefiles){
                File file = new File(i);
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while((line = reader.readLine()) != null){
                    String word = line.split("\t")[0];
                    this.top100.add(word);
                }
                reader.close();
            }
        }
        @Override
        public void map(Object key,Text value,Context context) throws IOException,InterruptedException{
            String line = value.toString();
            String[] tokens = line.split("[^\\w']+");
            for(String token : tokens){
                String i = stemmer.stem(token).toLowerCase();
                if(this.top100.contains(i)){
                    int prev_freq = this.freqmap.getOrDefault(i, 0);
                    this.freqmap.put(i, prev_freq + 1);
                }
            }
        }
        @Override
        public void cleanup(Context context) throws IOException,InterruptedException {
            MapWritable val = new MapWritable();
            for(Map.Entry<String,Integer> i : this.freqmap.entrySet()){
                val.put(new Text(i.getKey()),new IntWritable(i.getValue()));
            }
            context.write(this.id, val);
        }
    }
    public static class TFIDFReducer extends Reducer<IntWritable,MapWritable,IntWritable,TFPair>{
        private Map<String,Integer> dfMap = new HashMap<>();
        @Override
        public void setup(Context context) throws IOException,InterruptedException{
            URI[] cacheFiles = context.getCacheFiles();
            String line;
            for(URI i : cacheFiles){
                File file = new File(i);
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while((line = reader.readLine()) != null){
                    String[] p = line.split("\t");
                    this.dfMap.put(p[0], Integer.parseInt(p[1]));
                }
                reader.close();
            }
        }
        @Override
        public void reduce(IntWritable key,Iterable<MapWritable> values,Context context) throws IOException,InterruptedException {
            double n = 10000.0D;
            for(MapWritable i : values){
                for(Map.Entry<Writable,Writable> j : i.entrySet()){
                    Text word = (Text) j.getKey();
                    IntWritable tf = (IntWritable) j.getValue();
                    double tfidf = ((double)tf.get())*Math.log(n/((double)(1 + dfMap.get(word.toString()))));
                    context.write(key, new TFPair(tfidf,word.toString()));
                }
            }
        }
    }
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "tfidf");
        job.setMapperClass(TFIDFMapper.class);
        job.setReducerClass(TFIDFReducer.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(MapWritable.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(TFPair.class);
        File file = new File(args[2]);
        String absolutePath = file.getAbsoluteFile().toURI().toString();
        job.addCacheFile(new Path(absolutePath).toUri());
        FileInputFormat.addInputPath(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
    
}