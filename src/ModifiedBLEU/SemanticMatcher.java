package ModifiedBLEU;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import java.util.Set;

import edu.smu.tspell.wordnet.*;



import Metrics.*;

public class SemanticMatcher {

	
	Map<Sequence, Integer> candidateCounts, refCounts;
	static Map<String, Set<String>> synMap;
	static WordNetDatabase database;
	Graph matchingGraph;
	Map <String, Map<String, Boolean>> isSyn;
	public static double time = 0;
	public static double timeGraph = 0;
	public static void init(String WORDNET_ADDRESS){

		System.setProperty("wordnet.database.dir", WORDNET_ADDRESS);
		database = WordNetDatabase.getFileInstance();
		synMap = new HashMap<String, Set<String>>();
	}
	public SemanticMatcher(){
		
		candidateCounts = new HashMap<Sequence, Integer>();
		refCounts = new HashMap<Sequence, Integer>();
		
		isSyn = new HashMap<String, Map<String,Boolean>>();
	}
	
	Set<String> getSyns(String s){
		Synset[] synset = database.getSynsets(s);
		Set<String> syns = new HashSet<String>();
		for(int i = 0; i < synset.length; i++){
			String [] wf = synset[i].getWordForms();
			for(int j = 0; j < wf.length; j++)
				syns.add(wf[j]);
		}
		syns.add(s);
		return syns;
	}
	
	public void addSyns2(Sequence words){
		
		for(String word: words){
			if(synMap.containsKey(word))
				continue;
			Set st = getSyns(word);
			//if(!st.isEmpty())
//			System.err.println(word + " , " + st);
			synMap.put(word, st);
		}
		
	}
	
	public void addSyns(Map<Sequence, Integer> cCount, Map<Sequence, Integer> rCount){
		Vector<Set> synmp = new Vector<Set>();
		Vector<String> cWords = new Vector<String>();
		for(Map.Entry<Sequence, Integer> cEntry: cCount.entrySet()){
			String word = cEntry.getKey().get(0);
			Set st;
			if(synMap.containsKey(word))
				st = synMap.get(word);
			else{ st = getSyns(word);
				synMap.put(word, st);
			}
			//if(!st.isEmpty())
//			System.err.println(word + " , " + st);
			cWords.add(word);
			synmp.add(st);
			isSyn.put(word, new HashMap<String, Boolean>());
		}
		int n = cCount.size(), m = rCount.size();
		for(Map.Entry<Sequence, Integer> rEntry: rCount.entrySet()){
			Set st;
			String word = rEntry.getKey().get(0);
			st = getStems(word);
			for(int i = 0; i < n; i++){
				Boolean match = false;
				for(Object x : st.toArray()){
					
					if(synmp.get(i).contains(x)){
						match = true;
						break;
					}
				}
				if(match){
					isSyn.get(cWords.get(i)).put(word, true);
				}
			}
			
		}
		
	}

	
	Set<String> getStems(String s){
		Set<String> res = new HashSet<String>();
		String [] wf;
		for(int i = 0; i < SynsetType.ALL_TYPES.length; i++){
			wf = database.getBaseFormCandidates(s, SynsetType.ALL_TYPES[i]);
			for(int j = 0; j < wf.length; j++)
				res.add(wf[j]);
		}
		res.add(s);
		return res;
	}
	
	public int matchSemantics(Map<Sequence, Integer> cCount, Map<Sequence, Integer> rCount, int order) {
		double t = System.currentTimeMillis();
		matchingGraph = new Graph(cCount.size() + rCount.size() + 2);
		int nodes = cCount.size() + rCount.size() + 2;
		int rcnt = cCount.size() + 1;
		for(Map.Entry<Sequence, Integer> rEntry: rCount.entrySet()){
			Sequence rWords = rEntry.getKey();
			matchingGraph.addEdge(rcnt, nodes - 1 , rEntry.getValue());
			
			int ccnt = 1;
			for(Map.Entry<Sequence, Integer> cEntry: cCount.entrySet()){
				Sequence cWords = cEntry.getKey();
				boolean match = true;
			
				for(int i = 0; i < order && match; i++){
					match = false;
					if(isSyn.get(cWords.get(i)).containsKey(rWords.get(i)))
						match = true;
				}
				if(match){
//					System.err.println(rWords + " , " + cWords);
					int count = Math.min(rEntry.getValue(), cEntry.getValue());
					matchingGraph.addEdge(ccnt, rcnt, count);
				}
				ccnt++;
			}
			rcnt++;
			
		}
		int ccnt = 1;
		for(Map.Entry<Sequence, Integer> cEntry: cCount.entrySet()){
			matchingGraph.addEdge(0, ccnt, cEntry.getValue());
			ccnt++;
		}
//		t = System.currentTimeMillis() - t;
//		time = Math.max(t / 1000., time);
//		t = System.currentTimeMillis();
		int res = matchingGraph.maximumMatching(0, nodes - 1);;
		
//		t = System.currentTimeMillis() - t;
//		timeGraph = Math.max(timeGraph, t);
		return res;
		
	}
//	
//	public int matchSemantics2(Map<Sequence, Integer> cCount, Map<Sequence, Integer> rCount, int order){
//		double t = System.currentTimeMillis();
//		matchingGraph = new Graph(cCount.size() + rCount.size() + 2);
//		int nodes = cCount.size() + rCount.size() + 2;
//		int matchCount = 0;
//		Set<String> wordForms[] = new HashSet[order] ;
//		int rcnt = cCount.size() + 1;
//		for(Map.Entry<Sequence, Integer> rEntry: rCount.entrySet()){
//			Sequence rWords = rEntry.getKey();
//			matchingGraph.addEdge(rcnt, nodes - 1 , rEntry.getValue());
//			for(int i = 0; i < rWords.size(); i++){
//				
//				wordForms[i] = getStems(rWords.get(i));
//				
//			}
//			int ccnt = 1;
//			for(Map.Entry<Sequence, Integer> cEntry: cCount.entrySet()){
//				Sequence cWords = cEntry.getKey();
//				boolean match = true;
//				for(int i = 0; i < order && match; i++){
//					match = false;
//					Set<String> syn = synMap.get(cWords.get(i));
//					for(Object wf: (wordForms[i].toArray())){
//						if(match)
//							break;
//						if(syn.contains(wf))
//							match = true;
//					}
//				}
//				if(match){
////					System.err.println(rWords + " , " + cWords);
//					int count = Math.min(rEntry.getValue(), cEntry.getValue());
//					matchingGraph.addEdge(ccnt, rcnt, count);
//				}
//				ccnt++;
//			}
//			rcnt++;
//			
//		}
//		int ccnt = 1;
//		for(Map.Entry<Sequence, Integer> cEntry: cCount.entrySet()){
//			matchingGraph.addEdge(0, ccnt, cEntry.getValue());
//			ccnt++;
//		}
//		t = System.currentTimeMillis() - t;
//		time = Math.max(t / 1000., time);
//		t = System.currentTimeMillis();
//		int res = matchingGraph.maximumMatching(0, nodes - 1);;
//		
//		t = System.currentTimeMillis() - t;
//		timeGraph = Math.max(timeGraph, t);
//		return res;
//		
//	}
	
	public static void main(String args[]){
		SemanticMatcher sm = new SemanticMatcher();
	}
}
