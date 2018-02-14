package Metrics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class createXML_TER {

	  private static void writeLine(String line, BufferedWriter writer) throws IOException
	  {
	    writer.write(line, 0, line.length());
	    writer.newLine();
	    writer.flush();
	  }
	 public static void createXML(String hyp, String ref)
	  {
	    // calculate sufficient statistics for each sentence in an arbitrary set of candidates

		 
//	    int candCount = cand_strings.length;
	   
	    try {

	      // 1) Create input files for tercom

	      // 1a) Create hypothesis file
	      FileOutputStream outStream = new FileOutputStream("hyp.txt.TER", false); // false: don't append
	      OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, "utf8");
	      BufferedWriter outFile = new BufferedWriter(outStreamWriter);

	      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(hyp), "utf8"));
	     // for (int d = 0; d < candCount; ++d) {
	      int d = 0;
	      while(br.ready()){
	        writeLine(br.readLine() + " (ID" + d + ")",outFile);
	        d++;
	      }

	      outFile.close();

	      
	      // 1b) Create reference file
	      br = new BufferedReader(new InputStreamReader(new FileInputStream(ref), "utf8"));
	      outStream = new FileOutputStream("ref.txt.TER", false); // false: don't append
	      outStreamWriter = new OutputStreamWriter(outStream, "utf8");
	      outFile = new BufferedWriter(outStreamWriter);

	      d = 0;
	      while (br.ready()){
	          writeLine(br.readLine() + " (ID" + d + ")",outFile);
	          d++;
	      }

	      outFile.close();
	    }catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	  }
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		createXML(args[0], args[1]);
		
	}

}
