package kr.ac.sch.se.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;


public class FindPeaks {

	/*
    public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);

		Peak peak = new Peak();
		Pks pks = new Pks();
		Locs locs = new Locs();

		double[] temp = new double[7335];

		for(int i = 0; i < 7335; ++i) {
			temp[i] = scan.nextDouble();
		}

		parse_inputs(peak, temp, 0.5, 100);
		getPeaksAboveMinPeakHeight(peak, pks, locs);
		removePeaksBelowThreshold(peak, pks, locs);
		removePeaksSeparatedByLessThanMinPeakDistance(peak, pks, locs);
		orderPeaks(peak, pks, locs);
		keepAtMostNpPeaks(peak, pks, locs);

		double[] pksTemp = pks.getPks();
		int[] locsTemp = locs.getLocs();

		System.out.println("pks data");
		for(double d : pksTemp) {
			System.out.println(d);
		}
		System.out.println("\r\nlocs data");
		for(int i : locsTemp) {
			System.out.println(i);
		}
		
		scan.close();
	}*/

    //[X,Ph,Pd,Th,Np,Str,infIdx] = parse_inputs(X,varargin{:});
    public static void parse_inputs(Peak peak, double[] data, double minPeakHeight, long minPeakDistance) {
		/*%#function dspopts.findpeaks*/
        //hopts = uddpvparse('dspopts.findpeaks',varargin{:});
        //Ph  = hopts.MinPeakHeight;
        //Pd  = hopts.MinPeakDistance;
        //Th  = hopts.Threshold;
        //Np  = hopts.NPeaks;
        //Str = hopts.SortStr;
        peak.setX(data);
        peak.setPh(minPeakHeight);
        peak.setPd(minPeakDistance);
        peak.setTh(0);
        peak.setNp(data.length);
        peak.setStr("none");
		/*% Replace Inf by realmax because the diff of two Infs is not a number*/
        //infIdx = isinf(X);
        boolean[] temp = new boolean[data.length];
        for (int i = 0; i < temp.length; ++i) {
            if (!Double.isInfinite(data[i])) temp[i] = false;
            else {
                //X(infIdx) = sign(X(infIdx))*realmax;
                temp[i] = true;
                int sign;
                double X = peak.getX(i);
                if (X > 1) sign = 1;
                else if (X == 0) sign = 0;
                else sign = -1;
                peak.setX(i, sign * Double.MAX_VALUE);
                //infIdx = infIdx & X>0; % Keep only track of +Inf
                if (sign == 1) temp[i] = false;
            }
        }
        peak.setinfldx(temp);
    }

    public static void getPeaksAboveMinPeakHeight(Peak peak, Pks pks, Locs locs) {
        double[] X = peak.getX();

        //if all(isnan(X)),
		/*
		for(int i = 0; i < X.length; ++i) {
			if(Double.isNaN(X[i])) return;
		}
		 */

        //Indx = find(X > Ph);
        ArrayList<Integer> Indx = new ArrayList<Integer>(X.length);
        for (int i = 0; i < X.length; ++i) {
            if (X[i] > peak.getPh()) 
                Indx.add(i);
        }

		/*% Peaks cannot be easily solved by comparing the sample values. Instead, we
		% use first order difference information to identify the peak. A peak
		% happens when the trend change from upward to downward, i.e., a peak is
		% where the difference changed from a streak of positives and zeros to
		% negative. This means that for flat peak we'll keep only the rising
		% edge.*/
        //trend = sign(diff(X));
        ArrayList<Integer> trend = new ArrayList<Integer>(X.length);
        for (int i = 1; i < X.length; ++i) {
            double temp = X[i] - X[i - 1];
            if (temp > 0) trend.add(1);
            else if (temp < 0) trend.add(-1);
            else trend.add(0);
        }

        //idx = find(trend==0); % Find flats
        //N = length(trend);
        ArrayList<Integer> idx = new ArrayList<Integer>(trend.size());
        int N = trend.size();
        for (int i = 0; i < N; ++i) {
            if (trend.get(i) == 0) idx.add(i);
        }

        //for i=length(idx):-1:1,
        for (int i = idx.size() - 1; i >= 0; --i) {
            int temp = idx.get(i);
            if (trend.get(Math.min(temp, N)) >= 0) trend.set(temp, 1);
            else trend.set(temp, -1);
        }

        //idx  = find(diff(trend)==-2)+1;  % Get all the peaks
        idx.clear();
        for (int i = 1; i < trend.size(); ++i) {
            int temp = trend.get(i) - trend.get(i - 1);
            if (temp == -2) idx.add(i);
        }

        //locs = intersect(Indx,idx);  % Keep peaks above MinPeakHeight
        ArrayList<Integer> locsList;
        if (Indx.size() > idx.size()) {
            locsList = new ArrayList<Integer>(idx.size());
            for (int i = 0; i < idx.size(); ++i) {
                if (Indx.contains(idx.get(i))) locsList.add(idx.get(i));
            }
        } else {
            locsList = new ArrayList<Integer>(Indx.size());
            for (int i = 0; i < Indx.size(); ++i) {
                if (idx.contains(Indx.get(i))) locsList.add(Indx.get(i));
            }
        }
        locs.setLocs(locsList);

        //pks  = X(locs);
        double[] pksArray = new double[locsList.size()];
        for (int i = 0; i < locsList.size(); ++i) {
            pksArray[i] = X[locsList.get(i)];
        }
        pks.setPks(pksArray);
    }

    public static void removePeaksBelowThreshold(Peak _peak, Pks _pks, Locs _locs) {
        double[] X = _peak.getX();
        boolean[] infldx = _peak.getinfldx();
        double[] pks = _pks.getPks();
        int[] locs = _locs.getLocs();
        double Th = _peak.getTh();

        //for i = 1:length(pks),
        ArrayList<Integer> idelete = new ArrayList<Integer>(pks.length);
        for (int i = 0; i < pks.length; ++i) {
            double delta = Math.min(pks[i] - (X[locs[i] - 1]), pks[i] - (X[locs[i] + 1]));
            if (delta < Th) idelete.add(i);
        }

        //locs(idelete) = [];
        if (!idelete.isEmpty()) {
            ArrayList<Integer> locsList = new ArrayList<Integer>(locs.length);
            for (int i = 0; i < idelete.size(); ++i) {
                if (locsList.contains(idelete.get(i))) locsList.remove(i);
            }
            _locs.setLocs(locsList);
        }
        locs = _locs.getLocs();

        //X(infIdx) = Inf;  		% Restore +Inf
        for (int i = 0; i < X.length; ++i) {
            if (infldx[i]) _peak.setX(i, Double.POSITIVE_INFINITY);
        }
        X = _peak.getX();

        //locs = union(locs,find(infIdx)); % Make sure we find peaks like [realmax Inf realmax]
        ArrayList<Integer> tempList = _locs.getLocsList();
        for (int i = 0; i < infldx.length; ++i) {
            if (infldx[i]) tempList.add(i);
        }
        ArrayList<Integer> locsList = new ArrayList<Integer>(new HashSet<Integer>(tempList));
        Collections.sort(locsList);
        _locs.setLocs(locsList);
        locs = _locs.getLocs();

        //pks  = X(locs);
        pks = new double[locsList.size()];
        for (int i = 0; i < locsList.size(); ++i) {
            pks[i] = X[locsList.get(i)];
        }
        _pks.setPks(pks);
    }

    public static void removePeaksSeparatedByLessThanMinPeakDistance(Peak _peak, Pks _pks, Locs _locs) {
		/*% Start with the larger peaks to make sure we don't accidentally keep a
		% small peak and remove a large peak in its neighborhood. */
        ArrayList<Double> pks = _pks.getPksList();
        int[] locs = _locs.getLocs();
        long Pd = _peak.getPd();

        //if isempty(pks) || Pd==1,
        if (pks.isEmpty() || Pd == 1) return;

		/*% Order peaks from large to small*/
        //[pks, idx] = sort(pks,'descend');
        ArrayList<Double> pksTemp = new ArrayList<Double>(pks);
        Collections.sort(pks, Collections.reverseOrder());
        int[] idx = new int[pksTemp.size()];
        for (int i = 0; i < idx.length; ++i) {
            idx[i] = pksTemp.indexOf(pks.get(i));
        }
        _pks.setPks(pks);

        //locs = locs(idx);
        int[] newLocs = new int[locs.length];
        for (int i = 0; i < locs.length; ++i) {
            newLocs[i] = locs[idx[i]];
        }
        _locs.setLocs(newLocs);

        //idelete = ones(size(locs))<0;
        locs = _locs.getLocs();
        boolean[] idelete = new boolean[locs.length];
        for (int i = 0; i < idelete.length; ++i) {
            idelete[i] = false;
        }

        //for i = 1:length(locs),
        for (int i = 0; i < locs.length; ++i) {
            //if ~idelete(i),
            if (!idelete[i]) {
				/*% If the peak is not in the neighborhood of a larger peak, find
		        % secondary peaks to eliminate.*/
                //locs>=locs(i)-Pd
                boolean[] temp1 = new boolean[idelete.length];
                //locs<=locs(i)+Pd
                boolean[] temp2 = new boolean[idelete.length];
                for (int j = 0; j < idelete.length; ++j) {
                    if (locs[j] >= locs[i] - Pd) temp1[j] = true;
                    else temp1[j] = false;
                    if (locs[j] <= locs[i] + Pd) temp2[j] = true;
                    else temp2[j] = false;
                }
                //idelete = idelete | (locs>=locs(i)-Pd)&(locs<=locs(i)+Pd);
                for (int j = 0; j < idelete.length; ++j) {
                    idelete[j] = idelete[j] | temp1[j] & temp2[j];
                }
                //idelete(i) = 0; % Keep current peak
                idelete[i] = false;
            }
        }

        //pks(idelete) = [];
        double[] pksArray = _pks.getPks();
        ArrayList<Double> newPks = new ArrayList<Double>(pksArray.length);
        for (int i = 0; i < pksArray.length; ++i) {
            if (!idelete[i]) newPks.add(pksArray[i]);
        }
        _pks.setPks(newPks);

        //locs(idelete) = [];
        locs = _locs.getLocs();
        ArrayList<Integer> newLocsList = new ArrayList<Integer>(locs.length);
        for (int i = 0; i < locs.length; ++i) {
            if (!idelete[i]) newLocsList.add(locs[i]);
        }
        _locs.setLocs(newLocsList);
    }

    public static void orderPeaks(Peak _peak, Pks _pks, Locs _locs) {
        ArrayList<Double> pks = _pks.getPksList();
        ArrayList<Integer> locs = _locs.getLocsList();
        String Str = _peak.getStr();

        //if isempty(pks), return; end
        if (pks.isEmpty()) return;

        //if strcmp(Str,'none')
        //if (!TextUtils.isEmpty(Str) && Str.equals("none")) {
        if (Str.equals("none")) {
            //[locs idx] = sort(locs);
            ArrayList<Integer> locsTemp = new ArrayList<Integer>(locs);
            Collections.sort(locs);
            int[] idx = new int[locsTemp.size()];
            for (int i = 0; i < idx.length; ++i) {
                idx[i] = locsTemp.indexOf(locs.get(i));
            }
            _locs.setLocs(locs);

            //pks = pks(idx);
            double[] newPks = new double[pks.size()];
            for (int i = 0; i < pks.size(); ++i) {
                newPks[i] = pks.get(idx[i]);
            }
            _pks.setPks(newPks);
        }
        //else
        //[pks,s]  = sort(pks,Str);
        //locs = locs(s);
    }

    public static void keepAtMostNpPeaks(Peak _peak, Pks _pks, Locs _locs) {
        double[] pks = _pks.getPks();
        int[] locs = _locs.getLocs();
        int Np = _peak.getNp();

        //if length(pks)>Np,
        if (pks.length > Np) {
            //locs = locs(1:Np);
            int[] newLocs = new int[Np];
            for (int i = 0; i < Np; ++i) {
                newLocs[i] = locs[i];
            }
            _locs.setLocs(newLocs);

            //pks  = pks(1:Np);
            double[] newPks = new double[Np];
            for (int i = 0; i < Np; ++i) {
                newPks[i] = pks[i];
            }
            _pks.setPks(newPks);
        }
    }

}
