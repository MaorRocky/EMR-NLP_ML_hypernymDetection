/*
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.*;


public class Main {
    public static void main(String[] args) {

        if (!args[1].equals("local") && !args[1].equals("emr")) {
            System.err.println("Usage: java HDetector <DPmin> [local | emr]");
            System.exit(1);
        }

        if (args[1].equals("local")) {
            // Local machine, single node setup. Used in order to debug the M-R logic.
            String[] p1args = {"input", "intermediate", args[0], "local"};
            String[] p2args = {"intermediate", "output", "local"};
            StepOne.main(p1args);
            StepTwo.main(p2args);
            String[] ppargs = {args[1]};
            PostProcessor.main(ppargs);
        } else {
            // EMR setup. This is the main intent of this app.
            AWSCredentials credentials = null;
            try {
                credentials = new ProfileCredentialsProvider().getCredentials();
            } catch (Exception e) {
                throw new AmazonClientException(
                        "Cannot load the credentials from the credential profiles file. " +
                                "Please make sure that your credentials file is at the correct " +
                                "location (~/.aws/credentials), and is in valid format.",
                        e);
            }

            AmazonElasticMapReduce mapReduce = new AmazonElasticMapReduceClient(credentials);

            HadoopJarStepConfig jarStep1 = new HadoopJarStepConfig()
                    .withJar("s3n://dsps162assignment3benasaf/jars/HDetector.jar")
                    .withMainClass("Phase1")
                    .withArgs("s3n://dsps162assignment3benasaf/input2", "hdfs:///intermediate/", args[0], "emr");

            StepConfig step1Config = new StepConfig()
                    .withName("Phase 1")
                    .withHadoopJarStep(jarStep1)
                    .withActionOnFailure("TERMINATE_JOB_FLOW");

            HadoopJarStepConfig jarStep2 = new HadoopJarStepConfig()
                    .withJar("s3n://dsps162assignment3benasaf/jars/HDetector.jar")
                    .withMainClass("Phase2")
                    .withArgs("hdfs:///intermediate/", "s3n://dsps162assignment3benasaf/output_single_corpus", "emr");

            StepConfig step2Config = new StepConfig()
                    .withName("Phase 2")
                    .withHadoopJarStep(jarStep2)
                    .withActionOnFailure("TERMINATE_JOB_FLOW");

            JobFlowInstancesConfig instances = new JobFlowInstancesConfig()
                    .withInstanceCount(5)
                    .withMasterInstanceType(InstanceType.M1Medium.toString())
                    .withSlaveInstanceType(InstanceType.M1Medium.toString())
                    .withHadoopVersion("2.7.2")
                    .withEc2KeyName("AWS")
                    .withKeepJobFlowAliveWhenNoSteps(false)
                    .withPlacement(new PlacementType("us-east-1a"));

            RunJobFlowRequest runFlowRequest = new RunJobFlowRequest()
                    .withName("extract-hypernyms")
                    .withInstances(instances)
                    .withSteps(step1Config, step2Config)
                    .withJobFlowRole("EMR_EC2_DefaultRole")
                    .withServiceRole("EMR_DefaultRole")
                    .withReleaseLabel("emr-4.7.0")
                    .withLogUri("s3n://dsps162assignment3benasaf/logs/")
                    .withBootstrapActions();

            System.out.println("Submitting the JobFlow Request to Amazon EMR and running it...");
            RunJobFlowResult runJobFlowResult = mapReduce.runJobFlow(runFlowRequest);
            String jobFlowId = runJobFlowResult.getJobFlowId();
            System.out.println("Ran job flow with id: " + jobFlowId);
        }
    }
}
*/
