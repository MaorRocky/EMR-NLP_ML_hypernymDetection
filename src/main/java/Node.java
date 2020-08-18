import java.util.LinkedList;
import java.util.List;


class Node {

    private String stemmedWord;

    public String getWord() {
        return word;
    }

    private String word;
    private String pos_tag; // part of speech
    private String dep_label; // stanford dependency
    private int father;
    private List<Node> children;

    public Node(String[] args, Stemmer stemmer) {
        stemmer.add(args[0].toCharArray(), args[0].length());
        stemmer.stem();
        this.word = args[0];
        this.stemmedWord = stemmer.toString();
        this.pos_tag = args[1];
        this.dep_label = args[2];
        try {
            this.father = Integer.parseInt(args[3]);

        } catch (Exception e) {
        }
        children = new LinkedList<>();
    }

    String getStemmedWord() {
        return stemmedWord;
    }

    void addChild(Node child) {
        children.add(child);
    }

    int getFather() {
        return father;
    }

    String getDepencdencyPathComponent() {
        return pos_tag;
    }

    boolean isNoun() {
        return pos_tag.equals("NN") || pos_tag.equals("NNS") || pos_tag.equals("NNP") || pos_tag.equals("NNPS");
    }

    List<Node> getChildren() {
        return children;
    }


    public void toPrint() {
        System.out.print("Node{" +
                "stemmedWord='" + stemmedWord + '\'' +
                ", word='" + word + '\'' +
                ", pos_tag='" + pos_tag + '\'' +
                ", dep_label='" + dep_label + '\'' +
                ", father=" + father +
                ", children=" + children +
                '}' + "\t");
    }

    @Override
    public String toString() {
        return "Node{" +
                "stemmedWord='" + stemmedWord + '\'' +
                ", word='" + word + '\'' +
                ", pos_tag='" + pos_tag + '\'' +
                ", dep_label='" + dep_label + '\'' +
                ", father=" + father +
                ", children=" + children +
                '}'+"\n";
    }
}
