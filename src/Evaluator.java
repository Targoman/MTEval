

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Metrics.*;
import ModifiedBLEU.*;



public class Evaluator {

	/**
	 * @param args
	 */
	

	static String WORDNET_ADDRESS = "D:\\Program Files (x86)\\WordNet\\2.1\\dict";

	BufferedReader candidatesFile;
	BufferedReader referencesFile[];
	int normMethod = 1;
	boolean caseSensitive = true;
	boolean noPunctuation = false;
	String metricName = "bleu";
	EvaluationMetric evMetric;

	public FileOutputStream fos;
	public DataOutputStream dos;
	
	
	public void setOutputFile(String address){
		try {
			fos = new FileOutputStream(address);

			dos = new DataOutputStream(fos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void setOutputStream(DataOutputStream d){
		dos = d;
	}
	public void setCaseSensitive(boolean b) {
		caseSensitive = b;
	}

	public void setPunc(boolean b) {
		noPunctuation = b;
	}

	public void setNormMethod(int m) {
		normMethod = m;
	}

	public Evaluator(int numOfRefs, String cFile, String rFiles[])
			throws FileNotFoundException, UnsupportedEncodingException {
		referencesFile = new BufferedReader[numOfRefs];
		candidatesFile = new BufferedReader(new InputStreamReader(new FileInputStream(cFile), "UTF8"));
		
		for (int i = 0; i < numOfRefs; i++) {
			referencesFile[i] = new BufferedReader(new InputStreamReader(new FileInputStream(rFiles[i]), "UTF8"));
		}
	}

//	candidateFile numOfReferences ref1 ... refn bleu/ter args [-p -c]
	
	public Evaluator(String args[]) throws FileNotFoundException, Exception {
		int argNum = args.length;
		candidatesFile = new BufferedReader(new FileReader(args[0]));
		int numOfRefs = new Integer(args[1]);
		referencesFile = new BufferedReader[numOfRefs];
		for (int i = 0; i < numOfRefs; i++) {
			referencesFile[i] = new BufferedReader(new FileReader(args[2 + i]));
		}
		int k = 2 + numOfRefs;
		metricName = args[k++];
		if (metricName.equals("BLEU")) {
			int n = new Integer(args[k++]);
			evMetric = new BLEUMetric(n);
		} else if (metricName.equals("TER")) {

			int bw = new Integer(args[k++]);
			int shift_dis = new Integer(args[k++]);
			evMetric = new TERMetric(bw, shift_dis);
		} else if (metricName.equals("PER")){
			evMetric = new PERMetric();
		} else if (metricName.equals("WER")){
			evMetric = new WERMetric();
		}
		else if (metricName.equals("newMetric")){
		

			int n = new Integer(args[k++]);
			evMetric = new ModifiedBLEU(n, WORDNET_ADDRESS);
		}else{
			System.err.println("Invalid Metric!");
			throw (new Exception());
		}
		
		while (k < argNum) {
			if (args[k].equals("-p"))
				noPunctuation = true;
			else if (args[k].equals("-c"))
				caseSensitive = true;
			k++;
		}
	}

	
	public static Sequence tokenize(String s) {
		List<String> l = Arrays.asList(s.split("\\s+"));
		return new Sequence(l);
	}

	private static String removePunctuation(String str) {
		String s = str.replaceAll("[\\.,\\?:;!\"\\(\\)]", "");
		s = s.replaceAll("\\s+", " ");
		return s;
	}
	


	public Sequence readCandidate() throws IOException {
		String cnd = candidatesFile.readLine();
		
		if(!caseSensitive)
			cnd = cnd.toLowerCase();
		if(noPunctuation)
			cnd = removePunctuation(cnd);
		return tokenize(cnd);
	}

	public List<Sequence> readReferences() throws IOException {
		List<Sequence> refs = new ArrayList<Sequence>();
		int n = referencesFile.length;
		for (int i = 0; i < n; i++) {
			String s = referencesFile[i].readLine();

			if(!caseSensitive)
				s = s.toLowerCase();
			if(noPunctuation)
				s = removePunctuation(s);
			
			refs.add(tokenize(s));
		}
		return refs;
	}

	public boolean hasMoreCandidate() throws IOException {
		int t = 0;
		for (int i = 0; i < referencesFile.length; i++) {
			if (referencesFile[i].ready())
				t++;
		}
		if (candidatesFile.ready() && t == referencesFile.length)
			return true;
		if (candidatesFile.ready()) {
			System.err.println("candidateFile is larger than referencesFiles");
			System.exit(1);
		}
		if (t == referencesFile.length) {
			System.err.println("candidateFile is smaller than referencesFiles");
			System.exit(1);
		}
		return false;

	}

	

	public double calcScore() {
		int k = 0;
		try {
			while (hasMoreCandidate()) {
				Sequence candidate = readCandidate();
				List<Sequence> references = readReferences();
				dos.writeBytes(k++ + "   " + evMetric.addSentence(candidate, references) + "\n");
				
//				System.out.println(k++);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return evMetric.getScore();
	}

	// candidateFile numOfReferences ref1 ... refn BLEU/TER/PER/WER args [-p -c]
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
			Evaluator evaluator = new Evaluator(args);
			evaluator.setOutputStream(new DataOutputStream(System.err));
			System.out.println(evaluator.metricName + ":     " + evaluator.calcScore());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}

// 1 newstest2011\cz-en\newstest2011.de-en.systran newstest2011-ref.en
// newstest2011\de-en\newstest2011.de-en.rbmt-2 1 newstest2011-ref.en
// newstest2011\fr-en\newstest2011.fr-en.cu-zeman 1 newstest2011-ref.en
// newstest2011\de-en\newstest2011.de-en.cu-zeman 1 newstest2011-ref.en
// 1 test.tok.systran ref.tok.en
