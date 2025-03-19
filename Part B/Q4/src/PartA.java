import java.util.Collections;
import java.util.PriorityQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;


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

public class PartA {
	public static class Pair implements Comparable<Pair> {
		Text word;
		IntWritable count;

		public Pair(Text word, IntWritable count) {
			this.count = count;
			this.word = word;
		}

		@Override
		public int compareTo(Pair other) {
			return Integer.compare(this.count.get(), other.count.get()); // Extract integer value using get()
		}

		@Override
		public String toString() {
			return "{" + this.count + ", " + this.word + "}";
		}
	}

	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		private Set<String> patternsToSkip = new HashSet<>();

		@Override
		public void setup(Context context) throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
			URI[] patternsURIs = Job.getInstance(conf).getCacheFiles();
			if (patternsURIs != null) {
				for (URI patternsURI : patternsURIs) {
					Path patternsPath = new Path(patternsURI.getPath());
					parseSkipFile(patternsPath.toString());
				}
			}
		}

		private void parseSkipFile(String fileName) {
			try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
				String pattern;
				while ((pattern = reader.readLine()) != null) {
					if (!pattern.isEmpty()) {
						patternsToSkip.add(pattern.toLowerCase());
					}
				}
			} catch (IOException ioe) {
				System.err.println(
						"Caught exception while parsing the cached file: " + StringUtils.stringifyException(ioe));
			}
		}

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString().toLowerCase();
			String[] tokens = line.split("[^\\w']+");
			// String []tokens  = line.split("[^\\p{L}\\p{N}]+");
			for (String token : tokens) {
				token=token.trim();
				if (!patternsToSkip.contains(token)) {
					word.set(token);
					context.write(word, one);
				}
			}
		}
	}

	public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private PriorityQueue<Pair> pq = new PriorityQueue<>();//this is max heap
	
		public void reduce(Text key, Iterable<IntWritable> values, Context context) 
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
	
			// Store a new IntWritable instance
			Pair newPair = new Pair(new Text(key), new IntWritable(sum));
			// pq.add(newPair);
			if(pq.size()<50)
			{
				pq.add(newPair);
			}
			else
			{
				Pair temp = pq.peek();
				if(temp.count.get()<newPair.count.get())
				{
					pq.poll();
					pq.add(newPair);
				}
				
			}
		}

		@Override
		protected void cleanup(Context context) throws IOException,InterruptedException
		{
			int n = 50;
			while(n>0)
			{
				Pair temp = pq.poll();
				context.write(temp.word,temp.count);
				n--;
			}
		}
	}

	

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "wordcount2");

		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(IntSumReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.addCacheFile(new Path(args[0]).toUri());

		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
