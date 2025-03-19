import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import javax.naming.Context;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.MapWritable;

public class PartC {
    public static class mypair implements WritableComparable<mypair>
    {
        private Text word1;
        private Text word2;

        public mypair() {
            this.word1 = new Text();
            this.word2 = new Text();
        }

        public mypair(Text word1, Text word2) {
            this.word1 = word1;
            this.word2 = word2;
        }

        public Text getword1() {
            return this.word1;
        }

        public Text getword2() {
            return this.word2;
        }

        public void write(DataOutput out) throws IOException {
            word1.write(out);
            word2.write(out);
        }

        public void readFields(DataInput in) throws IOException {
            word1.readFields(in);
            word2.readFields(in);
        }

        public int compareTo(mypair o) {
            return this.word1.compareTo(o.word1);
        }
        // public int compareTo(mypair o) {
        //     int cmp = this.word1.compareTo(o.word1);
        //     if (cmp == 0) {
        //         return this.word2.compareTo(o.word2);
        //     }
        //     return cmp;
        // }

        @Override
        public String toString() {
            return word1 + "\t" + word2;
        }
    }

    public static class TokenizerMapper extends Mapper<Object, Text, Text, MapWritable>
    {
        private Set<String> words = new HashSet<>();

        @Override
        public void setup(Context context) throws IOException, InterruptedException
        {
            Configuration conf = context.getConfiguration();
            URI[] files = Job.getInstance(conf).getCacheFiles();
            if(files != null)
            {
                for(URI file: files)
                {
                    Path filepath = new Path(file.getPath());
                    parseSkipFile(filepath.toString());
                }
            }
        }

        private void parseSkipFile(String fileName)
        {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String pattern;
                while ((pattern = reader.readLine()) != null)
                {
                    words.add(pattern.split("\t")[0].toLowerCase());
                }
            } catch (IOException ioe) {
                System.err.println("Caught exception while parsing the cached file: " + StringUtils.stringifyException(ioe));
            }
        }    

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException
        {
            String line = value.toString().toLowerCase();
            String tokens[] = line.split("[^\\w']+");
            int distance = 4;
            Text word1 = new Text();
            for(int i = 0; i < tokens.length; i++)
            {
                if(words.contains(tokens[i]))
                {
                    MapWritable h = new MapWritable();
                    for(int j = i; j <= Math.min(i + distance, tokens.length - 1); j++)
                    {
                        if(words.contains(tokens[j]))
                        {
                            Text token = new Text(tokens[j]);
                            IntWritable cnt = (IntWritable) h.get(token); // Explicit cast
                            // System.out.println(cnt.get());
                            if(cnt != null)
                            {
                                h.put(token, new IntWritable(cnt.get() + 1));
                            }
                            else
                            {
                                h.put(token, new IntWritable(1));
                            }
                        }
                    }
                    word1.set(tokens[i]);
                    context.write(word1, h);
                }
            }
        }
    }

    public static class IntSumReducer extends Reducer<Text, MapWritable, mypair, IntWritable>
    {
        private IntWritable count = new IntWritable();
        private Text word = new Text();

        public void reduce(Text key, Iterable<MapWritable> values, Context context) throws IOException, InterruptedException
        {
            MapWritable h = new MapWritable();//map specific for this key

            word.set(key);

            for(MapWritable maps : values)
            {
                for (Map.Entry<Writable, Writable> entry : maps.entrySet()) 
                {
                    Text cur_key = (Text) entry.getKey();  // Explicit cast
                    IntWritable cur_val = (IntWritable) entry.getValue(); // Explicit cast

                    if(h.containsKey(cur_key))
                    {
                        IntWritable old_val = (IntWritable) h.get(cur_key); // Explicit cast
                        h.put(cur_key, new IntWritable(old_val.get() + cur_val.get()));
                    }
                    else
                    {
                        h.put(cur_key, new IntWritable(cur_val.get()));
                    }
                }
            }

            for(Map.Entry<Writable, Writable> entry : h.entrySet()) // Corrected entry type
            {
                Text neigh = (Text) entry.getKey();  // Explicit cast
                IntWritable value = (IntWritable) entry.getValue();  // Explicit cast

                mypair temp = new mypair(word, neigh);
                count.set(value.get());
                context.write(temp, count);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "wordcount2");

        job.setMapperClass(TokenizerMapper.class);
        job.setReducerClass(IntSumReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(MapWritable.class);

        job.setOutputKeyClass(mypair.class);
        job.setOutputValueClass(IntWritable.class);

        job.addCacheFile(new Path(args[0]).toUri());

        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
