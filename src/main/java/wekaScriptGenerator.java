import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;


public class wekaScriptGenerator {

    private static final String PREFIX1 = "@RELATION nounpair\n\n";
    private static final String PREFIX2 = "@RELATION nounpair\n\n@ATTRIBUTE nounPair STRING\n";
    private static final String POSTFIX = "@ATTRIBUTE ans {true, false}\n\n@DATA\n";
    private static final String BUCKET_NAME = "dsps3maorrocky";

    public static void main(String[] args) throws IOException {
        if (!args[0].equals("local") && !args[0].equals("emr")) {
            System.err.println("Usage: java wekaScriptGenerator <DPmin> [local | emr]");
            System.exit(1);
        }
        java.nio.file.Path path = Paths.get("classifier_input");
        if (!Files.exists(path))
            Files.createDirectory(path);
        boolean local = args[0].equals("local");
        BufferedReader br1, br2;
        BufferedWriter bw1, bw2, bwcopy = null;
        if (local) {
            br1 = new BufferedReader(new FileReader("output_step2/part-r-00000"));
            br2 = new BufferedReader(new FileReader("resource/numOfFeatures.txt"));
        } else {
            AmazonS3 s3 = new AmazonS3Client();
            Region usEast1 = Region.getRegion(Regions.US_EAST_1);
            s3.setRegion(usEast1);
            S3Object object1 = s3.getObject(new GetObjectRequest(BUCKET_NAME, "output_single_corpus/part-r-00000"));
            br1 = new BufferedReader(new InputStreamReader(object1.getObjectContent()));
            S3Object object2 = s3.getObject(new GetObjectRequest(BUCKET_NAME, "resource/numOfFeatures.txt"));
            br2 = new BufferedReader(new InputStreamReader(object2.getObjectContent()));
            path = Paths.get("output");
            if (!Files.exists(path))
                Files.createDirectory(path);
            /*
            Copy the M-R output to the local file system. This prevents a second read in ClassifierTester.
             */
            bwcopy = new BufferedWriter(new FileWriter("output/part-r-00000"));
        }
        bw1 = new BufferedWriter(new FileWriter(new File("classifier_input/processed_single_corpus.arff")));
        bw2 = new BufferedWriter(new FileWriter(new File("classifier_input/processed_single_corpus_with_words.arff")));
        int vectorLength = Integer.parseInt(br2.readLine());
        System.out.println("Number of features: " + vectorLength);
        String line;
        bw1.write(PREFIX1);
        bw2.write(PREFIX2);
        for (int i = 0; i < vectorLength; i++) {
            bw1.write("@ATTRIBUTE p" + i + " REAL\n");
            bw2.write("@ATTRIBUTE p" + i + " REAL\n");
        }
        bw1.write(POSTFIX);
        bw2.write(POSTFIX);
        // populate the arff files with actual data
        while ((line = br1.readLine()) != null) {
            if (!local)
                bwcopy.write(line);
            bw1.write(line.substring(line.indexOf("\t") + 1) + "\n");
            bw2.write(line + "\n");
        }
        br1.close();
        br2.close();
        bw1.close();
        bw2.close();
        if (!local)
            bwcopy.close();
    }

}
