package ModifiedBLEU;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import Tools.*;
import Metrics.*;

public class ModifiedBLEU extends EvaluationMetric {

	int totalClipCounts[], totalNgramCounts[];
	int order; // number of ngrams
	int globalR, globalC; // R = sum of best match references length
							// C = sum of candidates length

	double StemWeight = 0.5;
	double WordNetWeight = 0.9;

	double smooth = 1.;
	SemanticMatcher syn;
	public ModifiedBLEU(int n, String WORDNET_ADDRESS) {
		order = n;
		totalClipCounts = new int[order];
		totalNgramCounts = new int[order];
		globalR = globalC = 0;
		SemanticMatcher.init(WORDNET_ADDRESS);
	}

	static private void IncCount(Map<Sequence, Integer> counts, Sequence s) {
		Integer cnt = counts.get(s);
		if (cnt == null) {
			counts.put(s, 1);
		} else {
			counts.put(s, cnt + 1);
		}
	}

	static public Map<Sequence, Integer> getNGramCounts(Sequence sequence,
			int order) {

		Map<Sequence, Integer> counts = new HashMap<Sequence, Integer>();

		int sz = sequence.size();
		for (int i = 0; i <= sz - order; i++) {
			// int jMax = Math.min(sz, i + order);
			// for (int j = i + 1; j <= jMax; j++) {
			Sequence ngram = sequence.subsequence(i, i + order);
			IncCount(counts, ngram);
			// }
		}
		return counts;
	}

	static public Map<Sequence, Integer> getMaxNGramCounts(
			List<Sequence> refrences, int order) {
		Map<Sequence, Integer> maxCounts = new HashMap<Sequence, Integer>();

		for (Sequence sequence : refrences) {

			Map<Sequence, Integer> counts = getNGramCounts(sequence, order);
			for (Map.Entry<Sequence, Integer> countsEntry : counts.entrySet()) {

				Integer countValue = countsEntry.getValue();
				Integer maxCountValue = maxCounts.get(countsEntry.getKey());

				if (maxCountValue == null
						|| maxCountValue.compareTo(countValue) < 0) {
					maxCounts.put(countsEntry.getKey(), countValue);
				}
			}
		}
		return maxCounts;
	}

	static public int clipCount(Map<Sequence, Integer> ngramCounts,
			Map<Sequence, Integer> refNgramCounts) {

		int clipCounts = 0;
		Sequence before = null;
		for(Iterator<Entry<Sequence, Integer>> countIter = ngramCounts.entrySet().iterator(); countIter.hasNext();){
//		for (Map.Entry<Sequence, Integer> countsEntry : ngramCounts.entrySet()) {
			Entry<Sequence, Integer> countsEntry = countIter.next();
			Integer countValue = countsEntry.getValue();
			Integer refCountValue = refNgramCounts.get(countsEntry.getKey());
//			int len = countsEntry.getKey().size();
//			System.out.println(countsEntry.getKey());
			if (refCountValue == null)
				continue;
			else {
				int x = Math.min(countValue, refCountValue);
				clipCounts += x;
				if(refCountValue - x > 0)
				refNgramCounts.put(countsEntry.getKey(), refCountValue - x);
				else
					refNgramCounts.remove(countsEntry.getKey());
				if(countValue - x > 0)
					ngramCounts.put(countsEntry.getKey(),countValue - x);
				else
					countIter.remove();
				
			}
		}

		return clipCounts;
	}

	public static int bestMatchLength(int cLength, int rLength[]) {
		int res = rLength[0];
		for (int i = 0; i < rLength.length; i++) {
			if (Math.abs(cLength - res) > Math.abs(cLength - rLength[i])) {
				res = rLength[i];
			}
		}
		return res;
	}

	public static int possibleNgramCounts(int len, int order) {
		int res = len - order;
		if (res > 0)
			return res;
		return 0;
	}

	/****/
	public void printMap(Map<Sequence, Integer> mp){
		for(Map.Entry<Sequence, Integer> mpEntry : mp.entrySet()){
			System.out.println(mpEntry.getKey() + " " + mpEntry.getValue());
		}
	}
	public double addSentence(Sequence candidate, List<Sequence> ref) {

		StemMatcher stem = new StemMatcher();
		double sentenceClipCount[] = new double[order];
		stem.addWords(candidate);
		
		for(int i = 0; i < ref.size(); i++){
			stem.addWords(ref.get(i));
		}
		syn = new SemanticMatcher();
		
		for (int i = 0; i < order; i++) {
			
			Map<Sequence, Integer> candidateCounts = getNGramCounts(candidate,
					i + 1);
			Map<Sequence, Integer> maxRefCounts = getMaxNGramCounts(ref, i + 1);
			if(i == 0)
					syn.addSyns(candidateCounts, maxRefCounts);
			
			//Exact Match
			
			int matchedNgramCounts = clipCount(candidateCounts, maxRefCounts);

			totalClipCounts[i] += matchedNgramCounts;
			sentenceClipCount[i] += matchedNgramCounts;
			
			// Stem Match
			
			int matchedStems = stem.matchStems(candidateCounts, maxRefCounts, i + 1);
			
			totalClipCounts[i] += matchedStems;
			sentenceClipCount[i] += matchedStems;
			
			// Synonym match
			
			int matchSyns = 0;
			
			matchSyns = syn.matchSemantics(candidateCounts, maxRefCounts, i + 1 );
			totalClipCounts[i] += matchSyns;
			sentenceClipCount[i] += matchSyns;
		}
		double sentenceNgramCounts[] = new double[order];
		for (int i = 0; i < order; i++) {
			int x = possibleNgramCounts(candidate.size(), i);
			totalNgramCounts[i] += x;
			sentenceNgramCounts[i] = x;
		}

		int numOfRefs = ref.size();
		int rlens[] = new int[numOfRefs];
		for (int i = 0; i < numOfRefs; i++) {
			rlens[i] = ref.get(i).size();
		}
		int r = bestMatchLength(candidate.size(), rlens);
		int c = candidate.size();
		globalC += c;
		globalR += r;

		double bp = brevityPenalty(r, c);
		double p[] = new double[order];
		for(int i = 0; i < order; i++){
			p[i] = getSmoothedPrecision(sentenceClipCount[i], sentenceNgramCounts[i]);
		}
		
		double score = 0;
		double w = 1. / order;
		for (int i = 0; i < order; i++)
			score += w * Math.log(p[i]);
		score = bp * Math.exp(score);
		
		return score;

		
	}

	public double getPrecision(int n) {
		return totalClipCounts[n] / (double) (totalNgramCounts[n]);
	}

	public double getSmoothedPrecision(int n) {
		System.out.println("P" + n + " ClipCounts : " + totalClipCounts[n]
				+ "  NgramCounts: " + totalNgramCounts[n]);
		if (totalClipCounts[n] == 0) {
			smooth *= 2.;
			return 1. / smooth / totalNgramCounts[n];
		}
		return totalClipCounts[n] / (double) (totalNgramCounts[n]);
		// else{
		// return (totalClipCounts[n] + 1) / (double)(totalNgramCounts[n] + 1);
	}

	public double getSmoothedPrecision(double clip, double ngram) {
		if(clip > 0 && ngram > 0)
			return clip / ngram;
		 return (clip + 1.) / (double)(ngram + 1.);
	}
	
	public double brevityPenalty(double r, double c) {
		if (c > r)
			return 1.;
		return Math.exp(1. - (double) r / (double) c);
	}
	
	public double brevityPenalty() {
		if (globalC > globalR)
			return 1.;
		return Math.exp(1. - (double) globalR / (double) globalC);
	}

	public double getScore() {
		double score = 0;
		double w = 1. / order;
		for (int i = 0; i < order; i++)
			score += w * Math.log(getSmoothedPrecision(i));
		score = brevityPenalty() * Math.exp(score);
		return score;

	}

	public double testScore() {
		double score = 0;
		double w = 1. / order;
		for (int i = 0; i < order; i++)
			score += w * getPrecision(i);
		// score = brevityPenalty() * Math.exp(score);
		return score;

	}

	public static void main(String args[]) {
	}
}
