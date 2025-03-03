import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.io.WritableComparable;
import java.io.DataOutput;
import java.io.DataInput;

public class prob_two {
    public static class mypair implements WritableComparable<mypair> {
                private IntWritable index;
                private Text word;
            
                public mypair() {
                    this.index = new IntWritable();
                    this.word = new Text();
                }
            
                public mypair(IntWritable index, Text word) {
                    this.index = index;
                    this.word = word;
                }
                public IntWritable getindex()
                {
                    return this.index;
                }
                public Text getword()
                {
                    return this.word;
                }
                // @Override
                 public void write(DataOutput out) throws IOException {
                            index.write(out);
                            word.write(out);
                    }
                    
                //  @Override
                    public void readFields(DataInput in) throws IOException {
                            index.readFields(in);
                            word.readFields(in);
                    }
                    
                //  @Override
                    public int compareTo(mypair o) {
                            return this.index.compareTo(o.index);
                    }
                    
                @Override
                        public String toString() {
                            return index + "\t" + word;
                }
        }
    public static class TokenizerMapper extends Mapper<Object,Text,IntWritable,mypair>
    {
        private IntWritable file_id = new IntWritable();
        int wordindex=0;
        private Text word = new Text();
        @Override
        protected void setup(Context context) throws IOException , InterruptedException
        {
            FileSplit filesplit = (FileSplit) context.getInputSplit();
            String filename = filesplit.getPath().getName();
            filename = filename.substring(0,filename.length()-4);
            file_id.set(Integer.parseInt(filename));
        }
        @Override
        public void map(Object key,Text value,Context context)  throws IOException,InterruptedException
        {
            String line = value.toString();
            String []tokens = line.split("[^\\w']+");
            for(String token :tokens)
            {
                word.set(token);
                mypair p = new mypair(new IntWritable(wordindex), new Text(word));
                context.write(file_id,p);
                wordindex++;
            }
        }
    }
    public static  class CustomReducer extends Reducer<IntWritable,mypair,IntWritable,mypair>
    {
        
        @Override
        public void reduce(IntWritable key,Iterable<mypair> value,Context context) throws IOException, InterruptedException
        {
            for(mypair temp :value)
            {
                IntWritable index = temp.getindex();
                Text word = temp.getword();
                mypair v = new mypair(key,new Text(word));
                context.write(index,v);
            }
        }
    }
    public static void main(String[] args)throws Exception
    {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf,"wordcount");
        job.setJarByClass(prob_two.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setReducerClass(CustomReducer.class);

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(mypair.class);
        job.setNumReduceTasks(10);
        FileInputFormat.addInputPath(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job,new Path (args[1]));
        System.exit(job.waitForCompletion(true) ? 0:1);
    }
}