import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Debug;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class ClassifierTester {

//    private static final String HEADER1 = "classifier_input25";
    private static final String HEADER1 = "classifier_input";
//    private static final String HEADER2 = "classifier_output25";
    private static final String HEADER2 = "classifier_output25";

    public static void main(String[] args) throws Exception {
        java.nio.file.Path path = Paths.get(HEADER1);
        if (!Files.exists(path))
            Files.createDirectory(path);
        path = Paths.get(HEADER2);
        if (!Files.exists(path))
            Files.createDirectory(path);

        // cross-validate
        Instances taggedSet = DataSource.read(HEADER1 + "/processed_single_corpus.arff");
        taggedSet.setClassIndex(taggedSet.numAttributes() - 1);
        Evaluation crossValidation = new Evaluation(taggedSet);
        Classifier tree = new J48();
        crossValidation.crossValidateModel(tree, taggedSet, 10, new Debug.Random(1));
        System.out.println(crossValidation.toSummaryString("\nCross validation - Results\n\n", false));
        System.out.println(crossValidation.toClassDetailsString("\nCross validation - Statistics\n\n"));

        // train and test
        tree.buildClassifier(taggedSet);
        Instances testInput = DataSource.read(HEADER1 + "/processed_single_corpus.arff");
        testInput.setClassIndex(taggedSet.numAttributes() - 1);
        Instances classifiedSet = new Instances(testInput);
        for (int i = 0; i < testInput.numInstances(); i++) {
            double clsLabel = tree.classifyInstance(testInput.instance(i));
            classifiedSet.instance(i).setClassValue(clsLabel);
        }
        ConverterUtils.DataSink.write(HEADER2 + "/classified_set.arff", classifiedSet);

        classifiedSet = DataSource.read(HEADER2 + "/classified_set.arff");

        if (classifiedSet.size() != taggedSet.size())
            throw new Exception("Training set and tagged set differ in number of entries.");

        HashMap<String, String> tp = new HashMap<>(10); // true positives
        HashMap<String, String> fp = new HashMap<>(10); // false positives
        HashMap<String, String> tn = new HashMap<>(10); // true negatives
        HashMap<String, String> fn = new HashMap<>(10); // false negatives
        BufferedReader br = new BufferedReader(new FileReader(HEADER1 + "/processed_single_corpus_with_words.arff"));
        String line;
        while (!br.readLine().contains("@DATA")) { // skip all of the arff file header
        }
        line = br.readLine();
        for (int i = 0; i < taggedSet.size(); i++, line = br.readLine()) {
            String trainSetEntry = taggedSet.get(i).toString();
            String testSetEntry = classifiedSet.get(i).toString();
            boolean trainTruthValue = trainSetEntry.substring(trainSetEntry.lastIndexOf(",") + 1).equals("true");
            boolean testTruthValue = testSetEntry.substring(testSetEntry.lastIndexOf(",") + 1).equals("true");
            String nounPair = line.substring(0, line.indexOf("\t"));
            String vector = line.substring(line.indexOf("\t") + 1);
            if (trainTruthValue && testTruthValue && tp.size() < 10)
                tp.put(nounPair, vector);
            else if (!trainTruthValue && testTruthValue && fp.size() < 10)
                fp.put(nounPair, vector);
            else if (!trainTruthValue && !testTruthValue && tn.size() < 10)
                tn.put(nounPair, vector);
            else if (trainTruthValue && !testTruthValue && fn.size() < 10)
                fn.put(nounPair, vector);
        }

        System.out.println("\n\nTrue positives:");
        for (Map.Entry<String, String> entry : tp.entrySet())
            System.out.println(entry.getKey() + "\n" + entry.getValue());
        System.out.println("\n\nFalse positives:");
        for (Map.Entry<String, String> entry : fp.entrySet())
            System.out.println(entry.getKey() + "\n" + entry.getValue());
        System.out.println("\n\nTrue negatives:");
        for (Map.Entry<String, String> entry : tn.entrySet())
            System.out.println(entry.getKey() + "\n" + entry.getValue());
        System.out.println("\n\nFalse negatives:");
        for (Map.Entry<String, String> entry : fn.entrySet())
            System.out.println(entry.getKey() + "\n" + entry.getValue());


    }

}
