import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;


public class StepTwo {


    public static class Mapper2 extends Mapper<LongWritable, Text, Text, WritableLongPair> {

        //example (c$dror , NN:NN)
        private WritableLongPair count;
        private File pathsListCopy;

        /**
         * Setup a Mapper node. Copies the list of dependency paths from the S3 bucket to the local file system.
         *
         * @param context the Map-Reduce job context.
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void setup(Context context) throws IOException, InterruptedException {
            count = new WritableLongPair(0, 1);
            boolean local = context.getConfiguration().get("LOCAL_OR_EMR").equals("true");
            BufferedReader bufferedReader;
            if (local) {
                bufferedReader = new BufferedReader(new FileReader("resource/paths.txt"));
            } else {
                AmazonS3 s3 = new AmazonS3Client();
                Region usEast1 = Region.getRegion(Regions.US_EAST_1);
                s3.setRegion(usEast1);
                S3Object object = s3.getObject(new GetObjectRequest("dsps3maorrocky", "resource/paths.txt"));
                bufferedReader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
            }
            java.nio.file.Path path = Paths.get("resource");
            if (!Files.exists(path))
                Files.createDirectory(path);
            pathsListCopy = new File("resource/pathsListCopy.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathsListCopy));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line + "\n");
            }
            bufferedWriter.close();
            bufferedReader.close();
        }

        /**
         * Checks for a dependency path's index in the dependency paths file. Write to context the noun pair, the dependency
         * path's index, and a count of 1.
         *
         * @param key     a noun pair.
         * @param value   a dependency path.
         * @param context the Map-Reduce job context.
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            long index = 0;
            boolean found = false;
            String[] parts = value.toString().split("\\s"); //example [wai$dollar, NNS:TO:NNS]
            Scanner scanner = new Scanner(pathsListCopy);
            while (scanner.hasNextLine()) {
                if (parts[1].equals(scanner.nextLine())) {
                    found = true;
                    break;
                } else
                    index++;
            }
            if (found) {
                count.setL1(index);
                context.write(new Text(parts[0]), count); //example woman$mother	4 1
            }
            scanner.close();
        }

    }

    public static class Reducer2 extends Reducer<Text, WritableLongPair, Text, Text> {

        private HashMap<String, Boolean> testSet;
        private final String BUCKET = "dsps3maorrocky";
        private final String HYPERNYM_LIST = "resource/hypernym.txt";
        private final String NUM_OF_FEATURES_FILE = "resource/numOfFeatures.txt";
        private long numOfFeatures;
        private Stemmer stemmer;
        private AmazonS3 s3;

        /**
         * Setup a Reducer node.
         *
         * @param context the Map-Reduce job context.
         * @throws IOException
         */
        @Override
        public void setup(Context context) throws IOException {
            stemmer = new Stemmer();
            boolean local = context.getConfiguration().get("LOCAL_OR_EMR").equals("true");
            Scanner scanner;
            BufferedReader bufferedReader_hypernymList;
            if (local) {
                scanner = new Scanner(new FileReader(NUM_OF_FEATURES_FILE));
                bufferedReader_hypernymList = new BufferedReader(new FileReader(HYPERNYM_LIST));
            } else {
                s3 = new AmazonS3Client();
                Region usEast1 = Region.getRegion(Regions.US_EAST_1);
                s3.setRegion(usEast1);
                System.out.print("Downloading no. of features file from S3... ");
                S3Object object = s3.getObject(new GetObjectRequest(BUCKET, NUM_OF_FEATURES_FILE));
                System.out.println("Done");
                scanner = new Scanner(new InputStreamReader(object.getObjectContent()));
                object = s3.getObject(new GetObjectRequest(BUCKET, HYPERNYM_LIST));
                bufferedReader_hypernymList = new BufferedReader(new InputStreamReader(object.getObjectContent()));
            }
            numOfFeatures = scanner.nextInt();
            System.out.println("Number of features: " + numOfFeatures);
            scanner.close();
            testSet = new HashMap<>();
            String line;
            while ((line = bufferedReader_hypernymList.readLine()) != null) {
                String[] pieces = line.split("\\s");
                stemmer.add(pieces[0].toCharArray(), pieces[0].length());
                stemmer.stem();
                pieces[0] = stemmer.toString();
                stemmer.add(pieces[1].toCharArray(), pieces[1].length());
                stemmer.stem();
                pieces[1] = stemmer.toString();
                testSet.put(pieces[0] + "$" + pieces[1], pieces[2].equals("True"));
            }
            bufferedReader_hypernymList.close();
        }

        /**
         * @param key     a noun pair.
         * @param counts  a list of WritableLongPair, each one being an index of a dependency path and a count of its
         *                appearances, respectively.
         * @param context the Map-Reduce job context.
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void reduce(Text key, Iterable<WritableLongPair> counts, Context context) throws IOException, InterruptedException {
            String keyAsString = key.toString();
            if (testSet.containsKey(keyAsString)) {
                long[] featuresVector = new long[(int) numOfFeatures];
                for (WritableLongPair count : counts) {
                    featuresVector[(int) count.getL1()] += count.getL2();
                }
                StringBuilder sb = new StringBuilder();
                for (long index : featuresVector)
                    sb.append(index).append(",");
                sb.append(testSet.get(keyAsString));
                context.write(key, new Text(sb.toString()));
            }
        }

    }


    public static void main(String[] args) throws Exception {
        if (args.length != 3)
            throw new IOException("Phase 2: supply 3 arguments");

        Configuration conf = new Configuration();
        conf.set("LOCAL_OR_EMR", String.valueOf(args[2].equals("local")));
        if (args[2].equals("local")) {
            System.out.println("deleting "+args[1]+" directory");
            deleteDirectory(new File("/home/maor/Desktop/dsp3/output_step2"));
        }
        Job job = Job.getInstance(conf, "Phase 2");
        job.setJarByClass(StepTwo.class);
        job.setMapperClass(Mapper2.class);
        job.setReducerClass(Reducer2.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(WritableLongPair.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.out.println("Step 2 - input path: " + args[0] + ", output path: " + args[1]);
        if (job.waitForCompletion(true))
            System.out.println("Step 2: job completed successfully");
        else
            System.out.println("Step 2: job completed unsuccessfully");
        Counter counter = job.getCounters().findCounter("org.apache.hadoop.mapreduce.TaskCounter", "REDUCE_INPUT_RECORDS");
        System.out.println("Number of key-value pairs sent to reducers in step 2: " + counter.getValue());
    }

    static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}
