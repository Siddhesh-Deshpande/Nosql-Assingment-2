import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.PriorityQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

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
import org.apache.hadoop.io.WritableComparable;

public class PartB {
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

        // public int compareTo(mypair o) {
        //     return this.word1.compareTo(o.word1);
        // }
        public int compareTo(mypair o) {
            int cmp = this.word1.compareTo(o.word1);
            if (cmp == 0) {
                return this.word2.compareTo(o.word2);
            }
            return cmp;
        }

        @Override
        public String toString() {
            return word1 + "\t" + word2;
        }
    }
    public static class TokenizerMapper extends Mapper<Object, Text, mypair, IntWritable>
    {
        private Set<String> words = new HashSet<>();
        @Override
        public void setup(Context context) throws IOException, InterruptedException
        {
            Configuration conf = context.getConfiguration();
            URI[] files = Job.getInstance(conf).getCacheFiles();
            if(files!=null)
            {
                for(URI file:files)
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
                    // System.out.println(pattern.split("\t")[0]);
                }
            }catch (IOException ioe) {
				System.err.println(
						"Caught exception while parsing the cached file: " + StringUtils.stringifyException(ioe));
			}

        }
        @Override
        public void map(Object key,Text value,Context context)throws IOException, InterruptedException
        {
            String line = value.toString().toLowerCase();
            String tokens[] = line.split("[^\\w']+");
            int distance = 4;// Can be changed to 2,3,4 
            IntWritable one = new IntWritable(1);
            for(int i=0;i<tokens.length;i++)
            {
                // String  token=tokens[i].trim();
                Text word1=new Text();
                Text word2=new Text();
                word1.set(tokens[i]);
                if(words.contains(tokens[i]))
                {
                    for(int j=i;j<=Math.min(i+distance,tokens.length-1);j++)
                    {
                        if(words.contains(tokens[j]))
                        {
                            
                            word2.set(tokens[j]);
                            mypair temp = new mypair(word1,word2);
                            context.write(temp,one);
                        }
                    }
                }
            }
        }
    }
    public static class IntSumReducer extends Reducer<mypair,IntWritable,mypair,IntWritable>
    {
        private IntWritable count = new IntWritable();
        public void reduce(mypair key ,Iterable<IntWritable> values,Context context) throws IOException,InterruptedException
        {
            int sum = 0;
            for(IntWritable val:values)
            {
                sum+=val.get();
            }
            count.set(sum);
            context.write(key,count);
        }
    }
    public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "wordcount2");

		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(IntSumReducer.class);

		job.setOutputKeyClass(mypair.class);
		job.setOutputValueClass(IntWritable.class);

		job.addCacheFile(new Path(args[0]).toUri());

		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
    
}