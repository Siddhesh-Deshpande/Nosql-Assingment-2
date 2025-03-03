import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.io.Writable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


class TWPair implements Writable {
    private long timestamp;
    private String word;

    public TWPair() {}

    public TWPair(long timestamp, String word) {
        this.timestamp = timestamp;
        this.word = word;
    }
    public long getTimestamp() { 
        return timestamp; 
    }
    public String getWord() { 
        return word; 
    }
    public void set(long timestamp, String word) {
        this.timestamp = timestamp;
        this.word = word;
    }
    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(timestamp);
        out.writeUTF(word);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        timestamp = in.readLong();
        word = in.readUTF();
    }

    @Override
    public String toString() {
        return timestamp + "\t" + word;
    }
}


public class thirdquestionpartA {
    
    public static class TimestampMapper extends Mapper<Object, Text, IntWritable, TWPair> {
        @Override
        public void map(Object key,Text value,Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] split_l = line.split("[ \t]+");
            if(split_l.length < 3){
                String[] new_spl = new String[3];
                for(int i = 0;i<3;i++){
                    new_spl[i] = (i > (split_l.length - 1)) ? ""  : split_l[i];
                }
                split_l = new_spl;
            }
            int index = Integer.parseInt(split_l[0]);
            int docId = Integer.parseInt(split_l[1]);
            IntWritable keyx = new IntWritable(index);
            TWPair val = new TWPair(convTimestamp(docId),split_l[2]);
            context.write(keyx,val);
        }
        private long convTimestamp(int id){
            return (long)id; 
        }
    }

    public static class TimestampReducer extends Reducer<IntWritable, TWPair, IntWritable, TWPair>{
        @Override
        public void reduce(IntWritable key,Iterable<TWPair> values,Context context) throws IOException, InterruptedException {
            long maxTime = Long.MIN_VALUE;
            String maxStr = "";
            for(TWPair i : values){
                if(i.getTimestamp() > maxTime){
                    maxTime = i.getTimestamp();
                    maxStr = i.getWord();
                }
            }
            TWPair val = new TWPair(maxTime,maxStr);
            context.write(key,val);
        }
    }
    public static void main(String[] args) throws Exception{
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf,"timestampRecent");
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(TWPair.class);

        job.setMapperClass(TimestampMapper.class);
        job.setReducerClass(TimestampReducer.class);

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(TWPair.class);
        FileInputFormat.addInputPath(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}