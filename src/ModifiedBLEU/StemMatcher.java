package ModifiedBLEU;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import Metrics.*;

import Tools.*;

public class StemMatcher {

	HashMap<String, String> stems;
	Stemmer stemmer;
	public StemMatcher(){
		stems = new HashMap<String, String>();
		stemmer = new Stemmer();
	}
	
	public void addWords(Sequence s){
		for (int i = 0; i < s.size(); i++) {
			String word = s.get(i);
			if(stems.containsKey(word))
				continue;
			stemmer.add(word.toCharArray(), word.length());
			stemmer.stem();
			stems.put(word, stemmer.toString());
//			System.out.print(stemmer.toString() + " ");
		}
//		System.out.println();
	}
	
	
	public int matchStems(Map<Sequence, Integer> candidateCounts, Map<Sequence, Integer> maxRefCounts, int order){
		int count = 0;
		Map<Sequence, Integer> candidStemCount = new HashMap<Sequence, Integer>();
		Map<Sequence, Integer> refStemCount = new HashMap<Sequence, Integer>();
		Map<Sequence, Vector<Sequence> > candidStemToWords = new HashMap<Sequence, Vector<Sequence> >();
		Map<Sequence, Vector<Sequence> > refStemToWords = new HashMap<Sequence, Vector<Sequence> >();
		for(Map.Entry<Sequence, Integer> mapEntry : candidateCounts.entrySet()){
			Sequence stem = new Sequence();
			int cnt = mapEntry.getValue();
			Sequence seq = mapEntry.getKey();
			for(int i = 0; i < seq.size(); i++){
				stem.add(stems.get(seq.get(i)));
			}
			if(candidStemCount.containsKey(stem)){
				candidStemCount.put(stem, candidStemCount.get(stem) + cnt);
			}else{
				candidStemCount.put(stem, cnt);
				candidStemToWords.put(stem, new Vector<Sequence>());
			}
			candidStemToWords.get(stem).add(seq);
		}
		
		for(Map.Entry<Sequence, Integer> mapEntry : maxRefCounts.entrySet()){
			Sequence stem = new Sequence();
			int cnt = mapEntry.getValue();
			Sequence seq = mapEntry.getKey();
			for(int i = 0; i < seq.size(); i++){
				stem.add(stems.get(seq.get(i)));
			}
			if(refStemCount.containsKey(stem)){
				refStemCount.put(stem, refStemCount.get(stem) + cnt);
			}else{
				refStemCount.put(stem, cnt);
				refStemToWords.put(stem, new Vector<Sequence>());
			}
			refStemToWords.get(stem).add(seq);
		}
		
		for(Map.Entry<Sequence, Integer> mapEntry : candidStemCount.entrySet()){
			int candidCount = mapEntry.getValue();
			Sequence key = mapEntry.getKey();
			int refCount = 0;
			if(refStemCount.containsKey(key))
				refCount = refStemCount.get(key);
			int matchedCount = Math.min(candidCount, refCount);
			count += matchedCount;
			if(matchedCount > 0){
				int cnt = 0;
				// clear matchedStems from candidate
				Vector<Sequence> v = candidStemToWords.get(key);
				for(int i = 0; i < v.size() && cnt < matchedCount; i++){
					Sequence words = v.get(i);
//					System.err.println(words);
					int bfrCount = candidateCounts.get(words);
					int dif = Math.min(matchedCount - cnt, bfrCount);
					cnt += dif;
					int nxt = bfrCount - dif;
					if(nxt > 0)
						candidateCounts.put(words, nxt);
					else
						candidateCounts.remove(words);
				}
				// clear matchedStems from reference
				cnt = 0;
				v = refStemToWords.get(key);
				for(int i = 0; i < v.size() && cnt < matchedCount; i++){
					Sequence words = v.get(i);
//					System.err.println(words);
					int bfrCount = maxRefCounts.get(words);
					int dif = Math.min(matchedCount - cnt, bfrCount);
					cnt += dif;
					int nxt = bfrCount - dif;
					if(nxt > 0)
						maxRefCounts.put(words, nxt);
					else
						maxRefCounts.remove(words);
				}
			}
		}
		return count;
	}
	
	
}
