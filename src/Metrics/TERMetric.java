package Metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;


public class TERMetric extends EvaluationMetric{

	private static final double INF = 99999999;
	private static double BEAM_WIDTH = 20;
	int totalNumOfEdits;
	double totalRefLen;
	static int MAX_SHIFT_SIZE = 10;
	int MAX_SHIFT_DIST = 50;
	// used for minimum-Edit-Distance
	int dp[][] = new int[100][100];
	int path[][] = new int[100][100];

	public TERMetric(int b, int sh) {
		totalNumOfEdits = 0;
		totalRefLen = 0;
		BEAM_WIDTH = b;
		MAX_SHIFT_DIST = sh;
	}

	public double addSentence(Sequence hyp, List<Sequence> refs) {

		int minRefEdits = -1;
		for (int i = 0; i < refs.size(); i++) {
			int edits = calcMinEdits(hyp, refs.get(i));
			if (minRefEdits == -1 || minRefEdits > edits)
				minRefEdits = edits;
		}

		double avgRefLen = getAvgRefLen(refs);
		totalNumOfEdits += minRefEdits;
		totalRefLen += avgRefLen;

		return 0;

	}

	public double getScore() {

		return (double) totalNumOfEdits / totalRefLen;
	}

//	public int min_Edit_Distance(Sequence hyp, Sequence ref) {
//		int hlen = hyp.size();
//		int rlen = ref.size();
//		if (hlen >= dp.length || rlen >= dp.length) {
//			int m = Math.max(hlen, rlen) + 20;
//			dp = new int[m][m];
//			path = new int[m][m];
//		}
//		for (int i = 0; i <= hlen; i++)
//			for (int j = 0; j <= rlen; j++)
//				dp[i][j] = -1;
//		dp[0][0] = 0;
//		for (int i = 0; i <= hlen; i++) {
//			for (int j = 0; j <= rlen; j++) {
//				if (dp[i][j] != -1) {
//					if (i < hlen && j < rlen) {
//						if (hyp.get(i).equals(ref.get(j))) {
//							// match
//							if (dp[i + 1][j + 1] == -1
//									|| dp[i + 1][j + 1] > dp[i][j]) {
//								dp[i + 1][j + 1] = dp[i][j];
//								path[i + 1][j + 1] = 0;
//							}
//						} else {
//							// substitute
//							if (dp[i + 1][j + 1] == -1
//									|| dp[i + 1][j + 1] > dp[i][j] + 1) {
//								dp[i + 1][j + 1] = dp[i][j] + 1;
//								path[i + 1][j + 1] = 1;
//							}
//						}
//					}
//					if (i < hlen) {
//						// delete
//						if (dp[i + 1][j] == -1 || dp[i + 1][j] > dp[i][j] + 1) {
//							dp[i + 1][j] = dp[i][j] + 1;
//							path[i + 1][j] = 2;
//						}
//					}
//
//					if (j < rlen) {
//						// insert
//						if (dp[i][j + 1] == -1 || dp[i][j + 1] > dp[i][j] + 1) {
//							dp[i][j + 1] = dp[i][j] + 1;
//							path[i][j + 1] = 3;
//						}
//					}
//				}
//			}
//		}
//		return dp[hlen][rlen];
//	}

	public int min_Edit_Distance(Sequence hyp, Sequence ref) {
		double current_best = INF;
		double last_best = INF;
		int first_good = 0;
		int current_first_good = 0;
		int last_good = -1;
		int cur_last_good = 0;
		int last_peak = 0;
		int cur_last_peak = 0;
		int cost, icost, dcost;
		int score;
		
		int hlen = hyp.size();
		int rlen = ref.size();
		if (hlen >= dp.length || rlen >= dp.length) {
			int m = Math.max(hlen, rlen) + 20;
			dp = new int[m][m];
			path = new int[m][m];
		}
		
		for (int i = 0; i <= rlen; i++)
			for (int j = 0; j <= hlen; j++)
				dp[i][j] = -1;
		dp[0][0] = 0;
		
		for (int j=0; j <= hlen; j++) {
		      last_best = current_best;
		      current_best = INF;
			    
		      first_good = current_first_good;
		      current_first_good = -1;
			    
		      last_good = cur_last_good;
		      cur_last_good = -1;
			    
		      last_peak = cur_last_peak;
		      cur_last_peak = 0;
			    
		      for (int i=first_good; i <= rlen; i++) {
				if (i > last_good)
		          break;
				if (dp[i][j] < 0) 
		          continue;
				score = dp[i][j];

				if ((j < hlen) && (score > last_best+BEAM_WIDTH))
		          continue;

				if (current_first_good == -1)
		          current_first_good = i ;
				    
				if ((i < rlen) && (j < hlen)) {
		           
		          
		            if (ref.get(i).equals(hyp.get(j))) {
		              cost = score;
		              if ((dp[i+1][j+1] == -1) || (cost < dp[i+1][j+1])) {
		                dp[i+1][j+1] = cost;
		                path[i+1][j+1] = 0;
		              }
		              if (cost < current_best)
		                current_best = cost;
		              
		              if (current_best == cost)
		                cur_last_peak = i+1;
		            } else {
		              cost = 1 + score;
		              if ((dp[i+1][j+1] <0) || (cost < dp[i+1][j+1])) {
		                dp[i+1][j+1] = cost;
		                path[i+1][j+1] = 1;
		                if (cost < current_best)
		                  current_best = cost;
		                if (current_best == cost)
		                  cur_last_peak = i+1 ;
		              }
		          }
				}
					
				cur_last_good = i+1;
					
				if  (j < hlen) {
		          icost = score+1;
		          if ((dp[i][j+1] < 0) || (dp[i][j+1] > icost)) {
					dp[i][j+1] = icost;
					path[i][j+1] = 2;
					if (( cur_last_peak <  i) && ( current_best ==  icost))
		              cur_last_peak = i;
		          }
				}		

				if  (i < rlen) {
		          dcost =  score + 1;
		          if ((dp[ i+1][ j]<0.0) || ( dp[i+1][j] > dcost)) {
					dp[i+1][j] = dcost;
					path[i+1][j] = 3;
					if (i >= last_good)
		              last_good = i + 1 ;
		          }		
				}
		      }
			}
			
		return dp[rlen][hlen];
	}
	
	public Vector<Integer> getPath(Sequence hyp, Sequence ref) {
//		int i = hyp.size(), j = ref.size();
		int i = ref.size(), j = hyp.size();
		Vector<Integer> res = new Vector();
//		int nxt[][] = { { -1, -1 }, { -1, -1 }, { -1, 0 }, { 0, -1 } };
		int nxt[][] = { { -1, -1 }, { -1, -1 }, { 0, -1 }, { -1, 0 } };
		while (i > 0 || j > 0) {

			res.add(0, path[i][j]);
			int x = path[i][j];
			i += nxt[x][0];
			j += nxt[x][1];
		}
		return res;
	}

	public double getAvgRefLen(List<Sequence> refs) {
		double res = 0;
		for (int i = 0; i < refs.size(); i++)
			res += refs.get(i).size();
		res /= (double) refs.size();
		return res;
	}

	public int calcMinEdits(Sequence hyp, Sequence ref) {
		// TODO Auto-generated method stub
		Map<Sequence, Set> matchedRefSubstrs = BuildWordMatches(hyp, ref);
		Sequence currentHyp = hyp;
		int numOfShifts = 0, numOfEdits = min_Edit_Distance(currentHyp, ref);
		Vector<Integer> bestPath = getPath(currentHyp, ref);
		while (true) {

			Object shiftRes[] = calcBestShift(currentHyp, ref,
					matchedRefSubstrs, bestPath, numOfEdits);
			Sequence next = (Sequence) shiftRes[0];
			// System.out.println(next);
			// System.out.println(next);
			int edits = ((Integer) shiftRes[1]).intValue();
			// System.out.println(shiftRes[2]);
			if (next == null)
				break;
			bestPath = (Vector<Integer>) shiftRes[2];
			numOfEdits = edits;
			numOfShifts++;
			currentHyp = (Sequence) next.clone();

		}

		numOfEdits += numOfShifts;
		return numOfEdits;
	}

	private static Map<Sequence, Set> BuildWordMatches(Sequence hyp,
			Sequence ref) {
		Set<String> hwhash = new HashSet<String>();
		for (int i = 0; i < hyp.size(); i++) {
			hwhash.add(hyp.get(i));
		}
		boolean[] cor = new boolean[ref.size()];
		for (int i = 0; i < ref.size(); i++) {
			if (hwhash.contains(ref.get(i))) {
				cor[i] = true;
			} else {
				cor[i] = false;
			}
		}

		HashMap<Sequence, Set> to_return = new HashMap();
		for (int start = 0; start < ref.size(); start++) {
			if (cor[start]) {
				for (int end = start; ((end < ref.size())
						&& (end - start <= MAX_SHIFT_SIZE) && (cor[end])); end++) {
					Sequence topush = new Sequence(ref.subList(start, end + 1));
					if (to_return.containsKey(topush)) {
						Set vals = (Set) to_return.get(topush);
						vals.add(new Integer(start));
					} else {
						Set vals = new TreeSet();
						vals.add(new Integer(start));
						to_return.put(topush, vals);
					}
				}
			}
		}
		return to_return;
	}

	public Object[] calcBestShift(Sequence hyp, Sequence ref,
			Map<Sequence, Set> matchedRefSubstrs, Vector<Integer> bestPath,
			int curEdits) {
		Object res[] = new Object[3];
		/* Arrays that records which hyp and ref words are currently wrong */
		boolean[] herr = new boolean[hyp.size()];
		boolean[] rerr = new boolean[ref.size()];
		/* Array that records the alignment between ref and hyp */
		int[] ralign = new int[ref.size()];
		FindAlignErr(bestPath, herr, rerr, ralign);

		TERShift[][] possShifts = gatherPossShifts(hyp, ref, matchedRefSubstrs,
				herr, rerr, ralign);

		Sequence bestShift = null;
		int minEdit = curEdits;
		int curShiftCost = 0;
		Vector<Integer> newbestPath = null;

		for(int i = possShifts.length - 1; i >= 0; i--)  {
			
//			 double curfix = curEdits - 
//				(curShiftCost + minEdit);
//		      double maxfix = (2 * (1 + i));
//			    
//		      if ((curfix > maxfix) || 
//		          ((curShiftCost != 0) && (curfix == maxfix))) {
//				break;
//		      }
			
			for (int j = 0; j < possShifts[i].length; j++) {
//				curfix = curEdits - 
//		          (curShiftCost + minEdit);
//		        if ((curfix > maxfix) || 
//				    ((curShiftCost != 0) && (curfix == maxfix))) {
//		          break;
//				}
				TERShift curShift = possShifts[i][j];
				Sequence next = performShift(hyp, curShift);

				int edits = min_Edit_Distance(next, ref);
				
				double gain = (minEdit + curShiftCost) 
		          - (edits + 1);
				
				// System.out.println(next + " " + edits);
				if (gain > 0 || ( gain == 0 && curShiftCost == 0)) {

					minEdit = edits;
					bestShift = next;
					newbestPath = getPath(next, ref);
					curShiftCost = 1;
				}

			}

		}
		
		res[0] = bestShift;
		res[1] = new Integer(minEdit);
		res[2] = newbestPath;
		return res;

	}

	private TERShift[][] gatherPossShifts(Sequence hyp, Sequence ref,
			Map<Sequence, Set> matchedRefSubstrs, boolean[] herr,
			boolean[] rerr, int[] ralign) {
		// TODO Auto-generated method stub

		ArrayList[] allshifts = new ArrayList[MAX_SHIFT_SIZE + 1];
		for (int i = 0; i < allshifts.length; i++)
			allshifts[i] = new ArrayList();
		int hypLen = hyp.size();
		int refLen = ref.size();

		for (int start = 0; start < hypLen; start++) {
			if (!matchedRefSubstrs.containsKey(hyp
					.subsequence(start, start + 1)))
				continue;

		      boolean ok = false;
		      Iterator mti = ((Set) matchedRefSubstrs.get(hyp.subList(start, start+1))).iterator();
		      while (mti.hasNext() && (! ok)) {
				int moveto = ((Integer) mti.next()).intValue();
				if ((start != ralign[moveto]) &&
				    (ralign[moveto] - start <= MAX_SHIFT_DIST) &&
				    ((start - ralign[moveto] - 1) <= MAX_SHIFT_DIST)) 
		          ok = true;		
		      }
		      if (! ok) continue;

		      
			for (int end = start; (ok && (end < hypLen) && (end < start
					+ MAX_SHIFT_SIZE)); end++) {

				/* check if cand is good if so, add it */
				Sequence cand = hyp.subsequence(start, end + 1);
				ok = false;	
				if (!(matchedRefSubstrs.containsKey(cand)))
					break;
				// System.out.println(cand);
				boolean any_herr = false;
				for (int i = 0; (i <= end - start) && (!any_herr); i++) {
					if (herr[start + i])
						any_herr = true;
				}

				if (any_herr == false) {

					ok = true;
					continue;
				}
				boolean mark[] = new boolean[hyp.size() + 1];
				mark[start] = true;
				Iterator shiftto = matchedRefSubstrs.get(cand).iterator();
				while (shiftto.hasNext()) {

					int moveto = ((Integer) shiftto.next()).intValue();
					if (! ((ralign[moveto] != start) &&
			                 ((ralign[moveto] < start) || (ralign[moveto] > end)) &&
			                 ((ralign[moveto] - start) <= MAX_SHIFT_DIST) &&
			                 ((start - ralign[moveto]) <= MAX_SHIFT_DIST)))
						continue;
//					if(!((ralign[moveto] < start) || (ralign[moveto] > end)))
//						continue;
					ok = true;
					boolean any_rerr = false;
					for (int i = 0; (i <= end - start) && (!any_rerr); i++) {
						if (rerr[moveto + i])
							any_rerr = true;
					}
					if (!any_rerr)
						continue;

					for (int roff = -1; roff <= (end - start); roff++) {
						TERShift shift = null;
						if (roff == -1 && moveto == 0) {
							if (!mark[0]) {
								shift = new TERShift(start, end, 0);
								mark[0] = true;
							}
						} else 
							if ((start != ralign[moveto+roff]) &&
							    ((roff == 0) || 
									     (ralign[moveto+roff] != ralign[moveto]))) 
							{
							int newLoc = ralign[moveto + roff] + 1;
//							if (!mark[newLoc]
//									&& newLoc - start <= MAX_SHIFT_DIST
//									&& start - newLoc <= MAX_SHIFT_DIST) {
								shift = new TERShift(start, end, newLoc);
								mark[newLoc] = true;

//							}
						}
						if (shift != null)
							allshifts[end - start].add(shift);
					}

				}

			}
		}

		TERShift[][] res = new TERShift[allshifts.length][];
		for (int i = 0; i < allshifts.length; i++) {
			res[i] = (TERShift[]) allshifts[i].toArray(new TERShift[0]);
		}
		return res;
	}

	public Sequence performShift(Sequence hyp, TERShift shift) {
		// TODO Auto-generated method stub
		int start = shift.start;
		int end = shift.end;
		int moveto = shift.moveto;
		Sequence shiftedHyp = (Sequence) hyp.clone();
		int c = 0;
		if (moveto <= start) {
			for (int i = 0; i < moveto; i++)
				shiftedHyp.set(c++, hyp.get(i));
			for (int i = start; i <= end; i++)
				shiftedHyp.set(c++, hyp.get(i));
			for (int i = moveto; i <= start - 1; i++)
				shiftedHyp.set(c++, hyp.get(i));
			for (int i = end + 1; i < hyp.size(); i++)
				shiftedHyp.set(c++, hyp.get(i));
		} else if (moveto > end + 1) {
			for (int i = 0; i <= start - 1; i++)
				shiftedHyp.set(c++, hyp.get(i));
			for (int i = end + 1; i < moveto; i++)
				shiftedHyp.set(c++, hyp.get(i));
			for (int i = start; i <= end; i++)
				shiftedHyp.set(c++, hyp.get(i));
			for (int i = moveto; i < hyp.size(); i++)
				shiftedHyp.set(c++, hyp.get(i));
		} else {
			// we are moving inside of ourselves
			for (int i = 0; i <= start - 1; i++)
				shiftedHyp.set(c++, hyp.get(i));
			for (int i = end + 1; (i < hyp.size())
					&& (i <= (end + (moveto - start - 1))); i++)
				shiftedHyp.set(c++, hyp.get(i));
			for (int i = start; i <= end; i++)
				shiftedHyp.set(c++, hyp.get(i));
			for (int i = (end + (moveto - start)); i < hyp.size(); i++)
				shiftedHyp.set(c++, hyp.get(i));
		}
		return shiftedHyp;
	}

	public static void FindAlignErr(Vector<Integer> path, boolean[] herr,
			boolean[] rerr, int[] ralign) {
		int hpos = -1;
		int rpos = -1;
		for (int i = 0; i < path.size(); i++) {
			int sym = path.get(i);
			if (sym == 0) {
				hpos++;
				rpos++;
				herr[hpos] = false;
				rerr[rpos] = false;
				ralign[rpos] = hpos;
			} else if (sym == 1) {
				hpos++;
				rpos++;
				herr[hpos] = true;
				rerr[rpos] = true;
				ralign[rpos] = hpos;
			} else if (sym == 2) {
				hpos++;
				herr[hpos] = true;
			} else if (sym == 3) {
				rpos++;
				rerr[rpos] = true;
				ralign[rpos] = hpos;
			} else {
				System.err.print("Error!  Invalid mini align sequence " + sym
						+ " at pos " + i + "\n");
				System.exit(-1);
			}
		}
	}

}
