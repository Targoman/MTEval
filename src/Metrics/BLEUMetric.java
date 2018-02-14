package Metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BLEUMetric extends EvaluationMetric {

	int totalClipCounts[], totalNgramCounts[];
	int order; // number of ngrams
	int globalR, globalC; // R = sum of best match references length
							// C = sum of candidates length
	double smooth = 1.;
	public BLEUMetric(int n) {
		order = n;
		totalClipCounts = new int[order];
		totalNgramCounts = new int[order];
		globalR = globalC = 0;

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
			int maxOrder) {

		Map<Sequence, Integer> counts = new HashMap<Sequence, Integer>();

		int sz = sequence.size();
		for (int i = 0; i < sz; i++) {
			int jMax = Math.min(sz, i + maxOrder);
			for (int j = i + 1; j <= jMax; j++) {
				Sequence ngram = sequence.subsequence(i, j);
				IncCount(counts, ngram);
			}
		}
		return counts;
	}

	static public Map<Sequence, Integer> getMaxNGramCounts(
			List<Sequence> refrences, int maxOrder) {
		Map<Sequence, Integer> maxCounts = new HashMap<Sequence, Integer>();

		for (Sequence sequence : refrences) {

			Map<Sequence, Integer> counts = getNGramCounts(sequence, maxOrder);
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

	static public int[] clipCount(Map<Sequence, Integer> ngramCounts,
			Map<Sequence, Integer> refNgramCounts, int order) {

		int clipCounts[] = new int[order];
		for (Map.Entry<Sequence, Integer> countsEntry : ngramCounts.entrySet()) {

			Integer countValue = countsEntry.getValue();
			Integer refCountValue = refNgramCounts.get(countsEntry.getKey());
			int len = countsEntry.getKey().size();

			if (refCountValue == null)
				continue;
			else{
				clipCounts[len - 1] += Math.min(countValue, refCountValue);
				
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

	public double addSentence(Sequence candidate, List<Sequence> ref) {

		Map<Sequence, Integer> candidateCounts = getNGramCounts(candidate,
				order);
		Map<Sequence, Integer> maxRefCounts = getMaxNGramCounts(ref, order);

		int matchedNgramCounts[] = clipCount(candidateCounts, maxRefCounts,
				order);

		
		for (int i = 0; i < order; i++) {
			totalClipCounts[i] += matchedNgramCounts[i];
		}
		double sentenceNgramCounts[] = new double[order];
		for (int i = 0; i < order; i++) {
			sentenceNgramCounts[i] = possibleNgramCounts(candidate.size(), i); 
			totalNgramCounts[i] += sentenceNgramCounts[i];
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
		
		double bp = localBrevityPenalty(r, c);
		double p[] = new double[order];
		for(int i = 0; i < order; i++){
			p[i] = getLocalSmoothedPrecision(matchedNgramCounts[i], sentenceNgramCounts[i]);
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
//		System.out.println("P" + n + " ClipCounts : " + totalClipCounts[n] + "  NgramCounts: " + totalNgramCounts[n]);
		 if(totalClipCounts[n] == 0){
			 smooth *= 2.;
			 return 1. / smooth / totalNgramCounts[n];
		 }
		return totalClipCounts[n] / (double) (totalNgramCounts[n]);
//		 else{
//			 return (totalClipCounts[n] + 1) / (double)(totalNgramCounts[n] + 1);
	}
	
	public double getLocalSmoothedPrecision(double clip, double ngram) {
		if(clip > 0 && ngram > 0)
			return clip / ngram;
		 return (clip + 1.) / (double)(ngram + 1.);
	}
	
	public double localBrevityPenalty(double r, double c) {
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
			score += w * Math.log(getPrecision(i));
		score = brevityPenalty() * Math.exp(score);
//		System.out.println(brevityPenalty());
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

}
